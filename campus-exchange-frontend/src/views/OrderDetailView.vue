<template>
  <div class="order-detail-container">
    <div class="container">
      <div class="mb-4">
        <button class="btn-back" @click="goBack">
          <i class="fas fa-arrow-left"></i>
          <span>返回</span>
        </button>
      </div>
      
      <h2 class="page-title mb-4">订单详情</h2>

      <!-- 加载中状态 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">加载中...</span>
        </div>
        <p class="mt-2">正在加载订单信息...</p>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="error" class="alert alert-danger">
        {{ error }}
      </div>

      <!-- 订单详情内容 -->
      <div v-else-if="order" class="order-detail-content">
        <!-- 订单状态卡片 -->
        <div class="card mb-4">
          <div class="card-body">
            <div class="order-status-section">
              <div class="status-line">
                <h5 class="status-label">订单状态:</h5>
                <span class="badge" :class="getStatusBadgeClass(order.status)">
                  {{ orderApi.getStatusText(order.status) }}
                </span>
                <span v-if="order.status === 'PENDING_PAYMENT' && paymentCountdown" class="payment-countdown ms-2">
                  支付倒计时: <strong>{{ paymentCountdown }}</strong>
                </span>
              </div>
              
              <div class="order-info-line">
                <span class="info-label">订单号:</span>
                <span class="info-value">{{ order.orderNo }}</span>
              </div>
              
              <div class="order-info-line">
                <span class="info-label">下单时间:</span>
                <span class="info-value">{{ formatDate(order.createTime) }}</span>
              </div>
              
              <div v-if="order.merchantName" class="order-info-line">
                <span class="info-label">店铺:</span>
                <span class="info-value">{{ order.merchantName }}</span>
              </div>
              
              <!-- 退货申请信息 -->
              <div v-if="order.returnRequestInfo" class="mt-3 pt-3 border-top">
                <div class="return-request-info">
                  <h6 class="return-info-title">退货申请信息</h6>
                  
                  <div class="order-info-line">
                    <span class="info-label">申请时间:</span>
                    <span class="info-value">{{ formatDate(order.returnRequestInfo.applicationTime) }}</span>
                  </div>
                  
                  <div class="order-info-line">
                    <span class="info-label">退货原因:</span>
                    <span class="info-value">{{ order.returnRequestInfo.reason }}</span>
                  </div>
                  
                  <div v-if="order.status === 'RETURN_REJECTED' && order.returnRequestInfo.rejectionReason" class="order-info-line">
                    <span class="info-label">拒绝原因:</span>
                    <span class="info-value text-danger">{{ order.returnRequestInfo.rejectionReason }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="order-timeline mt-4">
              <div class="timeline-steps">
                <div
                  v-for="(step, index) in orderTimeline"
                  :key="index"
                  :class="['timeline-step', step.active ? 'active' : '', step.completed ? 'completed' : '']"
                >
                  <div class="timeline-icon">
                    <i :class="step.icon"></i>
                  </div>
                  <div class="timeline-content">
                    <div class="step-title">{{ step.title }}</div>
                    <div class="step-time" v-if="step.time">{{ step.time }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 商品信息卡片 -->
        <div class="card mb-4">
          <div class="card-header">
            <h5 class="mb-0"><i class="fas fa-shopping-cart me-2"></i>商品信息</h5>
          </div>
          <div class="card-body p-0">
            <div class="table-responsive">
              <table class="table product-table mb-0">
                <thead>
                  <tr>
                    <th class="text-center" width="120">商品图片</th>
                    <th width="40%">商品信息</th>
                    <th class="text-center" width="15%">单价</th>
                    <th class="text-center" width="10%">数量</th>
                    <th class="text-end" width="15%">小计</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(item, index) in order.orderItems" :key="index">
                    <td class="text-center">
                        <img
                        :src="item.productImage || item.productImageSnapshot || 'https://via.placeholder.com/70/e0e0e0/666666?text=商品'"
                        :alt="item.productName || item.productNameSnapshot"
                        class="product-image"
                        >
                    </td>
                    <td>
                      <div class="product-name">{{ item.productName || item.productNameSnapshot }}</div>
                          <div class="specifications text-muted small" v-if="item.specifications">
                            {{ item.specifications }}
                      </div>
                    </td>
                    <td class="text-center align-middle">¥{{ item.price || item.priceAtPurchase }}</td>
                    <td class="text-center align-middle">{{ item.quantity }}</td>
                    <td class="text-end align-middle">
                      <strong class="item-total">¥{{ ((item.price || item.priceAtPurchase) * item.quantity).toFixed(2) }}</strong>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- 收货信息和金额信息并排 -->
        <div class="row">
          <div class="col-md-6">
        <!-- 收货信息 -->
        <div class="card mb-4" v-if="order.tradeType === 'EXPRESS'">
          <div class="card-header">
                <h5 class="mb-0"><i class="fas fa-map-marker-alt me-2"></i>收货信息</h5>
          </div>
              <div class="card-body address-info">
                <p class="mb-1"><span class="info-label">收货人：</span> {{ order.orderAddress?.recipient }}</p>
                <p class="mb-1"><span class="info-label">联系电话：</span> {{ order.orderAddress?.phone }}</p>
                <p class="mb-3"><span class="info-label">收货地址：</span> {{ formatAddress(order.orderAddress) }}</p>
                <p v-if="order.status === 'SHIPPED' || order.status === 'RECEIVED' || order.status === 'COMPLETED'">
                  <span class="info-label">发货时间：</span> {{ formatDate(order.shippingTime) || '未知' }}
                </p>
          </div>
        </div>

        <!-- 线下交易信息 -->
        <div class="card mb-4" v-if="order.tradeType === 'OFFLINE'">
          <div class="card-header">
                <h5 class="mb-0"><i class="fas fa-handshake me-2"></i>线下交易信息</h5>
          </div>
              <div class="card-body address-info">
                <p class="mb-1"><span class="info-label">交易地点：</span> {{ order.offlineMeetingLocation || '未指定' }}</p>
                <p class="mb-1"><span class="info-label">交易时间：</span> {{ formatDate(order.offlineMeetingTime) || '未指定' }}</p>
              </div>
          </div>
        </div>

          <div class="col-md-6">
        <!-- 支付信息 -->
        <div class="card mb-4" v-if="order">
          <div class="card-header">
            <h5 class="mb-0"><i class="fas fa-credit-card me-2"></i>支付信息</h5>
          </div>
          <div class="card-body">
            <div class="payment-info">
              <div class="row mb-2">
                <div class="col-md-4 col-6 text-muted">订单总额：</div>
                <div class="col-md-8 col-6">¥{{ formatPrice(order.totalProductAmount) }}</div>
              </div>
              
              <!-- 积分抵扣信息 -->
              <div class="row mb-2" v-if="order.pointsUsed > 0">
                <div class="col-md-4 col-6 text-muted">积分抵扣：</div>
                <div class="col-md-8 col-6">
                  -¥{{ formatPrice(order.pointsDeductionAmount) }}
                  <span class="points-info text-muted">({{ order.pointsUsed }}积分)</span>
                </div>
              </div>
              
              <div class="row mb-2">
                <div class="col-md-4 col-6 text-muted">实付金额：</div>
                <div class="col-md-8 col-6 text-danger fw-bold">¥{{ formatPrice(order.actualPaymentAmount) }}</div>
              </div>
              <div class="row mb-2">
                <div class="col-md-4 col-6 text-muted">支付状态：</div>
                <div class="col-md-8 col-6">
                  <span :class="getStatusClass(order.status)">{{ getStatusText(order.status) }}</span>
                </div>
              </div>
              <div class="row mb-2" v-if="order.paymentTime">
                <div class="col-md-4 col-6 text-muted">支付时间：</div>
                <div class="col-md-8 col-6">{{ formatDate(order.paymentTime) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 订单操作区 -->
            <div class="card mb-4">
              <div class="card-header">
                <h5 class="mb-0"><i class="fas fa-cog me-2"></i>订单操作</h5>
              </div>
          <div class="card-body">
                <div class="d-flex justify-content-center">
              <!-- 待付款状态 -->
              <button
                v-if="order.status === 'PENDING_PAYMENT'"
                @click="payOrder"
                    class="btn btn-primary action-btn me-3"
              >
                    <i class="fas fa-credit-card me-1"></i> 立即支付
              </button>

                  <!-- 待付款 - 取消订单 -->
              <button
                v-if="order.status === 'PENDING_PAYMENT'"
                @click="cancelOrder"
                    class="btn btn-outline-danger action-btn"
              >
                    <i class="fas fa-times me-1"></i> 取消订单
              </button>

                  <!-- 已发货状态 - 确认收货 -->
              <button
                v-if="order.status === 'SHIPPED'"
                @click="confirmReceipt"
                    class="btn btn-primary action-btn"
              >
                    <i class="fas fa-check-circle me-1"></i> 确认收货
              </button>

                  <!-- 已收货状态 - 去评价 -->
              <button
                v-if="(order.status === 'RECEIVED' || order.status === 'RETURN_REJECTED') && !hasReviewed"
                @click="goToReview"
                class="btn btn-primary action-btn me-3"
              >
                <i class="fas fa-star me-1"></i> 去评价
              </button>

                  <!-- 已收货状态 - 申请退货 -->
              <button
                v-if="order.status === 'RECEIVED' && !hasReviewed"
                @click="requestReturn"
                    class="btn btn-outline-primary action-btn"
              >
                    <i class="fas fa-undo me-1"></i> 申请退货/退款
              </button>

              <!-- 商家同意退货后 - 发出退货 -->
              <button
                v-if="order.status === 'RETURN_APPROVED'"
                @click="showReturnShipDialog"
                class="btn btn-primary action-btn"
              >
                <i class="fas fa-shipping-fast me-1"></i> 发出退货
              </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 评价信息展示区域 -->
      <div v-if="order && (productReviews.length > 0 || merchantReview || buyerReview)" class="card mb-4">
        <div class="card-header">
          <h5 class="mb-0"><i class="fas fa-star me-2"></i>评价信息</h5>
        </div>
        <div class="card-body">
          <!-- 买家对商品的评价 -->
          <div v-if="productReviews.length > 0" class="mb-4">
            <h5 class="review-section-title">商品评价</h5>
            <div class="review-item">
              <div class="rating-score">
                <span class="label">评分：</span>
                <el-rate 
                  :model-value="getAverageProductRating()" 
                  disabled 
                  show-score 
                  text-color="#ff9900">
                </el-rate>
              </div>
              <div v-if="getProductReviewContent()" class="review-text">
                <p>{{ getProductReviewContent() }}</p>
              </div>
              <div class="review-time" v-if="productReviews.length > 0">
                <span>{{ formatDate(productReviews[0].createTime) }}</span>
              </div>
            </div>
          </div>
          
          <!-- 买家对商家服务的评价 -->
          <div v-if="merchantReview" class="mb-4">
            <h5 class="review-section-title">商家服务评价</h5>
            <div class="review-item">
              <div class="rating-score">
                <span class="label">评分：</span>
                <el-rate 
                  :model-value="merchantReview.serviceAttitudeRating || merchantReview.ratingScore" 
                  disabled 
                  show-score 
                  text-color="#ff9900">
                </el-rate>
              </div>
              <div v-if="merchantReview.content" class="review-text">
                <p>{{ merchantReview.content }}</p>
              </div>
              <div class="review-time">
                <span>{{ formatDate(merchantReview.createTime) }}</span>
              </div>
            </div>
          </div>
          
          <!-- 商家对买家的评价 -->
          <div v-if="buyerReview" class="mb-4">
            <h5 class="review-section-title">商家对您的评价</h5>
            <div class="review-item">
              <div class="rating-score">
                <span class="label">评分：</span>
                <el-rate 
                  :model-value="buyerReview.ratingScore" 
                  disabled 
                  show-score 
                  text-color="#ff9900">
                </el-rate>
              </div>
              <div v-if="buyerReview.content" class="review-text">
                <p>{{ buyerReview.content }}</p>
              </div>
              <div class="review-time">
                <span>{{ formatDate(buyerReview.createTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 退货申请弹窗 -->
    <el-dialog
      v-model="returnDialogVisible"
      title="申请退货/退款"
      width="500px"
    >
      <el-form ref="returnFormRef" :model="returnForm" label-width="100px">
        <el-form-item label="退货原因" prop="reason">
          <el-input
            v-model="returnForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请详细描述退货原因"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="returnDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReturnRequest" :loading="submitting">提交申请</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 发出退货弹窗 -->
    <el-dialog
      v-model="returnShipDialogVisible"
      title="发出退货"
      width="500px"
    >
      <div class="text-center">
        <p class="mb-3">确认已将商品寄回商家？</p>
        <p class="text-muted">确认后系统将通知商家确认收货</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="returnShipDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReturnShip" :loading="submitting">确认发出</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { orderApi, reviewApi } from '@/api/all';
import { ElMessage, ElMessageBox } from 'element-plus';

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const error = ref(null);
const order = ref(null);
const submitting = ref(false);

// 防止并发请求的标志
const isLoadingDetails = ref(false);
// 防止重复取消订单
const isCancelingOrder = ref(false);

// 支付倒计时
const paymentCountdown = ref('');
let countdownTimer = null;

// 退货申请表单
const returnDialogVisible = ref(false);
const returnForm = reactive({
  orderNo: '',
  reason: ''
});

// 发出退货表单
const returnShipDialogVisible = ref(false);
const returnShipForm = reactive({
  orderNo: '',
  trackingInfo: ''
});

// 在script部分添加hasReviewed状态和检查评价状态的函数
const hasReviewed = ref(false);

// 检查是否已评价
const checkReviewStatus = async () => {
  try {
    if (order.value && order.value.status === 'RECEIVED') {
      const response = await reviewApi.hasReviewedOrder(order.value.orderNo);
      if (response.data && response.data.code === 200) {
        hasReviewed.value = response.data.data || false;
      }
    }
  } catch (error) {
    console.error('检查评价状态出错:', error);
  }
};

// 获取订单详情
const fetchOrderDetail = async () => {
  loading.value = true;
  error.value = null;
  
  // 如果正在获取详情，则不重复请求
  if (isLoadingDetails.value) {
    return;
  }
  
  isLoadingDetails.value = true;
  let retryCount = 0;
  const maxRetries = 2;

  try {
    const orderNo = route.params.orderNo;
    if (!orderNo) {
      error.value = '订单号不存在';
      return;
    }

    const fetchData = async () => {
      try {
    const response = await orderApi.getOrderDetail(orderNo);
    if (response.data && response.data.code === 200) {
      order.value = response.data.data;
      
      // 添加调试信息输出
      console.log('订单信息：', {
        status: order.value.status,
        statusType: typeof order.value.status,
        orderNo: order.value.orderNo
      });

      // 如果有action=pay参数并且订单状态是待付款，自动弹出支付确认
      if (route.query.action === 'pay' && order.value.status === 'PENDING_PAYMENT') {
        payOrder();
      }
          
          // 如果是待付款状态，开始计算倒计时
          if (order.value.status === 'PENDING_PAYMENT') {
            updatePaymentCountdown();
            startCountdownTimer();
          } else {
            stopCountdownTimer();
          }
          
          return true;
    } else {
      error.value = '获取订单详情失败：' + (response.data?.message || '未知错误');
          return false;
    }
  } catch (err) {
    console.error('获取订单详情出错:', err);
    error.value = '获取订单详情失败：' + (err.message || '网络错误');
        return false;
      }
    };
    
    // 尝试获取数据，如果失败则重试
    let success = await fetchData();
    
    // 如果失败且未达到最大重试次数，则重试
    while (!success && retryCount < maxRetries) {
      retryCount++;
      console.log(`重试获取订单详情 (${retryCount}/${maxRetries})...`);
      // 等待一段时间再重试，每次等待时间递增
      await new Promise(resolve => setTimeout(resolve, 1000 * retryCount));
      success = await fetchData();
    }
    
    if (!success) {
      ElMessage.error('获取订单信息失败，请稍后再试');
    }
  } finally {
    loading.value = false;
    isLoadingDetails.value = false;
    
    // 检查评价状态
    if (order.value && order.value.status === 'RECEIVED') {
      checkReviewStatus();
    }
    
    // 获取评价信息
    if (order.value) {
      fetchReviews();
    }
  }
};

// 更新支付倒计时
const updatePaymentCountdown = () => {
  if (!order.value || order.value.status !== 'PENDING_PAYMENT') {
    paymentCountdown.value = '';
    return;
  }
  
  const orderTime = new Date(order.value.createTime).getTime();
  const expiryTime = orderTime + 30 * 60 * 1000; // 30分钟超时
  const now = Date.now();
  const remainingMs = expiryTime - now;
  
  if (remainingMs <= 0) {
    paymentCountdown.value = '已超时';
    stopCountdownTimer();
    
    // 只有当未在取消过程中才尝试取消
    if (!isCancelingOrder.value) {
      // 提示用户订单已超时
      ElMessage.warning('订单已超时，正在自动取消...');
      
      // 主动调用取消订单接口
      autoSystemCancelOrder();
    }
    
    return;
  }
  
  const minutes = Math.floor(remainingMs / 60000);
  const seconds = Math.floor((remainingMs % 60000) / 1000);
  paymentCountdown.value = `${minutes}分${seconds}秒`;
};

// 自动系统取消订单
const autoSystemCancelOrder = async () => {
  // 如果不是待支付状态或已经在取消中，则不执行
  if (!order.value || order.value.status !== 'PENDING_PAYMENT' || isCancelingOrder.value) {
    return;
  }
  
  // 设置取消中标志
  isCancelingOrder.value = true;
  
  try {
    const response = await orderApi.cancelOrder(order.value.orderNo);
    
    if (response.data && response.data.code === 200) {
      ElMessage.info('订单已自动取消');
      // 延迟1秒后刷新订单信息
      setTimeout(() => {
        fetchOrderDetail();
      }, 1000);
    } else {
      console.warn('自动取消订单失败，系统将稍后自动处理');
      // 延迟3秒后刷新订单信息
      setTimeout(() => {
        fetchOrderDetail();
      }, 3000);
    }
  } catch (err) {
    console.error('自动取消订单出错:', err);
    // 延迟3秒后刷新订单信息
    setTimeout(() => {
      fetchOrderDetail();
    }, 3000);
  } finally {
    // 操作完成后释放标志
    setTimeout(() => {
      isCancelingOrder.value = false;
    }, 5000); // 5秒钟内不允许再次触发取消
  }
};

// 开始倒计时定时器，每分钟更新一次
const startCountdownTimer = () => {
  stopCountdownTimer();
  updatePaymentCountdown();
  
  // 首次更新后，设置每秒更新一次
  countdownTimer = setInterval(() => {
    updatePaymentCountdown();
  }, 1000);
};

// 停止倒计时定时器
const stopCountdownTimer = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer);
    countdownTimer = null;
  }
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

// 格式化地址
const formatAddress = (address) => {
  if (!address) return '未知地址';
  return address.fullAddress || '未提供详细地址';
};

// 订单的商品总额（解决显示NaN的问题）
const productTotalAmount = computed(() => {
  if (!order.value) return 0;
  return order.value.totalProductAmount || 0;
});

// 计算可获得的积分
const calculateEarnablePoints = computed(() => {
  if (!order.value || !order.value.actualPaymentAmount) return 0;
  // 每消费1元获得1积分
  return Math.floor(order.value.actualPaymentAmount);
});

// 格式化金额
const formatPrice = (price) => {
  if (price === null || price === undefined) return '0.00';
  return Number(price).toFixed(2);
};

// 获取状态对应的样式类
const getStatusClass = (status) => {
  const statusClassMap = {
    'PENDING_PAYMENT': 'text-warning',
    'PENDING_SHIPMENT': 'text-info',
    'SHIPPED': 'text-primary',
    'RECEIVED': 'text-success',
    'COMPLETED': 'text-success',
    'CANCELLED': 'text-secondary',
    'RETURN_REQUESTED': 'text-warning',
    'RETURN_APPROVED': 'text-info',
    'RETURN_GOODS_RECEIVED': 'text-info',
    'RETURNED': 'text-success',
    'RETURN_REJECTED': 'text-danger'
  };

  return statusClassMap[status] || 'text-secondary';
};

// 获取状态对应的文本
const getStatusText = (status) => {
  return orderApi.getStatusText(status);
};

// 获取订单状态对应的Badge类
const getStatusBadgeClass = (status) => {
  const statusClassMap = {
    'PENDING_PAYMENT': 'bg-warning',
    'PENDING_SHIPMENT': 'bg-info',
    'SHIPPED': 'bg-primary',
    'RECEIVED': 'bg-success',
    'COMPLETED': 'bg-success',
    'CANCELLED': 'bg-secondary',
    'RETURN_REQUESTED': 'bg-warning',
    'RETURN_APPROVED': 'bg-info',
    'RETURN_GOODS_RECEIVED': 'bg-info',
    'RETURNED': 'bg-success',
    'RETURN_REJECTED': 'bg-danger'
  };

  return statusClassMap[status] || 'bg-secondary';
};

// 订单时间线
const orderTimeline = computed(() => {
  if (!order.value) return [];

  const timeline = [
    {
      title: '下单',
      icon: 'fas fa-shopping-cart',
      time: formatDate(order.value.createTime),
      completed: true,
      active: false
    },
    {
      title: '付款',
      icon: 'fas fa-credit-card',
      time: formatDate(order.value.paymentTime),
      completed: order.value.paymentTime != null,
      active: order.value.status === 'PENDING_PAYMENT'
    },
    {
      title: '发货',
      icon: 'fas fa-shipping-fast',
      time: formatDate(order.value.shippingTime),
      completed: order.value.shippingTime != null,
      active: order.value.status === 'PENDING_SHIPMENT'
    },
    {
      title: '收货',
      icon: 'fas fa-box-open',
      time: formatDate(order.value.receiptConfirmationTime),
      completed: order.value.receiptConfirmationTime != null,
      active: order.value.status === 'SHIPPED'
    },
    {
      title: '完成',
      icon: 'fas fa-check-circle',
      time: formatDate(order.value.completionTime),
      completed: order.value.status === 'COMPLETED',
      active: order.value.status === 'RECEIVED'
    }
  ];

  // 如果是取消状态，替换时间线
  if (order.value.status === 'CANCELLED') {
    return [
      {
        title: '下单',
        icon: 'fas fa-shopping-cart',
        time: formatDate(order.value.createTime),
        completed: true,
        active: false
      },
      {
        title: '已取消',
        icon: 'fas fa-times-circle',
        time: formatDate(order.value.updateTime),
        completed: true,
        active: true
      }
    ];
  }

  // 如果是退货状态，添加退货节点
  if (['RETURN_REQUESTED', 'RETURN_APPROVED', 'RETURN_GOODS_RECEIVED', 'RETURNED', 'RETURN_REJECTED'].includes(order.value.status)) {
    const returnTimeline = [
      {
        title: '下单',
        icon: 'fas fa-shopping-cart',
        time: formatDate(order.value.createTime),
        completed: true,
        active: false
      },
      {
        title: '付款',
        icon: 'fas fa-credit-card',
        time: formatDate(order.value.paymentTime),
        completed: order.value.paymentTime != null,
        active: false
      },
      {
        title: '发货',
        icon: 'fas fa-shipping-fast',
        time: formatDate(order.value.shippingTime),
        completed: order.value.shippingTime != null,
        active: false
      },
      {
        title: '收货',
        icon: 'fas fa-box-open',
        time: formatDate(order.value.receiptConfirmationTime),
        completed: order.value.receiptConfirmationTime != null,
        active: false
      }
    ];

    if (order.value.status === 'RETURN_REQUESTED') {
      returnTimeline.push({
        title: '申请退货',
        icon: 'fas fa-undo',
        time: formatDate(order.value.returnRequestTime),
        completed: true,
        active: true
      });
    } else if (order.value.status === 'RETURN_APPROVED') {
      returnTimeline.push({
        title: '申请退货',
        icon: 'fas fa-undo',
        time: formatDate(order.value.returnRequestTime),
        completed: true,
        active: false
      });
      returnTimeline.push({
        title: '同意退货',
        icon: 'fas fa-check',
        time: formatDate(order.value.returnApproveTime),
        completed: true,
        active: true
      });
    } else if (order.value.status === 'RETURN_GOODS_RECEIVED') {
      returnTimeline.push({
        title: '申请退货',
        icon: 'fas fa-undo',
        time: formatDate(order.value.returnRequestTime),
        completed: true,
        active: false
      });
      returnTimeline.push({
        title: '收到退货',
        icon: 'fas fa-box',
        time: formatDate(order.value.returnGoodsReceivedTime),
        completed: true,
        active: true
      });
    } else if (order.value.status === 'RETURNED') {
      returnTimeline.push({
        title: '申请退货',
        icon: 'fas fa-undo',
        time: formatDate(order.value.returnRequestTime),
        completed: true,
        active: false
      });
      returnTimeline.push({
        title: '已退款',
        icon: 'fas fa-exchange-alt',
        time: formatDate(order.value.returnedTime),
        completed: true,
        active: true
      });
    } else if (order.value.status === 'RETURN_REJECTED') {
      returnTimeline.push({
        title: '申请退货',
        icon: 'fas fa-undo',
        time: formatDate(order.value.returnRequestTime),
        completed: true,
        active: false
      });
      returnTimeline.push({
        title: '拒绝退货',
        icon: 'fas fa-ban',
        time: formatDate(order.value.returnRejectTime),
        completed: true,
        active: true
      });
    }

    return returnTimeline;
  }

  return timeline;
});

// 支付订单
const payOrder = async () => {
  try {
    // 直接跳转到支付页面
        router.push(`/payment?id=${order.value.orderNo}`);
  } catch (err) {
    // 处理可能的其他错误
    console.error('支付跳转出错:', err);
  }
};

// 取消订单
const cancelOrder = async () => {
  try {
    await ElMessageBox.confirm('确定要取消此订单吗？', '取消订单', {
      confirmButtonText: '确定取消',
      cancelButtonText: '不取消',
      type: 'warning'
    });

    submitting.value = true;
    const response = await orderApi.cancelOrder(order.value.orderNo);

    if (response.data && response.data.code === 200) {
      ElMessage.success('订单已取消');
      // 刷新订单信息
      fetchOrderDetail();
    } else {
      ElMessage.error('取消订单失败：' + (response.data?.message || '未知错误'));
    }
  } catch (err) {
    if (err !== 'cancel') {
      console.error('取消订单出错:', err);
      ElMessage.error('取消订单失败：' + (err.message || '网络错误'));
    }
  } finally {
    submitting.value = false;
  }
};

// 确认收货
const confirmReceipt = async () => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'info'
    });

    submitting.value = true;
    const response = await orderApi.confirmReceipt(order.value.orderNo);

    if (response.data && response.data.code === 200) {
      ElMessage.success('已确认收货');
      // 刷新订单信息
      fetchOrderDetail();
    } else {
      ElMessage.error('确认收货失败：' + (response.data?.message || '未知错误'));
    }
  } catch (err) {
    if (err !== 'cancel') {
      console.error('确认收货出错:', err);
      ElMessage.error('确认收货失败：' + (err.message || '网络错误'));
    }
  } finally {
    submitting.value = false;
  }
};

// 申请退货
const requestReturn = () => {
  returnForm.orderNo = order.value.orderNo;
  returnForm.reason = '';
  returnDialogVisible.value = true;
};

// 跳转到评价页面
const goToReview = () => {
  router.push(`/order/review/${order.value.orderNo}`);
};

// 检查订单是否可以评价
const canReview = computed(() => {
  if (!order.value) return false;
  
  // 订单已收货或拒绝退货后，可以评价
  return order.value.status === 'RECEIVED' || order.value.status === 'RETURN_REJECTED';
});

// 提交退货申请
const submitReturnRequest = async () => {
  if (!returnForm.reason) {
    ElMessage.warning('请填写退货原因');
    return;
  }

  submitting.value = true;
  try {
    const response = await orderApi.requestReturn(returnForm.orderNo, returnForm.reason);

    if (response.data && response.data.code === 200) {
      ElMessage.success('退货申请已提交');
      returnDialogVisible.value = false;
      // 刷新订单信息
      fetchOrderDetail();
    } else {
      ElMessage.error('提交退货申请失败：' + (response.data?.message || '未知错误'));
    }
  } catch (err) {
    console.error('提交退货申请出错:', err);
    ElMessage.error('提交退货申请失败：' + (err.message || '网络错误'));
  } finally {
    submitting.value = false;
  }
};

// 显示发出退货弹窗
const showReturnShipDialog = () => {
  returnShipForm.orderNo = order.value.orderNo;
  returnShipForm.trackingInfo = '';
  returnShipDialogVisible.value = true;
};

// 提交发出退货
const submitReturnShip = async () => {
  submitting.value = true;
  try {
    const response = await orderApi.buyerReturnGoods(
      returnShipForm.orderNo, 
      ""  // 传递空字符串，保持兼容性
    );

    if (response.data && response.data.code === 200) {
      ElMessage.success('已确认发出退货');
      returnShipDialogVisible.value = false;
      // 刷新订单信息
      fetchOrderDetail();
    } else {
      ElMessage.error('确认发出退货失败：' + (response.data?.message || '未知错误'));
    }
  } catch (err) {
    console.error('确认发出退货出错:', err);
    ElMessage.error('确认发出退货失败：' + (err.message || '网络错误'));
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
  // 重置标志
  isCancelingOrder.value = false;
  fetchOrderDetail().then(() => {
    // 在订单详情获取成功后获取评价信息
    if (order.value) {
      fetchReviews();
    }
  });
});

// 组件销毁时清除定时器
onUnmounted(() => {
  stopCountdownTimer();
});

// 在script部分添加评价相关数据和函数
// 评价信息
const productReviews = ref([]);
const merchantReview = ref(null);
const buyerReview = ref(null);

// 获取订单所有评价信息
const fetchReviews = async () => {
  if (!order.value || !order.value.orderNo) return;
  
  try {
    // 尝试获取当前商家ID（如果订单是作为商家查看的）
    let merchantId = null;
    try {
      const userJson = localStorage.getItem('user');
      if (userJson) {
        const userData = JSON.parse(userJson);
        if (userData && userData.merchantId) {
          merchantId = userData.merchantId;
        }
      }
    } catch (e) {
      console.error('解析用户信息失败:', e);
    }
    
    const response = await reviewApi.getOrderReviews(order.value.orderNo, merchantId);
    console.log('订单评价返回数据:', response.data);
    
    if (response.data && response.data.code === 200 && response.data.data) {
      const reviewData = response.data.data;
      
      // 商品评价
      productReviews.value = reviewData.productReviews || [];
      
      // 商家服务评价 - 后端返回的字段名是merchantReview
      merchantReview.value = reviewData.merchantReview || reviewData.merchantServiceReview;
      
      if (merchantReview.value) {
        console.log('商家服务评价信息:', merchantReview.value);
      }
      
      // 商家对买家的评价
      buyerReview.value = reviewData.buyerReview;
      
      if (buyerReview.value) {
        console.log('商家对买家的评价信息:', buyerReview.value);
        // 处理数据库字段与前端显示字段的映射
        if (buyerReview.value.rating_score !== undefined) {
          buyerReview.value.ratingScore = buyerReview.value.rating_score;
        }
      }
      
      console.log('评价信息获取成功：', {
        productReviews: productReviews.value.length || 0,
        merchantReview: merchantReview.value ? 'yes' : 'no',
        buyerReview: buyerReview.value ? 'yes' : 'no'
      });
      
      // 如果存在评价，则标记为已评价
      hasReviewed.value = productReviews.value.length > 0 || merchantReview.value != null;
    }
  } catch (error) {
    console.error('获取评价信息出错:', error);
  }
};

// 获取商品评价平均评分
const getAverageProductRating = () => {
  if (!productReviews.value || productReviews.value.length === 0) return 5;
  
  const sum = productReviews.value.reduce((total, review) => total + review.ratingScore, 0);
  return sum / productReviews.value.length;
};

// 获取商品评价内容
const getProductReviewContent = () => {
  if (!productReviews.value || productReviews.value.length === 0) return '';
  
  // 返回第一条评价内容
  return productReviews.value[0].content || '';
};
</script>

<style scoped>
.order-detail-container {
  min-height: 90vh;
  background-color: #f8f9fa;
  padding: 20px 0;
}

.container {
  max-width: 1200px;
  padding: 0 30px;
  margin: 0 auto;
}

.back-nav {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  position: relative;
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
  margin: 0;
  font-size: 1.5rem;
}

.order-status-section {
  padding: 5px 0 20px;
}

.status-line {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.status-label {
  margin-right: 10px;
  font-weight: 600;
  color: #444;
  font-size: 1.1rem;
}

.order-info-line {
  margin-bottom: 12px;
  display: flex;
}

.info-label {
  width: 90px;
  font-weight: 500;
  color: #666;
}

.info-value {
  color: #333;
}

.badge {
  padding: 6px 12px;
  font-size: 0.9rem;
  font-weight: 500;
  border-radius: 4px;
}

.card {
  border: none;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  margin-bottom: 1.5rem;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  height: calc(100% - 1.5rem);
}

.card-header {
  background-color: white;
  border-bottom: 1px solid rgba(0,0,0,0.1);
  padding: 16px 20px;
  font-weight: 600;
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
}

.table {
  width: 100%;
  table-layout: fixed;
}

.product-table th, .product-table td {
  padding: 16px;
  vertical-align: middle;
}

.product-table thead {
  background-color: #f5f5f5;
}

.product-table th {
  font-weight: 600;
  color: #444;
  border-bottom: 1px solid #eee;
}

.product-image {
  width: 70px;
  height: 70px;
  object-fit: cover;
  border-radius: 6px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.08);
  transition: all 0.2s ease;
}

.product-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
  line-height: 1.4;
}

.specifications {
  color: #777;
  font-size: 0.85rem;
}

.item-total {
  color: #e74c3c;
  font-size: 1.05rem;
}

.price-details {
  padding: 10px 0;
}

.total-price {
  font-size: 1.1rem;
  padding-top: 10px;
}

.address-info p {
  margin-bottom: 12px;
  font-size: 0.95rem;
}

.action-btn {
  padding: 8px 20px;
  font-weight: 500;
  border-radius: 6px;
  transition: all 0.2s ease;
  margin: 0 5px;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.08);
}

.btn-primary {
  background: linear-gradient(45deg, #4568dc, #5d7ef0);
  border: none;
}

.btn-outline-primary {
  color: #4568dc;
  border-color: #4568dc;
}

.btn-outline-primary:hover {
  background-color: #4568dc;
  color: white;
}

.btn-outline-danger {
  color: #e74c3c;
  border-color: #e74c3c;
}

.btn-outline-danger:hover {
  background-color: #e74c3c;
  color: white;
}

.timeline-steps {
  display: flex;
  justify-content: space-between;
  margin-top: 2rem;
}

.timeline-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  position: relative;
}

.timeline-step:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 20px;
  width: 100%;
  height: 3px;
  background-color: #e0e0e0;
  left: 50%;
  z-index: 0;
}

.timeline-step.completed:not(:last-child)::after {
  background-color: #4CAF50;
}

.timeline-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #f8f9fa;
  border: 2px solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
}

.timeline-step.completed .timeline-icon {
  background-color: #E8F5E9;
  border-color: #4CAF50;
  color: #4CAF50;
  transform: scale(1.1);
}

.timeline-step.active .timeline-icon {
  background-color: #E3F2FD;
  border-color: #2196F3;
  color: #2196F3;
  transform: scale(1.2);
  box-shadow: 0 0 10px rgba(33, 150, 243, 0.4);
}

.timeline-content {
  text-align: center;
  margin-top: 10px;
}

.step-title {
  font-weight: 600;
  font-size: 0.9rem;
  color: #333;
}

.step-time {
  font-size: 0.75rem;
  color: #6c757d;
  margin-top: 4px;
}

/* 支付倒计时样式 */
.payment-countdown {
  background-color: #fef1f1;
  color: #e74c3c;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 0.85rem;
  display: inline-flex;
  align-items: center;
}

.payment-countdown strong {
  font-weight: 600;
  margin-left: 4px;
}

/* 退货申请信息样式 */
.return-request-info {
  padding: 10px 0;
  background-color: #f8f9fa;
  border-radius: 6px;
  padding: 12px 15px;
}

.return-info-title {
  margin-bottom: 12px;
  font-weight: 600;
  color: #333;
  font-size: 0.95rem;
}

.shipping-note {
  font-size: 12px;
  color: #52c41a;
  margin-left: 4px;
}

.points-note {
  font-size: 12px;
  color: #6c757d;
  margin-left: 4px;
}

.review-section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.review-item {
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 15px;
}

.rating-score {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.rating-score .label {
  margin-right: 10px;
  font-weight: 500;
  color: #666;
  min-width: 70px;
}

.review-text {
  margin-bottom: 10px;
}

.review-text p {
  margin: 0;
  color: #333;
  background-color: white;
  padding: 10px;
  border-radius: 4px;
  font-size: 14px;
}

.review-time {
  text-align: right;
  font-size: 12px;
  color: #999;
}
</style>
