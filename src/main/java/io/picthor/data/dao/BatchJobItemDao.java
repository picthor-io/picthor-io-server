package io.picthor.data.dao;

import com.realcnbs.horizon.framework.data.dao.entity.AbstractEntityDao;
import com.realcnbs.horizon.framework.data.mapper.EntityMapper;
import io.picthor.data.entity.BatchJobItem;
import org.springframework.stereotype.Repository;

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
        return jobItemStore.values()
                           .stream()
                           .filter(jobItem -> jobItem.getId().equals(jobItemId))
                           .findAny()
                           .orElse(null);
    }

    public List<BatchJobItem> findByJobId(Long jobId) {
        return jobItemStore.values()
                           .stream()
                           .filter(jobItem -> jobItem.getBatchJobId().equals(jobId))
                           .collect(Collectors.toList());
    }


//
//    @Async
//    public void persistAsync(BatchJobItem item) {
//        super.persist(item);
//    }
//
//    @Override
//    public List<FilterDefinition> getAllowedFilters() {
//        List<FilterDefinition> filters = new ArrayList<>();
//        filters.add(new FilterDefinition("id", FieldFilter.CheckType.EQUALS, FilterDefinition.DataType.NUMBER));
//        filters.add(new FilterDefinition("state", FieldFilter.CheckType.LIKE, FilterDefinition.DataType.STRING));
//        filters.add(new FilterDefinition("batchJobId", FieldFilter.CheckType.EQUALS, FilterDefinition.DataType.NUMBER));
//        return filters;
//    }
//
//    public List<BatchJobItem> findByJobId(Long jobId){
//        FilterBuilder builder = FilterBuilder.instance();
//        builder.and("batch_job_id").eq(jobId);
//        return batchJobItemMapper.findAllFiltered(builder);
//    }

}
