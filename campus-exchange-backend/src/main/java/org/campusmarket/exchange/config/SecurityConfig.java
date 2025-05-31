package org.campusmarket.exchange.config;

import jakarta.annotation.Resource; 
// import org.springframework.beans.factory.annotation.Autowired; 
import org.campusmarket.exchange.filter.JwtAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService; 
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// --- 导入结束 ---

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 安全配置类
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // 保持 prePostEnabled = true
public class SecurityConfig {

    // --- 注入 UserDetailsService ---
    // Spring 会自动找到你标记了 @Service 且实现了 UserDetailsService 的 UserServiceImpl
    @Resource // 或者 @Autowired
    private UserDetailsService userDetailsService;
    
    @Resource
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    // --- 注入结束 ---

    // PasswordEncoder Bean 保持不变
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager Bean 保持不变
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        // 它会自动使用下面定义的 AuthenticationProvider Bean
        return authConfig.getAuthenticationManager();
    }

    // --- 新增 AuthenticationProvider Bean 定义 ---
    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) { // 可以直接注入上面定义的 passwordEncoder
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        // 设置用于加载用户信息的 UserDetailsService (你的 UserServiceImpl)
        provider.setUserDetailsService(userDetailsService);
        // 设置用于密码比对的 PasswordEncoder
        provider.setPasswordEncoder(passwordEncoder);
        // *** 这是关键修改：设置不隐藏 "用户未找到" 异常 ***
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }
    // --- Bean 定义结束 ---

    // SecurityFilterChain Bean 保持不变
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 添加日志，记录SecurityConfig初始化
        System.out.println("正在配置SecurityFilterChain...");
        
        http
                .authorizeHttpRequests(authz -> authz
                        // 公共接口，允许匿名访问
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/captcha", "/api/auth/captcha/**","/index.html").permitAll()
                        // 新增：允许访问根路径 '/'
                        .requestMatchers("/").permitAll()
                        // 允许匿名访问商品和分类相关API
                        .requestMatchers("/api/products/**", "/api/categories/**", "/api/static/**").permitAll()
                        // 允许匿名访问商家相关API
                        .requestMatchers("/api/merchants/**").permitAll()
                        // 允许访问买家信息API (商家和管理员可访问)
                        .requestMatchers("/api/buyer-info/**").hasAnyRole("MERCHANT", "ADMIN")
                        // 文件上传相关API匿名访问
                        .requestMatchers("/api/upload/**", "/api/files/**").permitAll()
                        // 允许获取当前用户信息
                        .requestMatchers("/api/users/current").permitAll()
                        // 允许访问用户个人资料API
                        .requestMatchers("/api/users/profile", "/api/users/profile/**").permitAll()
                        // 允许访问默认头像和静态资源
                        .requestMatchers("/api/default-avatar.png", "/images/**", "/api/images/**").permitAll()
                        // 购物车和订单相关API需要认证
                        .requestMatchers("/api/cart/**", "/api/orders/**").authenticated()
                        // 管理员API只允许ADMIN角色访问
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 其他请求需要认证 (确保这行在最后)
                        .anyRequest().authenticated()
                )
                // 禁用CSRF保护
                .csrf(AbstractHttpConfigurer::disable)
                // 启用CORS (使用下面的 corsConfigurationSource Bean)
                .cors(Customizer.withDefaults())
                // 配置会话管理为无状态
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        System.out.println("SecurityFilterChain配置完成，买家信息API权限设置为商家和管理员可访问");
        
        return http.build();
    }

    // CorsConfigurationSource Bean 保持不变 (但检查 allowedOrigins 是否正确)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 建议允许所有常用头，或者至少包括 Content-Type 和 Authorization (如果未来使用)
        configuration.setAllowedHeaders(Arrays.asList("*")); // 或者更具体列表如 Arrays.asList("Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With", ...)
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 应用到所有路径
        return source;
    }
}