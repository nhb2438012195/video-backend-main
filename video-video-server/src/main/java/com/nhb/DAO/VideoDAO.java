package com.nhb.DAO;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nhb.Entity.Video;
import com.nhb.Entity.VideoDetails;
import com.nhb.mapper.VideoMapper;
import org.springframework.stereotype.Service;

@Service
public class VideoDAO extends ServiceImpl<VideoMapper, Video> {

    public Video addVideo(Video build) {
        boolean result = save(build);
        if (result) {
            return build;
        } else {
            throw new RuntimeException("视频创建失败");
        }
    }


}
