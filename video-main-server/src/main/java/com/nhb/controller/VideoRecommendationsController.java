package com.nhb.controller;

import com.nhb.VO.RandomVideoInfoVO;
import com.nhb.result.Result;
import com.nhb.service.video.VideoRecommendationsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/video")
@Slf4j
@Tag(name = "获取随机推荐视频接口")
public class VideoRecommendationsController {

    @Autowired
    private VideoRecommendationsService videoRecommendationsService;

    @Operation(summary = "获取随机视频信息")
    @GetMapping("/recommendedVideoInfo")
    public Result getRandomVideoInfo(@RequestParam String num) {
        List<RandomVideoInfoVO> videoInfoVOList = videoRecommendationsService.getRandomVideoInfo(num);
        return Result.success(videoInfoVOList);
    }

}
