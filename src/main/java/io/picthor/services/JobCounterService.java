package io.picthor.services;

import io.picthor.data.dao.BatchJobDao;
import io.picthor.data.dao.BatchJobItemDao;
import io.picthor.data.entity.BatchJob;
import io.picthor.data.entity.BatchJobItem;
import io.picthor.websocket.service.WebSocketService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JobCounterService {

    private final NotificationsService notificationsService;

    private final WebSocketService webSocketService;

    private final BatchJobItemDao batchJobItemDao;

    private final BatchJobDao batchJobDao;
    @Getter
    private final Map<Long, JobCounter> counters = new HashMap<>();

    public JobCounterService(BatchJobItemDao batchJobItemDao, BatchJobDao batchJobDao, NotificationsService notificationsService,
                             WebSocketService webSocketService) {
        this.batchJobItemDao = batchJobItemDao;
        this.batchJobDao = batchJobDao;
        this.notificationsService = notificationsService;
        this.webSocketService = webSocketService;
    }

    public void init(BatchJob job, Integer total) {
        JobCounter counter = new JobCounter(job.getId(), job.getRootDirectoryId(), total);
        this.counters.put(job.getId(), counter);
        webSocketService.publishJobCounterUpdated(counter);
    }

    @Scheduled(fixedRate = 1000)
    public void checkStatus() {
        if (counters.isEmpty()) {
            return;
        }
        List<Long> finishedJobs = new ArrayList<>();
        try {
            for (Map.Entry<Long, JobCounter> en : counters.entrySet()) {
                Long jobId = en.getKey();
                JobCounter counter = en.getValue();
                List<BatchJobItem> items = batchJobItemDao.findByJobId(jobId);
                long seconds = TimeUnit.SECONDS.convert(System.nanoTime() - counter.getStartedAt(), TimeUnit.NANOSECONDS);
                if (seconds == 0) seconds = 1;
                long perSecond = counter.getCounter().get() / seconds;

                log.debug("JOB: {} processed: {} of: {}, item per second: {}, ITEMS: {}",
                        jobId,
                        counter.getCounter().get(),
                        counter.getTotal(),
                        perSecond,
                        items.stream().map(i -> i.getId() + ": " + i.getInternalProcessed() + "/" + i.getInternalTotal() + " ").collect(Collectors.joining()));

                if (counter.getCounter().get() == counter.getTotal()) {
                    log.debug("JOB: {} is finished", jobId);
                    finishedJobs.add(jobId);
                    BatchJob job = batchJobDao.findById(jobId);
                    if (job != null) {
                        job.setState(BatchJob.State.PROCESSED);
                        items.stream().peek(batchJobItemDao::remove);
                        batchJobDao.persist(job);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to update job counters", e);
        }

        if (!finishedJobs.isEmpty()) {
            for (Long jobId : finishedJobs) {
                JobCounter counter = counters.get(jobId);
                BatchJob job = batchJobDao.findById(jobId);
                if (job != null) {
                    if (counter.getTotal() > 0) {
                        notificationsService.addSuccess(job.getName() + " is finished", job.getDoneMessage());
                    }
                    webSocketService.publishJobRemoved(job);
                    counters.remove(jobId);
                    batchJobDao.remove(job);
                }
            }
        }
    }

    public JobCounter getJobCounter(Long jobId) {
        return counters.get(jobId);
    }

    public void incr(Long jobId) {
        JobCounter jobCounter = counters.get(jobId);
        jobCounter.getCounter().addAndGet(1);
        webSocketService.publishJobCounterUpdated(jobCounter);
    }

}
