package com.nhb.controller;

import cn.hutool.core.lang.UUID;
import com.nhb.Entity.Video;
import com.nhb.Message.VideoTranscodeMessage;
import com.nhb.properties.VideoProperties;
import com.nhb.result.Result;
import com.nhb.service.VideoService;
import com.nhb.util.RabbitMQUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/video")
@Slf4j
@Tag(name = "视频播放接口")
public class VideoController {
    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoProperties videoProperties;
    @Autowired
    private RabbitMQUtil rabbitMQUtil;
    @Operation(summary = "上传视频")
    @PostMapping("/upload")
    public Result upload(@RequestParam("video") MultipartFile video) throws Exception {
        log.info("开始上传视频");
        if (video.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        Video videoObject = null;
        String name = null;
        try {
            // 上传视频
             name = videoService.upload(video);
            // 创建视频对象
            videoObject = videoService.createVideo();
        } catch (Exception e) {
            log.error("上传视频失败", e);
            return Result.error("创建视频对象失败");
        }
        VideoTranscodeMessage videoTranscodeMessage = VideoTranscodeMessage.builder()
                .videoId(String.valueOf(videoObject.getVideoId()))// 视频id,这里要写数据库里的id
                .videoName(name)
                .bucket(videoProperties.getBucket())
                .build();
        log.info("发送视频转码消息:{}", videoTranscodeMessage);
        rabbitMQUtil.sendJsonMessage(
                videoProperties.getExchange(),  // 1. 发送到哪个 Exchange
                videoProperties.getRoutingKey(),           // 2. 使用什么 Routing Key
                videoTranscodeMessage                        // 3. 要发送的消息对象
        );
        return Result.success("上传成功"+ name);
    }
}
