package com.xiaoxiaoyi.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoxiaoyi.reggie.dto.DishDto;
import com.xiaoxiaoyi.reggie.entity.Dish;
import com.xiaoxiaoyi.reggie.entity.DishFlavor;
import com.xiaoxiaoyi.reggie.mapper.DishMapper;
import com.xiaoxiaoyi.reggie.service.DishFlavorService;
import com.xiaoxiaoyi.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    // 操作dish_flavor需要
    @Autowired
    private DishFlavorService dishFlavorService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveWithFlavor(DishDto dishDto) {
        try {
            // 1. 将菜品基本信息添加到dish表，这里this指的就是dishService
            this.save(dishDto);

            // 2. 存储菜品口味信息
            // 由于传过来的flavors中没有dishId的信息,所以需要手动进行封装
            // 获取flavor数组
            List<DishFlavor> flavors = dishDto.getFlavors();
            // 将数组转成流对象进行操作，peek用于不改变流中元素本身的类型而只操作其内容，map可以改变流中的元素类型，派生出另外一种元素类型
            flavors = flavors.stream().peek((item) -> {
                // 注入菜品dishId
                item.setDishId(dishDto.getId());
            }).collect(Collectors.toList());    // 流转换回列表对象

            // 添加进dish_flavor表中
            dishFlavorService.saveBatch(flavors);
        } catch (Exception e) {
        }
    }

    /**
     * 根据id查询菜品以及对应的口味信息
     *
     * @param id 菜品id
     * @return DishDto
     */
    @Override
    public DishDto getDishAndFlavorsById(Long id) {
        // 1. 根绝id查询菜品的基本信息
        Dish dish = this.getById(id);
        // 2. 创建一个DishDto对象，并将菜品的基本信息拷贝至其中
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
        // 3. 再根据id查询flavors信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper);
        // 4. 将flavor信息封装进dishDto对象中
        dishDto.setFlavors(dishFlavors);
        return dishDto;
    }
}
