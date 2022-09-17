package com.xiaoxiaoyi.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoxiaoyi.reggie.common.CustomException;
import com.xiaoxiaoyi.reggie.entity.Category;
import com.xiaoxiaoyi.reggie.entity.Dish;
import com.xiaoxiaoyi.reggie.entity.Setmeal;
import com.xiaoxiaoyi.reggie.mapper.CategoryMapper;
import com.xiaoxiaoyi.reggie.service.CategoryService;
import com.xiaoxiaoyi.reggie.service.DishService;
import com.xiaoxiaoyi.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;
    
    @Autowired
    private SetmealService setmealService;

    @Override
    public void remove(Long ids) {
        // 1. 查询分类是否包含菜品
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, ids);
        int dishCount = dishService.count(dishLambdaQueryWrapper);
        if (dishCount > 0) {
            // 说明该分类有关联菜品，抛出异常
            throw new CustomException("当前分类有关联菜品，无法删除");
        }

        // 2. 查询分类是否包含套餐
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, ids);
        int setmalCount = setmealService.count(setmealLambdaQueryWrapper);
        if (setmalCount > 0) {
            // 说明该分类有关联套餐，抛出异常
            throw new CustomException("当前分类有关联套餐，无法删除");
        }

        // 3. 如果都不包含则可以删除分类
        super.removeById(ids);
    }
}
