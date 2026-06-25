package cn.jxufe.iet.lostandfound.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("lost_goods")
public class Goods {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("user_id")
    private Integer userId;

    private Integer type;

    private String title;

    private String content;

    private String place;

    private String phone;

    @TableField("img_url")
    private String imgUrl;

    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<Media> mediaList;
}
