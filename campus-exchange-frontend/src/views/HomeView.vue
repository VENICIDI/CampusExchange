<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

// 模拟分类数据
const categories = ref([
  { id: 1, name: '电子产品', icon: '📱' },
  { id: 2, name: '图书教材', icon: '📚' },
  { id: 3, name: '生活用品', icon: '🏠' },
  { id: 4, name: '服装鞋帽', icon: '👕' },
  { id: 5, name: '运动器材', icon: '🏀' },
  { id: 6, name: '交通工具', icon: '🚲' },
  { id: 7, name: '美妆护肤', icon: '💄' },
  { id: 8, name: '其他物品', icon: '🎁' }
])

// 模拟商品数据
const products = ref([
  {
    id: 1,
    title: '二手 MacBook Pro 2019',
    price: 4999,
    image: 'https://via.placeholder.com/200x150',
    seller: 'user123',
    location: '东校区',
    createTime: '2小时前'
  },
  {
    id: 2,
    title: '高等数学第七版教材',
    price: 25,
    image: 'https://via.placeholder.com/200x150',
    seller: 'math_lover',
    location: '西校区',
    createTime: '5小时前'
  },
  {
    id: 3,
    title: '全新篮球',
    price: 85,
    image: 'https://via.placeholder.com/200x150',
    seller: 'sports2023',
    location: '南校区',
    createTime: '1天前'
  },
  {
    id: 4,
    title: '宿舍书桌',
    price: 120,
    image: 'https://via.placeholder.com/200x150',
    seller: 'dorm_furniture',
    location: '北校区',
    createTime: '2天前'
  },
  {
    id: 5,
    title: '自行车',
    price: 350,
    image: 'https://via.placeholder.com/200x150',
    seller: 'bike_seller',
    location: '中心校区',
    createTime: '3天前'
  },
  {
    id: 6,
    title: 'JBL 蓝牙音箱',
    price: 199,
    image: 'https://via.placeholder.com/200x150',
    seller: 'music_fan',
    location: '东校区',
    createTime: '3天前'
  },
  {
    id: 7,
    title: '计算机网络教材',
    price: 30,
    image: 'https://via.placeholder.com/200x150',
    seller: 'cs_student',
    location: '西校区',
    createTime: '4天前'
  },
  {
    id: 8,
    title: '小米手环6',
    price: 149,
    image: 'https://via.placeholder.com/200x150',
    seller: 'tech_lover',
    location: '南校区',
    createTime: '5天前'
  }
])

// 搜索相关
const searchKeyword = ref('')
const selectedCategory = ref(0)

const router = useRouter()

// 处理商品点击
const handleProductClick = (productId: number) => {
  // 路由到商品详情页
  // router.push(`/product/${productId}`)
  alert(`点击了商品ID: ${productId}，商品详情功能尚未实现`)
}

// 处理分类点击
const handleCategoryClick = (categoryId: number) => {
  selectedCategory.value = categoryId
  // 可以在这里添加筛选逻辑
}

// 处理搜索
const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    alert('请输入搜索关键词')
    return
  }

  alert(`搜索功能尚未实现，搜索关键词: ${searchKeyword.value}`)
  // 可以在这里添加搜索逻辑
}
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

    <!-- 分类导航 -->
    <div class="category-nav">
      <div class="container">
        <h3>商品分类</h3>
        <div class="category-list">
          <div
            class="category-item"
            :class="{ active: selectedCategory === 0 }"
            @click="selectedCategory = 0"
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
            <span class="category-icon">{{ category.icon }}</span>
            <span>{{ category.name }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品展示区 -->
    <div class="product-section">
      <div class="container">
        <h3>最新上架</h3>

        <div class="product-grid">
          <div
            v-for="product in products"
            :key="product.id"
            class="product-card"
            @click="handleProductClick(product.id)"
          >
            <div class="product-image">
              <img :src="product.image" :alt="product.title" />
            </div>
            <div class="product-info">
              <h4 class="product-title">{{ product.title }}</h4>
              <div class="product-price">¥{{ product.price }}</div>
              <div class="product-meta">
                <span class="product-location">{{ product.location }}</span>
                <span class="product-time">{{ product.createTime }}</span>
              </div>
              <div class="product-seller">卖家: {{ product.seller }}</div>
            </div>
          </div>
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
  background: linear-gradient(135deg, #4a6ee0, #5a7ef2);
  color: white;
  padding: 50px 0;
  text-align: center;
}

.banner-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.banner h2 {
  font-size: 2rem;
  margin-bottom: 10px;
}

.banner p {
  font-size: 1.1rem;
  margin-bottom: 30px;
  opacity: 0.9;
}

/* 搜索框 */
.search-box {
  display: flex;
  max-width: 600px;
  margin: 0 auto;
}

.search-box input {
  flex: 1;
  padding: 12px 15px;
  border: none;
  border-radius: 4px 0 0 4px;
  font-size: 16px;
}

.search-btn {
  background-color: #ffbb33;
  color: #333;
  border: none;
  border-radius: 0 4px 4px 0;
  padding: 0 20px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
}

.search-btn:hover {
  background-color: #ffa500;
}

/* 分类导航 */
.category-nav {
  background-color: white;
  padding: 20px 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.category-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 15px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 15px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.category-item:hover {
  background-color: #f5f7ff;
}

.category-item.active {
  background-color: #e6ecff;
  color: #4a6ee0;
}

.category-icon {
  font-size: 24px;
  margin-bottom: 5px;
}

/* 商品展示区 */
.product-section {
  padding: 40px 0;
  background-color: #f9f9f9;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.product-card {
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.product-image {
  height: 150px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  padding: 15px;
}

.product-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-price {
  color: #ff4757;
  font-weight: bold;
  font-size: 18px;
  margin-bottom: 8px;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  color: #888;
  font-size: 12px;
  margin-bottom: 5px;
}

.product-seller {
  font-size: 12px;
  color: #666;
}

/* 平台特点 */
.features-section {
  padding: 50px 0;
  background-color: white;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 30px;
  margin-top: 30px;
}

.feature-card {
  text-align: center;
  padding: 30px 20px;
  border-radius: 8px;
  background-color: #f8f9ff;
  transition: transform 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-icon {
  font-size: 40px;
  margin-bottom: 15px;
}

.feature-card h4 {
  margin: 0 0 10px;
  color: #4a6ee0;
}

.feature-card p {
  color: #666;
  margin: 0;
}

h3 {
  font-size: 1.5rem;
  color: #333;
  margin-bottom: 5px;
  font-weight: 600;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  }

  .features-grid {
    grid-template-columns: 1fr 1fr;
  }

  .banner h2 {
    font-size: 1.5rem;
  }
}

@media (max-width: 480px) {
  .features-grid {
    grid-template-columns: 1fr;
  }

  .category-list {
    justify-content: center;
  }
}
</style>
