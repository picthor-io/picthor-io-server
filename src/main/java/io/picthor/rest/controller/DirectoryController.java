package io.picthor.rest.controller;

import com.realcnbs.horizon.framework.data.dao.entity.AbstractEntityDao;
import com.realcnbs.horizon.framework.rest.RestRequestFilter;
import com.realcnbs.horizon.framework.rest.controller.AbstractEntityController;
import com.realcnbs.horizon.framework.rest.exception.NotFoundException;
import com.realcnbs.horizon.framework.rest.form.processor.FormProcessor;
import com.realcnbs.horizon.framework.rest.repr.PagedEntityRepr;
import io.picthor.batch.BatchJobService;
import io.picthor.batch.processor.DeletedFilesScannerProcessor;
import io.picthor.batch.processor.NewFilesScannerProcessor;
import io.picthor.data.dao.BatchJobDao;
import io.picthor.data.dao.DirectoryDao;
import io.picthor.data.entity.BatchJob;
import io.picthor.data.entity.Directory;
import io.picthor.rest.repr.BatchJobRepr;
import io.picthor.rest.repr.DirectoryRepr;
import io.picthor.services.JobCounter;
import io.picthor.services.JobCounterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/directories")
@RestController
@Slf4j
public class DirectoryController extends AbstractEntityController<Directory> {

    private final DirectoryDao directoryDao;

    private final BatchJobDao batchJobDao;

    private final NewFilesScannerProcessor newFilesScannerProcessor;

    private final DeletedFilesScannerProcessor deletedFilesScannerProcessor;

    private final BatchJobService batchJobService;

    private final JobCounterService jobCounterService;

    public DirectoryController(DirectoryDao DirectoryDao, BatchJobDao batchJobDao, NewFilesScannerProcessor newFilesScannerProcessor, DeletedFilesScannerProcessor deletedFilesScannerProcessor, BatchJobService batchJobService, JobCounterService jobCounterService) {
        this.directoryDao = DirectoryDao;
        this.batchJobDao = batchJobDao;
        this.newFilesScannerProcessor = newFilesScannerProcessor;
        this.deletedFilesScannerProcessor = deletedFilesScannerProcessor;
        this.batchJobService = batchJobService;
        this.jobCounterService = jobCounterService;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public DirectoryRepr getById(@PathVariable("id") Long id) throws Exception {
        return (DirectoryRepr) super.getEntityReprById(id);
    }

    @RequestMapping(value = "/{id}/sync", method = RequestMethod.POST)
    public BatchJobRepr syncDirectory(@PathVariable("id") Long id) throws Exception {
        Directory directory = directoryDao.findById(id);
        if (directory == null) {
            throw new NotFoundException("Directory not found");
        }
        if (directory.getType() != Directory.Type.ROOT) {
            throw new Exception("Directory must be root");
        }

        BatchJob newFilesJob = newFilesScannerProcessor.createJob(Map.of("directory", directory));
        if (newFilesJob != null) {
            batchJobService.startJob(newFilesJob);
        }
//        BatchJob deletedFilesJob = deletedFilesScannerProcessor.createJob(Map.of("directory", directory));
//        if (deletedFilesJob != null) {
//            batchJobService.startJob(deletedFilesJob);
//        }

        return new BatchJobRepr(newFilesJob);
    }

    @RequestMapping(value = "/{id}/jobs", method = RequestMethod.GET)
    public List<BatchJobRepr> getSyncJob(@PathVariable("id") Long id) throws Exception {
        Directory directory = directoryDao.findById(id);
        if (directory == null) {
            throw new NotFoundException("Directory not found");
        }
        if (directory.getType() != Directory.Type.ROOT) {
            throw new Exception("Directory must be root");
        }

        List<BatchJobRepr> reprs = new ArrayList<>();
        List<BatchJob> jobs = batchJobDao.findByRooDirectory(directory);
        for (BatchJob job : jobs) {
            JobCounter counter = jobCounterService.getJobCounter(job.getId());
            BatchJobRepr repr = new BatchJobRepr(job);
            repr.setJobCounter(counter);
            reprs.add(repr);
        }
        return reprs;
    }

//    @RequestMapping(value = "/{id}/tree", method = RequestMethod.GET)
//    public DirectoryRepr getTree(@PathVariable("id") Long id) throws Exception {
//        Directory directory = directoryDao.findById(id);
//        if(directory.getType() != Directory.Type.ROOT){
//            throw new NotFoundException("Directory must be ROOT");
//        }
//        directoryDao.fetchTree(directory);
//        return new DirectoryRepr(directory);
//    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public PagedEntityRepr getAll(RestRequestFilter filter) throws Exception {
        return (PagedEntityRepr) super.getAllEntitiesReprs(filter);
    }

    @Override
    protected AbstractEntityDao getDao() {
        return directoryDao;
    }

    @Override
    protected FormProcessor getFormProcessor() {
        return null;
    }
}
