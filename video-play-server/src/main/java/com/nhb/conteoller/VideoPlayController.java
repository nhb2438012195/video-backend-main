package com.nhb.conteoller;

import com.nhb.service.VideoPlayService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/video")
@Slf4j
@Tag(name = "视频播放接口")
public class VideoPlayController {
    @Autowired
    private VideoPlayService videoPlayService;
}
