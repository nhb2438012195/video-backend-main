package com.nhb.service;

import com.nhb.DTO.InitChunkUploadDTO;
import com.nhb.VO.InitChunkUploadVO;
import com.nhb.entity.Video;
import com.nhb.context.ChunkUploadContext;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface VideoService {



    InitChunkUploadVO initChunkUpload(InitChunkUploadDTO initChunkUploadDTO, String username);

    boolean checkChunkUploadPermission(String username, String uploadId, Integer chunkIndex);

    boolean checkChunkUploadFile(MultipartFile file);

    ChunkUploadContext getChunkUploadSession(String uploadId, Integer chunkIndex, String username);

    void uploadChunk(MultipartFile file, Integer chunkIndex, ChunkUploadContext chunkUploadContext) throws IOException;

    void mergeChunks(ChunkUploadContext chunkUploadContext, String uploadKey);

    void saveUploadSession(String uploadKey, ChunkUploadContext chunkUploadContext);
}
