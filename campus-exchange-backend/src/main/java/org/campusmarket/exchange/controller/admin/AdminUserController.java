package org.campusmarket.exchange.controller.admin;

import org.campusmarket.exchange.dto.PageResult;
import org.campusmarket.exchange.dto.Result;
import org.campusmarket.exchange.entity.User;
import org.campusmarket.exchange.enums.RoleEnum;
import org.campusmarket.exchange.enums.UserStatusEnum;
import org.campusmarket.exchange.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员用户管理控制器
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    
    @Autowired
    private IUserService userService;
    
    /**
     * 获取全部用户列表(分页查询)
     * @param page 页码
     * @param size 每页大小
     * @param role 角色筛选(可选)
     * @param status 状态筛选(可选)
     * @param keyword 关键词搜索(可选)
     * @return 分页用户数据
     */
    @GetMapping("")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) RoleEnum role,
            @RequestParam(required = false) UserStatusEnum status,
            @RequestParam(required = false) String keyword) {
        
        PageResult<User> pageResult = userService.getUserList(page, size, role, status, keyword);
        Map<String, Object> resultMap = Map.of(
            "list", pageResult.getRecords(),
            "total", pageResult.getTotal(),
            "pages", pageResult.getPages()
        );
        return Result.success(resultMap);
    }
    
    /**
     * 获取待审核用户列表
     */
    @GetMapping("/pending")
    public Result<?> getPendingUsers() {
        List<User> pendingUsers = userService.getPendingUsers();
        return Result.success(pendingUsers);
    }
    
    /**
     * 审核通过用户注册
     */
    @PutMapping("/{userId}/approve")
    public Result<?> approveUser(@PathVariable Long userId) {
        boolean success = userService.updateUserStatus(userId, UserStatusEnum.NORMAL);
        if (success) {
            return Result.success("用户审核通过");
        }
        return Result.error("操作失败，请检查用户ID是否存在");
    }
    
    /**
     * 拒绝用户注册
     */
    @PutMapping("/{userId}/reject")
    public Result<?> rejectUser(@PathVariable Long userId) {
        boolean success = userService.updateUserStatus(userId, UserStatusEnum.DISABLED);
        if (success) {
            return Result.success("已拒绝用户注册");
        }
        return Result.error("操作失败，请检查用户ID是否存在");
    }
    
    /**
     * 更新用户状态
     */
    @PutMapping("/{userId}/status")
    public Result<?> updateUserStatus(@PathVariable Long userId, @RequestParam UserStatusEnum status) {
        boolean success = userService.updateUserStatus(userId, status);
        if (success) {
            return Result.success("用户状态已更新");
        }
        return Result.error("操作失败，请检查用户ID是否存在");
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    public Result<User> getUserDetail(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{userId}")
    public Result<?> updateUser(@PathVariable Long userId, @RequestBody User user) {
        if (userId == null || !userId.equals(user.getId())) {
            return Result.error("用户ID不一致");
        }
        
        boolean success = userService.updateUser(user);
        if (success) {
            return Result.success("用户信息已更新");
        }
        return Result.error("更新用户信息失败，请检查用户ID是否存在");
    }
    
    /**
     * 获取用户统计数据
     * @return 用户统计数据
     */
    @GetMapping("/stats")
    public Result<?> getUserStats() {
        long totalUsers = userService.countAllUsers();
        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put("totalUsers", totalUsers);
        return Result.success(statsMap);
    }
} 