package com.canteen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canteen.common.CustomException;
import com.canteen.dto.SetmealDto;
import com.canteen.entity.Setmeal;
import com.canteen.entity.SetmealDish;
import com.canteen.mapper.SetmealMapper;
import com.canteen.service.SetmealDishService;
import com.canteen.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper,Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐，同时需要保存套餐和单品的关联关系
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和单品的关联信息，操作setmeal_dish,执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐，同时需要删除套餐和单品的关联数据
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //select count(*) from setmeal where id in (1,2,3) and status = 1
        //查询套餐状态，确定是否可用删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);

        int count = this.count(queryWrapper);
        if(count > 0){
            //如果不能删除，抛出一个业务异常
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        //如果可以删除，先删除套餐表中的数据---setmeal
        this.removeByIds(ids);

        //delete from setmeal_dish where setmeal_id in (1,2,3)
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        //删除关系表中的数据----setmeal_dish
        setmealDishService.remove(lambdaQueryWrapper);
    }
    @Override
    public void updateStatusbyIds(Integer status, List<Long> ids) {
        this.listByIds(ids).stream().forEach(item -> {
            item.setStatus(status);
            this.updateById(item);
        });
    }

    @Override
    public SetmealDto getDtoById(Long id) {
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        QueryWrapper<SetmealDish> queryWrapper = new QueryWrapper();
        queryWrapper.eq("setmeal_id", id);
        BeanUtils.copyProperties(setmeal, setmealDto);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }

    @Override
    @Transactional
    public void updateSetmealDto(SetmealDto setmealDto) {
        // 多态性保存setmeal表的基本信息
        this.updateById(setmealDto);
        if (setmealDto.getSetmealDishes() == null) {
            throw new CustomException("套餐没有单品,请添加单品");
        }
        Long id = setmealDto.getId();  // 套餐的id
        QueryWrapper<SetmealDish> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("setmeal_id", id);
        // 删除此套餐id之前在setmeal_dish表中的单品组合数据
        setmealDishService.remove(queryWrapper);
        // 给新修改套餐中的各单品前加上该套餐id后保存到setmeal_dish表中
        List<SetmealDish> list = setmealDto.getSetmealDishes().stream().map(dish -> {
            dish.setSetmealId(id);
            return dish;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(list);
    }
}
