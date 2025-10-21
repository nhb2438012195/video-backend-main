package com.nhb.service.impl.video;

import com.nhb.DAO.UserDAO;
import com.nhb.DAO.VideoDetailsDAO;
import com.nhb.Entity.VideoDetails;
import com.nhb.VO.RandomVideoInfoVO;
import com.nhb.service.video.VideoRecommendationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class VideoRecommendationsServiceImpl implements VideoRecommendationsService {
    @Autowired
    private VideoDetailsDAO videoDetailsDAO;
    @Autowired
    private UserDAO userDAO;

    @Override
    public List<RandomVideoInfoVO> getRandomVideoInfo(String num) {
        if(Objects.isNull(num)){
            throw new RuntimeException("num参数不能为空");
        }
        int numInt = Integer.parseInt(num);
        if(numInt > 20){
            throw new RuntimeException("num参数不能大于20");
        }
       List<VideoDetails> videoDetailsList = videoDetailsDAO.getRandomVideoDetails(numInt);
        return videoDetailsList.stream().map(videoDetails -> RandomVideoInfoVO.builder()
                .videoTitle(videoDetails.getVideoTitle())
                .videoAuthor(
                        userDAO.getUserById(videoDetails.getVideoAuthorId()).getName())
                .videoLength(videoDetails.getVideoLength())
                .videoPlayVolume(videoDetails.getVideoPlayVolume())
                .videoBarrageVolume(videoDetails.getVideoBarrageVolume())
                .createTime(videoDetails.getCreateTime())
                .state(videoDetails.getState())
                .videoCover(videoDetails.getVideoCover())
                .build()
        ).collect(Collectors.toList());
    }
}
