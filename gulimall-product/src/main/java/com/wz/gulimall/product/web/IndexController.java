package com.wz.gulimall.product.web;

import com.alibaba.nacos.common.util.UuidUtils;
import com.wz.gulimall.product.entity.CategoryEntity;
import com.wz.gulimall.product.service.CategoryService;
import com.wz.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redisson;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

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

    @RequestMapping("/index/tryLock")
    @ResponseBody
    public void testTryLock() throws InterruptedException {
        RLock rLock = redisson.getLock("my-lock");
        boolean res = rLock.tryLock(15, 50, TimeUnit.SECONDS);
        if (res) {
            try {
                System.out.println("加锁" + Thread.currentThread().getId());
                Thread.sleep(30000);
            } catch (Exception e) {

            } finally {
                // 如果这里宕机：有看门狗，不用担心
                System.out.println("解锁" + Thread.currentThread().getId());
                rLock.unlock();
            }
        } else {
            System.out.println("我不等了");
        }
    }

    @RequestMapping("/index/read")
    @ResponseBody
    public void read() {
        RReadWriteLock rwLock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = rwLock.readLock();
        rLock.lock();
        try {
            String str = stringRedisTemplate.opsForValue().get("qinjieID");
            System.out.println(str);
        } finally {
            rLock.unlock();
        }
    }

    @RequestMapping("/index/write")
    @ResponseBody
    public void write() {
        RReadWriteLock rwLock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = rwLock.writeLock();
        rLock.lock();
        try {
            String uuid = UuidUtils.generateUuid();
            Thread.sleep(10000);
            stringRedisTemplate.opsForValue().set("qinjieID", uuid);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            rLock.unlock();
        }
    }

    @RequestMapping("/index/door")
    @ResponseBody
    public String door() throws InterruptedException {
        RCountDownLatch rCountDownLatch = redisson.getCountDownLatch("count-down-latch");
        rCountDownLatch.trySetCount(5);
        rCountDownLatch.await();
        return "关门ok";
    }

    @RequestMapping("/index/go/{id}")
    @ResponseBody
    public String go(@PathVariable("id") Long id) {
        RCountDownLatch rCountDownLatch = redisson.getCountDownLatch("count-down-latch");
        rCountDownLatch.countDown();
        return id + "班放学";
    }

    @RequestMapping("index/park")
    @ResponseBody
    public String park() {
        RSemaphore rSemaphore = redisson.getSemaphore("part");
        boolean isPark = rSemaphore.tryAcquire();
        if (isPark) {
            return "ok";
        }else{
            return "error";
        }
    }

    @RequestMapping("index/go")
    @ResponseBody
    public String go() {
        RSemaphore rSemaphore = redisson.getSemaphore("part");
        rSemaphore.release();
        return "go";
    }
}
