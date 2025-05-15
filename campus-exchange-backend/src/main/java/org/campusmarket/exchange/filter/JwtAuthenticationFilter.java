package org.campusmarket.exchange.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.campusmarket.exchange.util.UserContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器，从请求头中读取用户信息
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 从请求头获取用户ID和角色
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-User-Name");
        String role = request.getHeader("X-User-Role");
        
        // 如果请求头中包含用户ID，说明前端正在传递用户信息
        if (userId != null && !userId.isEmpty()) {
            try {
                Long userIdLong = Long.parseLong(userId);
                
                // 构建用户权限
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + (role != null ? role : "USER"));
                
                // 创建认证令牌
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        String.valueOf(userIdLong), // principal为用户ID字符串
                        null, // 没有credentials
                        Collections.singletonList(authority) // 权限集合
                );
                
                // 设置认证详情
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 设置到Spring Security上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // 设置到UserContext
                UserContext.setUserId(userIdLong);
                UserContext.setUsername(username);
                UserContext.setRole(role);
                
                logger.debug("已通过X-User-Id设置认证用户: " + userId);
            } catch (NumberFormatException e) {
                logger.error("X-User-Id解析失败: " + userId, e);
            }
        }
        
        filterChain.doFilter(request, response);
    }
} 