package com.nhb.message;

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
public class VideoTranscodeMessage {
    // 视频在mysql数据库中的id，表名是video_play
    private String videoId;
    //视频在minio中的名字
    private String videoName;
    // 存储桶，默认是public
    private String bucket;

}
