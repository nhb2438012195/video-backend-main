package com.nhb.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("user") // 指定数据库表名（如果类名和表名一致，可省略）
public class User {

        @TableId(value = "user_id", type = IdType.AUTO) // 主键，自增
        private Long userId;

        @TableField(value = "username", condition = SqlCondition.EQUAL)
        private String username; // 用户名

        @TableField("name")
        private String name; // 用户昵称

        @TableField("password")
        private String password; // 密码（务必加密存储）

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

        public User (String username, String password) {
                this.username = username;
                this.password = password;
                this.state = "1";
                this.lv = 1;
                this.experience = 0;
                this.coin = 0;
                this.phone = "";
                this.vip = 1;
                this.followQuantity = 0;
                this.fansQuantity = 0;
                this.dynamicQuantity = 0;
                this.createTime = LocalDateTime.now();
                this.name = "用户"+ UUID.randomUUID().toString().substring(0, 8);
                log.info("用户注册成功！");
        }

}

