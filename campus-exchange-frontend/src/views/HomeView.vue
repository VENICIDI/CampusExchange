<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { productApi } from '@/api/all'
import ProductCard from '@/components/product/ProductCard.vue'
import { cartApi } from '@/api/cart'
import HomeBanner from '@/components/HomeBanner.vue'

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
// 购物车数据
const cartItems = ref([])
// 加载状态
const isLoading = ref({
  categories: false,
  products: false,
  cart: false
})
// 分页相关
const currentPage = ref(1)
const pageSize = ref(8)
const totalPages = ref(1)
// 搜索相关
const searchKeyword = ref('')
const selectedCategory = ref(null)
// 排序相关
const sortOptions = ref([
  { id: 1, value: 'publish_time', label: '最新发布', direction: 'desc' },
  { id: 2, value: 'price', label: '价格从低到高', direction: 'asc' },
  { id: 3, value: 'price', label: '价格从高到低', direction: 'desc' },
  { id: 4, value: 'sales_count', label: '销量排序', direction: 'desc' },
  { id: 5, value: 'average_rating', label: '好评度排序', direction: 'desc' }
])
const currentSort = ref('publish_time')
const currentSortDirection = ref('desc')

// 价格筛选
const priceRange = ref([0, 5000])
const minPrice = ref(0)
const maxPrice = ref(5000)

// 新旧程度筛选
const conditionOptions = ref([
  { value: 'NEW', label: '全新' },
  { value: 'LIKE_NEW', label: '九成新' },
  { value: 'GOOD', label: '八成新' },
  { value: 'FAIR', label: '七成新' },
  { value: 'POOR', label: '六成新及以下' }
])
const selectedCondition = ref([])

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
  currentPage.value = 1 // 重置页码
  fetchProducts({ categoryId: categoryId !== 0 ? categoryId : null })
}

// 处理搜索
const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    alert('请输入搜索关键词')
    return
  }

  // 修改为在当前页面筛选商品
  currentPage.value = 1 // 重置页码
  fetchProducts({ keyword: searchKeyword.value })
}

// 处理排序
const handleSortChange = (option) => {
  // 修改：传入完整的排序选项对象，而不只是排序字段
  currentSort.value = option.value
  currentSortDirection.value = option.direction
  console.log('选择排序:', option.label, '字段:', option.value, '方向:', option.direction)
  currentPage.value = 1 // 排序时重置页码
  fetchProducts()
}

// 处理价格筛选
const handlePriceFilter = () => {
  minPrice.value = priceRange.value[0]
  maxPrice.value = priceRange.value[1]
  currentPage.value = 1 // 筛选时重置页码
  fetchProducts()
}

// 处理新旧程度筛选
const handleConditionFilter = () => {
  currentPage.value = 1 // 筛选时重置页码
  fetchProducts()
}

// 获取分类数据
const fetchCategories = async () => {
  isLoading.value.categories = true
  try {
    const response = await productApi.getTopCategories()
    if (response.data.code === 200) {
      categories.value = response.data.data
    } else {
      // 使用模拟数据作为后备
      initializeMockCategories()
    }
  } catch (error) {
    // 使用模拟数据作为后备
    initializeMockCategories()
  } finally {
    isLoading.value.categories = false
  }
}

// 获取商品数据
const fetchProducts = async (params = {}) => {
  isLoading.value.products = true
  try {
    const query = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...params
    }
    
    // 添加分类筛选
    if (selectedCategory.value) query.categoryId = selectedCategory.value
    
    // 添加关键词筛选
    if (searchKeyword.value.trim()) query.keyword = searchKeyword.value
    
    // 添加价格筛选
    if (minPrice.value > 0) query.minPrice = minPrice.value
    if (maxPrice.value < 5000) query.maxPrice = maxPrice.value
    
    // 修复排序参数
    if (currentSort.value && currentSortDirection.value) {
      query.orderBy = currentSort.value
      query.orderDirection = currentSortDirection.value
    }
    
    // 修复新旧程度筛选参数
    if (selectedCondition.value.length > 0) {
      // 将数组参数转换为逗号分隔的字符串
      query.condition = selectedCondition.value.join(',')
    }
    
    console.log('发送查询参数:', query)
    const res = await productApi.getProducts(query)
    if (res.data && res.data.code === 200) {
      const data = res.data.data
      products.value = data.records || []
      totalPages.value = data.pages || 1
    }
  } catch (e) {
    console.error("获取商品列表失败:", e)
    products.value = []
    totalPages.value = 1
  } finally {
    isLoading.value.products = false
  }
}

// 获取购物车数据
const fetchCartItems = async () => {
  if (!isLoggedIn.value) return;
  
  isLoading.value.cart = true;
  try {
    const response = await cartApi.getCartItems();
    if (response.data && response.data.code === 200) {
      cartItems.value = response.data.data || [];
    } else {
      cartItems.value = [];
    }
  } catch (error) {
    cartItems.value = [];
  } finally {
    isLoading.value.cart = false;
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

// 组件挂载时获取数据
onMounted(() => {
  console.log('首页组件挂载，开始获取数据')
  
  // 获取分类和商品数据
  fetchCategories()
  fetchProducts()
  
  // 如果已登录，获取购物车数据
  if (isLoggedIn.value) {
    fetchCartItems()
  }
  
  // 监听分类变更事件 - 从FloatingHeader传递
  window.addEventListener('category-change', e => {
    console.log('捕获分类变更事件，分类ID:', e.detail)
    selectedCategory.value = e.detail
    searchKeyword.value = ''
    currentPage.value = 1
    fetchProducts({
      categoryId: selectedCategory.value,
      pageNum: 1,
      pageSize: pageSize.value
    })
  })
  
  // 监听搜索关键词事件 - 从FloatingHeader传递
  window.addEventListener('search-keyword', e => {
    console.log('捕获搜索事件，关键词:', e.detail)
    searchKeyword.value = e.detail
    selectedCategory.value = null
    currentPage.value = 1
    fetchProducts({
      keyword: searchKeyword.value,
      pageNum: 1,
      pageSize: pageSize.value
    })
  })
  
  // 组件卸载时移除事件监听
  return () => {
    window.removeEventListener('category-change', null)
    window.removeEventListener('search-keyword', null)
  }
})
</script>

<template>
  <div class="container">
    <!-- 轮播图组件 -->
    <HomeBanner />

    <!-- 筛选区域 -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-label">排序：</div>
        <div class="filter-options sort-options">
          <div 
            v-for="option in sortOptions" 
            :key="option.id"
            :class="{ active: currentSort === option.value && currentSortDirection === option.direction }"
            @click="handleSortChange(option)"
          >
            {{ option.label }}
        </div>
      </div>
    </div>
    
      <div class="filter-row">
        <div class="filter-label">价格：</div>
        <div class="price-filter">
          <el-slider
            v-model="priceRange"
            range
            :min="0"
            :max="5000"
            :step="100"
          ></el-slider>
          <div class="price-range-display">
            <span>¥{{ priceRange[0] }}</span>
            <span>-</span>
            <span>¥{{ priceRange[1] }}</span>
            <el-button class="price-apply-btn" size="small" @click="handlePriceFilter">应用</el-button>
        </div>
      </div>
    </div>

      <div class="filter-row">
        <div class="filter-label">新旧程度：</div>
        <div class="condition-filter">
          <el-checkbox-group v-model="selectedCondition" @change="handleConditionFilter">
            <el-checkbox v-for="option in conditionOptions" :key="option.value" :label="option.value">
              {{ option.label }}
            </el-checkbox>
          </el-checkbox-group>
        </div>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="product-section">
      <div v-if="isLoading.products" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载商品中...</p>
        </div>
      <template v-else-if="products.length > 0">
        <div class="products-container">
          <ProductCard 
            v-for="product in products" 
            :key="product.id" 
            :product="product"
            @click="handleProductClick"
          />
        </div>
        
        <!-- 分页控制 -->
        <div class="pagination-container">
          <button 
            :disabled="currentPage === 1" 
            @click="currentPage > 1 && fetchProducts({ pageNum: currentPage - 1 })"
          >上一页</button>
          <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
          <button 
            :disabled="currentPage === totalPages" 
            @click="currentPage < totalPages && fetchProducts({ pageNum: currentPage + 1 })"
          >下一页</button>
        </div>
      </template>
      <div v-else class="empty-container">
        <p>暂无商品，请尝试其他分类或搜索关键词</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 筛选区样式 */
.filter-section {
  background: #f9f9f9;
  border-radius: 10px;
  padding: 15px 20px;
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px dashed #eee;
}

.filter-row:last-child {
  border-bottom: none;
}

.filter-label {
  width: 90px;
  color: #333;
  font-weight: 500;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-options > div {
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  background: #fff;
  border: 1px solid #e8e8e8;
  transition: all 0.2s;
}

.filter-options > div.active {
  background: #4a6ee0;
  color: #fff;
  border-color: #4a6ee0;
}

.filter-options > div:hover {
  border-color: #4a6ee0;
  color: #4a6ee0;
}

.filter-options > div.active:hover {
  color: #fff;
}

.price-filter {
  width: 100%;
  max-width: 500px;
  padding: 0 15px;
}

.price-range-display {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
}

.price-apply-btn {
  margin-left: 15px;
}

.condition-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

/* 商品列表区域样式 */
.products-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 20px;
  margin-bottom: 30px;
}

/* 加载中样式 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #4a6ee0;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 空状态样式 */
.empty-container {
  text-align: center;
  padding: 60px 0;
  color: #666;
}

/* 分页样式 */
.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 30px;
  margin-bottom: 20px;
}

.pagination-container button {
  padding: 8px 16px;
  background-color: #4a6ee0;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin: 0 10px;
  transition: background-color 0.2s;
}

.pagination-container button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.pagination-container .page-info {
  font-size: 14px;
  color: #666;
}
</style>
