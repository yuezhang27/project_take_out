package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Transactional
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    public void submitWithDetails(Orders orders) {
        Long userId= BaseContext.getUserId();
        //userId查到ShoppingCart的List
        LambdaQueryWrapper<ShoppingCart> lqw=new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCartList=shoppingCartService.list(lqw);
        if(shoppingCartList==null||shoppingCartList.size()==0){
            throw new CustomException("please select a dish or setmeal");
        }
        //查用户信息，username，phone等克对应填入orders数据库
        User user=userService.getById(userId);
        BeanUtils.copyProperties(user,orders,"id");
        //先保留基本信息，生成order的id；
        Long addressBookId=orders.getAddressBookId();
        AddressBook addressBook=addressBookService.getById(addressBookId);
        if(addressBook==null){
            throw new CustomException("please set your address");
        }
        BeanUtils.copyProperties(addressBook,orders,"id");
        Long orderId=IdWorker.getId();
        orders.setId(orderId);
        //复制除了id的所有属性，对其中每一个添加orderId;
        AtomicInteger amount=new AtomicInteger(0);
        List<OrderDetail> orderDetailList=shoppingCartList.stream().map((i)->{
            OrderDetail orderDetail=new OrderDetail();
            BeanUtils.copyProperties(i,orderDetail,"id");
            orderDetail.setOrderId(orderId);
            amount.addAndGet(i.getAmount().multiply(new BigDecimal(i.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());
        //设置其他
        orders.setNumber(String.valueOf(orderId));
        orders.setUserId(userId);
        orders.setUserName(user.getName());
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAddress((addressBook.getProvinceName()==null?" ":addressBook.getProvinceName())
                +(addressBook.getCityName()==null?" ":addressBook.getCityName())
                +(addressBook.getDistrictName()==null?" ":addressBook.getDistrictName())
                +(addressBook.getDetail()==null?" ":addressBook.getDetail()));
        orders.setAmount(new BigDecimal(amount.get()));
        this.save(orders);
        orderDetailService.saveBatch(orderDetailList);
        shoppingCartService.remove(lqw);
    }
}
