package org.campusmarket.exchange.filter;

import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.campusmarket.exchange.config.JwtConfig;
import org.campusmarket.exchange.dto.UserDTO;
import org.campusmarket.exchange.util.JwtUtils;
import org.campusmarket.exchange.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器，从Authorization请求头中提取并验证JWT令牌
 * 同时继续支持旧的基于X-User-*头的认证方式，确保向后兼容
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Resource
    private JwtConfig jwtConfig;
    
    @Resource
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 记录请求路径
        String requestURI = request.getRequestURI();
        logger.debug("处理请求: " + requestURI);
        
        // 先尝试从Authorization头获取JWT令牌
        String authHeader = request.getHeader(jwtConfig.getHeader());
        if (authHeader != null && authHeader.startsWith(jwtConfig.getPrefix())) {
            // 提取JWT令牌
            String jwtToken = jwtUtils.extractTokenFromHeader(authHeader);
            
            if (jwtToken != null && !jwtToken.isEmpty()) {
                try {
                    // 验证JWT令牌
                    if (jwtUtils.validateToken(jwtToken)) {
                        // 从JWT中提取用户信息
                        Long userId = jwtUtils.getUserIdFromToken(jwtToken);
                        String username = jwtUtils.getUsernameFromToken(jwtToken);
                        String role = jwtUtils.getRoleFromToken(jwtToken);
                        
                        logger.debug("JWT令牌有效，用户信息 - userId: " + userId + ", username: " + username + ", role: " + role);
                        
                        // 创建认证对象
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + (role != null ? role : "USER"));
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                String.valueOf(userId), // principal为用户ID字符串
                                null, // 没有credentials
                                Collections.singletonList(authority) // 权限集合
                        );
                        
                        // 设置认证详情
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // 设置到Spring Security上下文
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.debug("已设置认证对象到SecurityContext - JWT认证成功");
                        
                        // 设置到UserContext
                        UserContext.setUserId(userId);
                        UserContext.setUsername(username);
                        UserContext.setRole(role);
                        
                        // 创建UserDTO对象
                        UserDTO userDTO = jwtUtils.createUserDTO(jwtToken);
                        
                        // 设置到UserContext
                        UserContext.setCurrentUser(userDTO);
                    } else {
                        logger.warn("JWT令牌无效或已过期");
                    }
                } catch (Exception e) {
                    logger.error("JWT令牌解析失败: " + e.getMessage());
                }
            }
        } else {
            // 兼容模式：检查基于X-User-*头的旧认证方式
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
                    logger.debug("兼容模式：已设置认证对象到SecurityContext - 用户ID: " + userIdLong + ", 角色: " + securityRole);
                    
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
                    
                    logger.debug("兼容模式：已通过X-User-Id设置认证用户: " + userId + ", 角色: " + role + " -> " + roleValue);
                } catch (NumberFormatException e) {
                    logger.error("X-User-Id解析失败: " + userId, e);
                }
            } else {
                logger.debug("未检测到任何认证信息，请求将作为匿名处理");
            }
        }
        
        filterChain.doFilter(request, response);
    }
} 