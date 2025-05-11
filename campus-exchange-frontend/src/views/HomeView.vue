<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { productApi } from '@/api/all'
import ProductCard from '@/components/product/ProductCard.vue'

// 判断用户是否登录
const isLoggedIn = computed(() => {
  const userJson = localStorage.getItem('user')
  return userJson ? true : false
})

// 判断用户是否是商家
const isMerchant = computed(() => {
  const userJson = localStorage.getItem('user')
  if (!userJson) return false
  try {
    const userData = JSON.parse(userJson)
    return userData.role === 'MERCHANT'
  } catch (e) {
    return false
  }
})

// 分类数据
const categories = ref([])
// 商品数据
const products = ref([])
// 加载状态
const isLoading = ref({
  categories: false,
  products: false
})
// 搜索相关
const searchKeyword = ref('')
const selectedCategory = ref(0)

const router = useRouter()

// 处理商品点击
const handleProductClick = (productId) => {
  // 路由到商品详情页
  router.push(`/product/${productId}`)
}

// 处理分类点击
const handleCategoryClick = (categoryId) => {
  selectedCategory.value = categoryId
  // 添加筛选逻辑
  fetchProducts({ categoryId: categoryId !== 0 ? categoryId : null })
}

// 处理搜索
const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    alert('请输入搜索关键词')
    return
  }

  // 执行搜索
  router.push(`/search?keyword=${encodeURIComponent(searchKeyword.value)}`)
}

// 获取分类数据
const fetchCategories = async () => {
  isLoading.value.categories = true
  try {
    const response = await productApi.getTopCategories()
    if (response.data.code === 200) {
      categories.value = response.data.data
    } else {
      console.error('获取分类失败:', response.data.message)
      // 使用模拟数据作为后备
      initializeMockCategories()
    }
  } catch (error) {
    console.error('获取分类出错:', error)
    // 使用模拟数据作为后备
    initializeMockCategories()
  } finally {
    isLoading.value.categories = false
  }
}

// 获取商品数据
const fetchProducts = async (params = {}) => {
  isLoading.value.products = true;
  try {
    console.log('开始获取商品列表数据...');
    const response = await productApi.getProducts({
      pageNum: 1,
      pageSize: 8,
      ...params
    });
    
    console.log('商品列表API响应:', response);
    
    if (response.data && response.data.code === 200) {
      // 检查数据结构，适应不同的返回格式
      if (response.data.data && Array.isArray(response.data.data.records)) {
        products.value = response.data.data.records;
      } else if (Array.isArray(response.data.data)) {
        products.value = response.data.data;
      } else {
        console.warn('无法解析商品数据格式，使用模拟数据');
        initializeMockProducts();
      }
    } else {
      console.error('获取商品列表失败:', response.data?.message || '未知错误');
      // 使用模拟数据作为后备
      initializeMockProducts();
    }
  } catch (error) {
    console.error('获取商品出错:', error);
    // 使用模拟数据作为后备
    initializeMockProducts();
  } finally {
    isLoading.value.products = false;
    console.log('最终加载的商品数据:', products.value);
  }
}

// 初始化模拟分类数据
const initializeMockCategories = () => {
  categories.value = [
    { id: 1, name: '电子产品', icon: '📱' },
    { id: 2, name: '图书教材', icon: '📚' },
    { id: 3, name: '生活用品', icon: '🏠' },
    { id: 4, name: '服装鞋帽', icon: '👕' },
    { id: 5, name: '运动器材', icon: '🏀' },
    { id: 6, name: '交通工具', icon: '🚲' },
    { id: 7, name: '美妆护肤', icon: '💄' },
    { id: 8, name: '其他物品', icon: '🎁' }
  ]
}

// 初始化模拟商品数据
const initializeMockProducts = () => {
  products.value = [
    {
      id: 1,
      name: '二手 MacBook Pro 2019',
      currentPrice: 4999,
      originalPrice: 6999,
      negotiable: true,
      coverImage: 'https://via.placeholder.com/200x150',
      merchantId: 1,
      storeName: 'user123的数码铺',
      location: '东校区',
      createTime: new Date(Date.now() - 2 * 60 * 60 * 1000), // 2小时前
      productCondition: 'LIKE_NEW'
    },
    {
      id: 2,
      name: '高等数学第七版教材',
      currentPrice: 25,
      originalPrice: 45,
      negotiable: false,
      coverImage: 'https://via.placeholder.com/200x150',
      merchantId: 2,
      storeName: 'math_lover书社',
      location: '西校区',
      createTime: new Date(Date.now() - 5 * 60 * 60 * 1000), // 5小时前
      productCondition: 'NEW'
    },
    {
      id: 3,
      name: '全新篮球',
      currentPrice: 85,
      originalPrice: 120,
      negotiable: true,
      coverImage: 'https://via.placeholder.com/200x150',
      merchantId: 3,
      storeName: 'sports2023运动用品',
      location: '南校区',
      createTime: new Date(Date.now() - 24 * 60 * 60 * 1000), // 1天前
      productCondition: 'NEW'
    },
    {
      id: 4,
      name: '宿舍书桌',
      currentPrice: 120,
      originalPrice: null,
      negotiable: true,
      coverImage: 'https://via.placeholder.com/200x150',
      merchantId: 4,
      storeName: 'dorm_furniture家具店',
      location: '北校区',
      createTime: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000), // 2天前
      productCondition: 'LIKE_NEW'
    },
    {
      id: 5,
      name: '自行车',
      currentPrice: 350,
      originalPrice: 580,
      negotiable: false,
      coverImage: 'https://via.placeholder.com/200x150',
      merchantId: 5,
      storeName: 'bike_seller车行',
      location: '中心校区',
      createTime: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000), // 3天前
      productCondition: 'GOOD'
    },
    {
      id: 6,
      name: 'JBL 蓝牙音箱',
      currentPrice: 199,
      originalPrice: 299,
      negotiable: true,
      coverImage: 'https://via.placeholder.com/200x150',
      merchantId: 6,
      storeName: 'music_fan音箱专卖',
      location: '东校区',
      createTime: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000), // 3天前
      productCondition: 'GOOD'
    },
    {
      id: 7,
      name: '计算机网络教材',
      currentPrice: 30,
      originalPrice: 68,
      negotiable: false,
      coverImage: 'https://via.placeholder.com/200x150',
      merchantId: 7,
      storeName: 'cs_student的书摊',
      location: '西校区',
      createTime: new Date(Date.now() - 4 * 24 * 60 * 60 * 1000), // 4天前
      productCondition: 'FAIR'
    },
    {
      id: 8,
      name: '小米手环6',
      currentPrice: 149,
      originalPrice: 229,
      negotiable: true,
      coverImage: 'https://via.placeholder.com/200x150',
      merchantId: 8,
      storeName: 'tech_lover数码店',
      location: '南校区',
      createTime: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000), // 5天前
      productCondition: 'POOR'
    }
  ]
}

// 格式化时间
const formatTime = (dateTime) => {
  if (!dateTime) return ''
  
  const now = new Date()
  const date = new Date(dateTime)
  const diff = now - date
  
  // 小于1小时显示xx分钟前
  if (diff < 60 * 60 * 1000) {
    return Math.floor(diff / (60 * 1000)) + '分钟前'
  }
  // 小于24小时显示xx小时前
  else if (diff < 24 * 60 * 60 * 1000) {
    return Math.floor(diff / (60 * 60 * 1000)) + '小时前'
  }
  // 小于30天显示xx天前
  else if (diff < 30 * 24 * 60 * 60 * 1000) {
    return Math.floor(diff / (24 * 60 * 60 * 1000)) + '天前'
  }
  // 其他情况显示年-月-日
  else {
    return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
  }
}

// 格式化商品新旧程度
const formatCondition = (condition) => {
  if (!condition) return '未知新旧';
  const conditionMap = {
    'NEW': '全新',
    'LIKE_NEW': '九成新',
    'GOOD': '八成新',
    'FAIR': '七成新',
    'POOR': '六成新及以下'
  };
  return conditionMap[condition] || condition;
};

// 处理成为商家请求
const becomeMerchant = () => {
  alert('商家申请功能正在开发中，请联系管理员申请成为商家')
  // TODO: 实现商家申请功能
}

// 跳转到商家主页
const navigateToStore = (merchantId) => {
  router.push(`/store/${merchantId}`);
};

// 页面加载完成后获取数据
onMounted(() => {
  fetchCategories()
  fetchProducts()
})
</script>

<template>
  <div class="home-container">
    <!-- 顶部横幅 -->
    <div class="banner">
      <div class="banner-content">
        <h2>校园二手交易，便捷可靠</h2>
        <p>让闲置物品流通起来，让校园生活更美好</p>

        <!-- 搜索框 -->
        <div class="search-box">
          <input
            type="text"
            v-model="searchKeyword"
            placeholder="搜索你想要的商品..."
            @keyup.enter="handleSearch"
          />
          <button @click="handleSearch" class="search-btn">搜索</button>
        </div>
      </div>
    </div>
    
    <!-- 买家提示信息 -->
    <div class="buyer-notice" v-if="isLoggedIn">
      <div class="notice-content">
        <div class="notice-icon">💡</div>
        <div class="notice-text">
          <h4>买家提示</h4>
          <p>浏览商品，联系卖家，便捷交易。如需发布商品，请先切换到商家身份。</p>
        </div>
      </div>
    </div>

    <!-- 分类导航 -->
    <div class="category-nav">
      <div class="container">
        <h3>商品分类</h3>
        <div v-if="isLoading.categories" class="loading">正在加载分类...</div>
        <div v-else class="category-list">
          <div
            class="category-item"
            :class="{ active: selectedCategory === 0 }"
            @click="selectedCategory = 0; fetchProducts()"
          >
            <span class="category-icon">🏷️</span>
            <span>全部</span>
          </div>

          <div
            v-for="category in categories"
            :key="category.id"
            class="category-item"
            :class="{ active: selectedCategory === category.id }"
            @click="handleCategoryClick(category.id)"
          >
            <span class="category-icon">{{ category.icon || '📦' }}</span>
            <span>{{ category.name }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品展示区 -->
    <div class="product-section">
      <div class="container">
        <h2>最新商品</h2>
        <div v-if="isLoading.products" class="loading-products">
          <div class="spinner"></div>
          <p>正在加载商品...</p>
        </div>
        <div v-else-if="products.length === 0" class="empty-products">
          <div class="empty-icon">📦</div>
          <p>暂无商品</p>
        </div>
        <div v-else class="product-grid">
          <ProductCard 
            v-for="product in products" 
            :key="product.id" 
            :product="product"
            @click="handleProductClick(product.id)"
          />
        </div>
        
        <!-- 分页器 -->
        <div class="pagination" v-if="totalPages > 1">
          <button 
            :disabled="currentPage === 1" 
            @click="currentPage > 1 && fetchProducts({pageNum: currentPage - 1})"
            class="pagination-btn"
          >
            上一页
          </button>
          <span class="pagination-info">{{ currentPage }} / {{ totalPages }}</span>
          <button 
            :disabled="currentPage === totalPages" 
            @click="currentPage < totalPages && fetchProducts({pageNum: currentPage + 1})"
            class="pagination-btn"
          >
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- 平台特点介绍 -->
    <div class="features-section">
      <div class="container">
        <h3>为什么选择校园二手交易平台？</h3>

        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">👨‍🎓</div>
            <h4>校园专属</h4>
            <p>只为校园师生服务，安全可靠的交易环境</p>
          </div>

          <div class="feature-card">
            <div class="feature-icon">💰</div>
            <h4>物美价廉</h4>
            <p>二手价格，一手品质，为你的校园生活省钱</p>
          </div>

          <div class="feature-card">
            <div class="feature-icon">♻️</div>
            <h4>环保循环</h4>
            <p>让物品循环利用，践行可持续发展理念</p>
          </div>

          <div class="feature-card">
            <div class="feature-icon">🤝</div>
            <h4>便捷交易</h4>
            <p>校内面对面交易，安全便捷无忧</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  width: 100%;
}

/* 顶部横幅 */
.banner {
  background: linear-gradient(135deg, #4a6ee0, #6a8fff);
  color: white;
  padding: 70px 0;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.banner::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="60" height="60" viewBox="0 0 80 80"><circle cx="40" cy="40" r="38" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="2"/></svg>');
  background-size: 120px 120px;
  opacity: 0.5;
}

.banner-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
  position: relative;
  z-index: 2;
}

.banner h2 {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 16px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  animation: fadeInDown 0.8s ease-out;
}

@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

.banner p {
  font-size: 1.2rem;
  margin-bottom: 35px;
  opacity: 0.95;
  animation: fadeIn 1s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 搜索框 */
.search-box {
  display: flex;
  max-width: 600px;
  margin: 0 auto;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
  border-radius: 8px;
  overflow: hidden;
  animation: fadeInUp 1.2s ease-out;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.search-box input {
  flex: 1;
  padding: 16px 20px;
  border: none;
  font-size: 16px;
  background-color: rgba(255, 255, 255, 0.95);
  transition: all 0.3s ease;
}

.search-box input:focus {
  outline: none;
  background-color: white;
}

.search-btn {
  background: linear-gradient(90deg, #ffbb33, #ffa500);
  color: #333;
  border: none;
  border-radius: 0;
  padding: 0 25px;
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.search-btn:hover {
  background: linear-gradient(90deg, #ffa500, #ff9500);
  transform: translateX(2px);
}

/* 分类导航 */
.category-nav {
  background-color: white;
  padding: 25px 0;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.03);
}

.category-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 15px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15px 20px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #f8f9fa;
  border: 1px solid #f0f0f0;
}

.category-item:hover {
  background-color: #f0f4ff;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.category-item.active {
  background-color: #e6ecff;
  color: #4a6ee0;
  border-color: #d0d8ff;
  font-weight: 500;
}

.category-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

/* 商品展示区 */
.product-section {
  padding: 50px 0;
  background-color: #f8f9fa;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 25px;
  margin-top: 25px;
}

.product-card {
  background-color: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.product-image {
  height: 180px;
  overflow: hidden;
  position: relative;
}

.product-image::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.05), transparent);
}

.negotiable-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: #ff9800;
  color: white;
  padding: 3px 8px;
  font-size: 12px;
  border-radius: 4px;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  z-index: 2;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.product-info {
  padding: 20px;
}

.product-title {
  margin: 0 0 10px;
  font-size: 17px;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.product-price {
  color: #e74c3c;
  font-weight: bold;
  font-size: 20px;
}

.original-price {
  color: #999;
  font-size: 16px;
  text-decoration: line-through;
}

.product-seller {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  padding: 5px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  background-color: #f8f9fa;
}

.product-seller:hover {
  background-color: #e6ecff;
  transform: translateX(2px);
}

.seller-icon {
  font-size: 18px;
  margin-right: 5px;
}

.seller-name {
  font-size: 14px;
  color: #666;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.store-link-arrow {
  font-size: 14px;
  color: #4a6ee0;
  margin-left: 5px;
  opacity: 0;
  transition: all 0.2s ease;
}

.product-seller:hover .store-link-arrow {
  opacity: 1;
  transform: translateX(2px);
}

.product-meta {
  display: flex;
  justify-content: space-between;
  color: #666;
  font-size: 13px;
  margin-bottom: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

/* 平台特点 */
.features-section {
  padding: 60px 0;
  background-color: white;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 35px;
  margin-top: 35px;
}

.feature-card {
  text-align: center;
  padding: 40px 25px;
  border-radius: 12px;
  background-color: #f8f9fa;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.08);
  background-color: white;
  border-color: #e6ecff;
}

.feature-icon {
  font-size: 48px;
  margin-bottom: 20px;
  display: inline-block;
  padding: 20px;
  background-color: #f0f4ff;
  border-radius: 50%;
  box-shadow: 0 5px 15px rgba(74, 110, 224, 0.1);
}

.feature-card h4 {
  margin: 0 0 15px;
  color: #4a6ee0;
  font-size: 20px;
  font-weight: 600;
}

.feature-card p {
  color: #666;
  margin: 0;
  line-height: 1.6;
}

h3 {
  font-size: 1.8rem;
  color: #333;
  margin-bottom: 10px;
  font-weight: 700;
  position: relative;
  display: inline-block;
}

h3::after {
  content: "";
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #4a6ee0, #6a8fff);
  border-radius: 3px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .banner {
    padding: 50px 0;
  }
  
  .banner h2 {
    font-size: 2rem;
  }
  
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 20px;
  }

  .features-grid {
    grid-template-columns: 1fr 1fr;
    gap: 25px;
  }
}

@media (max-width: 480px) {
  .banner h2 {
    font-size: 1.5rem;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }

  .search-box {
    flex-direction: column;
  }
  
  .search-btn {
    width: 100%;
    padding: 12px;
  }
  
  .category-list {
    justify-content: center;
  }
}

/* 加载和无数据状态 */
.loading, .no-data {
  text-align: center;
  padding: 40px 0;
  font-size: 1.2rem;
  color: #777;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0;
  background-color: #f8f9fa;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  margin: 20px 0;
}

.empty-icon {
  font-size: 60px;
  margin-bottom: 20px;
  opacity: 0.7;
}

.empty-state h3 {
  font-size: 22px;
  margin-bottom: 10px;
}

.empty-state p {
  color: #666;
  max-width: 500px;
  margin-bottom: 25px;
}

.empty-actions {
  display: flex;
  gap: 15px;
}

.action-button {
  display: inline-block;
  padding: 10px 20px;
  background-color: #4a6ee0;
  color: #fff;
  border-radius: 6px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s ease;
}

.action-button:hover {
  background-color: #3d5eca;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(74, 110, 224, 0.2);
}

/* 买家提示样式 */
.buyer-notice {
  background-color: #f8faff;
  border-radius: 10px;
  padding: 15px;
  margin: 20px auto;
  max-width: 1140px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border-left: 4px solid #4a6ee0;
}

.notice-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.notice-icon {
  font-size: 24px;
}

.notice-text h4 {
  margin: 0 0 5px 0;
  font-weight: 600;
  color: #333;
}

.notice-text p {
  margin: 0;
  color: #555;
  font-size: 14px;
}

.product-store {
  display: none; /* 隐藏多余的店铺链接 */
}
</style>
