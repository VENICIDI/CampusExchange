package org.campusmarket.exchange.util;

import jakarta.servlet.http.HttpServletRequest;
import org.campusmarket.exchange.dto.UserDTO;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 用户上下文工具类
 * 用于在线程中存储和获取当前登录用户信息
 */
public class UserContext {
    
    private static final ThreadLocal<UserDTO> userThreadLocal = new ThreadLocal<>();
    
    /**
     * 设置当前登录用户
     * @param userDTO 用户信息
     */
    public static void setCurrentUser(UserDTO userDTO) {
        userThreadLocal.set(userDTO);
    }
    
    /**
     * 获取当前登录用户
     * @return 用户信息
     */
    public static UserDTO getCurrentUser() {
        return userThreadLocal.get();
    }
    
    /**
     * 获取当前登录用户ID
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        UserDTO userDTO = getCurrentUser();
        return userDTO != null ? userDTO.getId() : null;
    }
    
    /**
     * 从请求中获取用户ID（用于不需要认证的接口）
     * @return 用户ID
     */
    public static Long getUserIdFromRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                Object userId = request.getAttribute("userId");
                if (userId instanceof Long) {
                    return (Long) userId;
                }
                
                // 从请求参数中获取
                String userIdParam = request.getParameter("userId");
                if (userIdParam != null && !userIdParam.isEmpty()) {
                    return Long.parseLong(userIdParam);
                }
                
                // 从请求头中获取
                String userIdHeader = request.getHeader("X-User-Id");
                if (userIdHeader != null && !userIdHeader.isEmpty()) {
                    return Long.parseLong(userIdHeader);
                }
            }
        } catch (Exception ignored) {
            // 忽略异常
        }
        return null;
    }
    
    /**
     * 清除当前用户信息
     */
    public static void clear() {
        userThreadLocal.remove();
    }
} 