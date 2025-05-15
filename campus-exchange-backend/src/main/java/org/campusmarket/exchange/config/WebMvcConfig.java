package org.campusmarket.exchange.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC 配置
 * 用于配置静态资源访问路径和CORS
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class);

    @Value("${file.upload.path:upload}")
    private String configuredUploadPath;

    /**
     * 配置内容协商选项
     * 确保API接口返回JSON，而不是尝试解析为视图
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .favorParameter(false)
            .ignoreAcceptHeader(false)
            .defaultContentType(MediaType.APPLICATION_JSON);
    }
    
    /**
     * 配置请求路径匹配
     * 确保API路径优先被Controller处理而不是静态资源
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseTrailingSlashMatch(false);
    }

    /**
     * 配置静态资源处理器
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 解析上传路径
        String uploadPath = resolveUploadPath();
        log.info("上传路径: {}", uploadPath);
        
        // 只配置明确的静态资源路径
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        
        // 配置上传文件访问路径
        registry.addResourceHandler("/api/static/**")
                .addResourceLocations(uploadPath);
    }

    /**
     * 解析上传路径
     */
    private String resolveUploadPath() {
        try {
            Path path = Paths.get(configuredUploadPath);
            if (path.isAbsolute()) {
                return path.toUri().toString();
            } else {
                String dir = System.getProperty("user.dir");
                return "file:" + dir + File.separator + configuredUploadPath + File.separator;
            }
        } catch (Exception e) {
            log.error("上传路径配置错误: {}", e.getMessage());
            String fallback = "file:" + System.getProperty("user.dir") + File.separator + "upload_fallback" + File.separator;
            new File(System.getProperty("user.dir") + File.separator + "upload_fallback").mkdirs();
            return fallback;
            }
    }

    /**
     * 配置跨域请求处理 (CORS)
     * @param registry 跨域注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}