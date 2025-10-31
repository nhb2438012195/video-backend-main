package com.nhb.service.messageConsumer;


import com.nhb.command.VideoTranscodeCommand;
import com.nhb.context.ChunkUploadContext;
import com.nhb.util.RabbitMQUtil;
import com.nhb.util.RedisHashObjectUtils;
import com.nhb.util.S3Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Slf4j
@Service
public class VideoTranscodeMessageConsumer {
    @Autowired
    private S3Util s3Util;
    @Autowired
    private RabbitMQUtil rabbitMQUtil;
    @Autowired
    private RedisHashObjectUtils redisHashObjectUtils;
    @RabbitListener(queues = "${video.transcode.queue}")
    public void transcode(VideoTranscodeCommand message) {

        System.out.println("接收到视频转码消息：" + message.getVideoName()+"uploadKey:"+message.getUploadKey());
        //合并分片
        log.info("开始合并分片：{}", message.getUploadKey());
        ChunkUploadContext chunkUploadContext = redisHashObjectUtils.getObject(message.getUploadKey(), ChunkUploadContext.class);
        s3Util.completeMultipartUpload(chunkUploadContext.getUploadId(), chunkUploadContext.getPartETags(), chunkUploadContext.getObjectName());
        log.info("合并分片完成：{}", message.getUploadKey());
        log.info("开始转码：{}", message.getVideoName());
    }
}
