package com.nhb.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoTranscodeCommand {

    //视频在minio中的名字
    private String videoName;
    // 存储桶，默认是public
    private String bucket;
    //上传会话的uploadKey
    private String uploadKey;

}
