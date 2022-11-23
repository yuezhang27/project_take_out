package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CacheManager cacheManager;
    @CacheEvict(value="setmealCache",allEntries = true)
    public void saveWithSetmealDishes(SetmealDto setmealDto) {
        //保存不含菜品的普通套餐信息
        this.save(setmealDto);
        //操作setmeal-dish表，批量保存dto中的list
        //将完成save后产生的套餐id赋予dto中的list
        List<SetmealDish> setmealDishList=setmealDto.getSetmealDishes().stream().map((i)->{
            i.setSetmealId(setmealDto.getId());
            return i;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishList);
    }
    @CacheEvict(value="setmealCache",allEntries = true)
    public void deleteSetmealWithDishes(List<Long> ids) {
        //查询每一个id对应status情况
        for (Long id: ids) {
            Setmeal setmeal=this.getById(id);
            if(setmeal.getStatus()!=0){
                throw new CustomException("still selling");
            }
            this.removeById(id);
            LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(SetmealDish::getDishId,id);
            setmealDishService.remove(lambdaQueryWrapper);
        }
    }
    public void changeStatus(List<Long> ids,Integer status) {
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        Integer oldStatus=(status!=0)?0:1;
        lambdaQueryWrapper.in(Setmeal::getId,ids).eq(Setmeal::getStatus,oldStatus);
        List<Setmeal> list=this.list(lambdaQueryWrapper);
        list=list.stream().map((i)->{
            i.setStatus(status);
            return i;
        }).collect(Collectors.toList());
        this.updateBatchById(list);
    }
    public SetmealDto getSetmealWithDishes(Long id) {
        //从setmeal数据表获得基本信息
        //用setmeal表中的setmealId获得setmeal中dish,将这些dish封装进listsetmealDishes
        Setmeal setmeal=this.getById(id);
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
        List<SetmealDish> setmealDishes=setmealDishService.list(lambdaQueryWrapper);
        SetmealDto setmealDto=new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);
        setmealDto.setSetmealDishes(setmealDishes);
        return setmealDto;
    }

    @CacheEvict(value="setmealCache",allEntries = true)
    public void updateWithSetmealDishes(SetmealDto setmealDto) {
        this.updateById(setmealDto);
        Long id= setmealDto.getId();
        //删除原来所有的setmealDish，再重新save
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId,id);
        setmealDishService.remove(lambdaQueryWrapper);
        List<SetmealDish> setmealDishes=setmealDto.getSetmealDishes().stream().map((i)->{
            i.setSetmealId(id);
            return i;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }

    @Cacheable(value="setmealCache",key="#setmeal.categoryId+'_'+#setmeal.status")
    public List<SetmealDto> getSetmealListWithDishes(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        lambdaQueryWrapper.eq(Setmeal::getStatus,setmeal.getStatus());
        List<SetmealDto> setmealList=this.list(lambdaQueryWrapper).stream().map((i)->{
            Long id=i.getId();
            LambdaQueryWrapper<SetmealDish> lqw=new LambdaQueryWrapper<>();
            lqw.eq(SetmealDish::getSetmealId,id);
            List<SetmealDish> dishes=setmealDishService.list(lqw);
            //将口味信息和基本信息装入dto并返回
            SetmealDto setmealDto=new SetmealDto();
            BeanUtils.copyProperties(i,setmealDto);
            setmealDto.setSetmealDishes(dishes);
            return setmealDto;
        }).collect(Collectors.toList());
        return setmealList;
    }
}
