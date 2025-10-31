package com.nhb.service;

import com.nhb.DTO.InitChunkUploadDTO;
import com.nhb.VO.InitChunkUploadVO;
import com.nhb.entity.Video;
import com.nhb.session.ChunkUploadSession;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface VideoService {
    String upload(MultipartFile video) throws Exception;

    Video createVideo();


    InitChunkUploadVO initChunkUpload(InitChunkUploadDTO initChunkUploadDTO, String username);

    boolean checkChunkUploadPermission(String username, String uploadId, Integer chunkIndex);

    boolean checkChunkUploadFile(MultipartFile file);

    ChunkUploadSession getChunkUploadSession(String uploadId, Integer chunkIndex, String username);

    void uploadChunk(MultipartFile file, Integer chunkIndex, ChunkUploadSession chunkUploadSession) throws IOException;

    void mergeChunks(ChunkUploadSession chunkUploadSession);

    void saveUploadSession(String uploadKey, ChunkUploadSession chunkUploadSession);
}
