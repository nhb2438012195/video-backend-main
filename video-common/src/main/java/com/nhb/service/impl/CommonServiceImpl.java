package com.nhb.service.impl;

import com.nhb.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Service
public class CommonServiceImpl implements CommonService {
@Override
    public String checkUserName() {
        String username =null;
        username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!StringUtils.hasText(username)) {
            throw new RuntimeException("用户名错误:"+ username);
        }
        return username;
    }

    @Override
    public String getUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
