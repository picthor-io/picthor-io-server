package io.picthor.batch.processor;

import io.picthor.batch.BatchProcessingException;
import io.picthor.config.AppProperties;
import io.picthor.data.dao.BatchJobDao;
import io.picthor.data.dao.BatchJobItemDao;
import io.picthor.data.dao.DirectoryDao;
import io.picthor.data.dao.FileDataDao;
import io.picthor.data.entity.BatchJob;
import io.picthor.data.entity.BatchJobItem;
import io.picthor.data.entity.Directory;
import io.picthor.data.entity.FileData;
import io.picthor.services.DirectoryStatsService;
import io.picthor.services.FilesIndexer;
import io.picthor.services.JobCounterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class NewFilesScannerProcessor extends AbstractBatchJobProcessor {

    private final FilesIndexer filesIndexer;

    private final FileDataDao fileDataDao;

    private final DirectoryDao directoryDao;

    private final JobCounterService jobCounterService;

    private final AppProperties appProperties;

    private final DirectoryStatsService statsService;

    @Autowired
    public NewFilesScannerProcessor(BatchJobDao batchJobDao, BatchJobItemDao batchJobItemDao, FilesIndexer filesIndexer,
                                    FileDataDao fileDataDao,
                                    DirectoryDao directoryDao, JobCounterService jobCounterService,
                                    AppProperties appProperties,
                                    DirectoryStatsService statsService) {
        this.filesIndexer = filesIndexer;
        this.fileDataDao = fileDataDao;
        this.directoryDao = directoryDao;
        this.jobCounterService = jobCounterService;
        this.appProperties = appProperties;
        this.statsService = statsService;
        this.batchJobDao = batchJobDao;
        this.batchJobItemDao = batchJobItemDao;
    }

    @Override
    public BatchJob createJob(Map<String, Object> parameters) throws BatchProcessingException {
        if (!parameters.containsKey("directory")) {
            throw new BatchProcessingException("Job parameters must contain directory");
        }
        Directory rootDir = (Directory) parameters.get("directory");

        if (!parameters.containsKey("directories")) {
            throw new BatchProcessingException("Job parameters must contain directories list");
        }
        List<Directory> directories = (List<Directory>) parameters.get("directories");

        log.info("Processing new files scanner batch job creation for root path: {} containing: {} directories", rootDir.getFullPath(), directories.size());

        // delete any previous jobs of same type
        batchJobDao.findByRooDirectory(rootDir).stream()
                   .filter(job -> job.getType() == BatchJob.Type.NEW_FILES_SCANNER)
                   .forEach(job -> {
                       log.debug("Deleting existing job: {}", job.getId());
                       batchJobDao.remove(job);
                   });

        BatchJob job = new BatchJob();
        job.setType(BatchJob.Type.NEW_FILES_SCANNER);
        job.setState(BatchJob.State.NEW);
        job.setName("Directories scan");
        job.setProcessType(BatchJob.ProcessType.PARALLEL);
        job.setProcessAt(LocalDateTime.now());
        job.setItems(new ArrayList<>());
        job.getPayload().put("rootDirectoryId", rootDir.getId());
        batchJobDao.persist(job);

        try {
            int subSetSize = (int) (Math.ceil((directories.size() / appProperties.getThreadsNum()) / 10.0) * 10);
            List<List<Directory>> subSets = ListUtils.partition(directories, subSetSize);

            log.info("JOB: {} Created {} sub sets of: {} items each", job.getId(),
                    subSets.size(), subSets.stream().map(List::size).collect(Collectors.toList()));
            int i = 1;
            for (List<Directory> subSet : subSets) {
                BatchJobItem item = new BatchJobItem();
                item.setBatchJobId(job.getId());
                item.setBatchJob(job);
                item.setState(BatchJobItem.State.NEW);
                item.getPayload().put("rootDirectoryId", rootDir.getId());
                item.getPayload().put("directoriesPaths", subSet.stream().map(Directory::getFullPath).collect(Collectors.toList()));
                item.setPositionInQueue(i++);
                item.setProcessAt(job.getProcessAt());
                item.setFirstInQueue(false);
                item.setLastInQueue(false);
                item.setInternalTotal(subSet.size());
                item.setInternalProcessed(0);
                batchJobItemDao.persist(item);
                job.getItems().add(item);
                log.info("JOB: {} Created job item for: {} directories", job.getId(), subSet.size());
            }
        } catch (Exception e) {
            throw new BatchProcessingException("JOB: " + job.getId() + " Failed to scan for directories", e);
        }

        job.setTotalItems(job.getItems().size());
        batchJobDao.persist(job);

        return job;
    }

    @Override
    public void cleanup(BatchJob batchJob) throws BatchProcessingException {
        //
    }

    @Override
    public void processItem(BatchJobItem item) {
        log.info("JOB: {} ITEM: {} Processing item for new files scanner job", item.getBatchJobId(), item.getId());
        item.setState(BatchJobItem.State.PROCESSING);
        batchJobItemDao.persist(item);

        try {

            List<String> directoriesPaths = (List<String>) item.getPayload().get("directoriesPaths");
            Long rootDirectoryId = Long.valueOf((Long) item.getPayload().get("rootDirectoryId"));

            List<Directory> directories = new ArrayList<>();
            List<List<String>> subSets = ListUtils.partition(directoriesPaths, Short.MAX_VALUE);
            for (List<String> subSet : subSets) {
                directories.addAll(directoryDao.findByFullPath(subSet));
            }

            StopWatch sw = new StopWatch();
            long totalTime = 0L;
            log.info("JOB: {} ITEM: {} directories to scan: {}", item.getBatchJobId(), item.getId(), directories.size());

            for (Directory directory : directories) {
                List<FileData> existing = fileDataDao.findByDirectory(directory);
                List<String> existingPaths = existing.stream().map(FileData::getFullPath).toList();
                statsService.syncStats();
                try {
                    List<Path> paths = listFilesUsingFileWalk(directory.getFullPath(), existingPaths);

                    // skip empty dirs
                    if (paths.isEmpty()) {
                        // count each directory as internal process unit
                        item.setInternalProcessed(item.getInternalProcessed() + 1);
                        jobCounterService.incr(item.getBatchJobId());
                        batchJobItemDao.persist(item);
                        log.info("JOB: {} ITEM: {} No new files found in directory: {}", item.getBatchJobId(), item.getId(), directory.getFullPath());
                        continue;
                    }
                    sw.start();
                    log.info("JOB: {} ITEM: {} found: {} files in directory: {}", item.getBatchJobId(), item.getId(), paths.size(), directory.getFullPath());

                    paths.stream()
                         .filter(Objects::nonNull)
                         .forEach(path -> {
                             FileData fileData = fileDataDao.findByFullPath(path.toString());
                             if (fileData == null) {
                                 try {
                                     fileData = filesIndexer.index(path);
                                     fileData.setRootDirectoryId(rootDirectoryId);
                                     fileDataDao.persist(fileData);
                                 } catch (IOException e) {
                                     log.error("JOB: {} ITEM: {} failed to to index file: {}", item.getBatchJobId(), item.getId(), path, e);
                                 }
                             }
                         });

                    // count each directory as internal process unit
                    item.setInternalProcessed(item.getInternalProcessed() + 1);
                    jobCounterService.incr(item.getBatchJobId());
                    batchJobItemDao.persist(item);

                    sw.stop();
                    totalTime += sw.getTotalTimeMillis();
                    log.debug("JOB: {} ITEM: {} TOOK: {} to scan {} files in directory: {}", item.getBatchJobId(), item.getId(),
                            DurationFormatUtils.formatDurationHMS(sw.getTotalTimeMillis()), paths.size(), directory.getFullPath());

                } catch (IOException e) {
                    log.info("JOB: {} ITEM: {} failed to scan for files in directory: {}", item.getBatchJobId(), item.getId(), directory.getFullPath());
                }
            }

            log.debug("JOB: {} ITEM: {} TOOK: {} to scan {} directories", item.getBatchJobId(), item.getId(),
                    DurationFormatUtils.formatDurationHMS(totalTime), directories.size());

        } catch (Exception e) {
            log.error("JOB: " + item.getBatchJobId() + " ITEM: " + item.getId() + " Failed to process job item", e);
        }
    }


    private List<Path> listFilesUsingFileWalk(String dir, List<String> existingPaths) throws IOException {
//        String find = processRunner.execute("find", "-printf", "{\\\"size\\\":%s\\, filename\\\": \\\"%p\\\"},\n");
        try (Stream<Path> stream = Files.walk(Paths.get(dir), 1)) {
            return stream.filter(path -> !Files.isDirectory(path))
                         .filter(path -> !existingPaths.contains(path.toString()))
                         .toList()
                    ;
        }
    }
}
