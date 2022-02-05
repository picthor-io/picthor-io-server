package io.picthor.data.dao;

import com.realcnbs.horizon.framework.data.dao.entity.AbstractEntityDao;
import com.realcnbs.horizon.framework.data.filter.FilterBuilder;
import io.picthor.data.entity.BatchJob;
import io.picthor.data.entity.Directory;
import io.picthor.data.mapper.BatchJobMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class BatchJobDao extends AbstractEntityDao<BatchJob> {

    private final BatchJobMapper batchJobMapper;

    @Autowired
    public BatchJobDao(BatchJobMapper batchJobMapper) {
        this.batchJobMapper = batchJobMapper;
    }

    @Override
    public BatchJobMapper getMapper() {
        return batchJobMapper;
    }

    public List<BatchJob> findAllByCreateDateInterval(LocalDateTime start, LocalDateTime end) {
        FilterBuilder builder = FilterBuilder.instance();
        builder.and("created_at").geq(start);
        builder.and("created_at").leq(end);
        return batchJobMapper.findAllFiltered(builder);
    }

    public List<BatchJob> findByRooDirectory(Directory directory) {
        return batchJobMapper.findByRooDirectoryId(String.valueOf(directory.getId()));
    }
}
