<template>
  <div class="product-details-tabs">
    <div class="tab-content">
      <h2>商品详情</h2>
      <div class="product-description">
        <p v-if="description">{{ description }}</p>
        <p v-else>暂无详细描述</p>
      </div>
      
      <slot name="product-images"></slot>
      
      <div v-if="usageInstructions" class="usage-instructions">
        <h3>使用说明</h3>
        <p>{{ usageInstructions }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  description: {
    type: String,
    default: ''
  },
  usageInstructions: {
    type: String,
    default: ''
  }
});
</script>

<style scoped>
.product-details-tabs {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  padding: 30px;
}

.tab-content h2 {
  font-size: 1.5rem;
  margin-bottom: 20px;
  color: #333;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
  position: relative;
}

.tab-content h2::after {
  content: "";
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, #4a6ee0, #6a8fff);
  border-radius: 3px;
}

.tab-content h3 {
  font-size: 1.2rem;
  margin: 25px 0 15px;
  color: #333;
  padding-left: 12px;
  border-left: 3px solid #4a6ee0;
}

.product-description {
  margin-bottom: 30px;
  line-height: 1.8;
  color: #555;
}

.usage-instructions {
  margin-top: 30px;
  line-height: 1.8;
  color: #555;
  padding: 20px;
  background-color: #f9fafc;
  border-radius: 8px;
  border-left: 4px solid #4a6ee0;
}
</style> 