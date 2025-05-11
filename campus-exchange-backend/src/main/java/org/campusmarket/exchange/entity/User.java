// 文件路径: E:\soft_ware\idea_projects\CampusExchange\campus-exchange-backend\src\main\java\org\campusmarket\exchange\entity\User.java
package org.campusmarket.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.campusmarket.exchange.enums.GenderEnum;
import org.campusmarket.exchange.enums.RoleEnum;
import org.campusmarket.exchange.enums.UserStatusEnum;

import java.time.LocalDateTime;

// 用户实体类, 对应数据库 user 表
@Data
@TableName("user")
public class User {

    // 用户ID (主键)
    @TableId(type = IdType.AUTO)
    private Long id;

    // 用户名 (唯一)
    private String username;

    // 加密后的密码哈希值
    private String password;

    // 真实姓名
    private String realName;

    // 手机号 (登录标识, 唯一)
    private String phone;

    // 邮箱 (唯一)
    private String email;

    // 城市
    private String city;

    // 性别：FEMALE-女，MALE-男, UNKNOWN-未知
    private GenderEnum gender;

    // 银行账号
    private String bankAccount;

    // 头像URL
    private String avatar;

    // 角色：USER-普通用户，MERCHANT-商家，ADMIN-管理员
    private RoleEnum role;

    // 状态：PENDING-待审核，NORMAL-正常，DISABLED-禁用
    private UserStatusEnum status;
    
    // 默认收货地址
    private String defaultAddress;

    // 个人介绍
    private String personalIntro;

    // 微信号
    private String wechat;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;
}