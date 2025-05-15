<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const currentYear = new Date().getFullYear()
const user = ref(null)

// 计算属性：当前是否在登录/注册页面
const isAuthPage = computed(() => {
  return route.path === '/login' || route.path === '/register'
})

// 计算属性：用户是否已登录
const isLoggedIn = computed(() => {
  const loggedIn = user.value !== null && user.value !== undefined;
  console.log('用户登录状态:', loggedIn, user.value);
  return loggedIn;
})

// 从本地存储获取用户信息
const getUserFromStorage = () => {
  console.log('尝试获取本地存储的用户信息');
  const userJson = localStorage.getItem('user');
  console.log('获取到的用户JSON:', userJson);
  
  if (userJson) {
    try {
      const userData = JSON.parse(userJson);
      console.log('解析后的用户数据:', userData);
      
      // 验证用户数据有效性
      if (userData && userData.userId && userData.username) {
        user.value = userData;
        console.log('成功加载用户数据, user.value:', user.value);
      } else {
        console.error('用户数据无效，缺少必要字段');
        localStorage.removeItem('user');
        user.value = null;
      }
    } catch (e) {
      console.error('解析用户数据失败:', e);
      localStorage.removeItem('user');
      user.value = null;
    }
  } else {
    console.log('本地存储中没有找到用户数据');
    user.value = null;
  }

  // 打印最终结果，确认用户状态
  console.log('最终用户状态:', user.value ? `已登录: ${user.value.username}` : '未登录');
}

// 处理退出登录
const handleLogout = () => {
  // 清除localStorage
  localStorage.removeItem('user');
  // 重置用户状态
  user.value = null;
  console.log('用户已退出登录');
  
  // 触发storage事件，确保所有组件感知到登出状态
  setTimeout(() => {
    window.dispatchEvent(new Event('storage'));
    console.log('已触发storage事件，通知组件用户已登出');
    // 导航到登录页
    router.push('/login');
  }, 50);
}

// 游客模式，跳转到首页
const handleGuestMode = () => {
  router.push('/');
}

// 监听localStorage的变化
const handleStorageChange = (event) => {
  console.log('检测到storage变化:', event);
  getUserFromStorage();
}

// 监听路由变化
watch(() => route.path, (newPath) => {
  console.log('路由变化:', newPath);
  // 当路由改变时，也更新一次用户状态，确保页面显示正确
  getUserFromStorage();
}, { immediate: true });

// 组件挂载时获取用户信息并添加监听器
onMounted(() => {
  getUserFromStorage();
  
  // 监听storage事件，即使是在同一个窗口触发的自定义事件
  window.addEventListener('storage', handleStorageChange);
  
  // 添加一个自定义事件监听器，用于登录成功后更新用户状态
  window.addEventListener('user-login', getUserFromStorage);
})

// 组件卸载时移除监听器
onUnmounted(() => {
  window.removeEventListener('storage', handleStorageChange);
  window.removeEventListener('user-login', getUserFromStorage);
})
</script>

<template>
  <header class="header">
    <div class="container header-container">
      <h1 class="logo">
        <router-link to="/">校园二手交易平台</router-link>
      </h1>
      
      <nav class="nav">
        <router-link :to="isLoggedIn && user.role === 'MERCHANT' ? '/merchant' : '/'" class="nav-link">首页</router-link>
        <router-link v-if="isLoggedIn" :to="user.role === 'MERCHANT' ? '/orders/merchant' : '/orders/user'" class="nav-link">我的订单</router-link>
        <router-link v-if="isLoggedIn && user.role === 'MERCHANT'" to="/product/publish" class="nav-link">发布商品</router-link>
        <router-link v-if="isLoggedIn" to="/cart" class="nav-link">
          <i class="fas fa-shopping-cart"></i> 购物车
        </router-link>
        <router-link to="/about" class="nav-link">关于</router-link>
      </nav>
      
      <div class="auth-links">
        <template v-if="isLoggedIn">
          <div class="user-menu">
            <span class="welcome-text">欢迎，{{ user.username }}</span>
            <div class="role-switcher" v-if="user.role === 'MERCHANT'">
              <router-link to="/" class="role-link" :class="{ 'router-link-active': $route.path === '/' }">买家首页</router-link>
              <router-link to="/merchant" class="role-link" :class="{ 'router-link-active': $route.path === '/merchant' }">商家中心</router-link>
            </div>
            <a href="#" @click.prevent="handleLogout" class="auth-link">退出</a>
          </div>
        </template>
        <template v-else-if="isAuthPage">
          <div class="guest-mode">
            <router-link to="/" class="guest-btn">
              <span class="guest-icon">👋</span>
              <span>游客访问</span>
            </router-link>
          </div>
        </template>
        <template v-else>
          <router-link to="/login" class="auth-link">登录</router-link>
          <router-link to="/register" class="auth-link register-link">注册</router-link>
        </template>
      </div>
    </div>
  </header>

  <main class="main">
    <router-view />
  </main>

  <footer class="footer">
    <div class="container">
      <p>© {{ currentYear }} 校园二手交易平台. 保留所有权利.</p>
    </div>
  </footer>
</template>

<style scoped>
.header {
  background-color: rgba(255, 255, 255, 0.98);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
  transition: all 0.3s ease;
  border-bottom: 2px solid #f0f0f0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  width: 100%;
}

.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
}

.logo a {
  color: #4a6ee0;
  text-decoration: none;
  font-size: 22px;
  font-weight: 700;
  transition: color 0.3s ease;
  letter-spacing: 0.5px;
  display: flex;
  align-items: center;
}

.logo a:before {
  content: "🔄";
  margin-right: 8px;
  font-size: 24px;
}

.logo a:hover {
  color: #304b99;
}

.nav {
  display: flex;
  gap: 24px;
}

.nav-link {
  color: #333;
  text-decoration: none;
  font-weight: 600;
  font-size: 16px;
  padding: 5px 0;
  transition: all 0.3s ease;
  position: relative;
}

.nav-link:after {
  content: "";
  position: absolute;
  width: 0;
  height: 2px;
  bottom: 0;
  left: 0;
  background-color: #4a6ee0;
  transition: width 0.3s ease;
}

.nav-link:hover,
.nav-link.router-link-active {
  color: #4a6ee0;
}

.nav-link:hover:after,
.nav-link.router-link-active:after {
  width: 100%;
}

.auth-links {
  display: flex;
  align-items: center;
  gap: 15px;
}

.welcome-text {
  color: #555;
  font-size: 14px;
  margin-right: 5px;
  font-weight: 500;
}

.auth-link {
  color: #4a6ee0;
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.auth-link:hover {
  color: #304b99;
  transform: translateY(-1px);
}

.register-link {
  padding: 8px 16px;
  background-color: #4a6ee0;
  color: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(74, 110, 224, 0.3);
  transition: all 0.3s ease;
}

.register-link:hover {
  background-color: #304b99;
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(74, 110, 224, 0.4);
}

.main {
  min-height: calc(100vh - 170px);
  width: 100%;
  background-color: #f8f9fa;
}

.footer {
  background-color: #f0f4ff;
  padding: 25px 0;
  text-align: center;
  color: #666;
  font-size: 14px;
  margin-top: 30px;
  border-top: 1px solid #e0e6f7;
}

.guest-mode {
  display: flex;
  align-items: center;
}

.guest-btn {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  background-color: #f0f4ff;
  color: #4a6ee0;
  border-radius: 8px;
  border: 1px solid #e0e6f7;
  transition: all 0.3s ease;
  text-decoration: none;
  font-weight: 500;
  font-size: 14px;
  box-shadow: 0 2px 6px rgba(74, 110, 224, 0.15);
}

.guest-btn:hover {
  background-color: #e6ecff;
  color: #3d5eca;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(74, 110, 224, 0.2);
}

.guest-icon {
  margin-right: 8px;
  font-size: 16px;
}

@media (max-width: 768px) {
  .header-container {
    flex-wrap: wrap;
    height: auto;
    padding: 15px 0;
  }
  
  .nav {
    order: 3;
    width: 100%;
    margin-top: 15px;
    justify-content: center;
  }
  
  .auth-links {
    margin-left: auto;
  }
}

/* 添加用户菜单和角色切换样式 */
.user-menu {
  display: flex;
  align-items: center;
  gap: 15px;
}

.role-switcher {
  display: flex;
  background-color: #f0f2f7;
  border-radius: 6px;
  padding: 2px;
}

.role-link {
  padding: 5px 10px;
  text-decoration: none;
  color: #555;
  font-size: 14px;
  font-weight: 500;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.role-link:hover,
.role-link.router-link-active {
  background-color: #4a6ee0;
  color: #ffffff;
}
</style>
