package io.picthor.data.dao;

import com.realcnbs.horizon.framework.data.dao.entity.AbstractEntityDao;
import com.realcnbs.horizon.framework.data.filter.FieldFilter;
import com.realcnbs.horizon.framework.data.filter.FilterBuilder;
import com.realcnbs.horizon.framework.data.filter.FilterDefinition;
import com.realcnbs.horizon.framework.data.filter.SortDefinition;
import com.realcnbs.horizon.framework.data.mapper.EntityMapper;
import io.picthor.data.entity.Directory;
import io.picthor.data.mapper.FileDataMapper;
import io.picthor.data.entity.FileData;
import io.picthor.services.DirectoryStatsService;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FileDataDao extends AbstractEntityDao<FileData> {

    private final FileDataMapper mapper;

    private final DirectoryDao directoryDao;

    private final DirectoryStatsService statsService;

    public FileDataDao(FileDataMapper mapper, DirectoryDao directoryDao, DirectoryStatsService statsService) {
        this.mapper = mapper;
        this.directoryDao = directoryDao;
        this.statsService = statsService;
    }

    @Override
    protected EntityMapper<FileData> getMapper() {
        return mapper;
    }

    @Override
    public List<SortDefinition> getAllowedSorts() {
        List<SortDefinition> sorts = super.getAllowedSorts();
        sorts.add(new SortDefinition("created_at"));
        return sorts;
    }

    public FileData findByFullPath(String name) {
        FilterBuilder builder = FilterBuilder.instance();
        builder.and("full_path").eq(name);
        return mapper.findOneFiltered(builder);
    }

    public List<FileData> findByRootDirectory(Directory directory) {
        FilterBuilder builder = FilterBuilder.instance();
        builder.and("root_directory_id").eq(directory.getId());
        return mapper.findAllFiltered(builder);
    }

    public List<FileData> findByDirectory(Directory directory) {
        FilterBuilder builder = FilterBuilder.instance();
        builder.and("directory_id").eq(directory.getId());
        return mapper.findAllFiltered(builder);
    }

    public List<Long> findIdsByRootDirectoryId(Long id) {
        return mapper.findIdsByRootDirectoryId(id);
    }

    @Override
    public List<FilterDefinition> getAllowedFilters() {
        List<FilterDefinition> filters = super.getAllowedFilters();
        filters.add(new FilterDefinition("directory_id", FieldFilter.CheckType.EQUALS, FilterDefinition.DataType.NUMBER));
        filters.add(new FilterDefinition("state", FieldFilter.CheckType.EQUALS, FilterDefinition.DataType.STRING));
        filters.add(new FilterDefinition("type", FieldFilter.CheckType.EQUALS, FilterDefinition.DataType.STRING));
        return filters;
    }
}
