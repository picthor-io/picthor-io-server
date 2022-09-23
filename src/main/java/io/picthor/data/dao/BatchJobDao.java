package io.picthor.data.dao;

import com.realcnbs.horizon.framework.data.dao.entity.AbstractEntityDao;
import com.realcnbs.horizon.framework.data.mapper.EntityMapper;
import io.picthor.data.entity.BatchJob;
import io.picthor.data.entity.Directory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class BatchJobDao extends AbstractEntityDao<BatchJob> {

    private final Map<Long, BatchJob> jobStore = new HashMap<>();

    private final AtomicLong id = new AtomicLong();

    public List<BatchJob> findByRooDirectory(Directory directory) {
        return jobStore.values()
                       .stream()
                       .filter(batchJob -> batchJob.getRootDirectoryId().equals(directory.getId()))
                       .collect(Collectors.toList());
    }

    @Override
    protected EntityMapper<BatchJob> getMapper() {
        return null;
    }

    public void persist(BatchJob job) {
        if (job.getId() == null) {
            job.setId(id.incrementAndGet());
        }
        jobStore.put(job.getId(), job);
    }

    public void remove(BatchJob job) {
        jobStore.remove(job.getId());
    }

    public BatchJob findById(Long jobId) {
        for (BatchJob jobItem : jobStore.values()) {
            if (jobItem.getId().equals(jobId)) {
                return jobItem;
            }
        }
        return null;
    }

    @Override
    public List<BatchJob> findAll() {
        return new ArrayList<>(jobStore.values());
    }

}
