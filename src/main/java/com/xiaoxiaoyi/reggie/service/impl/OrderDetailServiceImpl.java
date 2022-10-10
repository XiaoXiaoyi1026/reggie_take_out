package com.xiaoxiaoyi.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoxiaoyi.reggie.entity.OrderDetail;
import com.xiaoxiaoyi.reggie.mapper.OrderDetailMapper;
import com.xiaoxiaoyi.reggie.service.OrderDetailService;
import org.springframework.stereotype.Service;

/**
 * @author xiaoxiaoyi
 * Service Impl of OrderDetail
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
