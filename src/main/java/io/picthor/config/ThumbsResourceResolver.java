package io.picthor.config;

import io.picthor.data.dao.FileDataDao;
import io.picthor.data.entity.FileData;
import io.picthor.services.FilesIndexer;
import io.picthor.services.Thumbnailer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@Slf4j
public class ThumbsResourceResolver extends PathResourceResolver {

    private final FileDataDao fileDataDao;

    private final AppProperties properties;

    private final Thumbnailer thumbnailer;

    private final FilesIndexer filesIndexer;

    public ThumbsResourceResolver(FileDataDao fileDataDao, AppProperties properties, Thumbnailer thumbnailer, FilesIndexer filesIndexer) {
        this.fileDataDao = fileDataDao;
        this.properties = properties;
        this.thumbnailer = thumbnailer;
        this.filesIndexer = filesIndexer;
    }

    @Override
    protected Resource resolveResourceInternal(HttpServletRequest request, String requestPath, List<? extends Resource> locations, ResourceResolverChain chain) {
        if (!requestPath.isEmpty()) {
            log.debug("Resolving thumb resource for: {}", requestPath);
            try {
                String[] parts = requestPath.split("/");
                int width = Integer.parseInt(parts[0]);
                if (!ArrayUtils.contains(Thumbnailer.SIZES, width)) {
                    log.debug("Invalid width: {}, skipping", width);
                    return null;
                }
                Long id = Long.valueOf(parts[1].replace(".jpg", ""));
                FileData fileData = fileDataDao.findById(id);

                if (fileData.getHash() == null) {
                    fileData.setHash(filesIndexer.getFileHash(Path.of(fileData.getFullPath())));
                    fileDataDao.persist(fileData);
                }
                if (fileData.getSizeBytes() == null) {
                    fileData.setSizeBytes(Files.size(Path.of(fileData.getFullPath())));
                    fileDataDao.persist(fileData);
                }

                String thumbPath = fileData.getThumbPath(properties.getCacheDir(), width);
                if (!Files.exists(Path.of(thumbPath))) {
                    log.debug("Thumb: {} does not exist, generating", thumbPath);
                    thumbnailer.generate(fileData, width);
                }

                // checking again in case the generation failed, in this case return error image
                if (!Files.exists(Path.of(thumbPath))) {
                    return super.resolveResourceInternal(request, "error.png", locations, chain);
                }
                return super.resolveResourceInternal(request, fileData.getThumbPath("", width), locations, chain);
            } catch (Exception e) {
                log.debug("Failed to resolve resource for request: {}, reason: {}", requestPath, e.getMessage());
            }
        }
        return null;
    }

}
