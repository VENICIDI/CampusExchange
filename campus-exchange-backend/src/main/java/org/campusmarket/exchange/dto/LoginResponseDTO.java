// 文件路径: ...\src\main\java\org\campusmarket\exchange\dto\LoginResponseDTO.java
package org.campusmarket.exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 登录响应 DTO (Session 模式下可能主要用于登录成功后查询用户信息)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    // Token 字段已移除

    private Long userId;
    private String username;
    private String role; // 角色名称字符串
    private String avatar; // 头像 URL

}