<template>
  <div class="user-orders-container">
    <div class="container py-5">
      <h2 class="mb-4 section-title"><i class="fas fa-file-invoice-dollar me-2"></i>我的订单</h2>
      
      <!-- 订单状态筛选 -->
      <div class="status-filter mb-4">
        <div class="btn-group">
          <button 
            v-for="(label, status) in statusOptions" 
            :key="status" 
            :class="['btn filter-btn', currentStatus === status ? 'btn-primary' : 'btn-outline-primary']"
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
      <div v-else-if="orders.length === 0" class="empty-state text-center py-5 rounded">
        <i class="fas fa-box-open fa-4x text-muted mb-3"></i>
        <h4>暂无订单</h4>
        <p class="text-muted">您还没有任何订单，去逛逛看看有什么喜欢的商品吧</p>
        <router-link to="/" class="btn btn-primary btn-lg mt-3">
          <i class="fas fa-shopping-cart me-2"></i>浏览商品
        </router-link>
      </div>
      
      <!-- 订单列表 -->
      <div v-else class="order-list">
        <div v-for="order in orders" :key="order.id" class="order-card mb-4 bg-white rounded shadow-sm" @click="goToOrderDetail(order.orderNo)">
          <div class="order-header d-flex justify-content-between align-items-center p-3 border-bottom">
            <div class="order-info">
              <span class="order-number">订单号: {{ order.orderNo }}</span>
              <span class="order-date ms-3">下单时间: {{ formatDate(order.createTime) }}</span>
              <span v-if="order.merchantName" class="merchant-name ms-3">
                店铺: {{ order.merchantName }}
              </span>
            </div>
            <div class="order-status">
              <span class="badge" :class="getStatusBadgeClass(order.status)">
                {{ orderApi.getStatusText(order.status) }}
              </span>
            </div>
          </div>
          
          <div class="order-body">
            <div class="table-responsive">
              <table class="table table-hover mb-0">
                <thead class="table-light">
                  <tr>
                    <th style="width: 12%">商品图片</th>
                    <th style="width: 40%">商品名称</th>
                    <th style="width: 12%" class="text-center">单价</th>
                    <th style="width: 10%" class="text-center">数量</th>
                    <th style="width: 16%" class="text-center">小计</th>
                    <th style="width: 10%" class="text-center">操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in order.orderItems" :key="item.id">
                    <td class="text-center">
                      <img :src="item.productImage || 'https://via.placeholder.com/60/e0e0e0/666666?text=商品'" 
                           :alt="item.productName" 
                           class="product-img">
                    </td>
                    <td>
                      <div class="product-name">{{ item.productName }}</div>
                <div class="item-specs text-muted small">{{ item.specifications || '无规格信息' }}</div>
                    </td>
                    <td class="text-center align-middle">¥{{ item.price }}</td>
                    <td class="text-center align-middle">{{ item.quantity }}</td>
                    <td class="text-center align-middle"><strong>¥{{ (item.price * item.quantity).toFixed(2) }}</strong></td>
                    <td class="text-center align-middle">
                      <button v-if="order.status === 'PENDING_PAYMENT'" 
                              @click.stop="payOrder(order.orderNo)" 
                              class="btn btn-sm btn-outline-primary">
                        付款
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
          </div>
          
            <div class="order-footer d-flex justify-content-end align-items-center">
              <span class="text-muted me-3">共{{ getTotalItemCount(order) }}件商品</span>
              <span class="total-price">实付款：<strong class="text-danger">¥{{ order.actualPaymentAmount }}</strong></span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页控件 -->
      <div v-if="totalPages > 1" class="pagination-container d-flex justify-content-center mt-4">
        <nav aria-label="订单分页">
          <ul class="pagination">
            <li :class="['page-item', currentPage === 1 ? 'disabled' : '']">
              <a class="page-link" href="#" @click.prevent="goToPage(currentPage - 1)">
                <i class="fas fa-chevron-left"></i>
              </a>
            </li>
            <li v-for="page in paginationItems" :key="page" 
                :class="['page-item', page === currentPage ? 'active' : '']">
              <a class="page-link" href="#" @click.prevent="goToPage(page)">{{ page }}</a>
            </li>
            <li :class="['page-item', currentPage === totalPages ? 'disabled' : '']">
              <a class="page-link" href="#" @click.prevent="goToPage(currentPage + 1)">
                <i class="fas fa-chevron-right"></i>
              </a>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
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

// 订单状态选项
const statusOptions = {
  null: '全部订单',
  'PENDING_PAYMENT': '待付款',
  'PENDING_SHIPMENT': '待发货',
  'SHIPPED': '已发货',
  'RECEIVED': '已收货',
  'COMPLETED': '已完成',
  'CANCELLED': '已取消'
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

// 获取用户订单列表
const fetchOrders = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      status: currentStatus.value
    };
    
    const response = await orderApi.getUserOrders(params);
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

// 取消订单
const cancelOrder = async (orderNo) => {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '取消订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const response = await orderApi.cancelOrder(orderNo);
    if (response.data && response.data.code === 200) {
      ElMessage.success('订单已取消');
      fetchOrders(); // 刷新订单列表
    } else {
      ElMessage.error('取消订单失败：' + (response.data?.message || '未知错误'));
    }
  } catch (error) {
    if (error !== 'cancel') { // 不是用户取消操作
      console.error('取消订单出错：', error);
      ElMessage.error('取消订单失败：' + (error.message || '网络错误'));
    }
  }
};

// 支付订单
const payOrder = async (orderNo) => {
  try {
    await ElMessageBox.confirm('确定要支付这个订单吗？', '订单支付', {
      confirmButtonText: '确定支付',
      cancelButtonText: '取消',
      type: 'info'
    });
    
    const response = await orderApi.payOrder(orderNo);
    if (response.data && response.data.code === 200) {
      ElMessage.success('订单支付成功');
      fetchOrders(); // 刷新订单列表
    } else {
      ElMessage.error('订单支付失败：' + (response.data?.message || '未知错误'));
    }
  } catch (error) {
    if (error !== 'cancel') { // 不是用户取消操作
      console.error('支付订单出错：', error);
      ElMessage.error('支付订单失败：' + (error.message || '网络错误'));
    }
  }
};

// 确认收货
const confirmReceipt = async (orderNo) => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'info'
    });
    
    const response = await orderApi.confirmReceipt(orderNo);
    if (response.data && response.data.code === 200) {
      ElMessage.success('已确认收货');
      fetchOrders(); // 刷新订单列表
    } else {
      ElMessage.error('确认收货失败：' + (response.data?.message || '未知错误'));
    }
  } catch (error) {
    if (error !== 'cancel') { // 不是用户取消操作
      console.error('确认收货出错：', error);
      ElMessage.error('确认收货失败：' + (error.message || '网络错误'));
    }
  }
};

// 跳转到订单详情页
const goToOrderDetail = (orderNo) => {
  router.push(`/order/${orderNo}`);
};

// 页面加载时获取数据
onMounted(() => {
  fetchOrders();
});
</script>

<style scoped>
.user-orders-container {
  min-height: 90vh;
  background-color: #f8f9fa;
}

.section-title {
  font-weight: 700;
  color: #333;
  margin-bottom: 1.5rem;
  position: relative;
  display: inline-block;
}

.section-title::after {
  content: "";
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 40%;
  height: 3px;
  background: linear-gradient(90deg, #4568dc, #3f78e0);
  border-radius: 3px;
}

.status-filter .btn-group {
  overflow-x: auto;
  white-space: nowrap;
  display: flex;
  margin-bottom: 1.5rem;
  padding-bottom: 5px;
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
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.btn-primary {
  background: linear-gradient(135deg, #4568dc, #3c87f0);
  border: none;
}

.btn-outline-primary {
  color: #4568dc;
  border-color: #4568dc;
}

.order-card {
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid #eaeaea;
  width: 100%;
}

.order-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.08) !important;
}

.order-header {
  background-color: #f8f9fa;
  padding: 12px 20px;
  width: 100%;
}

.order-body {
  padding: 0;
}

.order-footer {
  padding: 12px 20px;
  background-color: #f9f9f9;
  border-top: 1px solid #eee;
}

.order-number, .order-date, .merchant-name {
  font-size: 0.9rem;
  color: #555;
}

.table {
  margin-bottom: 0;
  width: 100%;
}

.table th {
  font-weight: 600;
  color: #444;
  background-color: #f5f5f5;
  padding: 12px 16px;
}

.table td {
  vertical-align: middle;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
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
}

.item-specs {
  color: #999;
  font-size: 0.8rem;
}

.badge {
  padding: 6px 12px;
  font-weight: 500;
  font-size: 0.85rem;
  border-radius: 4px;
}

.btn-outline-primary {
  color: #4568dc;
  border-color: #4568dc;
  transition: all 0.2s ease;
}

.btn-outline-primary:hover {
  background-color: #4568dc;
  color: white;
}

.total-price {
  font-size: 1rem;
}

.total-price strong {
  font-size: 1.1rem;
}

.order-item {
  transition: all 0.3s ease;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid rgba(0,0,0,0.05);
  box-shadow: 0 2px 4px rgba(0,0,0,0.03);
  height: 100%;
}

.order-item:hover {
  background-color: #f8faff;
  border-color: rgba(69, 104, 220, 0.2);
  box-shadow: 0 4px 8px rgba(0,0,0,0.06);
}

.pay-now-section {
  padding: 15px;
  background-color: #fafeff;
  border-radius: 10px;
  margin: 15px 0;
  border: 1px dashed #4568dc;
}

.pay-button {
  padding: 12px 40px;
  font-size: 1.1rem;
  font-weight: 600;
  border-radius: 50px;
  letter-spacing: 1px;
  background: linear-gradient(135deg, #4568dc, #3c87f0);
  border: none;
  box-shadow: 0 5px 15px rgba(60, 135, 240, 0.3);
  transition: all 0.3s ease;
}

.pay-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(60, 135, 240, 0.4);
  background: linear-gradient(135deg, #3c87f0, #4568dc);
}

.btn-action {
  border-radius: 6px;
  padding: 8px 16px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0,0,0,0.08);
}

.btn-action:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.pagination {
  margin-top: 30px;
}

.pagination .page-link {
  border-radius: 8px;
  margin: 0 3px;
  color: #4568dc;
  transition: all 0.2s ease;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

.pagination .page-item.active .page-link {
  background-color: #4568dc;
  border-color: #4568dc;
}

.pagination .page-link:hover {
  background-color: #f0f7ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.empty-state {
  background: linear-gradient(135deg, #f8faff 0%, #eef5ff 100%);
  border-radius: 15px;
  padding: 60px 20px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.05);
  border: 1px solid rgba(69, 104, 220, 0.1);
}

.empty-state i {
  color: #4568dc;
  opacity: 0.5;
  margin-bottom: 20px;
}

.empty-state h4 {
  font-weight: 700;
  color: #333;
  margin-bottom: 15px;
}

@media (max-width: 768px) {
  .status-filter .btn-group {
    justify-content: space-between;
  }
  
  .filter-btn {
    min-width: auto;
    padding: 8px 12px;
    font-size: 0.9rem;
  }
  
  .order-footer {
    flex-direction: column;
  }
  
  .order-total {
    margin-bottom: 15px;
  }
  
  .order-actions {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .btn-action {
    margin: 5px;
  }
}
</style> 