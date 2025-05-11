package org.campusmarket.exchange.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.campusmarket.exchange.dto.Result;
import org.campusmarket.exchange.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class FileController {
    
    @Value("${file.upload.path:upload}")
    private String uploadPath;
    
    @Value("${file.upload.max-size:10485760}")
    private long maxFileSize; // 默认10MB
    
    @Value("${server.port:8080}")
    private int serverPort;
    
    @Value("${file.upload.domain:http://localhost}")
    private String domain;
    
    /**
     * 单个文件上传
     * @param file 文件
     * @return 文件URL
     */
    @PostMapping("/files/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "请选择要上传的文件");
        }
        
        try {
            // 打印上传目录信息，便于调试
            log.info("上传目录配置: {}", uploadPath);
            File uploadPathDir = new File(uploadPath);
            if (!uploadPathDir.exists()) {
                log.info("上传根目录不存在，尝试创建: {}", uploadPath);
                boolean created = uploadPathDir.mkdirs();
                if (!created) {
                    log.error("创建上传根目录失败: {}", uploadPath);
                    throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "无法创建文件上传目录");
                }
            }
            
            // 创建日期目录
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String dirPath = uploadPath + "/" + dateDir;
            // 确保路径分隔符在Windows下正确
            dirPath = dirPath.replace('/', File.separatorChar);
            
            Path uploadDir = Paths.get(dirPath);
            log.info("创建日期目录: {}", dirPath);
            
            if (!Files.exists(uploadDir)) {
                try {
                    Files.createDirectories(uploadDir);
                } catch (IOException e) {
                    log.error("创建日期目录失败: {}", e.getMessage());
                    throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "创建目录失败: " + e.getMessage());
                }
            }
            
            // 校验文件
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "无效的文件名");
            }
            
            // 校验文件大小
            if (file.getSize() > maxFileSize) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 
                        "文件 " + originalFilename + " 超出最大限制 " + (maxFileSize / 1024 / 1024) + "MB");
            }
            
            // 生成新文件名
            String fileExtension = getFileExtension(originalFilename);
            String newFileName = UUID.randomUUID().toString() + fileExtension;
            
            // 保存文件
            String filePath = dirPath + File.separator + newFileName;
            log.info("保存文件: {} -> {}", originalFilename, filePath);
            
            File dest = new File(filePath);
            try {
                file.transferTo(dest);
            } catch (IOException e) {
                log.error("保存文件失败: {}", e.getMessage());
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "保存文件失败: " + e.getMessage());
            }
            
            // 生成访问URL (保持URL中的斜杠格式)
            String urlDateDir = dateDir.replace('\\', '/');
            String fileUrl = domain + ":" + serverPort + "/api/static/" + urlDateDir + "/" + newFileName;
            
            log.info("文件上传成功: {} -> {}", originalFilename, fileUrl);
            
            return Result.success(fileUrl);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传图片
     * @param files 图片文件列表
     * @return 图片URL列表
     */
    @PostMapping("/upload/images")
    public Result<List<String>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "请选择要上传的图片");
        }
        
        List<String> imageUrls = new ArrayList<>();
        
        try {
            // 打印上传目录信息，便于调试
            log.info("上传目录配置: {}", uploadPath);
            File uploadPathDir = new File(uploadPath);
            if (!uploadPathDir.exists()) {
                log.info("上传根目录不存在，尝试创建: {}", uploadPath);
                boolean created = uploadPathDir.mkdirs();
                if (!created) {
                    log.error("创建上传根目录失败: {}", uploadPath);
                    throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "无法创建文件上传目录");
                }
            }
            
            // 创建日期目录
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String dirPath = uploadPath + "/" + dateDir;
            // 确保路径分隔符在Windows下正确
            dirPath = dirPath.replace('/', File.separatorChar);
            
            Path uploadDir = Paths.get(dirPath);
            log.info("创建日期目录: {}", dirPath);
            
            if (!Files.exists(uploadDir)) {
                try {
                    Files.createDirectories(uploadDir);
                } catch (IOException e) {
                    log.error("创建日期目录失败: {}", e.getMessage());
                    throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "创建目录失败: " + e.getMessage());
                }
            }
            
            // 处理每个文件
            for (MultipartFile file : files) {
                // 校验文件
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || originalFilename.isEmpty()) {
                    continue;
                }
                
                // 校验文件大小
                if (file.getSize() > maxFileSize) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 
                            "文件 " + originalFilename + " 超出最大限制 " + (maxFileSize / 1024 / 1024) + "MB");
                }
                
                // 校验文件类型
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 
                            "文件 " + originalFilename + " 不是有效的图片类型");
                }
                
                // 生成新文件名
                String fileExtension = getFileExtension(originalFilename);
                String newFileName = UUID.randomUUID().toString() + fileExtension;
                
                // 保存文件
                String filePath = dirPath + File.separator + newFileName;
                log.info("保存文件: {} -> {}", originalFilename, filePath);
                
                File dest = new File(filePath);
                try {
                    file.transferTo(dest);
                } catch (IOException e) {
                    log.error("保存文件失败: {}", e.getMessage());
                    throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "保存文件失败: " + e.getMessage());
                }
                
                // 生成访问URL (保持URL中的斜杠格式)
                String urlDateDir = dateDir.replace('\\', '/');
                String fileUrl = domain + ":" + serverPort + "/api/static/" + urlDateDir + "/" + newFileName;
                imageUrls.add(fileUrl);
                
                log.info("文件上传成功: {} -> {}", originalFilename, fileUrl);
            }
            
            return Result.success(imageUrls);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 扩展名
     */
    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex);
    }
} 