<template>
  <div class="user-orders-container">
    <div class="container py-5">
      <h2 class="mb-4">我的订单</h2>
      
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
        <p class="text-muted">您还没有任何订单，去逛逛看看有什么喜欢的商品吧</p>
        <router-link to="/" class="btn btn-primary">浏览商品</router-link>
      </div>
      
      <!-- 订单列表 -->
      <div v-else class="order-list">
        <div v-for="order in orders" :key="order.id" class="order-card mb-4 bg-white rounded shadow-sm">
          <div class="order-header d-flex justify-content-between align-items-center p-3 border-bottom">
            <div class="order-info">
              <span class="order-number">订单号: {{ order.orderNo }}</span>
              <span class="order-date ms-4">{{ formatDate(order.createTime) }}</span>
            </div>
            <div class="order-status">
              <span class="badge" :class="getStatusBadgeClass(order.status)">
                {{ orderApi.getStatusText(order.status) }}
              </span>
            </div>
          </div>
          
          <div class="order-body p-3">
            <div v-for="item in order.orderItems" :key="item.id" class="order-item d-flex mb-2">
              <div class="item-image">
                <img :src="item.productImage || 'https://via.placeholder.com/80/e0e0e0/666666?text=商品'" :alt="item.productName" class="img-thumbnail" style="width: 80px; height: 80px; object-fit: cover;">
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
          
          <div class="order-footer d-flex justify-content-between align-items-center p-3 border-top bg-light">
            <div class="order-total">
              <span class="text-muted">共{{ getTotalItemCount(order) }}件商品</span>
              <span class="total-price ms-3">实付款：<strong class="text-danger">¥{{ order.actualPaymentAmount }}</strong></span>
            </div>
            <div class="order-actions">
              <router-link :to="`/order/${order.orderNo}`" class="btn btn-sm btn-outline-primary me-2">
                查看详情
              </router-link>
              
              <!-- 待付款状态 -->
              <button v-if="order.status === 'PENDING_PAYMENT'" 
                      @click="payOrder(order.orderNo)" 
                      class="btn btn-sm btn-primary me-2">
                去支付
              </button>
              
              <button v-if="order.status === 'PENDING_PAYMENT'" 
                      @click="cancelOrder(order.orderNo)" 
                      class="btn btn-sm btn-outline-danger me-2">
                取消订单
              </button>
              
              <!-- 已发货状态 -->
              <button v-if="order.status === 'SHIPPED'" 
                      @click="confirmReceipt(order.orderNo)" 
                      class="btn btn-sm btn-primary">
                确认收货
              </button>
              
              <!-- 已收货状态 -->
              <button v-if="order.status === 'RECEIVED'" 
                      @click="goToOrderDetail(order.orderNo)" 
                      class="btn btn-sm btn-outline-primary">
                申请退货
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
  border-radius: 6px;
  margin-right: 5px;
  transition: all 0.2s ease;
}

.order-card {
  transition: all 0.3s ease;
  border-radius: 12px;
  overflow: hidden;
}

.order-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12) !important;
}

.order-header {
  padding: 15px;
  background-color: #f8faff;
}

.order-item {
  transition: background-color 0.2s;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 10px;
}

.order-item:hover {
  background-color: #f0f7ff;
}

.order-footer {
  background-color: #f9faff;
  padding: 15px;
}

.item-image img {
  border-radius: 8px;
  transition: all 0.3s ease;
  border: 1px solid #eee;
}

.item-image img:hover {
  transform: scale(1.05);
}

.item-name {
  font-weight: 600;
  margin-bottom: 5px;
  color: #333;
}

.price {
  color: #ff6b6b;
  font-weight: 600;
}

.total-price {
  font-size: 1.1rem;
}

.badge {
  padding: 5px 10px;
  border-radius: 6px;
  font-weight: 500;
}

.btn-sm {
  border-radius: 6px;
  padding: 5px 12px;
  transition: all 0.2s ease;
}

.btn-outline-primary:hover, .btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(74, 110, 224, 0.25);
}

.btn-outline-danger:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(220, 53, 69, 0.25);
}

.pagination .page-link {
  border-radius: 6px;
  margin: 0 3px;
  color: #4a6ee0;
}

.pagination .page-item.active .page-link {
  background-color: #4a6ee0;
  border-color: #4a6ee0;
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