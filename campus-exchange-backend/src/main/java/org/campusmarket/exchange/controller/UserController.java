package org.campusmarket.exchange.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.campusmarket.exchange.dto.Result;
import org.campusmarket.exchange.entity.User;
import org.campusmarket.exchange.enums.UserStatusEnum;
import org.campusmarket.exchange.service.IUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Resource
    private IUserService userService;
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public Result<User> getCurrentUser(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                      @RequestHeader(value = "X-User-Name", required = false) String username) {
        if (userId != null && username != null) {
            User user = new User();
            try {
                user.setId(Long.parseLong(userId));
            } catch (NumberFormatException e) {
                user.setId(0L);
            }
            user.setUsername(username);
            user.setStatus(UserStatusEnum.NORMAL);
            return Result.success(user);
        }
        return Result.error(401, "未登录");
    }
    
    /**
     * 获取用户个人资料
     */
    @GetMapping("/profile")
    public Result<User> getUserProfile(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId != null) {
            try {
                long userIdLong = Long.parseLong(userId);
                User user = userService.getUserById(userIdLong);
                if (user != null) {
                    // 出于安全考虑，清除敏感信息
                    user.setPassword(null);
                    return Result.success(user);
                }
                return Result.error(404, "用户不存在");
            } catch (NumberFormatException e) {
                return Result.error(400, "无效的用户ID");
            }
        }
        return Result.error(401, "未登录");
    }
    
    /**
     * 更新用户个人信息
     */
    @PutMapping("/profile")
    public Result<?> updateUserProfile(@RequestBody User user, @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        try {
            long userIdLong = Long.parseLong(userId);
            // 确保只能修改自己的信息，并防止恶意修改ID
            user.setId(userIdLong);
            
            System.out.println("更新用户资料，用户ID: " + userIdLong + ", 头像URL: " + user.getAvatar());
            boolean success = userService.updateUser(user);
            
            if (success) {
                // 获取更新后的用户信息
                User updatedUser = userService.getUserById(userIdLong);
                System.out.println("用户资料更新成功，更新后的头像URL: " + (updatedUser != null ? updatedUser.getAvatar() : "null"));
                return Result.success("更新成功", updatedUser);
            } else {
                return Result.error(400, "更新失败");
            }
        } catch (NumberFormatException e) {
            return Result.error(400, "无效的用户ID");
        }
    }
    
    /**
     * 管理员获取待审核用户列表
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<User>> getPendingUsers() {
        List<User> pendingUsers = userService.getPendingUsers();
        return Result.success(pendingUsers);
    }
    
    /**
     * 管理员审核用户
     */
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> updateUserStatus(
            @PathVariable Long userId, 
            @RequestParam UserStatusEnum status) {
        boolean success = userService.updateUserStatus(userId, status);
        return success ? Result.success(null, "状态更新成功") : Result.error(400, "状态更新失败");
    }
} 