package io.picthor.data.mapper;

import com.realcnbs.horizon.framework.data.mapper.EntityMapper;
import io.picthor.data.entity.FileData;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface FileDataMapper extends EntityMapper<FileData> {

    List<Long> findIdsByRootDirectoryId(@Param("id") Long id);

    List<Map<String, Object>> getAllExtensions();

}
