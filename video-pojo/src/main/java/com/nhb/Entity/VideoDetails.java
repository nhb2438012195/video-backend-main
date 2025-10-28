package com.nhb.Entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 视频实体类
 */
@Slf4j
@Data
@Builder
@AllArgsConstructor

@TableName("video_details") // 对应数据库表名
public class VideoDetails {

    @TableId(value = "video_details_id", type = IdType.AUTO)
    private Long videoDetailsId; // 主键，自增

    @TableField("video_id")
    private Long videoId;//视频ID

    @TableField("video_title")
    private String videoTitle; // 视频标题

    @TableField("video_description")
    private String videoDescription; // 视频描述

    @TableField("video_author_id")
    private Long videoAuthorId; // 视频作者ID

    @TableField("video_length")
    private String videoLength; // 视频时长（秒）

    @TableField("video_play_volume")
    private Integer videoPlayVolume; // 播放量

    @TableField("video_barrage_volume")
    private Integer videoBarrageVolume; // 弹幕数量

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 创建时间

    @TableField("state")
    private String state; // 状态：0=审核中，1=正常，2=下架

    @TableField("video_cover")
    private String videoCover;//视频封面

    public VideoDetails(){
        this.createTime=LocalDateTime.now();
        this.state="1";
    }
}