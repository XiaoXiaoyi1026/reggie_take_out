package com.xiaoxiaoyi.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoxiaoyi.reggie.entity.ShoppingCart;
import com.xiaoxiaoyi.reggie.mapper.ShoppingCartMapper;
import com.xiaoxiaoyi.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author xiaoxiaoyi
 * Impl of ShoppingCartService
 */
@Service
@Slf4j
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
