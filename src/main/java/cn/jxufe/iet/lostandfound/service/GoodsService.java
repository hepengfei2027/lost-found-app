package cn.jxufe.iet.lostandfound.service;

import cn.jxufe.iet.lostandfound.entity.Goods;

import java.util.List;

public interface GoodsService {
    Goods save(Goods goods, List<String> mediaUrls);

    List<Goods> getLostList();

    List<Goods> getFoundList();

    List<Goods> getAllPendingList();

    Goods getById(Integer id);

    List<Goods> getMyList(Integer userId, Integer type, Integer status);

    void updateStatus(Integer id, Integer status);

    List<Goods> search(String keyword, Integer type);

    Goods update(Goods goods, List<String> mediaUrls);

    void delete(Integer id);
}
