package com.cqust.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cqust.dto.DishDto;
import com.cqust.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    public List<Dish> findAll();

    // 新增菜品，同时插入菜品对应的口味数据，需要操作两张表:dish、dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    // 根据id查询菜品信息和对应口味信息
    public DishDto getByIdWithFlavor(Long id);

    // 更新菜品信息，同时更新口味信息
    void updateWithFlavor(DishDto dto);
}
