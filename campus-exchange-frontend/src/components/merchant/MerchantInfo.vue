<template>
  <div class="merchant-info-component">
    <div v-if="!merchant || !merchant.storeName" class="unknown-merchant">
      <div class="merchant-avatar">
        <span>?</span>
      </div>
      <div class="merchant-details">
        <h3>未知商家</h3>
        <p>暂无商家信息</p>
      </div>
    </div>
    <div v-else class="merchant-card" @click="navigateToStore">
      <div class="merchant-avatar">
        <img :src="processImageUrl(merchant.avatar) || '/images/default-avatar.png'" :alt="merchant.storeName">
      </div>
      <div class="merchant-details">
        <h3>{{ merchant.storeName }}</h3>
        <div class="merchant-meta" v-if="showMeta">
          <div class="meta-item" v-if="merchant.level">
            <span class="level-badge">{{ merchant.level }}</span>
          </div>
          <div class="meta-item" v-if="merchant.rating !== undefined">
            <span class="rating">{{ merchant.rating }}分</span>
          </div>
          <div class="meta-item" v-if="merchant.salesCount !== undefined">
            <span class="sales">销量: {{ merchant.salesCount }}</span>
          </div>
        </div>
        <div class="view-store">
          <span>进入店铺</span>
          <i class="el-icon-arrow-right"></i>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { fileApi } from '@/api/all';

// 引入URL处理函数
const processImageUrl = fileApi.processImageUrl;

const props = defineProps({
  merchant: {
    type: Object, 
    default: () => ({})
  },
  showMeta: {
    type: Boolean,
    default: true
  }
});

const router = useRouter();

const navigateToStore = () => {
  if (props.merchant && props.merchant.id) {
    router.push(`/store/${props.merchant.id}`);
  }
};
</script>

<style scoped>
.merchant-info-component {
  margin: 15px 0;
}

.merchant-card, .unknown-merchant {
  display: flex;
  align-items: center;
  padding: 15px;
  background-color: #f8fafc;
  border-radius: 10px;
  border-left: 4px solid #4a6ee0;
  transition: all 0.3s ease;
}

.merchant-card {
  cursor: pointer;
}

.merchant-card:hover {
  transform: translateX(5px);
  background-color: #edf0fa;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.merchant-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 15px;
  border: 2px solid #e8e8e8;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #aaa;
}

.merchant-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.merchant-details {
  flex: 1;
}

.merchant-details h3 {
  margin: 0 0 5px;
  font-size: 18px;
  color: #333;
}

.unknown-merchant .merchant-details p {
  margin: 0;
  font-size: 14px;
  color: #888;
}

.merchant-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 8px;
}

.meta-item {
  font-size: 14px;
}

.level-badge {
  background-color: #4a6ee0;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.rating {
  color: #ff9800;
}

.sales {
  color: #666;
}

.view-store {
  font-size: 14px;
  color: #4a6ee0;
  display: flex;
  align-items: center;
}

.view-store i {
  margin-left: 5px;
  transition: transform 0.2s ease;
}

.merchant-card:hover .view-store i {
  transform: translateX(3px);
}
</style> 