package io.picthor.data.dao;

import com.realcnbs.horizon.framework.data.dao.entity.AbstractEntityDao;
import com.realcnbs.horizon.framework.data.mapper.EntityMapper;
import io.picthor.data.entity.BatchJob;
import io.picthor.data.entity.Directory;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class BatchJobDao extends AbstractEntityDao<BatchJob>{

    private final Map<Long, BatchJob> jobStore = new HashMap<>();

    private final AtomicLong id = new AtomicLong();

    public List<BatchJob> findByRooDirectory(Directory directory) {
        return jobStore.values()
                       .stream()
                       .filter(batchJob -> batchJob.getPayload().get("rootDirectoryId").equals(directory.getRootDirectoryId()))
                       .collect(Collectors.toList());
    }

    public void remove(BatchJob job) {
        jobStore.remove(job.getId());
    }

    public void persist(BatchJob job) {
        if (job.getId() == null) {
            job.setId(id.incrementAndGet());
        }
        jobStore.put(job.getId(), job);
    }

    public BatchJob findById(Long jobId) {
        return jobStore.values()
                       .stream()
                       .filter(batchJob -> batchJob.getId().equals(jobId))
                       .findAny()
                       .orElse(null);
    }

    public List<BatchJob> findAllByCreateDateInterval(LocalDateTime start, LocalDateTime end) {
        return jobStore.values()
                       .stream()
                       .filter(batchJob -> batchJob.getCreatedAt().isAfter(start) && batchJob.getCreatedAt().isBefore(end))
                       .collect(Collectors.toList());
    }

    @Override
    protected EntityMapper<BatchJob> getMapper() {
        return null;
    }

}
