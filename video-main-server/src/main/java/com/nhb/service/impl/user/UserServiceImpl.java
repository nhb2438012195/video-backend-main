package com.nhb.service.impl.user;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nhb.BO.JWTclaims;
import com.nhb.DAO.UserDAO;
import com.nhb.DTO.UserLoginDTO;
import com.nhb.DTO.UserRegisterDTO;
import com.nhb.Entity.User;
import com.nhb.VO.UserInfoVO;
import com.nhb.VO.UserLoginVO;
import com.nhb.exception.RegisterFailedException;
import com.nhb.mapper.UserMapper;
import com.nhb.properties.JwtProperties;
import com.nhb.service.user.UserService;
import com.nhb.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private UserDAO userDAO;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    public String login(UserLoginDTO userLoginDTO) {
        // 进行认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userLoginDTO.getUsername(),
                        userLoginDTO.getPassword()
                )
        );
        // 将认证信息保存在SecurityContextHolder中
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtil.createJWT( jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(),
                new JWTclaims(userLoginDTO.getUsername()).getClaims());
        return token;
    }

    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        if(Objects.isNull(userRegisterDTO)){
            throw new RegisterFailedException("不能发空请求");
        }
        if(!StringUtils.hasText(userRegisterDTO.getUsername()) || !StringUtils.hasText(userRegisterDTO.getPassword())){
            throw new RegisterFailedException("用户名或密码不能为空");
        }
        if(userRegisterDTO.getPassword().length() < 6){
            throw new RegisterFailedException("密码长度不能小于6位");
        }
        if(userRegisterDTO.getPassword().length() > 20){
            throw new RegisterFailedException("密码长度不能大于20位");
        }
        if(userDAO.getUserCountByUsername(userRegisterDTO.getUsername())!=0){
            throw new RegisterFailedException("用户已存在");
        }
        String password = passwordEncoder.encode(userRegisterDTO.getPassword());
        userDAO.register(new User(userRegisterDTO.getUsername(),password));

    }

    @Override
    public UserInfoVO getUserInfo(String username) {
        User user = userDAO.getUserByUsername(username);
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user,userInfoVO);
        return userInfoVO;
    }
}
