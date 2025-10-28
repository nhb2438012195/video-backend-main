package com.nhb.service.messageConsumer;


import com.nhb.message.VideoTranscodeMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;



@Service
public class VideoTranscodeMessageConsumer {

    @RabbitListener(queues = "${video.transcode.queue}")
    public void transcode(VideoTranscodeMessage  message) {
        System.out.println("接收到视频转码消息：" + message.getVideoName());
    }
}
