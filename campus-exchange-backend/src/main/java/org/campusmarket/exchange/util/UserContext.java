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
    private static final ThreadLocal<Long> userIdThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> usernameThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> roleThreadLocal = new ThreadLocal<>();
    
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
        // 优先从用户线程变量获取
        UserDTO userDTO = getCurrentUser();
        if (userDTO != null && userDTO.getId() != null) {
            return userDTO.getId();
        }
        
        // 如果用户对象中没有ID，尝试从专门的ID线程变量获取
        Long userId = userIdThreadLocal.get();
        if (userId != null) {
            return userId;
        }
        
        // 最后尝试从请求中获取
        return getUserIdFromRequest();
    }
    
    /**
     * 设置用户ID
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        userIdThreadLocal.set(userId);
    }
    
    /**
     * 设置用户名
     * @param username 用户名
     */
    public static void setUsername(String username) {
        usernameThreadLocal.set(username);
    }
    
    /**
     * 获取用户名
     * @return 用户名
     */
    public static String getUsername() {
        String username = usernameThreadLocal.get();
        if (username != null) {
            return username;
        }
        
        UserDTO userDTO = getCurrentUser();
        return userDTO != null ? userDTO.getUsername() : null;
    }
    
    /**
     * 设置用户角色
     * @param role 角色
     */
    public static void setRole(String role) {
        roleThreadLocal.set(role);
    }
    
    /**
     * 获取用户角色
     * @return 角色
     */
    public static String getRole() {
        String role = roleThreadLocal.get();
        if (role != null) {
            return role;
        }
        
        UserDTO userDTO = getCurrentUser();
        if (userDTO != null && userDTO.getRole() != null) {
            // 根据角色数字返回角色名称
            switch (userDTO.getRole()) {
                case 0:
                    return "USER";
                case 1:
                    return "MERCHANT";
                case 2:
                    return "ADMIN";
                default:
                    return "USER";
            }
        }
        return null;
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
        userIdThreadLocal.remove();
        usernameThreadLocal.remove();
        roleThreadLocal.remove();
    }
} 