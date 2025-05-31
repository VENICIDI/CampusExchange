package org.campusmarket.exchange.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import org.campusmarket.exchange.config.JwtConfig;
import org.campusmarket.exchange.dto.UserDTO;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类：处理JWT令牌的生成、解析和验证
 */
@Component
public class JwtUtils {
    
    @Resource
    private JwtConfig jwtConfig;
    
    /**
     * 生成JWT令牌
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @return JWT令牌
     */
    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("role", role);
        
        return createToken(claims, username);
    }
    
    /**
     * 从User对象生成令牌
     * 
     * @param userDTO 用户数据传输对象
     * @return JWT令牌
     */
    public String generateToken(UserDTO userDTO) {
        // 将角色数值转换为字符串
        String roleName = "USER";
        if (userDTO.getRole() != null) {
            switch (userDTO.getRole()) {
                case 1:
                    roleName = "MERCHANT";
                    break;
                case 2:
                    roleName = "ADMIN";
                    break;
                default:
                    roleName = "USER";
                    break;
            }
        }
        
        return generateToken(userDTO.getId(), userDTO.getUsername(), roleName);
    }
    
    /**
     * 创建令牌
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtConfig.getExpiration() * 1000L);
        
        // 使用HSA256算法和密钥生成JWT
        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    /**
     * 从令牌中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        String userId = claims.get("userId", String.class);
        return userId != null ? Long.parseLong(userId) : null;
    }
    
    /**
     * 从令牌中获取用户角色
     */
    public String getRoleFromToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        return claims.get("role", String.class);
    }
    
    /**
     * 从令牌中获取过期日期
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    /**
     * 从令牌中获取声明
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * 解析JWT获取所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 检查令牌是否过期
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    
    /**
     * 验证令牌
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }
    
    /**
     * 从请求中提取JWT令牌
     * 格式: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * 
     * @param authHeader 授权请求头内容
     * @return 提取的JWT令牌，如果不存在或格式不正确则返回null
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith(jwtConfig.getPrefix())) {
            return authHeader.substring(jwtConfig.getPrefix().length());
        }
        return null;
    }
    
    /**
     * 为已验证的令牌创建认证对象
     */
    public SimpleGrantedAuthority createAuthority(String token) {
        String role = getRoleFromToken(token);
        return new SimpleGrantedAuthority("ROLE_" + (role != null ? role : "USER"));
    }
    
    /**
     * 创建UserDTO对象
     */
    public UserDTO createUserDTO(String token) {
        Long userId = getUserIdFromToken(token);
        String username = getUsernameFromToken(token);
        String role = getRoleFromToken(token);
        
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId);
        userDTO.setUsername(username);
        
        // 角色名称转换为数字
        Integer roleValue = 0; // 默认USER
        if (role != null) {
            if ("MERCHANT".equals(role)) {
                roleValue = 1;
            } else if ("ADMIN".equals(role)) {
                roleValue = 2;
            }
        }
        userDTO.setRole(roleValue);
        
        return userDTO;
    }
}
