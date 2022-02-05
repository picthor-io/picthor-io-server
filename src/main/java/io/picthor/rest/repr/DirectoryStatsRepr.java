package io.picthor.rest.repr;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.picthor.data.entity.DirectoryStats;

public class DirectoryStatsRepr {

    private final DirectoryStats stats;

    public DirectoryStatsRepr(DirectoryStats stats) {
        this.stats = stats;
    }

    @JsonProperty
    public Long getDirsNum() {
        return stats.getDirsNum();
    }

    @JsonProperty
    public Long getTotalDirsNum() {
        return stats.getDirsNum() + stats.getChildDirsNum();
    }

    @JsonProperty
    public Long getFilesNum() {
        return stats.getFilesNum();
    }

    @JsonProperty
    public Long getTotalFilesNum() {
        return stats.getFilesNum() + stats.getChildFilesNum();
    }

    @JsonProperty
    public Long getSizeBytes() {
        return stats.getSizeBytes();
    }

    @JsonProperty
    public Long getTotalSizeBytes() {
        return stats.getSizeBytes() + stats.getChildSizeBytes();
    }

    @JsonProperty
    public Long getChildDirsNum() {
        return stats.getChildDirsNum();
    }

    @JsonProperty
    public Long getChildFilesNum() {
        return stats.getChildFilesNum();
    }

    @JsonProperty
    public Long getChildSizeBytes() {
        return stats.getChildSizeBytes();
    }
}
