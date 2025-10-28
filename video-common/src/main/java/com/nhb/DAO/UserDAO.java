package com.nhb.DAO;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nhb.Entity.User;
import com.nhb.exception.RegisterFailedException;
import com.nhb.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserDAO extends ServiceImpl<UserMapper, User> {
    @Autowired
    private UserMapper userMapper;
    public Integer getUserCountByUsername(String username) {
       return lambdaQuery().eq(User::getUsername, username).count();
    }

    public void register(User user) {
        try {
            this.save(user);
        } catch (Exception e) {
            throw new RegisterFailedException("注册失败"+e.getMessage());
        }
    }

    public User getUserByUsername(String username) {
        return lambdaQuery()
                .eq(User::getUsername, username)
                .one();
    }

    public User getUserById(Long id) {
        return lambdaQuery()
                .eq(User::getUserId, id)
                .one();
    }
}
