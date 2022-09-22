package io.picthor.batch.processor;

import io.picthor.ProcessRunner;
import io.picthor.batch.BatchProcessingException;
import io.picthor.config.AppProperties;
import io.picthor.data.dao.BatchJobDao;
import io.picthor.data.dao.BatchJobItemDao;
import io.picthor.data.dao.DirectoryDao;
import io.picthor.data.entity.BatchJob;
import io.picthor.data.entity.BatchJobItem;
import io.picthor.data.entity.Directory;
import io.picthor.services.JobCounterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@Slf4j
public class DirectoryTreeScannerProcessor extends AbstractBatchJobProcessor {

    private final DirectoryDao directoryDao;

    private final AppProperties appProperties;

    private final ProcessRunner processRunner;

    private final JobCounterService jobCounterService;

    private final NewFilesScannerProcessor newFilesScannerProcessor;

    @Autowired
    public DirectoryTreeScannerProcessor(BatchJobDao batchJobDao, BatchJobItemDao batchJobItemDao,
                                         DirectoryDao directoryDao,
                                         AppProperties appProperties,
                                         ProcessRunner processRunner, JobCounterService jobCounterService, NewFilesScannerProcessor newFilesScannerProcessor) {
        this.directoryDao = directoryDao;
        this.appProperties = appProperties;
        this.processRunner = processRunner;
        this.jobCounterService = jobCounterService;
        this.newFilesScannerProcessor = newFilesScannerProcessor;
        this.batchJobDao = batchJobDao;
        this.batchJobItemDao = batchJobItemDao;
    }

    @Override
    public BatchJob createJob(Map<String, Object> parameters) throws BatchProcessingException {
        if (!parameters.containsKey("directory")) {
            throw new BatchProcessingException("Job parameters must contain directory");
        }
        Directory rootDir = (Directory) parameters.get("directory");
        log.info("Processing directory tree scanner batch job creation for root path: {}", rootDir.getFullPath());

        // delete any previous jobs of same type
        batchJobDao.findByRooDirectory(rootDir).stream()
                   .filter(job -> job.getType() == BatchJob.Type.DIRECTORY_TREE_SCANNER)
                   .forEach(job -> {
                       log.debug("Deleting existing job: {}", job.getId());
                       batchJobDao.remove(job);
                   });

        BatchJob job = new BatchJob();
        job.setType(BatchJob.Type.DIRECTORY_TREE_SCANNER);
        job.setState(BatchJob.State.PROCESSING);
        job.setName("Directory tree scan");
        job.setProcessType(BatchJob.ProcessType.PARALLEL);
        job.setProcessAt(LocalDateTime.now());
        job.setItems(new ArrayList<>());
        job.setTotalItems(1);
        job.getPayload().put("rootDirectoryId", rootDir.getId());

        BatchJobItem item = new BatchJobItem();
        item.setBatchJobId(job.getId());
        item.setBatchJob(job);
        item.setState(BatchJobItem.State.NEW);
        item.getPayload().put("rootDirectoryId", rootDir.getId());
        item.setPositionInQueue(1);
        item.setProcessAt(job.getProcessAt());
        item.setFirstInQueue(true);
        item.setLastInQueue(true);
        item.setInternalTotal(1);
        item.setInternalProcessed(0);
        batchJobItemDao.persist(item);
        job.getItems().add(item);
        batchJobDao.persist(job);
        return job;
    }

    @Override
    public void cleanup(BatchJob batchJob) throws BatchProcessingException {
        //
    }

    @Override
    public void processItem(BatchJobItem item) {
        log.info("JOB: {} ITEM: {} Processing item for directory tree scanner job", item.getBatchJobId(), item.getId());
        item.setState(BatchJobItem.State.PROCESSING);
        batchJobItemDao.persist(item);

        try {
            StopWatch sw = new StopWatch();
            sw.start();

            Long rootDirectoryId = (Long) item.getPayload().get("rootDirectoryId");
            Directory rootDir = directoryDao.findById(rootDirectoryId);
            List<Directory> directories = listAllDirectories(rootDir);
            directories.add(rootDir);

            item.setInternalProcessed(1);
            jobCounterService.incr(item.getBatchJobId());
            batchJobItemDao.persist(item);


            sw.stop();
            log.debug("JOB: {} ITEM: {} TOOK: {} to scan directory tree of size: {}", item.getBatchJobId(), item.getId(),
                    DurationFormatUtils.formatDurationHMS(sw.getTotalTimeMillis()), directories.size());

            BatchJob newFilesJob = newFilesScannerProcessor.createJob(Map.of("directory", rootDir, "directories", directories));
//            if (newFilesJob != null) {
////                batchJobService.startJob(newFilesJob);
//            }

        } catch (Exception e) {
            log.error("JOB: " + item.getBatchJobId() + " ITEM: " + item.getId() + " Failed to process job item", e);
        }
    }

    private List<Directory> listAllDirectories(Directory rootDir) throws Exception {
        StopWatch sw = new StopWatch();
        sw.start();
        log.debug("Executing: find {} -type d", rootDir.getFullPath());
        String find = processRunner.execute(appProperties.getFindBinPath(), rootDir.getFullPath(), "-type", "d");
        sw.stop();
        log.debug("Directories listing took: {}", DurationFormatUtils.formatDurationHMS(sw.getTotalTimeMillis()));

        Path rootPath = Paths.get(rootDir.getFullPath());

        sw.start();
        String[] lines = find.split("\n");
        List<Directory> existing = directoryDao.findByFullPath(List.of(lines));

        try (Stream<String> stream = Stream.of(lines)) {
            log.debug("Found: {} directories", lines.length);
            stream
                    .map(Path::of)
                    .distinct()
                    .filter(path -> existing.stream().noneMatch(d -> d.getFullPath().equalsIgnoreCase(path.toString())))
                    .forEach(path -> {
                        if (!path.equals(rootPath)) {
                            Directory directory = new Directory();
                            directory.setFullPath(path.toAbsolutePath().toString());
                            directory.setName(path.getFileName().toString());
                            directory.setState(Directory.State.ENABLED);
                            directory.setType(Directory.Type.STANDARD);
                            directory.setRootDirectoryId(rootDir.getId());
                            directoryDao.persist(directory);
                        }
                    });

            // update all the directories parent ids
            List<Directory> directories = directoryDao.findByType(Directory.Type.STANDARD);
            for (Directory directory : directories) {
                Directory parent = directoryDao.findByFullPath(Path.of(directory.getFullPath()).getParent().toString());
                if (parent != null) {
                    directory.setParentId(parent.getId());
                    directoryDao.persist(directory);
                }
            }
            sw.stop();
            log.debug("Initialised: {} directories in: {}", directories.size(), DurationFormatUtils.formatDurationHMS(sw.getTotalTimeMillis()));
            return directories;
        }
    }

}
