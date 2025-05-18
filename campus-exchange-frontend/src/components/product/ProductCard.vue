<template>
  <div class="product-card" @click="handleClick">
    <div class="product-image">
      <img :src="product.mainImage || product.coverImage || 'https://via.placeholder.com/200x150'" :alt="product.name" />
      <div v-if="product.negotiable" class="negotiable-badge">可议价</div>
    </div>
    <div class="product-info">
      <h4 class="product-title">{{ product.name }}</h4>
      <div class="product-price-row">
        <div class="product-price">¥{{ product.currentPrice }}</div>
        <div v-if="product.originalPrice && product.originalPrice > product.currentPrice" class="original-price">¥{{ product.originalPrice }}</div>
      </div>
      <div v-if="showMerchant && product.merchantId && product.storeName" class="product-seller" @click.stop="navigateToStore">
        <span class="seller-icon">👤</span>
        <span class="seller-name">{{ product.storeName || '未知卖家' }}</span>
        <span class="store-link-arrow">→</span>
      </div>
      <div class="product-meta">
        <span class="product-condition">{{ formatCondition(product.productCondition) }}</span>
        <span v-if="product.publishTime" class="product-time">{{ formatTime(product.publishTime) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const props = defineProps({
  product: {
    type: Object,
    required: true
  },
  showMerchant: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['click']);
const router = useRouter();

const handleClick = () => {
  emit('click', props.product.id);
  router.push(`/product/${props.product.id}`);
};

const navigateToStore = () => {
  if (props.product.merchantId) {
    router.push(`/store/${props.product.merchantId}`);
  }
};

// 格式化商品新旧程度
const formatCondition = (condition) => {
  if (!condition) return '未知新旧';
  const conditionMap = {
    'NEW': '全新',
    'LIKE_NEW': '九成新',
    'GOOD': '八成新',
    'FAIR': '七成新',
    'POOR': '六成新及以下'
  };
  return conditionMap[condition] || condition;
};

// 格式化时间
const formatTime = (dateTime) => {
  if (!dateTime) return '';
  
  const now = new Date();
  const date = new Date(dateTime);
  const diff = now - date;
  
  // 小于1小时显示xx分钟前
  if (diff < 60 * 60 * 1000) {
    return Math.floor(diff / (60 * 1000)) + '分钟前';
  }
  // 小于24小时显示xx小时前
  else if (diff < 24 * 60 * 60 * 1000) {
    return Math.floor(diff / (60 * 60 * 1000)) + '小时前';
  }
  // 小于30天显示xx天前
  else if (diff < 30 * 24 * 60 * 60 * 1000) {
    return Math.floor(diff / (24 * 60 * 60 * 1000)) + '天前';
  }
  // 其他情况显示年-月-日
  else {
    return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
  }
};
</script>

<style scoped>
.product-card {
  background-color: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.product-image {
  height: 180px;
  overflow: hidden;
  position: relative;
}

.product-image::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.05), transparent);
}

.negotiable-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: #ff9800;
  color: white;
  padding: 3px 8px;
  font-size: 12px;
  border-radius: 4px;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  z-index: 2;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.product-info {
  padding: 16px;
}

.product-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-price-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.product-price {
  color: #e74c3c;
  font-weight: bold;
  font-size: 18px;
  margin-right: 8px;
}

.original-price {
  color: #999;
  font-size: 14px;
  text-decoration: line-through;
}

.product-seller {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  background-color: #f8f9fa;
}

.product-seller:hover {
  background-color: #e6ecff;
  transform: translateX(2px);
}

.seller-icon {
  margin-right: 4px;
}

.seller-name {
  font-size: 14px;
  color: #555;
  flex-grow: 1;
}

.store-link-arrow {
  color: #4a6ee0;
  font-weight: bold;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #777;
}

.product-condition {
  background-color: #ebf5ff;
  color: #4a6ee0;
  padding: 2px 6px;
  border-radius: 3px;
  font-weight: 500;
}

.product-time {
  color: #888;
}
</style> 