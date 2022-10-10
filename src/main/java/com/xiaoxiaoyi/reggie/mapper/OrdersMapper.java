package com.xiaoxiaoyi.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoxiaoyi.reggie.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author xiaoxiaoyi
 * Mapper of Order
 */
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}
