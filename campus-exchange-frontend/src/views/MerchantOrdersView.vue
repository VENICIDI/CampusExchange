<template>
  <div class="merchant-orders-container">
    <div class="container py-5 px-md-5">
      <div class="page-header mb-4">
        <h2 class="page-title"><i class="fas fa-store-alt me-2"></i>商家订单管理</h2>
        <p class="text-muted small">管理和处理您的店铺订单，确保买家满意度</p>
      </div>
      
      <!-- 订单状态筛选 -->
      <div class="status-filter mb-5 pt-4">
        <div class="btn-group d-flex flex-wrap">
          <button 
            v-for="(label, status) in statusOptions" 
            :key="status" 
            :class="['btn filter-btn mb-2 me-3', currentStatus === status ? 'btn-primary' : 'btn-outline-primary']"
            @click="filterByStatus(status)"
          >
            {{ label }}
            <span v-if="getStatusCount(status) > 0" class="badge bg-danger ms-1">{{ getStatusCount(status) }}</span>
          </button>
        </div>
      </div>
      
      <!-- 加载中状态 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">加载中...</span>
        </div>
        <p class="mt-2">正在加载订单数据...</p>
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="orders.length === 0" class="empty-state text-center py-5 rounded">
        <i class="fas fa-box-open fa-4x text-muted mb-3"></i>
        <h4>暂无订单</h4>
        <p class="text-muted">还没有收到任何订单，等待买家下单吧</p>
      </div>
      
      <!-- 订单列表 -->
      <div v-else class="order-list">
        <div v-for="order in orders" :key="order.id" class="order-card mb-4 bg-white rounded shadow-sm" @click="navigateToOrderDetail(order.orderNo)">
          <!-- 订单头部信息 -->
          <div class="order-header d-flex justify-content-between align-items-center p-3 border-bottom">
            <div class="order-info">
              <span class="order-number">订单号：{{ order.orderNo }}</span>
              <span class="order-date ms-5">下单时间：{{ formatDate(order.createTime) }}</span>
              <span class="order-user ms-5 buyer-info">
                <i class="fas fa-user me-1"></i>
                买家：{{ order.userName || '未知用户' }}
              </span>
              <span class="ms-5 trade-type">
                <i class="fas" :class="order.tradeType === 'EXPRESS' ? 'fa-truck' : 'fa-handshake'"></i>
                {{ order.tradeType === 'EXPRESS' ? '快递配送' : '线下交易' }}
              </span>
            </div>
            <div class="order-status">
              <span class="badge" :class="getStatusBadgeClass(order.status)">
                {{ orderApi.getStatusText(order.status) }}
              </span>
            </div>
          </div>
          
          <!-- 订单商品表格 -->
          <div class="order-body" @click.stop>
            <div class="table-responsive">
              <table class="table table-hover mb-0 text-center">
                <thead class="table-light">
                  <tr>
                    <th style="width: 12%">商品图片</th>
                    <th style="width: 45%">商品名称</th>
                    <th style="width: 15%">单价</th>
                    <th style="width: 10%">数量</th>
                    <th style="width: 18%">小计</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in order.orderItems" :key="item.id" class="product-row">
                    <td class="align-middle text-center">
                      <img :src="item.productImage || 'https://via.placeholder.com/60/e0e0e0/666666?text=商品'" 
                           :alt="item.productName" 
                           class="product-img">
                    </td>
                    <td class="align-middle text-center">
                      <div class="product-name">{{ item.productName }}</div>
                      <div class="item-specs text-muted small">{{ item.specifications || '无规格信息' }}</div>
                    </td>
                    <td class="align-middle text-center">¥{{ item.price }}</td>
                    <td class="align-middle text-center">{{ item.quantity }}</td>
                    <td class="align-middle text-center"><strong>¥{{ (item.price * item.quantity).toFixed(2) }}</strong></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          
          <!-- 收货信息和订单金额 -->
          <div class="order-info-bar p-3 border-top bg-light">
            <div class="row w-100 align-items-center">
              <!-- 左侧：交易信息 -->
              <div class="col-md-6 col-sm-12 mb-md-0 mb-3">
                <div v-if="order.tradeType === 'EXPRESS' && order.orderAddress" class="delivery-info d-flex align-items-center mb-2">
                  <strong class="me-3 text-nowrap"><i class="fas fa-map-marker-alt me-1"></i>收货信息:</strong>
                  <div class="delivery-detail d-flex align-items-center flex-1 text-truncate">
                    <span class="recipient fw-semibold badge bg-light text-dark me-2">{{ order.orderAddress.recipient }}</span>
                    <span class="phone text-secondary badge bg-light text-dark me-2">{{ order.orderAddress.phone }}</span>
                    <span class="address text-muted text-truncate">{{ order.orderAddress.fullAddress }}</span>
                  </div>
                </div>
                
                <div v-if="order.tradeType === 'OFFLINE'" class="delivery-info d-flex align-items-center mb-2">
                  <strong class="me-3 text-nowrap"><i class="fas fa-map-pin me-1"></i>线下交易:</strong>
                  <div class="delivery-detail d-flex align-items-center flex-1">
                    <span class="me-3 badge bg-light text-dark"><i class="fas fa-map-marker me-1"></i>{{ formatLocation(order.offlineMeetingLocation) }}</span>
                    <span class="badge bg-light text-dark"><i class="fas fa-clock me-1"></i>{{ formatDate(order.offlineMeetingTime) }}</span>
                  </div>
                </div>
              </div>
              
              <!-- 右侧：订单操作和金额 -->
              <div class="col-md-6 col-sm-12">
                <div class="d-flex justify-content-md-end justify-content-start align-items-center">
                  <!-- 订单金额 -->
                  <div class="order-summary text-end">
                    <div class="text-muted">共{{ getTotalItemCount(order) }}件商品</div>
                    <div class="commission">平台佣金：<span class="text-primary">¥{{ formatCurrency(order.platformCommissionAmount) }}</span></div>
                    <div class="total-price">实付款：<strong class="text-danger">¥{{ formatCurrency(order.actualPaymentAmount) }}</strong></div>
                  </div>
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
        <p class="text-muted">订单号：{{shipForm.orderNo}}</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="shipDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmShip" :loading="submitting">确认发货</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 线下交易备货完成弹窗 -->
    <el-dialog
      v-model="offlineShipDialogVisible"
      title="确认备货完成"
      width="500px"
    >
      <div class="text-center">
        <p class="mb-3">确认已完成备货，等待买家线下取货？</p>
        <p class="text-muted">订单号：{{shipForm.orderNo}}</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="offlineShipDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmOfflineShip" :loading="submitting">确认备货完成</el-button>
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

    <!-- 买家评价对话框 -->
    <el-dialog
      v-model="buyerReviewDialogVisible"
      title="评价买家"
      width="500px">
      <div v-if="currentOrder" class="buyer-review-form">
        <div class="order-info">
          <div>订单号: {{ currentOrder.orderNo }}</div>
          <div>买家: {{ currentOrder.userName }}</div>
        </div>
        
        <div class="rating-section">
          <div class="rating-label">买家评分:</div>
          <div class="rating-stars">
            <el-rate 
              v-model="buyerReview.ratingScore"
              :colors="['#FFECB3', '#FFD54F', '#FFC107']" 
              :allow-half="true"
              :show-score="true"
            ></el-rate>
          </div>
        </div>
        
        <div class="comment-section">
          <el-form-item label="评价内容">
            <el-input 
              v-model="buyerReview.comment" 
              type="textarea" 
              :rows="3"
              placeholder="请输入对买家的评价内容（可选）"
            ></el-input>
          </el-form-item>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="buyerReviewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBuyerReview" :loading="submitting">提交评价</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { orderApi } from '@/api/order';
import { reviewApi } from '@/api/review';
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();
const loading = ref(true);
const orders = ref([]);
const total = ref(0);
const currentStatus = ref(null);
const submitting = ref(false);

// 订单状态选项
const statusOptions = {
  null: '全部订单',
  'PENDING_PAYMENT': '待付款',
  'PENDING_SHIPMENT': '待发货',
  'SHIPPED': '待收货',
  'RECEIVED': '待评价',
  'COMPLETED': '已完成',
  'CANCELLED': '已取消',
  'AFTERSALE': '售后处理'
};

// 存储各状态的订单数量
const statusCounts = ref({
  'PENDING_PAYMENT': 0,
  'PENDING_SHIPMENT': 0,
  'SHIPPED': 0,
  'RECEIVED': 0,
  'COMPLETED': 0,
  'CANCELLED': 0,
  'RETURN_REQUESTED': 0,
  'RETURN_APPROVED': 0,
  'RETURN_GOODS_RECEIVED': 0,
  'RETURNED': 0,
  'RETURN_REJECTED': 0
});

// 发货相关
const shipDialogVisible = ref(false);
const offlineShipDialogVisible = ref(false);
const shipForm = ref({
  orderNo: '',
  trackingNo: '',
  expressCompany: ''
});

// 退货处理相关
const returnDialogVisible = ref(false);
const isApproveReturn = ref(true);
const returnForm = ref({
  orderNo: '',
  approve: true,
  remark: ''
});

// 买家评价相关
const buyerReviewDialogVisible = ref(false);
const currentOrder = ref(null);
const buyerReview = ref({
  orderNo: '',
  ratingScore: 5,
  comment: ''
});

// 获取商家订单列表
const fetchOrders = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: 1,
      pageSize: 1000, // 设置一个足够大的数值，一次获取所有订单
      status: currentStatus.value === null ? undefined : currentStatus.value
    };
    
    const response = await orderApi.getMerchantOrders(params);
    if (response.data && response.data.code === 200) {
      orders.value = response.data.data.records || [];
      total.value = response.data.data.total || 0;
      
      // 只有当查询全部订单时，使用返回的total作为全部订单的数量
      if (currentStatus.value === null) {
        // 用于显示全部订单的数量
        orders.value._totalCount = total.value;
      }
      
      // 统计各状态订单数量
      await countOrdersByStatus();
    } else {
      ElMessage.error('获取订单列表失败：' + (response.data?.message || '未知错误'));
    }
  } catch (error) {
    console.error('获取订单列表出错：', error);
    ElMessage.error('获取订单列表失败：' + (error.message || '网络错误'));
  } finally {
    loading.value = false;
  }
};

// 统计各状态订单数量
const countOrdersByStatus = async () => {
  try {
    // 调用API获取订单状态计数
    const response = await orderApi.getOrderStatusCounts();
    
    if (response.data && response.data.code === 200) {
      const counts = response.data.data;
      
      // 更新状态计数
      Object.keys(counts).forEach(status => {
        if (statusCounts.value[status] !== undefined) {
          statusCounts.value[status] = counts[status];
        }
      });
      
      // 同时也获取全部订单数量
      let totalCount = 0;
      for (const statusKey in statusCounts.value) {
        totalCount += statusCounts.value[statusKey] || 0;
      }
      console.log("统计到的所有订单总数:", totalCount);
    } else {
      console.error('获取订单状态统计失败：', response.data?.message || '未知错误');
    }
  } catch (error) {
    console.error('获取订单状态统计失败：', error);
  }
};

// 获取订单状态统计数量
const getStatusCount = (status) => {
  // 全部订单选项的status值可能是字符串"null"或null值
  if (status === null || status === "null") {
    // 全部订单就是所有状态订单数量的总和
    let totalCount = 0;
    for (const statusKey in statusCounts.value) {
      totalCount += statusCounts.value[statusKey] || 0;
    }
    return totalCount;
  }
  
  // 处理"售后处理"的情况
  if (status === 'AFTERSALE') {
    // 计算所有售后相关状态订单的总和
    const afterSaleStatuses = ['RETURN_REQUESTED', 'RETURN_APPROVED', 'RETURN_GOODS_RECEIVED', 'RETURNED', 'RETURN_REJECTED'];
    return afterSaleStatuses.reduce((sum, status) => sum + (statusCounts.value[status] || 0), 0);
  }
  
  return statusCounts.value[status] || 0;
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

// 格式化地址
const formatLocation = (location) => {
  return location || '未设置';
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
    'RETURN_REJECTED': 'bg-danger'
  };
  return statusClassMap[status] || 'bg-secondary';
};

// 计算订单商品总数量
const getTotalItemCount = (order) => {
  if (!order.orderItems) return 0;
  return order.orderItems.reduce((sum, item) => sum + item.quantity, 0);
};

// 导航到订单详情页
const navigateToOrderDetail = (orderNo) => {
  if (!orderNo) return;
  router.push(`/merchant/order/${orderNo}`);
};

// 按状态筛选订单
const filterByStatus = (status) => {
  if (currentStatus.value === status) return;
  
  if (status === 'AFTERSALE') {
    // 处理售后状态特殊筛选 - 前端筛选售后相关状态
    currentStatus.value = null; // 先获取全部
    fetchOrders().then(() => {
      // 只显示售后相关状态的订单
      const afterSaleStatuses = ['RETURN_REQUESTED', 'RETURN_APPROVED', 'RETURN_GOODS_RECEIVED', 'RETURNED', 'RETURN_REJECTED'];
      orders.value = orders.value.filter(order => afterSaleStatuses.includes(order.status));
    });
  } else {
  currentStatus.value = status;
  fetchOrders();
  }
};

// 打开发货弹窗
const openShipDialog = (orderNo) => {
  shipForm.value = {
    orderNo: orderNo,
    trackingNo: '',
    expressCompany: ''
  };
  shipDialogVisible.value = true;
};

// 打开线下交易备货完成弹窗
const openOfflineShipDialog = (orderNo) => {
  shipForm.value = {
    orderNo: orderNo
  };
  offlineShipDialogVisible.value = true;
};

// 确认发货
const confirmShip = async () => {
  submitting.value = true;
  try {
    const response = await orderApi.shipOrder(shipForm.value);
    if (response.data && response.data.code === 200) {
      ElMessage.success('订单发货成功');
      shipDialogVisible.value = false;
      await countOrdersByStatus(); // 更新订单统计
      await fetchOrders(); // 刷新订单列表
    } else {
      ElMessage.error('订单发货失败：' + (response.data?.message || '未知错误'));
    }
  } catch (error) {
    ElMessage.error('订单发货失败：' + (error.message || '网络错误'));
  } finally {
    submitting.value = false;
  }
};

// 确认线下交易备货完成
const confirmOfflineShip = async () => {
  submitting.value = true;
  try {
    // 对于线下交易，仍然使用发货接口，只是不需要物流信息
    const response = await orderApi.shipOrder({
      orderNo: shipForm.value.orderNo,
      isOffline: true
    });
    
    if (response.data && response.data.code === 200) {
      ElMessage.success('已确认备货完成，等待买家上门取货');
      offlineShipDialogVisible.value = false;
      await countOrdersByStatus(); // 更新订单统计
      await fetchOrders(); // 刷新订单列表
    } else {
      ElMessage.error('确认备货失败：' + (response.data?.message || '未知错误'));
    }
  } catch (error) {
    ElMessage.error('确认备货失败：' + (error.message || '网络错误'));
  } finally {
    submitting.value = false;
  }
};

// 处理退货请求
const handleReturnRequest = (orderNo, approve) => {
  isApproveReturn.value = approve;
  returnForm.value = {
    orderNo: orderNo,
    approve: approve,
    remark: ''
  };
  returnDialogVisible.value = true;
};

// 确认处理退货请求
const confirmReturnProcess = async () => {
  submitting.value = true;
  try {
    const response = await orderApi.processReturnRequest({
      orderNo: returnForm.value.orderNo,
      approve: returnForm.value.approve,
      remark: returnForm.value.remark
    });
    
    if (response.data && response.data.code === 200) {
      ElMessage.success(returnForm.value.approve ? '已同意退货申请' : '已拒绝退货申请');
      returnDialogVisible.value = false;
      await countOrdersByStatus(); // 更新订单统计
      await fetchOrders(); // 刷新订单列表
    } else {
      ElMessage.error('处理退货申请失败：' + (response.data?.message || '未知错误'));
    }
  } catch (error) {
    ElMessage.error('处理退货申请失败：' + (error.message || '网络错误'));
  } finally {
    submitting.value = false;
  }
};

// 显示买家评价弹窗
const showBuyerReviewDialog = (order) => {
  currentOrder.value = order;
  buyerReview.value = {
    orderNo: order.orderNo,
    ratingScore: 5,
    comment: ''
  };
  buyerReviewDialogVisible.value = true;
};

// 提交买家评价
const submitBuyerReview = async () => {
  if (buyerReview.value.ratingScore < 1) {
    ElMessage.warning('请至少给出1星评价');
    return;
  }
  
  submitting.value = true;
  try {
    const response = await reviewApi.submitBuyerReview(buyerReview.value.orderNo, {
      ratingScore: buyerReview.value.ratingScore,
      content: buyerReview.value.comment
    });
    
    if (response.data && response.data.code === 200) {
      ElMessage.success('评价买家成功');
      buyerReviewDialogVisible.value = false;
      
      // 强制刷新整个订单列表
      currentStatus.value = null; // 重置筛选条件
      await countOrdersByStatus(); // 恢复对countOrdersByStatus的调用
      await fetchOrders(); // 重新获取订单列表
    } else {
      ElMessage.error('评价买家失败：' + (response.data?.message || '未知错误'));
    }
  } catch (error) {
    ElMessage.error('评价买家失败：' + (error.message || '网络错误'));
  } finally {
    submitting.value = false;
  }
};

// 格式化货币显示
const formatCurrency = (value) => {
  if (value === null || value === undefined) return '--';
  return parseFloat(value).toFixed(2);
};

// 组件挂载时获取数据
onMounted(async () => {
  // 先获取订单状态计数
  await countOrdersByStatus();
  // 再获取订单列表
  await fetchOrders();
});
</script>

<style scoped>
.merchant-orders-container {
  background-color: #f8f9fa;
  min-height: 100vh;
  padding: 0 15px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding-bottom: 2rem;
}

.page-header {
  margin-bottom: 2rem;
}

.page-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 0.5rem;
  position: relative;
  display: inline-block;
}

.page-title::after {
  content: "";
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 40%;
  height: 3px;
  background: linear-gradient(90deg, #4568dc, #3f78e0);
  border-radius: 3px;
}

.filter-btn {
  min-width: 110px;
  border-radius: 8px;
  margin-right: 10px;
  padding: 10px 15px;
  transition: all 0.3s ease;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.filter-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.08) !important;
}

.filter-btn.btn-primary {
  box-shadow: 0 3px 8px rgba(0, 123, 255, 0.3);
}

.order-card {
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid #eaeaea;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  width: 100%;
  overflow: hidden;
  position: relative;
  margin-bottom: 1.5rem;
  border-radius: 8px;
}

.order-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.08) !important;
}

.order-header {
  background-color: #f8f9fa;
  padding: 12px 20px;
  width: 100%;
  border-bottom: 1px solid #eee;
}

.order-number, .order-date {
  font-size: 0.9rem;
  color: #666;
}

.buyer-info {
  font-weight: 500;
  color: #333;
}

.order-status .badge {
  padding: 0.4rem 0.7rem;
  font-size: 0.85rem;
}

.trade-type {
  font-size: 0.85rem;
  color: #666;
}

.order-body {
  padding: 0;
}

/* 防止点击传播 */
.order-body, 
.order-actions button {
  position: relative;
  z-index: 2;
}

.delivery-info {
  margin-bottom: 0;
  width: 100%;
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  align-items: center;
}

.delivery-detail {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  align-items: center;
  color: #666;
}

.product-row {
  transition: background-color 0.2s;
}

.product-row:hover {
  background-color: #f0f7ff;
}

.table {
  margin-bottom: 0;
  width: 100%;
  text-align: center !important;
}

.table th {
  font-weight: 600;
  color: #444;
  background-color: #f5f5f5;
  padding: 12px 16px;
  text-align: center !important;
}

.table td {
  vertical-align: middle;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  text-align: center !important;
}

.product-img {
  width: 70px;
  height: 70px;
  object-fit: cover;
  border-radius: 4px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.08);
  transition: all 0.2s ease;
}

.product-name {
  font-weight: 500;
  margin-bottom: 6px;
  color: #333;
  text-align: center !important;
  width: 100%;
}

.order-info-bar {
  background-color: #f9f9f9;
}

.info-label {
  font-weight: 500;
  color: #555;
  margin-right: 8px;
}

.info-value {
  color: #666;
}

.recipient {
  color: #333;
  font-weight: 500;
}

.phone {
  color: #666;
}

.address {
  color: #666;
}

.total-price {
  font-size: 1rem;
  margin-top: 5px;
}

.total-price strong {
  font-size: 1.2rem;
}

.commission {
  font-size: 0.9rem;
  margin-top: 5px;
}

.pagination-container {
  margin-bottom: 3rem;
  width: 100%;
}

.pagination {
  margin-top: 40px;
  margin-bottom: 30px;
}

.page-link {
  color: #4a6ee0;
  border-radius: 8px;
  margin: 0 5px;
  padding: 10px 18px;
  transition: all 0.2s ease;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

.pagination-lg .page-link {
  font-size: 1.1rem;
}

.page-item.active .page-link {
  background-color: #4a6ee0;
  border-color: #4a6ee0;
  box-shadow: 0 2px 8px rgba(74, 110, 224, 0.3);
}

.page-link:hover {
  background-color: #f0f7ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.rating-section {
  margin: 1.5rem 0;
}

.rating-label {
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.empty-state {
  padding: 3rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.empty-state i {
  opacity: 0.5;
  margin-bottom: 1rem;
}

.empty-state h4 {
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.empty-state p {
  max-width: 400px;
  margin: 0 auto;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .merchant-orders-container {
    padding: 0 10px;
  }
  
  .container {
    padding-left: 10px;
    padding-right: 10px;
  }
  
  .filter-btn {
    margin-bottom: 0.5rem;
    min-width: 90px;
    padding: 8px 12px;
    font-size: 0.9rem;
  }
  
  .order-info, .order-status {
    width: 100%;
    margin-bottom: 0.5rem;
  }
  
  .order-header {
    flex-direction: column;
    align-items: flex-start;
    padding: 10px 15px;
  }
  
  .order-date, .order-user, .trade-type {
    margin-left: 0 !important;
    margin-top: 0.5rem;
    display: block;
  }
  
  .order-info-bar {
    padding: 10px;
  }
  
  .order-actions {
    margin-bottom: 10px;
    justify-content: center;
    width: 100%;
  }
  
  .order-summary {
    text-align: center;
    width: 100%;
  }
}

.status-filter {
  margin-top: 1.5rem;
  margin-bottom: 2rem;
}
</style> 