package io.picthor.data.dao;

import com.realcnbs.horizon.framework.data.dao.entity.AbstractEntityDao;
import com.realcnbs.horizon.framework.data.filter.FilterBuilder;
import com.realcnbs.horizon.framework.data.mapper.EntityMapper;
import io.picthor.data.entity.Setting;
import io.picthor.data.mapper.SettingMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SettingDao extends AbstractEntityDao<Setting> {

    private final SettingMapper mapper;

    public SettingDao(SettingMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected EntityMapper<Setting> getMapper() {
        return mapper;
    }

    public Setting findByName(String name) {
        FilterBuilder builder = FilterBuilder.instance();
        builder.and("name").eq(name);
        return mapper.findOneFiltered(builder);
    }
}
