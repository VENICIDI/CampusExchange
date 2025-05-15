<template>
  <div class="product-info-container">
    <h1 class="product-title">{{ product.name }}</h1>
    
    <div class="product-price">
      <span class="current-price">¥{{ product.currentPrice }}</span>
      <span v-if="product.originalPrice && product.originalPrice > product.currentPrice" class="original-price">¥{{ product.originalPrice }}</span>
      <span v-if="product.negotiable" class="negotiable-tag">可议价</span>
      <span v-if="product.status === 'SOLD_OUT' || product.stock <= 0" class="soldout-tag">已售罄</span>
    </div>
    
    <div class="product-meta">
      <div class="meta-item merchant-item">
        <span class="meta-label">店铺:</span>
        <span class="meta-value store-link" @click="navigateToStore">
          {{ product.storeName || '未知店铺' }}
          <i class="el-icon-arrow-right"></i>
        </span>
      </div>
      <div class="meta-item">
        <span class="meta-label">新旧程度:</span>
        <span class="meta-value">{{ formatCondition(product.productCondition) }}</span>
      </div>
      <div class="meta-item">
        <span class="meta-label">库存:</span>
        <span class="meta-value" :class="{'stock-low': product.stock > 0 && product.stock <= 5, 'stock-out': product.stock <= 0}">
          {{ product.stock > 0 ? `${product.stock} 件` : '无货' }}
        </span>
      </div>
      <div v-if="product.size" class="meta-item">
        <span class="meta-label">尺寸大小:</span>
        <span class="meta-value">{{ product.size }}</span>
      </div>
      <div class="meta-item">
        <span class="meta-label">销量:</span>
        <span class="meta-value">{{ product.sales || 0 }}</span>
      </div>
      <div v-if="product.rating" class="meta-item">
        <span class="meta-label">评分:</span>
        <span class="meta-value">{{ product.rating }} 分</span>
      </div>
      <div class="meta-item">
        <span class="meta-label">状态:</span>
        <span class="meta-value" :class="getStatusClass(product.status)">
          {{ formatStatus(product.status) }}
        </span>
      </div>
    </div>
    
    <div class="product-actions">
      <button @click="$emit('add-to-cart')" 
              class="btn-add-to-cart" 
              :disabled="!isProductAvailable"
              :class="{'btn-disabled': !isProductAvailable}">
        {{ isProductAvailable ? '加入购物车' : '已售罄' }}
      </button>
      <button @click="$emit('buy-now')" 
              class="btn-buy-now" 
              :disabled="!isProductAvailable"
              :class="{'btn-disabled': !isProductAvailable}">
        {{ isProductAvailable ? '立即购买' : '已售罄' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, computed } from 'vue';
import { useRouter } from 'vue-router';

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
});

defineEmits(['add-to-cart', 'buy-now']);
const router = useRouter();

// 判断商品是否可购买
const isProductAvailable = computed(() => {
  return props.product.status === 'ON_SALE' && props.product.stock > 0;
});

// 导航到商家主页
const navigateToStore = () => {
  if (props.product && props.product.merchantId) {
    router.push(`/store/${props.product.merchantId}`);
  }
};

// 格式化商品新旧程度
const formatCondition = (condition) => {
  if (!condition) return '未知';
  
  const conditionMap = {
    'NEW': '全新',
    'LIKE_NEW': '九成新',
    'GOOD': '八成新',
    'FAIR': '七成新',
    'POOR': '六成新及以下'
  };
  
  return conditionMap[condition] || condition;
};

// 格式化商品状态
const formatStatus = (status) => {
  if (!status) return '未知';
  
  const statusMap = {
    'PENDING_APPROVAL': '待审核',
    'REJECTED_RESUBMIT': '审核不通过',
    'ON_SALE': '在售',
    'LOCKED': '已锁定',
    'SOLD_OUT': '已售罄',
    'REMOVED_BY_SELLER': '已下架'
  };
  
  return statusMap[status] || status;
};

// 获取状态对应的样式类
const getStatusClass = (status) => {
  const classMap = {
    'PENDING_APPROVAL': 'status-pending',
    'REJECTED_RESUBMIT': 'status-rejected',
    'ON_SALE': 'status-onsale',
    'LOCKED': 'status-locked',
    'SOLD_OUT': 'status-soldout',
    'REMOVED_BY_SELLER': 'status-removed'
  };
  
  return classMap[status] || '';
};
</script>

<style scoped>
.product-info-container {
  display: flex;
  flex-direction: column;
}

.product-title {
  font-size: 1.8rem;
  font-weight: 700;
  margin-bottom: 20px;
  color: #333;
}

.product-price {
  margin-bottom: 25px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.current-price {
  font-size: 2rem;
  font-weight: bold;
  color: #ff4757;
  margin-right: 15px;
}

.original-price {
  font-size: 1.2rem;
  color: #999;
  text-decoration: line-through;
}

.negotiable-tag {
  margin-left: 15px;
  background-color: #fff4e5;
  color: #ff9800;
  padding: 4px 10px;
  font-size: 0.8rem;
  border-radius: 4px;
  font-weight: 500;
}

.soldout-tag {
  margin-left: 15px;
  background-color: #f5f5f5;
  color: #999;
  padding: 4px 10px;
  font-size: 0.8rem;
  border-radius: 4px;
  font-weight: 500;
}

.product-meta {
  margin-bottom: 30px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  row-gap: 15px;
  padding: 20px;
  background-color: #f9fafc;
  border-radius: 10px;
  border-left: 4px solid #4a6ee0;
}

.meta-item {
  font-size: 1rem;
  display: flex;
  align-items: center;
}

.merchant-item {
  grid-column: 1 / -1;
  margin-bottom: 5px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #e0e0e0;
}

.meta-label {
  color: #666;
  margin-right: 8px;
  min-width: 70px;
}

.meta-value {
  color: #333;
  font-weight: 500;
}

.meta-value.store-link {
  color: #3273dc;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 4px;
  transition: all 0.2s ease;
  background-color: rgba(50, 115, 220, 0.1);
}

.meta-value.store-link:hover {
  background-color: rgba(50, 115, 220, 0.2);
  transform: translateX(2px);
}

.stock-low {
  color: #fa8c16;
}

.stock-out {
  color: #f5222d;
}

.status-pending, .status-rejected {
  color: #fa8c16;
}

.status-onsale {
  color: #52c41a;
}

.status-locked, .status-soldout, .status-removed {
  color: #f5222d;
}

.product-actions {
  display: flex;
  gap: 15px;
  margin-top: auto;
}

.btn-add-to-cart, .btn-buy-now {
  flex: 1;
  padding: 15px 0;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-add-to-cart {
  background-color: #fff0f0;
  color: #ff4757;
  border: 1px solid #ff4757;
}

.btn-add-to-cart:hover:not(:disabled) {
  background-color: #ffe5e5;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(255, 71, 87, 0.2);
}

.btn-buy-now {
  background-color: #ff4757;
  color: white;
}

.btn-buy-now:hover:not(:disabled) {
  background-color: #ff2c3e;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(255, 71, 87, 0.3);
}

.btn-disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}

.btn-add-to-cart.btn-disabled {
  background-color: #f5f5f5;
  color: #999;
  border-color: #d9d9d9;
}

.btn-buy-now.btn-disabled {
  background-color: #d9d9d9;
}
</style> 