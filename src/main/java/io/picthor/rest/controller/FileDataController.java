package io.picthor.rest.controller;

import com.realcnbs.horizon.framework.data.dao.entity.AbstractEntityDao;
import com.realcnbs.horizon.framework.rest.RestRequestFilter;
import com.realcnbs.horizon.framework.rest.controller.AbstractEntityController;
import com.realcnbs.horizon.framework.rest.form.processor.FormProcessor;
import com.realcnbs.horizon.framework.rest.repr.PagedEntityRepr;
import io.picthor.data.dao.FileDataDao;
import io.picthor.data.entity.FileData;
import io.picthor.rest.repr.FileDataRepr;
import io.picthor.services.FilesIndexer;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/file-data")
@RestController
@Slf4j
public class FileDataController extends AbstractEntityController<FileData> {

    private final FileDataDao fileDataDao;

    private final FilesIndexer filesIndexer;

    public FileDataController(FileDataDao fileDataDao, FilesIndexer filesIndexer) {
        this.fileDataDao = fileDataDao;
        this.filesIndexer = filesIndexer;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public FileDataRepr getById(@PathVariable("id") Long id) throws Exception {
        return (FileDataRepr) super.getEntityReprById(id);
    }

    @RequestMapping(value = "/extensions", method = RequestMethod.GET)
    public List<Map<String, Object>> getAllExtensions() {
        return fileDataDao.getAllExtensions();
    }

    @RequestMapping(value = "/{id}/meta", method = RequestMethod.GET)
    public String getMetaById(@PathVariable("id") Long id) throws Exception {
        FileData fileData = fileDataDao.findById(id);
        if (fileData == null) {
            throw new NotFoundException("File not found");
        }
        if (fileData.getHash() == null) {
            fileData.setHash(filesIndexer.getFileHash(Path.of(fileData.getFullPath())));
            fileDataDao.persist(fileData);
        }
        if (fileData.getSizeBytes() == null) {
            fileData.setSizeBytes(Files.size(Path.of(fileData.getFullPath())));
            fileDataDao.persist(fileData);
        }
        if (fileData.getMeta() == null) {
            filesIndexer.readFileMeta(fileData);
            fileDataDao.persist(fileData);
        }
        return fileData.getMeta();
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public PagedEntityRepr getAll(RestRequestFilter filter) throws Exception {
        return (PagedEntityRepr) super.getAllEntitiesReprs(filter);
    }

    @Override
    protected AbstractEntityDao getDao() {
        return fileDataDao;
    }

    @Override
    protected FormProcessor getFormProcessor() {
        return null;
    }
}
