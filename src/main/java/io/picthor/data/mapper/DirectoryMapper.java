package io.picthor.data.mapper;

import com.realcnbs.horizon.framework.data.mapper.EntityMapper;
import io.picthor.data.entity.Directory;
import io.picthor.data.entity.DirectoryStats;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface DirectoryMapper extends EntityMapper<Directory> {

    void updateFileNumStats();

    void updateDirNumStats();

    List<Directory> fetchFlatTree(@Param("rootId") Long rootId);

    void persistStatsMaps(@Param("statsMap") Map<Long, DirectoryStats> statsMap);
}
