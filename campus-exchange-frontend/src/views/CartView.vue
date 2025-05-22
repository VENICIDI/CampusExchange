<template>
  <div class="cart-container">
    <div class="cart-header">
      <h2>我的购物车</h2>
      <div class="cart-actions">
        <router-link to="/" class="btn-secondary">
          <i class="fas fa-arrow-left me-1"></i> 继续购物
        </router-link>
      </div>
    </div>
      
      <!-- 加载中状态 -->
    <div v-if="loading" class="text-center py-3">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">加载中...</span>
        </div>
        <p class="mt-2">正在加载购物车数据...</p>
      </div>
      
      <!-- 空购物车状态 -->
    <div v-else-if="!cartItems.length" class="empty-cart">
        <i class="fas fa-shopping-cart fa-4x text-muted mb-3"></i>
        <h4>购物车空空如也</h4>
        <p class="text-muted">快去选购心仪的商品吧</p>
        <router-link to="/" class="btn btn-primary mt-3">
          <i class="fas fa-shopping-bag me-2"></i>去购物
        </router-link>
      </div>
      
      <!-- 购物车内容 -->
      <div v-else class="cart-content">
      <!-- 购物车表格 -->
      <div class="cart-table">
        <div class="cart-table-header">
          <div class="table-cell checkbox-cell">
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="selectAll" 
                     v-model="selectAll" @change="toggleSelectAll">
              <label class="form-check-label" for="selectAll">全选</label>
            </div>
          </div>
          <div class="table-cell product-cell">商品信息</div>
          <div class="table-cell price-cell">单价</div>
          <div class="table-cell quantity-cell">数量</div>
          <div class="table-cell subtotal-cell">小计</div>
          <div class="table-cell action-cell">操作</div>
          </div>
          
          <!-- 按商家分组显示 -->
        <div v-for="(group, merchantId) in groupedCartItems" :key="merchantId" class="merchant-group">
          <div class="merchant-header">
            <div class="shop-title">
                  <i class="fas fa-store me-2"></i>{{ group.merchantName || '未知商家' }}
              </div>
            </div>
            
          <div class="shop-items-container">
            <!-- 该商家的商品 -->
            <div v-for="item in group.items" :key="item.id" class="cart-table-row">
              <div class="table-cell checkbox-cell">
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" 
                           :id="'item-' + item.id"
                           v-model="item.selected"
                           @change="updateItemSelected(item)">
                </div>
                  </div>
              
              <div class="table-cell product-cell">
                <div class="product-info">
                  <img :src="item.productImage || '/images/default-product.png'" class="product-image" :alt="item.productName" @error="handleImageError">
                  <div class="product-details">
                      <div class="product-name">{{ item.productName }}</div>
                      <div class="text-muted small" v-if="!item.inStock">
                      <span class="stock-warning">库存不足</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="table-cell price-cell">
                <span class="product-price">¥{{ item.price }}</span>
              </div>
              
              <div class="table-cell quantity-cell">
                  <div class="quantity-control">
                  <button class="btn-quantity decrease" 
                            @click="decreaseQuantity(item)"
                            :disabled="item.quantity <= 1">-</button>
                    <input type="number" min="1" v-model.number="item.quantity" 
                         class="quantity-input"
                           @change="updateQuantity(item)">
                  <button class="btn-quantity increase" 
                            @click="increaseQuantity(item)"
                            :disabled="item.quantity >= item.stock">+</button>
                  </div>
              </div>
              
              <div class="table-cell subtotal-cell">
                <span class="product-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
              </div>
              
              <div class="table-cell action-cell">
                <button class="btn-remove" @click="removeItem(item)">
                  <i class="fas fa-trash-alt"></i> 删除
                    </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 结算栏 -->
      <div class="checkout-bar">
        <div class="selected-info">
          <span class="me-3">已选商品 <strong>{{ selectedCount }}</strong> 件</span>
        </div>
        <div class="price-info">
          <span>合计：<strong class="total-price">¥{{ totalPrice.toFixed(2) }}</strong></span>
            </div>
        <button class="btn-checkout" 
                    @click="checkout" 
                    :disabled="selectedCount === 0">
              结算
            </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { cartApi } from '@/api/cart';
import { orderApi } from '@/api/all';

const router = useRouter();
const loading = ref(true);
const cartItems = ref([]);
const merchantSelected = reactive({});

// 计算是否全选
const selectAll = computed({
  get: () => {
    return cartItems.value.length > 0 && cartItems.value.every(item => item.selected);
  },
  set: (value) => {
    // 在toggleSelectAll中处理
  }
});

// 计算选中的商品数量
const selectedCount = computed(() => {
  return cartItems.value.filter(item => item.selected).length;
});

// 计算总价
const totalPrice = computed(() => {
  return cartItems.value
    .filter(item => item.selected)
    .reduce((total, item) => total + (item.price * item.quantity), 0);
});

// 按商家分组的商品
const groupedCartItems = computed(() => {
  const groups = {};
  
  cartItems.value.forEach(item => {
    const merchantId = item.merchantId || 'unknown';
    
    if (!groups[merchantId]) {
      groups[merchantId] = {
        merchantName: item.merchantName || '未知商家',
        items: []
      };
    }
    
    groups[merchantId].items.push(item);
  });
  
  return groups;
});

// 获取购物车数据
const fetchCartItems = async () => {
  loading.value = true;
  
  // 检查登录状态
  const userJson = localStorage.getItem('user');
  console.log('检查登录状态:', userJson ? '已登录' : '未登录');
  if (userJson) {
    try {
      const userData = JSON.parse(userJson);
      console.log('用户数据:', userData);
      // 测试请求头
      try {
        console.log('测试请求头...');
        const response = await cartApi.testHeaders();
        console.log('请求头测试结果:', response.data);
      } catch (error) {
        console.error('测试请求头失败:', error);
      }
    } catch (e) {
      console.error('解析用户数据失败:', e);
    }
  } else {
    console.warn('用户未登录，无法获取购物车数据');
  }
  
  try {
    const response = await cartApi.getCartItems();
    if (response.data && response.data.code === 200) {
      cartItems.value = response.data.data || [];
      
      // 初始化商家选中状态
      const merchantGroups = groupBy(cartItems.value, 'merchantId');
      Object.keys(merchantGroups).forEach(merchantId => {
        merchantSelected[merchantId] = merchantGroups[merchantId].every(item => item.selected);
      });
    } else {
      ElMessage.error('获取购物车失败：' + (response.data?.message || '未知错误'));
    }
  } catch (err) {
    console.error('获取购物车出错:', err);
    ElMessage.error('获取购物车失败：' + (err.message || '网络错误'));
  } finally {
    loading.value = false;
  }
};

// 全选/取消全选
const toggleSelectAll = async (event) => {
  const selected = event.target.checked;
  
  // 更新所有商品的选中状态
  for (const item of cartItems.value) {
    if (item.selected !== selected) {
      item.selected = selected;
      await updateItemSelected(item);
    }
  }
  
  // 更新所有商家的选中状态
  Object.keys(merchantSelected).forEach(merchantId => {
    merchantSelected[merchantId] = selected;
  });
};

// 切换某个商家下所有商品的选中状态
const toggleMerchantItems = async (merchantId) => {
  const selected = merchantSelected[merchantId];
  
  // 找出该商家下的所有商品
  const merchantItems = cartItems.value.filter(item => item.merchantId === merchantId);
  
  // 更新这些商品的选中状态
  for (const item of merchantItems) {
    if (item.selected !== selected) {
      item.selected = selected;
      await updateItemSelected(item);
    }
  }
  
  // 检查是否需要更新全选状态
  const allSelected = Object.values(merchantSelected).every(val => val);
  if (selectAll.value !== allSelected) {
    selectAll.value = allSelected;
  }
};

// 更新商品选中状态
const updateItemSelected = async (item) => {
  try {
    await cartApi.updateSelected(item.id, item.selected);
    
    // 更新对应商家的选中状态
    if (item.merchantId) {
      const merchantItems = cartItems.value.filter(i => i.merchantId === item.merchantId);
      merchantSelected[item.merchantId] = merchantItems.every(i => i.selected);
    }
  } catch (err) {
    console.error('更新选中状态出错:', err);
    ElMessage.error('更新选中状态失败');
    
    // 回滚状态
    item.selected = !item.selected;
  }
};

// 增加商品数量
const increaseQuantity = (item) => {
  if (item.quantity < item.stock) {
    item.quantity++;
    updateQuantity(item);
  }
};

// 减少商品数量
const decreaseQuantity = (item) => {
  if (item.quantity > 1) {
    item.quantity--;
    updateQuantity(item);
  }
};

// 更新商品数量
const updateQuantity = async (item) => {
  try {
    await cartApi.updateQuantity(item.id, item.quantity);
  } catch (err) {
    console.error('更新数量出错:', err);
    ElMessage.error('更新数量失败');
  }
};

// 删除商品
const removeItem = async (item) => {
  try {
    await ElMessageBox.confirm('确定从购物车中删除该商品吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await cartApi.removeItem(item.id);
    
    // 从列表中移除该商品
    cartItems.value = cartItems.value.filter(i => i.id !== item.id);
    
    ElMessage.success('商品已从购物车中删除');
    
    // 更新商家选中状态
    if (item.merchantId) {
      const merchantItems = cartItems.value.filter(i => i.merchantId === item.merchantId);
      if (merchantItems.length === 0) {
        delete merchantSelected[item.merchantId];
      } else {
        merchantSelected[item.merchantId] = merchantItems.every(i => i.selected);
      }
    }
  } catch (err) {
    if (err !== 'cancel') {
      console.error('删除商品出错:', err);
      ElMessage.error('删除商品失败');
    }
  }
};

// 图片加载失败处理
const handleImageError = (event) => {  
  event.target.src = 'https://via.placeholder.com/80/e0e0e0/666666?text=商品图片';
};

// 结算
const checkout = async () => {  
  console.log('开始结算流程...');
  // 检查是否有选中的商品
  if (selectedCount.value === 0) {
    console.log('没有选中商品，无法结算');
    ElMessage.warning('请选择要结算的商品');
    return;
  }
  
  // 检查是否有库存不足的商品
  const invalidItems = cartItems.value.filter(item => item.selected && (item.quantity > item.stock || item.stock <= 0));
  console.log('库存不足的商品数量:', invalidItems.length);
  if (invalidItems.length > 0) {
    console.log('库存不足的商品:', invalidItems);
    ElMessage.error('部分商品库存不足，请修改数量或取消选择');
    return;
  }
  
  try {
    // 设置标志，从购物车创建订单
  localStorage.setItem('orderFromCart', 'true');
  
  // 跳转到订单确认页面
  router.push('/order/confirm');
    
    // 以下代码移动到订单确认页面
    /*
    // 直接从购物车创建订单
    console.log('直接从购物车创建订单');
    ElMessage.info('正在创建订单...');
    
    // 构建订单数据
    const cartOrderData = {
      tradeType: 'EXPRESS', // 默认使用快递配送
      pointsUsed: 0 // 暂不支持积分抵扣
    };
    
    // 调用API从购物车创建订单
    const response = await orderApi.createOrderFromCart(cartOrderData);
    
    console.log('购物车创建订单响应:', response);
    
    if (response.data && response.data.code === 200) {
      // 获取订单号列表
      const orderNos = response.data.data;
      console.log('订单创建成功，订单号列表:', orderNos);
      
      ElMessage.success('订单创建成功');
      // 跳转到订单列表页面
      setTimeout(() => {
        router.push('/orders/user');
      }, 500);
    } else {
      ElMessage.error('创建订单失败：' + (response.data?.message || '未知错误'));
    }
    */
  } catch (err) {
    console.error('提交订单出错:', err);
    ElMessage.error('提交订单失败：' + (err.message || '网络错误'));
  }
};

// 工具函数：按属性分组
const groupBy = (array, key) => {
  return array.reduce((result, item) => {
    const keyValue = item[key] || 'unknown';
    (result[keyValue] = result[keyValue] || []).push(item);
    return result;
  }, {});
};

// 页面加载时获取购物车数据
onMounted(() => {
  fetchCartItems();
});
</script>

<style scoped>
.cart-container {
  min-height: 90vh;
  background-color: #f8f9fa;
  padding: 20px 30px;
  max-width: 1200px;
  margin: 0 auto;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border-bottom: 1px solid #e4e4e4;
  padding-bottom: 15px;
}

.cart-header h2 {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.cart-actions {
  display: flex;
  gap: 10px;
}

.btn-secondary {
  padding: 8px 16px;
  background-color: #f0f0f0;
  color: #333;
  border: none;
  border-radius: 4px;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-secondary:hover {
  background-color: #e4e4e4;
}

.empty-cart {
  text-align: center;
  padding: 60px 0;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  margin-top: 20px;
}

.cart-table {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  overflow: hidden;
  margin-bottom: 20px;
}

.cart-table-header {
  display: flex;
  background-color: #f5f7fa;
  color: #606266;
  font-weight: bold;
  padding: 15px 10px;
  border-bottom: 1px solid #e4e4e4;
}

.table-cell {
  padding: 0 10px;
  display: flex;
  align-items: center;
}

.checkbox-cell {
  width: 5%;
  justify-content: center;
}

.product-cell {
  width: 40%;
}

.price-cell {
  width: 15%;
  justify-content: center;
}

.quantity-cell {
  width: 15%;
  justify-content: center;
}

.subtotal-cell {
  width: 15%;
  justify-content: center;
}

.action-cell {
  width: 10%;
  justify-content: center;
}

.merchant-group {
  margin-bottom: 15px;
  border: 1px solid #e4e4e4;
  border-radius: 4px;
  overflow: hidden;
}

.merchant-header {
  padding: 12px 15px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e4e4;
  font-weight: 500;
  color: #333;
  position: relative;
}

.shop-title {
  display: flex;
  align-items: center;
  font-weight: 600;
  color: #303133;
  font-size: 15px;
}

.shop-title i {
  color: #4a6ee0;
}

.shop-items-container {
  background-color: #ffffff;
}

.cart-table-row {
  display: flex;
  padding: 15px 10px;
  border-bottom: 1px solid #e4e4e4;
  transition: background-color 0.2s;
}

.cart-table-row:hover {
  background-color: #f9f9f9;
}

.cart-table-row:last-child {
  border-bottom: none;
}

.product-info {
  display: flex;
  align-items: center;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eee;
  margin-right: 15px;
}

.product-details {
  flex: 1;
}

.product-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stock-warning {
  color: #f56c6c;
  font-size: 12px;
  background-color: #fef0f0;
  padding: 2px 6px;
  border-radius: 2px;
}

.product-price {
  color: #606266;
  font-weight: 500;
}

.quantity-control {
  display: flex;
  align-items: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  width: 120px;
}

.btn-quantity {
  width: 36px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border: none;
  cursor: pointer;
  user-select: none;
}

.btn-quantity:hover {
  background: #e4e7ed;
}

.btn-quantity:disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}

.quantity-input {
  width: 48px;
  height: 32px;
  text-align: center;
  border: none;
  border-left: 1px solid #dcdfe6;
  border-right: 1px solid #dcdfe6;
  outline: none;
}

.product-subtotal {
  color: #f56c6c;
  font-weight: 600;
}

.btn-remove {
  color: #909399;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  padding: 6px 10px;
  transition: all 0.2s;
}

.btn-remove:hover {
  color: #f56c6c;
}

.checkout-bar {
  background-color: white;
  padding: 15px 20px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  position: sticky;
  bottom: 20px;
}

.selected-info {
  margin-right: 20px;
  color: #606266;
}

.price-info {
  margin-right: 20px;
  color: #606266;
}

.total-price {
  font-size: 20px;
  color: #f56c6c;
}

.btn-checkout {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 10px 25px;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-checkout:hover {
  background-color: #f78989;
}

.btn-checkout:disabled {
  background-color: #fab6b6;
  cursor: not-allowed;
}

.form-check-input:checked {
  background-color: #409eff;
  border-color: #409eff;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .cart-container {
    padding: 15px;
  }
  
  .cart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .cart-table-header {
    display: none;
  }
  
  .cart-table-row {
    flex-wrap: wrap;
    padding: 10px;
  }
  
  .table-cell {
    padding: 5px;
  }
  
  .checkbox-cell {
    width: 10%;
  }
  
  .product-cell {
    width: 90%;
  }
  
  .price-cell, .quantity-cell, .subtotal-cell, .action-cell {
    width: 50%;
    justify-content: flex-start;
    margin-top: 10px;
  }
  
  .product-image {
    width: 60px;
    height: 60px;
  }
  
  .checkout-bar {
    flex-direction: column;
    gap: 10px;
    align-items: flex-end;
  }
}
</style> 