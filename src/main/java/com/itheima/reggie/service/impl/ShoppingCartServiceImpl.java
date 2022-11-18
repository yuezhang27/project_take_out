package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.mapper.ShoppingCartMapper;
import com.itheima.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
    public ShoppingCart addToShoppingCart(ShoppingCart shoppingCart){
        //检查购物车之前有没有传入的dishId对应的菜，没有就正常save完事，有就
        Long userId=BaseContext.getUserId();
        shoppingCart.setUserId(userId);
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(shoppingCart.getDishId()!=null,ShoppingCart::getDishId,shoppingCart.getDishId());
        lambdaQueryWrapper.eq(shoppingCart.getSetmealId()!=null,ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
        ShoppingCart existingDish=this.getOne(lambdaQueryWrapper);
        if(existingDish!=null){
            Integer number=existingDish.getNumber();
            number+=1;
            existingDish.setNumber(number);
            this.updateById(existingDish);
            return existingDish;
        }
        shoppingCart.setNumber(1);
        this.save(shoppingCart);
        return shoppingCart;
    }

    public List<ShoppingCart> ShoppingCartList(ShoppingCart shoppingCart) {
        Long userId=BaseContext.getUserId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> result=this.list(lambdaQueryWrapper);
        return result;
    }

    public void subFromList(Long dishId, Long setmealId) {
        Long userId=BaseContext.getUserId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(dishId!=null,ShoppingCart::getDishId,dishId);
        lambdaQueryWrapper.eq(setmealId!=null,ShoppingCart::getSetmealId,setmealId);
        lambdaQueryWrapper.eq(userId!=null,ShoppingCart::getUserId,userId);
        ShoppingCart existingDish=this.getOne(lambdaQueryWrapper);
        if(existingDish!=null){
            Integer number=existingDish.getNumber();
            if(number==1) {
                this.remove(lambdaQueryWrapper);
            }
            number-=1;
            existingDish.setNumber(number);
            this.updateById(existingDish);
        }
    }
}
