<template>
  <div class="platform-settings">
    <h1>平台设置</h1>
    
    <div class="settings-cards">
      <!-- 商家等级管理卡片 -->
      <div class="settings-card" @click="navigateToMerchantLevels">
        <div class="card-icon">
          <el-icon><Medal /></el-icon>
        </div>
        <div class="card-content">
          <h3>商家等级与费率</h3>
          <p>管理不同商家等级及对应的交易手续费率</p>
        </div>
        <div class="card-action">
          <el-button type="primary">管理</el-button>
        </div>
      </div>
      
      <!-- 系统参数设置卡片 -->
      <div class="settings-card" @click="navigateToSystemParams">
        <div class="card-icon">
          <el-icon><Setting /></el-icon>
        </div>
        <div class="card-content">
          <h3>系统参数设置</h3>
          <p>配置系统基本参数，如平台名称、联系方式等</p>
        </div>
        <div class="card-action">
          <el-button type="primary">管理</el-button>
        </div>
      </div>
      
      <!-- 交易规则设置卡片 -->
      <div class="settings-card" @click="navigateToTradeRules">
        <div class="card-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="card-content">
          <h3>交易规则设置</h3>
          <p>设置平台交易规则、服务条款等</p>
        </div>
        <div class="card-action">
          <el-button type="primary">管理</el-button>
        </div>
      </div>
      
      <!-- 消息通知设置卡片 -->
      <div class="settings-card" @click="navigateToNotifications">
        <div class="card-icon">
          <el-icon><Bell /></el-icon>
        </div>
        <div class="card-content">
          <h3>消息通知设置</h3>
          <p>设置各类消息通知的内容模板</p>
        </div>
        <div class="card-action">
          <el-button type="primary">管理</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { Medal, Setting, Document, Bell } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const router = useRouter();

// 导航函数
const navigateToMerchantLevels = () => {
  router.push('/admin/merchant-levels');
};

const navigateToSystemParams = () => {
  // 目前此功能未实现，可以显示一个提示信息
  ElMessage({
    message: '系统参数设置功能正在开发中',
    type: 'info'
  });
};

const navigateToTradeRules = () => {
  // 目前此功能未实现，可以显示一个提示信息
  ElMessage({
    message: '交易规则设置功能正在开发中',
    type: 'info'
  });
};

const navigateToNotifications = () => {
  // 目前此功能未实现，可以显示一个提示信息
  ElMessage({
    message: '消息通知设置功能正在开发中',
    type: 'info'
  });
};
</script>

<style scoped>
.platform-settings {
  padding: 20px;
}

.platform-settings h1 {
  margin-bottom: 30px;
  font-size: 24px;
  color: #303133;
}

.settings-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.settings-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s;
}

.settings-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-3px);
}

.card-icon {
  width: 50px;
  height: 50px;
  background-color: #f0f5ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.card-icon .el-icon {
  font-size: 24px;
  color: #1890ff;
}

.card-content {
  flex: 1;
}

.card-content h3 {
  margin: 0 0 8px;
  font-size: 16px;
  color: #303133;
}

.card-content p {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

.card-action {
  margin-left: 16px;
}
</style> 