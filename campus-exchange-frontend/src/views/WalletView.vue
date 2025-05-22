<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus';
import { getUserWalletAPI, initUserWalletAPI, rechargeWalletAPI, withdrawWalletAPI, getWalletTransactionsAPI } from '@/api/wallet';

// 页面状态
const loading = ref(false);
const activeTab = ref('balance'); // balance 或 points

// 钱包信息
const wallet = ref({
  balance: 0,
  frozenAmount: 0,
  points: 0
});

// 交易记录
const transactions = ref([]);
const currentPage = ref(1);
const pageSize = 10;
const hasMoreTransactions = ref(true);

// 充值和提现
const showRechargeDialog = ref(false);
const showWithdrawDialog = ref(false);
const rechargeAmount = ref(null);
const withdrawAmount = ref(null);
const processingPayment = ref(false);

// 获取钱包信息
const fetchWalletInfo = async (retryCount = 0) => {
  const loadingInstance = ElLoading.service({ 
    target: '.wallet-container',
    text: '加载钱包信息...' 
  });
  
  try {
    loading.value = true;
    const response = await getUserWalletAPI();
    
    if (response.data && response.data.code === 200) {
      wallet.value = response.data.data;
    } else {
      ElMessage.error('获取钱包信息失败');
    }
  } catch (error) {
    console.error('获取钱包信息出错:', error);
    
    // 检测是否是钱包初始化冲突错误
    if (error.message && error.message.includes('Duplicate entry') && error.message.includes('wallet.uk_user_id')) {
      // 这是钱包初始化时的并发问题，尝试重试
      if (retryCount < 3) { // 最多重试3次
        console.log(`检测到钱包初始化冲突，1秒后进行第${retryCount + 1}次重试...`);
        loadingInstance.close();
        
        // 等待一秒后重试，给后端时间处理冲突
        setTimeout(() => {
          fetchWalletInfo(retryCount + 1);
        }, 1000);
        return;
      } else {
        ElMessage.error('系统正在初始化您的钱包，请稍后再试');
      }
    } else {
      // 尝试初始化钱包
      try {
        console.log('尝试初始化钱包...');
        await initUserWallet();
      } catch (initError) {
        console.error('初始化钱包失败:', initError);
      }
    }
  } finally {
    loading.value = false;
    loadingInstance.close();
  }
};

// 初始化用户钱包
const initUserWallet = async () => {
  try {
    const response = await initUserWalletAPI();
    
    if (response.data && response.data.code === 200) {
      console.log('钱包初始化成功');
      wallet.value = response.data.data;
      ElMessage.success('钱包初始化成功');
    } else {
      ElMessage.error('钱包初始化失败');
    }
  } catch (error) {
    console.error('初始化钱包API错误:', error);
    
    // 如果是钱包已存在的错误，忽略它并重新获取钱包信息
    if (error.message && error.message.includes('Duplicate entry')) {
      console.log('钱包已存在，重新获取钱包信息...');
      setTimeout(() => {
        fetchWalletInfo();
      }, 1000);
    } else {
      ElMessage.error('初始化钱包失败: ' + (error.message || '网络错误'));
    }
  }
};

// 获取交易记录
const fetchTransactions = async (reset = false) => {
  if (reset) {
    currentPage.value = 1;
    transactions.value = [];
    hasMoreTransactions.value = true;
  }
  
  if (!hasMoreTransactions.value) return;
  
  try {
    loading.value = true;
    // 确保传递正确的页码参数
    const response = await getWalletTransactionsAPI(
      currentPage.value, 
      pageSize,
      activeTab.value === 'points' ? 'POINTS' : 'BALANCE'
    );
    
    if (response.data && response.data.code === 200) {
      const newTransactions = response.data.data.records || [];
      console.log('获取到交易记录:', newTransactions);
      
      // 处理余额显示问题 - 如果余额都显示为0，手动计算正确的余额
      let runningBalance = activeTab.value === 'points' ? wallet.value.points : wallet.value.balance;
      
      // 按时间倒序排列交易记录（假设服务器返回的是按时间倒序排列的）
      const processedTransactions = [...newTransactions];
      
      // 处理余额显示
      for (let i = processedTransactions.length - 1; i >= 0; i--) {
        const transaction = processedTransactions[i];
        
        // 确保amount字段有值（积分记录应使用pointsChange或amount）
        if (activeTab.value === 'points' && !transaction.amount && transaction.pointsChange) {
          transaction.amount = transaction.pointsChange;
        }
        
        // 确保balanceAfter字段有值（积分记录应使用balanceAfterTransaction或balanceAfter）
        if (activeTab.value === 'points') {
          // 如果为积分记录，将balanceAfterTransaction赋值给balanceAfter
          if (!transaction.balanceAfter && transaction.balanceAfterTransaction !== undefined) {
            transaction.balanceAfter = transaction.balanceAfterTransaction;
          }
        }
        
        // 如果balanceAfter为空或0，则根据当前余额和交易金额计算
        if (!transaction.balanceAfter && transaction.balanceAfter !== 0) {
          // 对于较早的交易，累加计算余额（消费为负，充值为正）
          if (i < processedTransactions.length - 1) {
            runningBalance = runningBalance - transaction.amount;
          }
          transaction.balanceAfter = runningBalance;
        }
      }
      
      // 特殊处理积分交易记录筛选
      let filteredTransactions = processedTransactions;
      if (activeTab.value === 'points') {
        // 积分页不需要额外筛选，因为API已经返回的是积分记录
        filteredTransactions = processedTransactions;
      } else {
        // 余额页面，过滤掉积分相关记录
        filteredTransactions = processedTransactions.filter(t => 
          !t.type.includes('POINT') && 
          !t.type.includes('PURCHASE_EARNED') && 
          !t.type.includes('ORDER_DEDUCTION_USED') && 
          !t.type.includes('SYSTEM_REWARD') && 
          !t.type.includes('SYSTEM_DEDUCTION') && 
          !t.type.includes('REFUND_RETURNED')
        );
      }
      
      // 检查当前页是否是最后一页
      if (filteredTransactions.length === 0 || 
          (response.data.data.pages && currentPage.value >= response.data.data.pages) ||
          filteredTransactions.length < pageSize) {
        hasMoreTransactions.value = false;
      } else {
        // 只有在成功加载并且还有更多页面时，才增加页码
        currentPage.value++;
      }
      
      // 正确合并新加载的数据与现有数据
      transactions.value = [...transactions.value, ...filteredTransactions];
    } else {
      ElMessage.error('获取交易记录失败');
      hasMoreTransactions.value = false;
    }
  } catch (error) {
    console.error('获取交易记录出错:', error);
    ElMessage.error('获取交易记录失败: ' + (error.message || '网络错误'));
    hasMoreTransactions.value = false;
  } finally {
    loading.value = false;
  }
};

// 处理充值
const handleRecharge = async () => {
  if (!rechargeAmount.value || rechargeAmount.value <= 0) {
    ElMessage.warning('请输入有效的充值金额');
    return;
  }
  
  try {
    processingPayment.value = true;
    
    await ElMessageBox.confirm(
      `确认充值 ¥${rechargeAmount.value} 到您的账户？`, 
      '确认充值', 
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'info' }
    );
    
    try {
      const response = await rechargeWalletAPI(rechargeAmount.value);
      
      if (response.data && response.data.code === 200) {
        ElMessage.success('充值成功');
        // 刷新钱包信息和交易记录
        await fetchWalletInfo();
        fetchTransactions(true);
        showRechargeDialog.value = false;
        rechargeAmount.value = null;
      } else {
        ElMessage.error('充值失败');
      }
    } catch (error) {
      console.error('充值API错误:', error);
      ElMessage.error('充值失败: ' + (error.message || '网络错误'));
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('充值出错:', error);
      ElMessage.error('充值失败: ' + (error.message || '网络错误'));
    }
  } finally {
    processingPayment.value = false;
  }
};

// 处理提现
const handleWithdraw = async () => {
  if (!withdrawAmount.value || withdrawAmount.value <= 0) {
    ElMessage.warning('请输入有效的提现金额');
    return;
  }
  
  if (withdrawAmount.value > wallet.value.balance) {
    ElMessage.warning('提现金额不能超过账户余额');
    return;
  }
  
  try {
    processingPayment.value = true;
    
    await ElMessageBox.confirm(
      `确认从您的账户提现 ¥${withdrawAmount.value}？`, 
      '确认提现', 
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'info' }
    );
    
    try {
      const response = await withdrawWalletAPI(withdrawAmount.value);
      
      if (response.data && response.data.code === 200) {
        ElMessage.success('提现申请已提交，请等待处理');
        // 刷新钱包信息和交易记录
        await fetchWalletInfo();
        fetchTransactions(true);
        showWithdrawDialog.value = false;
        withdrawAmount.value = null;
      } else {
        ElMessage.error('提现失败');
      }
    } catch (error) {
      console.error('提现API错误:', error);
      ElMessage.error('提现失败: ' + (error.message || '网络错误'));
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提现出错:', error);
      ElMessage.error('提现失败: ' + (error.message || '网络错误'));
    }
  } finally {
    processingPayment.value = false;
  }
};

// 获取交易类型文本
const getTransactionTypeText = (type) => {
  // 支持钱包交易类型
  const walletTypeMap = {
    RECHARGE: '充值',
    WITHDRAW: '提现',
    PAYMENT: '支付',
    REFUND: '退款',
    REWARD: '奖励',
    COMMISSION: '佣金',
    CONSUMPTION: '消费',
    INCOME: '收入',
    MERCHANT_INCOME: '商家收入',
    TRANSFER: '转账',
    EXCHANGE: '兑换',
    DEDUCTION: '扣除',
    SYSTEM_ADJUSTMENT: '系统调整',
    POINT_EARNED: '获得积分',
    POINT_USED: '使用积分',
    POINT_EXPIRED: '积分过期',
    INTEREST: '利息'
  };
  
  // 积分交易类型
  const pointsTypeMap = {
    PURCHASE_EARNED: '消费获得',
    ORDER_DEDUCTION_USED: '订单抵扣使用',
    SYSTEM_REWARD: '系统奖励',
    SYSTEM_DEDUCTION: '系统扣减',
    REFUND_RETURNED: '退款返还积分'
  };
  
  return pointsTypeMap[type] || walletTypeMap[type] || type;
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 监听标签页变化
watch(activeTab, () => {
  fetchTransactions(true);
});

// 页面加载
onMounted(() => {
  // 注意：如果后端没有实现wallet/init接口，可能会在首次访问时报错
  // 这个问题是因为后端在用户访问钱包信息时，如果检测到用户没有钱包，会尝试创建一个
  // 但如果同时发起多个请求，就会出现数据库唯一约束冲突
  // 解决方法：
  // 1. 后端实现wallet/init接口，支持显式初始化钱包
  // 2. 后端在wallet/info接口中进行并发控制，确保同一用户只有一个钱包创建请求能够成功
  fetchWalletInfo();
  fetchTransactions();
});
</script>

<template>
  <div class="wallet-container">
    <h1 class="page-title">我的钱包</h1>
    
    <!-- 钱包概览 -->
    <div class="wallet-overview">
      <div class="wallet-card balance-card">
        <h2>我的余额</h2>
        <p class="amount">¥{{ wallet.balance ? wallet.balance.toFixed(2) : '0.00' }}</p>
        <div class="wallet-actions">
          <button @click="showRechargeDialog = true" class="btn-recharge">充值</button>
          <button @click="showWithdrawDialog = true" class="btn-withdraw">提现</button>
        </div>
      </div>
      
      <div class="wallet-card points-card">
        <h2>我的积分</h2>
        <p class="amount">{{ wallet.points }}</p>
        <p class="points-tips">积分可在支付时抵扣，100积分=1元</p>
      </div>
    </div>
    
    <!-- 交易记录标签页 -->
    <div class="transaction-tabs">
      <button 
        :class="{ active: activeTab === 'balance' }"
        @click="activeTab = 'balance'"
      >
        余额明细
      </button>
      <button 
        :class="{ active: activeTab === 'points' }"
        @click="activeTab = 'points'"
      >
        积分明细
      </button>
    </div>
    
    <!-- 交易记录列表 -->
    <div class="transaction-list">
      <table>
        <thead>
          <tr>
            <th>时间</th>
            <th>类型</th>
            <th>{{ activeTab === 'balance' ? '金额' : '积分' }}</th>
            <th>{{ activeTab === 'balance' ? '余额' : '积分余额' }}</th>
            <th>说明</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="transaction in transactions" :key="transaction.id">
            <td>{{ formatDate(transaction.createTime) }}</td>
            <td>{{ getTransactionTypeText(transaction.type) }}</td>
            <td :class="transaction.amount > 0 ? 'credit' : 'debit'">
              {{ transaction.amount > 0 ? '+' : '' }}{{ 
                activeTab === 'balance' 
                  ? '¥' + Math.abs(transaction.amount).toFixed(2) 
                  : Math.abs(transaction.amount) 
              }}
            </td>
            <td>
              {{ activeTab === 'balance' 
                  ? '¥' + (transaction.balanceAfter !== null && transaction.balanceAfter !== undefined 
                      ? Number(transaction.balanceAfter).toFixed(2) 
                      : '0.00')
                  : transaction.balanceAfter || 0
              }}
            </td>
            <td>{{ transaction.description }}</td>
          </tr>
          
          <tr v-if="transactions.length === 0">
            <td colspan="5" class="empty-message">
              {{ loading ? '加载中...' : '暂无交易记录' }}
            </td>
          </tr>
        </tbody>
      </table>
      
      <div v-if="hasMoreTransactions && transactions.length > 0" class="load-more">
        <button 
          @click="fetchTransactions(false)"
          :disabled="loading"
        >
          {{ loading ? '加载中...' : '加载更多' }}
        </button>
      </div>
    </div>
    
    <!-- 充值对话框 -->
    <div v-if="showRechargeDialog" class="modal-overlay">
      <div class="modal-container">
        <div class="modal-header">
          <h3>账户充值</h3>
          <button class="close-btn" @click="showRechargeDialog = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>充值金额</label>
            <input 
              v-model.number="rechargeAmount" 
              type="number" 
              min="1" 
              placeholder="请输入充值金额"
              class="form-control"
            >
          </div>
        </div>
        <div class="modal-footer">
          <button 
            class="btn-cancel" 
            @click="showRechargeDialog = false"
            :disabled="processingPayment"
          >
            取消
          </button>
          <button 
            class="btn-confirm" 
            @click="handleRecharge"
            :disabled="processingPayment"
          >
            {{ processingPayment ? '处理中...' : '确认充值' }}
          </button>
        </div>
      </div>
    </div>
    
    <!-- 提现对话框 -->
    <div v-if="showWithdrawDialog" class="modal-overlay">
      <div class="modal-container">
        <div class="modal-header">
          <h3>账户提现</h3>
          <button class="close-btn" @click="showWithdrawDialog = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>提现金额</label>
            <input 
              v-model.number="withdrawAmount" 
              type="number" 
              :max="wallet.balance" 
              min="1" 
              placeholder="请输入提现金额"
              class="form-control"
            >
                        <div class="balance-info">              可提现金额: ¥{{ wallet.balance ? wallet.balance.toFixed(2) : '0.00' }}            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button 
            class="btn-cancel" 
            @click="showWithdrawDialog = false"
            :disabled="processingPayment"
          >
            取消
          </button>
          <button 
            class="btn-confirm" 
            @click="handleWithdraw"
            :disabled="processingPayment"
          >
            {{ processingPayment ? '处理中...' : '确认提现' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.wallet-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #333;
}

/* 钱包概览样式 */
.wallet-overview {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.wallet-card {
  flex: 1;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.balance-card {
  background: linear-gradient(135deg, #4a6ee0, #6a8cff);
  color: white;
}

.points-card {
  background: linear-gradient(135deg, #ff9800, #ffb74d);
  color: white;
}

.wallet-card h2 {
  font-size: 18px;
  margin-bottom: 10px;
}

.wallet-card .amount {
  font-size: 36px;
  font-weight: 700;
  margin: 15px 0;
}

.wallet-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.btn-recharge, .btn-withdraw {
  flex: 1;
  padding: 8px 0;
  border: none;
  border-radius: 4px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-recharge {
  background-color: rgba(255, 255, 255, 0.9);
  color: #4a6ee0;
}

.btn-withdraw {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.btn-recharge:hover, .btn-withdraw:hover {
  transform: translateY(-2px);
}

.points-tips {
  font-size: 14px;
  opacity: 0.8;
}

/* 交易记录标签页 */
.transaction-tabs {
  margin-bottom: 20px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
}

.transaction-tabs button {
  padding: 10px 20px;
  background: none;
  border: none;
  font-size: 16px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  position: relative;
}

.transaction-tabs button.active {
  color: #4a6ee0;
}

.transaction-tabs button.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #4a6ee0;
}

/* 交易记录列表 */
.transaction-list {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 15px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  background-color: #f9f9f9;
  font-weight: 600;
  color: #555;
}

td.credit {
  color: #4caf50;
}

td.debit {
  color: #f44336;
}

.empty-message {
  text-align: center;
  color: #888;
  padding: 30px;
}

.load-more {
  text-align: center;
  padding: 20px;
}

.load-more button {
  padding: 8px 20px;
  background-color: #f0f2f5;
  border: none;
  border-radius: 4px;
  color: #555;
  cursor: pointer;
  transition: all 0.2s;
}

.load-more button:hover:not(:disabled) {
  background-color: #e0e2e5;
}

.load-more button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 对话框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-container {
  background-color: white;
  border-radius: 8px;
  width: 400px;
  max-width: 90%;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-weight: 500;
}

.form-control {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

.balance-info {
  margin-top: 8px;
  font-size: 14px;
  color: #888;
}

.modal-footer {
  padding: 15px 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  border-top: 1px solid #eee;
}

.btn-cancel, .btn-confirm {
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.btn-cancel {
  background-color: #f0f2f5;
  color: #555;
}

.btn-confirm {
  background-color: #4a6ee0;
  color: white;
}

.btn-cancel:hover:not(:disabled), .btn-confirm:hover:not(:disabled) {
  opacity: 0.9;
}

.btn-cancel:disabled, .btn-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .wallet-overview {
    flex-direction: column;
  }
  
  th, td {
    padding: 10px;
  }
  
  .modal-container {
    width: 95%;
  }
}
</style> 