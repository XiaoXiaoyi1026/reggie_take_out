package com.xiaoxiaoyi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoxiaoyi.reggie.common.BaseContext;
import com.xiaoxiaoyi.reggie.common.R;
import com.xiaoxiaoyi.reggie.entity.ShoppingCart;
import com.xiaoxiaoyi.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            cartServiceOne = shoppingCart;
        }

        return R.success(cartServiceOne);
    }

}
