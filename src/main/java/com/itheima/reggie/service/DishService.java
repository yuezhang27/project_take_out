package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.dto.DishDto;

import java.util.List;

public interface DishService extends IService<Dish> {
    public void addNewDish(DishDto dishDto);
    public void updateDish(DishDto dishDto);
    public DishDto getByIdWithFlavor(Long id);
    public List<DishDto> getDishList(Dish dish);
}
