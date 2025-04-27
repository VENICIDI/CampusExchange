// 文件路径: ...\src\main\java\org\campusmarket\exchange\controller\AuthController.java
package org.campusmarket.exchange.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.campusmarket.exchange.dto.LoginRequestDTO;
import org.campusmarket.exchange.dto.LoginResponseDTO;
import org.campusmarket.exchange.dto.Result;
import org.campusmarket.exchange.dto.UserRegisterDTO;
import org.campusmarket.exchange.service.ICaptchaService;
import org.campusmarket.exchange.service.IUserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

// 认证相关控制器
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private IUserService userService;
    
    @Resource
    private ICaptchaService captchaService;
    
    @Resource
    private AuthenticationManager authenticationManager;

    // 用户注册接口
    @PostMapping("/register")
    public Result<?> registerUser(@Valid @RequestBody UserRegisterDTO registerDTO) {
        // 校验验证码
        boolean isValidCaptcha = captchaService.validateCaptcha(
            registerDTO.getCaptchaKey(), 
            registerDTO.getCaptcha()
        );
        
        if (!isValidCaptcha) {
            return Result.error(400, "验证码错误或已过期");
        }
        
        // 校验两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            return Result.error(400, "两次输入的密码不一致");
        }
        
        // 调用服务进行注册
        Long userId = userService.register(registerDTO);
        return Result.success("注册成功，请等待管理员审核！", userId);
    }
    
    // 用户登录接口
    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        // 验证码校验
        boolean isValidCaptcha = captchaService.validateCaptcha(
            loginRequest.getCaptchaId(), 
            loginRequest.getCaptchaCode()
        );
        
        if (!isValidCaptcha) {
            return Result.error(400, "验证码错误或已过期");
        }
        
        // 使用Spring Security进行认证
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getPrincipal(),
                loginRequest.getPassword()
            )
        );
        
        // 认证成功，将认证信息存入上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 创建登录响应
        LoginResponseDTO response = new LoginResponseDTO();
        response.setUsername(authentication.getName());
        // role字段也需要设置
        String role = authentication.getAuthorities().stream()
            .findFirst()
            .map(authority -> authority.getAuthority().replace("ROLE_", ""))
            .orElse("USER");
        response.setRole(role);
        
        return Result.success("登录成功", response);
    }
    
    // 获取验证码ID
    @GetMapping("/captcha")
    public Result<String> getCaptchaId() {
        String captchaId = captchaService.generateCaptcha();
        return Result.success(captchaId);
    }
    
    // 获取验证码图片
    @GetMapping("/captcha/{captchaId}")
    public void getCaptchaImage(@PathVariable String captchaId, HttpServletResponse response) throws IOException {
        response.setContentType("image/jpeg");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        BufferedImage image = captchaService.getCaptchaImage(captchaId);
        if (image != null) {
            ImageIO.write(image, "jpg", response.getOutputStream());
        }
    }
}