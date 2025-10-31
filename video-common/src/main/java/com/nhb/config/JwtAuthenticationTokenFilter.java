package com.nhb.config;

import com.nhb.properties.JwtProperties;
import com.nhb.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@Component
@Slf4j
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil; // 你需要实现这个工具类
    @Autowired
    private JwtProperties jwtProperties;
    // 定义不需要 Token 的路径（和 SecurityConfig 保持一致）
    private static final String[] PERMIT_ALL_PATHS = {
            "/user/login",
            "/user/register",
            "/admin/account/login",
            "/category/getall",
            "/video/random/visitor",
            "/druid/**",
            "/favicon.ico"
    };
    private boolean isPermitAll(String uri) {
        for (String path : PERMIT_ALL_PATHS) {
            if (path.endsWith("**")) {
                String prefix = path.substring(0, path.length() - 2);
                if (uri.startsWith(prefix)) return true;
            } else if (uri.equals(path) || uri.startsWith(path + "/")) {
                return true;
            }
        }
        return false;
    }
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain chain) throws ServletException, IOException {
            // 获取请求路径
            try {
                String requestURI = request.getRequestURI();
                // ✅ 跳过不需要认证的路径
                if (isPermitAll(requestURI)) {
                    chain.doFilter(request, response);
                    return;
                }
                String token = request.getHeader(jwtProperties.getUserTokenName());
                if (token.isEmpty()||!StringUtils.hasText(token) ) {
                    chain.doFilter(request, response);
                    // 未登录
                    return;
                }
                Claims claims = jwtUtil.parseJWT(jwtProperties.getUserSecretKey(),token);
                if (StringUtils.hasText(token) && Objects.nonNull(claims)) {
                    String username = claims.get("username").toString();
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                log.error("JWT 认证失败: {}", e.getMessage());
            }
            chain.doFilter(request, response);
        }

}
