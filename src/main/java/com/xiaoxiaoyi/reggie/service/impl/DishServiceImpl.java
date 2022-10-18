package com.xiaoxiaoyi.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoxiaoyi.reggie.common.CustomException;
import com.xiaoxiaoyi.reggie.common.R;
import com.xiaoxiaoyi.reggie.dto.DishDto;
import com.xiaoxiaoyi.reggie.entity.Dish;
import com.xiaoxiaoyi.reggie.entity.DishFlavor;
import com.xiaoxiaoyi.reggie.mapper.DishMapper;
import com.xiaoxiaoyi.reggie.service.DishFlavorService;
import com.xiaoxiaoyi.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    /**
     * 菜品flavors相关服务
     */
    @Autowired
    private DishFlavorService dishFlavorService;

    @Value("${reggie.img-path}")
    private String imgPath;


    @Override
    public void saveWithFlavor(DishDto dishDto) {
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
        }).collect(Collectors.toList());

        // 添加进dish_flavor表中
        dishFlavorService.saveBatch(flavors);
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
        if (dish == null) {
            throw new CustomException("查询菜品失败！");
        }
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

    /**
     * 根据dishId修改其信息
     *
     * @param dishDto dishDto信息
     */
    @Override
    public void updateDishAndFlavorsById(DishDto dishDto) {
        // 1. 根据dishDto更新dish
        this.updateById(dishDto);
        // 2. 使用saveOrUpdate修改dish_flavors的信息
        // 2.1 给所有flavors设置上dishId的信息
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().peek(
                        (item) -> item.setDishId(dishDto.getId()))
                .collect(Collectors.toList());
        // 2.2 构造Lambda查询条件
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        // 2.3 删除原来数据表中的flavors数据
        dishFlavorService.remove(queryWrapper);

        // 2.4 执行saveOrUpdateBatch方法更新dish_flavors表中的值
        dishFlavorService.saveOrUpdateBatch(flavors);
    }

    /**
     * 根据dishId删除dish和flavors
     *
     * @param ids dishIds
     */
    @Override
    public void deleteDishAndFlavorsByIds(List<Long> ids) {
        // 1. 判断是否存在启售的dish
        // select count(*) from dish where id in (1, 2) and status == 1;
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper =
                new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(Dish::getId, ids);
        // 启售 1 停售 0
        dishLambdaQueryWrapper.eq(Dish::getStatus, 1);

        int count = this.count(dishLambdaQueryWrapper);
        if (count > 0) {
            throw new CustomException("菜品尚在售卖！！");
        }

        // 删除图片
        for (Long id : ids) {
            try {
                Dish dish = this.getById(id);
                Files.delete(Paths.get(imgPath + dish.getImage()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 菜品都不在售卖，则删除dish
        this.removeByIds(ids);

        // 删除dish_flavors
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper =
                new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.in(DishFlavor::getDishId, ids);

        dishFlavorService.remove(dishFlavorLambdaQueryWrapper);
    }
}
