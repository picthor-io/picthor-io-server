package io.picthor.rest.controller;

import com.realcnbs.horizon.framework.data.dao.entity.AbstractEntityDao;
import com.realcnbs.horizon.framework.rest.RestRequestFilter;
import com.realcnbs.horizon.framework.rest.controller.AbstractEntityController;
import com.realcnbs.horizon.framework.rest.form.processor.FormProcessor;
import com.realcnbs.horizon.framework.rest.repr.PagedEntityRepr;
import io.picthor.batch.BatchJobService;
import io.picthor.data.dao.BatchJobDao;
import io.picthor.data.entity.BatchJob;
import io.picthor.rest.repr.BatchJobRepr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/api/batch-jobs")
@RestController
@Slf4j
public class BatchJobsController extends AbstractEntityController<BatchJob> {

    private final BatchJobService batchJobService;

    private final BatchJobDao batchJobDao;

    public BatchJobsController(BatchJobService batchJobService, BatchJobDao batchJobDao) {
        this.batchJobService = batchJobService;
        this.batchJobDao = batchJobDao;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public BatchJobRepr getById(@PathVariable("id") Long id) throws Exception {
        return (BatchJobRepr) super.getEntityReprById(id);
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public PagedEntityRepr getAll(RestRequestFilter filter) throws Exception {
        return (PagedEntityRepr) super.getAllEntitiesReprs(filter);
    }

    @RequestMapping(value = "/{id}/start", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void start(@PathVariable("id") Long id) throws Exception {
        BatchJob batchJob = batchJobDao.findById(id);
        batchJobService.startJob(batchJob);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.CREATED)
    public void delete(@PathVariable("id") Long id) throws Exception {
        BatchJob batchJob = batchJobDao.findById(id);
        batchJobService.deleteJob(batchJob);
    }

    @RequestMapping(value = "/root-paths-scan", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public List<BatchJobRepr> directoriesScan() throws Exception {
        List<BatchJob> jobs = batchJobService.createDirectoryScannerJobs();
        List<BatchJobRepr> reprs = new ArrayList<>();
        for (BatchJob job : jobs) {
            reprs.add(new BatchJobRepr(job));
        }
        return reprs;
    }

    @Override
    protected AbstractEntityDao getDao() {
        return batchJobDao;
    }

    @Override
    protected FormProcessor getFormProcessor() {
        return null;
    }
}
