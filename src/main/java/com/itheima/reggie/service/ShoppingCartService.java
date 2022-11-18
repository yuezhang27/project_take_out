package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService extends IService<ShoppingCart> {
    public ShoppingCart addToShoppingCart(ShoppingCart shoppingCart);
    public List<ShoppingCart> ShoppingCartList(ShoppingCart shoppingCart);
    public void subFromList(Long dishId,Long setmealId);
}
