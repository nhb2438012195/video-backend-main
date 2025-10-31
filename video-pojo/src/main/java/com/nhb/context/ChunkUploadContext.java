package com.nhb.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChunkUploadContext {
    //上传会话ID
    private String uploadId;
    //对象名
    private String objectName;
    //分片上传的ETags
    private List<String> partETags;
    // 用户名
    private String userName;
    //总分片数
    private Integer totalChunks;
    //已上传分片数
    private Integer uploadedChunkCount;
    //已上传分片的分片索引
    private List<Integer> uploadedChunkIndexes;
    //是否上传完毕
    private boolean isUploaded;
    //是否暂停上传
    private boolean isPaused;
    //是否取消上传
    private boolean isCanceled;

}
