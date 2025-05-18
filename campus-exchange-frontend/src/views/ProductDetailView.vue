<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '@/api/all'
import { fileApi } from '@/api/all'
import { cartApi } from '@/api/cart'
import { ElMessage } from 'element-plus'
import ProductGallery from '@/components/product/ProductGallery.vue'
import ProductInfo from '@/components/product/ProductInfo.vue'
import ProductDetailTabs from '@/components/product/ProductDetailTabs.vue'
import MerchantInfo from '@/components/merchant/MerchantInfo.vue'
import { reviewApi } from '@/api/all'

const route = useRoute()
const router = useRouter()
const productId = ref(route.params.id)
const product = ref(null)
const isLoading = ref(true)
const error = ref(null)

// 引入URL处理函数
const processImageUrl = fileApi.processProductImageUrl;

// 提取商家信息
const merchantInfo = computed(() => {
  if (!product.value) return null;
  
  return {
    id: product.value.merchantId,
    storeName: product.value.storeName,
    rating: product.value.averageRating,
    salesCount: product.value.salesCount
  };
});

// 设置用于订单的商品主图
const productImage = computed(() => {
  let imageUrl = '';
  if (product.value) {
    if (product.value.mainImage) {
      imageUrl = product.value.mainImage;
    } else if (product.value.imageUrls && product.value.imageUrls.length > 0) {
      imageUrl = product.value.imageUrls[0];
    }
  }
  console.log('商品主图原始URL:', imageUrl);
  return imageUrl;
});

// 处理后的图片URL数组
const processedImageUrls = computed(() => {
  if (!product.value || !product.value.imageUrls) {
    console.log('商品没有图片URLs数组');
    return [];
  }
  
  try {
    console.log('原始商品图片URLs数组长度:', product.value.imageUrls.length);
    
    // 检查数组中的URL格式
    product.value.imageUrls.forEach((url, index) => {
      console.log(`原始图片${index+1}:`, url, '类型:', typeof url);
    });
    
    // 保留原始URL数组，交给ProductGallery组件处理
    const urls = [...product.value.imageUrls];
    console.log('传递给组件的图片URLs:', urls);
    return urls;
  } catch (error) {
    console.error('处理图片URLs数组出错:', error);
    return [];
  }
});

// 商品评价数据
const reviews = ref([]);
const loading = ref(false);
const ratingFilter = ref('all'); // 评分筛选
const filteredReviews = ref([]); // 筛选后的评价列表

// 计算平均评分
const avgRating = computed(() => {
  if (!reviews.value || reviews.value.length === 0) return 0;
  
  const sum = reviews.value.reduce((total, review) => total + review.ratingScore, 0);
  return sum / reviews.value.length;
});

// 获取某个评分的百分比
const getRatingPercentage = (star) => {
  if (!reviews.value || reviews.value.length === 0) return 0;
  
  const count = reviews.value.filter(review => review.ratingScore === star).length;
  return Math.round((count / reviews.value.length) * 100);
};

// 筛选评价
const filterReviews = () => {
  if (ratingFilter.value === 'all') {
    filteredReviews.value = [...reviews.value];
  } else {
    const starFilter = parseInt(ratingFilter.value);
    filteredReviews.value = reviews.value.filter(review => review.ratingScore === starFilter);
  }
};

// 获取商品评价
const fetchProductReviews = async (productId) => {
  loading.value = true;
  try {
    const response = await reviewApi.getProductReviews(productId);
    if (response.data && response.data.code === 200) {
      reviews.value = response.data.data;
      
      // 处理用户名，保护隐私
      reviews.value.forEach(review => {
        if (review.username) {
          review.anonymousUsername = review.username.substring(0, 1) + '***' + 
              (review.username.length > 1 ? review.username.substring(review.username.length - 1) : '');
        }
      });
      
      // 初始化筛选结果
      filterReviews();
    }
  } catch (error) {
    console.error('获取商品评价失败:', error);
  } finally {
    loading.value = false;
  }
};

// 格式化评价时间
const formatReviewTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

// 获取商品详情
const fetchProductDetail = async () => {
  isLoading.value = true
  error.value = ''
  
  try {
    const response = await productApi.getProductById(productId.value)
    if (response.data.code === 200) {
      product.value = response.data.data
      
      // 检查并输出图片URL
      if (product.value.imageUrls && product.value.imageUrls.length > 0) {
        console.log('商品图片URLs数组:', product.value.imageUrls);
        product.value.imageUrls.forEach((url, index) => {
          console.log(`图片${index+1}原始URL:`, url);
          console.log(`图片${index+1}处理后URL:`, processImageUrl(url));
          
          // 测试图片是否可以加载
          const img = new Image();
          img.onload = () => console.log(`图片${index+1}成功加载`);
          img.onerror = () => console.error(`图片${index+1}加载失败`);
          img.src = processImageUrl(url);
        });
      } else {
        console.log('商品没有图片');
      }
      
      // 直接使用product中的商家信息
      console.log('商品中的商家信息:', {
        merchantId: product.value.merchantId,
        storeName: product.value.storeName
      });
      
    } else {
      error.value = response.data.message || '获取商品信息失败'
    }
  } catch (err) {
    console.error('获取商品详情失败:', err)
    error.value = '获取商品信息失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

// 返回按钮
const goBack = () => {
  router.back()
}

// 添加到购物车
const handleAddToCart = async () => {
  // 检查用户是否登录
  const userJson = localStorage.getItem('user')
  if (!userJson) {
    ElMessage.warning('请先登录后再添加到购物车')
    router.push({
      name: 'login',
      query: { redirect: `/product/${productId.value}` }
    })
    return
  }
  
  // 检查商品库存
  if (!product.value || product.value.stock <= 0) {
    ElMessage.error('商品库存不足')
    return
  }
  
  try {
    const response = await cartApi.addToCart(product.value.id, 1)
    if (response.data && response.data.code === 200) {
      ElMessage.success('成功添加到购物车')
    } else {
      ElMessage.error('添加到购物车失败：' + (response.data?.message || '未知错误'))
    }
  } catch (err) {
    console.error('添加到购物车失败:', err)
    ElMessage.error('添加到购物车失败：' + (err.message || '网络错误'))
  }
}

// 立即购买
const handleBuyNow = () => {
  // 检查用户是否登录
  const userJson = localStorage.getItem('user')
  if (!userJson) {
    ElMessage.warning('请先登录后再购买')
    router.push({
      name: 'login',
      query: { redirect: `/product/${productId.value}` }
    })
    return
  }
  
  // 检查商品库存
  if (!product.value || product.value.stock <= 0) {
    ElMessage.error('商品库存不足')
    return
  }
  
  // 构建订单预览信息
  const orderPreview = {
    productId: product.value.id,
    productName: product.value.name,
    productImage: productImage.value,
    price: product.value.currentPrice,
    quantity: 1,
    stock: product.value.stock, // 添加库存信息
    sellerId: product.value.merchantId,
    sellerName: product.value.storeName || '未知卖家'
  }
  
  // 存储订单预览信息到本地
  localStorage.setItem('orderPreview', JSON.stringify(orderPreview))
  
  // 跳转到订单确认页面
  router.push('/order/confirm')
}

onMounted(() => {
  fetchProductDetail()
  fetchProductReviews(productId.value)
})
</script>

<template>
  <div class="product-detail-container">
    <div class="container">
      <!-- 返回按钮 -->
      <div class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </div>
      
      <div v-if="isLoading" class="loading">
        <div class="spinner"></div>
        <p>正在加载商品信息...</p>
      </div>
      
      <div v-else-if="error" class="error-message">
        <p>{{ error }}</p>
        <button @click="fetchProductDetail" class="retry-button">重试</button>
      </div>
      
      <div v-else-if="product" class="product-content">
        <div class="product-detail">
          <!-- 使用商品图片组件 -->
          <div class="product-gallery-wrapper">
            <ProductGallery 
              :images="processedImageUrls" 
              :coverImage="product.mainImage ? processImageUrl(product.mainImage) : ''" 
              :altText="product.name"
            />
          </div>
          
          <!-- 使用商品信息组件 -->
          <div class="product-info-wrapper">
            <ProductInfo 
              :product="product"
              @add-to-cart="handleAddToCart"
              @buy-now="handleBuyNow"
            />
            
            <!-- 使用商家信息组件 -->
            <MerchantInfo 
              :merchant="merchantInfo"
              :showMeta="true"
            />
          </div>
        </div>
        
        <!-- 使用商品详情选项卡组件 -->
        <ProductDetailTabs 
          :description="product.description"
          :usageInstructions="product.usageInstructions"
        >
          <template #product-images>
            <!-- 商品图片展示区 -->
            <ProductGallery 
              :images="processedImageUrls" 
              :coverImage="product.mainImage ? processImageUrl(product.mainImage) : ''" 
              :altText="product.name"
              :showAllImages="true"
            />
          </template>
        </ProductDetailTabs>

        <!-- 添加商品评价区域 -->
        <div class="product-reviews-section">
          <h2 class="section-title">
            <span>商品评价</span>
            <span class="review-count">({{ reviews.length }}条)</span>
            
            <!-- 添加评分统计 -->
            <div class="rating-stats" v-if="reviews.length > 0">
              <div class="avg-rating">
                <span class="avg-score">{{ avgRating.toFixed(1) }}</span>
                <el-rate v-model="avgRating" disabled show-score text-color="#ff9900"></el-rate>
              </div>
              <div class="rating-distribution">
                <div v-for="i in 5" :key="i" class="rating-bar">
                  <span class="star-level">{{ i }}星</span>
                  <div class="progress-bar">
                    <div class="progress" :style="{width: getRatingPercentage(i) + '%'}"></div>
                  </div>
                  <span class="rating-percent">{{ getRatingPercentage(i) }}%</span>
                </div>
              </div>
            </div>
          </h2>

          <!-- 添加评价筛选 -->
          <div class="review-filters" v-if="reviews.length > 0">
            <el-radio-group v-model="ratingFilter" size="small" @change="filterReviews">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="5">5星</el-radio-button>
              <el-radio-button label="4">4星</el-radio-button>
              <el-radio-button label="3">3星</el-radio-button>
              <el-radio-button label="2">2星</el-radio-button>
              <el-radio-button label="1">1星</el-radio-button>
            </el-radio-group>
          </div>

          <div v-if="loading" class="loading-reviews">
            <div class="loading-spinner"></div>
            <span>加载评价...</span>
          </div>

          <div v-else-if="filteredReviews.length > 0" class="reviews-list">
            <div v-for="review in filteredReviews" :key="review.id" class="review-item">
              <div class="review-header">
                <div class="user-info">
                  <span class="username">{{ review.anonymousUsername || '匿名用户' }}</span>
                  <span class="review-time">{{ formatReviewTime(review.createTime) }}</span>
                </div>
                <div class="rating">
                  <el-rate
                    v-model="review.ratingScore"
                    disabled
                    text-color="#ff9900">
                  </el-rate>
                </div>
              </div>
              <div class="review-content">{{ review.content || '此用户未填写评价内容' }}</div>
            </div>
          </div>

          <div v-else-if="reviews.length > 0 && filteredReviews.length === 0" class="empty-reviews">
            <div class="empty-icon">🔍</div>
            <div class="empty-text">没有符合条件的评价</div>
          </div>

          <div v-else class="empty-reviews">
            <div class="empty-icon">📝</div>
            <div class="empty-text">暂无评价</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-detail-container {
  padding: 30px 0;
  background-color: #f8f9fa;
  min-height: 100vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.product-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.back-button {
  display: inline-flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 8px 15px;
  background-color: #f1f1f1;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.3s;
}

.back-button:hover {
  background-color: #e5e5e5;
}

.back-icon {
  margin-right: 5px;
  font-size: 18px;
}

.loading, .error-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 0;
  text-align: center;
}

.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top: 4px solid #4a6ee0;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message p {
  margin-bottom: 20px;
  color: #dc3545;
}

.retry-button {
  background-color: #4a6ee0;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.retry-button:hover {
  background-color: #3a58b6;
}

.product-detail {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
}

.product-gallery-wrapper {
  width: 100%;
}

.product-info-wrapper {
  width: 100%;
  display: flex;
  flex-direction: column;
}

@media (max-width: 768px) {
  .product-detail {
    grid-template-columns: 1fr;
  }
  
  .container {
    padding: 0 15px;
  }
}

/* 评价区域样式 */
.product-reviews-section {
  margin-top: 30px;
  padding: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.section-title > span {
  display: flex;
  align-items: center;
}

.review-count {
  font-size: 14px;
  color: #909399;
  margin-left: 8px;
  font-weight: normal;
}

.loading-reviews, .empty-reviews {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #909399;
}

.loading-spinner {
  width: 30px;
  height: 30px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.review-item {
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.review-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.review-content {
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
}

/* 评价区域增强样式 */
.rating-stats {
  margin-top: 15px;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px dashed #ebeef5;
  padding-top: 15px;
  gap: 20px;
}

.avg-rating {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 120px;
}

.avg-score {
  font-size: 32px;
  font-weight: 700;
  color: #ff9900;
  line-height: 1;
  margin-bottom: 5px;
}

.rating-distribution {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rating-bar {
  display: flex;
  align-items: center;
  font-size: 12px;
  gap: 8px;
}

.star-level {
  width: 35px;
  text-align: right;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background-color: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
}

.progress {
  height: 100%;
  background-color: #ff9900;
  border-radius: 3px;
}

.rating-percent {
  width: 40px;
  text-align: left;
}

.review-filters {
  margin: 15px 0;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .rating-stats {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .avg-rating {
    margin-bottom: 15px;
  }
  
  .rating-distribution {
    width: 100%;
  }
}
</style> 