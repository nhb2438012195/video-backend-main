package com.nhb.service;

import com.nhb.DTO.UserLoginDTO;
import com.nhb.DTO.UserRegisterDTO;
import com.nhb.VO.UserInfoVO;

public interface UserService {
     void hello();

    String login(UserLoginDTO userLoginDTO);

    void register(UserRegisterDTO userRegisterDTO);

    UserInfoVO getUserInfo(String username);
}
