package com.xiaoxiaoyi.reggie.dto;

import com.xiaoxiaoyi.reggie.entity.Dish;
import com.xiaoxiaoyi.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object 输出传输对象，用于表示层和服务层之间的数据传输
 *
 */
@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
