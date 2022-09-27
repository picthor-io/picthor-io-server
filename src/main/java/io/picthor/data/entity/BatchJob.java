package io.picthor.data.entity;

import com.realcnbs.horizon.framework.data.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class BatchJob extends AbstractEntity {

    public enum Type {
        NEW_FILES_SCANNER, DELETED_FILES_SCANNER, DIRECTORY_TREE_SCANNER
    }

    public enum ProcessType {
        QUEUE, PARALLEL
    }

    public enum State {
        NEW, PROCESSING, ON_HOLD, CANCELLED, PROCESSED, ARCHIVED;
    }

    private String name;
    private String doneMessage;
    private Integer totalItems;
    private Integer totalProcessed;
    private Type type;
    private ProcessType processType;
    private State state;
    private LocalDateTime processAt;
    private List<BatchJobItem> items;
    private Map<String, Object> payload = new HashMap<>();
    private Long rootDirectoryId;

    public String getSafeName() {
        if (name == null) {
            return null;
        }
        return name.toLowerCase().replaceAll("\\s", "_").replaceAll(":", "_");
    }

}
