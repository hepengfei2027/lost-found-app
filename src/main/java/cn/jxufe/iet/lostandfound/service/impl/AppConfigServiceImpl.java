package cn.jxufe.iet.lostandfound.service.impl;

import cn.jxufe.iet.lostandfound.entity.AppConfig;
import cn.jxufe.iet.lostandfound.mapper.AppConfigMapper;
import cn.jxufe.iet.lostandfound.service.AppConfigService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppConfigServiceImpl implements AppConfigService {

    @Autowired
    private AppConfigMapper appConfigMapper;

    @Override
    public List<AppConfig> getAll() {
        return appConfigMapper.selectList(null);
    }

    @Override
    public AppConfig getByKey(String key) {
        QueryWrapper<AppConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("config_key", key);
        return appConfigMapper.selectOne(wrapper);
    }

    @Override
    public void saveOrUpdate(String key, String value, String description) {
        AppConfig existing = getByKey(key);
        if (existing != null) {
            existing.setConfigValue(value);
            existing.setDescription(description);
            appConfigMapper.updateById(existing);
        } else {
            AppConfig config = new AppConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setDescription(description);
            appConfigMapper.insert(config);
        }
    }

    @Override
    public String getValue(String key) {
        AppConfig config = getByKey(key);
        return config != null ? config.getConfigValue() : null;
    }
}
