package com.xiaoxiaoyi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoxiaoyi.reggie.common.R;
import com.xiaoxiaoyi.reggie.entity.Category;
import com.xiaoxiaoyi.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        // 保存分类
        categoryService.save(category);
        return R.success("分类添加成功！");
    }

    /**
     * 分类页面的分页查询
     *
     * @return
     */
    @GetMapping("/page")
    public R<Page<Category>> page(@RequestParam("page") int page, @RequestParam("pageSize") int pageSize) {
        // 1. 创建分页构造器
        Page<Category> categoryPage = new Page<>(page, pageSize);
        log.info("page:{} pageSize:{}", page, pageSize);

        // 2. 创建条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 按照排序字段升序排序
        queryWrapper.orderByAsc(Category::getSort);

        // 3. 使用分页查询
        Page<Category> res = categoryService.page(categoryPage, queryWrapper);

        // 4. 返回查询结果
        return R.success(res);
    }

    /**
     * 删除分类功能
     * 需要判断分类有无关联菜品，如果有则无法删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping()
    public R<String> remove(@RequestParam(value = "ids", required = false) Long ids) {

        log.info("删除分类id：{}", ids);

        categoryService.remove(ids);

        return R.success("删除成功！");
    }

    /**
     * 修改分类信息
     *
     * @param category
     * @return
     */
    @PutMapping()
    public R<String> update(@RequestBody Category category) {
        log.info("更新：{}", category);
        categoryService.updateById(category);
        return R.success("更新成功！");
    }

}
