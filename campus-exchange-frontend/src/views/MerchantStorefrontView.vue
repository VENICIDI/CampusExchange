<template>
  <div class="merchant-storefront">
    <!-- 顶部商家信息卡片 -->
    <div class="merchant-info-card" v-if="merchantProfile">
      <div class="merchant-header">
        <div class="merchant-avatar">
          <img :src="merchantProfile.avatar || '/images/default-avatar.png'" alt="店铺头像">
        </div>
        <div class="merchant-basic-info">
          <h1 class="store-name">{{ merchantProfile.storeName }}</h1>
          <div class="merchant-badges">
            <span class="merchant-level">{{ merchantProfile.levelName || '新手商家' }}</span>
            <span class="merchant-rating" v-if="merchantProfile.storePositiveRate">
              <i class="el-icon-star-on"></i>
              好评率: {{ (merchantProfile.storePositiveRate * 100).toFixed(1) }}%
            </span>
          </div>
          <div class="merchant-stats">
            <div class="stat-item">
              <span class="stat-label">总销量</span>
              <span class="stat-value">{{ merchantProfile.totalSalesCount || 0 }}件</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">创店时间</span>
              <span class="stat-value">{{ formatDate(merchantProfile.createTime) }}</span>
            </div>
          </div>
        </div>
        <!-- 只有商家自己能进入后台管理 -->
        <div class="merchant-actions" v-if="isSelfStore">
          <el-button type="primary" @click="goToMerchantDashboard">管理店铺</el-button>
        </div>
      </div>
      
      <!-- 店铺公告/简介 -->
      <div class="merchant-description" v-if="merchantProfile.description">
        <h3>店铺公告</h3>
        <p>{{ merchantProfile.description }}</p>
      </div>
      
      <!-- 联系方式（仅在确认购买后可见） -->
      <div class="merchant-contact" v-if="showContact">
        <h3>联系方式</h3>
        <ul>
          <li v-if="merchantProfile.phone">
            <span class="contact-label">电话:</span>
            <span class="contact-value">{{ merchantProfile.phone }}</span>
          </li>
          <li v-if="merchantProfile.wechat">
            <span class="contact-label">微信:</span>
            <span class="contact-value">{{ merchantProfile.wechat }}</span>
          </li>
          <li v-if="merchantProfile.email">
            <span class="contact-label">邮箱:</span>
            <span class="contact-value">{{ merchantProfile.email }}</span>
          </li>
        </ul>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="merchant-products">
      <h3>店铺商品</h3>
      <div v-if="isLoading.products" class="loading-products">正在加载商品...</div>
      <div v-else-if="!merchantProfile.products || merchantProfile.products.length === 0" class="no-products">
        <p>商家暂无商品发布</p>
      </div>
      <div v-else class="product-grid">
        <ProductCard 
          v-for="product in merchantProfile.products" 
          :key="product.id"
          :product="product"
          :showMerchant="false"
          @click="viewProductDetail"
        />
      </div>
      
      <!-- 查看更多商品按钮 -->
      <div class="view-more" v-if="merchantProfile && merchantProfile.products && merchantProfile.products.length > 0">
        <el-button type="text" @click="viewAllProducts">查看全部商品</el-button>
      </div>
    </div>

    <!-- 店铺评价 -->
    <div class="merchant-reviews-section">
      <h2>买家评价</h2>
      <div v-if="isLoading.reviews" class="loading">
        <el-skeleton :rows="3" animated />
      </div>
      <div v-else-if="!merchantProfile || !merchantProfile.reviews || merchantProfile.reviews.length === 0" class="empty-state">
        <div class="empty-icon">💬</div>
        <p>暂无评价</p>
      </div>
      <div v-else class="reviews-list">
        <div v-for="review in merchantProfile.reviews" :key="review.id" class="review-item">
          <div class="review-header">
            <div class="reviewer-info">
              <div class="reviewer-avatar">👤</div>
              <span class="reviewer-name">{{ review.username || '匿名用户' }}</span>
            </div>
            <div class="review-rating">
              <el-rate
                v-model="review.serviceAttitudeRating"
                disabled
                text-color="#ff9900">
              </el-rate>
            </div>
          </div>
          <div class="review-content">{{ review.content || '该用户没有填写评价内容' }}</div>
          <div class="review-time">{{ formatDate(review.createTime) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import ProductCard from '@/components/product/ProductCard.vue';

const route = useRoute();
const router = useRouter();
const merchantId = ref(route.params.id);
const merchantProfile = ref(null);
const isLoading = ref({
  profile: true,
  products: true,
  reviews: true
});
const showContact = ref(false); // 控制是否显示联系方式

// 判断是否是自己的店铺
const isSelfStore = computed(() => {
  const userJson = localStorage.getItem('user');
  if (!userJson) return false;
  
  try {
    const userData = JSON.parse(userJson);
    // 如果是商家且ID匹配，则为自己的店铺
    return userData.role === 'MERCHANT' && merchantProfile.value && 
           userData.merchantId === merchantProfile.value.merchantId;
  } catch (e) {
    return false;
  }
});

// 获取商家主页信息
const fetchMerchantProfile = async () => {
  isLoading.value.profile = true;
  isLoading.value.products = true;
  isLoading.value.reviews = true;
  
  try {
    const response = await fetch(`/api/merchants/${merchantId.value}/profile`);
    const data = await response.json();
    
    if (data.code === 200 && data.data) {
      merchantProfile.value = data.data;
      console.log('获取到的商家主页信息:', merchantProfile.value);
    } else {
      ElMessage.error(data.message || '获取商家信息失败');
    }
  } catch (error) {
    console.error('获取商家主页信息出错:', error);
    ElMessage.error('获取商家信息出错，请稍后重试');
  } finally {
    isLoading.value.profile = false;
    isLoading.value.products = false;
    isLoading.value.reviews = false;
  }
};

// 查看商品详情
const viewProductDetail = (productId) => {
  router.push(`/product/${productId}`);
};

// 查看全部商品
const viewAllProducts = () => {
  // TODO: 实现查看全部商品功能（可能需要新页面）
  ElMessage.info('查看全部商品功能开发中');
};

// 进入商家中心
const goToMerchantDashboard = () => {
  router.push('/merchant');
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

// 格式化商品新旧程度
const formatCondition = (condition) => {
  if (!condition) return '未知';
  
  const conditionMap = {
    'NEW': '全新',
    'LIKE_NEW': '九成新',
    'GOOD': '八成新',
    'FAIR': '七成新',
    'POOR': '六成新及以下'
  };
  
  return conditionMap[condition] || condition;
};

// 页面加载时获取数据
onMounted(() => {
  if (merchantId.value) {
    fetchMerchantProfile();
  } else {
    ElMessage.error('商家ID不能为空');
    router.push('/');
  }
});
</script>

<style scoped>
.merchant-storefront {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.merchant-info-card {
  background-color: white;
  border-radius: 10px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.merchant-header {
  display: flex;
  align-items: center;
}

.merchant-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 25px;
  border: 3px solid #f0f0f0;
}

.merchant-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.merchant-basic-info {
  flex: 1;
}

.store-name {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 10px;
  color: #333;
}

.merchant-badges {
  display: flex;
  gap: 12px;
  margin-bottom: 15px;
}

.merchant-level {
  background-color: #4a6ee0;
  color: white;
  padding: 3px 10px;
  border-radius: 15px;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
}

.merchant-rating {
  color: #ff9900;
  display: inline-flex;
  align-items: center;
  font-size: 14px;
}

.merchant-stats {
  display: flex;
  gap: 25px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 13px;
  color: #999;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.merchant-actions {
  margin-left: auto;
}

.merchant-description {
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.merchant-description h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 10px;
}

.merchant-description p {
  color: #666;
  line-height: 1.6;
}

.merchant-contact {
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.merchant-contact h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 10px;
}

.merchant-contact ul {
  list-style: none;
  padding: 0;
}

.merchant-contact li {
  display: flex;
  margin-bottom: 8px;
}

.contact-label {
  width: 70px;
  color: #666;
}

.contact-value {
  color: #333;
  font-weight: 500;
}

.merchant-products {
  background-color: white;
  border-radius: 10px;
  padding: 25px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.merchant-products h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
  position: relative;
  padding-left: 15px;
}

.merchant-products h3::before {
  content: "";
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background: #4a6ee0;
  border-radius: 2px;
}

.loading-products {
  padding: 20px;
}

.no-products {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #999;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 10px;
  opacity: 0.5;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.view-more {
  text-align: center;
  margin-top: 20px;
}

.merchant-reviews-section {
  background-color: white;
  border-radius: 10px;
  padding: 25px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.merchant-reviews-section h2 {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
  position: relative;
  padding-left: 15px;
}

.merchant-reviews-section h2::before {
  content: "";
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background: #4a6ee0;
  border-radius: 2px;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  padding: 15px;
  border-radius: 8px;
  background-color: #f9f9f9;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.reviewer-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background-color: #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.reviewer-name {
  font-weight: 500;
  color: #333;
}

.review-content {
  color: #666;
  line-height: 1.6;
  margin-bottom: 10px;
}

.review-time {
  color: #999;
  font-size: 12px;
  text-align: right;
}

.loading {
  padding: 20px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #999;
  text-align: center;
}

@media (max-width: 768px) {
  .merchant-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .merchant-avatar {
    margin-bottom: 15px;
  }
  
  .merchant-actions {
    margin-left: 0;
    margin-top: 15px;
  }
  
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  }
}
</style> 