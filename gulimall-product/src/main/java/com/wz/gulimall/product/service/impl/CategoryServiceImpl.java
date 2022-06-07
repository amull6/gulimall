package com.wz.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wz.gulimall.product.service.CategoryBrandRelationService;
import com.wz.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.Query;

import com.wz.gulimall.product.dao.CategoryDao;
import com.wz.gulimall.product.entity.CategoryEntity;
import com.wz.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        List<CategoryEntity> levelMenus = categoryEntities.stream().filter(categoryEntity -> categoryEntity.getParentCid().longValue() == 0)
                .map((menu) -> {
                    menu.setChildren(this.getChildren(menu, categoryEntities));
                    return menu;
                }).sorted((menu1, menu2) -> {
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                }).collect(Collectors.toList());
        return levelMenus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1检查当前的菜单是否被别的地方所引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] getCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        paths = this.catelogPath(catelogId, paths);
        Collections.reverse(paths);
        return paths.toArray(new Long[paths.size()]);
    }

    @Override
    @Transactional
    public void updateDetail(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            categoryBrandRelationService.updateByCategoryId(category);
        }
    }

    @Override
    public List<CategoryEntity> getLevel1Categories() {
        return this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    @Override
    public Map<String, List<Catalog2Vo>> getlevel2Categories() {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String catalogJson = valueOperations.get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {

//            数据库查询
            Map<String, List<Catalog2Vo>> map = this.getlevel2CategoriesFromDbWithRedissionLock();
            return map;
        }
        System.out.println("缓存获取数据---------");
        return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
        });
    }

    public Map<String, List<Catalog2Vo>> getlevel2CategoriesFromDbWithRedissionLock() {
        RLock rLock = redisson.getLock("catalogJson-lock");
        rLock.lock();
        Map<String, List<Catalog2Vo>> map;
        try {
            map = getFromDb();
        } finally {
            rLock.unlock();
        }
        return map;
    }

    public Map<String, List<Catalog2Vo>> getlevel2CategoriesFromDbWithRedisLock() {
        String uuid = UUID.randomUUID().toString();
        Boolean isSet = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (isSet) {
            Map<String, List<Catalog2Vo>> map = getFromDb();
//            删除锁
//            非原子性
//            String uuidLock = (String) redisTemplate.opsForValue().get("lock");
//            if (uuidLock.equals(uuid)) {
//                redisTemplate.delete("lock");
//            }
//            原子性lua脚本删除锁
            String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n" +
                    "    return redis.call(\"del\",KEYS[1])\n" +
                    "else\n" +
                    "    return 0\n" +
                    "end";
            Long isDel = (Long) redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            return map;
        } else {
//            自旋
            Map<String, List<Catalog2Vo>> map = this.getlevel2CategoriesFromDbWithRedisLock();
            return map;
        }
//
    }

    public Map<String, List<Catalog2Vo>> getlevel2CategoriesFromDb() {
        synchronized (this) {
            return getFromDb();
        }
    }

    private Map<String, List<Catalog2Vo>> getFromDb() {
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
        }
//        获取所有栏目
        List<CategoryEntity> categoryEntitiesAll = this.baseMapper.selectList(null);
        System.out.println("查询数据库了---------");
//        获取层级一List
        List<CategoryEntity> level1Entities = this.getLevel1Categories();
//        层级一转MAP，key为ID，value为层级二
        Map<String, List<Catalog2Vo>> map = level1Entities.stream().collect(Collectors.toMap((k) -> k.getCatId().toString(),
                (v) -> {
                    List<CategoryEntity> categoryEntitiesL2 = getChildren(v, categoryEntitiesAll);
                    List<Catalog2Vo> catalog2Vos = categoryEntitiesL2.stream().map((s) -> {
                        Catalog2Vo catalog2Vo = new Catalog2Vo();
                        catalog2Vo.setCatalog1Id(v.getCatId().toString());
                        catalog2Vo.setId(s.getCatId().toString());
                        catalog2Vo.setName(s.getName());
                        List<CategoryEntity> categoryEntitiesL3 = getChildren(s, categoryEntitiesAll);
                        List<Catalog2Vo.catalog3Vo> catalog3Vos = categoryEntitiesL3.stream().map((l3) -> new Catalog2Vo.catalog3Vo(s.getCatId().toString(), l3.getCatId().toString(), l3.getName())).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(catalog3Vos);
                        return catalog2Vo;
                    }).collect(Collectors.toList());
                    return catalog2Vos;
                }));
        stringRedisTemplate.opsForValue().set("catalogJson", JSON.toJSONString(map));
        return map;
    }

    private List<Long> catelogPath(Long catelogId, List<Long> paths) {
        CategoryEntity categoryEntity = this.getById(catelogId);
        paths.add(catelogId);
        if (categoryEntity.getParentCid() != 0) {
            catelogPath(categoryEntity.getParentCid(), paths);
        }
        return paths;
    }

    private List<CategoryEntity> getChildren(CategoryEntity menu, List<CategoryEntity> categoryEntities) {
        List<CategoryEntity> children = categoryEntities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == menu.getCatId()
        ).map((categoryEntity) -> {
            categoryEntity.setChildren(getChildren(categoryEntity, categoryEntities));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return children;
    }

}