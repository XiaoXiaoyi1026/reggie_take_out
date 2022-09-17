package com.xiaoxiaoyi.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品
 */
@Data
public class Dish implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;


    //菜品名称
    private String name;


    //菜品分类id
    private Long categoryId;


    //菜品价格
    private BigDecimal price;


    //商品码
    private String code;


    //图片
    private String image;


    //描述信息
    private String description;


    //0 停售 1 起售
    private Integer status;


    //顺序
    private Integer sort;


    // 插入时自动填充元数据
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;


    // 插入或者更新时自动填充元数据
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    // 插入时自动填充元数据
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;


    // 插入或者更新时自动填充元数据
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;


    //是否删除
    private Integer isDeleted;

}
