package io.picthor.batch;

import io.picthor.data.dao.BatchJobItemDao;
import io.picthor.data.entity.BatchJobItem;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class BatchJobItemJob implements Job {

    private Long batchJobItemId;

    private final BatchJobItemDao jobItemDao;

    private final BatchJobService batchJobService;

    public BatchJobItemJob(BatchJobItemDao jobItemDao, BatchJobService batchJobService) {
        this.jobItemDao = jobItemDao;
        this.batchJobService = batchJobService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        BatchJobItem item = jobItemDao.findById(batchJobItemId);
        batchJobService.processJobItem(item);
    }

    public Long getBatchJobItemId() {
        return batchJobItemId;
    }

    public void setBatchJobItemId(Long batchJobItemId) {
        this.batchJobItemId = batchJobItemId;
    }
}
