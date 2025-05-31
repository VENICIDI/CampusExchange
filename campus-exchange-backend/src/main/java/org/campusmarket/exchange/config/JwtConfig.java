package org.campusmarket.exchange.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置类
 */
@Configuration
public class JwtConfig {
    
    @Value("${jwt.secret:campusExchangeSecretKey12345678901234567890}")
    private String secret;
    
    @Value("${jwt.expiration:86400}") // 默认24小时 (86400秒)
    private int expiration;
    
    @Value("${jwt.header:Authorization}")
    private String header;
    
    @Value("${jwt.prefix:Bearer }")
    private String prefix;
    
    /**
     * 获取JWT密钥
     */
    public String getSecret() {
        return secret;
    }
    
    /**
     * 获取JWT有效期（秒）
     */
    public int getExpiration() {
        return expiration;
    }
    
    /**
     * 获取存储JWT的请求头名称
     */
    public String getHeader() {
        return header;
    }
    
    /**
     * 获取JWT前缀
     */
    public String getPrefix() {
        return prefix;
    }
}
