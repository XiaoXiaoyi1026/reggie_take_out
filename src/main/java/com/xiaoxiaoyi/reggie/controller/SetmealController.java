package com.xiaoxiaoyi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.xiaoxiaoyi.reggie.common.CustomException;
import com.xiaoxiaoyi.reggie.common.R;
import com.xiaoxiaoyi.reggie.dto.DishDto;
import com.xiaoxiaoyi.reggie.dto.SetmealDto;
import com.xiaoxiaoyi.reggie.entity.Category;
import com.xiaoxiaoyi.reggie.entity.Dish;
import com.xiaoxiaoyi.reggie.entity.Setmeal;
import com.xiaoxiaoyi.reggie.entity.SetmealDish;
import com.xiaoxiaoyi.reggie.service.CategoryService;
import com.xiaoxiaoyi.reggie.service.DishService;
import com.xiaoxiaoyi.reggie.service.SetmealDishService;
import com.xiaoxiaoyi.reggie.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author xiaoxiaoyi
 * 套餐相关的查询
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    /**
     * 图片根路径，用于删除图片
     */
    @Value("${reggie.img-path}")
    private String imgPath;

    /**
     * 保存套餐
     *
     * @return 套餐信息
     */
    @PostMapping
    @CacheEvict(value = "setmealCache", allEntries = true)
    @ApiOperation(value = "新增套餐接口")
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info(setmealDto.getName());

        setmealService.saveWithDish(setmealDto);

        return R.success("操作成功！");
    }

    /**
     * 套餐分页查询
     *
     * @param page     页码
     * @param pageSize 大小
     * @param name     套餐名称
     * @return 分页数据
     */
    @GetMapping("/page")
    @ApiOperation("套餐分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSize", value = "页面记录数", required = true),
            @ApiImplicitParam(name = "name", value = "套餐名称", required = false),
    })
    public R<Page<SetmealDto>> page(int page, int pageSize, String name) {
        // 1. 创建一个Page对象
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        // 2. 创建查询QueryWrapper对象
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // 3. 添加条件
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getCreateTime);

        // 4. 执行查询
        setmealPage = setmealService.page(setmealPage, queryWrapper);

        // 对象拷贝，但不拷贝setmeal的records字段(浅拷贝)
        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");

        // 获取setmeal分页查询的返回值
        List<Setmeal> records = setmealPage.getRecords();

        // 对返回列表进行操作
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();

            // 将setmeal基本信息复制到setmealDto中
            BeanUtils.copyProperties(item, setmealDto);

            // 根据categoryId查询分类信息
            Category category = categoryService.getById(setmealDto.getCategoryId());
            if (category != null) {
                setmealDto.setCategoryName(category.getName());
            }

            return setmealDto;
        }).collect(Collectors.toList());

        // 将records设置进setmealDto的分页数据中
        setmealDtoPage.setRecords(list);

        // 5. 返回
        return R.success(setmealDtoPage);
    }

    /**
     * 根据id查询套餐的信息
     *
     * @param id 套餐id
     * @return 套餐信息
     */
    @GetMapping("/{id}")
    @Cacheable(value = "setmealCache", key = "#id", unless = "#result.data == null")
    @ApiOperation("套餐查询接口")
    public R<SetmealDto> getById(@PathVariable Long id) {
        log.info(Long.toString(id));

        // 1. 根据id查询套餐信息
        Setmeal setmeal = setmealService.getById(id);
        // 2. 将查询到的套餐信息封装进SetmealDto中
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);
        // 3. 根据id查询SetmealDish
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        // 4. 将查询到的Dishes设置回Dto
        setmealDto.setSetmealDishes(setmealDishes);

        return R.success(setmealDto);
    }

    /**
     * 修改套餐状态
     *
     * @param status 目标状态
     * @param ids    更改的id
     * @return 信息
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    @PostMapping("/status/{status}")
    @CacheEvict(value = "setmealCache", allEntries = true)
    @ApiOperation("更改套餐状态接口")
    public R<String> changeStatusById(@PathVariable Integer status, @RequestParam List<Long> ids) {
        log.info("status: {} ids: {}", status, ids);

        // 1. 先查询需要更新的套餐
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper =
                new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId, ids);
        setmealLambdaQueryWrapper.eq(Setmeal::getStatus, status == 0 ? 1 : 0);

        int count = setmealService.count(setmealLambdaQueryWrapper);
        if (count == 0) {
            throw new CustomException("不存在需要更新的套餐");
        }

        // update setmeal set status = 1 where id in (1, 2);
        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(status);
        setmealService.update(setmeal, setmealLambdaQueryWrapper);

        return R.success("状态更新成功！");
    }

    /**
     * 根据id删除
     *
     * @param ids 删除ids
     * @return 信息
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    @DeleteMapping
    @CacheEvict(value = "setmealCache", allEntries = true)
    @ApiOperation("删除套餐接口")
    public R<String> deleteByIds(@RequestParam List<Long> ids) {
        log.info("参数：{}", ids);

        // 1. select count(*) form setmeal where id in (1, 2, 3) where status = 1;
        // 查询ids中启售状态的套餐数
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper =
                new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId, ids);
        setmealLambdaQueryWrapper.eq(Setmeal::getStatus, 1);

        int count = setmealService.count(setmealLambdaQueryWrapper);
        if (count > 0) {
            // 如果存在启售状态的套餐
            throw new CustomException("套餐尚在售卖！！");
        }

        // 删除图片
        for (Long id : ids) {
            try {
                Setmeal setmeal = setmealService.getById(id);
                Files.delete(Paths.get(imgPath + setmeal.getImage()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 如果都停售了，则删除setmeal
        setmealService.removeByIds(ids);

        // delete from setmeal_dish where setmeal_id in (1, 2, 3);
        // 删除setmeal_dish
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper =
                new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);

        return R.success("删除套餐成功！");
    }

    /**
     * 更新套餐信息
     *
     * @return 信息
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    @PutMapping
    @CacheEvict(value = "setmealCache", allEntries = true)
    @ApiOperation("更新套餐接口")
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        log.info("更新信息：{}", setmealDto);

        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDto, setmeal);

        // 更新套餐信息
        setmealService.updateById(setmeal);

        // 更新setmeal_dish信息
        // 先删除原来的
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes = setmealDishes.stream().peek((item)
                        -> item.setSetmealId(setmealDto.getId()))
                .collect(Collectors.toList());

        // 再添加新的
        setmealDishService.saveBatch(setmealDishes);

        return R.success("更新成功！");
    }

    /**
     * 根据分类id获取套餐菜品
     *
     * @param setmeal 套餐信息
     * @return 菜品信息
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId + '_' + #setmeal.status", unless = "#result.data.size() == 0")
    @ApiOperation("获取套餐菜品信息")
    public R<List<Setmeal>> getSetmealDishesByCategoryId(Setmeal setmeal) {

        log.info("categoryId: {} status: {}", setmeal.getCategoryId(), setmeal.getStatus());

        // 1. 根据categoryId查询setmeal
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(
                setmeal.getCategoryId() != null,
                Setmeal::getCategoryId,
                setmeal.getCategoryId());
        setmealLambdaQueryWrapper.eq(
                setmeal.getStatus() != null,
                Setmeal::getStatus,
                setmeal.getStatus());

        List<Setmeal> setmealList = setmealService.list(setmealLambdaQueryWrapper);

        return R.success(setmealList);
    }

    /**
     * 根据套餐id获取其对应的菜品信息
     *
     * @return 套餐的菜品信息
     */
    @GetMapping("/dish/{setMealId}")
    @ApiOperation("根据套餐id获取其对应的菜品信息")
    public R<List<DishDto>> getDishesBySetMealId(@PathVariable Long setMealId) {
        log.info("setMealId: {}", setMealId);
        // 根据setMealId查询对应套餐的菜品信息
        // SQL: select * from setmeal_dish where setmeal_id = ?
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, setMealId);
        List<SetmealDish> setmealDishList = setmealDishService.list(setmealDishLambdaQueryWrapper);
        List<Long> dishIds = setmealDishList.stream().map(
                SetmealDish::getDishId)
                .collect(Collectors.toList());
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // SQL: select * from dish where id in (1, 2, ...)
        dishLambdaQueryWrapper.in(Dish::getId, dishIds);
        List<Dish> dishes = dishService.list(dishLambdaQueryWrapper);
        List<DishDto> dishDtoList = dishes.stream().map((dish) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto);
            return dishDto;
        }).collect(Collectors.toList());
        for (int i = 0; i < dishDtoList.size(); i++) {
            dishDtoList.get(i).setCopies(setmealDishList.get(i).getCopies());
        }
        return R.success(dishDtoList);
    }

}
