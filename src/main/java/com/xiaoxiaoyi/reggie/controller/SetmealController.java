package com.xiaoxiaoyi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoxiaoyi.reggie.common.R;
import com.xiaoxiaoyi.reggie.dto.SetmealDto;
import com.xiaoxiaoyi.reggie.entity.Category;
import com.xiaoxiaoyi.reggie.entity.Setmeal;
import com.xiaoxiaoyi.reggie.entity.SetmealDish;
import com.xiaoxiaoyi.reggie.service.CategoryService;
import com.xiaoxiaoyi.reggie.service.SetmealDishService;
import com.xiaoxiaoyi.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiaoxiaoyi
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 保存套餐
     *
     * @return 套餐信息
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info(setmealDto.getName());

        setmealService.saveWithDish(setmealDto);

        return R.success("操作成功！");
    }

    /**
     * 套餐分页查询
     *
     * @param page     页码
     * @param pageSize 大小
     * @param name     套餐名称
     * @return 分页数据
     */
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page, int pageSize, String name) {
        // 1. 创建一个Page对象
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        // 2. 创建查询QueryWrapper对象
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // 3. 添加条件
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getCreateTime);

        // 4. 执行查询
        setmealPage = setmealService.page(setmealPage, queryWrapper);

        // 对象拷贝，但不拷贝setmeal的records字段(浅拷贝)
        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");

        // 获取setmeal分页查询的返回值
        List<Setmeal> records = setmealPage.getRecords();

        // 对返回列表进行操作
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();

            // 将setmeal基本信息复制到setmealDto中
            BeanUtils.copyProperties(item, setmealDto);

            // 根据categoryId查询分类信息
            Category category = categoryService.getById(setmealDto.getCategoryId());
            if (category != null) {
                setmealDto.setCategoryName(category.getName());
            }

            return setmealDto;
        }).collect(Collectors.toList());

        // 将records设置进setmealDto的分页数据中
        setmealDtoPage.setRecords(list);

        // 5. 返回
        return R.success(setmealDtoPage);
    }

    /**
     * 根据id查询套餐的信息
     *
     * @param id 套餐id
     * @return 套餐信息
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id) {
        log.info(Long.toString(id));

        // 1. 根据id查询套餐信息
        Setmeal setmeal = setmealService.getById(id);
        // 2. 将查询到的套餐信息封装进SetmealDto中
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);
        // 3. 根据id查询SetmealDish
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        // 4. 将查询到的Dishes设置回Dto
        setmealDto.setSetmealDishes(setmealDishes);

        return R.success(setmealDto);
    }

    /**
     * 根据id删除
     *
     * @param ids 删除ids
     * @return 信息
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    @DeleteMapping
    public R<String> deleteByIds(@RequestParam String ids) {
        // 切割出id
        String[] tmplIds = ids.split(",");
        for (String id : tmplIds) {
            long cur = Long.parseLong(id);
            // 根据id获取套餐
            Setmeal setmeal = setmealService.getById(cur);
            // 判断售卖状态
            if (setmeal.getStatus() == 0) {
                // 如果是停售状态则修改其is_deleted字段为1
                setmeal.setIsDeleted(1);
                setmealService.updateById(setmeal);
                // 根据套餐id查询setmeal_dish
                LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(SetmealDish::getSetmealId, Long.parseLong(id));
                List<SetmealDish> list = setmealDishService.list(queryWrapper);
                // 更新setmeal_dish的is_deleted字段
                for (SetmealDish setmealDish : list) {
                    setmealDish.setIsDeleted(1);
                }
                setmealDishService.updateBatchById(list);
            } else {
                // 如果是启售状态则直接返回
                return R.error("套餐并未停售！");
            }
        }
        return R.success("删除成功！");
    }

    /**
     * 修改套餐状态
     *
     * @param status 目标状态
     * @param ids    更改的id
     * @return 信息
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    @PostMapping("/status/{status}")
    public R<String> changeStatusById(@PathVariable Integer status, @RequestParam String ids) {

        String[] tmpIds = ids.split(",");
        for (String id : tmpIds) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(Long.parseLong(id));
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }

        return R.success("修改成功！");
    }

    /**
     * 更新套餐信息
     *
     * @return 信息
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        log.info("更新信息：{}", setmealDto);

        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDto, setmeal);

        // 更新套餐信息
        setmealService.updateById(setmeal);

        // 更新setmeal_dish信息
        // 先删除原来的
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes = setmealDishes.stream().peek((item)
                        -> item.setSetmealId(setmealDto.getId()))
                .collect(Collectors.toList());

        // 再添加新的
        setmealDishService.saveBatch(setmealDishes);

        return R.success("更新成功！");
    }

}
