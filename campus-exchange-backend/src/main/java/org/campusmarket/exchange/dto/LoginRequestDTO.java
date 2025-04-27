// 文件路径: E:\soft_ware\idea_projects\CampusExchange\campus-exchange-backend\src\main\java\org\campusmarket\exchange\dto\LoginRequestDTO.java
package org.campusmarket.exchange.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// 用户登录请求的数据传输对象
@Data
public class LoginRequestDTO {

    @NotBlank(message = "登录凭证不能为空") // 可以是用户名、手机号或邮箱
    private String principal; // 使用一个通用字段表示登录凭证

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在 6 到 20 之间")
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
    
    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;
}