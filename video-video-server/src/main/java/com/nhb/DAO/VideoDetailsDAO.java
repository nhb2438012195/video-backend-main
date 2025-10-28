package com.nhb.DAO;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nhb.entity.VideoDetails;
import com.nhb.mapper.VideoDetailsMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoDetailsDAO extends ServiceImpl<VideoDetailsMapper, VideoDetails> {
    public List<VideoDetails> getRandomVideoDetails(int numInt) {
        return lambdaQuery()
                .last("ORDER BY RAND() LIMIT " + numInt)
                .list();
    }

    public VideoDetails addVideoDetails(VideoDetails videoDetails) {
        boolean result = save(videoDetails);
        if (result) {
            return videoDetails;
        } else {
            throw new RuntimeException("视频详情创建失败");
        }
    }

    public void updateVideoIdById(VideoDetails videoDetails) {
        updateById(videoDetails);
    }
}
