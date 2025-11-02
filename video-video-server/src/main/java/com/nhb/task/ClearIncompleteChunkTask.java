package com.nhb.task;

import com.nhb.properties.VideoProperties;
import com.nhb.util.S3Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class ClearIncompleteChunkTask {
    @Autowired
    private S3Util s3Util;
    @Autowired
    private VideoProperties videoProperties;

//    //清除未完成的分片任务
//    @Scheduled(initialDelay = 0, fixedRate = 3600_000) // 每小时一次，启动立刻执行
//    public void clearIncompleteChunkTask() {
//        log.info("清理未完成的分片任务...");
//        s3Util.clearAllIncompleteMultipartUploads(videoProperties.getBucket());
//    }


}
