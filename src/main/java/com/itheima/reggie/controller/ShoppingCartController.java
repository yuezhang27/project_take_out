package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @PostMapping("/add")
    public Result<ShoppingCart> addToShoppingCart(@RequestBody ShoppingCart shoppingCart){
        ShoppingCart result=shoppingCartService.addToShoppingCart(shoppingCart);
        return Result.success(result);
    }
    @GetMapping("/list")
    public Result<List<ShoppingCart>> ShoppingCartList(ShoppingCart shoppingCart){
        List<ShoppingCart> result=shoppingCartService.ShoppingCartList(shoppingCart);
        return Result.success(result);
    }
    @DeleteMapping("/clean")
    public Result<String> cleanAll(ShoppingCart shoppingCart){
        Long userId= shoppingCart.getUserId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(userId!=null,ShoppingCart::getUserId,userId);
        shoppingCartService.remove(lambdaQueryWrapper);
        return Result.success("successfully removed");
    }
    @PostMapping("/sub")
    public Result<String> subFromList(Long dishId,Long setmealId){
        shoppingCartService.subFromList(dishId,setmealId);
        return Result.success("successfully deleted");
    }
}
