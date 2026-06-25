package cn.jxufe.iet.lostandfound.service.impl;

import cn.jxufe.iet.lostandfound.entity.Goods;
import cn.jxufe.iet.lostandfound.entity.Media;
import cn.jxufe.iet.lostandfound.mapper.GoodsMapper;
import cn.jxufe.iet.lostandfound.mapper.MediaMapper;
import cn.jxufe.iet.lostandfound.service.GoodsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GoodsServiceImpl implements GoodsService {

    private static final String PARENT_TYPE_GOODS = "goods";
    private static final Integer STATUS_PENDING = 0;
    private static final Integer STATUS_RESOLVED = 1;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private MediaMapper mediaMapper;

    @Override
    @Transactional
    public Goods save(Goods goods, List<String> mediaUrls) {
        goods.setStatus(STATUS_PENDING);
        goodsMapper.insert(goods);

        if (mediaUrls != null && !mediaUrls.isEmpty()) {
            for (int i = 0; i < mediaUrls.size(); i++) {
                String url = mediaUrls.get(i);
                Media media = new Media();
                media.setParentId(goods.getId());
                media.setParentType(PARENT_TYPE_GOODS);
                media.setUrl(url);
                media.setType(detectMediaType(url));
                media.setSortOrder(i);
                media.setCreateTime(LocalDateTime.now());
                mediaMapper.insert(media);
            }
        }

        goods.setMediaList(mediaMapper.selectByParent(goods.getId(), PARENT_TYPE_GOODS));
        return goods;
    }

    @Override
    public List<Goods> getLostList() {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getType, 1).eq(Goods::getStatus, STATUS_PENDING).orderByDesc(Goods::getCreateTime);
        List<Goods> list = goodsMapper.selectList(wrapper);
        for (Goods goods : list) {
            goods.setMediaList(mediaMapper.selectByParent(goods.getId(), PARENT_TYPE_GOODS));
        }
        return list;
    }

    @Override
    public List<Goods> getFoundList() {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getType, 2).eq(Goods::getStatus, STATUS_PENDING).orderByDesc(Goods::getCreateTime);
        List<Goods> list = goodsMapper.selectList(wrapper);
        for (Goods goods : list) {
            goods.setMediaList(mediaMapper.selectByParent(goods.getId(), PARENT_TYPE_GOODS));
        }
        return list;
    }

    @Override
    public List<Goods> getAllPendingList() {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getStatus, STATUS_PENDING).orderByDesc(Goods::getCreateTime);
        List<Goods> list = goodsMapper.selectList(wrapper);
        for (Goods goods : list) {
            goods.setMediaList(mediaMapper.selectByParent(goods.getId(), PARENT_TYPE_GOODS));
        }
        return list;
    }

    @Override
    public Goods getById(Integer id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods != null) {
            goods.setMediaList(mediaMapper.selectByParent(id, PARENT_TYPE_GOODS));
        }
        return goods;
    }

    @Override
    public List<Goods> getMyList(Integer userId, Integer type, Integer status) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getUserId, userId);
        if (type != null) {
            wrapper.eq(Goods::getType, type);
        }
        if (status != null) {
            wrapper.eq(Goods::getStatus, status);
        }
        wrapper.orderByDesc(Goods::getCreateTime);
        List<Goods> list = goodsMapper.selectList(wrapper);
        for (Goods goods : list) {
            goods.setMediaList(mediaMapper.selectByParent(goods.getId(), PARENT_TYPE_GOODS));
        }
        return list;
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        Goods goods = goodsMapper.selectById(id);
        if (goods != null) {
            goods.setStatus(status);
            goodsMapper.updateById(goods);
        }
    }

    @Override
    public List<Goods> search(String keyword, Integer type) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getStatus, STATUS_PENDING);
        if (type != null) {
            wrapper.eq(Goods::getType, type);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Goods::getTitle, keyword).or().like(Goods::getContent, keyword));
        }
        wrapper.orderByDesc(Goods::getCreateTime);
        List<Goods> list = goodsMapper.selectList(wrapper);
        for (Goods goods : list) {
            goods.setMediaList(mediaMapper.selectByParent(goods.getId(), PARENT_TYPE_GOODS));
        }
        return list;
    }

    @Override
    @Transactional
    public Goods update(Goods goods, List<String> mediaUrls) {
        Goods existing = goodsMapper.selectById(goods.getId());
        if (existing == null) {
            return null;
        }

        existing.setTitle(goods.getTitle());
        existing.setContent(goods.getContent());
        existing.setPlace(goods.getPlace());
        existing.setPhone(goods.getPhone());
        existing.setType(goods.getType());

        goodsMapper.updateById(existing);

        if (mediaUrls != null && !mediaUrls.isEmpty()) {
            mediaMapper.delete(new LambdaQueryWrapper<Media>()
                    .eq(Media::getParentId, goods.getId())
                    .eq(Media::getParentType, PARENT_TYPE_GOODS));

            for (int i = 0; i < mediaUrls.size(); i++) {
                String url = mediaUrls.get(i);
                Media media = new Media();
                media.setParentId(goods.getId());
                media.setParentType(PARENT_TYPE_GOODS);
                media.setUrl(url);
                media.setType(detectMediaType(url));
                media.setSortOrder(i);
                media.setCreateTime(LocalDateTime.now());
                mediaMapper.insert(media);
            }
        }

        existing.setMediaList(mediaMapper.selectByParent(existing.getId(), PARENT_TYPE_GOODS));
        return existing;
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        goodsMapper.deleteById(id);
        mediaMapper.delete(new LambdaQueryWrapper<Media>()
                .eq(Media::getParentId, id)
                .eq(Media::getParentType, PARENT_TYPE_GOODS));
    }

    private Integer detectMediaType(String url) {
        if (url == null) return 1;
        String lower = url.toLowerCase();
        if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mov") || lower.endsWith(".wmv") || lower.endsWith(".flv") || lower.endsWith(".mkv")) {
            return 2;
        }
        return 1;
    }
}
