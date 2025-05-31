package org.campusmarket.exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 登录响应 DTO (包含JWT令牌和用户基本信息)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private String token; // JWT令牌
    private Long userId;
    private String username;
    private String role; // 角色名称字符串
    private String avatar; // 头像 URL

}