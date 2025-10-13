package com.nhb.Entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("user") // 指定数据库表名（如果类名和表名一致，可省略）
public class User {

        @TableId(value = "id", type = IdType.AUTO) // 主键，自增
        private Long id;

        @TableField(value = "username", condition = SqlCondition.EQUAL)
        private String username; // 用户名

        @TableField("name")
        private String name; // 用户昵称

        @TableField("password")
        private String password; // 密码（务必加密存储）

        @TableField("avatar")
        private String avatar; // 头像 URL

        @TableField(value = "create_time", fill = FieldFill.INSERT)
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

