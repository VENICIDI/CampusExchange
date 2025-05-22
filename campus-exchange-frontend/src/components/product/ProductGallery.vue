<template>
  <div class="product-gallery-container">
    <!-- 主图区域 -->
    <div class="product-image">
      <img 
        :src="processedMainImageUrl" 
        :alt="altText"
        @error="handleImageError"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { fileApi } from '@/api/all';

// 引入URL处理函数
const processImageUrl = fileApi.processProductImageUrl;
const imageLoadError = ref(false);

const props = defineProps({
  images: {
    type: Array,
    default: () => []
  },
  coverImage: {
    type: String,
    default: ''
  },
  altText: {
    type: String,
    default: '商品图片'
  },
  showAllImages: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['select-image']);

// 处理全部图片URLs
const processedImages = computed(() => {
  try {
    console.log('处理图库中的所有图片, 图片数量:', props.images.length);
    
    return props.images.map((url, index) => {
      try {
        console.log(`预处理图片${index+1}:`, url);
        const processedUrl = processImageUrl(url);
        console.log(`处理后的图片${index+1}:`, processedUrl);
        return {
          originalUrl: url,
          url: processedUrl,
          index: index
        };
      } catch (error) {
        console.error(`处理图片${index+1}出错:`, error);
        return {
          originalUrl: url,
          url: '/images/no-image-small.png',
          index: index,
          error: true
        };
      }
    });
  } catch (error) {
    console.error('处理图片数组出错:', error);
    return [];
  }
});

// 计算主图原始URL
const mainImageUrl = computed(() => {
  if (props.images && props.images.length > 0) {
    return props.images[0];
  }
  return props.coverImage || 'https://via.placeholder.com/500x400';
});

// 处理后的主图URL
const processedMainImageUrl = computed(() => {
  try {
    console.log('处理主图URL:', mainImageUrl.value);
    console.log('主图URL类型:', typeof mainImageUrl.value);
    
    if (!mainImageUrl.value) {
      console.log('主图URL为空，使用默认图片');
      return '/images/no-image.png';
    }
    
    const processedUrl = processImageUrl(mainImageUrl.value);
    console.log('处理后的主图URL:', processedUrl);
    return processedUrl;
  } catch (error) {
    console.error('处理主图URL出错:', error);
    return '/images/no-image.png';
  }
});

// 处理图片加载错误
const handleImageError = (event) => {
  console.error('主图加载失败:', event);
  imageLoadError.value = true;
  event.target.src = '/images/no-image.png';
  
  // 如果本地图片也加载失败，使用内联的base64图片（1x1透明像素）
  event.target.onerror = function() {
    this.src = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=';
    this.onerror = null; // 防止循环错误
    this.style.backgroundColor = '#f0f0f0';
    this.style.border = '1px dashed #ccc';
  };
};

// 处理图库图片加载错误
const handleGalleryImageError = (event, index) => {
  console.error(`图库图片${index+1}加载失败:`, event);
  event.target.src = `/images/no-image-small.png`;
  
  // 如果本地图片也加载失败，使用内联的base64图片
  event.target.onerror = function() {
    this.src = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=';
    this.onerror = null; // 防止循环错误
    this.style.backgroundColor = '#f0f0f0';
    this.style.border = '1px dashed #ccc';
  };
};

// 添加一个方法处理图库中的每个图片URL
const processGalleryImageUrl = (url, index) => {
  try {
    console.log(`处理图库图片${index}:`, url);
    const processedUrl = processImageUrl(url);
    console.log(`图库图片${index}处理结果:`, processedUrl);
    return processedUrl;
  } catch (error) {
    console.error(`处理图库图片${index}出错:`, error);
    return 'https://via.placeholder.com/200x150';
  }
};
</script>

<style scoped>
.product-gallery-container {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.product-image {
  height: 450px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 6px 20px rgba(0,0,0,0.1);
  background-color: #fff;
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.product-image:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0,0,0,0.15);
}

.product-image img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  transition: transform 0.4s ease;
}

.product-image:hover img {
  transform: scale(1.05);
}
</style> 