package com.cqust.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqust.entity.SetmealDish;
import com.cqust.mapper.SetmealDishMapper;
import com.cqust.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SetmealDishServiceImpl extends ServiceImpl<SetmealDishMapper, SetmealDish> implements SetmealDishService {
}
