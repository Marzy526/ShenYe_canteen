package com.canteen.dto;

import com.canteen.entity.Setmeal;
import com.canteen.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
