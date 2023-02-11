package com.canteen.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.canteen.common.BaseContext;
import com.canteen.common.R;
import com.canteen.entity.ShoppingCart;
import com.canteen.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart, HttpServletRequest request){
        //设置用户id，指定当前是哪个用户的购物车数据
        Long id = (Long) request.getSession().getAttribute("user");
        shoppingCart.setUserId(id);

        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,id);
        if(dishId != null){
            //添加到购物车的是单品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else{
            //添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //查询当前单品或者套餐是否在购物车中
        //SQL:select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        if(cartServiceOne != null){
            //如果已经存在，就在原来数量基础上加一
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        }else{
            //如果不存在，则添加到购物车，数量默认就是一
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }
        return R.success(cartServiceOne);
    }
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart,HttpServletRequest request) {
        Long dishId = shoppingCart.getDishId();
        Long id = (Long) request.getSession().getAttribute("user");
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        if (dishId != null) {
            queryWrapper.eq("dish_id", dishId).eq("user_id", id);
            ShoppingCart cart = shoppingCartService.getOne(queryWrapper);
            cart.setNumber(cart.getNumber() - 1);
            Integer latestNumber = cart.getNumber();
            if (latestNumber > 0) { // 对数据进行更新操作
                shoppingCartService.updateById(cart);
            } else if (latestNumber == 0) { // 如果购物车的单品数量减为0，那么就把单品从购物车删除
                shoppingCartService.removeById(cart);
            } else {
                return R.error("操作异常");
            }
            return R.success(cart);
        }
        Long setmealId = shoppingCart.getSetmealId();
        if (setmealId != null) {
            //代表是套餐数量减少
            queryWrapper.eq("setmeal_id", setmealId).eq("user_id",id);
            ShoppingCart cart = shoppingCartService.getOne(queryWrapper);
            cart.setNumber(cart.getNumber() - 1);
            Integer latestNumber = cart.getNumber();
            if (latestNumber > 0) {
                shoppingCartService.updateById(cart);
            } else if (latestNumber == 0) {
                shoppingCartService.removeById(cart);
            } else {
                return R.error("操作异常");
            }
            return R.success(cart);
        }
        return R.error("操作异常");
    }
    /**
     * 查看用户已选的菜品
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }
    /**
     * 清空菜品栏
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        //SQL:delete from shopping_cart where user_id = ?
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }
}