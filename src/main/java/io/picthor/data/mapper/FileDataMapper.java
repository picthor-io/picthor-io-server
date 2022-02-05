package io.picthor.data.mapper;

import com.realcnbs.horizon.framework.data.mapper.EntityMapper;
import io.picthor.data.entity.FileData;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;

public interface FileDataMapper extends EntityMapper<FileData> {

    List<Long> findIdsByRootDirectoryId(@Param("id") Long id);

}
