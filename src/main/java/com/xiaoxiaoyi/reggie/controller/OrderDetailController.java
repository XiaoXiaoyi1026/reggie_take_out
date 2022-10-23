package com.xiaoxiaoyi.reggie.controller;

import com.xiaoxiaoyi.reggie.service.OrderDetailService;
import io.swagger.annotations.ApiModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiaoxiaoyi
 * Controller of OrderDetail
 */
@Slf4j
@RestController
@RequestMapping("/orderDetail")
@ApiModel("订单详情")
public class OrderDetailController {

    @Autowired
    private OrderDetailService orderDetailService;
}
