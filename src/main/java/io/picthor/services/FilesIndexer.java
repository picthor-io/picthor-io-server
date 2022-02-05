package io.picthor.services;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import io.picthor.ProcessRunner;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilesIndexer {

    private final ProcessRunner processRunner;

    private final DirectoryDao directoryDao;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ");

    private final Map<String, Long> directoryIdsMap = new HashMap<>();

    public FilesIndexer(ProcessRunner processRunner, DirectoryDao directoryDao) {
        this.processRunner = processRunner;
        this.directoryDao = directoryDao;
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
            return processRunner.execute("sh", "-c", " xxhsum '" + path.toString() + "' | awk '{print $1}'");
        } catch (Exception e) {
            log.error("Failed to get file hash: {}", path.toString(), e);
        }
        return null;
    }

    public void readFileMeta(FileData fileData) {
        String output = null;

        // video meta is not supported yet
        if (FileData.VIDEO_EXTENSIONS.contains(fileData.getExtension().toUpperCase())) {
            try {
                fileData.setTakenAt(Files.getLastModifiedTime(Path.of(fileData.getFullPath())).toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime());
            } catch (IOException e) {
                log.error("Failed to read video modified time: {}", fileData.getFullPath(), e);
            }
            return;
        }

        // Some extensions might represent different formats - TIF and TIFF might be both raw and libtiff
        // For such cases try raw: delegate first and the automatic delegate if raw: fails
        try {
            if (FileData.RAW_EXTENSIONS.contains(fileData.getExtension().toUpperCase())) {
                try {
                    // try raw: delegate
                    output = processRunner.execute("sh", "-c", "magick identify -format '%[DNG:*]' 'raw:" + fileData.getFullPath() + "[1x1+0+0]' ");
                } catch (Exception e) {
                    // try automatic: delegate
                    output = processRunner.execute("sh", "-c", "magick identify -format '%[EXIF:*]' '" + fileData.getFullPath() + "[1x1+0+0]' ");
                }
            } else {
                output = processRunner.execute("sh", "-c", "magick identify -format '%[EXIF:*]' '" + fileData.getFullPath() + "[1x1+0+0]' ");
            }
        } catch (Exception e) {
            log.error("Failed to read file meta: {}", fileData.getFullPath(), e);
        }
        if (output != null) {
            String json = Arrays.stream(output.replace("\"", "\\\"")
                            .split("\n"))
                    .map(s -> s.split("="))
                    .filter(s -> s.length > 1)
                    .peek(s -> s[0] = s[0].replace(":", "_"))
                    .map(s -> "\"" + s[0] + "\":\"" + s[1] + "\"")
                    .collect(Collectors.joining(",", "{", "}"));

            fileData.setMeta(json);
            if (output.contains("date:modify".toLowerCase())) {
                ReadContext ctx = JsonPath.parse(output);
                fileData.setTakenAt(LocalDateTime.parse(ctx.read("$[0].exif_date_modify"), formatter));
            }
        }
    }

}
