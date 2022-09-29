package com.xiaoxiaoyi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoxiaoyi.reggie.common.R;
import com.xiaoxiaoyi.reggie.dto.DishDto;
import com.xiaoxiaoyi.reggie.entity.Category;
import com.xiaoxiaoyi.reggie.entity.Dish;
import com.xiaoxiaoyi.reggie.service.CategoryService;
import com.xiaoxiaoyi.reggie.service.DishFlavorService;
import com.xiaoxiaoyi.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品相关的接口
 */
@RestController
@RequestMapping("/dish")
@Slf4j
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
     * 新增菜品
     *
     * @param dishDto Data Transfer Object
     * @return
     */
    @PostMapping
    public R<String> add(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功！");
    }

    /**
     * 菜品分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
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
    public R<String> updateDishAndFlavorsById(@RequestBody DishDto dishDto) {
        dishService.updateDishAndFlavorsById(dishDto);
        return R.success("修改成功！");
    }

    /**
     * 更新菜品售卖状态
     *
     * @param status 更新后的状态 0 停售 1 起售
     * @return 更新信息
     */
    @PostMapping("/status/{status}")
    public R<String> updateDishStatusById(@PathVariable Integer status, @RequestParam("ids") String idsString) {
        String[] idStrings = idsString.split(",");
        log.info("status:{} ids:{}", status, Arrays.toString(idStrings));
        Dish dish = new Dish();
        dish.setStatus(status);
        for (String id : idStrings) {
            dish.setId(Long.parseLong(id));
            dishService.updateById(dish);
        }
        return R.success("修改成功！");
    }

    @DeleteMapping
    public R<String> deleteDishById(@RequestParam("ids") String idsString) {
        String[] idStrings = idsString.split(",");
        log.info("ids:{}", Arrays.toString(idStrings));
        dishService.deleteDishAndFlavorsById(idStrings);
        return R.success("删除菜品成功！");
    }

    /**
     * 根据category id 查询菜品数据
     *
     * @param dish
     * @return
     */
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
    }

}
