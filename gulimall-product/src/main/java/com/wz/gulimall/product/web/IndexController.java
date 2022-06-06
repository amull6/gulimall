package com.wz.gulimall.product.web;

import com.wz.gulimall.product.entity.CategoryEntity;
import com.wz.gulimall.product.service.CategoryService;
import com.wz.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redisson;

    @RequestMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        List<CategoryEntity> categories = categoryService.getLevel1Categories();
        model.addAttribute("categories", categories);
        return "index";
    }

    @RequestMapping("/index/json/catalog.json")
    @ResponseBody
    public Map<String, List<Catalog2Vo>> catalogJson(Model model) {
        Map<String, List<Catalog2Vo>> map = categoryService.getlevel2Categories();
        return map;
    }


    @RequestMapping("/index/hello")
    @ResponseBody
    public void hello() {
        RLock rLock = redisson.getLock("my-lock");
        rLock.lock();
        try {
            System.out.println("加锁" + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (Exception e) {

        } finally {
            // 如果这里宕机：有看门狗，不用担心
            System.out.println("解锁" + Thread.currentThread().getId());
            rLock.unlock();
        }
    }
}
