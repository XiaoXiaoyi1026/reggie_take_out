package com.xiaoxiaoyi.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoxiaoyi.reggie.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author xiaoxiaoyi
 * 地址簿
 */
@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {
}
