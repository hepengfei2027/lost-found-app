package cn.jxufe.iet.lostandfound.service;

import cn.jxufe.iet.lostandfound.entity.AppConfig;

import java.util.List;

public interface AppConfigService {
    List<AppConfig> getAll();
    
    AppConfig getByKey(String key);
    
    void saveOrUpdate(String key, String value, String description);
    
    String getValue(String key);
}
