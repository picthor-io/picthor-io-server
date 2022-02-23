package io.picthor.batch.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.picthor.batch.BatchProcessingException;
import io.picthor.config.AppProperties;
import io.picthor.data.dao.BatchJobDao;
import io.picthor.data.dao.BatchJobItemDao;
import io.picthor.data.dao.FileDataDao;
import io.picthor.data.entity.BatchJob;
import io.picthor.data.entity.BatchJobItem;
import io.picthor.data.entity.Directory;
import io.picthor.data.entity.FileData;
import io.picthor.services.JobCounterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DeletedFilesScannerProcessor extends AbstractBatchJobProcessor {

    private final FileDataDao fileDataDao;

    private final AppProperties appProperties;

    private final ObjectMapper objectMapper;

    private final JobCounterService jobCounterService;

    public DeletedFilesScannerProcessor(FileDataDao fileDataDao, BatchJobDao batchJobDao, BatchJobItemDao batchJobItemDao, AppProperties appProperties,
                                        ObjectMapper objectMapper, JobCounterService jobCounterService) {
        this.fileDataDao = fileDataDao;
        this.appProperties = appProperties;
        this.jobCounterService = jobCounterService;
        this.batchJobDao = batchJobDao;
        this.batchJobItemDao = batchJobItemDao;
        this.objectMapper = objectMapper;
    }

    @Override
    public void processItem(BatchJobItem item) throws BatchProcessingException {
        log.info("JOB: {} ITEM: {} Processing item for deleted files scanner job", item.getBatchJobId(), item.getId());
        item.setState(BatchJobItem.State.PROCESSING);
        batchJobItemDao.persist(item);

        try {
            Map<String, Object> params = objectMapper.readValue(item.getPayload(), new TypeReference<>() {
            });
            List<Long> ids = (List<Long>) params.get("ids");
            List<FileData> files = fileDataDao.findByIds(ids);
            log.info("JOB: {} ITEM: {} Checking: {} files for existence", item.getBatchJobId(), item.getId(), files.size());
            for (FileData file : files) {
                if (!Files.exists(Path.of(file.getFullPath()))) {
                    file.setSyncState(FileData.SyncState.MISSING);
                    fileDataDao.persist(file);
                }
                jobCounterService.incr(item.getBatchJobId());
                item.setInternalProcessed(item.getInternalProcessed() + 1);
                batchJobItemDao.persistAsync(item);
            }
        } catch (IOException e) {
            log.error("JOB: " + item.getBatchJobId() + " ITEM: " + item.getId() + " Failed to deserialize item payload", e);
        }
    }

    @Override
    public BatchJob createJob(Map<String, Object> parameters) throws BatchProcessingException {
        if (!parameters.containsKey("directory")) {
            throw new BatchProcessingException("Job parameters must contain directory");
        }
        Directory rootDir = (Directory) parameters.get("directory");
        List<Long> filesIds = fileDataDao.findIdsByRootDirectoryId(rootDir.getId());
        if (filesIds.isEmpty()) {
            return null;
        }

        // delete any previous jobs of same type
        batchJobDao.findByRooDirectory(rootDir).stream()
                .filter(job -> job.getType() == BatchJob.Type.DELETED_FILES_SCANNER)
                .forEach(job -> {
                    log.debug("Deleting existing job: {}", job.getId());
                    batchJobDao.remove(job);
                });

        log.info("Processing deleted files scanner batch job creation for root path: {}", rootDir.getFullPath());

        BatchJob job = new BatchJob();
        job.setType(BatchJob.Type.DELETED_FILES_SCANNER);
        job.setState(BatchJob.State.NEW);
        job.setName("Deleted files scan");
        job.setProcessType(BatchJob.ProcessType.PARALLEL);
        job.setProcessAt(LocalDateTime.now());
        job.setItems(new ArrayList<>());
        try {
            job.setPayload(objectMapper.writeValueAsString(
                            Map.of(
                                    "rootDirectoryId", rootDir.getId())
                    )
            );
        } catch (JsonProcessingException e) {
            throw new BatchProcessingException("JOB: " + job.getId() + " Failed to created batch job", e);
        }
        batchJobDao.persist(job);


        log.info("JOB: {} Total: {} files to scan, creating batch job items based on threadsNum config: {}", job.getId(), filesIds.size(),
                appProperties.getThreadsNum());
        int subSetSize = (int) (Math.ceil((filesIds.size() / appProperties.getThreadsNum()) / 10.0) * 10);
        List<List<Long>> subSets = ListUtils.partition(filesIds, subSetSize);

        log.info("JOB: {} Created {} sub sets of: {} items each", job.getId(), subSets.size(), subSets.stream().map(List::size).collect(Collectors.toList()));
        int i = 1;
        try {
            for (List<Long> subSet : subSets) {
                BatchJobItem item = new BatchJobItem();
                item.setBatchJobId(job.getId());
                item.setBatchJob(job);
                item.setState(BatchJobItem.State.NEW);
                item.setPayload(objectMapper.writeValueAsString(
                                Map.of(
                                        "ids", subSet,
                                        "rootDirectoryId", rootDir.getId())
                        )
                );
                item.setPositionInQueue(i);
                item.setProcessAt(job.getProcessAt());
                item.setFirstInQueue(false);
                item.setLastInQueue(false);
                item.setInternalTotal(subSet.size());
                item.setInternalProcessed(0);
                batchJobItemDao.persist(item);
                job.getItems().add(item);
                i++;
                log.info("JOB: {} Created job item for: {} files", job.getId(), subSet.size());
            }

        } catch (
                JsonProcessingException e) {
            throw new BatchProcessingException("JOB: " + job.getId() + " Failed to created batch job", e);
        }

        job.setTotalItems(job.getItems().size());
        batchJobDao.persist(job);

        return job;
    }

    @Override
    public void cleanup(BatchJob batchJob) throws BatchProcessingException {

    }
}
