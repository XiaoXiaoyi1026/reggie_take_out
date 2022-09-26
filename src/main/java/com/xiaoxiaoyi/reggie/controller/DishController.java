package com.xiaoxiaoyi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoxiaoyi.reggie.common.R;
import com.xiaoxiaoyi.reggie.dto.DishDto;
import com.xiaoxiaoyi.reggie.entity.Dish;
import com.xiaoxiaoyi.reggie.service.DishFlavorService;
import com.xiaoxiaoyi.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 菜品相关的接口
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    // 菜品相关的Service
    @Autowired
    private DishService dishService;

    // 菜品属性相关的Service
    @Autowired
    private DishFlavorService dishFlavorService;

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
    public R<Page<Dish>> page(int page, int pageSize, String name) {
        // 1. 创建一个Page对象
        Page<Dish> dishPage = new Page<>(page, pageSize);
        // 2. 创建查询QueryWrapper对象
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 3. 添加条件
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getCreateTime);
        // 4. 执行查询
        Page<Dish> res = dishService.page(dishPage, queryWrapper);
        // 5. 返回
        return R.success(res);
    }
}
