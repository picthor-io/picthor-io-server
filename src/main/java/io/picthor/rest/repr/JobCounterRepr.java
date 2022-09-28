package io.picthor.rest.repr;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.picthor.services.JobCounter;

public class JobCounterRepr {

    private final JobCounter jobCounter;

    public JobCounterRepr(JobCounter jobCounter) {
        this.jobCounter = jobCounter;
    }

    @JsonProperty
    public Integer getTotal() {
        if (jobCounter != null) {
            return jobCounter.getTotal();
        }
        return null;
    }

    @JsonProperty
    public Integer getCurrent() {
        if (jobCounter != null) {
            return jobCounter.getCounter().get();
        }
        return null;
    }

    @JsonProperty
    private Long getJobId() {
        return jobCounter.getJobId();
    }

    @JsonProperty
    private Long getRootDirectoryId() {
        return jobCounter.getRootDirectoryId();
    }

}
