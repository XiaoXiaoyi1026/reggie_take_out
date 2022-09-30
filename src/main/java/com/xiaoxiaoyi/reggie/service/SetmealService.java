package com.xiaoxiaoyi.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoxiaoyi.reggie.dto.SetmealDto;
import com.xiaoxiaoyi.reggie.entity.Setmeal;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface SetmealService extends IService<Setmeal> {

    /**
     * 保存菜品和套餐的关联信息
     *
     * @param setmealDto
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    void saveWithDish(SetmealDto setmealDto);

}
