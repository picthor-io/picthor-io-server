package io.picthor.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.migration.DataSourceConnectionProvider;
import org.apache.ibatis.migration.FileMigrationLoader;
import org.apache.ibatis.migration.operations.BootstrapOperation;
import org.apache.ibatis.migration.operations.UpOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Configuration
@Slf4j
public class MyBatisConfig {

    private final DataSource dataSource;

    @Value(value = "classpath:migrations/*.*")
    private Resource[] migrations;

    public MyBatisConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void runMigrations() throws IOException {
        log.info("Running {} mybatis migrations", migrations.length);

        // extract all migrations from jar to system tmp directory
        List<File> files = new ArrayList<>();
        Path tmpDir = Files.createTempDirectory("picthor-migrations-");
        files.add(tmpDir.toFile());
        for (Resource migration : migrations) {
            Path path = Path.of(tmpDir.toString(), migration.getFilename());
            Files.copy(migration.getInputStream(), path);
            files.add(path.toFile());
            log.info("Extracted migration: {}", path);
        }

        FileMigrationLoader loader = new FileMigrationLoader(tmpDir.toFile(), null, null);
        DataSourceConnectionProvider provider = new DataSourceConnectionProvider(dataSource);

        new BootstrapOperation().operate(provider, loader, null, System.out);
        new UpOperation().operate(provider, loader, null, System.out);

        // delete all created tmp files
        for (File file : files) {
            file.delete();
        }
    }

    @Bean
    public VendorDatabaseIdProvider databaseIdProvider() {
        VendorDatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
        Properties properties = new Properties();
        properties.put("PostgreSQL", "POSTGRES");
        databaseIdProvider.setProperties(properties);
        return databaseIdProvider;
    }

}
