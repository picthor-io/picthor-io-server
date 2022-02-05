package io.picthor.data.entity;


import com.realcnbs.horizon.framework.data.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class BatchJobItem extends AbstractEntity {

    public enum State {
        NEW, PROCESSING, PROCESSED, ERROR
    }

    public enum RelationType {
    }

    private BatchJob batchJob;
    private Long batchJobId;
    private Long nextItemId;
    private Long prevItemId;
    private RelationType relationType;
    private Long relatedId;
    private String payload;
    private Boolean lastInQueue;
    private Boolean firstInQueue;
    private Integer positionInQueue;
    private LocalDateTime processAt;
    private Long duration;
    private String error;
    private State state;
    private Integer internalTotal;
    private Integer internalProcessed;

    public BatchJob getBatchJob() {
        return batchJob;
    }

    public void setBatchJob(BatchJob batchJob) {
        if (batchJob != null) this.batchJobId = batchJob.getId();
        this.batchJob = batchJob;
    }

    public Long getBatchJobId() {
        return batchJobId != null ? batchJobId : batchJob != null ? batchJob.getId() : null;
    }

    public void setBatchJobId(Long batchJobId) {
        this.batchJobId = batchJobId;
    }

}
