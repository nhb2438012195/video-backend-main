package com.nhb.service.impl;

import cn.hutool.core.lang.UUID;
import com.nhb.DAO.VideoDAO;
import com.nhb.DAO.VideoDetailsDAO;
import com.nhb.Entity.Video;
import com.nhb.Entity.VideoDetails;
import com.nhb.service.VideoService;
import com.nhb.util.MinIOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VideoServiceImpl implements VideoService {
    @Autowired
    private VideoDAO videoDAO;
    @Autowired
    private MinIOUtil minIOUtil;
    @Autowired
    private VideoDetailsDAO videoDetailsDAO;

    @Override
    public String upload(MultipartFile video) throws Exception {
        return minIOUtil.uploadFile(video, "video"+ UUID.randomUUID());
    }

    @Override
    public Video createVideo() {
        VideoDetails videoDetails = videoDetailsDAO.addVideoDetails(new VideoDetails());

       Video video = videoDAO.addVideo(Video.builder()
                .videoId(null)
                .detailsId(videoDetails.getVideoDetailsId())
                .videoMpdUrl(null)
                .isReady(0)
                .build()
       );
       if(video.getVideoId()==null){
           throw new RuntimeException("视频创建失败");
       }
       // 更新视频详情表中的视频ID
        videoDetails.setVideoId(video.getVideoId());
        videoDetailsDAO.updateVideoIdById(videoDetails);
       return video;
    }
}
