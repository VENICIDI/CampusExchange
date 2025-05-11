// 文件路径: ...\src\main\java\org\campusmarket\exchange\controller\AuthController.java
package org.campusmarket.exchange.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.campusmarket.exchange.dto.LoginRequestDTO;
import org.campusmarket.exchange.dto.LoginResponseDTO;
import org.campusmarket.exchange.dto.Result;
import org.campusmarket.exchange.dto.UserRegisterDTO;
import org.campusmarket.exchange.entity.User;
// --- 确保导入 BusinessException ---
import org.campusmarket.exchange.exception.BusinessException;
// --- 导入结束 ---
import org.campusmarket.exchange.service.ICaptchaService;
import org.campusmarket.exchange.service.IUserService;
import org.springframework.http.HttpStatus; // 导入 HttpStatus (如果需要更精细控制)
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 认证相关控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private IUserService userService;

    @Resource
    private ICaptchaService captchaService;

    @Resource
    private AuthenticationManager authenticationManager;

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public Result<?> registerUser(@Valid @RequestBody UserRegisterDTO registerDTO) {
        System.out.println("收到注册请求: " + registerDTO);

        // 校验验证码
        boolean isValidCaptcha = captchaService.validateCaptcha(
                registerDTO.getCaptchaKey(),
                registerDTO.getCaptcha()
        );
        if (!isValidCaptcha) {
            System.out.println("验证码验证失败: captchaKey=" + registerDTO.getCaptchaKey() + ", captcha=" + registerDTO.getCaptcha());
            // 抛出业务异常，由 GlobalExceptionHandler 处理返回 400
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "验证码错误或已过期");
        }

        // 校验两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            // 抛出业务异常，由 GlobalExceptionHandler 处理返回 400
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "两次输入的密码不一致");
        }

        // 调用服务进行注册 (如果 userService.register 内部有校验失败也会抛出 BusinessException)
        Long userId = userService.register(registerDTO);
        // 注册成功，返回 200 OK
        return Result.success("注册成功，请等待管理员审核！", userId);
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        System.out.println("收到登录请求: principal=" + loginRequest.getPrincipal() + ", captchaId=" + loginRequest.getCaptchaId());

        // 1. 验证码校验
        boolean isValidCaptcha = captchaService.validateCaptcha(
                loginRequest.getCaptchaId(),
                loginRequest.getCaptchaCode()
        );
        if (!isValidCaptcha) {
            System.out.println("验证码验证失败");
            // 抛出业务异常，由 GlobalExceptionHandler 处理返回 400
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "验证码错误或已过期");
        }

        System.out.println("验证码验证通过，开始身份认证");

        // 2. Spring Security 身份认证
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getPrincipal(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            System.out.println("用户名或密码错误: " + e.getMessage());
            // 抛出业务异常，由 GlobalExceptionHandler 处理（可指定 401 Unauthorized）
            // 注意：GlobalExceptionHandler 默认 BusinessException 返回 400，如需 401 需要定制
            // 简单起见，仍用 400 或 401 内部代码，依赖 GlobalExceptionHandler 的 @ResponseStatus(400)
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "用户名或密码错误，请重新输入"); // 使用 401 状态码对应的数值
        } catch (DisabledException e) {
            System.out.println("账号被禁用: " + e.getMessage());
            // 抛出业务异常，由 GlobalExceptionHandler 处理（可指定 403 Forbidden）
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "账号已被禁用，请联系管理员"); // 使用 403 状态码对应的数值
        } catch (LockedException e) {
            System.out.println("账号被锁定: " + e.getMessage());
            // 抛出业务异常，由 GlobalExceptionHandler 处理（可指定 403 Forbidden）
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "账号已被锁定，请联系管理员"); // 使用 403 状态码对应的数值
        } catch (UsernameNotFoundException e) {
            // UserServiceImpl 的 loadUserByUsername 抛出此异常
            System.out.println("用户不存在或状态异常: " + e.getMessage());
            // 抛出业务异常，由 GlobalExceptionHandler 处理（可指定 404 Not Found）
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), e.getMessage()); // 使用 404 状态码对应的数值和原始消息
        } catch (AuthenticationException e) {
            // 其他 Spring Security 认证异常
            System.out.println("认证异常: " + e.getMessage());
            // 抛出业务异常，由 GlobalExceptionHandler 处理（可指定 401 Unauthorized）
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "登录认证失败: " + e.getMessage()); // 使用 401 状态码对应的数值
        }
        // 注意：这里不需要 catch (Exception e)，让 GlobalExceptionHandler 处理最后的通用异常

        // 3. 认证成功处理
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 从认证对象中获取用户名
        String username = authentication.getName();
        
        // 查询数据库获取完整用户信息
        User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统错误：无法获取用户信息");
        }

        LoginResponseDTO response = new LoginResponseDTO();
        response.setUserId(user.getId()); // 设置用户ID
        response.setUsername(username);
        response.setAvatar(user.getAvatar()); // 设置头像
        
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElse("USER");
        response.setRole(role);

        System.out.println("登录成功: username=" + username + ", userId=" + user.getId() + ", role=" + role);
        // 认证成功，返回 200 OK
        return Result.success("登录成功", response);
    }

    /**
     * 获取验证码ID
     */
    @GetMapping("/captcha")
    public Result<String> getCaptchaId() {
        String captchaId = captchaService.generateCaptcha();
        // 成功，返回 200 OK
        return Result.success(captchaId);
    }

    /**
     * 获取验证码图片
     */
    @GetMapping("/captcha/{captchaId}")
    public void getCaptchaImage(@PathVariable String captchaId, HttpServletResponse response) throws IOException {
        response.setContentType("image/jpeg");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        BufferedImage image = captchaService.getCaptchaImage(captchaId);
        if (image != null) {
            ImageIO.write(image, "jpg", response.getOutputStream());
            // 成功，隐式返回 200 OK
        } else {
            // 如果需要处理图片获取失败的情况，可以考虑抛出异常或设置错误状态码
            response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 例如返回 404
        }
    }
}