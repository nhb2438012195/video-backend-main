package com.nhb.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "video")
@Data
public class VideoProperties {
    //视频存储桶
    private String bucket;
    //发送消息的交换机
    private String Exchange;
    //发送消息的key
    private String transcodeRoutingKey;
    //获取视频信息超时时间
    private long timeout;
    //最大分片大小
    private long maxChunkSize;

    private String videoTemporaryFile;

    private String dashFileSaveBucket;

    private String uploadRoutingKey;

    private String uploadQueue;



}
