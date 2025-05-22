<template>
  <div class="review-container">
    <div class="review-header">
      <h1>订单评价</h1>
      <div class="order-info">
        <span class="order-no">订单号: {{ orderNo }}</span>
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <span>加载订单信息...</span>
    </div>

    <div v-else-if="order" class="review-form-container">
      <!-- 商品列表 -->
      <div class="products-section">
        <h2>评价商品</h2>
        <div class="product-review-list">
          <div v-for="(item, index) in order.orderItems" :key="item.id" class="product-review-item">
            <div class="product-info">
              <div class="product-image">
                <img :src="item.productImageSnapshot || '/default-product.png'" :alt="item.productNameSnapshot">
              </div>
              <div class="product-details">
                <h3>{{ item.productNameSnapshot }}</h3>
                <div class="product-price">¥{{ item.priceAtPurchase }} x {{ item.quantity }}</div>
              </div>
            </div>
            
            <div class="product-rating">
              <div class="rating-label">商品评分:</div>
              <div class="star-rating">
                <el-rate 
                  v-if="productReviews[index] && productReviews[index].productReview"
                  v-model="productReviews[index].productReview.ratingScore" 
                  :colors="['#FFCDD2', '#EF9A9A', '#E57373']" 
                  show-text 
                  :texts="['差评', '一般', '还不错', '满意', '非常满意']"
                  :score-template="'{value}'">
                </el-rate>
                <el-rate v-else
                  :model-value="5" 
                  disabled
                  :colors="['#FFCDD2', '#EF9A9A', '#E57373']" 
                  show-text 
                  :texts="['差评', '一般', '还不错', '满意', '非常满意']"
                  :score-template="'{value}'">
                </el-rate>
              </div>
            </div>
            
            <div class="product-comment">
              <el-input
                v-if="productReviews[index] && productReviews[index].productReview"
                v-model="productReviews[index].productReview.content"
                type="textarea"
                :rows="3"
                placeholder="请分享您对商品的使用感受，对其他买家帮助很大哦！"
                maxlength="1000"
                show-word-limit
              ></el-input>
              <el-input
                v-else
                :model-value="''"
                disabled
                type="textarea"
                :rows="3"
                placeholder="加载中..."
                maxlength="1000"
                show-word-limit
              ></el-input>
            </div>
          </div>
        </div>
      </div>

      <!-- 商家服务评价 -->
      <div class="merchant-section">
        <h2>评价商家服务</h2>
        <div class="merchant-review">
          <div class="merchant-name">{{ order.merchantName }}</div>
          
          <div class="service-rating">
            <div class="rating-label">服务态度:</div>
            <div class="star-rating">
              <el-rate 
                v-model="merchantServiceReview.serviceAttitudeRating"
                :colors="['#FFCDD2', '#EF9A9A', '#E57373']"
                show-text
                :texts="['差评', '一般', '还不错', '满意', '非常满意']"
                :score-template="'{value}'">
              </el-rate>
            </div>
          </div>
          
          <div class="service-comment">
            <el-input
              v-model="merchantServiceReview.content"
              type="textarea"
              :rows="3"
              placeholder="请评价商家的服务态度、发货速度等方面"
              maxlength="1000"
              show-word-limit
            ></el-input>
          </div>
        </div>
      </div>

      <div class="submit-section">
        <el-button type="primary" :loading="submitting" @click="submitReview">提交评价</el-button>
        <el-button @click="goBack">返回</el-button>
      </div>
    </div>

    <div v-else class="error-state">
      <div class="error-icon">❌</div>
      <div class="error-message">无法加载订单信息或该订单不存在</div>
      <el-button type="primary" @click="goBack">返回</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { orderApi, reviewApi } from '@/api/all';

const route = useRoute();
const router = useRouter();
const orderNo = ref(route.params.orderNo);
const order = ref(null);
const loading = ref(true);
const submitting = ref(false);

// 商品评价表单
const productReviews = ref([]);

// 商家服务评价表单
const merchantServiceReview = ref({
  serviceAttitudeRating: 5,
  content: ''
});

// 初始化商品评价表单 - 确保productReviews有初始值
const initializeProductReviews = () => {
  if (order.value && order.value.orderItems && order.value.orderItems.length > 0) {
    productReviews.value = order.value.orderItems.map(item => ({
      orderItemId: item.id,
      productReview: {
        ratingScore: 5,
        content: ''
      }
    }));
  } else {
    productReviews.value = [];
  }
};

// 获取订单详情
const fetchOrderDetail = async () => {
  loading.value = true;
  try {
    const response = await orderApi.getOrderDetail(orderNo.value);
    if (response.data && response.data.code === 200) {
      order.value = response.data.data;
      
      // 检查订单状态是否为已收货或拒绝退货状态
      if (order.value.status !== 'RECEIVED' && order.value.status !== 'RETURN_REJECTED') {
        ElMessage.warning('只能评价已收货或拒绝退货的订单');
        router.push(`/order/detail/${orderNo.value}`);
        return;
      }
      
      // 初始化商品评价表单
      initializeProductReviews();
    } else {
      ElMessage.error('获取订单详情失败');
    }
  } catch (error) {
    console.error('获取订单详情出错:', error);
    ElMessage.error('获取订单详情出错');
  } finally {
    loading.value = false;
  }
};

// 提交评价
const submitReview = async () => {
  // 验证商品评分
  for (let i = 0; i < productReviews.value.length; i++) {
    if (!productReviews.value[i].productReview.ratingScore) {
      ElMessage.warning(`请对第${i + 1}个商品进行评分`);
      return;
    }
  }
  
  // 验证商家服务评分
  if (!merchantServiceReview.value.serviceAttitudeRating) {
    ElMessage.warning('请对商家服务进行评分');
    return;
  }
  
  try {
    submitting.value = true;
    
    // 构建评价请求数据
    const reviewData = {
      orderNo: orderNo.value,
      productReviews: productReviews.value,
      merchantServiceReview: merchantServiceReview.value
    };
    
    // 提交评价
    const response = await reviewApi.submitOrderReview(reviewData);
    
    if (response.data && response.data.code === 200) {
      ElMessage.success('评价提交成功');
      router.push('/orders/user');
    } else {
      ElMessage.error(response.data?.message || '评价提交失败');
    }
  } catch (error) {
    console.error('提交评价出错:', error);
    ElMessage.error('提交评价出错');
  } finally {
    submitting.value = false;
  }
};

// 返回上一页
const goBack = () => {
  router.push('/orders/user');
};

onMounted(() => {
  fetchOrderDetail();
  
  // 检查是否已评价
  reviewApi.hasReviewedOrder(orderNo.value).then(response => {
    if (response.data && response.data.code === 200 && response.data.data) {
      ElMessage.warning('该订单已评价');
      router.push(`/order/detail/${orderNo.value}`);
    }
  }).catch(error => {
    console.error('检查评价状态出错:', error);
  });
});
</script>

<style scoped>
.review-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px 20px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.review-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.order-no {
  font-size: 14px;
  color: #606266;
}

.loading-state, .error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-icon {
  font-size: 40px;
  margin-bottom: 16px;
}

.error-message {
  font-size: 16px;
  color: #606266;
  margin-bottom: 20px;
}

.products-section, .merchant-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

h2 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.product-review-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.product-review-item {
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.product-info {
  display: flex;
  margin-bottom: 15px;
}

.product-image {
  width: 80px;
  height: 80px;
  margin-right: 15px;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.product-details {
  flex: 1;
}

.product-details h3 {
  font-size: 16px;
  font-weight: 500;
  margin: 0 0 8px 0;
  color: #333;
}

.product-price {
  color: #f56c6c;
  font-size: 14px;
}

.product-rating, .service-rating {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.rating-label {
  width: 80px;
  font-size: 14px;
  color: #606266;
}

.star-rating {
  flex: 1;
}

.product-comment, .service-comment {
  margin-bottom: 10px;
}

.merchant-review {
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.merchant-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 15px;
}

.submit-section {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 30px;
}
</style> 