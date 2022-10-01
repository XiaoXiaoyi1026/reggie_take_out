package com.xiaoxiaoyi.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoxiaoyi.reggie.common.R;
import com.xiaoxiaoyi.reggie.dto.DishDto;
import com.xiaoxiaoyi.reggie.entity.Dish;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DishService extends IService<Dish> {

    /**
     * 保存菜品相关信息和口味相关信息，需要同时操作dish和dish_flavor表
     *
     * @param dishDto
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    void saveWithFlavor(DishDto dishDto);

    /**
     * 根据id查询菜品以及对应的口味信息
     *
     * @param id 菜品id
     * @return DishDto
     */
    DishDto getDishAndFlavorsById(Long id);

    /**
     * 根据dishId修改信息
     *
     * @param dishDto dto信息
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    void updateDishAndFlavorsById(DishDto dishDto);

    /**
     * 根据dishId删除dish和flavors
     *
     * @param ids dishIds
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    void deleteDishAndFlavorsByIds(List<Long> ids);
}
