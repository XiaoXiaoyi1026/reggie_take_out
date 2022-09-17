package com.xiaoxiaoyi.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoxiaoyi.reggie.entity.Category;

public interface CategoryService extends IService<Category> {

    public void remove(Long ids);

}
