package com.nhb.controller;

import com.nhb.DTO.UserLoginDTO;
import com.nhb.DTO.UserRegisterDTO;
import com.nhb.VO.UserInfoVO;
import com.nhb.VO.UserLoginVO;
import com.nhb.result.Result;
import com.nhb.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "分类相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     *
     * @param userLoginDTO 用户登录信息
     * @return 登录成功返回JWT令牌
     */
    @Operation(summary = "用户登录", description = "用户登录接口，返回JWT令牌")
    @PostMapping("/login")
    public Result login(@RequestBody UserLoginDTO userLoginDTO) {
        try {
            // 进行登录
            String token = userService.login(userLoginDTO);
            if (!StringUtils.hasText(token)) {
                return Result.error("登录失败,token居然是空的");
            }
            UserLoginVO userLoginVO = new UserLoginVO(token);
            return Result.success(userLoginVO, "登录成功");
        } catch (BadCredentialsException e) {
            return Result.error("用户名或密码错误");
        }
    }

    /**
     * 用户注册
     *
     * @param userRegisterDTO 用户注册信息
     * @return 注册成功返回消息
     */

    @Operation(summary = "用户注册", description = "用户注册接口")
    @PostMapping("/register")
    public Result register(@RequestBody UserRegisterDTO userRegisterDTO) {
        userService.register(userRegisterDTO);
        return Result.success("注册成功");
    }

    @Operation(summary = "获取用户信息", description = "获取用户信息接口")
    @GetMapping("/userInfo")
    public Result getUserInfo() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserInfoVO userInfoVO = userService.getUserInfo(username);
        return Result.success(userInfoVO, "获取用户信息成功");
    }
}
