package io.picthor.rest.repr;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.realcnbs.horizon.framework.rest.repr.AbstractEntityRepr;
import io.picthor.data.entity.Directory;

import java.time.format.DateTimeFormatter;

public class DirectoryRepr extends AbstractEntityRepr {

    private final Directory directory;

    public DirectoryRepr(Directory directory) {
        super(directory);
        this.directory = directory;
    }

    @JsonProperty
    public String getLastSyncAt() {
        if (directory.getLastSyncAt() != null) {
            return directory.getLastSyncAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        }
        return null;
    }

    @JsonProperty
    public String getFullPath() {
        return directory.getFullPath();
    }

    @JsonProperty
    public Directory.Type getType() {
        return directory.getType();
    }

    @JsonProperty
    public String getLabel() {
        return directory.getLabel();
    }

    @JsonProperty
    public String getName() {
        return directory.getName();
    }

    @JsonProperty
    public Long getParentId() {
        return directory.getParentId();
    }

    @JsonProperty
    public Long getRootDirectoryId() {
        return directory.getRootDirectoryId();
    }

    @JsonProperty
    public String getDescription() {
        return directory.getDescription();
    }

    @JsonProperty
    public Directory.State getState() {
        return directory.getState();
    }

    @JsonProperty
    public String getExcludes() {
        return directory.getExcludes();
    }

    @JsonProperty
    public DirectoryRepr getParent() {
        if (directory.getParent() != null) {
            return new DirectoryRepr(directory.getParent());
        }
        return null;
    }

    @JsonProperty
    public DirectoryStatsRepr getStats() {
        if (directory.getStats() != null) {
            return new DirectoryStatsRepr(directory.getStats());
        }
        return null;
    }
}
