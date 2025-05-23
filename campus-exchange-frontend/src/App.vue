<script setup>
import { ref, computed, onMounted, onUnmounted, watch, onErrorCaptured } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import FloatingHeader from './components/FloatingHeader.vue'

const router = useRouter()
const route = useRoute()
const currentYear = new Date().getFullYear()
const user = ref(null)
const hasError = ref(false)
const errorMsg = ref('')
// 用户是否处于卖家模式
const isSeller = ref(false)

// 计算属性：当前是否在登录/注册页面
const isAuthPage = computed(() => {
  return route.path === '/login' || route.path === '/register'
})

// 计算属性：是否在管理员页面
const isAdminPage = computed(() => {
  return route.path.startsWith('/admin')
})

// 计算属性：用户是否已登录
const isLoggedIn = computed(() => {
  const loggedIn = user.value !== null && user.value !== undefined;
  console.log('用户登录状态:', loggedIn, user.value);
  return loggedIn;
})

// 计算属性：用户是否是卖家
const isMerchant = computed(() => {
  return isLoggedIn.value && user.value && user.value.role === 'MERCHANT';
})

// 错误捕获
onErrorCaptured((err, instance, info) => {
  console.error('组件错误被捕获:', err);
  console.error('错误信息:', info);
  hasError.value = true;
  errorMsg.value = err.message || '页面渲染出错';
  
  // 如果是路由组件错误，尝试重新加载或返回首页
  if (window.__ERROR_COUNT === undefined) {
    window.__ERROR_COUNT = 1;
  } else {
    window.__ERROR_COUNT++;
  }
  
  if (window.__ERROR_COUNT > 2 && route.path !== '/') {
    console.log('检测到多次错误，尝试返回首页');
    window.__ERROR_COUNT = 0;
    setTimeout(() => {
      router.push('/');
    }, 100);
  }
  
  // 返回false允许错误继续传播到全局处理器
  return false;
})

// 尝试恢复页面
const handleRetry = () => {
  hasError.value = false;
  errorMsg.value = '';
  if (route.path === '/') {
    window.location.reload();
  } else {
    router.go(0); // 刷新当前页面
  }
}

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
        
        // 商家角色默认设置卖家模式状态
        if (userData.role === 'MERCHANT') {
          // 从localStorage中读取用户设置的模式，默认为卖家模式
          const sellerMode = localStorage.getItem('sellerMode');
          isSeller.value = sellerMode === null ? true : sellerMode === 'true';
          console.log('商家用户模式:', isSeller.value ? '卖家模式' : '买家模式');
        } else {
          isSeller.value = false;
        }
      } else {
        console.error('用户数据无效，缺少必要字段');
        localStorage.removeItem('user');
        user.value = null;
        isSeller.value = false;
      }
    } catch (e) {
      console.error('解析用户数据失败:', e);
      localStorage.removeItem('user');
      user.value = null;
      isSeller.value = false;
    }
  } else {
    console.log('本地存储中没有找到用户数据');
    user.value = null;
    isSeller.value = false;
  }

  // 打印最终结果，确认用户状态
  console.log('最终用户状态:', user.value ? `已登录: ${user.value.username}` : '未登录');
}

// 处理退出登录
const handleLogout = () => {
  // 清除localStorage
  localStorage.removeItem('user');
  localStorage.removeItem('sellerMode');
  // 重置用户状态
  user.value = null;
  isSeller.value = false;
  console.log('用户已退出登录');
  
  // 触发storage事件，确保所有组件感知到登出状态
  setTimeout(() => {
    window.dispatchEvent(new Event('storage'));
    console.log('已触发storage事件，通知组件用户已登出');
    // 导航到登录页
    router.push('/login');
  }, 50);
}

// 切换卖家/买家模式
const toggleSellerMode = () => {
  if (!isMerchant.value) return;
  
  isSeller.value = !isSeller.value;
  console.log('切换模式:', isSeller.value ? '卖家模式' : '买家模式');
  
  // 保存用户选择
  localStorage.setItem('sellerMode', isSeller.value.toString());
  
  // 切换路由到对应入口页面
  if (isSeller.value) {
    // 切换到卖家中心
    router.push('/merchant');
  } else {
    // 切换到买家首页
    router.push('/');
  }
  
  // 触发事件通知组件模式变化
  window.dispatchEvent(new CustomEvent('mode-change', { detail: isSeller.value }));
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
  
  // 监听模式变化事件
  window.addEventListener('mode-change', (e) => {
    isSeller.value = e.detail;
  });
})

// 组件卸载时移除监听器
onUnmounted(() => {
  window.removeEventListener('storage', handleStorageChange);
  window.removeEventListener('user-login', getUserFromStorage);
  window.removeEventListener('mode-change', null);
})
</script>

<template>
  <!-- 黑色顶部导航栏 - 在管理员页面不显示 -->
  <nav v-if="!isAdminPage" class="app-topnav">
    <div class="container">
      <div class="left">
        <!-- 导航链接 - 左侧区域 -->
        <!-- 根据模式显示不同的"首页"链接 -->
        <template v-if="isLoggedIn && isMerchant && isSeller">
          <!-- 卖家模式下，首页链接直接指向卖家中心 -->
          <router-link to="/merchant" class="primary-link">首页</router-link>
        </template>
        <template v-else>
          <!-- 买家模式下的普通首页 -->
          <router-link to="/" class="primary-link">首页</router-link>
        </template>
        
        <!-- 用户登录且是商家+卖家模式 -->
        <template v-if="isLoggedIn && isMerchant && isSeller">
          <router-link to="/orders/merchant" class="primary-link">卖家订单</router-link>
          <router-link to="/product/publish" class="primary-link">发布商品</router-link>
        </template>
        
        <!-- 用户登录且是买家模式 -->
        <template v-if="isLoggedIn && (!isMerchant || !isSeller)">
          <router-link to="/orders/user" class="primary-link">我的订单</router-link>
        </template>
      </div>
      
      <ul>
        <template v-if="isLoggedIn">
          <li><router-link to="/user/profile">{{ user.username }}</router-link></li>
          <!-- 商家显示模式切换选项 -->
          <li v-if="isMerchant">
            <a href="javascript:;" @click.prevent="toggleSellerMode">
              切换到{{ isSeller ? '买家' : '卖家' }}模式
            </a>
          </li>
          <!-- 会员中心/商家信息 -->
          <li>
            <router-link :to="isMerchant && isSeller ? '/merchant/profile' : '/wallet'">
              {{ isMerchant && isSeller ? '商家信息' : '会员中心' }}
            </router-link>
          </li>
          <li><a href="javascript:;" @click.prevent="handleLogout">退出登录</a></li>
        </template>
        <template v-else>
          <li><router-link to="/login">请先登录</router-link></li>
          <li><router-link to="/register">免费注册</router-link></li>
        </template>
      </ul>
    </div>
  </nav>
  
  <!-- 浮动顶部导航栏，只在首页显示且不在管理员页面 -->
  <FloatingHeader v-if="(route.path === '/' || route.path === '/home' || route.path === '/buyer') && !isAdminPage" />

  <!-- 内容区域 -->
  <main class="content">
    <!-- 错误处理显示 -->
    <div v-if="hasError" class="error-container">
      <div class="error-box">
        <div class="error-icon">⚠️</div>
        <h3>页面加载错误</h3>
        <p>{{ errorMsg || '加载内容时发生错误' }}</p>
        <div class="error-actions">
          <button @click="handleRetry" class="retry-btn">重试</button>
          <button @click="router.push('/')" class="home-btn">返回首页</button>
        </div>
      </div>
    </div>
    
    <!-- 正常内容 -->
    <router-view v-else />
  </main>

  <!-- 页脚 - 在管理员页面不显示 -->
  <footer v-if="!isAdminPage" class="footer">
    <div class="container">
      <p>© {{ currentYear }} 校园二手交易平台. 保留所有权利.</p>
    </div>
  </footer>
</template>

<style>
/* 全局重置 - 添加在样式最上方 */
:root {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body, html {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  overflow-x: hidden;
}

/* 黑色顶部导航栏样式 */
.app-topnav {
  background: #333;
  color: #cdcdcd;
  margin: 0;
  padding: 0;
  width: 100%;
}

.app-topnav .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 53px;
  padding: 0;
  margin: 0 auto;
  width: 100%;
  max-width: 100%;
}

.app-topnav .left {
  display: flex;
  align-items: center;
  padding-left: 40px;
}

.app-topnav .left a {
  color: #cdcdcd;
  text-decoration: none;
  padding: 0 20px;
}

.app-topnav .left a:first-child {
  padding-left: 0;
}

.app-topnav .left a:hover {
  color: #4a6ee0;
}

.app-topnav .primary-link {
  border-left: 2px solid #666;
}

.app-topnav .primary-link:first-child {
  border-left: none;
}

.app-topnav ul {
  display: flex;
  height: 53px;
  justify-content: flex-end;
  align-items: center;
  list-style: none;
  margin: 0;
  padding: 0 40px 0 0;
}

.app-topnav li {
  position: relative;
}

.app-topnav li a {
  padding: 0 15px;
  color: #cdcdcd;
  line-height: 1;
  display: inline-block;
  text-decoration: none;
}

.app-topnav li a:hover {
  color: #4a6ee0;
}

.app-topnav li ~ li a {
  border-left: 2px solid #666;
}

.container {
  max-width: 100%;
  margin: 0;
  padding: 0;
  width: 100%;
}

/* 内容区域样式 */
.content {
  min-height: calc(100vh - 33px - 80px); /* 减去顶部导航和页脚的高度 */
  padding: 0;
  margin: 0;
  background-color: #f8f9fa;
}

/* 导航栏框线修复 */
.floating-header {
  outline: 1px solid #e4e4e4;
  outline-offset: -1px;
}

/* 错误处理样式 */
.error-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.error-box {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.error-icon {
  font-size: 48px;
  color: #ff0000;
  margin-bottom: 10px;
}

.error-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}

.retry-btn,
.home-btn {
  padding: 10px 20px;
  background-color: #4a6ee0;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retry-btn:hover,
.home-btn:hover {
  background-color: #304b99;
}

/* 页脚样式 */
.footer {
  background-color: #f0f4ff;
  padding: 25px 0;
  text-align: center;
  color: #666;
  font-size: 14px;
  border-top: 1px solid #e0e6f7;
}

@media (max-width: 768px) {
  .app-topnav .container {
    flex-direction: column;
    height: auto;
    padding: 10px 20px;
  }
  
  .app-topnav .left {
    flex-wrap: wrap;
    justify-content: center;
    margin-bottom: 10px;
  }
  
  .app-topnav ul {
    height: auto;
    justify-content: center;
  }
}
</style>
