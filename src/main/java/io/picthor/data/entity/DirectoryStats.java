package io.picthor.data.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DirectoryStats {
    @JsonProperty("dirs_num")
    private Long dirsNum = 0L;
    @JsonProperty("files_num")
    private Long filesNum = 0L;
    @JsonProperty("size_bytes")
    private Long sizeBytes = 0L;
    @JsonProperty("child_dirs_num")
    private Long childDirsNum = 0L;
    @JsonProperty("child_files_num")
    private Long childFilesNum = 0L;
    @JsonProperty("child_size_bytes")
    private Long childSizeBytes = 0L;

    public void resetChildStats(){
        childDirsNum = 0L;
        childSizeBytes = 0L;
        childFilesNum = 0L;
    }
}
