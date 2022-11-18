package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;
    @PostMapping
    public Result<String> addSetmeal(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithSetmealDishes(setmealDto);
        return Result.success("successfully added");
    }
    @GetMapping("/page")
    public Result<Page> getSetmeal(Integer page,Integer pageSize,String name){
        Page<Setmeal> pageInfo=new Page<>(page,pageSize);
        Page<SetmealDto> result=new Page<>();
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(name!=null,Setmeal::getName,name);
        setmealService.page(pageInfo,lambdaQueryWrapper);
        //复制current等属性到返回的page
        BeanUtils.copyProperties(pageInfo,result,"records");
        List<Setmeal> setmealList=pageInfo.getRecords();
        List<SetmealDto> records=setmealList.stream().map((i)->{
            SetmealDto item=new SetmealDto();
            BeanUtils.copyProperties(i,item);
            Long categoryId=i.getCategoryId();
            if(categoryId!=null){
                Category category=categoryService.getById(categoryId);
                String categoryName= category.getName();
                item.setCategoryName(categoryName);
            }
            return item;
        }).collect(Collectors.toList());
        result.setRecords(records);
        return Result.success(result);
    }
    @DeleteMapping
    public Result<String> deleteById(@RequestParam List<Long> ids){
        setmealService.deleteSetmealWithDishes(ids);
        return Result.success("successfully deleted");
    }
    @PostMapping("/status/{status}")
    public Result<String> changeStatus(@RequestParam List<Long> ids,@PathVariable Integer status){
        setmealService.changeStatus(ids, status);
        return Result.success("successfully changed status");
    }
    @GetMapping("/{id}")
    public Result<SetmealDto> getSetmealWithDishes(@PathVariable Long id){
        SetmealDto setmealDto=setmealService.getSetmealWithDishes(id);
        return Result.success(setmealDto);
    }
    @PutMapping
    public Result<String> updateSetmealWithDishes(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithSetmealDishes(setmealDto);
        return Result.success("successfully updated");
    }
    @GetMapping("/list")
    public Result<List<SetmealDto>> getSetmealListWithDishes (Setmeal setmeal){
        List<SetmealDto> result=setmealService.getSetmealListWithDishes(setmeal);
        return Result.success(result);
    }
}
