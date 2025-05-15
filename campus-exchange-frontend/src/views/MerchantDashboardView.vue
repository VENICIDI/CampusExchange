<template>
  <div class="merchant-dashboard">
    <div class="dashboard-header">
      <h1>商家中心</h1>
      <p v-if="isLoadingMerchant">加载中...</p>
      <div v-else-if="merchant" class="merchant-info">
        <h2>{{ storeName }}</h2>
        <p class="merchant-description">{{ merchant.description }}</p>
        <div class="merchant-meta">
          <span class="merchant-level">等级: {{ merchantLevelName }}</span>
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
            <div class="product-condition-badge" :class="getConditionClass(product.productCondition)">
              {{ formatCondition(product.productCondition) }}
            </div>
          </div>
          <div class="product-info">
            <h3>{{ product.name }}</h3>
            <div class="product-price">
              <span class="current-price">¥{{ product.currentPrice }}</span>
              <span class="original-price" v-if="product.originalPrice > product.currentPrice">¥{{ product.originalPrice }}</span>
            </div>
            <div class="product-meta">
              <span class="product-status" :class="getStatusClass(product.status)">
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
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { productApi, merchantApi, orderApi } from '@/api/all';

const router = useRouter();
const storeName = ref('');
const productCount = ref(0);
const orderCount = ref(0);
const rating = ref(0);
const products = ref([]);
const user = ref(null);
const merchant = ref(null);
const isLoadingMerchant = ref(false);

// 商家等级名称
const merchantLevelName = computed(() => {
  if (!merchant.value || !merchant.value.levelId) return '普通商家';
  
  // 根据商家等级ID获取对应的名称
  const levelMap = {
    1: '钻石商家',
    2: '金牌商家',
    3: '银牌商家',
    4: '铜牌商家',
    5: '普通商家'
  };
  
  return levelMap[merchant.value.levelId] || '未知商家';
});

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
        
        // 设置商家评分
        rating.value = merchant.value.storePositiveRate || 100;
        
        // 成功获取商家信息后加载商品
        await loadMerchantProducts();
        
        // 加载商家订单数量
        await loadMerchantOrderCount();
      } else {
        console.error('未找到与用户关联的商家信息');
        // 如果没有商家信息，可以使用默认值
        merchant.value = {
          id: userId,
          userId: userId,
          storeName: user.value.username + "的店铺",
          description: "欢迎来到我的校园店铺！",
          levelId: 5,
          createTime: new Date().toISOString()
        };
        storeName.value = merchant.value.storeName;
        rating.value = 100;
      }
    } else {
      console.error('获取商家信息失败:', response);
      // 使用默认值
      merchant.value = {
        id: userId,
        userId: userId,
        storeName: user.value.username + "的店铺",
        description: "欢迎来到我的校园店铺！",
        levelId: 5,
        createTime: new Date().toISOString()
      };
      storeName.value = merchant.value.storeName;
      rating.value = 100;
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
      levelId: 5,
      createTime: new Date().toISOString()
    };
    storeName.value = merchant.value.storeName;
    rating.value = 100;
  } finally {
    isLoadingMerchant.value = false;
    
    // 如果此时已经有商家ID，尝试加载商品
    if (merchant.value && merchant.value.id) {
      await loadMerchantProducts();
      await loadMerchantOrderCount();
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
        console.log('商品新旧程度实际值:', products.value[0].productCondition);
        console.log('所有商品新旧程度值:');
        products.value.forEach((product, index) => {
          console.log(`商品${index+1} (${product.name}) 新旧程度:`, product.productCondition);
        });
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

// 加载商家订单数量
const loadMerchantOrderCount = async () => {
  if (!merchant.value || !merchant.value.id) {
    console.error('商家信息不完整，无法获取订单数量');
    return;
  }
  
  try {
    console.log('开始获取商家订单数量，商家ID:', merchant.value.id);
    
    // 使用orderApi获取商家订单数量，替换直接fetch请求
    // 这样会自动带上身份验证信息
    const response = await orderApi.getMerchantOrders({
      pageNum: 1,
      pageSize: 1
    });
    
    console.log('获取商家订单数量响应:', response);
    
    if (response.data && response.data.code === 200 && response.data.data && typeof response.data.data.total === 'number') {
      orderCount.value = response.data.data.total;
      console.log('商家订单数量:', orderCount.value);
    } else {
      console.error('获取商家订单数量失败，无法解析响应数据:', response.data);
      // 使用默认值
      orderCount.value = merchant.value.totalSalesCount || 0;
    }
  } catch (error) {
    console.error('获取商家订单数量出错:', error);
    // 使用默认值
    orderCount.value = merchant.value.totalSalesCount || 0;
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

// 获取状态对应的类名
const getStatusClass = (status) => {
  if (!status) return '';
  return `status-${status.toLowerCase()}`;
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
  router.push('/orders/merchant');
};

// 获取商品新旧程度对应的类名
const getConditionClass = (condition) => {
  if (!condition) return 'condition-unknown';
  
  // 检查condition是否是对象（有些API可能返回枚举对象）
  if (typeof condition === 'object' && condition !== null) {
    // 尝试从对象中获取可能的值
    condition = condition.value || condition.name || condition.code || JSON.stringify(condition);
  }
  
  // 转为字符串并转大写，确保匹配
  const conditionStr = String(condition).toUpperCase();
  
  // 使用包含判断而非精确匹配
  if (conditionStr.includes('NEW') || conditionStr.includes('全新')) return 'condition-new';
  if (conditionStr.includes('LIKE_NEW') || conditionStr.includes('九成新')) return 'condition-like-new';
  if (conditionStr.includes('GOOD') || conditionStr.includes('八成新')) return 'condition-good';
  if (conditionStr.includes('FAIR') || conditionStr.includes('七成新')) return 'condition-fair';
  if (conditionStr.includes('POOR') || conditionStr.includes('六成新')) return 'condition-poor';
  
  return 'condition-unknown';
};

// 格式化商品新旧程度
const formatCondition = (condition) => {
  if (!condition) return '未知新旧';
  
  // 检查condition是否是对象
  if (typeof condition === 'object' && condition !== null) {
    // 尝试从对象中获取值
    condition = condition.value || condition.name || condition.code || JSON.stringify(condition);
  }
  
  // 转为字符串处理
  const conditionStr = String(condition);
  
  // 判断是否已经是中文
  if (conditionStr.includes('全新') || conditionStr.includes('九成新') || 
      conditionStr.includes('八成新') || conditionStr.includes('七成新') || 
      conditionStr.includes('六成新')) {
    return conditionStr;
  }
  
  // 英文映射到中文
  const conditionMap = {
    'NEW': '全新',
    'LIKE_NEW': '九成新',
    'GOOD': '八成新',
    'FAIR': '七成新',
    'POOR': '六成新及以下'
  };
  
  // 使用包含判断
  const conditionUpper = conditionStr.toUpperCase();
  if (conditionUpper.includes('NEW') && !conditionUpper.includes('LIKE')) return '全新';
  if (conditionUpper.includes('LIKE_NEW')) return '九成新';
  if (conditionUpper.includes('GOOD')) return '八成新';
  if (conditionUpper.includes('FAIR')) return '七成新';
  if (conditionUpper.includes('POOR')) return '六成新及以下';
  
  return conditionMap[conditionUpper] || conditionStr;
};

// 页面加载时获取数据
onMounted(() => {
  fetchUserInfo();
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
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 14px;
}

.product-item {
  border: 1px solid #e0e6f7;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  background-color: white;
  position: relative;
  max-width: 100%;
}

.product-item:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-3px);
}

/* 增强品质标签样式 */
.product-condition-badge {
  position: absolute;
  left: 0;
  top: 10px;
  padding: 4px 10px 4px 8px;
  border-radius: 0 4px 4px 0;
  font-size: 13px;
  font-weight: 700;
  color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  text-shadow: 0 1px 1px rgba(0, 0, 0, 0.2);
}

/* 增强商品状态标签 */
.product-status {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 700;
  border-left: 4px solid transparent;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
  text-shadow: 0 1px 1px rgba(0, 0, 0, 0.1);
}

/* 商品新旧程度标签样式 */
.condition-new.product-condition-badge {
  background-color: #4caf50;
  border-right: 3px solid #2e7d32;
}

.condition-like-new.product-condition-badge {
  background-color: #2196f3;
  border-right: 3px solid #1565c0;
}

.condition-good.product-condition-badge {
  background-color: #3f51b5;
  border-right: 3px solid #283593;
}

.condition-fair.product-condition-badge {
  background-color: #ff9800;
  border-right: 3px solid #ef6c00;
}

.condition-poor.product-condition-badge {
  background-color: #f44336;
  border-right: 3px solid #c62828;
}

.condition-unknown.product-condition-badge {
  background-color: #9e9e9e;
  border-right: 3px solid #616161;
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

.negotiable-badge {
  position: absolute;
  right: 10px;
  top: 10px;
  background-color: #ff9800;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.product-info {
  padding: 12px;
}

.product-info h3 {
  font-size: 15px;
  line-height: 1.3;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-price {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
}

.current-price {
  font-size: 17px;
  font-weight: 700;
  color: #e53935;
  margin-right: 8px;
}

.original-price {
  font-size: 13px;
  color: #999;
  text-decoration: line-through;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.product-stats {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
}

.product-actions {
  display: flex;
  padding: 8px 12px;
  border-top: 1px solid #e0e6f7;
  background-color: #f8f9fa;
}

.edit-button, .view-button {
  flex: 1;
  padding: 7px 0;
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

/* 添加半屏幕宽度的响应式设计 */
@media (max-width: 1200px) and (min-width: 768px) {
  .product-list {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 10px;
  }
  
  .product-info h3 {
    font-size: 14px;
  }
  
  .current-price {
    font-size: 15px;
  }
  
  .product-actions {
    padding: 6px 8px;
  }
  
  .edit-button, .view-button {
    padding: 5px 0;
    font-size: 13px;
  }
}

/* 手机屏幕响应式设计 */
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

/* 商品状态标签样式 */
.status-pending_approval {
  background-color: #e3f2fd;
  color: #1976d2;
  border-left-color: #1976d2;
}

.status-rejected_resubmit {
  background-color: #ffebee;
  color: #d32f2f;
  border-left-color: #d32f2f;
}

.status-on_sale {
  background-color: #e8f5e9;
  color: #388e3c;
  border-left-color: #388e3c;
}

.status-locked {
  background-color: #fff8e1;
  color: #f57c00;
  border-left-color: #f57c00;
}

.status-sold_out {
  background-color: #f5f5f5;
  color: #616161;
  border-left-color: #616161;
}

.status-removed_by_seller {
  background-color: #efebe9;
  color: #5d4037;
  border-left-color: #5d4037;
}
</style> 