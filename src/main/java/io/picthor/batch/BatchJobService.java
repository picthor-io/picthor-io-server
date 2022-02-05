package io.picthor.batch;

import io.picthor.batch.processor.DeletedFilesScannerProcessor;
import io.picthor.batch.processor.NewFilesScannerProcessor;
import io.picthor.data.dao.BatchJobDao;
import io.picthor.data.dao.BatchJobItemDao;
import io.picthor.data.dao.DirectoryDao;
import io.picthor.data.entity.BatchJob;
import io.picthor.data.entity.BatchJobItem;
import io.picthor.data.entity.Directory;
import io.picthor.services.JobCounterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;


@Service
@Slf4j
public class BatchJobService {

    private final BatchJobDao batchJobDao;

    private final BatchJobItemDao batchJobItemDao;

    private final Scheduler scheduler;

    private final NewFilesScannerProcessor newFilesScannerProcessor;

    private final DeletedFilesScannerProcessor deletedFilesScannerProcessor;

    private final JobCounterService jobCounterService;

    private final DirectoryDao directoryDao;

    public BatchJobService(
            BatchJobDao batchJobDao,
            BatchJobItemDao batchJobItemDao,
            Scheduler scheduler,
            NewFilesScannerProcessor newFilesScannerProcessor,
            DeletedFilesScannerProcessor deletedFilesScannerProcessor, JobCounterService jobCounterService, DirectoryDao directoryDao) {
        this.batchJobDao = batchJobDao;
        this.batchJobItemDao = batchJobItemDao;
        this.scheduler = scheduler;
        this.newFilesScannerProcessor = newFilesScannerProcessor;
        this.deletedFilesScannerProcessor = deletedFilesScannerProcessor;
        this.jobCounterService = jobCounterService;
        this.directoryDao = directoryDao;
    }

    public void deleteJob(BatchJob batchJob) throws BatchProcessingException {
        log.info("Deleting batch job: {}", batchJob.getId());
        List<BatchJobItem> items = batchJobItemDao.findByJobId(batchJob.getId());
        unScheduleJobItems(items);
        switch (batchJob.getType()) {
            case NEW_FILES_SCANNER -> newFilesScannerProcessor.cleanup(batchJob);
            case DELETED_FILES_SCANNER -> deletedFilesScannerProcessor.cleanup(batchJob);
        }
        batchJob.setState(BatchJob.State.ARCHIVED);
        batchJobDao.persist(batchJob);
    }

    public void removeExpired(LocalDateTime start, LocalDateTime end) throws BatchProcessingException {
        List<BatchJob> batchJobs = batchJobDao.findAllByCreateDateInterval(start, end);
        for (BatchJob batchJob : batchJobs) {
            deleteJob(batchJob);
            batchJobDao.remove(batchJob);
        }
    }

    public BatchJobItem processJobItem(BatchJobItem item) {
        if (item.getState() != BatchJobItem.State.NEW) {
            log.error("JOB: {} ITEM: {} Cannot process batch job item with state: {}, must be: NEW", item.getBatchJobId(), item.getId(), item.getState());
            return null;
        }

        if (item.getBatchJob() == null) {
            item.setBatchJob(batchJobDao.findById(item.getBatchJobId()));
        }

        StopWatch sw = new StopWatch();
        sw.start();
        try {
            item.setState(BatchJobItem.State.PROCESSING);
            switch (item.getBatchJob().getType()) {
                case NEW_FILES_SCANNER -> newFilesScannerProcessor.processItem(item);
                case DELETED_FILES_SCANNER -> deletedFilesScannerProcessor.processItem(item);
            }
        } catch (Exception e) {
            log.error("JOB: " + item.getBatchJobId() + " ITEM: " + item.getId() + " Failed to process batch job item", e);
            item.setState(BatchJobItem.State.ERROR);
            item.setError(e.getMessage());
        } finally {
            sw.stop();
            item.setDuration(sw.getTotalTimeMillis());
            item.setState(BatchJobItem.State.PROCESSED);
            log.info("JOB: {} ITEM: {} Processed item in: {} ms", item.getBatchJobId(), item.getId(), DurationFormatUtils.formatDurationHMS(item.getDuration()));
            batchJobItemDao.persist(item);
            if (item.getLastInQueue()) {
                log.info("JOB: {} ITEM: {} Processed last item, setting job to PROCESSED state", item.getBatchJobId(), item.getId());
                item.getBatchJob().setState(BatchJob.State.PROCESSED);
                batchJobDao.persist(item.getBatchJob());
            } else {
                if (item.getBatchJob().getProcessType() == BatchJob.ProcessType.QUEUE) {
                    log.info("JOB: {} ITEM: {} Scheduling next batch job item: {} to process immediately", item.getBatchJobId(), item.getId(), item.getNextItemId());
                    BatchJobItem nextItem = batchJobItemDao.findById(item.getNextItemId());
                    nextItem.setProcessAt(LocalDateTime.now());
                    batchJobItemDao.persist(nextItem);
                    processJobItem(nextItem);
                }
            }
        }
        return item;
    }

    public List<BatchJob> createDirectoryScannerJobs() throws BatchProcessingException {
        List<BatchJob> jobs = new ArrayList<>();
        try {
            for (Directory directory : directoryDao.findAll()) {
                BatchJob job = newFilesScannerProcessor.createJob(Map.of("directory", directory));
                jobs.add(job);
            }
        } catch (Exception e) {
            throw new BatchProcessingException("Failed to obtain root paths", e);
        }
        return jobs;
    }

    private void scheduleJobItem(BatchJobItem item, LocalDateTime runAt) {
        if (item.getState() != BatchJobItem.State.NEW) {
            return;
        }
        log.info("Scheduling batch job item: {}", item.getId());
        JobDetail jobDetail = newJob(BatchJobItemJob.class)
                .withIdentity("batch_job_item_" + item.getId(), "batch_job_item_job")
                .usingJobData("batchJobItemId", item.getId().toString())
                .build();

        Trigger trigger = newTrigger()
                .withIdentity("batch_job_item_" + item.getId(), "batch_job_item_trigger")
                .startAt(Date.from(runAt.atZone(ZoneId.systemDefault()).toInstant()))
                .build();
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            log.error("Failed to schedule a batch job item job", e);
        }
    }

    private void unScheduleJobItems(List<BatchJobItem> items) {
        log.info("Removing scheduled: {} batch job items", items.size());
        List<JobKey> keys = new ArrayList<>();
        for (BatchJobItem item : items) {
            keys.add(new JobKey("batch_job_item_" + item.getId(), "batch_job_item_job"));
        }
        try {
            scheduler.deleteJobs(keys);
        } catch (SchedulerException e) {
            // ignore
        }
    }

    public void startJob(BatchJob batchJob) {
        batchJob.setState(BatchJob.State.PROCESSING);
        batchJobDao.persist(batchJob);
        List<BatchJobItem> items = batchJobItemDao.findByJobId(batchJob.getId());
        jobCounterService.init(batchJob.getId(), items.stream().mapToInt(BatchJobItem::getInternalTotal).sum());
        log.info("JOB: {} Starting {} job, items to process: {}", batchJob.getId(), batchJob.getProcessType(), items.size());

        switch (batchJob.getProcessType()) {
            case PARALLEL:
                for (BatchJobItem item : items) {
                    item.setBatchJob(batchJob);
                    scheduleJobItem(item, item.getProcessAt());
                }
                break;
            case QUEUE:
                for (BatchJobItem item : items) {
                    if (item.getFirstInQueue()) {
                        item.setBatchJob(batchJob);
                        scheduleJobItem(item, LocalDateTime.now());
                        break;
                    }
                }
                break;
        }
    }

}
