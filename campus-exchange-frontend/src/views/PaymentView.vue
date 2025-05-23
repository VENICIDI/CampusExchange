<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { orderApi } from '@/api/order';
import { getUserWalletAPI, payOrderWithWalletAPI } from '@/api/wallet';
import { ElMessage, ElMessageBox } from 'element-plus';

const route = useRoute();
const router = useRouter();
const orderNo = ref(route.query.id);
const order = ref(null);
const loading = ref(true);
const submitting = ref(false);

// 钱包信息
const wallet = ref({
  balance: 0,
  points: 0
});

// 支付选项
const paymentMethod = ref('WALLET'); // WALLET, ALIPAY, WECHAT

// 倒计时
const countdown = ref(0);
let timer = null;

// 计算属性
const walletEnough = computed(() => {
  if (!order.value || !order.value.actualPaymentAmount) return false;
  return (wallet.value.balance || 0) >= parseFloat(order.value.actualPaymentAmount);
});

const countdownText = computed(() => {
  const minutes = Math.floor(countdown.value / 60);
  const seconds = countdown.value % 60;
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
});

// 获取订单信息
const fetchOrderInfo = async () => {
  try {
    loading.value = true;
    const response = await orderApi.getOrderDetail(orderNo.value);
    
    if (response.data && response.data.code === 200) {
      order.value = response.data.data;
      
      // 检查订单状态，如果已经支付则跳转到订单详情页
      if (order.value.status !== 'PENDING_PAYMENT') {
        ElMessage.info('该订单已处理，无需支付');
        router.replace(`/order/${orderNo.value}`);
        return;
      }
      
      // 设置倒计时（订单有30分钟支付时间）
      const orderTime = new Date(order.value.createTime).getTime();
      const expiryTime = orderTime + 30 * 60 * 1000; // 30分钟后
      const now = Date.now();
      const remainingTime = Math.max(0, Math.floor((expiryTime - now) / 1000));
      
      if (remainingTime <= 0) {
        ElMessage.warning('订单已超时，请重新下单');
        router.replace(`/order/${orderNo.value}`);
        return;
      }
      
      startCountdown(remainingTime);
      
      // 获取钱包信息
      await fetchWalletInfo();
    } else {
      ElMessage.error('获取订单信息失败');
      router.replace(`/order/${orderNo.value}`);
    }
  } catch (error) {
    console.error('获取订单信息出错:', error);
    ElMessage.error('获取订单信息失败: ' + (error.message || '网络错误'));
    router.replace(`/order/${orderNo.value}`);
  } finally {
    loading.value = false;
  }
};

// 获取钱包信息
const fetchWalletInfo = async () => {
  try {
    const response = await getUserWalletAPI();
    
    if (response.data && response.data.code === 200) {
      wallet.value = response.data.data;
    } else {
      ElMessage.error('获取钱包信息失败');
    }
  } catch (error) {
    console.error('获取钱包信息出错:', error);
    ElMessage.error('获取钱包信息失败: ' + (error.message || '网络错误'));
  }
};

// 开始倒计时
const startCountdown = (seconds) => {
  countdown.value = seconds;
  clearInterval(timer);
  
  timer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--;
    } else {
      clearInterval(timer);
      ElMessage.warning('支付超时，订单已取消');
      router.replace(`/order/${orderNo.value}`);
    }
  }, 1000);
};

// 处理支付
const handlePayment = async () => {
  if (submitting.value) return;
  
  if (paymentMethod.value === 'WALLET' && !walletEnough.value) {
    ElMessage.warning('钱包余额不足，请选择其他支付方式或充值后再支付');
    return;
  }
  
  try {
    submitting.value = true;
    
    await ElMessageBox.confirm(
      `确认支付订单 ${orderNo.value}？金额: ¥${order.value.actualPaymentAmount}`, 
      '确认支付', 
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'info' }
    );
    
    try {
      let response;
      
      if (paymentMethod.value === 'WALLET') {
        // 使用钱包支付
        response = await payOrderWithWalletAPI(orderNo.value);
      } else {
        // 使用其他支付方式
        response = await orderApi.payOrder(orderNo.value);
      }
      
      if (response.data && response.data.code === 200) {
        ElMessage.success('支付成功！');
        router.replace(`/order/${orderNo.value}?paid=true`);
      } else {
        ElMessage.error('支付失败');
      }
    } catch (error) {
      console.error('支付API错误:', error);
      ElMessage.error('支付失败: ' + (error.message || '网络错误'));
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('支付出错:', error);
      ElMessage.error('支付失败: ' + (error.message || '网络错误'));
    }
  } finally {
    submitting.value = false;
  }
};

// 取消支付
const cancelPayment = () => {
  ElMessageBox.confirm(
    '确定取消支付并返回订单详情页？', 
    '取消支付', 
    { confirmButtonText: '确定', cancelButtonText: '继续支付', type: 'warning' }
  )
    .then(() => {
      router.replace(`/order/${orderNo.value}`);
    })
    .catch(() => {
      // 用户选择继续支付，不做任何处理
    });
};

// 组件挂载
onMounted(() => {
  if (!orderNo.value) {
    ElMessage.error('订单号不能为空');
    router.replace('/');
    return;
  }
  
  // 从URL参数获取支付方式
  const methodParam = route.query.method;
  if (methodParam) {
    if (methodParam === 'WALLET' || methodParam === 'ALIPAY' || methodParam === 'WECHAT') {
      paymentMethod.value = methodParam;
    } else if (methodParam === 'OTHER') {
      // 其他支付方式默认设置为支付宝
      paymentMethod.value = 'ALIPAY';
    }
  }
  
  fetchOrderInfo();
});

// 组件卸载
onUnmounted(() => {
  if (timer) {
    clearInterval(timer);
  }
});
</script>

<template>
  <div class="payment-page">
    <div class="container">
      <!-- 加载中状态 -->
      <div v-if="loading" class="loading-container">
        <div class="spinner"></div>
        <p>正在加载订单信息...</p>
      </div>
      
      <!-- 支付界面 -->
      <div v-else class="payment-content">
        <!-- 订单信息 -->
        <div class="order-info-section">
          <div class="order-header">
            <span class="icon">✓</span>
            <div class="order-details">
              <h2>订单提交成功！请尽快完成支付</h2>
              <p class="countdown">
                支付倒计时：<span>{{ countdownText }}</span>
              </p>
            </div>
          </div>
          
          <div class="order-summary">
            <div class="order-id">
              <span class="label">订单号：</span>
              <span class="value">{{ orderNo }}</span>
            </div>
            
            <div class="amount-row">
              <span class="label">订单金额：</span>
              <span class="value">¥{{ order?.actualPaymentAmount?.toFixed(2) }}</span>
            </div>
            
            <div v-if="order?.pointsUsed > 0" class="amount-row">
              <span class="label">使用积分：</span>
              <div class="points-info">
                <span>{{ order.pointsUsed }} (已抵扣 ¥{{ order.pointsDeductionAmount?.toFixed(2) }})</span>
              </div>
            </div>
            
            <div class="amount-row final-amount">
              <span class="label">应付金额：</span>
              <span class="value">¥{{ order?.actualPaymentAmount?.toFixed(2) }}</span>
            </div>
          </div>
        </div>
        
        <!-- 支付方式选择 -->
        <div class="payment-methods-section">
          <h3>选择支付方式</h3>
          
          <!-- 钱包信息 -->
          <div v-if="wallet.balance !== undefined" class="wallet-info">
            <p>
              钱包余额：
              <span :class="{ 'insufficient': !walletEnough && paymentMethod === 'WALLET' }">
                ¥{{ wallet.balance?.toFixed(2) }}
              </span>
            </p>
            <p v-if="wallet.points">积分：{{ wallet.points }}</p>
            <p v-if="paymentMethod === 'WALLET' && !walletEnough" class="error-tip">
              余额不足，请选择其他支付方式或 
              <router-link to="/wallet">前往充值</router-link>
            </p>
          </div>
          
          <!-- 支付方式列表 -->
          <div class="payment-options">
            <div 
              class="payment-option" 
              :class="{ active: paymentMethod === 'WALLET' }"
              @click="paymentMethod = 'WALLET'"
            >
              <div class="payment-icon wallet-icon"></div>
              <span>钱包支付</span>
            </div>
            
            <div 
              class="payment-option" 
              :class="{ active: paymentMethod === 'ALIPAY' }"
              @click="paymentMethod = 'ALIPAY'"
            >
              <div class="payment-icon alipay-icon"></div>
              <span>支付宝</span>
            </div>
            
            <div 
              class="payment-option" 
              :class="{ active: paymentMethod === 'WECHAT' }"
              @click="paymentMethod = 'WECHAT'"
            >
              <div class="payment-icon wechat-icon"></div>
              <span>微信支付</span>
            </div>
          </div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="payment-actions">
          <button 
            class="btn-cancel" 
            @click="cancelPayment"
            :disabled="submitting"
          >
            取消支付
          </button>
          <button 
            class="btn-pay" 
            @click="handlePayment"
            :disabled="submitting || (paymentMethod === 'WALLET' && !walletEnough)"
          >
            {{ submitting ? '处理中...' : '立即支付' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.payment-page {
  min-height: 80vh;
  padding: 40px 0;
  background-color: #f8f9fa;
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #4a6ee0;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.payment-content {
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

/* 订单信息区域 */
.order-info-section {
  padding: 30px;
  border-bottom: 1px solid #f1f1f1;
}

.order-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}

.icon {
  width: 50px;
  height: 50px;
  background-color: #4caf50;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  margin-right: 20px;
}

.order-details h2 {
  font-size: 20px;
  margin: 0 0 10px;
  color: #333;
}

.countdown {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.countdown span {
  color: #ff5252;
  font-weight: 600;
}

.order-summary {
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 6px;
}

.order-id, .amount-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.amount-row:last-child {
  margin-bottom: 0;
}

.label {
  color: #666;
}

.value {
  font-weight: 500;
  color: #333;
}

.points-info {
  margin-left: 10px;
  font-size: 14px;
  color: #888;
}

.final-amount .value {
  font-size: 20px;
  color: #f44336;
  font-weight: 600;
}

/* 支付方式区域 */
.payment-methods-section {
  padding: 30px;
  border-bottom: 1px solid #f1f1f1;
}

.payment-methods-section h3 {
  font-size: 18px;
  color: #333;
  margin: 0 0 20px;
}

.wallet-info {
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 20px;
}

.wallet-info p {
  margin: 8px 0;
}

.insufficient {
  color: #f44336;
}

.error-tip {
  color: #f44336;
  font-size: 14px;
}

.error-tip a {
  color: #4a6ee0;
  text-decoration: none;
}

.payment-options {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.payment-option {
  flex: 1;
  min-width: 150px;
  height: 80px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.payment-option:hover {
  border-color: #4a6ee0;
}

.payment-option.active {
  border-color: #4a6ee0;
  background-color: rgba(74, 110, 224, 0.05);
}

.payment-icon {
  width: 30px;
  height: 30px;
  margin-bottom: 8px;
  background-position: center;
  background-repeat: no-repeat;
  background-size: contain;
}

.wallet-icon {
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="%234a6ee0"><path d="M22 6h-5c-1.7 0-3 1.3-3 3s1.3 3 3 3h5v8H2V4h20v2zm0 2v2h-5c-.6 0-1-.4-1-1s.4-1 1-1h5zm-7-6H2C.9 2 0 2.9 0 4v16c0 1.1.9 2 2 2h20c1.1 0 2-.9 2-2V6c0-2.2-1.8-4-4-4z"/></svg>');
}

.alipay-icon {
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="%2300a0e9"><path d="M21.6 12.2c0-5.4-4.4-9.8-9.8-9.8S2 6.8 2 12.2c0 5.4 4.4 9.8 9.8 9.8s9.8-4.4 9.8-9.8zM8.7 15.8c-1.7 0-2.9-1.1-2.9-2.7 0-1.6 1.2-2.7 2.9-2.7 1.7 0 2.9 1.1 2.9 2.7 0 1.6-1.2 2.7-2.9 2.7zm6.6 0c-1.7 0-2.9-1.1-2.9-2.7 0-1.6 1.2-2.7 2.9-2.7 1.7 0 2.9 1.1 2.9 2.7-.1 1.6-1.3 2.7-2.9 2.7z"/></svg>');
}

.wechat-icon {
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="%2309bb07"><path d="M9.5 7.5c-.8 0-1.5.7-1.5 1.5s.7 1.5 1.5 1.5 1.5-.7 1.5-1.5-.7-1.5-1.5-1.5zm5 0c-.8 0-1.5.7-1.5 1.5s.7 1.5 1.5 1.5 1.5-.7 1.5-1.5-.7-1.5-1.5-1.5zm6.5 5.5c0-5-5-9-10-9-5.5 0-10 4-10 9 0 5 4.5 9 10 9 1.2 0 2.2-.2 3.2-.5l3 1.7c.1.1.2.1.3.1.1 0 .2 0 .2-.1.2-.1.3-.3.3-.5l-.6-3.4c2.2-1.9 3.6-4.3 3.6-6.3zm-13-4.5c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm5 0c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"/></svg>');
}

/* 操作按钮区域 */
.payment-actions {
  padding: 20px 30px;
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.btn-cancel, .btn-pay {
  padding: 12px 24px;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.btn-cancel {
  background-color: #f0f2f5;
  color: #555;
}

.btn-pay {
  background-color: #4a6ee0;
  color: white;
}

.btn-cancel:hover:not(:disabled), .btn-pay:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
}

.btn-cancel:disabled, .btn-pay:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .payment-page {
    padding: 20px 0;
  }
  
  .order-header {
    flex-direction: column;
    text-align: center;
  }
  
  .icon {
    margin-right: 0;
    margin-bottom: 15px;
  }
  
  .payment-options {
    flex-direction: column;
  }
  
  .payment-option {
    flex-direction: row;
    height: 60px;
    justify-content: flex-start;
    padding: 0 20px;
  }
  
  .payment-icon {
    margin-bottom: 0;
    margin-right: 15px;
  }
}
</style> 