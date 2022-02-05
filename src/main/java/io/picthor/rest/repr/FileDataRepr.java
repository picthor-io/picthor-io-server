package io.picthor.rest.repr;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.realcnbs.horizon.framework.rest.repr.AbstractEntityRepr;
import io.picthor.data.entity.FileData;

import java.util.Map;

public class FileDataRepr extends AbstractEntityRepr {

    private final FileData fileData;

    public FileDataRepr(FileData fileData) {
        super(fileData);
        this.fileData = fileData;
    }

    @JsonProperty
    public String getFullPath() {
        return fileData.getFullPath();
    }

    @JsonProperty
    public String getDirPath() {
        return fileData.getDirPath();
    }

    @JsonProperty
    public String getHash() {
        return fileData.getHash();
    }

    @JsonProperty
    public String getFileName() {
        return fileData.getFileName();
    }

    @JsonProperty
    public String getBaseName() {
        return fileData.getBaseName();
    }

    @JsonProperty
    public String getExtension() {
        return fileData.getExtension();
    }

    @JsonProperty
    public Long getSizeBytes() {
        return fileData.getSizeBytes();
    }

    @JsonProperty
    public FileData.Type getType() {
        return fileData.getType();
    }

    @JsonProperty
    public FileData.SyncState getSyncState() {
        return fileData.getSyncState();
    }

    @JsonProperty
    public Long getIndexNanos() {
        return fileData.getIndexNanos();
    }

    @JsonProperty
    public Long getDirectoryId() {
        return fileData.getDirectoryId();
    }

    @JsonProperty
    public Long getRootDirectoryId() {
        return fileData.getRootDirectoryId();
    }
}
