package io.picthor.rest.controller;

import com.realcnbs.horizon.framework.data.dao.entity.AbstractEntityDao;
import com.realcnbs.horizon.framework.rest.RestRequestFilter;
import com.realcnbs.horizon.framework.rest.controller.AbstractEntityController;
import com.realcnbs.horizon.framework.rest.exception.NotFoundException;
import com.realcnbs.horizon.framework.rest.form.processor.FormProcessor;
import com.realcnbs.horizon.framework.rest.repr.PagedEntityRepr;
import io.picthor.batch.BatchJobService;
import io.picthor.batch.processor.DeletedFilesScannerProcessor;
import io.picthor.batch.processor.DirectoryTreeScannerProcessor;
import io.picthor.data.dao.DirectoryDao;
import io.picthor.data.entity.BatchJob;
import io.picthor.data.entity.Directory;
import io.picthor.rest.form.RootDirectoryForm;
import io.picthor.rest.form.RootDirectoryFormProcessor;
import io.picthor.rest.repr.DirectoryRepr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RequestMapping("/api/directories")
@RestController
@Slf4j
public class DirectoryController extends AbstractEntityController<Directory> {

    private final DirectoryDao directoryDao;

    private final DirectoryTreeScannerProcessor directoryTreeScannerProcessor;

    private final DeletedFilesScannerProcessor deletedFilesScannerProcessor;

    private final BatchJobService batchJobService;

    private final RootDirectoryFormProcessor formProcessor;

    public DirectoryController(DirectoryDao directoryDao, DirectoryTreeScannerProcessor directoryTreeScannerProcessor,
                               DeletedFilesScannerProcessor deletedFilesScannerProcessor, BatchJobService batchJobService,
                               RootDirectoryFormProcessor formProcessor) {
        this.directoryDao = directoryDao;
        this.directoryTreeScannerProcessor = directoryTreeScannerProcessor;
        this.deletedFilesScannerProcessor = deletedFilesScannerProcessor;
        this.batchJobService = batchJobService;
        this.formProcessor = formProcessor;
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public DirectoryRepr addRoot(@Valid @RequestBody RootDirectoryForm form) throws Exception {
        return (DirectoryRepr) super.createAndWrapEntity(form);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public DirectoryRepr getById(@PathVariable("id") Long id) throws Exception {
        return (DirectoryRepr) super.getEntityReprById(id);
    }

    @RequestMapping(value = "/{id}/sync", method = RequestMethod.POST)
    public void syncDirectory(@PathVariable("id") Long id) throws Exception {
        Directory directory = directoryDao.findById(id);
        if (directory == null) {
            throw new NotFoundException("Directory not found");
        }
        if (directory.getType() != Directory.Type.ROOT) {
            throw new Exception("Directory must be root");
        }

        BatchJob newFilesJob = directoryTreeScannerProcessor.createJob(Map.of("directory", directory));
        if (newFilesJob != null) {
            batchJobService.startJob(newFilesJob);
        }
        BatchJob deletedFilesJob = deletedFilesScannerProcessor.createJob(Map.of("directory", directory));
        if (deletedFilesJob != null) {
            batchJobService.startJob(deletedFilesJob);
        }
    }

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
        return formProcessor;
    }
}
