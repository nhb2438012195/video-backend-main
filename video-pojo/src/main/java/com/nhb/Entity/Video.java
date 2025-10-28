package com.nhb.Entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("video") // 指定数据库表名（如果类名和表名一致，可省略）
public class Video {

        @TableId(value = "video_id", type = IdType.AUTO) // 主键，自增
        private Long videoId;

        @TableField("details_id")
        private Long detailsId; // 视频详情ID

        @TableField("video_mpd_url")
        private String videoMpdUrl;// 视频MPD URL

        @TableField("is_ready")
        private Integer isReady; // 是否就绪


}

