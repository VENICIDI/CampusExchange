<template>
  <div class="order-confirm-container">
    <div class="container py-5">
      <h2 class="mb-4">确认订单</h2>
      
      <!-- 加载中状态 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">加载中...</span>
        </div>
        <p class="mt-2">正在准备订单信息...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="error" class="alert alert-danger">
        {{ error }}
        <button @click="goBack" class="btn btn-outline-primary ms-3">返回上一页</button>
      </div>
      
      <!-- 订单确认信息 -->
      <div v-else class="order-confirm-content">
        <!-- 商品信息 -->
        <div class="card mb-4">
          <div class="card-header">
            <h5 class="mb-0">商品信息</h5>
          </div>
          <div class="card-body">
            <div class="product-item d-flex align-items-center">
              <img :src="orderPreview.productImage || 'https://via.placeholder.com/80'" :alt="orderPreview.productName" class="product-image me-3">
              <div class="product-info flex-grow-1">
                <h5 class="product-name">{{ orderPreview.productName }}</h5>
                <div class="product-seller text-muted">卖家: {{ orderPreview.sellerName }}</div>
              </div>
              <div class="product-price-qty text-end">
                <div class="price text-danger fw-bold">¥{{ orderPreview.price }}</div>
                <div class="quantity">x {{ orderPreview.quantity }}</div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 收货信息 -->
        <div class="card mb-4">
          <div class="card-header d-flex justify-content-between align-items-center">
            <h5 class="mb-0">收货信息</h5>
            <div class="form-check form-switch">
              <input class="form-check-input" type="checkbox" id="pickupSwitch" v-model="isOfflineTrade">
              <label class="form-check-label" for="pickupSwitch">线下交易</label>
            </div>
          </div>
          <div class="card-body">
            <!-- 快递配送 -->
            <div v-if="!isOfflineTrade">
              <div v-if="addresses.length > 0">
                <div v-for="(address, index) in addresses" :key="index" class="address-item mb-3">
                  <div class="form-check">
                    <input 
                      class="form-check-input" 
                      type="radio" 
                      :id="`address-${index}`" 
                      :value="index" 
                      v-model="selectedAddressIndex"
                    >
                    <label class="form-check-label" :for="`address-${index}`">
                      <div class="address-content">
                        <div class="address-user mb-1">{{ address.recipient }} {{ address.phone }}</div>
                        <div class="address-detail text-muted">
                          {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}
                        </div>
                      </div>
                    </label>
                  </div>
                </div>
              </div>
              <div v-else class="no-address alert alert-warning">
                您还没有收货地址，请先添加收货地址
              </div>
              
              <button @click="showAddAddressForm = true" class="btn btn-outline-primary mt-2">
                <i class="fas fa-plus me-1"></i> 添加新地址
              </button>
              
              <!-- 添加地址表单 -->
              <div v-if="showAddAddressForm" class="add-address-form mt-3 p-3 border rounded">
                <h6 class="mb-3">添加新地址</h6>
                <div class="row g-3">
                  <div class="col-md-6">
                    <label for="recipient" class="form-label">收件人</label>
                    <input type="text" class="form-control" id="recipient" v-model="newAddress.recipient">
                  </div>
                  <div class="col-md-6">
                    <label for="phone" class="form-label">手机号码</label>
                    <input type="text" class="form-control" id="phone" v-model="newAddress.phone">
                  </div>
                  <div class="col-md-4">
                    <label for="province" class="form-label">省份</label>
                    <input type="text" class="form-control" id="province" v-model="newAddress.province">
                  </div>
                  <div class="col-md-4">
                    <label for="city" class="form-label">城市</label>
                    <input type="text" class="form-control" id="city" v-model="newAddress.city">
                  </div>
                  <div class="col-md-4">
                    <label for="district" class="form-label">区/县</label>
                    <input type="text" class="form-control" id="district" v-model="newAddress.district">
                  </div>
                  <div class="col-12">
                    <label for="detailAddress" class="form-label">详细地址</label>
                    <input type="text" class="form-control" id="detailAddress" v-model="newAddress.detailAddress">
                  </div>
                  <div class="col-12 mt-3 d-flex justify-content-end">
                    <button type="button" class="btn btn-secondary me-2" @click="showAddAddressForm = false">取消</button>
                    <button type="button" class="btn btn-primary" @click="addNewAddress">保存地址</button>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 线下交易 -->
            <div v-else>
              <div class="mb-3">
                <label for="meetingLocation" class="form-label">交易地点</label>
                <input type="text" class="form-control" id="meetingLocation" v-model="offlineTradeInfo.meetingLocation" placeholder="请输入线下交易地点">
              </div>
              <div class="mb-3">
                <label for="meetingTime" class="form-label">交易时间</label>
                <input type="datetime-local" class="form-control" id="meetingTime" v-model="offlineTradeInfo.meetingTime">
              </div>
            </div>
          </div>
        </div>
        
        <!-- 支付信息 -->
        <div class="card mb-4">
          <div class="card-header">
            <h5 class="mb-0">支付信息</h5>
          </div>
          <div class="card-body">
            <div class="d-flex justify-content-between mb-3">
              <span>商品金额</span>
              <span>¥{{ orderPreview.price * orderPreview.quantity }}</span>
            </div>
            <div class="d-flex justify-content-between mb-3">
              <span>运费</span>
              <span>¥{{ isOfflineTrade ? 0 : shippingFee }}</span>
            </div>
            <div class="d-flex justify-content-between mb-3">
              <span>平台服务费</span>
              <span>¥{{ serviceFee }}</span>
            </div>
            <hr/>
            <div class="d-flex justify-content-between fw-bold">
              <span>实付金额</span>
              <span class="text-danger">¥{{ totalAmount }}</span>
            </div>
          </div>
        </div>
        
        <!-- 订单备注 -->
        <div class="card mb-4">
          <div class="card-header">
            <h5 class="mb-0">订单备注</h5>
          </div>
          <div class="card-body">
            <textarea class="form-control" rows="3" placeholder="填写订单备注信息" v-model="orderRemark"></textarea>
          </div>
        </div>
        
        <!-- 提交订单 -->
        <div class="order-submit d-flex justify-content-between align-items-center">
          <div class="order-total">
            <span class="me-3">共{{ orderPreview.quantity }}件商品</span>
            <span class="total-price">合计：<strong class="text-danger fs-4">¥{{ totalAmount }}</strong></span>
          </div>
          <button 
            class="btn btn-danger btn-lg" 
            @click="submitOrder" 
            :disabled="submitting || !canSubmit"
          >
            <span v-if="submitting">
              <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
              提交中...
            </span>
            <span v-else>提交订单</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { orderApi } from '@/api/all';
import { ElMessage } from 'element-plus';

const router = useRouter();
const loading = ref(true);
const error = ref(null);
const submitting = ref(false);

// 订单预览信息（从本地存储获取）
const orderPreview = ref({});

// 收货地址
const addresses = ref([]);
const selectedAddressIndex = ref(0);

// 新增地址表单
const showAddAddressForm = ref(false);
const newAddress = reactive({
  recipient: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: ''
});

// 线下交易信息
const isOfflineTrade = ref(false);
const offlineTradeInfo = reactive({
  meetingLocation: '',
  meetingTime: ''
});

// 订单备注
const orderRemark = ref('');

// 费用信息
const shippingFee = ref(0); // 运费
const serviceFee = ref(0); // 平台服务费

// 计算总金额
const totalAmount = computed(() => {
  const productTotal = orderPreview.value.price * orderPreview.value.quantity;
  const shipping = isOfflineTrade.value ? 0 : shippingFee.value;
  return productTotal + shipping + serviceFee.value;
});

// 判断是否可以提交订单
const canSubmit = computed(() => {
  if (isOfflineTrade.value) {
    return offlineTradeInfo.meetingLocation && offlineTradeInfo.meetingTime;
  } else {
    return addresses.value.length > 0;
  }
});

// 加载数据
const loadData = async () => {
  loading.value = true;
  error.value = null;
  
  try {
    // 获取订单预览信息
    const previewData = localStorage.getItem('orderPreview');
    if (!previewData) {
      error.value = '订单信息不存在，请重新选择商品';
      return;
    }
    
    orderPreview.value = JSON.parse(previewData);
    
    // TODO: 从后端加载用户的收货地址
    // 这里暂时使用模拟数据
    addresses.value = [
      {
        recipient: '张三',
        phone: '13800138000',
        province: '北京市',
        city: '北京市',
        district: '海淀区',
        detailAddress: '清华大学计算机科学与技术系'
      },
      {
        recipient: '李四',
        phone: '13900139000',
        province: '上海市',
        city: '上海市',
        district: '浦东新区',
        detailAddress: '复旦大学软件学院'
      }
    ];
    
    // 设置费用
    shippingFee.value = 0; // 假设免运费
    serviceFee.value = Math.round(orderPreview.value.price * orderPreview.value.quantity * 0.03); // 3%平台服务费
    
  } catch (err) {
    console.error('加载订单数据出错:', err);
    error.value = '加载订单数据出错，请重试';
  } finally {
    loading.value = false;
  }
};

// 添加新地址
const addNewAddress = () => {
  // 验证地址信息
  if (!newAddress.recipient || !newAddress.phone || !newAddress.province || 
      !newAddress.city || !newAddress.district || !newAddress.detailAddress) {
    ElMessage.warning('请填写完整的地址信息');
    return;
  }
  
  // 添加到地址列表
  addresses.value.push({ ...newAddress });
  
  // 选中新添加的地址
  selectedAddressIndex.value = addresses.value.length - 1;
  
  // 清空表单并隐藏
  Object.keys(newAddress).forEach(key => newAddress[key] = '');
  showAddAddressForm.value = false;
  
  ElMessage.success('地址添加成功');
};

// 提交订单
const submitOrder = async () => {
  if (!canSubmit.value) {
    ElMessage.warning(isOfflineTrade.value ? '请填写完整的线下交易信息' : '请选择收货地址');
    return;
  }
  
  submitting.value = true;
  try {
    // 构建订单数据
    const orderData = {
      productId: orderPreview.value.productId,
      quantity: orderPreview.value.quantity,
      tradeType: isOfflineTrade.value ? 'OFFLINE' : 'EXPRESS',
      remark: orderRemark.value
    };
    
    // 添加收货地址或线下交易信息
    if (isOfflineTrade.value) {
      orderData.offlineMeetingLocation = offlineTradeInfo.meetingLocation;
      orderData.offlineMeetingTime = offlineTradeInfo.meetingTime;
    } else {
      orderData.recipientAddress = addresses.value[selectedAddressIndex.value];
    }
    
    // 调用API创建订单
    const response = await orderApi.createOrder(orderData);
    
    if (response.data && response.data.code === 200) {
      // 获取订单号
      const orderNo = response.data.data.orderNo || response.data.data;
      
      // 清除本地存储的订单预览
      localStorage.removeItem('orderPreview');
      
      // 跳转到支付页面或订单详情页
      ElMessage.success('订单创建成功');
      router.push(`/order/${orderNo}?action=pay`);
    } else {
      ElMessage.error('创建订单失败：' + (response.data?.message || '未知错误'));
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
  min-height: 90vh;
  background-color: #f8f9fa;
}

.order-confirm-content {
  max-width: 800px;
  margin: 0 auto;
}

.card {
  border: none;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.card-header {
  background-color: white;
  border-bottom: 1px solid rgba(0,0,0,0.1);
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.product-name {
  font-size: 1.1rem;
  margin-bottom: 0.5rem;
}

.price {
  font-size: 1.2rem;
}

.address-item {
  padding: 10px;
  border-radius: 5px;
  transition: background-color 0.2s;
}

.address-item:hover {
  background-color: #f8f9fa;
}

.form-check-input:checked ~ .form-check-label .address-content {
  font-weight: 500;
}

.total-price {
  font-size: 1.1rem;
}

button.btn-danger {
  min-width: 150px;
}
</style> 