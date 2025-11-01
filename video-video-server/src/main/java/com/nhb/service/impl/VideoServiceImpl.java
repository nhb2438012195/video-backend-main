package com.nhb.service.impl;

import cn.hutool.core.lang.UUID;
import com.nhb.DAO.VideoDAO;
import com.nhb.DAO.VideoDetailsDAO;
import com.nhb.DTO.InitChunkUploadDTO;
import com.nhb.VO.InitChunkUploadVO;
import com.nhb.entity.Video;
import com.nhb.entity.VideoDetails;
import com.nhb.exception.BusinessException;
import com.nhb.command.VideoTranscodeCommand;
import com.nhb.properties.VideoProperties;
import com.nhb.service.VideoService;
import com.nhb.context.ChunkUploadContext;
import com.nhb.util.MinIOUtil;
import com.nhb.util.RabbitMQUtil;
import com.nhb.util.RedisHashObjectUtils;
import com.nhb.util.S3Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class VideoServiceImpl implements VideoService {
    @Autowired
    private VideoDAO videoDAO;
    @Autowired
    private MinIOUtil minIOUtil;
    @Autowired
    private VideoDetailsDAO videoDetailsDAO;
    @Autowired
    private RedisHashObjectUtils redisHashObjectUtils;
    @Autowired
    private VideoProperties videoProperties;
    @Autowired
    private S3Util s3Util;
    @Autowired
    private RabbitMQUtil rabbitMQUtil;
    @Override
    public String upload(MultipartFile video) throws Exception {
        return minIOUtil.uploadFile(video, "video"+ UUID.randomUUID());
    }

    @Override
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

    @Override
    public InitChunkUploadVO initChunkUpload(InitChunkUploadDTO initChunkUploadDTO, String username) {
        if(Objects.isNull(initChunkUploadDTO) || Objects.isNull(initChunkUploadDTO.getTotalChunks()) || initChunkUploadDTO.getTotalChunks()<=0){
            throw new BusinessException("TotalChunks参数错误:不能为null或者小于0");
        }
        String objectName=UUID.randomUUID()+"."+initChunkUploadDTO.getFileType().split("/")[1];
        String uploadKey = "chunkUpload."+username+"."+objectName;
        String uploadId =s3Util.initiateMultipartUpload(objectName);
        List<String> partETags = new ArrayList<>();
        for (int i = 0; i < initChunkUploadDTO.getTotalChunks(); i++) {
            partETags.add("");
        }
        ChunkUploadContext chunkUploadContext = ChunkUploadContext.builder()
                .uploadId(uploadId)
                .objectName(objectName)
                .partETags(partETags)
                .totalChunks(initChunkUploadDTO.getTotalChunks())
                .userName(username)
                .uploadedChunkCount(0)
                .isPaused(false)
                .build();
        redisHashObjectUtils.setObject(uploadKey, chunkUploadContext,videoProperties.getTimeout(), TimeUnit.MINUTES);
        s3Util.initiateMultipartUpload(objectName);
        return InitChunkUploadVO.builder()
                .uploadKey(uploadKey)
                .build();

    }

    @Override
    public boolean checkChunkUploadPermission(String username, String uploadId, Integer chunkIndex) {
        if(chunkIndex==null || chunkIndex<0){
            return false;
        }
        if(!username.equals(uploadId.split("\\.")[1])){
            return false;
        }
        return redisHashObjectUtils.exists(uploadId);
    }

    @Override
    public boolean checkChunkUploadFile(MultipartFile file) {
        if(file==null){
            return false;
        }
        if(file.getSize()>videoProperties.getMaxChunkSize()){
            return false;
        }
        return true;
    }

    @Override
    public ChunkUploadContext getChunkUploadSession(String uploadId, Integer chunkIndex, String username) {
        ChunkUploadContext chunkUploadContext = redisHashObjectUtils.getObject(uploadId, ChunkUploadContext.class);
        if(chunkUploadContext ==null){
            throw new BusinessException("上传视频失败:上传会话不存在");
        }
        return chunkUploadContext;
    }

    @Override
    public void uploadChunk(MultipartFile file, Integer chunkIndex, ChunkUploadContext chunkUploadContext) throws IOException {
        String partETag = s3Util.uploadPart(chunkUploadContext.getUploadId(), chunkIndex, file, chunkUploadContext.getObjectName());
        chunkUploadContext.getPartETags().set(chunkIndex-1, partETag);
        chunkUploadContext.setUploadedChunkCount(chunkUploadContext.getUploadedChunkCount()+1);
    }

    @Override
    public void mergeChunks(ChunkUploadContext chunkUploadContext, String uploadKey) {
        //s3Util.completeMultipartUpload(chunkUploadSession.getUploadId(), chunkUploadSession.getPartETags(), chunkUploadSession.getObjectName());
        Video videoObject = createVideo();
        VideoTranscodeCommand videoTranscodeCommand = VideoTranscodeCommand.builder()
                .videoId(String.valueOf(videoObject.getVideoId()))// 视频id,这里要写数据库里的id
                .videoName(chunkUploadContext.getObjectName())
                .bucket(videoProperties.getBucket())
                .uploadKey(uploadKey)
                .build();
        rabbitMQUtil.sendJsonMessage(
                videoProperties.getExchange(),  // 1. 发送到哪个 Exchange
                videoProperties.getRoutingKey(),           // 2. 使用什么 Routing Key
                videoTranscodeCommand                        // 3. 要发送的消息对象
        );
    }

    @Override
    public void saveUploadSession(String uploadKey, ChunkUploadContext chunkUploadContext) {
        redisHashObjectUtils.putField(uploadKey,"partETags", chunkUploadContext.getPartETags());
        redisHashObjectUtils.putField(uploadKey,"uploadedChunkCount", chunkUploadContext.getUploadedChunkCount());
    }
}
