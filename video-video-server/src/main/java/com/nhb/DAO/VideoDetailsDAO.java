package com.nhb.DAO;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nhb.Entity.VideoDetails;
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
}
