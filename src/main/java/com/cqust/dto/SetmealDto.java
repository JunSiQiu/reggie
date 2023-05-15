package com.cqust.dto;

import com.cqust.entity.Setmeal;
import com.cqust.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
