package com.xiaoxiaoyi.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoxiaoyi.reggie.common.R;
import com.xiaoxiaoyi.reggie.dto.DishDto;
import com.xiaoxiaoyi.reggie.entity.Dish;

public interface DishService extends IService<Dish> {

    /**
     * 保存菜品相关信息和口味相关信息，需要同时操作dish和dish_flavor表
     *
     * @param dishDto
     */
    void saveWithFlavor(DishDto dishDto);

    /**
     * 根据id查询菜品以及对应的口味信息
     *
     * @param id 菜品id
     * @return DishDto
     */
    DishDto getDishAndFlavorsById(Long id);
}
