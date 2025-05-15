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
</style> 