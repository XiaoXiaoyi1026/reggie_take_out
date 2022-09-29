package com.xiaoxiaoyi.reggie.dto;
import com.xiaoxiaoyi.reggie.entity.Setmeal;
import com.xiaoxiaoyi.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
