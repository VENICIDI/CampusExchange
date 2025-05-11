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

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        System.out.println("开始处理用户注册: " + registerDTO.getUsername() + ", 是否商家: " + registerDTO.getIsMerchant());
        try {
            // 检查用户名是否已存在
            User existUser = getUserByUsername(registerDTO.getUsername());
            if (existUser != null) {
                System.out.println("用户名已存在: " + registerDTO.getUsername());
                throw new BusinessException("用户名已存在");
            }

            // 检查手机号是否已存在
            existUser = getUserByPhone(registerDTO.getPhone());
            if (existUser != null) {
                System.out.println("手机号已被注册: " + registerDTO.getPhone());
                throw new BusinessException("手机号已被注册");
            }

            // 检查邮箱是否已存在
            existUser = getUserByEmail(registerDTO.getEmail());
            if (existUser != null) {
                System.out.println("邮箱已被注册: " + registerDTO.getEmail());
                throw new BusinessException("邮箱已被注册");
            }

            System.out.println("用户信息校验通过，开始创建用户");

            // 创建用户实体
            User user = new User();
            BeanUtils.copyProperties(registerDTO, user);

            // 密码加密 (此时会触发 @Lazy 加载 PasswordEncoder)
            try {
                System.out.println("开始加密密码");
                user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
                System.out.println("密码加密完成");
            } catch (Exception e) {
                System.out.println("密码加密失败: " + e.getMessage());
                e.printStackTrace();
                throw new BusinessException("密码加密失败: " + e.getMessage());
            }

            // 设置用户角色
            user.setRole(registerDTO.getIsMerchant() ? RoleEnum.MERCHANT : RoleEnum.USER);
            System.out.println("设置用户角色: " + user.getRole().name());

            // 设置用户状态为正常（临时方案：取消审核流程）
            // 注意：如果实际需要审核，这里应该是 PENDING
            user.setStatus(UserStatusEnum.NORMAL);
            System.out.println("设置用户状态: " + user.getStatus().name());

            // 保存用户
            try {
                System.out.println("开始保存用户信息");
                userMapper.insert(user);
                System.out.println("用户信息保存成功，ID: " + user.getId());
            } catch (Exception e) {
                System.out.println("保存用户信息失败: " + e.getMessage());
                e.printStackTrace();
                throw new BusinessException("保存用户信息失败: " + e.getMessage());
            }

            // 如果是商家用户，创建商家信息
            if (registerDTO.getIsMerchant()) {
                System.out.println("用户是商家，开始创建商家信息");
                try {
                    Merchant merchant = new Merchant();
                    merchant.setUserId(user.getId());
                    
                    // 检查必要字段是否存在
                    if (registerDTO.getShopName() == null || registerDTO.getShopName().isEmpty()) {
                        System.out.println("商家注册缺少店铺名称");
                        throw new BusinessException("店铺名称不能为空");
                    }
                    
                    merchant.setStoreName(registerDTO.getShopName());
                    
                    // 店铺简介可以为空
                    if (registerDTO.getShopIntro() != null) {
                        merchant.setDescription(registerDTO.getShopIntro());
                    }
                    
                    // 设置营业执照和身份证图片URL (实际项目中应该处理文件上传)
                    merchant.setBusinessLicense("待上传"); // 需要实际的文件URL
                    merchant.setIdCard("待上传"); // 需要实际的文件URL
                    
                    // 设置默认的商家等级为5（最低等级）
                    merchant.setLevelId(5L);
                    
                    // 初始化统计数据为0
                    merchant.setTotalSalesCount(0);
                    merchant.setTotalSalesAmount(new BigDecimal("0.00"));
                    merchant.setStorePositiveRate(new BigDecimal("0.00")); // 初始好评率设为0%
                    
                    // 设置创建和更新时间
                    merchant.setCreateTime(LocalDateTime.now());
                    merchant.setUpdateTime(LocalDateTime.now());

                    System.out.println("开始保存商家信息: " + merchant.getStoreName());
                    merchantMapper.insert(merchant);
                    System.out.println("商家信息保存成功");
                } catch (Exception e) {
                    System.out.println("保存商家信息失败: " + e.getMessage());
                    e.printStackTrace();
                    throw new BusinessException("保存商家信息失败: " + e.getMessage());
                }
            }

            System.out.println("用户注册完成，ID: " + user.getId());
            return user.getId();
        } catch (BusinessException be) {
            // 直接抛出业务异常
            throw be;
        } catch (Exception e) {
            // 记录并包装其他异常
            System.out.println("用户注册过程中发生系统异常: " + e.getMessage());
            e.printStackTrace();
            throw new BusinessException("注册失败: " + e.getMessage());
        }
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
        
        System.out.println("准备更新用户信息: " + user.getId());
        System.out.println("用户头像URL: " + user.getAvatar());
        
        // 先获取完整的用户信息
        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            System.out.println("用户不存在: " + user.getId());
            return false;
        }
        
        // 只更新不为null的字段（防止覆盖现有数据）
        if (user.getRealName() != null) {
            existingUser.setRealName(user.getRealName());
        }
        if (user.getPhone() != null) {
            existingUser.setPhone(user.getPhone());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getWechat() != null) {
            existingUser.setWechat(user.getWechat());
        }
        if (user.getDefaultAddress() != null) {
            existingUser.setDefaultAddress(user.getDefaultAddress());
        }
        if (user.getPersonalIntro() != null) {
            existingUser.setPersonalIntro(user.getPersonalIntro());
        }
        // 特别处理头像URL
        if (user.getAvatar() != null) {
            System.out.println("更新头像URL为: " + user.getAvatar());
            existingUser.setAvatar(user.getAvatar());
        }
        
        // 更新时间
        existingUser.setUpdateTime(LocalDateTime.now());
        
        int result = userMapper.updateById(existingUser);
        System.out.println("用户信息更新结果: " + (result > 0 ? "成功" : "失败"));
        return result > 0;
    }

    @Override
    public User getUserById(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return userMapper.selectById(userId);
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