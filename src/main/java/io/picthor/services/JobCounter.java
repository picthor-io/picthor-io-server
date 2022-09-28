package io.picthor.services;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class JobCounter {

    public JobCounter(Long jobId, Long rootDirectoryId, Integer total) {
        this.jobId = jobId;
        this.rootDirectoryId = rootDirectoryId;
        this.total = total;
        counter = new AtomicInteger(0);
        startedAt = System.nanoTime();
    }

    private Long jobId;

    private Long rootDirectoryId;

    private AtomicInteger counter;

    private Integer total;

    private Long startedAt;

}
