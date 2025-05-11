import { createApp } from 'vue'
import { createPinia } from 'pinia'
import axios from 'axios'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

// 检查存储的用户数据是否有效
const validateUserData = () => {
  console.log('开始验证用户数据...')
  try {
    const userData = localStorage.getItem('user')
    console.log('从localStorage读取到的用户数据:', userData)
    
    if (userData) {
      // 尝试解析用户数据
      const user = JSON.parse(userData)
      console.log('解析后的用户数据:', user)
      
      // 检查必要的字段 - 只检查用户名，不强制要求token
      if (!user.username) {
        console.warn('用户数据无效(缺少username)，清除localStorage')
        localStorage.removeItem('user')
      } else {
        console.log('用户数据有效，用户名:', user.username)
        
        // 触发一个自定义事件，确保所有组件都能感知到用户已登录状态
        setTimeout(() => {
          window.dispatchEvent(new Event('storage'))
          console.log('已触发storage事件，通知组件用户已登录')
        }, 100) // 设置短延迟，确保Vue组件已完成挂载
      }
    } else {
      console.log('未找到用户数据，用户未登录')
    }
  } catch (e) {
    console.error('用户数据解析失败，清除localStorage:', e)
    localStorage.removeItem('user')
  }
}

// 启动前验证用户数据
validateUserData()

// 配置全局错误拦截
const handleGlobalErrors = (error) => {
  console.error('全局错误:', error)
  return Promise.reject(error)
}

axios.interceptors.response.use(
  response => response,
  error => handleGlobalErrors(error)
)

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus) // 使用ElementPlus

app.mount('#app') 