package com.xiaoxiaoyi.reggie.controller;

import com.xiaoxiaoyi.reggie.common.R;
import com.xiaoxiaoyi.reggie.dto.SetmealDto;
import com.xiaoxiaoyi.reggie.entity.Setmeal;
import com.xiaoxiaoyi.reggie.entity.SetmealDish;
import com.xiaoxiaoyi.reggie.service.SetmealDishService;
import com.xiaoxiaoyi.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 保存套餐
     *
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info(setmealDto.getName());

        setmealService.saveWithDish(setmealDto);

        return R.success("操作成功！");
    }

}
