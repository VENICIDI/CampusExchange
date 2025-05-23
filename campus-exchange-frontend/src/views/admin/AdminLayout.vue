<template>
  <div class="admin-layout">
    <div class="admin-sidebar">
      <div class="sidebar-header">
        <h2>管理员后台</h2>
      </div>
      <div class="sidebar-nav">
        <router-link to="/admin" class="nav-item" exact>仪表盘</router-link>
        <router-link to="/admin/users" class="nav-item">
          用户管理
          <span v-if="pendingUsersCount > 0" class="badge">{{ pendingUsersCount }}</span>
        </router-link>
        <router-link to="/admin/merchants" class="nav-item">商家管理</router-link>
        <router-link to="/admin/products" class="nav-item">商品审核</router-link>
        <router-link to="/admin/discounts" class="nav-item">优惠券发放</router-link>
        <router-link to="/admin/settings" class="nav-item">平台设置</router-link>
      </div>
      <div class="sidebar-footer">
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </div>
    <div class="admin-content">
      <div class="admin-header">
        <div class="header-title">{{ pageTitle }}</div>
        <div class="header-user">
          <span>管理员: {{ adminName }}</span>
        </div>
      </div>
      <div class="admin-main">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { getPendingUsers } from '@/api/adminUser';

export default {
  setup() {
    const router = useRouter();
    const route = useRoute();
    
    const adminName = ref('管理员');
    const pageTitle = ref('管理员仪表盘');
    const pendingUsersCount = ref(0);
    
    // 根据当前路由设置页面标题
    const updatePageTitle = () => {
      switch(route.path) {
        case '/admin':
          pageTitle.value = '管理员仪表盘';
          break;
        case '/admin/users':
          pageTitle.value = '用户管理';
          break;
        case '/admin/merchants':
          pageTitle.value = '商家管理';
          break;
        case '/admin/products':
          pageTitle.value = '商品审核';
          break;
        case '/admin/discounts':
          pageTitle.value = '优惠券发放';
          break;
        case '/admin/settings':
          pageTitle.value = '平台设置';
          break;
        default:
          pageTitle.value = '管理员后台';
      }
    };
    
    // 加载待审核用户数量
    const loadPendingUsersCount = async () => {
      try {
        const response = await getPendingUsers();
        pendingUsersCount.value = response.data ? response.data.length : 0;
      } catch (error) {
        console.error('获取待审核用户数量失败:', error);
        pendingUsersCount.value = 0;
      }
    };
    
    // 从本地存储获取管理员信息
    const loadAdminInfo = () => {
      const userStr = localStorage.getItem('user');
      if (userStr) {
        try {
          const userData = JSON.parse(userStr);
          if (userData.username) {
            adminName.value = userData.username;
          }
        } catch (e) {
          console.error('解析用户数据失败:', e);
        }
      }
    };
    
    // 退出登录
    const handleLogout = () => {
      localStorage.removeItem('user');
      localStorage.removeItem('token');
      router.push('/login');
    };
    
    onMounted(() => {
      loadAdminInfo();
      updatePageTitle();
      loadPendingUsersCount();
    });
    
    // 监听路由变化，更新页面标题
    router.afterEach((to) => {
      updatePageTitle();
      // 当从用户管理页面切换到其他页面时，重新加载待审核用户数量
      if (to.path !== '/admin/users' && route.path === '/admin/users') {
        loadPendingUsersCount();
      }
    });
    
    return {
      adminName,
      pageTitle,
      pendingUsersCount,
      handleLogout
    };
  }
};
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.admin-sidebar {
  width: 250px;
  background-color: #001529;
  color: white;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #00284d;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 18px;
  text-align: center;
}

.sidebar-nav {
  flex: 1;
  padding: 20px 0;
}

.nav-item {
  display: block;
  padding: 12px 20px;
  color: #a6b0bc;
  text-decoration: none;
  transition: all 0.3s;
  border-left: 3px solid transparent;
  position: relative;
}

.nav-item:hover {
  color: white;
  background-color: #0c2033;
}

.nav-item.router-link-active {
  color: white;
  background-color: #0c2033;
  border-left-color: #1890ff;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid #00284d;
}

.logout-btn {
  width: 100%;
  padding: 10px;
  background-color: #ff4d4f;
  border: none;
  color: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.logout-btn:hover {
  background-color: #ff7875;
}

.admin-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.admin-header {
  height: 64px;
  background-color: white;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
}

.header-user {
  display: flex;
  align-items: center;
}

.admin-main {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.badge {
  position: absolute;
  top: 12px;
  right: 20px;
  background-color: #ff4d4f;
  color: white;
  font-size: 12px;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
}
</style> 