package com.xiaoxiaoyi.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoxiaoyi.reggie.entity.AddressBook;
import com.xiaoxiaoyi.reggie.mapper.AddressBookMapper;
import com.xiaoxiaoyi.reggie.service.AddressBookService;
import org.springframework.stereotype.Service;

/**
 * @author xiaoxiaoyi
 * 地址簿ServiceImpl
 */
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}
