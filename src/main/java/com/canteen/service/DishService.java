package com.canteen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.canteen.dto.DishDto;
import com.canteen.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    //新增单品，同时插入单品对应的口味数据，需要操作两张表：dish、dish_flavor
    void saveWithFlavor(DishDto dishDto);

    //根据id查询单品信息和对应的口味信息
    DishDto getByIdWithFlavor(Long id);

    //更新单品信息，同时更新对应的口味信息
    void updateWithFlavor(DishDto dishDto);

    void deleteWithFlavor(List<Long> ids);
}
