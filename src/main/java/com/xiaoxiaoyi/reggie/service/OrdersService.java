package com.xiaoxiaoyi.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoxiaoyi.reggie.entity.Orders;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author xiaoxiaoyi
 * Service of Orders
 */
public interface OrdersService extends IService<Orders> {

    /**
     * 用户提交订单
     *
     * @param orders 订单数据
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    void submitOrders(Orders orders);
}
