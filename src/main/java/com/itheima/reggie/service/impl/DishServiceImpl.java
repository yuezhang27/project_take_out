package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CacheManager cacheManager;
    @CacheEvict(value="dishCache")
    public void addNewDish(DishDto dishDto){
        this.save(dishDto);
        Long dishId= dishDto.getId();
        List<DishFlavor> flavors=dishDto.getFlavors();
        flavors=flavors.stream().map((i)->{i.setDishId(dishId);return i;}).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
        //clear the redis data when save new dish
        //String key="dish_"+dishDto.getCategoryId()+"_"+dishDto.getStatus();
        //redisTemplate.delete(key);
    }

    @CacheEvict(value="dishCache")
    public void updateDish(DishDto dishDto) {
        //保存基本信息
        this.updateById(dishDto);
        Long dishId= dishDto.getId();
        //清除原本的所有flavor
        LambdaQueryWrapper<DishFlavor> lqw=new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(lqw);
        //保存flavour信息
        List<DishFlavor> flavors=dishDto.getFlavors().stream().map((i)->{
            i.setDishId(dishId);
            return i;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
        //clear the redis data when save new dish
        //String key="dish_"+dishDto.getCategoryId()+"_"+dishDto.getStatus();
        //redisTemplate.delete(key);
    }

    public DishDto getByIdWithFlavor(Long id){
        //获得菜品基本信息
        Dish dish=this.getById(id);
        //获得口味信息
        LambdaQueryWrapper<DishFlavor> lqw=new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors=dishFlavorService.list(lqw);
        //将口味信息和基本信息装入dto并返回
        DishDto dishDto=new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    @Cacheable(value="dishCache",key="'dish_'+#dish.categoryId+#dish.status")
    public List<DishDto> getDishList(Dish dish) {
        List<DishDto> dishList=null;
        //get cache data from redis
        //build key of each category: 1 category has 1 key: use its categoryId and status to build the key
        //String key="dish_"+dish.getCategoryId()+"_"+dish.getStatus(); //key: dish_1234567_1
        //dishList= (List<DishDto>) redisTemplate.opsForValue().get(key);
        //if there is related data, return, no need to get data from mysql database
        //if(dishList!=null){
        //    return dishList;
        //}
        //if there is not, get data from mysql database and save into redis
        //用lqw查到符合categoryId的Dish集合
        LambdaQueryWrapper<Dish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        lambdaQueryWrapper.like(dish.getName()!=null,Dish::getName,dish.getName());
        dishList=this.list(lambdaQueryWrapper).stream().map((i)->{
            Long id=i.getId();
            LambdaQueryWrapper<DishFlavor> lqw=new LambdaQueryWrapper<>();
            lqw.eq(DishFlavor::getDishId,id);
            List<DishFlavor> flavors=dishFlavorService.list(lqw);
            //将口味信息和基本信息装入dto并返回
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(i,dishDto);
            dishDto.setFlavors(flavors);
            return dishDto;
        }).collect(Collectors.toList());
        //redisTemplate.opsForValue().set(key,dishList);
        return dishList;
    }
}
