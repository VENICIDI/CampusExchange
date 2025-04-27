// 文件路径: ...\src\main\java\org\campusmarket\exchange\service\impl\UserServiceImpl.java
package org.campusmarket.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

// 用户服务实现类
@Service
public class UserServiceImpl implements IUserService, UserDetailsService {

    @Resource
    private UserMapper userMapper;
    
    @Resource
    private MerchantMapper merchantMapper;
    
    @Resource
    private PasswordEncoder passwordEncoder;
    
    @Resource
    private ICaptchaService captchaService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(UserRegisterDTO registerDTO) {
        // 验证验证码
        if (!captchaService.validateCaptcha(registerDTO.getCaptchaKey(), registerDTO.getCaptcha())) {
            throw new BusinessException("验证码错误或已过期");
        }
        
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
        
        // 密码加密
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        
        // 设置用户角色
        user.setRole(registerDTO.getIsMerchant() ? RoleEnum.MERCHANT : RoleEnum.USER);
        
        // 设置用户状态为待审核
        user.setStatus(UserStatusEnum.PENDING);
        
        // 保存用户
        userMapper.insert(user);
        
        // 如果是商家用户，创建商家信息
        if (registerDTO.getIsMerchant()) {
            Merchant merchant = new Merchant();
            merchant.setUserId(user.getId());
            merchant.setStoreName(registerDTO.getShopName());
            merchant.setDescription(registerDTO.getShopIntro());
            // 设置营业执照和身份证图片URL (实际项目中应该处理文件上传)
            merchant.setBusinessLicense("待上传");
            merchant.setIdCard("待上传");
            
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
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);
        return userMapper.selectOne(queryWrapper);
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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户信息
        User user = getUserByUsername(username);
        
        // 如果用户不存在，尝试通过手机号查询
        if (user == null) {
            user = getUserByPhone(username);
        }
        
        // 如果用户仍不存在，尝试通过邮箱查询
        if (user == null) {
            user = getUserByEmail(username);
        }
        
        // 用户不存在，抛出异常
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        
        // 用户状态非正常，无法登录
        if (user.getStatus() != UserStatusEnum.NORMAL) {
            throw new UsernameNotFoundException("用户状态异常，请联系管理员");
        }
        
        // 设置用户权限
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.getStatus() == UserStatusEnum.NORMAL, // 只有正常状态的用户可用
            true,
            true,
            true,
            authorities
        );
    }
}