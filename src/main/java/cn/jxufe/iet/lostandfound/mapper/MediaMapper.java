package cn.jxufe.iet.lostandfound.mapper;

import cn.jxufe.iet.lostandfound.entity.Media;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MediaMapper extends BaseMapper<Media> {

    @Select("SELECT * FROM lost_media WHERE parent_id = #{parentId} AND parent_type = #{parentType} ORDER BY sort_order")
    List<Media> selectByParent(Integer parentId, String parentType);
}
