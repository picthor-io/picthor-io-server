package io.picthor.batch.processor;

import io.picthor.batch.BatchProcessingException;
import io.picthor.data.dao.BatchJobDao;
import io.picthor.data.dao.BatchJobItemDao;
import io.picthor.data.entity.BatchJob;
import io.picthor.data.entity.BatchJobItem;

import java.util.List;
import java.util.Map;

public abstract class AbstractBatchJobProcessor {

    protected BatchJobDao batchJobDao;

    protected BatchJobItemDao batchJobItemDao;

    public abstract BatchJob createJob(Map<String, Object> parameters) throws BatchProcessingException;

    public abstract void cleanup(BatchJob batchJob) throws BatchProcessingException;

    public abstract void processItem(BatchJobItem item) throws BatchProcessingException;

    protected void setPrevAndNextIds(List<BatchJobItem> items) {
        // set prev and next ids for job items
        for (int j = 0; j < items.size(); j++) {
            BatchJobItem item = items.get(j);
            try {
                BatchJobItem nextItem = items.get(j + 1);
                item.setNextItemId(nextItem.getId());
            } catch (IndexOutOfBoundsException e) {
                // ignore
            }
            if (j != 0) {
                item.setPrevItemId(items.get(j - 1).getId());
            }
            batchJobItemDao.persist(item);
        }
    }
}
