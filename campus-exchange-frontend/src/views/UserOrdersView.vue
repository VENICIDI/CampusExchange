<template>
  <div class="user-orders-container">
    <div class="container py-5 px-md-5">
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
              <span class="order-number">订单号：{{ order.orderNo }}&nbsp;&nbsp;</span>
              <span class="order-date ms-5">下单时间：{{ formatDate(order.createTime) }}&nbsp;&nbsp;</span>
              <span v-if="order.merchantName" class="merchant-name ms-5">
                店铺：{{ order.merchantName }}
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
              <table class="table table-hover mb-0 text-center">
                <thead class="table-light">
                  <tr>
                    <th style="width: 12%">商品图片</th>
                    <th style="width: 40%">商品名称</th>
                    <th style="width: 15%">单价</th>
                    <th style="width: 12%">数量</th>
                    <th style="width: 21%">小计</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in order.orderItems" :key="item.id">
                    <td class="text-center align-middle">
                      <img :src="item.productImage || 'https://via.placeholder.com/60/e0e0e0/666666?text=商品'" 
                           :alt="item.productName" 
                           class="product-img">
                    </td>
                    <td class="text-center align-middle">
                      <div class="product-name">{{ item.productName }}</div>
                      <div class="item-specs text-muted small">{{ item.specifications || '无规格信息' }}</div>
                    </td>
                    <td class="text-center align-middle">¥{{ item.price }}</td>
                    <td class="text-center align-middle">{{ item.quantity }}</td>
                    <td class="text-center align-middle"><strong>¥{{ (item.price * item.quantity).toFixed(2) }}</strong></td>
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
const total = ref(0);
const currentStatus = ref(null);
const showUnreviewed = ref(false);
const unreviewedCount = ref(0);

// 删除不再需要的分页相关变量
const currentPage = ref(1);
const pageSize = ref(10);

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

// 获取用户订单列表
const fetchOrders = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: 1,
      pageSize: 1000, // 设置一个足够大的数值，一次获取所有订单
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
  if (!dateString) return '';
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
  // 如果是'null'或者'all'，将其转为null，确保后端接收到的是null而不是字符串
  showUnreviewed.value = false; // 退出待评价状态
  
  if (status === 'AFTERSALE') {
    // 处理售后状态特殊筛选 - 前端筛选售后相关状态
    currentStatus.value = null; // 先获取全部
    fetchOrders().then(() => {
      // 只显示售后相关状态的订单
      const afterSaleStatuses = ['RETURN_REQUESTED', 'RETURN_APPROVED', 'RETURN_GOODS_RECEIVED', 'RETURNED', 'RETURN_REJECTED'];
      orders.value = orders.value.filter(order => afterSaleStatuses.includes(order.status));
    });
  } else {
    currentStatus.value = (status === 'null' || status === null || status === 'all') ? null : status;
  fetchOrders();
  }
};

// 筛选未评价订单
const filterUnreviewed = async () => {
  showUnreviewed.value = !showUnreviewed.value;
  if (showUnreviewed.value) {
    // 如果进入待评价状态，清除当前状态选择
    currentStatus.value = null;
    await fetchUnreviewedOrders();
  } else {
    // 如果退出待评价状态，恢复默认全部订单
    currentStatus.value = null;
    fetchOrders();
  }
};

// 获取未评价订单
const fetchUnreviewedOrders = async () => {
  loading.value = true;
  try {
    const response = await orderApi.getUnreviewedOrders();
    if (response.data && response.data.code === 200) {
      const unreviewedOrderNos = response.data.data || [];
      unreviewedCount.value = unreviewedOrderNos.length;
      
      if (unreviewedOrderNos.length === 0) {
        orders.value = [];
        total.value = 0;
        loading.value = false;
        return;
      }
      
      // 获取所有未评价订单的详情（不使用分页）
      const unreviewedOrders = [];
      for (const orderNo of unreviewedOrderNos) {
        try {
          const orderResponse = await orderApi.getOrderDetail(orderNo);
          if (orderResponse.data && orderResponse.data.code === 200) {
            const orderDetail = orderResponse.data.data;
            
            // 处理订单项，确保有必要的信息
            if (orderDetail.orderItems) {
              orderDetail.orderItems = orderDetail.orderItems.map(item => {
                return {
                  id: item.id,
                  productId: item.productId,
                  productName: item.productNameSnapshot || '未知商品',
                  productImage: item.productImageSnapshot,
                  price: item.priceAtPurchase || 0,
                  quantity: item.quantity || 1,
                  specifications: ''
                };
              });
            }
            
            unreviewedOrders.push(orderDetail);
          }
        } catch (error) {
          console.error('获取订单详情失败:', error);
        }
      }
      
      orders.value = unreviewedOrders;
      total.value = unreviewedOrders.length;
    } else {
      ElMessage.error('获取未评价订单失败');
    }
  } catch (error) {
    console.error('获取未评价订单出错:', error);
    ElMessage.error('获取未评价订单出错');
  } finally {
    loading.value = false;
  }
};

// 跳转到订单详情页
const goToOrderDetail = (orderNo) => {
  router.push(`/order/${orderNo}`);
};

// 获取未评价订单数量
const fetchUnreviewedCount = async () => {
  try {
    const response = await orderApi.getUnreviewedOrders();
    if (response.data && response.data.code === 200) {
      unreviewedCount.value = response.data.data ? response.data.data.length : 0;
    }
  } catch (error) {
    console.error('获取未评价订单数量出错:', error);
  }
};

// 获取指定状态的订单数量
const getStatusCount = (status) => {
  // 全部订单显示所有状态订单数量的总和
  if (status === null || status === 'null') {
    // 计算所有状态订单的总和
    return Object.values(statusCounts.value).reduce((sum, count) => sum + count, 0);
  }
  
  // 处理"售后处理"的情况
  if (status === 'AFTERSALE') {
    // 计算所有售后相关状态订单的总和
    const afterSaleStatuses = ['RETURN_REQUESTED', 'RETURN_APPROVED', 'RETURN_GOODS_RECEIVED', 'RETURNED', 'RETURN_REJECTED'];
    return afterSaleStatuses.reduce((sum, status) => sum + (statusCounts.value[status] || 0), 0);
  }
  
  // 返回状态计数
  return statusCounts.value[status] || 0;
};

// 获取各状态订单数量
const fetchStatusCounts = async () => {
  try {
    // 调用API获取订单状态计数
    const response = await orderApi.getUserOrderStatusCounts();
    
    if (response.data && response.data.code === 200) {
      const counts = response.data.data;
      
      // 更新状态计数
      Object.keys(counts).forEach(status => {
        if (statusCounts.value[status] !== undefined) {
          statusCounts.value[status] = counts[status];
        }
      });
      
      // 计算总订单数量
      let totalCount = 0;
      for (const statusKey in statusCounts.value) {
        totalCount += statusCounts.value[statusKey] || 0;
      }
      console.log("用户订单总数:", totalCount);
    } else {
      console.error('获取订单状态统计失败：', response.data?.message || '未知错误');
    }
  } catch (error) {
    console.error('获取订单状态统计失败：', error);
  }
};

// 取消订单
const cancelOrder = async (orderNo) => {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '取消订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      duration: 2000 // 减少显示时间
    });
    
    const response = await orderApi.cancelOrder(orderNo);
    if (response.data && response.data.code === 200) {
      ElMessage({
        message: '订单已取消',
        type: 'success',
        duration: 2000 // 减少显示时间
      });
      fetchOrders(); // 刷新订单列表
    } else {
      ElMessage({
        message: '取消订单失败：' + (response.data?.message || '未知错误'),
        type: 'error',
        duration: 2000 // 减少显示时间
      });
    }
  } catch (error) {
    if (error !== 'cancel') { // 不是用户取消操作
      console.error('取消订单出错：', error);
      ElMessage({
        message: '取消订单失败：' + (error.message || '网络错误'),
        type: 'error',
        duration: 2000 // 减少显示时间
      });
    }
  }
};

// 支付订单
const payOrder = async (orderNo) => {
  try {
    await ElMessageBox.confirm('您想使用哪种方式支付此订单？', '选择支付方式', {
      confirmButtonText: '钱包支付',
      cancelButtonText: '普通支付',
      distinguishCancelAndClose: true,
      type: 'info',
      duration: 2000
    })
      .then(() => {
        // 钱包支付 - 跳转到支付页面
        router.push(`/payment?id=${orderNo}`);
      })
      .catch((action) => {
        if (action === 'cancel') {
          // 普通支付
          handleNormalPayment(orderNo);
        }
      });
  } catch (error) {
    console.error('支付选择出错：', error);
  }
};

// 普通支付方法
const handleNormalPayment = async (orderNo) => {
  try {
    await ElMessageBox.confirm('确定要支付这个订单吗？', '订单支付', {
      confirmButtonText: '确定支付',
      cancelButtonText: '取消',
      type: 'info',
      duration: 2000 // 减少显示时间
    });
    
    const response = await orderApi.payOrder(orderNo);
    if (response.data && response.data.code === 200) {
      ElMessage({
        message: '订单支付成功',
        type: 'success',
        duration: 2000 // 减少显示时间
      });
      fetchOrders(); // 刷新订单列表
    } else {
      ElMessage({
        message: '订单支付失败：' + (response.data?.message || '未知错误'),
        type: 'error',
        duration: 2000 // 减少显示时间
      });
    }
  } catch (error) {
    if (error !== 'cancel') { // 不是用户取消操作
      console.error('支付订单出错：', error);
      ElMessage({
        message: '支付订单失败：' + (error.message || '网络错误'),
        type: 'error',
        duration: 2000 // 减少显示时间
      });
    }
  }
};

// 确认收货
const confirmReceipt = async (orderNo) => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'info',
      duration: 2000 // 减少显示时间
    });
    
    const response = await orderApi.confirmReceipt(orderNo);
    if (response.data && response.data.code === 200) {
      ElMessage({
        message: '已确认收货',
        type: 'success',
        duration: 2000 // 减少显示时间
      });
      fetchOrders(); // 刷新订单列表
    } else {
      ElMessage({
        message: '确认收货失败：' + (response.data?.message || '未知错误'),
        type: 'error',
        duration: 2000 // 减少显示时间
      });
    }
  } catch (error) {
    if (error !== 'cancel') { // 不是用户取消操作
      console.error('确认收货出错：', error);
      ElMessage({
        message: '确认收货失败：' + (error.message || '网络错误'),
        type: 'error',
        duration: 2000 // 减少显示时间
      });
    }
  }
};

// 页面加载
onMounted(async () => {
  await fetchOrders();
  await fetchStatusCounts(); // 获取各状态订单数量
});
</script>

<style scoped>
.user-orders-container {
  background-color: #f8f9fa;
  min-height: 100vh;
  padding: 0 15px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
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

.table tr {
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

.item-specs {
  color: #999;
  font-size: 0.8rem;
  text-align: center !important;
  width: 100%;
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

.pagination-container {
  margin-bottom: 3rem;
  width: 100%;
}

.pagination {
  margin-top: 40px;
  margin-bottom: 30px;
}

.pagination .page-link {
  border-radius: 8px;
  margin: 0 5px;
  color: #4568dc;
  transition: all 0.2s ease;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
  padding: 10px 18px;
}

.pagination-lg .page-link {
  font-size: 1.1rem;
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
  .user-orders-container {
    padding: 0 10px;
  }
  
  .status-filter .btn-group {
    justify-content: space-between;
    flex-wrap: wrap;
  }
  
  .filter-btn {
    min-width: auto;
    padding: 8px 12px;
    font-size: 0.9rem;
    margin-bottom: 5px;
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