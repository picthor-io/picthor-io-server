package io.picthor.services;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import io.picthor.ProcessRunner;
import io.picthor.config.AppProperties;
import io.picthor.data.dao.DirectoryDao;
import io.picthor.data.entity.Directory;
import io.picthor.data.entity.FileData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FilesIndexer {

    private final ProcessRunner processRunner;

    private final DirectoryDao directoryDao;

    private final AppProperties appProperties;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Map<String, Long> directoryIdsMap = new HashMap<>();

    public FilesIndexer(ProcessRunner processRunner, DirectoryDao directoryDao, AppProperties appProperties) {
        this.processRunner = processRunner;
        this.directoryDao = directoryDao;
        this.appProperties = appProperties;
    }

    public FileData index(Path path) throws IOException {
        log.trace("Indexing file: {}", path);
        FileData fileData = new FileData();
        String fileName = path.getFileName().toString();
        fileData.setDirectoryId(getDirectoryId(path.getParent().toString()));
        fileData.setFileName(fileName);
        fileData.setBaseName(FilenameUtils.getBaseName(fileName));
        fileData.setExtension(FilenameUtils.getExtension(fileName));
        fileData.setCreatedAt(LocalDateTime.now());
        fileData.setFullPath(path.toString());
        fileData.setDirPath(path.getParent().toString());
        fileData.setSizeBytes(Files.size(path));
        fileData.setSyncState(FileData.SyncState.SCANNED);
        return fileData;
    }

    private Long getDirectoryId(String fullPath) {
        if (!directoryIdsMap.containsKey(fullPath)) {
            Directory directory = directoryDao.findByFullPath(fullPath);
            if (directory != null) {
                directoryIdsMap.put(fullPath, directory.getId());
            }
        }
        return directoryIdsMap.get(fullPath);
    }

    public String getFileHash(Path path) {
        try {
            return processRunner.execute("sh", "-c", appProperties.getXxhsumBinPath() + " '" + path.toString() + "' | awk '{print $1}'");
        } catch (Exception e) {
            log.error("Failed to get file hash: {}", path.toString(), e);
        }
        return null;
    }

    public void readFileMeta(FileData fileData) {
        String json = null;

        try {
            json = processRunner.execute("sh", "-c", appProperties.getExifToolBinPath() + " -Software -GPSAltitude -GPSLongitude -GPSLatitude -LensID " +
                    "-ImageWidth -ImageHeight" +
                    " -FocalLength -FocalLengthIn35mmFormat -ISO -Aperture -Model -Make -ShutterSpeed" +
                    " -CreateDate -DateTimeOriginal -json '" + fileData.getFullPath() + "'");
        } catch (Exception e) {
            log.error("Failed to read file meta: {}", fileData.getFullPath(), e);
        }
        if (json != null) {
            if (json.startsWith("[")) {
                json = json.substring(1).substring(0, json.length() - 2);
            }
            fileData.setMeta(json);
            if (json.contains("CreateDate".toLowerCase())) {
                ReadContext ctx = JsonPath.parse(json);
                fileData.setTakenAt(LocalDateTime.parse(ctx.read("$[0].CreateDate"), formatter));
            }
        }
    }

}
