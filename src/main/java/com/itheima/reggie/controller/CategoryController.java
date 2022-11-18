package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @PostMapping
    public Result<String> addCategory(@RequestBody Category category){
        categoryService.save(category);
        return Result.success("successfully added");
    }
    @GetMapping("/page")
    public Result<Page> selectAll(int page,int pageSize){
        Page<Category> p=new Page<>(page,pageSize);
        LambdaQueryWrapper<Category> lqw=new LambdaQueryWrapper<>();
        lqw.orderByAsc(Category::getSort);
        categoryService.page(p,lqw);
        return Result.success(p);
    }
    @DeleteMapping
    public Result<String> deleteById(Long id){
        categoryService.remove(id);
        return Result.success("successfully deleted");
    }
    @PutMapping
    public Result<String> editById(@RequestBody Category category){
        categoryService.updateById(category);
        return Result.success("successfully updated");
    }
    @GetMapping("/list")
    public Result<List<Category>> getCategoryList(Category category){
        LambdaQueryWrapper<Category> lqw=new LambdaQueryWrapper<>();
        lqw.eq((category.getType()!=null),Category::getType,category.getType());
        lqw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        return Result.success(categoryService.list(lqw));
    }
}
