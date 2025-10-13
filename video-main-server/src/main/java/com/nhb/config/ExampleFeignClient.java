package com.nhb.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// name：客户端名称（任意）；url：目标服务地址（非微服务场景需指定）
@FeignClient(name = "exampleApi", url = "https://api.weixin.qq.com/sns/jscode2session")
public interface ExampleFeignClient {

    // GET请求带参数
    @GetMapping("")
    String login(@RequestParam("js_code") String js_code,
                 @RequestParam("appid") String appid,
                  @RequestParam("secret") String secret,
                  @RequestParam("grant_type") String grant_type
    );

}