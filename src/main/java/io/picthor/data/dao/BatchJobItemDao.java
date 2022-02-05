package io.picthor.data.dao;

import com.realcnbs.horizon.framework.data.dao.entity.AbstractEntityDao;
import com.realcnbs.horizon.framework.data.filter.FieldFilter;
import com.realcnbs.horizon.framework.data.filter.FilterBuilder;
import com.realcnbs.horizon.framework.data.filter.FilterDefinition;
import io.picthor.data.entity.BatchJobItem;
import io.picthor.data.mapper.BatchJobItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class BatchJobItemDao extends AbstractEntityDao<BatchJobItem> {

    private final BatchJobItemMapper batchJobItemMapper;

    @Autowired
    public BatchJobItemDao(BatchJobItemMapper batchJobItemMapper) {
        this.batchJobItemMapper = batchJobItemMapper;
    }

    @Override
    public BatchJobItemMapper getMapper() {
        return batchJobItemMapper;
    }

    @Async
    public void persistAsync(BatchJobItem item) {
        super.persist(item);
    }

    @Override
    public List<FilterDefinition> getAllowedFilters() {
        List<FilterDefinition> filters = new ArrayList<>();
        filters.add(new FilterDefinition("id", FieldFilter.CheckType.EQUALS, FilterDefinition.DataType.NUMBER));
        filters.add(new FilterDefinition("state", FieldFilter.CheckType.LIKE, FilterDefinition.DataType.STRING));
        filters.add(new FilterDefinition("batchJobId", FieldFilter.CheckType.EQUALS, FilterDefinition.DataType.NUMBER));
        return filters;
    }

    public List<BatchJobItem> findByJobId(Long jobId){
        FilterBuilder builder = FilterBuilder.instance();
        builder.and("batch_job_id").eq(jobId);
        return batchJobItemMapper.findAllFiltered(builder);
    }

}
