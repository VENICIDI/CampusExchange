<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

interface User {
  userId?: number;
  username: string;
  role: string;
  avatar?: string;
}

const router = useRouter()
const currentYear = new Date().getFullYear()
const user = ref<User | null>(null)

// 计算属性：用户是否已登录
const isLoggedIn = computed(() => {
  return user.value !== null
})

// 从本地存储获取用户信息
const getUserFromStorage = () => {
  const userJson = localStorage.getItem('user')
  if (userJson) {
    try {
      const userData = JSON.parse(userJson)
      user.value = userData
    } catch (e) {
      console.error('解析用户数据失败:', e)
      localStorage.removeItem('user')
    }
  }
}

// 处理退出登录
const handleLogout = () => {
  localStorage.removeItem('user')
  user.value = null
  router.push('/login')
}

// 组件挂载时获取用户信息
onMounted(() => {
  getUserFromStorage()
})
</script>

<template>
  <header class="header">
    <div class="container header-container">
      <h1 class="logo">
        <router-link to="/">校园二手交易平台</router-link>
      </h1>
      
      <nav class="nav">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/about" class="nav-link">关于</router-link>
      </nav>
      
      <div class="auth-links">
        <template v-if="isLoggedIn">
          <span class="welcome-text">欢迎，{{ user?.username }}</span>
          <a href="#" @click.prevent="handleLogout" class="auth-link">退出</a>
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
  background-color: #ffffff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
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
  font-size: 20px;
  font-weight: 700;
}

.nav {
  display: flex;
  gap: 20px;
}

.nav-link {
  color: #333;
  text-decoration: none;
  font-weight: 500;
  font-size: 16px;
  padding: 5px 0;
  transition: color 0.3s;
}

.nav-link:hover,
.nav-link.router-link-active {
  color: #4a6ee0;
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
}

.auth-link {
  color: #4a6ee0;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.3s;
}

.auth-link:hover {
  color: #3a5cc5;
}

.register-link {
  padding: 8px 16px;
  background-color: #4a6ee0;
  color: #fff;
  border-radius: 4px;
}

.register-link:hover {
  background-color: #3a5cc5;
  color: #fff;
}

.main {
  min-height: calc(100vh - 170px);
  width: 100%;
}

.footer {
  background-color: #f5f5f5;
  padding: 20px 0;
  text-align: center;
  color: #666;
  font-size: 14px;
  margin-top: 30px;
}
</style>
