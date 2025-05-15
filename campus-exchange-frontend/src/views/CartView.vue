<template>
  <div class="cart-container">
    <div class="container py-5">
      <h2 class="mb-4">我的购物车</h2>
      
      <!-- 加载中状态 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">加载中...</span>
        </div>
        <p class="mt-2">正在加载购物车数据...</p>
      </div>
      
      <!-- 空购物车状态 -->
      <div v-else-if="!cartItems.length" class="text-center py-5">
        <i class="fas fa-shopping-cart fa-4x text-muted mb-3"></i>
        <h4>购物车空空如也</h4>
        <p class="text-muted">快去选购心仪的商品吧</p>
        <router-link to="/" class="btn btn-primary mt-3">
          <i class="fas fa-shopping-bag me-2"></i>去购物
        </router-link>
      </div>
      
      <!-- 购物车内容 -->
      <div v-else class="cart-content">
        <!-- 商品列表 -->
        <div class="card mb-4">
          <div class="card-header d-flex justify-content-between align-items-center">
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="selectAll" 
                     v-model="selectAll" @change="toggleSelectAll">
              <label class="form-check-label fw-bold" for="selectAll">全选</label>
            </div>
            <div class="cart-header-info">
              <span class="text-muted me-3">单价</span>
              <span class="text-muted me-3">数量</span>
              <span class="text-muted me-3">小计</span>
              <span class="text-muted">操作</span>
            </div>
          </div>
          
          <!-- 按商家分组显示 -->
          <div v-for="(group, merchantId) in groupedCartItems" :key="merchantId" class="merchant-group pb-3">
            <div class="merchant-header p-3 border-bottom bg-light">
              <div class="form-check">
                <input class="form-check-input" type="checkbox" 
                       :id="'merchant-' + merchantId"
                       v-model="merchantSelected[merchantId]"
                       @change="toggleMerchantItems(merchantId)">
                <label class="form-check-label" :for="'merchant-' + merchantId">
                  <i class="fas fa-store me-2"></i>{{ group.merchantName || '未知商家' }}
                </label>
              </div>
            </div>
            
            <!-- 该商家的商品 -->
            <div v-for="item in group.items" :key="item.id" class="cart-item p-3 border-bottom">
              <div class="row align-items-center">
                <div class="col-md-6 d-flex align-items-center">
                  <div class="form-check me-3">
                    <input class="form-check-input" type="checkbox" 
                           :id="'item-' + item.id"
                           v-model="item.selected"
                           @change="updateItemSelected(item)">
                    <label class="form-check-label" :for="'item-' + item.id"></label>
                  </div>
                  <div class="product-info d-flex align-items-center">
                    <img :src="item.productImage || 'https://via.placeholder.com/80/e0e0e0/666666?text=商品图片'" class="product-image me-3" :alt="item.productName">
                    <div>
                      <div class="product-name">{{ item.productName }}</div>
                      <div class="text-muted small" v-if="!item.inStock">
                        <span class="text-danger">库存不足</span>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="col-md-6 d-flex align-items-center justify-content-between">
                  <div class="product-price">¥{{ item.price }}</div>
                  <div class="quantity-control">
                    <button class="btn btn-sm btn-outline-secondary" 
                            @click="decreaseQuantity(item)"
                            :disabled="item.quantity <= 1">-</button>
                    <input type="number" min="1" v-model.number="item.quantity" 
                           class="form-control quantity-input"
                           @change="updateQuantity(item)">
                    <button class="btn btn-sm btn-outline-secondary" 
                            @click="increaseQuantity(item)"
                            :disabled="item.quantity >= item.stock">+</button>
                  </div>
                  <div class="product-subtotal text-danger fw-bold">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
                  <div class="product-actions">
                    <button class="btn btn-link text-danger" @click="removeItem(item)">
                      <i class="fas fa-trash"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 结算栏 -->
        <div class="card checkout-bar">
          <div class="card-body d-flex justify-content-between align-items-center">
            <div class="checkout-info">
              <span class="me-4">已选商品 <strong>{{ selectedCount }}</strong> 件</span>
              <span>合计：<strong class="text-danger fs-4">¥{{ totalPrice.toFixed(2) }}</strong></span>
            </div>
            <button class="btn btn-danger btn-lg" 
                    @click="checkout" 
                    :disabled="selectedCount === 0">
              结算
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { cartApi } from '@/api/cart';

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

// 结算
const checkout = () => {
  // 检查是否有选中的商品
  if (selectedCount.value === 0) {
    ElMessage.warning('请选择要结算的商品');
    return;
  }
  
  // 检查是否有库存不足的商品
  const invalidItems = cartItems.value.filter(item => item.selected && !item.inStock);
  if (invalidItems.length > 0) {
    ElMessage.error('部分商品库存不足，请修改数量或取消选择');
    return;
  }
  
  // 标记为从购物车创建订单
  localStorage.setItem('orderFromCart', 'true');
  
  // 跳转到订单确认页面
  router.push('/order/confirm');
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
  padding: 20px 0;
}

.cart-content {
  max-width: 1000px;
  margin: 0 auto;
}

.card {
  border: none;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  background-color: white;
  border-bottom: 1px solid rgba(0,0,0,0.1);
  padding: 1rem;
}

.cart-header-info {
  width: 50%;
  display: flex;
  justify-content: space-between;
}

.merchant-header {
  background-color: #f8f9fa;
  transition: all 0.2s ease;
}

.merchant-header:hover {
  background-color: #f0f4ff;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #eee;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
  transition: all 0.3s ease;
}

.product-image:hover {
  transform: scale(1.05);
  box-shadow: 0 3px 8px rgba(0,0,0,0.1);
}

.product-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.quantity-control {
  display: flex;
  align-items: center;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  overflow: hidden;
}

.quantity-control button {
  border: none;
  background-color: #f8f9fa;
  padding: 5px 12px;
  font-weight: bold;
  transition: all 0.2s;
}

.quantity-control button:hover {
  background-color: #e9ecef;
}

.quantity-input {
  width: 50px;
  text-align: center;
  border: none;
  border-left: 1px solid #dee2e6;
  border-right: 1px solid #dee2e6;
  padding: 5px 0;
}

.product-price, .product-subtotal {
  font-weight: 600;
}

.product-subtotal {
  color: #dc3545;
}

.checkout-bar {
  position: sticky;
  bottom: 20px;
  z-index: 100;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.checkout-bar:hover {
  box-shadow: 0 -4px 15px rgba(0,0,0,0.15);
}

.btn-danger {
  background-color: #ff4757;
  border-color: #ff4757;
  transition: all 0.3s ease;
  padding: 10px 25px;
}

.btn-danger:hover {
  background-color: #f03a47;
  border-color: #f03a47;
  transform: translateY(-2px);
  box-shadow: 0 5px 10px rgba(255, 71, 87, 0.3);
}

.cart-item {
  transition: background-color 0.2s;
}

.cart-item:hover {
  background-color: #f8faff;
}

.form-check-input:checked {
  background-color: #4a6ee0;
  border-color: #4a6ee0;
}
</style> 