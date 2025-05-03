<script setup>
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
const handleProductClick = (productId) => {
  // 路由到商品详情页
  // router.push(`/product/${productId}`)
  alert(`点击了商品ID: ${productId}，商品详情功能尚未实现`)
}

// 处理分类点击
const handleCategoryClick = (categoryId) => {
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

.product-price {
  color: #e74c3c;
  font-weight: bold;
  font-size: 20px;
  margin-bottom: 12px;
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

.product-seller {
  font-size: 13px;
  color: #666;
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
</style>
