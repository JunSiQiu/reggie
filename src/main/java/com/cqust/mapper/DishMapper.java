package com.cqust.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqust.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
    @Select("select d.*, c.name from dish d left join category c on d.category_id = c.id")
    List<Dish> findAll();
}
