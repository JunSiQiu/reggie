package com.cqust.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqust.common.R;
import com.cqust.dto.DishDto;
import com.cqust.entity.Category;
import com.cqust.entity.Dish;
import com.cqust.entity.DishFlavor;
import com.cqust.service.CategoryService;
import com.cqust.service.DishFlavorService;
import com.cqust.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
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
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * left join 连接查询
     */
    @GetMapping("/test")
    public void findAll(){
        System.out.println(dishService.findAll());
    }

    /**
     * 新增菜品
     * @param dto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dto){
        log.info(dto.toString());
        dishService.saveWithFlavor(dto);
        // 清理所有菜品缓存数据
        //Set keys = redisTemplate.keys("*");
        //redisTemplate.delete(keys);
        // 清理某个分类下的菜品缓存
        String key = "dish_" + dto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        return R.success(null,"新增菜品成功");
    }

    /**
     * 菜品信息分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<DishDto>> pageR(int page, int pageSize, String name){
        // 分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        // 条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 添加过滤条件
        queryWrapper.like(name != null, Dish::getName, name);
        // 排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        // 执行分页查询
        dishService.page(pageInfo, queryWrapper);
        // 对象拷贝
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();  // 分类id
            // 根据id查对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        })).collect(Collectors.toList());
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage,"success");
    }

    /**
     * 根据id查询菜品信息和口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto,"success");
    }

    /**
     * 修改菜品
     * @param dto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dto){
        log.info(dto.toString());
        dishService.updateWithFlavor(dto);
        // 清理所有菜品缓存数据
        //Set keys = redisTemplate.keys("*");
        //redisTemplate.delete(keys);
        // 清理某个分类下的菜品缓存
        String key = "dish_" + dto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        return R.success(null,"新增菜品成功");
    }

    /**
     * 根据条件查询菜品数据
     * @param dish
     * @return
     */
//    @GetMapping("/list")
//    public R<List<Dish>> listR(Dish dish){
//        // 构建条件查询
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
//        // 添加条件，查询状态为1（起售）
//        queryWrapper.eq(Dish::getStatus,1);
//        // 添加排序条件
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> list = dishService.list(queryWrapper);
//        return R.success(list, "success");
//    }

    @GetMapping("/list")
    public R<List<DishDto>> listR(Dish dish){
        List<DishDto> dishDtoList = null;
        // 动态构造key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        // 先从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        if(dishDtoList != null){
            // 如果存在，直接返回，无需查询数据库
            return R.success(dishDtoList, "success");
        }

        // 构建条件查询
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        // 添加条件，查询状态为1（起售）
        queryWrapper.eq(Dish::getStatus,1);
        // 添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);
        dishDtoList = list.stream().map((item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();  // 分类id
            // 根据id查对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            // 当前菜品的ID
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        })).collect(Collectors.toList());
        // 如果不存在，需要查询数据库，将查询到的菜品数据缓存到数据库
        redisTemplate.opsForValue().set(key, dishDtoList, 2, TimeUnit.HOURS);
        return R.success(dishDtoList, "success");
    }
}
