package org.campusmarket.exchange.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.campusmarket.exchange.dto.UserDTO;
import org.campusmarket.exchange.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 用户上下文过滤器
 * 主要负责两个功能：
 * 1. 如果SecurityContext已经有认证信息(通常由JwtAuthenticationFilter设置)但UserContext为空，则将其同步到UserContext
 * 2. 在请求处理结束时清理ThreadLocal变量，防止内存泄漏
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20) // 确保在JwtAuthenticationFilter之后执行
public class UserContextFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(UserContextFilter.class);@Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 检查UserContext是否已被设置（通常由JwtAuthenticationFilter设置）
            if (UserContext.getCurrentUser() == null) {
                // 如果UserContext为空，尝试从SecurityContext获取信息
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                
                if (authentication != null && authentication.isAuthenticated() && 
                        !"anonymousUser".equals(authentication.getPrincipal())) {
                    
                    // 从认证对象中提取用户信息
                    String username = authentication.getName();
                    
                    // 尝试从认证对象的Principal中获取更多信息
                    Object principal = authentication.getPrincipal();
                    
                    // 构建UserDTO
                    UserDTO userDTO = new UserDTO();
                    userDTO.setUsername(username);
                    
                    // 如果principal是字符串且可能是用户ID，尝试解析
                    if (principal instanceof String) {
                        try {
                            Long userId = Long.parseLong((String) principal);
                            userDTO.setId(userId);
                            logger.debug("从认证对象获取用户ID: " + userId);
                        } catch (NumberFormatException ignored) {
                            // 忽略解析异常
                        }
                    }
                    
                    // 从认证对象的权限中提取角色
                    if (!authentication.getAuthorities().isEmpty()) {
                        String authority = authentication.getAuthorities().iterator().next().getAuthority();
                        if (authority != null && authority.startsWith("ROLE_")) {
                            String role = authority.substring(5); // 去掉"ROLE_"前缀
                            
                            // 转换为角色值
                            Integer roleValue = 0; // 默认USER
                            if ("MERCHANT".equals(role)) {
                                roleValue = 1;
                            } else if ("ADMIN".equals(role)) {
                                roleValue = 2;
                            }
                            
                            userDTO.setRole(roleValue);
                            logger.debug("从认证对象获取角色: " + role + " -> " + roleValue);
                        }
                    }
                    
                    // 设置到UserContext
                    UserContext.setCurrentUser(userDTO);
                    logger.debug("已从SecurityContext同步用户信息到UserContext");
                }
            }
            
            // 继续执行过滤器链
            filterChain.doFilter(request, response);
        } finally {
            // 清除用户上下文
            UserContext.clear();
        }
    }
} 