package com.nhb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nhb.Entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
