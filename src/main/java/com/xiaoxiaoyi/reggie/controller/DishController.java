package com.xiaoxiaoyi.reggie.controller;

import com.xiaoxiaoyi.reggie.service.DishFlavorService;
import com.xiaoxiaoyi.reggie.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 菜品相关的接口
 */
public class DishController {

    // 菜品相关的Service
    @Autowired
    private DishService dishService;

    // 菜品属性相关的Service
    @Autowired
    private DishFlavorService dishFlavorService;
}
