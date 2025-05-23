<template>
  <div class="merchant-order-detail-container">
    <div class="container py-5 px-md-5">
      <div class="mb-4">
        <button class="btn-back" @click="goBack">
          <i class="fas fa-arrow-left"></i>
          <span>返回</span>
        </button>
      </div>
      
      <h2 class="page-title mb-4">商家订单详情</h2>

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
        <div class="card mb-4 order-status-card">
          <div class="card-body">
            <div class="merchant-badge mb-3">商家视图</div>
            <div class="order-status-section">
              <div class="status-line">
                <h5 class="status-label">订单状态:</h5>
                <span class="badge" :class="getStatusBadgeClass(order.status)">
                  {{ orderApi.getStatusText(order.status) }}
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
              
              <div class="order-info-line">
                <span class="info-label">买家:</span>
                <span class="info-value buyer-name">{{ order.userName || '未知用户' }}</span>
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
        
        <div class="row">
          <div class="col-md-8">
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
          </div>
          
          <div class="col-md-4">
            <!-- 买家信息 -->
            <div class="card mb-4">
              <div class="card-header">
                <h5 class="mb-0"><i class="fas fa-user me-2"></i>买家信息</h5>
              </div>
              <div class="card-body">
                <div class="buyer-info-item mb-3">
                  <div class="info-label">买家用户名:</div>
                  <div class="info-value">
                    <a href="javascript:;" @click="viewBuyerInfo" class="buyer-name-link">
                      {{ order.userName || '未知用户' }}
                    </a>
                  </div>
                </div>
                <div class="buyer-actions">
                  <!-- 移除查看买家评价信息按钮 -->
                </div>
              </div>
            </div>

            <!-- 订单金额信息 -->
            <div class="card mb-4">
              <div class="card-header">
                <h5 class="mb-0"><i class="fas fa-calculator me-2"></i>订单金额</h5>
              </div>
              <div class="card-body">
                <div class="price-details">
                  <div v-if="order.showTotalProductAmount" class="d-flex justify-content-between mb-2">
                    <span class="info-label">商品总额：</span>
                    <span>¥{{ productTotalAmount }}</span>
                  </div>
                  <div v-if="order.showPointsDeduction && order.pointsDeductionAmount > 0" class="d-flex justify-content-between mb-2">
                    <span class="info-label">积分抵扣：</span>
                    <span>-¥{{ order.pointsDeductionAmount || 0 }}</span>
                  </div>
                  <div class="d-flex justify-content-between mb-2">
                    <span class="info-label">平台服务费：</span>
                    <span>¥{{ order.platformCommissionAmount || 0 }}</span>
                  </div>
                  <hr />
                  <div class="d-flex justify-content-between fw-bold total-price">
                    <span class="info-label">实付金额：</span>
                    <span class="text-danger fs-5">¥{{ order.actualPaymentAmount }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

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

        <!-- 商家订单操作区 -->
        <div class="card mb-4">
          <div class="card-header">
            <h5 class="mb-0"><i class="fas fa-cog me-2"></i>订单操作</h5>
          </div>
          <div class="card-body">
            <div class="order-actions">
              <button
                v-if="order.status === 'PENDING_SHIPMENT'"
                @click="showShippingDialog"
                class="btn btn-primary action-btn"
              >
                <i class="fas fa-shipping-fast me-1"></i> 确认发货
              </button>

              <button
                v-if="order.status === 'RETURN_REQUESTED'"
                @click="showReturnDialog(true)"
                class="btn btn-success action-btn me-2"
              >
                <i class="fas fa-check me-1"></i> 同意退货
              </button>

              <button
                v-if="order.status === 'RETURN_REQUESTED'"
                @click="showReturnDialog(false)"
                class="btn btn-danger action-btn"
              >
                <i class="fas fa-times me-1"></i> 拒绝退货
              </button>

              <button
                v-if="order.status === 'RETURN_APPROVED'"
                @click="confirmReturnGoodsReceived"
                class="btn btn-success action-btn"
              >
                <i class="fas fa-box-open me-1"></i> 确认收到退货
              </button>
              
              <button
                v-if="(order.status === 'RECEIVED' || order.status === 'COMPLETED' || order.status === 'RETURN_REJECTED') && !buyerReview"
                @click="showReviewBuyerDialog"
                class="btn btn-info action-btn"
              >
                <i class="fas fa-star me-1"></i> 评价买家
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 评价信息 -->
      <div v-if="order && (order.status === 'COMPLETED' || order.status === 'RECEIVED' || order.status === 'RETURN_REJECTED') && (buyerReview || productReviews?.length > 0 || merchantReview)" class="review-section mt-4">
        <h3 class="section-title">评价信息</h3>
        <div class="card">
          <div class="card-body">
            <!-- 商家对买家的评价 -->
            <div v-if="buyerReview" class="buyer-review mb-4">
              <h5 class="review-title">您对买家的评价</h5>
              <div class="review-content">
                <div class="rating-score">
                  <span class="label">评分：</span>
                  <el-rate 
                    v-model="buyerReview.ratingScore" 
                    disabled 
                    show-score 
                    text-color="#ff9900">
                  </el-rate>
                </div>
                <div v-if="buyerReview.content" class="review-text">
                  <span class="label">评价内容：</span>{{ buyerReview.content }}
                </div>
              </div>
            </div>
            
            <!-- 买家对商品的评价 -->
            <div v-if="productReviews && productReviews.length > 0" class="product-reviews mb-4">
              <h5 class="review-title">买家对商品的评价</h5>
              <div v-for="(review, index) in productReviews" :key="index" class="product-review-item">
                <div class="rating-score">
                  <span class="label">商品评分：</span>
                  <el-rate 
                    v-model="review.ratingScore" 
                    disabled 
                    show-score 
                    text-color="#ff9900">
                  </el-rate>
                </div>
                <div v-if="review.content" class="review-text">
                  <span class="label">评价内容：</span>{{ review.content }}
                </div>
              </div>
            </div>
            
            <!-- 买家对商家服务的评价 -->
            <div v-if="merchantReview" class="merchant-review">
              <h5 class="review-title">买家对您服务的评价</h5>
              <div class="review-content">
                <div class="rating-score">
                  <span class="label">服务评分：</span>
                  <el-rate 
                    v-model="merchantReview.serviceAttitudeRating" 
                    disabled 
                    show-score 
                    text-color="#ff9900">
                  </el-rate>
                </div>
                <div v-if="merchantReview.content" class="review-text">
                  <span class="label">评价内容：</span>{{ merchantReview.content }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 发货弹窗 -->
    <el-dialog
      v-model="shipDialogVisible"
      title="确认发货"
      width="500px"
    >
      <div class="text-center">
        <p class="mb-3">是否确认发货该订单？</p>
        <p class="text-muted">订单号：{{ order?.orderNo }}</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="shipDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmShip" :loading="submitting">确认发货</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 退货处理弹窗 -->
    <el-dialog
      v-model="returnDialogVisible"
      :title="isApproveReturn ? '同意退货申请' : '拒绝退货申请'"
      width="500px"
    >
      <el-form ref="returnFormRef" :model="returnForm" label-width="100px">
        <el-form-item label="处理备注" prop="remark">
          <el-input
            v-model="returnForm.remark"
            type="textarea"
            :rows="4"
            :placeholder="isApproveReturn ? '请输入同意退货的备注信息，如退货地址等' : '请输入拒绝退货的原因'"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="returnDialogVisible = false">取消</el-button>
          <el-button
            :type="isApproveReturn ? 'success' : 'danger'"
            @click="confirmReturnProcess"
            :loading="submitting"
          >
            {{ isApproveReturn ? '确认同意' : '确认拒绝' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 评价买家弹窗 -->
    <el-dialog
      v-model="reviewBuyerDialogVisible"
      title="评价买家"
      width="500px"
    >
      <el-form ref="reviewBuyerFormRef" :model="reviewBuyerForm" label-width="100px">
        <el-form-item label="评分" prop="ratingScore">
          <el-rate
            v-model="reviewBuyerForm.ratingScore"
            show-score
            :max="5"
            :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
          ></el-rate>
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input
            v-model="reviewBuyerForm.content"
            type="textarea"
            :rows="4"
            placeholder="请填写您对买家的评价，如交易态度、沟通顺畅度等"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewBuyerDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBuyerReview" :loading="submitting">提交评价</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { orderApi } from '@/api/order';
import { reviewApi } from '@/api/review';
import { ElMessage, ElMessageBox } from 'element-plus';

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const error = ref(null);
const order = ref(null);
const submitting = ref(false);

// 发货弹窗
const shipDialogVisible = ref(false);

// 退货处理弹窗
const returnDialogVisible = ref(false);
const isApproveReturn = ref(true);
const returnForm = reactive({
  orderNo: '',
  remark: ''
});

// 评价信息
const buyerReview = ref(null);
const productReviews = ref([]);
const merchantReview = ref(null);

// 评价买家弹窗
const reviewBuyerDialogVisible = ref(false);
const reviewBuyerForm = reactive({
  ratingScore: 0,
  content: ''
});

// 获取订单详情
const fetchOrderDetail = async () => {
  loading.value = true;
  error.value = null;

  try {
    const orderNo = route.params.orderNo;
    if (!orderNo) {
      error.value = '订单号不存在';
      return;
    }

    const response = await orderApi.getOrderDetail(orderNo);
    if (response.data && response.data.code === 200) {
      order.value = response.data.data;
      console.log('商家订单详情:', order.value);
    } else {
      error.value = '获取订单详情失败：' + (response.data?.message || '未知错误');
    }
  } catch (err) {
    console.error('获取订单详情出错:', err);
    error.value = '获取订单详情失败：' + (err.message || '网络错误');
  } finally {
    loading.value = false;
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

// 订单的商品总额
const productTotalAmount = computed(() => {
  if (!order.value) return 0;
  return order.value.totalProductAmount || 0;
});

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
        time: formatDate(order.value.cancelTime),
        completed: true,
        active: true
      }
    ];
  }

  // 如果是退货状态，添加退货节点
  if (['RETURN_REQUESTED', 'RETURN_APPROVED', 'RETURN_GOODS_RECEIVED', 'RETURNED', 'RETURN_REJECTED'].includes(order.value.status)) {
    let returnTimeline;
    
    if (order.value.status === 'RETURN_REQUESTED') {
      returnTimeline = timeline.slice(0, 4); // 保留到收货
      returnTimeline.push({
        title: '申请退货',
        icon: 'fas fa-undo',
        time: formatDate(order.value.returnRequestTime),
        completed: true,
        active: true
      });
    } else if (order.value.status === 'RETURN_APPROVED') {
      returnTimeline = timeline.slice(0, 4);
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
    } else if (order.value.status === 'RETURN_REJECTED') {
      returnTimeline = timeline.slice(0, 4);
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
    } else if (order.value.status === 'RETURNED') {
      returnTimeline = timeline.slice(0, 4);
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
    }
    
    return returnTimeline;
  }

  return timeline;
});

// 获取评价信息
const fetchReviews = async () => {
  try {
    // 从localStorage获取merchantId
    const merchantId = localStorage.getItem('merchantId');
    if (!merchantId) {
      console.error('未找到merchantId，无法获取评价信息');
      return;
    }

    const response = await reviewApi.getOrderReviews(order.value.orderNo, merchantId);
    if (response.data && response.data.code === 200) {
      const reviewData = response.data.data;
      
      buyerReview.value = reviewData.buyerReview;
      productReviews.value = reviewData.productReviews || [];
      merchantReview.value = reviewData.merchantReview;
      
      console.log('评价信息获取成功：', reviewData);
    } else {
      console.error('获取评价信息失败:', response.data?.message);
    }
  } catch (err) {
    console.error('获取评价信息出错:', err);
  }
};

// 发货相关
const shipOrder = () => {
  shipDialogVisible.value = true;
};

const confirmShip = async () => {
  submitting.value = true;
  try {
    const response = await orderApi.shipOrder(order.value.orderNo);
    
    if (response.data && response.data.code === 200) {
      ElMessage.success('订单发货成功');
      shipDialogVisible.value = false;
      fetchOrderDetail(); // 刷新订单信息
    } else {
      ElMessage.error('订单发货失败：' + (response.data?.message || '未知错误'));
    }
  } catch (err) {
    console.error('订单发货出错:', err);
    ElMessage.error('订单发货失败：' + (err.message || '网络错误'));
  } finally {
    submitting.value = false;
  }
};

// 退货处理相关
const approveReturn = () => {
  returnForm.orderNo = order.value.orderNo;
  returnForm.remark = '';
  isApproveReturn.value = true;
  returnDialogVisible.value = true;
};

const rejectReturn = () => {
  returnForm.orderNo = order.value.orderNo;
  returnForm.remark = '';
  isApproveReturn.value = false;
  returnDialogVisible.value = true;
};

const confirmReturnProcess = async () => {
  if (!returnForm.remark) {
    ElMessage.warning('请填写处理备注');
    return;
  }

  submitting.value = true;
  try {
    const response = await orderApi.processReturnRequest(
      returnForm.orderNo,
      isApproveReturn.value,
      returnForm.remark
    );
    
    if (response.data && response.data.code === 200) {
      ElMessage.success(isApproveReturn.value ? '已同意退货申请' : '已拒绝退货申请');
      returnDialogVisible.value = false;
      fetchOrderDetail(); // 刷新订单信息
    } else {
      ElMessage.error('处理退货申请失败：' + (response.data?.message || '未知错误'));
    }
  } catch (err) {
    console.error('处理退货申请出错:', err);
    ElMessage.error('处理退货申请失败：' + (err.message || '网络错误'));
  } finally {
    submitting.value = false;
  }
};

// 确认收到退货
const confirmReturnGoodsReceived = async () => {
  try {
    await ElMessageBox.confirm('确认已收到买家的退货？', '确认收货', {
      confirmButtonText: '确认收到',
      cancelButtonText: '取消',
      type: 'info'
    });

    submitting.value = true;
    const response = await orderApi.confirmReturnReceived(order.value.orderNo);
    
    if (response.data && response.data.code === 200) {
      ElMessage.success('已确认收到退货');
      fetchOrderDetail(); // 刷新订单信息
    } else {
      ElMessage.error('确认收到退货失败：' + (response.data?.message || '未知错误'));
    }
  } catch (err) {
    if (err !== 'cancel') {
      console.error('确认收到退货出错:', err);
      ElMessage.error('确认收到退货失败：' + (err.message || '网络错误'));
    }
  } finally {
    submitting.value = false;
  }
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 页面加载时，自动获取订单信息
onMounted(async () => {
  try {
    loading.value = true;
    await fetchOrderDetail();
    await fetchReviews();
  } catch (error) {
    console.error(error);
    ElMessage.error('获取订单详情失败');
  } finally {
    loading.value = false;
  }
});

// 显示评价买家弹窗
const showReviewBuyerDialog = () => {
  reviewBuyerForm.ratingScore = 5; // 默认5星
  reviewBuyerForm.content = '';
  reviewBuyerDialogVisible.value = true;
};

// 提交买家评价
const submitBuyerReview = async () => {
  if (!reviewBuyerForm.ratingScore) {
    ElMessage.warning('请为买家评分');
    return;
  }

  submitting.value = true;
  try {
    const reviewData = {
      ratingScore: reviewBuyerForm.ratingScore,
      content: reviewBuyerForm.content
    };

    const response = await reviewApi.submitBuyerReview(order.value.orderNo, reviewData);
    
    if (response.data && response.data.code === 200) {
      ElMessage.success('评价提交成功');
      reviewBuyerDialogVisible.value = false;
      
      // 刷新评价和订单信息
      await fetchReviews();
      await fetchOrderDetail();
    } else {
      ElMessage.error('评价提交失败：' + (response.data?.message || '未知错误'));
    }
  } catch (err) {
    console.error('评价提交出错:', err);
    ElMessage.error('评价提交失败：' + (err.message || '网络错误'));
  } finally {
    submitting.value = false;
  }
};

// 显示发货弹窗
const showShippingDialog = () => {
  shipDialogVisible.value = true;
};

// 显示退货处理弹窗
const showReturnDialog = (approve) => {
  returnForm.orderNo = order.value.orderNo;
  returnForm.remark = '';
  isApproveReturn.value = approve;
  returnDialogVisible.value = true;
};

// 查看买家信息详情
const viewBuyerInfo = () => {
  // 检查订单中的用户ID
  if (order.value?.userId) {
    // 保存身份信息到localStorage，确保跳转后的页面能获取到
    try {
      // 检查用户信息是否存在
      const userJson = localStorage.getItem('user');
      if (userJson) {
        const userData = JSON.parse(userJson);
        if (userData && userData.role === 'MERCHANT') {
          console.log('商家身份确认，准备跳转查看买家信息');
        }
      }
      
      // 存储当前商家ID，供下一个页面使用
      if (order.value.merchantId) {
        localStorage.setItem('currentViewingMerchantId', order.value.merchantId);
      }
      
      // 添加访问时间戳
      localStorage.setItem('buyerInfoAccessTime', new Date().getTime());
      
      // 导航到买家信息页
      const buyerId = order.value.userId;
      router.push(`/buyer-info/${buyerId}`);
    } catch (err) {
      console.error('准备跳转查看买家信息时出错:', err);
      ElMessage.error('无法查看买家信息，请确认登录状态');
    }
  } else {
    ElMessage.warning('找不到买家信息');
  }
};
</script>

<style scoped>
.merchant-order-detail-container {
  min-height: 90vh;
  background-color: #f8f9fa;
  padding: 0 15px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding-left: 30px;
  padding-right: 30px;
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

.order-status-card {
  background: linear-gradient(135deg, #f5f7ff 0%, #f0f4ff 100%);
}

.merchant-badge {
  display: inline-block;
  background-color: #4568dc;
  color: white;
  padding: 5px 10px;
  border-radius: 4px;
  font-weight: 500;
  font-size: 0.8rem;
  margin-bottom: 10px;
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

.buyer-name {
  color: #4568dc;
  font-weight: 500;
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

.buyer-info-item {
  margin-bottom: 15px;
}

.buyer-info-item .info-label {
  font-weight: 600;
  color: #555;
  margin-bottom: 5px;
}

.buyer-info-item .info-value {
  color: #333;
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

.btn-success {
  background: linear-gradient(45deg, #28a745, #5cce85);
  border: none;
}

.btn-danger {
  background: linear-gradient(45deg, #dc3545, #f27380);
  border: none;
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

@media (max-width: 768px) {
  .timeline-steps {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .timeline-step {
    width: 100%;
    flex-direction: row;
    margin-bottom: 1rem;
  }
  
  .timeline-step:not(:last-child)::after {
    display: none;
  }
  
  .timeline-content {
    margin-left: 15px;
    margin-top: 0;
    text-align: left;
  }
}

.review-section {
  margin-top: 30px;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 15px;
  color: #333;
  position: relative;
  padding-left: 15px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: #4568dc;
  border-radius: 4px;
}

.review-content {
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
  margin-top: 10px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.review-title {
  margin: 10px 0;
  padding: 10px 0;
  border-bottom: 1px dashed #e0e0e0;
  color: #555;
  font-weight: 600;
  font-size: 1.1rem;
}

.rating-score {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.label {
  font-weight: 500;
  color: #555;
  margin-right: 10px;
  min-width: 80px;
}

.review-text {
  color: #666;
  display: flex;
  align-items: flex-start;
}

.product-review-item {
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
  margin-bottom: 15px;
}

.product-review-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.buyer-name-link {
  color: #4568dc;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.2s ease;
}

.buyer-name-link:hover {
  color: #2c4bb3;
  text-decoration: underline;
}
</style> 