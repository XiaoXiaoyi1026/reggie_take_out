package com.xiaoxiaoyi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.xiaoxiaoyi.reggie.common.BaseContext;
import com.xiaoxiaoyi.reggie.common.R;
import com.xiaoxiaoyi.reggie.entity.AddressBook;
import com.xiaoxiaoyi.reggie.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author xiaoxiaoyi
 * 地址簿控制器
 */
@RestController
@RequestMapping("/addressBook")
@Slf4j
@Api(tags = "地址簿相关接口")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址
     *
     * @param addressBook 地址
     * @return 地址
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook) {
        // 获取当前登录的用户id来进行设置
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook: {}", addressBook);
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    /**
     * 设置默认地址
     *
     * @param addressBook 地址
     * @return 地址
     */
    @PutMapping("/default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
        log.info("addressBook: {}", addressBook);
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        wrapper.set(AddressBook::getIsDefault, 0);
        // 将当前用户所有的address改为非默认
        // SQL: update address_book set is_default = 0 where user_id = ?
        addressBookService.update(wrapper);

        addressBook.setIsDefault(1);
        // SQL: update address_book set is_default = 1 where id = ?
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    /**
     * 根据id查询地址信息
     *
     * @param id addressId
     * @return 地址
     */
    @GetMapping("/{id}")
    public R getAddressById(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            return R.success(addressBook);
        } else {
            return R.error("未查询到相关地址");
        }
    }

    /**
     * 查询默认地址
     *
     * @return 地址
     */
    @GetMapping("/default")
    public R getDefaultAddressByUser() {
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault, 1);

        // SQL: select * from address_book where user_id = ? and is_default = 1
        AddressBook addressBook = addressBookService.getOne(queryWrapper);

        if (addressBook != null) {
            return R.success(addressBook);
        } else {
            return R.error("未找到默认地址");
        }
    }

    /**
     * 根据user_id查询所有地址
     *
     * @return 地址列表
     */
    @GetMapping("/list")
    public R<List<AddressBook>> getAddressListByUser(AddressBook addressBook) {
        // 将当前登录的用户id赋值进去
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBook);

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(null != addressBook.getUserId()
                , AddressBook::getUserId
                , addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);

        // SQL: select * from address_book where user_id = ? order by update_time desc
        return R.success(addressBookService.list(queryWrapper));
    }

}
