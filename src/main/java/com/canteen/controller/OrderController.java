package com.canteen.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.canteen.common.BaseContext;
import com.canteen.common.R;
import com.canteen.dto.OrdersDto;
import com.canteen.entity.OrderDetail;
import com.canteen.entity.Orders;
import com.canteen.entity.User;
import com.canteen.service.OrderDetailService;
import com.canteen.service.OrderService;
import com.canteen.service.UserService;
import com.sun.org.apache.xpath.internal.operations.Or;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;

    @PutMapping
    public R<String> deliver(@RequestBody Orders orders){
        Orders order = orderService.getById(orders.getId());
        order.setStatus(orders.getStatus());
        orderService.updateById(order);
        return R.success("已派送");
    }

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        orderService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 后台人员分页查看订单信息
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number, String beginTime, String endTime) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();

        lqw.eq(StringUtils.isNotEmpty(number), Orders::getNumber, number);
        lqw.between(beginTime != null && endTime != null, Orders::getOrderTime, beginTime, endTime);
        //查询订单基本信息
        Page<Orders> ordersPage = orderService.page(pageInfo, lqw);

        //把基本信息拷贝到OrdersDto对象中
        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");

        List<Orders> ordersRecords = ordersPage.getRecords();
        List<OrdersDto> ordersDtoList = ordersRecords.stream().map((orderRecord) -> {
            OrdersDto ordersDto = new OrdersDto();
            //拷贝对象
            BeanUtils.copyProperties(orderRecord, ordersDto);
            //获取订单id
            Long orderId = orderRecord.getId();
            //通过订单id查询该订单下对应的单品/套餐
            LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderDetail::getOrderId, orderId);
            List<OrderDetail> orderDetails = orderDetailService.list(queryWrapper);
            ordersDto.setOrderDetails(orderDetails);

            //根据用户id获取用户名
            Long userId = orderRecord.getUserId();
            User user = userService.getById(userId);
            if (StringUtils.isNotEmpty(user.getName())) {
                ordersDto.setUserName(user.getName());
            }
            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(ordersDtoList);
        return R.success(ordersPage);
    }

    /**
     * 查询订单明细表
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> orderDetail(int page, int pageSize) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();
        Long currentId = BaseContext.getCurrentId();
//      查询订单数据
        QueryWrapper<Orders> wrapper = new QueryWrapper<>();
        wrapper.eq(currentId!=null,"user_id", currentId);
        Page<Orders> ordersPage = orderService.page(pageInfo, wrapper);
        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");
        List<Orders> ordersList = ordersPage.getRecords();
        List<OrdersDto> ordersDtoList = ordersList.stream().map(item -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            Long orderId = item.getId();
            if (orderId != null) {
                //      查询订单明细表
                QueryWrapper<OrderDetail> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq(orderId!=null,"order_id", orderId);
                List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);
                ordersDto.setOrderDetails(orderDetailList);
            }
            return ordersDto;
        }).collect(Collectors.toList());
        ordersDtoPage.setRecords(ordersDtoList);
        return R.success(ordersDtoPage);
    }

}