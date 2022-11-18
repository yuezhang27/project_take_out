package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto){
        dishService.addNewDish(dishDto);
        return Result.success("success");
    }
    @GetMapping("/page")
    public Result<Page> getDishes(Integer page,Integer pageSize, String name){
        //用于操作SQL查询的Page
        Page<Dish> pageInfo=new Page<>(page,pageSize);
        LambdaQueryWrapper<Dish> lqw=new LambdaQueryWrapper();
        lqw.like(name!=null,Dish::getName,name);
        dishService.page(pageInfo,lqw);
        //用于返回给Result的Page,其中包含的records（查询结果的List）里应该用Dto类
        Page<DishDto> dishDtoPage=new Page<>();
        List<Dish> records= pageInfo.getRecords();
        List<DishDto> list=records.stream().map((i)->{
            DishDto dishDto=new DishDto();
            Long categoryId=i.getCategoryId();
            Category category=categoryService.getById(categoryId);
            if(category!=null){
                String categoryName=category.getName();
                dishDto.setCategoryName(categoryName);
            }
            BeanUtils.copyProperties(i,dishDto);
            return dishDto;
        }).collect(Collectors.toList());
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        dishDtoPage.setRecords(list);
        return Result.success(dishDtoPage);
    }
    @GetMapping("/{id}")
    public Result<DishDto> getById(@PathVariable Long id){
        DishDto dishDto=dishService.getByIdWithFlavor(id);
        return Result.success(dishDto);
    }
    @PutMapping
    public Result<String> updateDish(@RequestBody DishDto dishDto){
        dishService.updateDish(dishDto);
        return Result.success("successfully updated");
    }
    @GetMapping("/list")
    public Result<List<DishDto>> getDishList(Dish dish){
        List<DishDto> result=dishService.getDishList(dish);
        return Result.success(result);
    }
}
