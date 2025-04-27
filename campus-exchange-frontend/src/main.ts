import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import axios from 'axios'

import App from './App.vue'
import router from './router'

// 配置全局错误拦截
const handleGlobalErrors = (error: any) => {
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

app.mount('#app')
