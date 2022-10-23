package com.xiaoxiaoyi.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工实体
 */

@Data
@ApiModel("员工")
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String name;

    private String password;

    private String phone;

    private String sex;

    // 身份证号码
    private String idNumber;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)    // 插入时更新
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时更新
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

}
