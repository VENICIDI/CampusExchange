// 文件路径: ...\src\main\java\org\campusmarket\exchange\service\impl\UserServiceImpl.java
package org.campusmarket.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
// --- 1. 导入 @Lazy 注解 ---
import org.springframework.context.annotation.Lazy;
// --- 导入结束 ---
import org.campusmarket.exchange.dto.UserRegisterDTO;
import org.campusmarket.exchange.entity.Merchant;
import org.campusmarket.exchange.entity.User;
import org.campusmarket.exchange.enums.RoleEnum;
import org.campusmarket.exchange.enums.UserStatusEnum;
import org.campusmarket.exchange.exception.BusinessException;
import org.campusmarket.exchange.mapper.MerchantMapper;
import org.campusmarket.exchange.mapper.UserMapper;
import org.campusmarket.exchange.service.ICaptchaService;
import org.campusmarket.exchange.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder; // 确保导入 PasswordEncoder
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements IUserService, UserDetailsService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private MerchantMapper merchantMapper;

    // --- 2. 在注入 PasswordEncoder 时添加 @Lazy ---
    @Lazy
    @Resource // 或者 @Autowired
    private PasswordEncoder passwordEncoder;
    // --- 修改结束 ---

    @Resource
    private ICaptchaService captchaService; // 假设 ICaptchaService 不依赖 SecurityConfig 中的 Bean

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(UserRegisterDTO registerDTO) {
        // 检查用户名是否已存在
        User existUser = getUserByUsername(registerDTO.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 检查手机号是否已存在
        existUser = getUserByPhone(registerDTO.getPhone());
        if (existUser != null) {
            throw new BusinessException("手机号已被注册");
        }

        // 检查邮箱是否已存在
        existUser = getUserByEmail(registerDTO.getEmail());
        if (existUser != null) {
            throw new BusinessException("邮箱已被注册");
        }

        // 创建用户实体
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);

        // 密码加密 (此时会触发 @Lazy 加载 PasswordEncoder)
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        // 设置用户角色
        user.setRole(registerDTO.getIsMerchant() ? RoleEnum.MERCHANT : RoleEnum.USER);

        // 设置用户状态为正常（临时方案：取消审核流程）
        // 注意：如果实际需要审核，这里应该是 PENDING
        user.setStatus(UserStatusEnum.NORMAL);

        // 保存用户
        userMapper.insert(user);

        // 如果是商家用户，创建商家信息
        if (registerDTO.getIsMerchant()) {
            Merchant merchant = new Merchant();
            merchant.setUserId(user.getId());
            merchant.setStoreName(registerDTO.getShopName()); // 假设 DTO 中有 shopName
            merchant.setDescription(registerDTO.getShopIntro()); // 假设 DTO 中有 shopIntro
            // 设置营业执照和身份证图片URL (实际项目中应该处理文件上传)
            merchant.setBusinessLicense("待上传"); // 需要实际的文件URL
            merchant.setIdCard("待上传"); // 需要实际的文件URL

            merchantMapper.insert(merchant);
        }

        return user.getId();
    }

    @Override
    public User getUserByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public User getUserByPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return null;
        }
        System.out.println("尝试通过手机号查询用户: " + phone);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);
        User user = userMapper.selectOne(queryWrapper);
        System.out.println("通过手机号查询用户结果: " + (user != null ? "找到用户" : "未找到用户"));
        return user;
    }

    @Override
    public User getUserByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public List<User> getPendingUsers() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getStatus, UserStatusEnum.PENDING);
        return userMapper.selectList(queryWrapper);
    }

    @Override
    public boolean updateUserStatus(Long userId, UserStatusEnum status) {
        if (userId == null || status == null) {
            return false;
        }
        User user = new User();
        user.setId(userId);
        user.setStatus(status);
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean updateUser(User user) {
        if (user == null || user.getId() == null) {
            return false;
        }
        return userMapper.updateById(user) > 0;
    }

    /**
     * Spring Security UserDetailsService 接口实现
     * 用于根据用户名（或手机号、邮箱）加载用户信息进行认证
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("尝试加载用户，用户名/手机号/邮箱: " + username);

        User user = getUserByUsername(username);
        if (user == null) {
            System.out.println("通过用户名未找到用户，尝试通过手机号查询");
            user = getUserByPhone(username);
        }
        if (user == null) {
            System.out.println("通过手机号未找到用户，尝试通过邮箱查询");
            user = getUserByEmail(username);
        }

        if (user == null) {
            System.out.println("用户不存在: " + username);
            // 这个异常会被 DaoAuthenticationProvider 捕获
            // 如果 SecurityConfig 中设置了 hideUserNotFoundExceptions=false，这个异常会原样抛出
            throw new UsernameNotFoundException("用户不存在，请检查输入或注册新账号");
        }

        System.out.println("找到用户: " + user.getUsername() + ", 状态: " + user.getStatus());

        // 根据用户状态决定是否允许登录 (抛出 UsernameNotFoundException 以符合接口要求，但消息更具体)
        boolean accountNonLocked = true; // 假设没有锁定逻辑
        boolean credentialsNonExpired = true; // 假设密码不过期
        boolean accountNonExpired = true; // 假设账号不过期
        boolean enabled = false; // 默认不可用

        switch (user.getStatus()) {
            case NORMAL:
                enabled = true;
                break;
            case PENDING:
                // 抛出异常，消息会被 AuthController 捕获并转换
                throw new UsernameNotFoundException("账号正在审核中，请耐心等待");
            case DISABLED:
                // 抛出异常，消息会被 AuthController 捕获并转换
                throw new UsernameNotFoundException("账号已被禁用，请联系管理员");
            default:
                // 其他未知状态，也按不可用处理
                throw new UsernameNotFoundException("账号状态异常，请联系管理员");
        }

        // 设置用户权限 (角色)
        List<GrantedAuthority> authorities = new ArrayList<>();
        // 确保 RoleEnum.name() 返回的是正确的角色字符串 (如 "USER", "ADMIN")
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        // 返回 Spring Security 需要的 UserDetails 对象
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), // 使用用户名作为 principal
                user.getPassword(), // 数据库中存储的加密密码
                enabled,            // 账号是否启用
                accountNonExpired,  // 账号是否未过期
                credentialsNonExpired, // 凭证（密码）是否未过期
                accountNonLocked,   // 账号是否未锁定
                authorities         // 用户权限列表
        );
    }
}