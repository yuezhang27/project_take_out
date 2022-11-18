package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    public void saveWithSetmealDishes(SetmealDto setmealDto);
    public void deleteSetmealWithDishes(List<Long> ids);
    public void changeStatus(List<Long> ids,Integer status);
    public SetmealDto getSetmealWithDishes(Long id);
    public void updateWithSetmealDishes(SetmealDto setmealDto);
    public List<SetmealDto> getSetmealListWithDishes(Setmeal setmeal);
}
