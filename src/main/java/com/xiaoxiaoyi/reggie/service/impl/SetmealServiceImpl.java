package com.xiaoxiaoyi.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoxiaoyi.reggie.dto.SetmealDto;
import com.xiaoxiaoyi.reggie.entity.Setmeal;
import com.xiaoxiaoyi.reggie.entity.SetmealDish;
import com.xiaoxiaoyi.reggie.mapper.SetmealMapper;
import com.xiaoxiaoyi.reggie.service.SetmealDishService;
import com.xiaoxiaoyi.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 保存菜品和套餐的关联信息
     *
     * @param setmealDto
     */
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        // 保存套餐信息
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().peek((item) ->{
            item.setSetmealId(setmealDto.getId());
        }).collect(Collectors.toList());

        // 保存菜品和套餐的关联信息setmeal_dish
        setmealDishService.saveBatch(setmealDishes);
    }
}
