<template>
  <div class="order-confirm-container">
    <div class="container py-4 px-md-5">
      <div class="order-title">
        <h4>确认订单</h4>
      </div>
      
      <!-- 加载中状态 -->
      <div v-if="loading" class="loading-wrapper">
        <div class="spinner"></div>
        <p class="loading-text">正在准备订单信息...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="error" class="error-wrapper">
        <div class="error-message">
          <i class="fas fa-exclamation-circle me-2"></i>{{ error }}
        </div>
        <div class="back-button-wrapper">
          <button @click="goBack" class="btn-back">
            <i class="fas fa-arrow-left me-1"></i>返回上一页
          </button>
        </div>
      </div>
      
      <!-- 订单确认信息 -->
      <div v-else class="order-content">
        <!-- 收货信息 -->
        <div class="order-section delivery-section">
          <div class="section-header">
            <h5 class="section-title">收货信息</h5>
            <div class="trade-type-switch">
              <input class="switch-input" type="checkbox" id="pickupSwitch" v-model="isOfflineTrade">
              <label class="switch-label" for="pickupSwitch">线下交易</label>
            </div>
          </div>
          
          <!-- 快递配送 -->
          <div v-if="!isOfflineTrade" class="delivery-content">
            <div v-if="hasAddress" class="address-card">
              <div class="address-info">
                <div class="recipient-info">
                  <span class="recipient-name">{{ address.recipient }}</span>
                  <span class="recipient-phone">{{ address.phone }}</span>
                  <span class="default-tag">默认</span>
                </div>
                <div class="address-detail">{{ address.fullAddress }}</div>
              </div>
              <div class="address-actions">
                <button @click="openEditAddress" class="btn-edit-address">
                  <i class="fas fa-edit"></i> 修改
                </button>
              </div>
            </div>
            
            <div v-else class="no-address-card">
              <div class="no-address-tip">
                <i class="fas fa-map-marker-alt"></i>
                <span>您还没有收货地址</span>
              </div>
              <button @click="openEditAddress" class="btn-add-address">
                添加收货地址
              </button>
            </div>
            
            <!-- 编辑地址表单 -->
            <div v-if="showEditAddressForm" class="address-form">
              <div class="form-header">
                <h6 class="form-title">{{ hasAddress ? '修改地址' : '添加地址' }}</h6>
                <button @click="showEditAddressForm = false" class="btn-close-form">
                  <i class="fas fa-times"></i>
                </button>
              </div>
              <div class="form-content">
                <div class="form-row">
                  <div class="form-group">
                    <label for="recipient">收件人</label>
                    <input type="text" id="recipient" v-model="editAddress.recipient" 
                         :placeholder="userInfo.realName || '请输入收件人姓名'">
                  </div>
                  <div class="form-group">
                    <label for="phone">手机号码</label>
                    <input type="text" id="phone" v-model="editAddress.phone" 
                         :placeholder="userInfo.phone || '请输入手机号码'">
                  </div>
                </div>
                <div class="form-group">
                  <label for="fullAddress">详细地址</label>
                  <input type="text" id="fullAddress" v-model="editAddress.fullAddress" 
                         placeholder="请输入完整的收货地址">
                </div>
                <div class="form-action">
                  <button class="btn-save-address" @click="saveAddress">
                    保存地址
                  </button>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 线下交易 -->
          <div v-else class="offline-trade-form">
            <div class="form-group">
              <label for="meetingLocation">线下交易地点</label>
              <input type="text" id="meetingLocation" 
                     v-model="offlineTradeInfo.meetingLocation" 
                     placeholder="请输入详细的线下交易地点，如学校某教学楼">
            </div>
            <div class="form-group">
              <label for="meetingTime">线下交易时间</label>
              <input type="datetime-local" id="meetingTime" 
                     v-model="offlineTradeInfo.meetingTime">
              <small class="form-tip">请选择合适的交易时间</small>
            </div>
          </div>
        </div>
        
        <!-- 商品信息 -->
        <div class="order-section product-section">
          <h5 class="section-title">商品信息</h5>
          
          <!-- 从购物车结算时显示所有选中的商品 -->
          <div v-if="cartItems.length > 0" class="cart-products">
            <div v-for="item in cartItems" :key="item.id" class="product-card">
              <div class="product-image">
                <img :src="item.productImage || 'https://via.placeholder.com/80'" 
                     :alt="item.productName">
              </div>
              <div class="product-info">
                <h5 class="product-name">{{ item.productName }}</h5>
                <div class="product-seller">
                  <i class="fas fa-store-alt"></i> {{ item.merchantName }}
                </div>
                <div class="product-stock" :class="item.stock > 0 ? 'in-stock' : 'out-of-stock'">
                  <i class="fas" :class="item.stock > 0 ? 'fa-check-circle' : 'fa-times-circle'"></i>
                  {{ item.stock > 0 ? `库存充足，${item.stock}件可售` : '库存不足' }}
                </div>
              </div>
              <div class="product-price-wrapper">
                <div class="product-price">¥{{ item.price.toFixed(2) }}</div>
                <div class="quantity-info">
                  <span class="quantity-label">× {{ item.quantity }}</span>
                </div>
                <div class="item-subtotal">
                  小计: <span class="subtotal-value">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
                </div>
              </div>
            </div>
            
            <!-- 购物车商品汇总 -->
            <div class="cart-summary">
              <div class="summary-info">
                <span>共{{ cartItems.length }}种商品，合计{{ cartItems.reduce((total, item) => total + item.quantity, 0) }}件</span>
              </div>
            </div>
          </div>
          
          <!-- 直接购买时显示单个商品 -->
          <div v-else class="product-card">
            <div class="product-image">
              <img :src="orderPreview.productImage || 'https://via.placeholder.com/80'" 
                   :alt="orderPreview.productName">
            </div>
            <div class="product-info">
              <h5 class="product-name">{{ orderPreview.productName }}</h5>
              <div class="product-seller">
                <i class="fas fa-store-alt"></i> {{ orderPreview.sellerName }}
              </div>
              <div class="product-stock" :class="stockStatus.class">
                <i class="fas" :class="stockStatus.hasStock ? 'fa-check-circle' : 'fa-times-circle'"></i>
                {{ stockStatus.text }}
              </div>
            </div>
            <div class="product-price-wrapper">
              <div class="product-price">¥{{ orderPreview.price.toFixed(2) }}</div>
              <div class="quantity-selector">
                <button class="quantity-btn minus" 
                        @click="updateQuantity(quantity - 1)" 
                        :disabled="quantity <= 1">−</button>
                <input type="text" class="quantity-input" v-model.number="quantity"
                       @change="updateQuantity(quantity)" />
                <button class="quantity-btn plus" 
                        @click="updateQuantity(quantity + 1)" 
                        :disabled="quantity >= maxQuantity">+</button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 支付信息 -->
        <div class="order-section payment-section">
          <h5 class="section-title">支付信息</h5>
          <div class="payment-card">
            <div class="payment-item">
              <span class="payment-label">商品金额</span>
              <span class="payment-value">¥{{ (orderPreview.price * quantity).toFixed(2) }}</span>
            </div>
            <div class="payment-item">
              <span class="payment-label">运费</span>
              <span class="payment-value">¥{{ isOfflineTrade ? '0.00' : shippingFee.toFixed(2) }}</span>
            </div>
            <!-- 积分抵扣选项 -->
            <div class="points-section" v-if="userPoints > 0">
              <div class="points-title-row">
                <span class="section-title">积分抵扣</span>
                <span class="user-points">当前可用积分: {{ userPoints }}</span>
              </div>
              <div class="points-control">
                <el-checkbox v-model="usePoints" @change="handlePointsChange">使用积分</el-checkbox>
                <div class="points-input-group" v-if="usePoints">
                  <el-input-number 
                    v-model="pointsUsed" 
                    :min="1" 
                    :max="maxAvailablePoints"
                    size="small"
                    @change="checkPointsLimit"
                  />
                  <span class="points-hint">
                    最多可用 {{ maxAvailablePoints }} 积分，抵扣 ¥{{ (maxAvailablePoints / 100).toFixed(2) }}
                  </span>
                </div>
              </div>
            </div>
            <div class="payment-total">
              <span class="total-label">实付金额</span>
              <span class="total-value">¥{{ totalAmount.toFixed(2) }}</span>
            </div>
          </div>
        </div>
        
        <!-- 支付说明 -->
        <div class="order-section note-section">
          <div class="payment-note">
            <i class="fas fa-info-circle"></i>
            <span>订单创建后将进入<strong>待付款</strong>状态，您需要在订单页面完成支付</span>
          </div>
        </div>
        
        <!-- 提交订单 -->
        <div class="order-submit-section">
          <div class="order-summary">
            <span class="order-count">共{{ quantity }}件商品</span>
            <span class="order-amount">合计：<strong>¥{{ totalAmount.toFixed(2) }}</strong></span>
          </div>
          <button class="btn-submit" 
                  @click="submitOrder" 
                  :disabled="submitting || !canSubmit">
            <span v-if="submitting">
              <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
              提交中...
            </span>
            <span v-else>
              提交订单
            </span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { orderApi, userApi, productApi, cartApi } from '@/api/all';
import { getUserWalletAPI } from '@/api/wallet';
import { ElMessage } from 'element-plus';

const router = useRouter();
const loading = ref(true);
const error = ref(null);
const submitting = ref(false);

// 订单预览信息（从本地存储获取）
const orderPreview = ref({});
// 购物车商品列表
const cartItems = ref([]);

// 用户信息
const userInfo = ref({
  realName: '',
  phone: '',
});

// 商品数量相关
const quantity = ref(1); // 默认购买数量为1
const maxQuantity = ref(1); // 最大可购买数量（基于库存）

// 检查商品库存状态
const stockStatus = computed(() => {
  if (!orderPreview.value.stock) {
    return { hasStock: false, text: '库存不足', class: 'out-of-stock' };
  }
  
  if (orderPreview.value.stock <= 5) {
    return { hasStock: true, text: `库存紧张，仅剩${orderPreview.value.stock}件`, class: 'low-stock' };
  }
  
  return { hasStock: true, text: `库存充足，${orderPreview.value.stock}件可售`, class: 'in-stock' };
});

// 判断是否可以提交订单
const canSubmit = computed(() => {
  // 首先检查库存是否充足
  if (!orderPreview.value.stock || orderPreview.value.stock < quantity.value) {
    return false;
  }

  if (isOfflineTrade.value) {
    // 验证线下交易信息，包括日期格式
    return offlineTradeInfo.meetingLocation && 
           offlineTradeInfo.meetingTime && 
           validateDate(offlineTradeInfo.meetingTime);
  } else {
    return address.value.recipient && address.value.phone && address.value.fullAddress;
  }
});

// 收货地址（只保留一个）
const address = ref({
  recipient: '',
  phone: '',
  fullAddress: ''
});

// 编辑地址表单
const showEditAddressForm = ref(false);
const editAddress = reactive({
  recipient: '',
  phone: '',
  fullAddress: ''
});

// 线下交易信息
const isOfflineTrade = ref(false);
const offlineTradeInfo = reactive({
  meetingLocation: '',
  meetingTime: ''
});

// 费用信息
const shippingFee = ref(0); // 运费
const serviceFee = ref(0); // 平台服务费

// 积分相关
const usePoints = ref(false);
const pointsUsed = ref(0);
const userPoints = ref(0);
const maxAvailablePoints = computed(() => {
  // 计算订单总金额
  let orderAmount = 0;
  if (cartItems.value.length > 0) {
    // 购物车结算时计算所有商品总价
    orderAmount = cartItems.value.reduce((total, item) => total + (item.price * item.quantity), 0);
  } else {
    // 直接购买时计算单个商品总价
    orderAmount = orderPreview.value.price * quantity.value;
  }
  
  // 最大可用积分：订单金额的100倍（100积分=1元）
  const maxPoints = Math.floor(orderAmount * 100);
  return Math.min(userPoints.value, maxPoints);
});

// 检查积分使用限制
const checkPointsLimit = () => {
  if (pointsUsed.value > maxAvailablePoints.value) {
    pointsUsed.value = maxAvailablePoints.value;
  }
};

// 处理积分勾选变化
const handlePointsChange = (val) => {
  if (val) {
    // 默认使用最大可用积分
    pointsUsed.value = maxAvailablePoints.value;
  } else {
    pointsUsed.value = 0;
  }
};

// 计算积分抵扣金额（100积分=1元）
const pointsDeduction = computed(() => {
  if (!usePoints.value || pointsUsed.value <= 0) return 0;
  return pointsUsed.value / 100; // 100积分=1元
});

// 计算总金额
const totalAmount = computed(() => {
  // 购物车结算时，计算所有购物车商品的总价
  if (cartItems.value.length > 0) {
    const productTotal = cartItems.value.reduce((total, item) => total + (item.price * item.quantity), 0);
    const shipping = isOfflineTrade.value ? 0 : shippingFee.value;
    return productTotal + shipping - pointsDeduction.value;
  } else {
    // 直接购买时计算单个商品的总价
    const productTotal = orderPreview.value.price * quantity.value;
    const shipping = isOfflineTrade.value ? 0 : shippingFee.value;
    return productTotal + shipping - pointsDeduction.value;
  }
});

// 判断是否有收货地址
const hasAddress = computed(() => {
  return address.value.recipient && address.value.phone && address.value.fullAddress;
});

// 日期验证函数
const validateDate = (dateTimeStr) => {
  if (!dateTimeStr) return false;
  
  // 检查是否是有效的日期时间格式
  const date = new Date(dateTimeStr);
  if (isNaN(date.getTime())) return false;
  
  // 检查年份是否合理 (2000-2099)
  const year = date.getFullYear();
  if (year < 2000 || year > 2099) return false;
  
  return true;
};

// 格式化日期时间为后端需要的格式 yyyy-MM-dd HH:mm:ss
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '';
  
  const date = new Date(dateTimeStr);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hours}:${minutes}:00`;
};

// 获取用户积分
const fetchUserPoints = async () => {
  try {
    // 从钱包API获取积分信息
    const response = await getUserWalletAPI();
    if (response.data && response.data.code === 200) {
      userPoints.value = response.data.data.points || 0;
      console.log('获取到用户积分:', userPoints.value);
      
      // 默认不使用积分
      usePoints.value = false;
      pointsUsed.value = 0;
    }
  } catch (err) {
    console.error('获取用户积分失败:', err);
  }
};

// 加载数据
const loadData = async () => {
  loading.value = true;
  error.value = null;
  
  try {
    // 判断是否从购物车来
    const isFromCart = localStorage.getItem('orderFromCart') === 'true';
    console.log('是否从购物车进入订单页面:', isFromCart);
    
    if (isFromCart) {
      console.log('从购物车创建订单，加载购物车数据');
      // 从购物车加载数据
      try {
        const response = await cartApi.getCartItems();
        if (response.data && response.data.code === 200) {
          // 获取已选中的购物车商品
          cartItems.value = response.data.data.filter(item => item.selected) || [];
          console.log('已选中的购物车商品:', cartItems.value);
          
          if (cartItems.value.length === 0) {
            error.value = '购物车中没有选中的商品，请返回购物车选择商品';
            return;
          }
          
          // 初始化订单预览信息
          const firstItem = cartItems.value[0];
          orderPreview.value = {
            productId: firstItem.productId,
            productName: '购物车结算',
            productImage: firstItem.productImage,
            price: cartItems.value.reduce((total, item) => total + (item.price * item.quantity), 0),
            quantity: 1, // 购物车结算时数量固定为1
            stock: 999, // 购物车商品已经检查过库存，这里设置一个大值
            sellerId: firstItem.merchantId,
            sellerName: firstItem.merchantName || '多商家'
          };
        } else {
          error.value = '获取购物车数据失败: ' + (response.data?.message || '未知错误');
          return;
        }
      } catch (err) {
        console.error('获取购物车数据失败:', err);
        error.value = '获取购物车数据失败，请返回重试';
        return;
      }
    } else {
    const previewData = localStorage.getItem('orderPreview');
    if (!previewData) {
        console.error('订单信息不存在');
      error.value = '订单信息不存在，请重新选择商品';
      return;
    }
    
    orderPreview.value = JSON.parse(previewData);
      console.log('解析的订单预览数据:', orderPreview.value);
    }
    
    // 设置最大可购买数量
    if (orderPreview.value.stock) {
      maxQuantity.value = orderPreview.value.stock;
      // 确保当前数量不超过库存
      if (quantity.value > maxQuantity.value) {
        quantity.value = maxQuantity.value;
      }
    } else {
      // 如果没有库存信息，尝试从后端获取最新商品信息
      try {
        // 检查商品ID是否有效
        if (!orderPreview.value.productId) {
          console.error('商品ID无效:', orderPreview.value.productId);
          error.value = '商品信息不完整，请返回商品详情页重新购买';
          loading.value = false;
          return;
        }

        const productResponse = await productApi.getProductById(orderPreview.value.productId);
        if (productResponse.data && productResponse.data.code === 200) {
          const productData = productResponse.data.data;
          // 更新库存信息
          orderPreview.value.stock = productData.stock;
          maxQuantity.value = productData.stock;
          // 确保数量不超过库存
          if (quantity.value > maxQuantity.value) {
            quantity.value = maxQuantity.value;
          }
        }
      } catch (err) {
        console.error('获取商品库存信息失败:', err);
        error.value = '获取商品信息失败，请返回重试';
      }
    }
    
    // 从后端获取当前用户的信息和收货地址
    try {
      const response = await userApi.getUserProfile();
      if (response.data && response.data.code === 200) {
        const userData = response.data.data;
        if (userData) {
          // 保存用户真实姓名和手机号
          userInfo.value = {
            realName: userData.realName || '',
            phone: userData.phone || ''
          };
          
          // 预填充用户真实姓名和手机号到编辑表单
          editAddress.recipient = userData.realName || '';
          editAddress.phone = userData.phone || '';
          
          // 如果有默认地址，则解析
          if (userData.defaultAddress) {
            // 直接用逗号分隔的形式保存地址
            const addressParts = userData.defaultAddress.split(',');
            if (addressParts.length >= 3) {
              address.value = {
                recipient: addressParts[0] || userData.realName || '',
                phone: addressParts[1] || userData.phone || '',
                fullAddress: addressParts[2] || ''
              };
            }
          }
        }
      }
    } catch (err) {
      console.error('获取用户信息失败:', err);
      // 失败时不阻止页面加载，允许用户手动添加地址
    }
    
    // 设置费用
    shippingFee.value = 0; // 假设免运费
      
    // 获取用户积分信息
    await fetchUserPoints();
    
  } catch (err) {
    console.error('加载订单数据出错:', err);
    error.value = '加载订单数据出错，请重试';
  } finally {
    loading.value = false;
  }
};

// 编辑地址
const openEditAddress = () => {
  // 将当前地址填入编辑表单
  if (hasAddress.value) {
    editAddress.recipient = address.value.recipient;
    editAddress.phone = address.value.phone;
    editAddress.fullAddress = address.value.fullAddress;
  } else {
    // 如果没有地址，使用用户信息预填充
    editAddress.recipient = userInfo.value.realName || '';
    editAddress.phone = userInfo.value.phone || '';
    editAddress.fullAddress = '';
  }
  showEditAddressForm.value = true;
};

// 保存编辑的地址
const saveAddress = () => {
  // 验证地址信息
  if (!editAddress.recipient || !editAddress.phone || !editAddress.fullAddress) {
    ElMessage.warning('请填写完整的地址信息');
    return;
  }
  
  // 更新地址
  address.value = {
    recipient: editAddress.recipient,
    phone: editAddress.phone,
    fullAddress: editAddress.fullAddress
  };
  
  // 保存到用户默认地址
  saveDefaultAddress(address.value);
  
  // 隐藏表单
  showEditAddressForm.value = false;
  
  ElMessage.success('地址保存成功');
};

// 保存默认地址到用户信息
const saveDefaultAddress = async (addr) => {
  try {
    // 生成地址字符串：收件人,手机号,详细地址
    const addressStr = `${addr.recipient},${addr.phone},${addr.fullAddress}`;
    
    // 保存到用户资料
    await userApi.updateUserProfile({
      defaultAddress: addressStr
    });
  } catch (err) {
    console.error('保存默认地址失败:', err);
    // 不打断流程，只是记录日志
  }
};

// 更新商品数量
const updateQuantity = (newQuantity) => {
  // 确保数量不小于1且不超过库存
  if (newQuantity < 1) {
    quantity.value = 1;
  } else if (newQuantity > maxQuantity.value) {
    quantity.value = maxQuantity.value;
    ElMessage.warning(`商品库存仅剩${maxQuantity.value}件`);
  } else {
    quantity.value = newQuantity;
  }
};

// 提交订单
const submitOrder = async () => {
  if (!canSubmit.value) {
    if (!orderPreview.value.stock || orderPreview.value.stock < quantity.value) {
      ElMessage.error('商品库存不足');
    } else {
      ElMessage.warning(isOfflineTrade.value ? '请填写完整的线下交易信息' : '请填写完整的收货地址');
    }
    return;
  }
  
  submitting.value = true;
  try {
    // 判断是从购物车创建还是直接创建
    const isFromCart = localStorage.getItem('orderFromCart') === 'true';
    let orderNo;
    
    if (isFromCart) {
      // 从购物车创建订单 - 每个商品创建独立订单
      if (cartItems.value.length > 0) {
        // 准备交易信息
        const tradeInfo = {
          tradeType: isOfflineTrade.value ? 'OFFLINE' : 'EXPRESS',
          pointsUsed: usePoints.value ? pointsUsed.value : 0,
        };
      
        // 添加线下交易信息
        if (isOfflineTrade.value) {
          // 验证日期格式
          if (!validateDate(offlineTradeInfo.meetingTime)) {
            ElMessage.error('请输入有效的交易时间（年份必须在2000-2099之间）');
            submitting.value = false;
            return;
          }
          tradeInfo.offlineMeetingLocation = offlineTradeInfo.meetingLocation;
          // 使用格式化函数处理日期
          tradeInfo.offlineMeetingTime = formatDateTime(offlineTradeInfo.meetingTime);
        } else {
          // 添加收货地址信息
          tradeInfo.address = {
            receiverName: address.value.recipient,
            receiverPhone: address.value.phone,
            fullAddress: address.value.fullAddress,
            isDefault: true
          };
        }
        
        // 创建订单
        const orderPromises = cartItems.value.map(item => {
          // 为每个商品创建独立订单
          const orderData = {
            merchantId: item.merchantId,
            items: [{
              productId: item.productId,
              quantity: item.quantity
            }],
            ...tradeInfo // 合并交易信息
          };
          
          return orderApi.createOrder(orderData);
        });
        
        // 等待所有订单创建完成
        const responses = await Promise.all(orderPromises);
        
        // 检查订单创建结果
        const successOrders = responses.filter(res => res.data && res.data.code === 200);
        const failOrders = responses.filter(res => !res.data || res.data.code !== 200);
        
        // 清除本地存储
        localStorage.removeItem('orderFromCart');
        
        if (successOrders.length > 0) {
          // 有订单创建成功，清空购物车中已选中的商品
          try {
            await cartApi.clearSelectedItems();
            console.log('已清空购物车中已结算的商品');
          } catch (err) {
            console.error('清空购物车已选商品失败:', err);
          }
          
          // 提示订单创建结果
          if (failOrders.length > 0) {
            // 部分成功部分失败
            ElMessage.warning(`成功创建${successOrders.length}个订单，${failOrders.length}个订单创建失败`);
          } else {
            // 全部成功
            ElMessage.success(`成功创建${successOrders.length}个订单`);
          }
          
          // 跳转到订单列表
        setTimeout(() => {
          router.push('/orders/user');
        }, 500);
      } else {
          // 全部失败
          ElMessage.error('创建订单失败，请重试');
        }
      } else {
        ElMessage.error('购物车中没有选中的商品');
      }
    } else {
      // 直接创建订单
      const orderData = {
        merchantId: orderPreview.value.sellerId,
        items: [{
          productId: orderPreview.value.productId,
          quantity: quantity.value
        }],
        tradeType: isOfflineTrade.value ? 'OFFLINE' : 'EXPRESS',
        pointsUsed: usePoints.value ? pointsUsed.value : 0 // 添加积分抵扣
      };
      
      // 添加收货地址或线下交易信息
      if (isOfflineTrade.value) {
        // 验证日期格式
        if (!validateDate(offlineTradeInfo.meetingTime)) {
          ElMessage.error('请输入有效的交易时间（年份必须在2000-2099之间）');
          submitting.value = false;
          return;
        }
        orderData.offlineMeetingLocation = offlineTradeInfo.meetingLocation;
        // 使用格式化函数处理日期
        orderData.offlineMeetingTime = formatDateTime(offlineTradeInfo.meetingTime);
      } else {
        orderData.address = {
          receiverName: address.value.recipient,
          receiverPhone: address.value.phone,
          fullAddress: address.value.fullAddress,
          isDefault: true // 只有一个地址，所以默认为true
        };
      }
      
      // 调用API创建订单
      const response = await orderApi.createOrder(orderData);
      
      console.log('订单创建完整响应:', response);
      
      if (response.data && response.data.code === 200) {
        // 获取订单号
        orderNo = response.data.data;
        console.log('订单创建成功，订单号类型:', typeof orderNo, '值:', orderNo);
        
        // 尝试清空购物车中可能存在的已选商品
        try {
          await cartApi.clearSelectedItems();
          console.log('已清空购物车中可能选择的商品');
        } catch (err) {
          console.error('清空购物车操作失败:', err);
          // 不影响主流程继续
        }
        
        // 清除本地存储的订单预览
        localStorage.removeItem('orderPreview');
        
        // 无论是否获取到订单号，都跳转到订单列表
        ElMessage.success('订单创建成功');
        setTimeout(() => {
          router.push('/orders/user');
        }, 500);
      } else {
        ElMessage.error('创建订单失败：' + (response.data?.message || '未知错误'));
      }
    }
  } catch (err) {
    console.error('提交订单出错:', err);
    ElMessage.error('提交订单失败：' + (err.message || '网络错误'));
  } finally {
    submitting.value = false;
  }
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 页面加载
onMounted(() => {
  loadData();
});
</script>

<style scoped>
.order-confirm-container {
  background-color: #f8f9fa;
  min-height: 100vh;
  padding: 0 15px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  padding: 20px;
}

.order-title {
  margin-bottom: 20px;
}

.order-title h4 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

/* 加载和错误状态 */
.loading-wrapper, .error-wrapper {
  background-color: #fff;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.loading-text {
  margin-top: 15px;
  color: #666;
}

.error-message {
  color: #cf1322;
  background-color: #fff2f0;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.back-button-wrapper {
  margin-top: 20px;
}

.btn-back {
  background-color: #fff;
  color: #4a6ee0;
  border: 1px solid #4a6ee0;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-back:hover {
  background-color: #f0f5ff;
}

/* 订单内容样式 */
.order-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

/* 交易方式切换 */
.trade-type-switch {
  display: flex;
  align-items: center;
}

.switch-input {
  margin-right: 8px;
}

.switch-label {
  font-size: 14px;
  color: #666;
  cursor: pointer;
}

/* 地址卡片 */
.address-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  background-color: #fafafa;
  transition: all 0.3s;
}

.address-card:hover {
  border-color: #4a6ee0;
  background-color: #f0f5ff;
}

.recipient-info {
  margin-bottom: 8px;
}

.recipient-name {
  font-weight: 600;
  font-size: 15px;
  margin-right: 10px;
}

.recipient-phone {
  color: #666;
}

.default-tag {
  display: inline-block;
  background-color: #e74c3c;
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: 10px;
}

.address-detail {
  color: #666;
  font-size: 14px;
}

.btn-edit-address {
  background-color: transparent;
  color: #4a6ee0;
  border: none;
  cursor: pointer;
  padding: 5px 10px;
  font-size: 14px;
  border-radius: 4px;
  transition: all 0.3s;
}

.btn-edit-address:hover {
  background-color: #f0f5ff;
}

/* 无地址状态 */
.no-address-card {
  padding: 20px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  text-align: center;
  background-color: #fafafa;
}

.no-address-tip {
  color: #666;
  margin-bottom: 15px;
}

.no-address-tip i {
  font-size: 18px;
  margin-right: 8px;
  color: #e74c3c;
}

.btn-add-address {
  background-color: #4a6ee0;
  color: #fff;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-add-address:hover {
  background-color: #3d5bbf;
}

/* 地址表单 */
.address-form {
  margin-top: 16px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #f5f5f5;
  border-bottom: 1px solid #e8e8e8;
}

.form-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.btn-close-form {
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 16px;
  color: #666;
}

.form-content {
  padding: 16px;
  background-color: #fff;
}

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.form-group {
  flex: 1;
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.form-group input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  transition: all 0.3s;
}

.form-group input:focus {
  border-color: #4a6ee0;
  box-shadow: 0 0 0 2px rgba(74, 110, 224, 0.2);
  outline: none;
}

.form-tip {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

.form-action {
  text-align: right;
}

.btn-save-address {
  background-color: #4a6ee0;
  color: #fff;
  border: none;
  padding: 8px 20px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-save-address:hover {
  background-color: #3d5bbf;
}

/* 线下交易表单 */
.offline-trade-form {
  padding: 16px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  background-color: #fafafa;
}

/* 商品卡片 */
.product-card {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.product-image {
  margin-right: 16px;
}

.product-image img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eee;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 16px;
  margin: 0 0 8px 0;
  color: #333;
}

.product-seller {
  color: #999;
  font-size: 13px;
}

.product-seller i {
  margin-right: 5px;
}

.product-stock {
  color: #999;
  font-size: 13px;
}

.product-stock i {
  margin-right: 5px;
}

.product-price-wrapper {
  text-align: right;
}

.product-price {
  font-size: 16px;
  color: #e74c3c;
  font-weight: 600;
  margin-bottom: 5px;
}

.quantity-selector {
  display: flex;
  align-items: center;
}

.quantity-btn {
  background-color: transparent;
  border: none;
  cursor: pointer;
  padding: 5px;
  font-size: 14px;
  color: #666;
}

.quantity-input {
  width: 40px;
  padding: 5px;
  text-align: center;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}

/* 支付信息 */
.payment-card {
  padding: 16px;
  background-color: #fafafa;
  border-radius: 8px;
}

.payment-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
  color: #666;
}

.payment-total {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #eee;
  font-weight: 600;
}

.total-value {
  color: #e74c3c;
  font-size: 20px;
}

/* 提交订单区 */
.order-submit-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.order-summary {
  display: flex;
  flex-direction: column;
}

.order-count {
  color: #666;
  font-size: 14px;
  margin-bottom: 5px;
}

.order-amount {
  font-size: 14px;
}

.order-amount strong {
  color: #e74c3c;
  font-size: 20px;
  font-weight: 600;
}

.btn-submit {
  background-color: #e74c3c;
  color: #fff;
  border: none;
  padding: 12px 30px;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-submit:hover:not(:disabled) {
  background-color: #d63725;
}

.btn-submit:disabled {
  background-color: #f5f5f5;
  color: #bfbfbf;
  cursor: not-allowed;
}

/* 加载动画 */
.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top: 4px solid #4a6ee0;
  width: 30px;
  height: 30px;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

.spinner-border-sm {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid currentColor;
  border-right-color: transparent;
  border-radius: 50%;
  animation: spin 0.75s linear infinite;
  vertical-align: text-bottom;
  margin-right: 8px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .order-confirm-container {
    padding: 0 10px;
  }
  
  .container {
    padding: 15px;
  }
  
  .form-row {
    flex-direction: column;
    gap: 0;
  }
  
  .order-submit-section {
    flex-direction: column;
    gap: 16px;
  }
  
  .order-summary {
    text-align: center;
  }
  
  .btn-submit {
    width: 100%;
  }
}

.in-stock {
  color: #52c41a;
}

.low-stock {
  color: #faad14;
}

.out-of-stock {
  color: #f5222d;
}

.points-section {
  margin-top: 5px;
  padding: 8px 0;
  border-top: 1px dashed #eee;
}

.points-toggle {
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
}

.toggle-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  margin-right: 5px;
  border: 1px solid #4a6ee0;
  border-radius: 3px;
  color: white;
  background-color: #4a6ee0;
  font-size: 12px;
}

.points-info {
  font-size: 12px;
  color: #888;
  margin-left: 5px;
}

.payment-note {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  background-color: #f9f9f9;
  border-radius: 4px;
  color: #666;
  font-size: 14px;
}

.payment-note i {
  color: #faad14;
  margin-right: 8px;
  font-size: 16px;
}

/* 购物车商品样式 */
.cart-products {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
}

.cart-products .product-card {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px;
}

.cart-products .product-card:last-child {
  border-bottom: none;
}

.quantity-info {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.item-subtotal {
  font-size: 14px;
  color: #666;
}

.subtotal-value {
  color: #e74c3c;
  font-weight: 500;
}

.cart-summary {
  padding: 12px 16px;
  background-color: #f9f9f9;
  border-top: 1px solid #f0f0f0;
  text-align: right;
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.points-section {
  margin-top: 5px;
  padding: 8px 0;
  border-top: 1px dashed #eee;
}

.points-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.points-control {
  display: flex;
  align-items: center;
}

.points-input-group {
  margin-left: 10px;
}

.points-hint {
  font-size: 12px;
  color: #888;
}

.user-points {
  font-size: 14px;
  color: #666;
}
</style> 