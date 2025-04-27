package org.campusmarket.exchange.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.campusmarket.exchange.dto.Result;
import org.campusmarket.exchange.service.ICaptchaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 验证码相关控制器
 */
@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

    @Resource
    private ICaptchaService captchaService;

    /**
     * 生成验证码
     * @return 验证码ID
     */
    @GetMapping("/generate")
    public Result<String> generateCaptcha() {
        String captchaId = captchaService.generateCaptcha();
        return Result.success(captchaId, "验证码生成成功");
    }

    /**
     * 获取验证码图片
     * @param captchaId 验证码ID
     * @param response HTTP响应
     */
    @GetMapping("/image/{captchaId}")
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