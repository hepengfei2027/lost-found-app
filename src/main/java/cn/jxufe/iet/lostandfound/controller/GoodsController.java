package cn.jxufe.iet.lostandfound.controller;

import cn.jxufe.iet.lostandfound.common.Result;
import cn.jxufe.iet.lostandfound.entity.Goods;
import cn.jxufe.iet.lostandfound.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @PostMapping("/save")
    public Result<Goods> save(@RequestBody Goods goods, @RequestParam(required = false) List<String> mediaUrls) {
        Goods saved = goodsService.save(goods, mediaUrls);
        return Result.success(saved);
    }

    @GetMapping("/lost/list")
    public Result<List<Goods>> getLostList() {
        List<Goods> list = goodsService.getLostList();
        return Result.success(list);
    }

    @GetMapping("/found/list")
    public Result<List<Goods>> getFoundList() {
        List<Goods> list = goodsService.getFoundList();
        return Result.success(list);
    }

    @GetMapping("/all/list")
    public Result<List<Goods>> getAllPendingList() {
        List<Goods> list = goodsService.getAllPendingList();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Goods> getById(@PathVariable Integer id) {
        Goods goods = goodsService.getById(id);
        return Result.success(goods);
    }

    @GetMapping("/my")
    public Result<List<Goods>> getMyList(@RequestParam Integer userId,
                                         @RequestParam(required = false) Integer type,
                                         @RequestParam(required = false) Integer status) {
        List<Goods> list = goodsService.getMyList(userId, type, status);
        return Result.success(list);
    }

    @PostMapping("/status")
    public Result<Void> updateStatus(@RequestParam Integer id, @RequestParam Integer status) {
        goodsService.updateStatus(id, status);
        return Result.success(null);
    }

    @GetMapping("/search")
    public Result<List<Goods>> search(@RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Integer type) {
        List<Goods> list = goodsService.search(keyword, type);
        return Result.success(list);
    }

    @PutMapping("/update")
    public Result<Goods> update(@RequestBody Goods goods, @RequestParam(required = false) List<String> mediaUrls) {
        Goods updated = goodsService.update(goods, mediaUrls);
        if (updated == null) {
            return Result.error("物品不存在");
        }
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        goodsService.delete(id);
        return Result.success(null);
    }
}
