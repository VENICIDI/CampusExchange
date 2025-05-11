package org.campusmarket.exchange.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.campusmarket.exchange.dto.UserDTO;
import org.campusmarket.exchange.util.UserContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 用户上下文过滤器
 * 用于从SecurityContextHolder获取认证信息并设置到UserContext
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 尝试从SecurityContextHolder获取认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() && 
                    !"anonymousUser".equals(authentication.getPrincipal())) {
                
                // 从认证对象中提取用户信息
                String username = authentication.getName();
                // 这里简化处理，实际项目中可能需要通过UserService等查询完整用户信息
                UserDTO userDTO = new UserDTO();
                userDTO.setUsername(username);
                
                // 尝试从请求头中获取用户ID
                String userIdHeader = request.getHeader("X-User-Id");
                if (userIdHeader != null && !userIdHeader.isEmpty()) {
                    try {
                        Long userId = Long.parseLong(userIdHeader);
                        userDTO.setId(userId);
                    } catch (NumberFormatException ignored) {
                        // 忽略解析异常
                    }
                }
                
                // 设置到UserContext
                UserContext.setCurrentUser(userDTO);
            } else {
                // 尝试从请求头中获取用户信息
                String userIdHeader = request.getHeader("X-User-Id");
                String usernameHeader = request.getHeader("X-User-Name");
                
                if (userIdHeader != null && !userIdHeader.isEmpty() && 
                        usernameHeader != null && !usernameHeader.isEmpty()) {
                    try {
                        UserDTO userDTO = new UserDTO();
                        userDTO.setId(Long.parseLong(userIdHeader));
                        userDTO.setUsername(usernameHeader);
                        
                        // 设置到UserContext
                        UserContext.setCurrentUser(userDTO);
                    } catch (NumberFormatException ignored) {
                        // 忽略解析异常
                    }
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