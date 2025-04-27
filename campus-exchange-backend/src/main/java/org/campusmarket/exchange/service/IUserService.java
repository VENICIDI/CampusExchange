// 文件路径: ...\src\main\java\org\campusmarket\exchange\service\IUserService.java
package org.campusmarket.exchange.service;

// 暂时不使用IService，避免可能的问题
// import com.baomidou.mybatisplus.extension.service.IService;
import org.campusmarket.exchange.dto.UserRegisterDTO;
import org.campusmarket.exchange.entity.User;
import org.campusmarket.exchange.enums.UserStatusEnum;

import java.util.List;

// 用户服务接口
public interface IUserService {

    // 处理用户注册（包括普通用户和商家）
    Long register(UserRegisterDTO registerDTO);
    
    // 通过用户名查询用户
    User getUserByUsername(String username);
    
    // 通过手机号查询用户
    User getUserByPhone(String phone);
    
    // 通过邮箱查询用户
    User getUserByEmail(String email);
    
    // 获取待审核用户列表
    List<User> getPendingUsers();
    
    // 更新用户状态
    boolean updateUserStatus(Long userId, UserStatusEnum status);
    
    // 更新用户信息
    boolean updateUser(User user);
    
    // 登录逻辑由 Spring Security 通过 UserDetailsService 处理
}