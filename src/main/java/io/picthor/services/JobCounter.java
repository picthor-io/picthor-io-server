package io.picthor.services;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class JobCounter {

    public JobCounter(Long jobId, Integer total) {
        this.jobId = jobId;
        this.total = total;
        counter = new AtomicInteger(0);
        startedAt = System.nanoTime();
    }

    private Long jobId;

    private AtomicInteger counter;

    private Integer total;

    private Long startedAt;

}
