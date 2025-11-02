package com.nhb.service.consumer;


import com.nhb.DAO.VideoDAO;
import com.nhb.DAO.VideoDetailsDAO;
import com.nhb.command.VideoTranscodeCommand;
import com.nhb.context.ChunkUploadContext;
import com.nhb.entity.Video;
import com.nhb.entity.VideoDetails;
import com.nhb.exception.BusinessException;
import com.nhb.properties.VideoProperties;
import com.nhb.service.CommonService;
import com.nhb.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Slf4j
@Service
public class VideoTranscodeMessageConsumer {
    @Autowired
    private S3Util s3Util;
    @Autowired
    private RabbitMQUtil rabbitMQUtil;
    @Autowired
    private RedisHashObjectUtils redisHashObjectUtils;
    @Autowired
    private VideoDAO videoDAO;
    @Autowired
    private VideoDetailsDAO videoDetailsDAO;
    @Autowired
    private MinIOUtil minIOUtil;
    @Autowired
    private VideoProperties videoProperties;
    @Autowired
    private FFmpegUtils ffmpegUtils;
    @Autowired
    private CommonService commonService;
    @RabbitListener(queues = "${video.transcodeQueue}")
    public void transcode(VideoTranscodeCommand message)  {

        try {
            System.out.println("接收到视频转码消息：" + message.getVideoName()+"uploadKey:"+message.getUploadKey());
            //合并分片
            log.info("开始合并分片：{}", message.getUploadKey());
            ChunkUploadContext chunkUploadContext = redisHashObjectUtils.getObject(message.getUploadKey(), ChunkUploadContext.class);
            s3Util.completeMultipartUpload(chunkUploadContext.getUploadId(), chunkUploadContext.getPartETags(), chunkUploadContext.getObjectName());
            log.info("合并分片完成：{}", message.getUploadKey());
            log.info("开始转码：{}", message.getVideoName());
            //Video videoObject = createVideo();
            minIOUtil.downloadFileToLocal(message.getVideoName(), videoProperties.getVideoTemporaryFile()+message.getVideoName());
            Path input = Paths.get( videoProperties.getVideoTemporaryFile()+message.getVideoName());
            Path output = Paths.get(videoProperties.getVideoTemporaryFile()+message.getVideoName().replace(".mp4", "")+"/");
            // 执行转换（4秒分片）
            ffmpegUtils.convertMp4ToDash(input, output, 4);
            minIOUtil.uploadDirectory(videoProperties.getDashFileSaveBucket(), output.toString());
        } catch (Exception e) {
            log.error("视频转码失败：{}", message.getVideoName(), e);
        }finally {
            try {
                Files.delete(Paths.get(videoProperties.getVideoTemporaryFile()+message.getVideoName()));
                commonService.deleteFolder(videoProperties.getVideoTemporaryFile(), message.getVideoName().replace(".mp4", ""));
            } catch (Exception e) {
                log.error("删除临时文件失败：{}", message.getVideoName(), e);
            }
        }
    }

    public Video createVideo() {
        VideoDetails videoDetails = videoDetailsDAO.addVideoDetails(new VideoDetails());
        Video video = videoDAO.addVideo(Video.builder()
                .videoId(null)
                .detailsId(videoDetails.getVideoDetailsId())
                .videoMpdUrl(null)
                .isReady(0)
                .build()
        );
        if(video.getVideoId()==null){
            throw new BusinessException("视频创建失败");
        }
        // 更新视频详情表中的视频ID
        videoDetails.setVideoId(video.getVideoId());
        videoDetailsDAO.updateVideoIdById(videoDetails);
        return video;
    }
}
