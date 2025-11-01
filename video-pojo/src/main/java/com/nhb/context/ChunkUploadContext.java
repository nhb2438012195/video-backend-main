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
    //总分片数
    private Integer totalChunks;
    //已上传分片数
    private Integer uploadedChunkCount;
    //是否暂停上传
    private boolean isPaused;

}
