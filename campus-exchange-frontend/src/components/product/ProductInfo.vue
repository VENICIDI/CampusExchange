<template>
  <div class="product-info-container">
    <h1 class="product-title">{{ product.name }}</h1>
    
    <div class="product-price">
      <span class="current-price">¥{{ product.currentPrice }}</span>
      <span v-if="product.originalPrice && product.originalPrice > product.currentPrice" class="original-price">¥{{ product.originalPrice }}</span>
      <span v-if="product.negotiable" class="negotiable-tag">可议价</span>
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
        <span class="meta-value">{{ product.stock || 0 }} 件</span>
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
    </div>
    
    <div class="product-actions">
      <button @click="$emit('add-to-cart')" class="btn-add-to-cart">加入购物车</button>
      <button @click="$emit('buy-now')" class="btn-buy-now">立即购买</button>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';
import { useRouter } from 'vue-router';

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
});

defineEmits(['add-to-cart', 'buy-now']);
const router = useRouter();

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

.btn-add-to-cart:hover {
  background-color: #ffe5e5;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(255, 71, 87, 0.2);
}

.btn-buy-now {
  background-color: #ff4757;
  color: white;
}

.btn-buy-now:hover {
  background-color: #ff2c3e;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(255, 71, 87, 0.3);
}
</style> 