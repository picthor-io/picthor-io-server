package io.picthor.services;

import io.picthor.ProcessRunner;
import io.picthor.config.AppProperties;
import io.picthor.data.entity.FileData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class Thumbnailer {

    public static final int[] SIZES = {250, 500, 800, 1080, 1350};

    private final ProcessRunner processRunner;

    private final AppProperties appProperties;

    public Thumbnailer(ProcessRunner processRunner, AppProperties appProperties) {
        this.processRunner = processRunner;
        this.appProperties = appProperties;
    }

    @PostConstruct
    private void checkCacheDirs() throws IOException {
        if (!Files.exists(Path.of(appProperties.getCacheDir() + "/thumbs/"))) {
            Files.createDirectory(Path.of(appProperties.getCacheDir() + "/thumbs/"));
        }
        for (int size : SIZES) {
            Path path = Path.of(appProperties.getCacheDir() + "/thumbs/" + size);
            if (!Files.exists(path)) {
                log.debug("Creating cache dir: {}", path);
                Files.createDirectory(path);
            }
        }
    }

    public void generate(FileData fileData, int width) {
        log.debug("Generating thumbnail for fileData: {}", fileData.getFileName());

        Path dir = Path.of(fileData.getThumbDir(appProperties.getCacheDir(), width));
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                log.error("Failed to generate thumbnail for: {}", fileData.getFullPath(), e);
            }
        }

        String thumbPath = fileData.getThumbPath(appProperties.getCacheDir(), width);

        // Some extensions might represent different formats - TIF and TIFF might be both raw and libtiff
        // For such cases try raw: delegate first and the automatic delegate if raw: fails
        try {
            if (FileData.RAW_EXTENSIONS.contains(fileData.getExtension().toUpperCase())) {
                try {
                    // try raw: delegate
                    processRunner.execute("sh", "-c", "convert 'raw:" + fileData.getFullPath() + "[1]' -thumbnail '"
                            + width + "x>' -gravity center -auto-orient '" + thumbPath + "'");
                } catch (Exception e) {
                    // try automatic delegate
                    processRunner.execute("sh", "-c", "convert '" + fileData.getFullPath() + "[1]' -thumbnail '"
                            + width + "x>' -gravity center -auto-orient '" + thumbPath + "'");
                }
            } else {
                // default to  automatic delegate
                processRunner.execute("sh", "-c", "convert '" + fileData.getFullPath() + "[1]' -thumbnail '"
                        + width + "x>' -gravity center -auto-orient '" + thumbPath + "'");
            }
        } catch (Exception e) {
            log.error("Failed to generate thumbnail for: {}", fileData.getFullPath(), e);
        }

    }

}
