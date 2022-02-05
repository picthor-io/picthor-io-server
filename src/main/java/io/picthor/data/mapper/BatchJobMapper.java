package io.picthor.data.mapper;


import com.realcnbs.horizon.framework.data.mapper.EntityMapper;
import io.picthor.data.entity.BatchJob;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BatchJobMapper extends EntityMapper<BatchJob> {

    List<BatchJob> findByRooDirectoryId(@Param("rootDirectoryId") String rootDirectoryId);

}
