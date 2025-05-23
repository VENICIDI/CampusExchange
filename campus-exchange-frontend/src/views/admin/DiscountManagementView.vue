<template>
  <div class="discount-management container-fluid py-4">
    <div class="admin-page-header mb-4">
      <div class="header-content">
        <h2 class="page-title mb-1">优惠券发放管理</h2>
        <p class="text-muted">管理员可以在此为待付款订单发放优惠券折扣</p>
      </div>
      <div class="header-action">
        <button @click="loadPendingOrders" class="btn btn-outline-primary">
          <i class="fas fa-sync-alt me-1"></i> 刷新列表
        </button>
      </div>
    </div>
    
    <!-- 只保留可发放优惠券的统计卡片 -->
    <div v-if="!loading && !error && availableOrders.length > 0" class="card stats-card border-0 shadow-sm mb-4">
      <div class="card-body d-flex align-items-center">
        <div class="stats-icon bg-primary">
          <i class="fas fa-ticket-alt text-white"></i>
        </div>
        <div class="stats-info ms-3">
          <h5 class="stats-title mb-1">可发放优惠券的订单</h5>
          <h3 class="stats-number">{{ availableOrders.length }}</h3>
        </div>
        <div class="ms-auto">
          <p class="text-muted mb-0">选择订单发放优惠券</p>
        </div>
      </div>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">加载中...</span>
      </div>
      <p class="mt-3 text-muted">正在加载待处理订单...</p>
    </div>
    
    <!-- 错误提示 -->
    <div v-else-if="error" class="alert alert-danger d-flex align-items-center">
      <i class="fas fa-exclamation-circle me-2"></i>
      <div>{{ error }}</div>
    </div>
    
    <!-- 没有可发放的订单 -->
    <div v-else-if="availableOrders.length === 0" class="empty-state card border-0 shadow-sm">
      <div class="card-body text-center py-5">
        <div class="empty-icon-wrapper mb-3">
          <i class="fas fa-ticket-alt empty-icon"></i>
        </div>
        <h4>暂无可发放优惠券的订单</h4>
        <p class="text-muted">当前没有待付款订单需要处理</p>
      </div>
    </div>
    
    <!-- 订单列表（只显示未发放优惠券的订单） -->
    <div v-else class="card border-0 shadow-sm">
      <div class="card-header d-flex justify-content-between align-items-center">
        <div>
          <h5 class="mb-0">可发放优惠券的订单</h5>
        </div>
        <div class="header-actions">
          <div class="input-group">
            <input type="text" class="form-control" placeholder="搜索订单号或买家" v-model="searchKeyword">
            <button class="btn btn-primary">
              <i class="fas fa-search"></i>
            </button>
          </div>
        </div>
      </div>
      
      <div class="table-responsive">
        <table class="table align-middle mb-0 order-table">
          <thead>
            <tr>
              <th>订单编号</th>
              <th>买家</th>
              <th>商品总额</th>
              <th>实付金额</th>
              <th>创建时间</th>
              <th class="text-center">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in filteredAvailableOrders" :key="order.id" class="order-row">
              <td class="order-no">{{ order.orderNo }}</td>
              <td>
                <div class="d-flex align-items-center">
                  <span class="avatar bg-primary text-white me-2">{{ getUserInitials(order.userName) }}</span>
                  {{ order.userName }}
                </div>
              </td>
              <td>¥{{ order.totalProductAmount.toFixed(2) }}</td>
              <td class="actual-amount">¥{{ order.actualPaymentAmount.toFixed(2) }}</td>
              <td>{{ formatDate(order.createTime) }}</td>
              <td class="text-center">
                <button 
                  class="btn btn-primary action-btn"
                  @click="showDiscountModal(order)"
                >
                  <i class="fas fa-gift me-1"></i> 发放优惠券
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- 分页信息 -->
      <div class="card-footer d-flex justify-content-between align-items-center">
        <div class="pagination-info">
          显示 {{ filteredAvailableOrders.length }} 条记录
        </div>
      </div>
    </div>
    
    <!-- 折扣发放弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="发放优惠券"
      width="450px"
      custom-class="discount-dialog"
    >
      <div v-if="selectedOrder" class="discount-form">
        <div class="order-info-card mb-4">
          <div class="info-row mb-2">
            <div class="info-label">订单编号:</div>
            <div class="info-value">{{ selectedOrder.orderNo }}</div>
          </div>
          <div class="info-row mb-2">
            <div class="info-label">买家:</div>
            <div class="info-value">{{ selectedOrder.userName }}</div>
          </div>
          <div class="info-row mb-2">
            <div class="info-label">商品总金额:</div>
            <div class="info-value">¥{{ selectedOrder.totalProductAmount.toFixed(2) }}</div>
          </div>
          <div class="info-row mb-0">
            <div class="info-label">当前应付金额:</div>
            <div class="info-value text-danger fw-bold">¥{{ selectedOrder.actualPaymentAmount.toFixed(2) }}</div>
          </div>
        </div>
        
        <div class="discount-amount-section">
          <div class="form-group">
            <label for="discountAmount" class="form-label fw-bold">优惠券金额 (¥)</label>
            <el-input-number 
              v-model="discountAmount" 
              :min="0"
              :max="selectedOrder.actualPaymentAmount"
              :precision="2"
              :step="5"
              placeholder="请输入优惠金额"
              style="width: 100%"
              size="large"
            />
            <div class="form-text mt-2">
              <i class="fas fa-info-circle me-1"></i> 不能超过订单金额 ¥{{ selectedOrder.actualPaymentAmount.toFixed(2) }}
            </div>
          </div>
        </div>
        
        <div class="preview-section mt-4" v-if="discountAmount > 0">
          <div class="result-card">
            <div class="discount-info">
              <div class="discount-percent">
                {{ ((discountAmount / selectedOrder.totalProductAmount) * 100).toFixed(0) }}% OFF
              </div>
            </div>
            <div class="result-info">
              <div class="result-label">优惠后实付金额:</div>
              <div class="result-value">¥{{ (selectedOrder.actualPaymentAmount - discountAmount).toFixed(2) }}</div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="createOrderDiscount"
            :loading="isSubmitting"
            :disabled="!discountAmount || discountAmount <= 0 || discountAmount > selectedOrder?.actualPaymentAmount"
          >
            {{ isSubmitting ? '处理中...' : '确认发放' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElDialog, ElButton, ElInputNumber } from 'element-plus';
import { getPendingOrders, createDiscount } from '@/api/adminDiscount';

// 状态变量
const loading = ref(false);
const error = ref(null);
const pendingOrders = ref([]);
const selectedOrder = ref(null);
const discountAmount = ref(0);
const isSubmitting = ref(false);
const dialogVisible = ref(false);
const searchKeyword = ref('');

// 只获取未发放优惠券的订单
const availableOrders = computed(() => {
  return pendingOrders.value.filter(order => !order.hasDiscount);
});

// 过滤可用订单
const filteredAvailableOrders = computed(() => {
  if (!searchKeyword.value) return availableOrders.value;
  
  const keyword = searchKeyword.value.toLowerCase();
  return availableOrders.value.filter(order => 
    order.orderNo.toLowerCase().includes(keyword) || 
    order.userName.toLowerCase().includes(keyword)
  );
});

// 初始化页面
onMounted(() => {
  loadPendingOrders();
});

// 加载待处理订单
const loadPendingOrders = async () => {
  loading.value = true;
  error.value = null;
  
  try {
    const response = await getPendingOrders();
    if (response.data && response.data.code === 200) {
      pendingOrders.value = response.data.data || [];
    } else {
      error.value = response.data?.message || '获取待处理订单失败';
    }
  } catch (err) {
    console.error('获取待处理订单出错:', err);
    error.value = '获取待处理订单出错: ' + (err.message || err);
  } finally {
    loading.value = false;
  }
};

// 获取用户名首字母
const getUserInitials = (name) => {
  if (!name) return '?';
  return name.charAt(0).toUpperCase();
};

// 显示折扣发放弹窗
const showDiscountModal = (order) => {
  selectedOrder.value = order;
  discountAmount.value = 0;
  dialogVisible.value = true;
};

// 创建订单折扣
const createOrderDiscount = async () => {
  if (!selectedOrder.value || !discountAmount.value || discountAmount.value <= 0) {
    ElMessage.warning('请输入有效的优惠金额');
    return;
  }
  
  if (discountAmount.value > selectedOrder.value.actualPaymentAmount) {
    ElMessage.warning('优惠金额不能超过订单实付金额');
    return;
  }
  
  isSubmitting.value = true;
  
  try {
    const response = await createDiscount(
      selectedOrder.value.id,
      parseFloat(discountAmount.value)
    );
    
    if (response.data && response.data.code === 200) {
      ElMessage.success('优惠券发放成功！');
      dialogVisible.value = false;
      await loadPendingOrders();
    } else {
      ElMessage.error(response.data?.message || '优惠券发放失败');
    }
  } catch (err) {
    console.error('优惠券发放出错:', err);
    ElMessage.error('优惠券发放出错: ' + (err.message || err));
  } finally {
    isSubmitting.value = false;
  }
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-';
  
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hours}:${minutes}`;
};
</script>

<style scoped>
.discount-management {
  min-height: 75vh;
}

.admin-page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.page-title {
  font-weight: 600;
  color: #333;
  font-size: 1.75rem;
  margin-bottom: 0.25rem;
}

/* 统计卡片样式 */
.stats-card {
  border-radius: 8px;
  background: linear-gradient(to right, #fff, #f8f9fa);
  transition: all 0.3s ease;
}

.stats-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 15px rgba(0,0,0,0.1) !important;
}

.stats-icon {
  width: 50px;
  height: 50px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 10px;
  font-size: 1.5rem;
}

.stats-title {
  font-size: 0.9rem;
  color: #555;
  font-weight: 500;
}

.stats-number {
  font-size: 1.75rem;
  font-weight: 600;
  margin-bottom: 0;
  color: #333;
}

/* 加载状态样式 */
.loading-container {
  padding: 3rem 0;
}

/* 空状态样式 */
.empty-state {
  border-radius: 10px;
  padding: 2rem;
}

.empty-icon-wrapper {
  width: 80px;
  height: 80px;
  margin: 0 auto;
  background-color: #f0f7ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-icon {
  font-size: 2.5rem;
  color: #4096ff;
}

/* 订单表格样式 */
.order-table {
  font-size: 0.95rem;
}

.order-table th {
  font-weight: 600;
  color: #495057;
  padding: 1rem;
}

.order-table td {
  padding: 0.85rem 1rem;
}

.order-row {
  transition: background-color 0.2s ease;
}

.order-row:hover {
  background-color: #f0f7ff !important;
}

.order-no {
  font-family: monospace;
  font-weight: 600;
  color: #333;
}

.avatar {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  font-weight: 600;
  font-size: 0.85rem;
}

.actual-amount {
  font-weight: 600;
  color: #e53935;
}

.action-btn {
  border-radius: 6px;
  padding: 0.5rem 1rem;
  transition: all 0.2s ease;
}

/* 卡片样式 */
.card {
  border: none;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.card-header {
  background-color: #fff;
  border-bottom: 1px solid rgba(0,0,0,0.06);
  padding: 1rem 1.25rem;
}

.card-footer {
  background-color: #fff;
  border-top: 1px solid rgba(0,0,0,0.06);
  padding: 0.75rem 1.25rem;
}

/* 弹窗样式 */
.discount-form {
  padding: 0.5rem;
}

.order-info-card {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 1.25rem;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.75rem;
}

.info-label {
  color: #6c757d;
  font-weight: 500;
}

.info-value {
  font-weight: 500;
}

.result-card {
  background-color: #f0f7ff;
  border-radius: 8px;
  padding: 1.25rem;
  position: relative;
  overflow: hidden;
}

.discount-info {
  position: absolute;
  top: -5px;
  right: -5px;
  background-color: #e53935;
  color: white;
  padding: 0.5rem;
  border-bottom-left-radius: 8px;
  transform: rotate(10deg);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.discount-percent {
  font-weight: 700;
  font-size: 1.1rem;
}

.result-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-label {
  font-weight: 600;
  font-size: 1rem;
}

.result-value {
  font-weight: 700;
  font-size: 1.5rem;
  color: #4caf50;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .admin-page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-action {
    margin-top: 1rem;
    width: 100%;
  }
  
  .header-actions {
    margin-top: 1rem;
    width: 100%;
  }
}
</style> 