package com.xiaoxiaoyi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoxiaoyi.reggie.common.BaseContext;
import com.xiaoxiaoyi.reggie.common.R;
import com.xiaoxiaoyi.reggie.entity.ShoppingCart;
import com.xiaoxiaoyi.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author xiaoxiaoyi
 * Controller of ShoppingCart
 */
@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加进购物车
     *
     * @param shoppingCart 购物车信息
     * @return 购物车信息
     */
    @PostMapping("/add")
    public R<ShoppingCart> shoppingCartAdd(@RequestBody ShoppingCart shoppingCart) {
        log.info("shoppingCart: {}", shoppingCart);
        // 从ThreadLocal中拿到当前登录的用户id并设置
        shoppingCart.setUserId(BaseContext.getCurrentId());
        // 判断当前菜品/套餐是否已在购物车中
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        Long dishId = shoppingCart.getDishId();
        if (dishId != null) {
            // 说明传进来的是dish，则根据dishId进行查询
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            // 传进来的是setmeal，根据setmealId进行查询
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        // SQL: seelect * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(shoppingCartLambdaQueryWrapper);

        // 判断是否已存在于购物车中
        if (cartServiceOne != null) {
            // 说明已存在，只在原先的数量上+1
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        } else {
            // 如果不存在，则添加进购物车，默认数量为1
            shoppingCart.setNumber(1);
            shoppingCartService.save(shoppingCart);
            // 添加创建时间
            shoppingCart.setCreateTime(LocalDateTime.now());
            cartServiceOne = shoppingCart;
        }

        return R.success(cartServiceOne);
    }

    /**
     * 减少购物车中的dish/setmeal数量
     *
     * @return 减少后的购物车
     */
    @PostMapping("/sub")
    public R<ShoppingCart> shoppingCartSub(@RequestBody ShoppingCart shoppingCart) {
        log.info("Shopping cart: {}", shoppingCart);
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (dishId != null) {
            // 传进来的是dish
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            // 传进来的是setmeal
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart cartServiceOne = shoppingCartService.getOne(shoppingCartLambdaQueryWrapper);
        // 更新数量
        cartServiceOne.setNumber(cartServiceOne.getNumber() - 1);
        // 判断更新后数量是否为0
        if (cartServiceOne.getNumber() == 0) {
            // 为0则从购物车中移除该项
            shoppingCartService.removeById(cartServiceOne.getId());
        } else {
            // 不为0则更新
            shoppingCartService.updateById(cartServiceOne);
        }

        return R.success(cartServiceOne);
    }

    /**
     * 查看购物车
     *
     * @return 购物车列表
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> getShoppingCartList() {
        log.info("查看购物车...");
        // 根据当前登录的user_id查询
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        // 根据创建时间降序排，先创建的(大的)排前面
        shoppingCartLambdaQueryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        // 返回最新的购物车数据
        return R.success(shoppingCartService.list(shoppingCartLambdaQueryWrapper));
    }

    /**
     * 清空购物车
     *
     * @return 清空信息
     */
    @DeleteMapping("/clean")
    public R<String> cleanShoppingCart() {
        log.info("清空购物车...");
        // 根据当前登录的用户id删除shopping_cart的记录
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
        return R.success("购物车清空成功！");
    }

}
