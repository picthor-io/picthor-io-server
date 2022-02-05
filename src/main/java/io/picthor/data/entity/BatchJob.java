package io.picthor.data.entity;

import com.realcnbs.horizon.framework.data.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class BatchJob extends AbstractEntity {

    public enum Type {
        NEW_FILES_SCANNER, DELETED_FILES_SCANNER
    }

    public enum ProcessType {
        QUEUE, PARALLEL
    }

    public enum State {
        NEW, PROCESSING, ON_HOLD, CANCELLED, PROCESSED, ARCHIVED;
    }

    private String name;
    private Integer totalItems;
    private Integer totalProcessed;
    private Type type;
    private ProcessType processType;
    private State state;
    private LocalDateTime processAt;
    private List<BatchJobItem> items;
    private String payload;

    public String getSafeName() {
        if (name == null) {
            return null;
        }
        return name.toLowerCase().replaceAll("\\s", "_").replaceAll(":", "_");
    }

}
