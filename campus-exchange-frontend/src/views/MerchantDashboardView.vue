<template>
  <div class="merchant-dashboard">
    <div class="dashboard-header">
      <h1>商家中心</h1>
      <p v-if="isLoadingMerchant">加载中...</p>
      <div v-else-if="merchant" class="merchant-info">
        <h2>{{ storeName }}</h2>
        <p class="merchant-description">{{ merchant.description }}</p>
        <div class="merchant-meta">
          <span class="merchant-level">等级: {{ merchant.level }}</span>
          <span class="merchant-since">开店时间: {{ new Date(merchant.createTime).toLocaleDateString() }}</span>
        </div>
      </div>
    </div>

    <div class="dashboard-stats">
      <div class="stat-card">
        <div class="stat-icon">📦</div>
        <div class="stat-value">{{ productCount }}</div>
        <div class="stat-label">商品数量</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">💰</div>
        <div class="stat-value">{{ orderCount }}</div>
        <div class="stat-label">订单数量</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">⭐</div>
        <div class="stat-value">{{ rating }}%</div>
        <div class="stat-label">好评率</div>
      </div>
    </div>

    <div class="dashboard-actions">
      <router-link to="/product/publish" class="action-button">
        <span class="action-icon">+</span>
        <span>发布新商品</span>
      </router-link>
      <button class="action-button" @click="viewProducts">
        <span class="action-icon">📋</span>
        <span>管理商品</span>
      </button>
      <button class="action-button" @click="viewOrders">
        <span class="action-icon">📊</span>
        <span>订单管理</span>
      </button>
      <router-link to="/merchant/profile" class="action-button">
        <span class="action-icon">👤</span>
        <span>个人信息管理</span>
      </router-link>
    </div>

    <div class="merchant-products" v-if="products.length > 0">
      <h2>我的商品</h2>
      <div class="product-list">
        <div v-for="product in products" :key="product.id" class="product-item">
          <div class="product-image">
            <img :src="product.mainImage || '/placeholder.png'" :alt="product.name">
            <div v-if="product.negotiable" class="negotiable-badge">可议价</div>
          </div>
          <div class="product-info">
            <h3>{{ product.name }}</h3>
            <div class="product-price">
              <span class="current-price">¥{{ product.currentPrice }}</span>
              <span class="original-price" v-if="product.originalPrice > product.currentPrice">¥{{ product.originalPrice }}</span>
            </div>
            <div class="product-meta">
              <span class="product-condition">{{ formatCondition(product.productCondition) }}</span>
              <span class="product-status" :class="'status-' + product.status.toLowerCase()">
                {{ getStatusText(product.status) }}
              </span>
            </div>
            <div class="product-stats">
              <span>库存: {{ product.stock }}</span>
              <span>销量: {{ product.salesCount || 0 }}</span>
            </div>
          </div>
          <div class="product-actions">
            <router-link :to="'/product/edit/' + product.id" class="edit-button">编辑</router-link>
            <button class="view-button" @click="viewProduct(product.id)">查看</button>
          </div>
        </div>
      </div>
    </div>
    <div class="merchant-products-empty" v-else>
      <div class="empty-state">
        <div class="empty-icon">📦</div>
        <h3>暂无商品</h3>
        <p>您还没有发布任何商品，立即发布商品开始销售吧！</p>
        <router-link to="/product/publish" class="publish-button">发布商品</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { productApi, merchantApi } from '@/api/all';

const router = useRouter();
const storeName = ref('');
const productCount = ref(0);
const orderCount = ref(0);
const rating = ref(0);
const products = ref([]);
const user = ref(null);
const merchant = ref(null);
const isLoadingMerchant = ref(false);

// 获取用户信息
const fetchUserInfo = () => {
  const userJson = localStorage.getItem('user');
  if (userJson) {
    try {
      user.value = JSON.parse(userJson);
      // 这里根据用户ID加载更多用户信息
      console.log('当前登录用户:', user.value);
      fetchMerchantInfo(user.value.userId);
    } catch (e) {
      console.error('解析用户数据失败:', e);
    }
  }
};

// 获取商家信息
const fetchMerchantInfo = async (userId) => {
  isLoadingMerchant.value = true;
  try {
    console.log('获取商家信息，用户ID:', userId);
    
    // 调用API获取商家信息
    const response = await merchantApi.getMerchantByUserId(userId);
    console.log('商家信息API响应:', response);
    
    if (response.data && response.data.code === 200) {
      merchant.value = response.data.data;
      console.log('获取到的商家信息:', merchant.value);
      
      if (merchant.value) {
        // 使用API返回的商家名称
        storeName.value = merchant.value.storeName || user.value.username + "的店铺";
        
        // 成功获取商家信息后加载商品
        await loadMerchantProducts();
      } else {
        console.error('未找到与用户关联的商家信息');
        // 如果没有商家信息，可以使用默认值
        merchant.value = {
          id: userId,
          userId: userId,
          storeName: user.value.username + "的店铺",
          description: "欢迎来到我的校园店铺！",
          level: "普通商家",
          createTime: new Date().toISOString()
        };
        storeName.value = merchant.value.storeName;
      }
    } else {
      console.error('获取商家信息失败:', response);
      // 使用默认值
      merchant.value = {
        id: userId,
        userId: userId,
        storeName: user.value.username + "的店铺",
        description: "欢迎来到我的校园店铺！",
        level: "普通商家",
        createTime: new Date().toISOString()
      };
      storeName.value = merchant.value.storeName;
    }
  } catch (error) {
    console.error('获取商家信息失败:', error);
    if (error.response) {
      console.error('服务器返回错误:', error.response.status, error.response.data);
    }
    // 发生错误时使用默认值
    merchant.value = {
      id: userId,
      userId: userId,
      storeName: user.value.username + "的店铺",
      description: "欢迎来到我的校园店铺！",
      level: "普通商家",
      createTime: new Date().toISOString()
    };
    storeName.value = merchant.value.storeName;
  } finally {
    isLoadingMerchant.value = false;
    
    // 如果此时已经有商家ID，尝试加载商品
    if (merchant.value && merchant.value.id) {
      await loadMerchantProducts();
    }
  }
};

// 加载商家商品
const loadMerchantProducts = async () => {
  if (!user.value || !user.value.userId) {
    console.error('未找到有效的用户ID');
    return;
  }

  try {
    console.log('加载商家商品，用户ID:', user.value.userId);
    
    // 检查用户是否有merchant数据
    if (!merchant.value || !merchant.value.id) {
      console.error('商家信息不完整，无法获取商品');
      return;
    }
    
    // 使用商家ID而非用户ID
    const merchantId = merchant.value.id;
    console.log('开始请求商家商品数据，使用merchantId:', merchantId);
    
    const response = await productApi.getMerchantProducts(merchantId);
    console.log('商家商品API响应:', response);
    
    if (response.data && response.data.code === 200) {
      // 检查数据结构，适应不同的返回格式
      if (response.data.data && Array.isArray(response.data.data.records)) {
        products.value = response.data.data.records;
      } else if (Array.isArray(response.data.data)) {
        products.value = response.data.data;
      } else {
        console.warn('无法解析商品数据格式，使用空数组');
        products.value = [];
      }
      
      productCount.value = products.value.length;
      console.log('获取商家商品成功，商品数量:', products.value.length);
      
      if (products.value.length > 0) {
        console.log('第一个商品样例:', products.value[0]);
      } else {
        console.log('商家暂无商品');
      }
    } else {
      console.error('获取商家商品失败，服务器响应:', response);
      products.value = [];
      productCount.value = 0;
    }
  } catch (error) {
    console.error('加载商家商品时发生错误:', error);
    if (error.response) {
      console.error('服务器返回错误:', error.response.status, error.response.data);
    }
    products.value = [];
    productCount.value = 0;
  }
};

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'PENDING_APPROVAL': '待审核',
    'REJECTED_RESUBMIT': '审核未通过',
    'ON_SALE': '在售',
    'LOCKED': '已锁定',
    'SOLD_OUT': '已售罄',
    'REMOVED_BY_SELLER': '已下架'
  };
  return statusMap[status] || status;
};

// 查看商品
const viewProduct = (id) => {
  router.push(`/product/${id}`);
};

// 查看所有商品
const viewProducts = () => {
  // 这里可以实现跳转到商品管理页面的逻辑
  console.log('查看所有商品');
};

// 查看订单
const viewOrders = () => {
  // 这里可以实现跳转到订单管理页面的逻辑
  console.log('查看订单');
};

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

// 页面加载时获取数据
onMounted(() => {
  fetchUserInfo();
  
  // 商家数据
  orderCount.value = 0;  // 示例数据
  rating.value = 100;    // 示例数据
});
</script>

<style scoped>
.merchant-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
}

.dashboard-header {
  margin-bottom: 30px;
}

.dashboard-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
}

.dashboard-header p {
  font-size: 16px;
  color: #666;
}

.dashboard-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background-color: white;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #4a6ee0;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.dashboard-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 40px;
}

.action-button {
  display: flex;
  align-items: center;
  background-color: white;
  border: 1px solid #e0e6f7;
  border-radius: 8px;
  padding: 12px 20px;
  font-size: 15px;
  color: #333;
  cursor: pointer;
  transition: all 0.2s ease;
  text-decoration: none;
}

.action-button:hover {
  background-color: #f0f4ff;
  border-color: #4a6ee0;
  color: #4a6ee0;
}

.action-icon {
  font-size: 18px;
  margin-right: 8px;
}

.merchant-products {
  background-color: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.merchant-products h2 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #333;
}

.product-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.product-item {
  border: 1px solid #e0e6f7;
  border-radius: 10px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.product-item:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-3px);
}

.product-image {
  height: 180px;
  overflow: hidden;
  position: relative;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.product-item:hover .product-image img {
  transform: scale(1.05);
}

.product-info {
  padding: 15px;
}

.product-info h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
  /* 文本超出省略号 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-price {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.current-price {
  font-size: 18px;
  font-weight: 700;
  color: #e53935;
  margin-right: 8px;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.product-condition {
  background-color: #ebf5ff;
  color: #4a6ee0;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
  font-weight: 500;
}

.product-status {
  font-size: 13px;
  padding: 3px 8px;
  border-radius: 4px;
  display: inline-block;
  font-weight: 500;
}

.status-pending_approval {
  background-color: #e3f2fd;
  color: #2196f3;
}

.status-rejected_resubmit {
  background-color: #ffebee;
  color: #f44336;
}

.status-on_sale {
  background-color: #e8f5e9;
  color: #4caf50;
}

.status-locked {
  background-color: #fffde7;
  color: #fbc02d;
}

.status-sold_out {
  background-color: #f5f5f5;
  color: #9e9e9e;
}

.status-removed_by_seller {
  background-color: #efebe9;
  color: #795548;
}

.product-actions {
  display: flex;
  padding: 10px 15px;
  border-top: 1px solid #e0e6f7;
  background-color: #f8f9fa;
}

.edit-button, .view-button {
  flex: 1;
  padding: 8px 0;
  font-size: 14px;
  text-align: center;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.edit-button {
  background-color: #4a6ee0;
  color: white;
  margin-right: 8px;
  text-decoration: none;
}

.edit-button:hover {
  background-color: #3d5eca;
}

.view-button {
  background-color: white;
  color: #4a6ee0;
  border: 1px solid #4a6ee0;
}

.view-button:hover {
  background-color: #f0f4ff;
}

.merchant-products-empty {
  background-color: white;
  border-radius: 12px;
  padding: 50px 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.empty-icon {
  font-size: 60px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-state h3 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
}

.empty-state p {
  font-size: 15px;
  color: #666;
  margin-bottom: 20px;
  max-width: 400px;
}

.publish-button {
  background-color: #4a6ee0;
  color: white;
  padding: 12px 25px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.2s ease;
  text-decoration: none;
}

.publish-button:hover {
  background-color: #3d5eca;
  transform: translateY(-2px);
}

@media (max-width: 768px) {
  .product-list {
    grid-template-columns: 1fr;
  }
  
  .dashboard-stats {
    grid-template-columns: 1fr;
  }
}

.merchant-info {
  margin-top: 10px;
}

.merchant-description {
  color: #666;
  margin: 5px 0 10px;
}

.merchant-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #888;
}

.merchant-level {
  color: #4a6ee0;
  font-weight: 500;
}
</style> 