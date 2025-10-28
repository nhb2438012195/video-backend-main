package com.nhb.controller;

import com.nhb.DTO.InitChunkUploadDTO;
import com.nhb.VO.InitChunkUploadVO;
import com.nhb.entity.Video;
import com.nhb.exception.BusinessException;
import com.nhb.message.VideoTranscodeMessage;
import com.nhb.properties.VideoProperties;
import com.nhb.result.Result;
import com.nhb.service.CommonService;
import com.nhb.service.VideoService;
import com.nhb.session.ChunkUploadSession;
import com.nhb.util.RabbitMQUtil;
import com.nhb.util.RedisHashObjectUtils;
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

    @Autowired
    private RedisHashObjectUtils redisHashObjectUtils;

    @Autowired
    private CommonService commonService;
    @Operation(summary = "上传视频")
    @PostMapping("/upload")
    public Result upload(@RequestParam("video") MultipartFile video) throws Exception {
        log.info("开始上传视频");
        if (video.isEmpty()) {
            throw new BusinessException("上传视频失败:上传文件不能为空");
        }
        Video videoObject = null;
        String name = null;
            // 上传视频
             name = videoService.upload(video);
            // 创建视频对象
            videoObject = videoService.createVideo();
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
    @Operation(summary = "初始化分片上传视频")
    @PostMapping("/initChunkUpload")
    public Result initChunkUpload(InitChunkUploadDTO initChunkUploadDTO) {
        //校验用户名
        log.info("开始初始化分片上传");
        String username = commonService.checkUserName();
        InitChunkUploadVO initChunkUploadVO = null;
        initChunkUploadVO = videoService.initChunkUpload(initChunkUploadDTO, username);
        log.info("初始化分片上传成功:{}", initChunkUploadVO);
        return Result.success(initChunkUploadVO);
    }
    @Operation(summary = "上传分片视频")
    @PostMapping("/chunkUpload")
    public Result chunkUpload(@RequestParam("file") MultipartFile file,
                             @RequestParam("uploadKey") String uploadKey,
                             @RequestParam("chunkIndex") Integer chunkIndex) {
        log.info("开始上传分片视频:{}", chunkIndex);
        String username = commonService.checkUserName();
        //检查是否有权限上传
        if(!videoService.checkChunkUploadPermission(username, uploadKey,chunkIndex)){
            throw new BusinessException("上传分片视频失败:无权限上传");
        };
        //检查分片文件是否合理
        if(!videoService.checkChunkUploadFile(file)){
            throw new BusinessException("上传分片视频失败:分片文件不合法");
        }
        //获取上传会话
        ChunkUploadSession chunkUploadSession = null;
        chunkUploadSession = videoService.getChunkUploadSession(uploadKey,chunkIndex, username);
        log.info("获取上传会话成功:{}", chunkUploadSession);
        //上传分片视频
        videoService.uploadChunk(file, chunkIndex, chunkUploadSession);
        log.info("上传分片成功");
        return Result.success("上传分片成功");
    }
}
