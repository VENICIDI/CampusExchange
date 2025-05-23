package org.campusmarket.exchange.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.campusmarket.exchange.dto.UserDTO;
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
        
        // 记录请求路径
        String requestURI = request.getRequestURI();
        logger.debug("处理请求: " + requestURI);
        
        // 从请求头获取用户ID和角色
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-User-Name");
        String role = request.getHeader("X-User-Role");
        
        logger.debug("请求头信息 - X-User-Id: " + userId + ", X-User-Name: " + username + ", X-User-Role: " + role);
        
        // 如果请求头中包含用户ID，说明前端正在传递用户信息
        if (userId != null && !userId.isEmpty()) {
            try {
                Long userIdLong = Long.parseLong(userId);
                
                // 将前端传来的角色名称标准化为Spring Security期望的格式
                String securityRole = role != null ? role : "USER";
                logger.debug("原始角色: " + securityRole);
                
                // 构建用户权限
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + securityRole);
                logger.debug("创建权限: " + authority.getAuthority());
                
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
                logger.debug("已设置认证对象到SecurityContext - 用户ID: " + userIdLong + ", 角色: " + securityRole);
                
                // 设置到UserContext
                UserContext.setUserId(userIdLong);
                UserContext.setUsername(username);
                UserContext.setRole(role);
                
                // 用户角色转换并放入UserDTO对象
                UserDTO userDTO = new UserDTO();
                userDTO.setId(userIdLong);
                userDTO.setUsername(username);
                
                // 将字符串角色转换为Integer
                Integer roleValue = null;
                if (role != null) {
                    if ("MERCHANT".equals(role)) {
                        roleValue = 1;
                    } else if ("ADMIN".equals(role)) {
                        roleValue = 2;
                    } else {
                        roleValue = 0; // 默认USER
                    }
                }
                userDTO.setRole(roleValue);
                
                // 设置到UserContext
                UserContext.setCurrentUser(userDTO);
                
                logger.debug("已通过X-User-Id设置认证用户: " + userId + ", 角色: " + role + " -> " + roleValue);
            } catch (NumberFormatException e) {
                logger.error("X-User-Id解析失败: " + userId, e);
            }
        } else {
            logger.debug("未在请求头中找到用户ID，请求将作为匿名处理");
        }
        
        filterChain.doFilter(request, response);
    }
} 