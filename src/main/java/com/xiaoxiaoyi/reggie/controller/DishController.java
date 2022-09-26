package com.xiaoxiaoyi.reggie.controller;

import com.xiaoxiaoyi.reggie.common.R;
import com.xiaoxiaoyi.reggie.dto.DishDto;
import com.xiaoxiaoyi.reggie.service.DishFlavorService;
import com.xiaoxiaoyi.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
