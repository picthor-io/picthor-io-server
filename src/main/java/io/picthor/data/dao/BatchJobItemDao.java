package io.picthor.data.dao;

import com.realcnbs.horizon.framework.data.dao.entity.AbstractEntityDao;
import com.realcnbs.horizon.framework.data.mapper.EntityMapper;
import io.picthor.data.entity.BatchJobItem;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class BatchJobItemDao extends AbstractEntityDao<BatchJobItem> {

    private final Map<Long, BatchJobItem> jobItemStore = new HashMap<>();

    private final AtomicLong id = new AtomicLong();

    @Override
    protected EntityMapper<BatchJobItem> getMapper() {
        return null;
    }

    public void persist(BatchJobItem jobItem) {
        if (jobItem.getId() == null) {
            jobItem.setId(id.incrementAndGet());
        }
        jobItemStore.put(jobItem.getId(), jobItem);
    }

    public void remove(BatchJobItem jobItem) {
        jobItemStore.remove(jobItem.getId());
    }

    public BatchJobItem findById(Long jobItemId) {
        for (BatchJobItem jobItem : jobItemStore.values()) {
            if (jobItem.getId().equals(jobItemId)) {
                return jobItem;
            }
        }
        return null;
    }

    public List<BatchJobItem> findByJobId(Long jobId) {
        List<BatchJobItem> items = new ArrayList<>();
        for (BatchJobItem jobItem : jobItemStore.values()) {
            if (jobItem.getBatchJobId().equals(jobId)) {
                items.add(jobItem);
            }
        }
        return items;
    }

}
