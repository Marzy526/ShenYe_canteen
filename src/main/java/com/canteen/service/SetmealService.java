package com.canteen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.canteen.dto.SetmealDto;
import com.canteen.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐，同时需要保存套餐和单品的关联关系
     * @param setmealDto
     */
    void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时需要删除套餐和单品的关联数据
     * @param ids
     */
    void removeWithDish(List<Long> ids);

    void updateStatusbyIds(Integer status, List<Long> ids);

    SetmealDto getDtoById(Long id);

    void updateSetmealDto(SetmealDto setmealDto);
}
