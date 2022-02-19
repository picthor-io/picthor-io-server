package io.picthor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "config")
public class AppProperties {

    private List<String> allowedCorsOrigins;

    private String cacheDir;

    private String convertBinPath;

    private String exifToolBinPath;

    private String xxhsumBinPath;

    private String findBinPath;

    private Integer threadsNum;

}
