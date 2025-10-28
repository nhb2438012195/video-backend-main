package com.nhb.service;

import com.nhb.Entity.Video;
import org.springframework.web.multipart.MultipartFile;

public interface VideoService {
    String upload(MultipartFile video) throws Exception;

    Video createVideo();
}
