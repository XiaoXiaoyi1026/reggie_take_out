package com.xiaoxiaoyi.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoxiaoyi.reggie.common.BaseContext;
import com.xiaoxiaoyi.reggie.common.CustomException;
import com.xiaoxiaoyi.reggie.entity.*;
import com.xiaoxiaoyi.reggie.mapper.OrdersMapper;
import com.xiaoxiaoyi.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author xiaoxiaiyi
 * Service Impl of Orders
 */
@Service
@Slf4j
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户提交订单
     *
     * @param orders 订单数据
     */
    @Override
    public void submitOrders(Orders orders) {
        // 1. 获取当前用户信息
        Long userId = BaseContext.getCurrentId();
        orders.setUserId(userId);
        // 2. 根据当前用户信息查询购物车数据
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(shoppingCartLambdaQueryWrapper);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new CustomException("购物车为空，无法提交订单！");
        }
        // 查询用户信息
        User user = userService.getById(userId);
        // 查询地址信息
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if (addressBook == null) {
            throw new CustomException("地址信息有误，无法下单！");
        }
        // 3. 将信息写回orders和order_detail表中
        // 生成订单号
        long orderId = IdWorker.getId();
        // 设置订单号
        orders.setId(orderId);
        // 设置订单号
        orders.setNumber(String.valueOf(orderId));
        // 设置订单创建时间
        orders.setOrderTime(LocalDateTime.now());
        // 设置checkout_time
        orders.setCheckoutTime(LocalDateTime.now());
        // 设置默认状态为2：待派送
        orders.setStatus(2);
        // 设置订单总金额amount
        AtomicInteger amount = new AtomicInteger(0);

        // 遍历购物车数据计算总金额
        List<OrderDetail> orderDetails = shoppingCartList.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            // 相当于 +=                         *
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        orders.setAmount(BigDecimal.valueOf(amount.get()));
        // 设置用户名
        orders.setUserName(user.getName());
        // 设置Consignee 收件人
        orders.setConsignee(addressBook.getConsignee());
        // 设置phone
        orders.setPhone(addressBook.getPhone());
        // 设置address
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName()) +
                (addressBook.getCityName() == null ? "" : addressBook.getCityName()) +
                (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName()) +
                (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        // 向orders插入1条数据
        this.save(orders);

        // 向order_detail插入多条数据
        orderDetailService.saveBatch(orderDetails);

        // 4. 清空购物车
        shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
    }
}
