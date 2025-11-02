package com.nhb.service.consumer;

import com.nhb.command.ChunksUploadCommand;
import com.nhb.context.ChunkUploadContext;
import com.nhb.exception.BusinessException;
import com.nhb.properties.VideoProperties;
import com.nhb.result.Result;
import com.nhb.service.CommonService;
import com.nhb.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Service
public class VideoUploadMessageConsumer {
    @Autowired
    private VideoService videoService;
    @Autowired
    private VideoProperties videoProperties;
    @Autowired
    private CommonService commonService;
    @RabbitListener(queues = "${video.uploadQueue}")
    public void upload(ChunksUploadCommand  message)  {
        try {
            log.info("开始上传视频分片：{}", message.getChunkIndex());
            //获取上传会话
            ChunkUploadContext chunkUploadContext =
                    videoService.getChunkUploadSession(message.getUploadKey(),message.getChunkIndex(),message.getUsername());
            log.info("获取上传会话成功:{}", chunkUploadContext);
            //上传分片视频
            videoService.uploadChunk(new File(videoProperties.getVideoTemporaryFile()+message.getFileName()), message.getChunkIndex(), chunkUploadContext);
            log.info("上传分片成功");
            //保存上传会话
            videoService.saveUploadSession(message.getUploadKey(), chunkUploadContext);
            if(chunkUploadContext.getUploadedChunkCount().equals(chunkUploadContext.getTotalChunks())){
                log.info("所有分片上传成功,进行分片合并");
                //合并分片
                videoService.mergeChunks(chunkUploadContext,message.getUploadKey());
                log.info("分片合并成功");
            }
        } catch (Exception e) {
            log.error("上传分片视频失败:{}", e.getMessage(), e);
        } finally {
            log.info("删除临时文件:{}", videoProperties.getVideoTemporaryFile()+message.getFileName());
            try {
                Files.delete(Paths.get(videoProperties.getVideoTemporaryFile()+message.getFileName()));
            } catch (Exception e) {
                log.error("删除临时文件失败:{}", e.getMessage(),e);
            }
        }
    }
}
