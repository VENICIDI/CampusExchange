package org.campusmarket.exchange.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户数据传输对象
 */
@Data
public class UserDTO implements Serializable {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户头像
     */
    private String avatar;
    
    /**
     * 用户手机号
     */
    private String mobile;
    
    /**
     * 用户邮箱
     */
    private String email;
    
    /**
     * 用户角色（0-普通用户，1-商家，2-管理员）
     */
    private Integer role;
    
    /**
     * 用户状态（0-正常，1-禁用）
     */
    private Integer status;
    
    /**
     * 登录令牌
     */
    private String token;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 