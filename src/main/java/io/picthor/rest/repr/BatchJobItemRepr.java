package io.picthor.rest.repr;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.realcnbs.horizon.framework.rest.repr.AbstractEntityRepr;
import io.picthor.data.entity.BatchJobItem;

import java.time.LocalDateTime;

public class BatchJobItemRepr extends AbstractEntityRepr {

    private final BatchJobItem batchJobItem;

    public BatchJobItemRepr(BatchJobItem batchJobItem) {
        super(batchJobItem);
        this.batchJobItem = batchJobItem;
    }

    @JsonProperty
    public Long getBatchJobId() {
        return batchJobItem.getBatchJobId();
    }

    @JsonProperty
    public Long getNextItemId() {
        return batchJobItem.getNextItemId();
    }

    @JsonProperty
    public Long getPrevItemId() {
        return batchJobItem.getPrevItemId();
    }

    @JsonProperty
    public BatchJobItem.RelationType getRelationType() {
        return batchJobItem.getRelationType();
    }

    @JsonProperty
    public Long getRelatedId() {
        return batchJobItem.getRelatedId();
    }

    @JsonProperty
    public Boolean getLastInQueue() {
        return batchJobItem.getLastInQueue();
    }

    @JsonProperty
    public Boolean getFirstInQueue() {
        return batchJobItem.getFirstInQueue();
    }

    @JsonProperty
    public Integer getPositionInQueue() {
        return batchJobItem.getPositionInQueue();
    }

    @JsonProperty
    public LocalDateTime getProcessAt() {
        return batchJobItem.getProcessAt();
    }

    @JsonProperty
    public Long getDuration() {
        return batchJobItem.getDuration();
    }

    @JsonProperty
    public String getError() {
        return batchJobItem.getError();
    }

    @JsonProperty
    public BatchJobItem.State getState() {
        return batchJobItem.getState();
    }
}
