package io.picthor.rest.repr;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.realcnbs.horizon.framework.rest.repr.AbstractEntityRepr;
import io.picthor.data.entity.BatchJob;
import io.picthor.services.JobCounter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

public class BatchJobRepr extends AbstractEntityRepr {

    private final BatchJob batchJob;

    @Setter
    private JobCounter counter;

    public BatchJobRepr(BatchJob batchJob) {
        super(batchJob);
        this.batchJob = batchJob;
    }

    @JsonProperty
    public JobCounterRepr getCounter() {
        if (counter != null) {
            return new JobCounterRepr(counter);
        }
        return null;
    }

    @JsonProperty
    public String getName() {
        return batchJob.getName();
    }

    @JsonProperty
    public BatchJob.Type getType() {
        return batchJob.getType();
    }

    @JsonProperty
    public BatchJob.ProcessType getProcessType() {
        return batchJob.getProcessType();
    }

    @JsonProperty
    public BatchJob.State getState() {
        return batchJob.getState();
    }

    @JsonProperty
    public String getProcessAt() {
        return batchJob == null ? null : batchJob.getProcessAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    @JsonProperty
    public Integer getTotalItems() {
        return batchJob.getTotalItems();
    }

    @JsonProperty
    public Integer getTotalProcessed() {
        return batchJob.getTotalProcessed();
    }

    @JsonProperty
    public Long getRootDirectoryId() {
        return batchJob.getRootDirectoryId();
    }
}
