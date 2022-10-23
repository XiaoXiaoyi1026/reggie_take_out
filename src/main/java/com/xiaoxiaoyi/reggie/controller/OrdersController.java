package com.xiaoxiaoyi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoxiaoyi.reggie.common.BaseContext;
import com.xiaoxiaoyi.reggie.common.R;
import com.xiaoxiaoyi.reggie.entity.Orders;
import com.xiaoxiaoyi.reggie.service.OrdersService;
import io.swagger.annotations.ApiModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author xiaoxiaoyi
 * Controller of Orders
 */
@Slf4j
@RestController
@RequestMapping("/order")
@ApiModel("订单")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    /**
     * 用户提交订单
     *
     * @param orders 订单数据
     * @return 信息
     */
    @PostMapping("/submit")
    public R<String> submitOrders(@RequestBody Orders orders) {
        log.info("订单数据：{}", orders);

        ordersService.submitOrders(orders);

        return R.success("订单提交成功！");
    }

    /**
     * 用户订单分页查询
     *
     * @param page     页数
     * @param pageSize 页面大小
     * @return 分页查询
     */
    @GetMapping("/userPage")
    public R<Page<Orders>> getUserOrders(@RequestParam Integer page, @RequestParam Integer pageSize) {
        Page<Orders> ordersPage = new Page<>(page,  pageSize);
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.eq(Orders::getUserId, userId);
        ordersLambdaQueryWrapper.orderByDesc(Orders::getOrderTime).orderByDesc(Orders::getCheckoutTime);
        // 按条件进行分页查询
        // SQL: select * from orders where user_id = ? limit ?, ?
        ordersPage = ordersService.page(ordersPage, ordersLambdaQueryWrapper);

        return R.success(ordersPage);
    }
}
