package com.xiaoxiaoyi.reggie.controller;

import com.xiaoxiaoyi.reggie.common.R;
import com.xiaoxiaoyi.reggie.entity.Orders;
import com.xiaoxiaoyi.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiaoxiaoyi
 * Controller of Orders
 */
@Slf4j
@RestController
@RequestMapping("/order")
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
}
