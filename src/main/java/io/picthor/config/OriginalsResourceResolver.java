package io.picthor.config;

import io.picthor.data.dao.FileDataDao;
import io.picthor.data.entity.FileData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
@Slf4j
public class OriginalsResourceResolver extends PathResourceResolver {

    private final FileDataDao fileDataDao;

    public OriginalsResourceResolver(FileDataDao fileDataDao) {
        this.fileDataDao = fileDataDao;
    }

    @Override
    protected Resource resolveResourceInternal(HttpServletRequest request, String requestPath, List<? extends Resource> locations,
                                               ResourceResolverChain chain) {
        System.out.println(requestPath);
        if (!requestPath.isEmpty()) {
            log.debug("Resolving originals resource for: {}", requestPath);
            try {
                String[] parts = requestPath.split("/");
                Long id = Long.valueOf(parts[0]);
                FileData fileData = fileDataDao.findById(id);
                return new FileUrlResource(fileData.getFullPath());
            } catch (Exception e) {
                log.debug("Failed to resolve resource for request: {}, reason: {}", requestPath, e.getMessage());
            }
        }
        return null;
    }

}
