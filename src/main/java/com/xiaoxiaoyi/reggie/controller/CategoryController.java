package com.xiaoxiaoyi.reggie.controller;

import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.xiaoxiaoyi.reggie.common.R;
import com.xiaoxiaoyi.reggie.entity.Category;
import com.xiaoxiaoyi.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新建分类
     *
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        log.info(category.toString());
        categoryService.save(category);
        return R.success("分类添加成功！");
    }

}
