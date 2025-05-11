package org.campusmarket.exchange.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path; // 引入 Path
import java.nio.file.Paths; // 引入 Paths

/**
 * Web MVC 配置
 * 用于配置静态资源访问路径和CORS
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class);

    @Value("${file.upload.path:upload}")
    private String configuredUploadPath; // 重命名，更清晰

    /**
     * 配置静态资源处理器
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String os = System.getProperty("os.name").toLowerCase();
        String resolvedResourceLocation;

        try {
            Path path = Paths.get(configuredUploadPath);

            if (path.isAbsolute()) {
                // 如果 application.properties 中配置的是绝对路径，则直接使用
                // 转换为 "file:" URI 格式, 例如 "file:///E:/path/" 或 "file:/var/www/path/"
                resolvedResourceLocation = path.toUri().toString();
                log.info("检测到绝对上传路径配置: {}", configuredUploadPath);
            } else {
                // 如果配置的是相对路径 (比如默认的 "upload")，则相对于项目当前工作目录
                String currentWorkDir = System.getProperty("user.dir");
                resolvedResourceLocation = "file:" + currentWorkDir + File.separator + configuredUploadPath + File.separator;
                log.info("检测到相对上传路径配置: {}, 将解析为: {}", configuredUploadPath, resolvedResourceLocation);
            }
        } catch (InvalidPathException e) {
            log.error("配置的上传路径 '{}' 无效: {}", configuredUploadPath, e.getMessage());
            // 提供一个默认的回退路径或抛出配置异常
            String currentWorkDir = System.getProperty("user.dir");
            resolvedResourceLocation = "file:" + currentWorkDir + File.separator + "upload_fallback" + File.separator;
            log.warn("回退到默认上传路径: {}", resolvedResourceLocation);
            // 确保这个回退目录存在或者应用有权限创建
            new File(currentWorkDir + File.separator + "upload_fallback").mkdirs();
        }


        // 确保资源位置以斜杠结尾，如果它是一个目录URI
        if (!resolvedResourceLocation.endsWith("/") && !resolvedResourceLocation.endsWith(File.separator)) {
            // 对于 file:///C:/path 这种格式，末尾已经是目录的URI表示，通常不需要再加斜杠
            // 但如果是 file:C:/path，则需要加
            if (resolvedResourceLocation.startsWith("file:") && !resolvedResourceLocation.matches("file:/+[A-Za-z]:/.*")) { // 简化判断非标准URI结尾
                resolvedResourceLocation += "/";
            }
        }


        log.info("最终配置静态资源[物理路径]为: {}", resolvedResourceLocation);
        System.out.println("最终配置静态资源[物理路径]为: " + resolvedResourceLocation); // 保留System.out便于快速查看

        // 配置静态资源访问路径
        // 当请求 URL 以 /api/static/ 开头时 (例如 /api/static/foo/bar.jpg)
        // Spring Boot 会从 resolvedResourceLocation 指定的物理路径下去寻找 foo/bar.jpg 这个文件
        registry.addResourceHandler("/api/static/**")
                .addResourceLocations(resolvedResourceLocation);

        // 配置默认静态资源路径 (例如 /favicon.ico, /css/style.css 等会从 src/main/resources/static/ 查找)
        // 这个通常用于前端静态文件，如果您的Vue项目是独立部署的，这个可能不是必需的，或者只用于后端自身的少量静态资源
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * 配置跨域请求处理 (CORS)
     * @param registry 跨域注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 匹配所有请求路径
                // .allowedOrigins("http://localhost:5173") // 更安全的做法是指定确切的前端源
                .allowedOriginPatterns("*") // 允许所有源，用于开发环境或需要更灵活配置时。生产环境建议收紧。
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的HTTP方法
                .allowedHeaders("*") // 允许所有请求头
                .allowCredentials(true) // 是否允许发送Cookie等凭证信息。如果前端需要携带凭证，后端必须设为true，且allowedOrigins不能为简单的"*", 需用allowedOriginPatterns或具体域名
                .maxAge(3600); // 预检请求（OPTIONS请求）的缓存时间，单位秒
    }
}