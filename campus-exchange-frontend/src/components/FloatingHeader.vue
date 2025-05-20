<template>
  <header class="floating-header" :class="{ sticky: isSticky }">
    <div class="header-container">
      <!-- logo -->
      <div class="logo">
        <router-link to="/">
          <img :src="logoUrl" alt="logo" />
        </router-link>
      </div>
      
      <!-- 卖家模式导航 - 只在卖家模式下显示 -->
      <ul v-if="isMerchantMode" class="seller-nav">
        <li :class="{ active: route.path === '/merchant' }">
          <router-link to="/merchant">卖家中心</router-link>
        </li>
        <li :class="{ active: route.path === '/product/publish' }">
          <router-link to="/product/publish">发布商品</router-link>
        </li>
        <li :class="{ active: route.path.includes('/orders/merchant') }">
          <router-link to="/orders/merchant">订单管理</router-link>
        </li>
        <li :class="{ active: route.path === '/merchant/profile' }">
          <router-link to="/merchant/profile">店铺设置</router-link>
        </li>
        <li @click="switchToBuyerMode">
          <a href="javascript:;">切换到买家</a>
        </li>
      </ul>
      
      <!-- 分类导航 - 只在买家模式下显示 -->
      <ul v-else class="category-nav">
        <li :class="{ active: selectedCategory === null }" @click="selectCategory(null)">推荐</li>
        <li v-for="cat in categories" :key="cat.id" :class="{ active: selectedCategory === cat.id }" @click="selectCategory(cat.id)">
          {{ cat.name }}
        </li>
      </ul>
      
      <!-- 搜索框 - 买家模式显示 -->
      <div v-if="!isMerchantMode" class="search-box">
        <!-- 显示搜索图标 - 直接使用iconfont -->
        <i class="iconfont icon-search"></i>
        <input type="text" v-model="searchKeyword" placeholder="搜一搜" @keyup.enter="handleSearch" />
      </div>
      
      <!-- 购物车入口 - 买家模式显示 -->
      <div v-if="!isMerchantMode" class="cart-wrapper">
        <div class="cart-entry" @mouseenter="showCart = true">
          <!-- 显示购物车图标 - 直接使用iconfont -->
          <i class="iconfont icon-cart"></i>
          <em v-if="cartItems.length > 0">{{ cartItems.length }}</em>
        </div>
        <!-- 购物车下拉 -->
        <div class="cart-dropdown" v-if="showCart" @mouseenter="showCart = true" @mouseleave="showCart = false">
          <div class="cart-list">
            <div class="cart-item" v-for="item in cartItems" :key="item.id">
              <img :src="item.productImage" alt="商品图片" />
              <div class="cart-item-center">
                <p class="cart-item-name ellipsis-2">{{ item.productName }}</p>
                <p class="cart-item-desc ellipsis">{{ item.productDescription }}</p>
              </div>
              <div class="cart-item-right">
                <p class="cart-item-price">¥{{ item.price }}</p>
                <p class="cart-item-count">x{{ item.quantity }}</p>
              </div>
              <i class="iconfont icon-close-new cart-item-delete" @click.stop="removeCartItem(item)"></i>
            </div>
            <div v-if="cartItems.length === 0" class="cart-empty">购物车空空如也</div>
          </div>
          <div class="cart-foot">
            <div class="cart-total">
              <p>共 {{ cartItems.length }} 件商品</p>
              <p>¥ {{ totalPrice }}</p>
            </div>
            <button class="cart-checkout" @click="goToCart" :disabled="cartItems.length === 0">去购物车结算</button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { productApi, cartApi } from '@/api/all'
import logoUrl from '../assets/images/logo.png'

const router = useRouter()
const route = useRoute()
const categories = ref([])
const selectedCategory = ref(null) // null为推荐
const searchKeyword = ref('')
const cartItems = ref([])
const showCart = ref(false)
const isSticky = ref(false)

// 判断当前是否在首页
const isHome = computed(() => route.path === '/')

// 判断用户是否处于卖家模式
const isMerchantMode = computed(() => {
  // 从localStorage获取用户数据
  const userJson = localStorage.getItem('user')
  if (!userJson) return false
  
  try {
    const userData = JSON.parse(userJson)
    // 只有商家角色且处于卖家模式才显示卖家导航
    if (userData.role !== 'MERCHANT') return false
    
    // 检查用户是否激活了卖家模式
    const sellerMode = localStorage.getItem('sellerMode') === 'true'
    return sellerMode
  } catch (e) {
    console.error('解析用户数据出错:', e)
    return false
  }
})

// 获取分类
const fetchCategories = async () => {
  try {
    console.log('获取商品分类...')
    const res = await productApi.getTopCategories()
    if (res.data && res.data.code === 200) {
      console.log('获取到分类数据:', res.data.data)
      categories.value = res.data.data || []
    } else {
      // 如果接口不可用，使用模拟数据
      console.log('使用模拟分类数据')
      categories.value = [
        { id: 1, name: '电子产品' },
        { id: 2, name: '图书' },
        { id: 3, name: '数码' },
        { id: 4, name: '服装' },
        { id: 5, name: '个护' },
        { id: 6, name: '艺术' },
        { id: 7, name: '生活' },
        { id: 8, name: '运动' },
        { id: 9, name: '其他' }
      ]
    }
  } catch (e) {
    console.error('获取分类出错:', e)
    // 使用模拟数据作为备选
    categories.value = [
      { id: 1, name: '电子产品' },
      { id: 2, name: '图书' },
      { id: 3, name: '数码' },
      { id: 4, name: '服装' },
      { id: 5, name: '个护' },
      { id: 6, name: '艺术' },
      { id: 7, name: '生活' },
      { id: 8, name: '运动' },
      { id: 9, name: '其他' }
    ]
  }
}

// 切换到买家模式
const switchToBuyerMode = () => {
  localStorage.setItem('sellerMode', 'false')
  // 触发模式变更事件
  window.dispatchEvent(new CustomEvent('mode-change', { detail: false }))
  // 跳转到首页
  router.push('/')
}

// 购物车
const fetchCart = async () => {
  try {
    console.log('获取购物车数据...')
    const res = await cartApi.getCartItems()
    if (res.data && res.data.code === 200) {
      console.log('获取到购物车数据:', res.data.data)
      cartItems.value = res.data.data || []
    } else {
      cartItems.value = []
    }
  } catch (e) {
    console.error('获取购物车出错:', e)
    cartItems.value = []
  }
}

const removeCartItem = async (item) => {
  try {
    await cartApi.removeItem(item.id)
    fetchCart()
  } catch (e) {
    console.error('删除购物车商品出错:', e)
  }
}

const totalPrice = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + (item.price * item.quantity), 0).toFixed(2)
})

const selectCategory = (catId) => {
  selectedCategory.value = catId
  // 通知首页刷新商品
  window.dispatchEvent(new CustomEvent('category-change', { detail: catId }))
}

const handleSearch = () => {
  // 检查是否有搜索关键词
  if (!searchKeyword.value.trim()) {
    return
  }
  
  // 触发搜索事件
  console.log('触发搜索:', searchKeyword.value)
  window.dispatchEvent(new CustomEvent('search-keyword', { detail: searchKeyword.value.trim() }))
  
  // 如果不在首页，则导航到首页并传递搜索词
  if (route.path !== '/') {
    router.push('/')
  }
}

const goToCart = () => {
  router.push('/cart')
  showCart.value = false
}

// 组件挂载
onMounted(() => {
  console.log('FloatingHeader组件挂载')
  
  // 获取数据
  fetchCategories()
  
  // 获取购物车数据
  const userJson = localStorage.getItem('user')
  if (userJson) {
    try {
      JSON.parse(userJson)
      fetchCart()
    } catch (e) {
      console.error('解析用户数据出错:', e)
    }
  }
  
  // 添加滚动监听
  const handleScroll = () => {
    isSticky.value = window.scrollY > 100 // 滚动超过100px时激活吸顶
    console.log('滚动位置:', window.scrollY, '吸顶状态:', isSticky.value)
  }
  
  // 添加事件监听
  window.addEventListener('scroll', handleScroll)
  
  // 登录状态变化时刷新购物车
  window.addEventListener('storage', fetchCart)
  
  // 监听模式切换事件
  window.addEventListener('mode-change', (e) => {
    console.log('FloatingHeader检测到模式切换:', e.detail ? '卖家模式' : '买家模式')
    // 如果切换到买家模式，重新获取分类和购物车
    if (!e.detail) {
      fetchCategories()
      fetchCart()
    }
  })
  
  // 组件卸载时清理监听器
  return () => {
    window.removeEventListener('scroll', handleScroll)
    window.removeEventListener('storage', fetchCart)
    window.removeEventListener('mode-change', null)
  }
})
</script>

<style scoped>
.floating-header {
  width: 100%;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  z-index: 999;
  position: relative;
  transition: all 0.3s;
  margin: 0;
  padding: 0;
  border: none;
}

.floating-header.sticky {
  position: fixed;
  top: 0;
  left: 0;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  animation: slideDown 0.3s;
}

@keyframes slideDown {
  from {
    transform: translateY(-100%);
  }
  to {
    transform: translateY(0);
  }
}

.header-container {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 20px;
  height: 80px;
  box-sizing: border-box;
  background-color: #fff;
}

.logo {
  margin-right: 15px;
}

.logo img {
  height: 56px;
  width: auto;
  display: block;
}

/* 买家模式下的分类导航 */
.category-nav {
  display: flex;
  flex: 1;
  gap: 15px;
  list-style: none;
  padding: 0;
  margin: 0;
}
.category-nav li {
  font-size: 16px;
  color: #333;
  padding: 0 15px;
  line-height: 40px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  list-style: none;
  background: none;
  border: none;
  white-space: nowrap;
}
.category-nav li.active, .category-nav li:hover {
  background: #e6ecff;
  color: #4a6ee0;
}

/* 卖家模式下的导航 */
.seller-nav {
  display: flex;
  flex: 1;
  gap: 15px;
  list-style: none;
  padding: 0;
  margin: 0;
}
.seller-nav li {
  font-size: 16px;
  color: #333;
  padding: 0 15px;
  line-height: 40px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  list-style: none;
  background: none;
  border: none;
  white-space: nowrap;
}
.seller-nav li.active, .seller-nav li:hover {
  background: #ffe6e6;
  color: #e63946;
}
.seller-nav li a {
  color: inherit;
  text-decoration: none;
  display: block;
  width: 100%;
  height: 100%;
}

.search-box {
  display: flex;
  align-items: center;
  margin-left: 20px;
  background: #f8f9fa;
  border-radius: 6px;
  padding: 0 12px;
  height: 40px;
  border: 1px solid #e0e6f7;
  width: 180px;
}
.search-box .iconfont {
  font-size: 18px;
  color: #888;
  margin-right: 6px;
}
.search-box input {
  border: none;
  background: transparent;
  outline: none;
  font-size: 15px;
  width: 100%;
  color: #333;
}

.cart-wrapper {
  position: relative;
  margin-left: 20px;
}

.cart-entry {
  cursor: pointer;
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cart-entry .iconfont {
  font-size: 26px;
  color: #333;
}

/* 购物车数量标记 */
.cart-entry em {
  position: absolute;
  top: -6px;
  right: -10px;
  background: #e26237;
  color: #fff;
  font-size: 12px;
  border-radius: 10px;
  padding: 1px 6px;
  font-style: normal;
}

/* 购物车下拉菜单 */
.cart-dropdown {
  position: absolute;
  right: -15px;
  top: 40px;
  width: 400px;
  background: #fff;
  box-shadow: 0 0 10px rgba(0,0,0,0.2);
  border-radius: 4px;
  z-index: 1000;
  padding-top: 10px;
  transform-origin: top right;
  animation: dropdownFade 0.3s ease;
}

@keyframes dropdownFade {
  from {
    opacity: 0;
    transform: translateY(-10px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.cart-list {
  max-height: 310px;
  overflow: auto;
  padding: 0 10px;
}
.cart-item {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #f5f5f5;
  padding: 10px 0;
  position: relative;
}
.cart-item img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  background: #f8f8f8;
}
.cart-item-center {
  flex: 1;
  padding: 0 10px;
}
.cart-item-name {
  font-size: 15px;
  color: #333;
  margin-bottom: 4px;
}
.cart-item-desc {
  color: #999;
  font-size: 13px;
}
.cart-item-right {
  width: 80px;
  text-align: right;
}
.cart-item-price {
  font-size: 15px;
  color: #cf4444;
}
.cart-item-count {
  color: #999;
  font-size: 14px;
}
.cart-item-delete {
  position: absolute;
  right: 0;
  top: 18px;
  color: #666;
  font-size: 18px;
  opacity: 0;
  transition: opacity 0.2s;
}
.cart-item:hover .cart-item-delete {
  opacity: 1;
}
.cart-empty {
  text-align: center;
  color: #aaa;
  padding: 40px 0;
}
.cart-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8f8f8;
  padding: 10px 20px;
  border-radius: 0 0 4px 4px;
  height: 70px;
}
.cart-total p {
  margin: 0;
  color: #999;
}
.cart-total p:last-child {
  font-size: 18px;
  color: #cf4444;
}
.cart-checkout {
  background: #27ba9b;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 10px 28px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}
.cart-checkout:disabled {
  background: #eee;
  color: #aaa;
  cursor: not-allowed;
}

/* 文本溢出省略号显示 */
.ellipsis {
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}

.ellipsis-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  text-overflow: ellipsis;
  overflow: hidden;
}

/* 适配移动端 */
@media (max-width: 768px) {
  .header-container {
    height: auto;
    flex-wrap: wrap;
    padding: 10px;
  }
  .category-nav, .seller-nav {
    order: 3;
    width: 100%;
    overflow-x: auto;
    padding: 10px 0;
  }
  .search-box {
    margin-left: auto;
    width: 120px;
  }
  .logo img {
    height: 40px;
  }
  .cart-dropdown {
    width: 300px;
    right: -80px;
  }
}
</style> 