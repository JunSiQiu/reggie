package com.cqust.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqust.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User>{
}
