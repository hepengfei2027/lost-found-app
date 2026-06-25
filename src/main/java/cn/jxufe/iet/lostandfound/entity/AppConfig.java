package cn.jxufe.iet.lostandfound.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("app_config")
public class AppConfig {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private String configKey;
    
    private String configValue;
    
    private String description;
}
