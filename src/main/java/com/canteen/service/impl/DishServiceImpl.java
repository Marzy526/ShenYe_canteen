package com.canteen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canteen.common.CustomException;
import com.canteen.dto.DishDto;
import com.canteen.entity.Dish;
import com.canteen.entity.DishFlavor;
import com.canteen.mapper.DishMapper;
import com.canteen.service.DishFlavorService;
import com.canteen.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper,Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增单品，同时保存对应的口味数据
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存单品的基本信息到单品表dish
        this.save(dishDto);

        Long dishId = dishDto.getId();//单品id

        //单品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().peek(item -> item.setDishId(dishId)).collect(Collectors.toList());

        //保存单品口味数据到单品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据id查询单品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询单品基本信息，从dish表查询
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查询当前单品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息
        this.updateById(dishDto);

        //清理当前单品对应口味数据---dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());

        dishFlavorService.remove(queryWrapper);

        //添加当前提交过来的口味数据---dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    @Override
    @Transactional
    public void deleteWithFlavor(List<Long> ids) {
        //构造条件查询器
        QueryWrapper<Dish> dishQueryWrapper = new QueryWrapper<>();
        //先查询该单品是否在售卖，如果是则抛出业务异常
        dishQueryWrapper.in(ids != null, "id", ids);
        List<Dish> list = this.list(dishQueryWrapper);
        for (Dish dish : list) {
            //如果不是在售卖,则可以删除
            if (dish.getStatus() == 0) {
                this.removeById(dish.getId());
            } else {
                //此时应回滚,因为可能已批量删除了前一部分，但是后面的正在售卖
                throw new CustomException("删除失败！单品中有正在售卖单品...");
            }
        }
        QueryWrapper<DishFlavor> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("dish_id", ids);
        dishFlavorService.remove(queryWrapper);
    }
}
