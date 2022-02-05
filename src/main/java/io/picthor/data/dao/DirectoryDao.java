package io.picthor.data.dao;

import com.realcnbs.horizon.framework.data.dao.entity.AbstractEntityDao;
import com.realcnbs.horizon.framework.data.entity.AbstractEntity;
import com.realcnbs.horizon.framework.data.filter.FieldFilter;
import com.realcnbs.horizon.framework.data.filter.FilterBuilder;
import com.realcnbs.horizon.framework.data.filter.FilterDefinition;
import io.picthor.data.entity.Directory;
import io.picthor.data.entity.DirectoryStats;
import io.picthor.data.mapper.DirectoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class DirectoryDao extends AbstractEntityDao<Directory> {

    private final DirectoryMapper mapper;

    public DirectoryDao(DirectoryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected DirectoryMapper getMapper() {
        return mapper;
    }

    @Override
    public Directory findById(Long id) {
        Directory directory = super.findById(id);
        loadParents(directory);
        return directory;
    }

    private void loadParents(Directory directory) {
        if (directory.getParentId() != null) {
            directory.setParent(findById(directory.getParentId()));
            if (directory.getParent().getParentId() != null) {
                loadParents(directory.getParent());
            }
        }
    }

    @Override
    public List<FilterDefinition> getAllowedFilters() {
        List<FilterDefinition> filters = new ArrayList<>();
        filters.add(new FilterDefinition("id", FieldFilter.CheckType.EQUALS, FilterDefinition.DataType.NUMBER));
        filters.add(new FilterDefinition("state", FieldFilter.CheckType.EQUALS, FilterDefinition.DataType.STRING));
        filters.add(new FilterDefinition("parent_id", FieldFilter.CheckType.EQUALS, FilterDefinition.DataType.NUMBER));
        filters.add(new FilterDefinition("type", FieldFilter.CheckType.EQUALS, FilterDefinition.DataType.STRING));
        return filters;
    }

    public void fetchTree(Directory root) {
        List<Directory> directories = mapper.fetchFlatTree(root.getId());
        Map<Long, Directory> idMap = directories.stream().collect(Collectors.toMap(AbstractEntity::getId, directory -> directory));
        idMap.put(root.getId(), root);
        for (Directory directory : idMap.values()) {
            if (directory.getParentId() != null) {
                directory.setParent(idMap.get(directory.getParentId()));
                if (directory.getParent().getChildren() == null) {
                    directory.getParent().setChildren(new ArrayList<>());
                }
                if (!directory.getParent().getChildren().contains(directory)) {
                    directory.getParent().getChildren().add(directory);
                }
            }
        }
    }

    public void updateFileNumStats() {
        mapper.updateFileNumStats();
    }
    public void updateDirNumStats() {
        mapper.updateDirNumStats();
    }

    public void persistStatsMaps(Map<Long, DirectoryStats> statsMap) {
        mapper.persistStatsMaps(statsMap);
    }

    public Directory findByFullPath(String path) {
        FilterBuilder builder = FilterBuilder.instance();
        builder.and("full_path").eq(path);
        return mapper.findOneFiltered(builder);
    }

    public List<Directory> findByFullPath(List<String> paths) {
        FilterBuilder builder = FilterBuilder.instance();
        builder.and("full_path").in(paths);
        return mapper.findAllFiltered(builder);
    }

    public Directory findByName(String name) {
        FilterBuilder builder = FilterBuilder.instance();
        builder.and("name").eq(name);
        return mapper.findOneFiltered(builder);
    }

    public List<Directory> findByType(Directory.Type type) {
        FilterBuilder builder = FilterBuilder.instance();
        builder.and("type").eq(type);
        return mapper.findAllFiltered(builder);
    }
}
