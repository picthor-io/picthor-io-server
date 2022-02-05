package io.picthor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.picthor.data.dao.SettingDao;
import io.picthor.data.entity.Setting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SettingService {

    private final SettingDao settingsDao;

    private final ObjectMapper objectMapper;

    public SettingService(SettingDao settingDao, ObjectMapper objectMapper) {
        this.settingsDao = settingDao;
        this.objectMapper = objectMapper;
    }

}
