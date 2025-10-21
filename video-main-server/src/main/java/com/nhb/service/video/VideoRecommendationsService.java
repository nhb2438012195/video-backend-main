package com.nhb.service.video;

import com.nhb.VO.RandomVideoInfoVO;

import java.util.List;

public interface VideoRecommendationsService {
    List<RandomVideoInfoVO> getRandomVideoInfo(String num);
}
