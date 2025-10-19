package com.nhb.VO;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoVO {

    @TableField(value = "username", condition = SqlCondition.EQUAL)
    private String username; // 用户名

    @TableField("name")
    private String name; // 用户昵称

    @TableField("avatar")
    private String avatar; // 头像 URL

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 创建时间

    @TableField("state")
    private String state; // 用户状态：1=正常，2=被封禁

    @TableField("follow_quantity")
    private Integer followQuantity; // 关注数量

    @TableField("fans_quantity")
    private Integer fansQuantity; // 粉丝数

    @TableField("dynamic_quantity")
    private Integer dynamicQuantity; // 动态数

    @TableField("lv")
    private Integer lv; // 用户等级

    @TableField("experience")
    private Integer experience; // 经验

    @TableField("coin")
    private Integer coin; // 硬币数

    @TableField("phone")
    private String phone; // 手机号

    @TableField("vip")
    private Integer vip; // 会员：1=普通，2=大会员
}
