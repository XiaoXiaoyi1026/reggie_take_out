package com.xiaoxiaoyi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoxiaoyi.reggie.common.CustomException;
import com.xiaoxiaoyi.reggie.common.R;
import com.xiaoxiaoyi.reggie.dto.DishDto;
import com.xiaoxiaoyi.reggie.entity.Category;
import com.xiaoxiaoyi.reggie.entity.Dish;
import com.xiaoxiaoyi.reggie.entity.DishFlavor;
import com.xiaoxiaoyi.reggie.service.CategoryService;
import com.xiaoxiaoyi.reggie.service.DishFlavorService;
import com.xiaoxiaoyi.reggie.service.DishService;
import io.swagger.annotations.ApiModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author xiaoxiaoyi
 * 菜品相关的接口
 */
@RestController
@RequestMapping("/dish")
@Slf4j
@ApiModel("菜品")
public class DishController {

    /**
     * 菜品Service
     */
    @Autowired
    private DishService dishService;

    /**
     * 菜品属性相关的Service
     */
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 分类的Service
     */
    @Autowired
    private CategoryService categoryService;

    /**
     * 操作redis
     */
    /*@Autowired
    private RedisTemplate<Object, Object> redisTemplate;*/

    /**
     * 新增菜品
     *
     * @param dishDto Data Transfer Object
     * @return 信息
     */
    @PostMapping
    @CacheEvict(value = "dish", key = "#dishDto.id")
    public R<String> add(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        // 清理所有的dish缓存
        /*Set<Object> keys = redisTemplate.keys("dish_*");
        assert keys != null;
        redisTemplate.delete(keys);*/

        // 定向清理
        /*String key = "dish_" + dishDto.getCategoryId() + "*";
        redisTemplate.delete(key);*/

        return R.success("新增菜品成功！");
    }

    /**
     * 菜品分页查询
     *
     * @param page     页码
     * @param pageSize 页面大小
     * @param name     菜品名字
     * @return 菜品页面
     */
    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name) {

        // 1. 创建一个Page对象
        Page<Dish> dishPage = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        // 2. 创建查询QueryWrapper对象
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 3. 添加条件
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getCreateTime);

        // 4. 执行查询
        dishPage = dishService.page(dishPage, queryWrapper);

        // 对象拷贝，但不拷贝page的records字段
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");

        // 获取page分页查询的返回值
        List<Dish> records = dishPage.getRecords();

        // 对返回列表进行操作
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            // 将dish基本信息复制到dishDto中
            BeanUtils.copyProperties(item, dishDto);
            // 根据categoryId查询分类名称
            Category category = categoryService.getById(item.getCategoryId());
            // 将查询到的categoryName赋值给dishDto
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            return dishDto;
        }).collect(Collectors.toList());

        // 将records设置进dishDto的分页数据中
        dishDtoPage.setRecords(list);

        // 5. 返回
        return R.success(dishDtoPage);
    }

    /**
     * 根据id获取菜品以及口味信息
     *
     * @param id 菜品id
     * @return DishDto
     */
    @GetMapping("/{id}")
    @Cacheable(value = "dish", key = "#id", unless = "#result.data == null")
    public R<DishDto> getDishAndFlavorById(@PathVariable Long id) {

        DishDto dishDto = dishService.getDishAndFlavorsById(id);

        return R.success(dishDto);
    }

    /**
     * 根据菜品id修改其信息
     *
     * @param dishDto dishDto信息
     * @return 成功信息
     */
    @PutMapping
    @CacheEvict(value = "dish", key = "#dishDto.id")
    public R<String> updateDishAndFlavorsById(@RequestBody DishDto dishDto) {

        dishService.updateDishAndFlavorsById(dishDto);

        /*// 定向清理
        String key = "dish_" + dishDto.getCategoryId() + "*";
        redisTemplate.delete(key);*/

        return R.success("修改成功！");
    }

    /**
     * 根据id删除菜品
     *
     * @param ids ids
     * @return 信息
     */
    @DeleteMapping
    @CacheEvict(value = "dish", allEntries = true)
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public R<String> deleteDishById(@RequestParam("ids") List<Long> ids) {
        log.info("ids: {}", ids);
        dishService.deleteDishAndFlavorsByIds(ids);
        return R.success("删除成功！");
    }

    /**
     * 更新菜品售卖状态
     *
     * @param status 更新后的状态 0 停售 1 起售
     * @return 更新信息
     */
    @PostMapping("/status/{status}")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    @CacheEvict(value = "dish", allEntries = true)
    public R<String> updateDishStatusByIds(@PathVariable Integer status, @RequestParam("ids") List<Long> ids) {
        log.info("status:{} ids:{}", status, ids);

        // 先判断是否有需要更新状态的dish
        // select count(*) form dish where id in (2, 3) and status == 1;
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper =
                new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(Dish::getId, ids);
        dishLambdaQueryWrapper.eq(Dish::getStatus, status == 0 ? 1 : 0);
        int count = dishService.count(dishLambdaQueryWrapper);

        if (count == 0) {
            throw new CustomException("不存在需要修改状态的菜品");
        }

        // 如果存在需要修改的菜品则进行修改
        Dish dish = new Dish();
        dish.setStatus(status);
        dishService.update(dish, dishLambdaQueryWrapper);

        return R.success("修改成功！");
    }

    /**
     * 根据category id 查询菜品数据
     *
     * @param dish 菜品
     * @return 列表
     */
    /*
    @GetMapping("/list")
    public R<List<Dish>> getDishListByCategoryId(Dish dish) {

        // 构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());

        // 查询启售状态的菜品
        queryWrapper.eq(Dish::getStatus, 1);

        // 添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        // 执行查询
        List<Dish> dishList = dishService.list(queryWrapper);

        return R.success(dishList);
    }*/

    /**
     * 根据category id 查询菜品数据
     *
     * @param dish 菜品
     * @return 列表
     */
    @GetMapping("/list")
    @Cacheable(value = "dish", key = "#dish.categoryId + '_' + #dish.status")
    public R<List<DishDto>> getDishListByCategoryId(Dish dish) {

        List<DishDto> dishDtoList;

        /*// 动态构造key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();

        // 从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if (dishDtoList != null) {
            // 如果存在直接返回
            return R.success(dishDtoList);
        }
*/
        // 如果不存在则查询数据库，然后放入redis

        // 构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());

        // 查询启售状态的菜品
        queryWrapper.eq(Dish::getStatus, dish.getStatus() == null ? 1 : dish.getStatus());

        // 添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        // 执行查询
        List<Dish> dishList = dishService.list(queryWrapper);

        // 将Dish结果转换成带Flavors的DishDto
        dishDtoList = dishList.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            // 查询口味数据
            LambdaQueryWrapper<DishFlavor> dishFlavorQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorQueryWrapper.eq(DishFlavor::getDishId, item.getId());
            // SQL: select * from dish_flavor where dish_id = ?;
            dishDto.setFlavors(dishFlavorService.list(dishFlavorQueryWrapper));
            return dishDto;
        }).collect(Collectors.toList());

        /*// 将查询到的数据缓存到redis
        redisTemplate.opsForValue().set(key, dishDtoList, 1, TimeUnit.HOURS);*/

        return R.success(dishDtoList);
    }

}
