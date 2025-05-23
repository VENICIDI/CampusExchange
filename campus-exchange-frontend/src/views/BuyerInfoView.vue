<template>
  <div class="buyer-info-container">
    <div class="container py-5 px-md-5">
      <div class="mb-4">
        <button class="btn-back" @click="goBack">
          <i class="fas fa-arrow-left"></i>
          <span>返回</span>
        </button>
      </div>
      
      <h2 class="page-title mb-4">买家信息</h2>

      <!-- 加载中状态 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">加载中...</span>
        </div>
        <p class="mt-2">正在加载买家信息...</p>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="error" class="alert alert-danger">
        {{ error }}
      </div>

      <!-- 买家信息内容 -->
      <div v-else-if="buyerInfo" class="buyer-info-content">
        <!-- 买家基本信息卡片 -->
        <div class="card mb-4">
          <div class="card-header">
            <h5 class="mb-0"><i class="fas fa-user me-2"></i>个人资料</h5>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-md-3 text-center mb-4 mb-md-0">
                <div class="avatar-container">
                  <img 
                    :src="buyerInfo.avatar || 'https://via.placeholder.com/100/e0e0e0/666666?text=用户'" 
                    alt="买家头像" 
                    class="buyer-avatar"
                  >
                </div>
              </div>
              <div class="col-md-9">
                <div class="buyer-details">
                  <div class="detail-item">
                    <span class="label">用户名：</span>
                    <span class="value">{{ buyerInfo.username }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">注册时间：</span>
                    <span class="value">{{ buyerInfo.registerTime || '未知' }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">交易次数：</span>
                    <span class="value">{{ buyerInfo.totalTransactionCount || 0 }} 次</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">好评率：</span>
                    <span class="value">
                      <span class="rating-value">{{ formatPositiveRate(buyerInfo.positiveRate) }}</span>
                      <el-progress 
                        :percentage="buyerInfo.positiveRate * 100" 
                        :color="getRatingColor(buyerInfo.positiveRate)"
                        :format="() => ''"
                        :stroke-width="10"
                        class="rating-progress"
                      ></el-progress>
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 买家评价列表 -->
        <div class="card mb-4">
          <div class="card-header">
            <h5 class="mb-0"><i class="fas fa-comment-dots me-2"></i>历史评价</h5>
          </div>
          <div class="card-body">
            <div v-if="!buyerInfo.reviews || buyerInfo.reviews.length === 0" class="text-center py-4">
              <p class="text-muted">暂无历史评价信息</p>
            </div>
            <div v-else class="review-list">
              <div v-for="(review, index) in buyerInfo.reviews" :key="index" class="review-item">
                <div class="review-header">
                  <span class="merchant-name">匿名商家</span>
                  <span class="review-time">{{ review.createTime }}</span>
                </div>
                <div class="review-rating">
                  <el-rate 
                    v-model="review.ratingScore" 
                    disabled 
                    show-score 
                    text-color="#ff9900">
                  </el-rate>
                </div>
                <div v-if="review.content" class="review-content">
                  {{ review.content }}
                </div>
                <div v-if="index < buyerInfo.reviews.length - 1" class="divider"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import api from '@/api/index'; // 正确导入项目的API实例

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const error = ref(null);
const buyerInfo = ref(null);

// 获取买家信息
const fetchBuyerInfo = async () => {
  const buyerId = route.params.buyerId;
  if (!buyerId) {
    error.value = '买家ID不存在';
    loading.value = false;
    return;
  }

  try {
    loading.value = true;
    
    // 获取当前用户信息和商家信息
    const userStr = localStorage.getItem('user');
    const merchantId = localStorage.getItem('merchantId') || localStorage.getItem('currentViewingMerchantId');
    
    console.log('当前用户信息:', userStr);
    console.log('当前商家ID:', merchantId);
    
    // 确保用户已登录且是商家身份
    if (!userStr) {
      throw new Error('未登录，请先登录');
    }
    
    // 解析用户数据
    const userData = JSON.parse(userStr);
    if (!userData || !userData.role || userData.role !== 'MERCHANT') {
      throw new Error('您不是商家，无权查看买家信息');
    }
    
    // 构建请求头
    const headers = {
      'X-User-Id': userData.userId,
      'X-User-Name': userData.username,
      'X-User-Role': userData.role,
      'X-Merchant-Id': merchantId
    };
    
    console.log('使用以下头信息发送请求:', headers);
    
    // 发送请求
    const response = await api.get(`/buyer-info/${buyerId}`, { headers });
    
    if (response.data && response.data.code === 200) {
      buyerInfo.value = response.data.data;
      console.log('买家信息:', buyerInfo.value);
    } else {
      error.value = '获取买家信息失败：' + (response.data?.message || '未知错误');
    }
  } catch (err) {
    console.error('获取买家信息出错:', err);
    error.value = '获取买家信息失败：' + (err.message || '网络错误');
  } finally {
    loading.value = false;
  }
};

// 格式化好评率
const formatPositiveRate = (rate) => {
  if (rate === null || rate === undefined) {
    return '暂无评分';
  }
  return (rate * 100).toFixed(0) + '%';
};

// 获取评分颜色
const getRatingColor = (rate) => {
  if (rate === null || rate === undefined) {
    return '#909399';
  }
  if (rate >= 0.9) {
    return '#67C23A';  // 绿色 - 优秀
  } else if (rate >= 0.8) {
    return '#E6A23C';  // 黄色 - 良好
  } else if (rate >= 0.6) {
    return '#F56C6C';  // 红色 - 一般
  } else {
    return '#FF0000';  // 深红色 - 较差
  }
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 页面加载时获取数据
onMounted(async () => {
  await fetchBuyerInfo();
});
</script>

<style scoped>
.buyer-info-container {
  min-height: 90vh;
  background-color: #f8f9fa;
  padding: 0 15px;
}

.container {
  max-width: 1000px;
}

.btn-back {
  display: flex;
  align-items: center;
  background-color: rgba(70, 90, 230, 0.1);
  border: none;
  color: #4568dc;
  font-size: 0.95rem;
  padding: 10px 16px;
  border-radius: 6px;
  transition: all 0.2s ease;
  cursor: pointer;
  font-weight: 500;
}

.btn-back:hover {
  background-color: rgba(70, 90, 230, 0.15);
  color: #3652b3;
}

.btn-back i {
  margin-right: 8px;
  font-size: 14px;
}

.page-title {
  font-weight: 600;
  color: #333;
  font-size: 1.5rem;
}

.card {
  border: none;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
}

.card-header {
  background-color: white;
  border-bottom: 1px solid rgba(0,0,0,0.1);
  padding: 16px 20px;
}

.card-header h5 {
  margin: 0;
  font-weight: 600;
  color: #444;
  font-size: 1.1rem;
}

.card-header h5 i {
  color: #4568dc;
}

.card-body {
  padding: 20px;
  background-color: #ffffff;
}

.avatar-container {
  margin-bottom: 15px;
}

.buyer-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #f0f0f0;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.buyer-details .detail-item {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.detail-item .label {
  width: 90px;
  font-weight: 500;
  color: #666;
}

.detail-item .value {
  color: #333;
  flex: 1;
}

.rating-value {
  font-weight: 600;
  margin-right: 10px;
  color: #f5a623;
}

.rating-progress {
  width: 200px;
  display: inline-block;
  vertical-align: middle;
}

.review-list {
  padding: 0;
}

.review-item {
  padding: 20px 0;
}

.review-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.merchant-name {
  font-weight: 600;
  color: #4568dc;
}

.review-time {
  color: #999;
  font-size: 0.9rem;
}

.review-rating {
  margin-bottom: 15px;
}

.review-content {
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
  color: #555;
  line-height: 1.5;
  box-shadow: 0 1px 3px rgba(0,0,0,0.03);
}

.divider {
  height: 1px;
  background-color: #f0f0f0;
  margin: 20px 0 0 0;
}

@media (max-width: 767.98px) {
  .buyer-details .detail-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .detail-item .label {
    width: 100%;
    margin-bottom: 5px;
  }
  
  .rating-progress {
    width: 100%;
    margin-top: 10px;
  }
}
</style> 