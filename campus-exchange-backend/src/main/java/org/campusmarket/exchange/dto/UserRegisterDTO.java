// 文件路径: ...\src\main\java\org\campusmarket\exchange\dto\UserRegisterDTO.java
package org.campusmarket.exchange.dto;

import lombok.Data;
import org.campusmarket.exchange.enums.GenderEnum;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 用户注册数据传输对象
@Data
public class UserRegisterDTO {
    
    // 用户名
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须在4-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    // 密码
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;
    
    // 确认密码
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
    
    // 真实姓名
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;
    
    // 手机号
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    // 邮箱
    @Email(message = "邮箱格式不正确")
    private String email;
    
    // 城市
    private String city;
    
    // 性别
    private GenderEnum gender;
    
    // 银行账号
    private String bankAccount;
    
    // 个人简介
    @Size(max = 500, message = "个人简介不能超过500个字符")
    private String personalIntro;
    
    // 微信号
    private String wechat;
    
    // 是否是商家用户
    private Boolean isMerchant;
    
    // 店铺名称（仅商家用户需填写）
    private String shopName;
    
    // 店铺简介（仅商家用户需填写）
    @Size(max = 500, message = "店铺简介不能超过500个字符")
    private String shopIntro;
    
    // 营业执照图片（Base64编码，仅商家用户需填写）
    private String businessLicense;
    
    // 身份证图片（Base64编码，仅商家用户需填写）
    private String idCard;
    
    // 验证码
    @NotBlank(message = "验证码不能为空")
    private String captcha;
    
    // 验证码对应的key
    @NotBlank(message = "验证码KEY不能为空")
    private String captchaKey;
}