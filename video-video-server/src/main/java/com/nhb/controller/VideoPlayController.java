package com.nhb.controller;

import cn.hutool.core.lang.UUID;
import com.nhb.result.Result;
import com.nhb.service.VideoPlayService;
import com.nhb.util.MinIOUtil;
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
public class VideoPlayController {
    @Autowired
    private VideoPlayService videoPlayService;
    @Autowired
    private MinIOUtil minIOUtil;

    @Operation(summary = "上传视频")
    @PostMapping("/upload")
    public Result upload(@RequestParam("video") MultipartFile video) throws Exception {
        if (video.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        String url = minIOUtil.uploadFile(video, "video"+ UUID.randomUUID());
        return Result.success("上传成功");
    }
}
