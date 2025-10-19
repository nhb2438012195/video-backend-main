package com.nhb.service.user;

import com.nhb.DTO.UserLoginDTO;
import com.nhb.DTO.UserRegisterDTO;
import com.nhb.Entity.User;
import com.nhb.VO.UserInfoVO;
import com.nhb.VO.UserLoginVO;

public interface UserService {
    String login(UserLoginDTO userLoginDTO);

    void register(UserRegisterDTO userRegisterDTO);

    UserInfoVO getUserInfo(String username);
}
