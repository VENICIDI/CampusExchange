<template>
  <div class="merchant-orders-container">
    <div class="container py-5">
      <h2 class="mb-4">商家订单管理</h2>
      
      <!-- 订单状态筛选 -->
      <div class="status-filter mb-4">
        <div class="btn-group">
          <button 
            v-for="(label, status) in statusOptions" 
            :key="status" 
            :class="['btn', currentStatus === status ? 'btn-primary' : 'btn-outline-primary']"
            @click="filterByStatus(status)"
          >
            {{ label }}
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
      <div v-else-if="orders.length === 0" class="text-center py-5 bg-light rounded">
        <i class="fas fa-box-open fa-3x text-muted mb-3"></i>
        <h4>暂无订单</h4>
        <p class="text-muted">还没有收到任何订单，等待买家下单吧</p>
      </div>
      
      <!-- 订单列表 -->
      <div v-else class="order-list">
        <div v-for="order in orders" :key="order.id" class="order-card mb-4 bg-white rounded shadow-sm">
          <div class="order-header d-flex justify-content-between align-items-center p-3 border-bottom">
            <div class="order-info">
              <span class="order-number">订单号: {{ order.orderNo }}</span>
              <span class="order-date ms-4">{{ formatDate(order.createTime) }}</span>
              <span class="order-user ms-4">买家: {{ order.username || '未知用户' }}</span>
            </div>
            <div class="order-status">
              <span class="badge" :class="getStatusBadgeClass(order.status)">
                {{ orderApi.getStatusText(order.status) }}
              </span>
              <span class="ms-3 trade-type">
                {{ order.tradeType === 'EXPRESS' ? '快递配送' : '线下交易' }}
              </span>
            </div>
          </div>
          
          <div class="order-body p-3">
            <div v-for="item in order.orderItems" :key="item.id" class="order-item d-flex mb-2">
              <div class="item-image">
                <img :src="item.productImage || 'https://via.placeholder.com/80'" :alt="item.productName" class="img-thumbnail" style="width: 80px; height: 80px; object-fit: cover;">
              </div>
              <div class="item-info ms-3 flex-grow-1">
                <div class="item-name">{{ item.productName }}</div>
                <div class="item-specs text-muted small">{{ item.specifications || '无规格信息' }}</div>
                <div class="item-price-qty">
                  <span class="price">¥{{ item.price }}</span>
                  <span class="qty ms-2">x {{ item.quantity }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 收货信息 -->
          <div v-if="order.tradeType === 'EXPRESS'" class="address-info p-3 border-top bg-light">
            <div class="address-header mb-2">收货信息:</div>
            <div class="address-body">
              <span class="recipient">{{ order.orderAddress?.recipient }}</span>
              <span class="phone ms-3">{{ order.orderAddress?.phone }}</span>
              <div class="address-detail mt-1">
                {{ order.orderAddress?.province }} {{ order.orderAddress?.city }} {{ order.orderAddress?.district }} {{ order.orderAddress?.detailAddress }}
              </div>
            </div>
          </div>
          
          <!-- 线下交易信息 -->
          <div v-else-if="order.tradeType === 'OFFLINE'" class="offline-info p-3 border-top bg-light">
            <div class="offline-header mb-2">线下交易信息:</div>
            <div class="offline-body">
              <div><strong>交易地点:</strong> {{ order.offlineMeetingLocation || '暂无' }}</div>
              <div><strong>交易时间:</strong> {{ formatDate(order.offlineMeetingTime) || '暂无' }}</div>
            </div>
          </div>
          
          <div class="order-footer d-flex justify-content-between align-items-center p-3 border-top">
            <div class="order-total">
              <span class="text-muted">共{{ getTotalItemCount(order) }}件商品</span>
              <span class="total-price ms-3">实付款：<strong class="text-danger">¥{{ order.actualPaymentAmount }}</strong></span>
              <span class="commission ms-3 text-muted">平台佣金：¥{{ order.platformCommissionAmount }}</span>
            </div>
            <div class="order-actions">
              <router-link :to="`/order/${order.orderNo}`" class="btn btn-sm btn-outline-primary me-2">
                查看详情
              </router-link>
              
              <!-- 待发货状态 -->
              <button v-if="order.status === 'PENDING_SHIPMENT' && order.tradeType === 'EXPRESS'" 
                      @click="openShipDialog(order.orderNo)" 
                      class="btn btn-sm btn-primary">
                发货
              </button>
              
              <!-- 退货申请处理 -->
              <button v-if="order.status === 'RETURN_REQUESTED'" 
                      @click="handleReturnRequest(order.orderNo, true)" 
                      class="btn btn-sm btn-success me-2">
                同意退货
              </button>
              
              <button v-if="order.status === 'RETURN_REQUESTED'" 
                      @click="handleReturnRequest(order.orderNo, false)" 
                      class="btn btn-sm btn-danger">
                拒绝退货
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页控件 -->
      <div v-if="totalPages > 1" class="pagination-container d-flex justify-content-center mt-4">
        <nav aria-label="订单分页">
          <ul class="pagination">
            <li :class="['page-item', currentPage === 1 ? 'disabled' : '']">
              <a class="page-link" href="#" @click.prevent="goToPage(currentPage - 1)">上一页</a>
            </li>
            <li v-for="page in paginationItems" :key="page" 
                :class="['page-item', page === currentPage ? 'active' : '']">
              <a class="page-link" href="#" @click.prevent="goToPage(page)">{{ page }}</a>
            </li>
            <li :class="['page-item', currentPage === totalPages ? 'disabled' : '']">
              <a class="page-link" href="#" @click.prevent="goToPage(currentPage + 1)">下一页</a>
            </li>
          </ul>
        </nav>
      </div>
    </div>
    
    <!-- 发货弹窗 -->
    <el-dialog
      v-model="shipDialogVisible"
      title="填写发货信息"
      width="500px"
    >
      <el-form ref="shipFormRef" :model="shipForm" :rules="shipRules" label-width="100px">
        <el-form-item label="快递公司" prop="expressCompany">
          <el-select v-model="shipForm.expressCompany" placeholder="请选择快递公司" style="width: 100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="申通快递" value="申通快递" />
            <el-option label="百世快递" value="百世快递" />
            <el-option label="京东物流" value="京东物流" />
            <el-option label="邮政EMS" value="邮政EMS" />
            <el-option label="其他快递" value="其他快递" />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号" prop="trackingNo">
          <el-input v-model="shipForm.trackingNo" placeholder="请输入快递单号"></el-input>
        </el-form-item>
      </el-form>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { orderApi } from '@/api/order';
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();
const loading = ref(true);
const orders = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const currentStatus = ref(null);

// 发货弹窗
const shipDialogVisible = ref(false);
const shipForm = reactive({
  orderNo: '',
  expressCompany: '',
  trackingNo: ''
});

// 退货处理弹窗
const returnDialogVisible = ref(false);
const isApproveReturn = ref(true);
const returnForm = reactive({
  orderNo: '',
  remark: ''
});

const submitting = ref(false);

// 表单验证规则
const shipRules = {
  expressCompany: [
    { required: true, message: '请选择快递公司', trigger: 'change' }
  ],
  trackingNo: [
    { required: true, message: '请输入快递单号', trigger: 'blur' },
    { min: 5, message: '快递单号长度不能小于5个字符', trigger: 'blur' }
  ]
};

// 订单状态选项
const statusOptions = {
  null: '全部订单',
  'PENDING_PAYMENT': '待付款',
  'PENDING_SHIPMENT': '待发货',
  'SHIPPED': '已发货',
  'RECEIVED': '已收货',
  'COMPLETED': '已完成',
  'CANCELLED': '已取消',
  'RETURN_REQUESTED': '退货申请'
};

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(total.value / pageSize.value);
});

// 计算分页项
const paginationItems = computed(() => {
  const items = [];
  const maxVisiblePages = 5;
  
  if (totalPages.value <= maxVisiblePages) {
    // 总页数小于最大可见页数，显示所有页码
    for (let i = 1; i <= totalPages.value; i++) {
      items.push(i);
    }
  } else {
    // 总页数大于最大可见页数，显示部分页码
    let startPage = Math.max(1, currentPage.value - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages.value, startPage + maxVisiblePages - 1);
    
    // 调整开始页码，确保显示maxVisiblePages个页码
    if (endPage - startPage + 1 < maxVisiblePages) {
      startPage = Math.max(1, endPage - maxVisiblePages + 1);
    }
    
    for (let i = startPage; i <= endPage; i++) {
      items.push(i);
    }
  }
  
  return items;
});

// 获取商家订单列表
const fetchOrders = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      status: currentStatus.value
    };
    
    const response = await orderApi.getMerchantOrders(params);
    if (response.data && response.data.code === 200) {
      orders.value = response.data.data.records || [];
      total.value = response.data.data.total || 0;
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

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知时间';
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
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

// 获取订单总件数
const getTotalItemCount = (order) => {
  if (!order.orderItems || !Array.isArray(order.orderItems)) return 0;
  return order.orderItems.reduce((total, item) => total + (item.quantity || 0), 0);
};

// 按状态筛选
const filterByStatus = (status) => {
  if (status === 'null') status = null;
  currentStatus.value = status;
  currentPage.value = 1; // 重置为第一页
  fetchOrders();
};

// 跳转到指定页
const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  fetchOrders();
};

// 打开发货弹窗
const openShipDialog = (orderNo) => {
  shipForm.orderNo = orderNo;
  shipForm.expressCompany = '';
  shipForm.trackingNo = '';
  shipDialogVisible.value = true;
};

// 确认发货
const confirmShip = async () => {
  // 表单验证
  if (!shipForm.expressCompany || !shipForm.trackingNo) {
    ElMessage.warning('请填写完整的发货信息');
    return;
  }
  
  submitting.value = true;
  try {
    const response = await orderApi.shipOrder(
      shipForm.orderNo,
      shipForm.trackingNo,
      shipForm.expressCompany
    );
    
    if (response.data && response.data.code === 200) {
      ElMessage.success('发货成功');
      shipDialogVisible.value = false;
      fetchOrders(); // 刷新订单列表
    } else {
      ElMessage.error('发货失败：' + (response.data?.message || '未知错误'));
    }
  } catch (error) {
    console.error('发货出错：', error);
    ElMessage.error('发货失败：' + (error.message || '网络错误'));
  } finally {
    submitting.value = false;
  }
};

// 打开退货处理弹窗
const handleReturnRequest = (orderNo, approve) => {
  returnForm.orderNo = orderNo;
  returnForm.remark = '';
  isApproveReturn.value = approve;
  returnDialogVisible.value = true;
};

// 确认处理退货申请
const confirmReturnProcess = async () => {
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
      fetchOrders(); // 刷新订单列表
    } else {
      ElMessage.error('处理失败：' + (response.data?.message || '未知错误'));
    }
  } catch (error) {
    console.error('处理退货申请出错：', error);
    ElMessage.error('处理失败：' + (error.message || '网络错误'));
  } finally {
    submitting.value = false;
  }
};

// 页面加载时获取数据
onMounted(() => {
  fetchOrders();
});
</script>

<style scoped>
.merchant-orders-container {
  min-height: 80vh;
  background-color: #f8f9fa;
}

.status-filter .btn-group {
  overflow-x: auto;
  white-space: nowrap;
  display: flex;
  margin-bottom: 1rem;
}

.status-filter .btn {
  min-width: 100px;
}

.order-card {
  transition: all 0.3s ease;
}

.order-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1) !important;
}

.order-item {
  transition: background-color 0.2s;
  padding: 10px;
  border-radius: 5px;
}

.order-item:hover {
  background-color: #f8f9fa;
}

.item-name {
  font-weight: 500;
  margin-bottom: 5px;
}

.price {
  color: #ff6b6b;
  font-weight: 500;
}

.total-price {
  font-size: 1.1rem;
}

.address-info, .offline-info {
  font-size: 0.9rem;
}

.trade-type {
  font-size: 0.85rem;
  color: #666;
}

@media (max-width: 768px) {
  .order-header, .order-footer {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .order-status, .order-actions {
    margin-top: 10px;
  }
  
  .order-item {
    flex-direction: column;
  }
  
  .item-info {
    margin-left: 0;
    margin-top: 10px;
  }
}
</style> 