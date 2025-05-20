<script setup>
import { onMounted, ref } from 'vue';
import { productApi } from '@/api/all'; // 适配当前项目的API导入

// 轮播图数据 - 使用本地静态资源
const bannerList = ref([
  {
    id: 1,
    imgUrl: '/static/banner/banner1.png', // 静态资源路径
    title: '校园二手交易平台',
    description: '便宜又实用，助力校园生活'
  },
  {
    id: 2,
    imgUrl: '/static/banner/banner2.png',
    title: '全新商品推荐',
    description: '高品质二手商品推荐'
  },
  {
    id: 3,
    imgUrl: '/static/banner/banner3.png',
    title: '校园闲置换钱',
    description: '变废为宝，合理利用资源'
  }
])

// 这里可以替换为真实API调用
// const getBanner = async () => {
//   try {
//     const res = await productApi.getBanners()
//     bannerList.value = res.data.data || []
//   } catch (error) {
//     console.error('获取轮播图数据失败:', error)
//   }
// }

// onMounted(() => getBanner())
</script>

<template>
  <div class="home-banner">
    <el-carousel height="400px">
      <el-carousel-item v-for="item in bannerList" :key="item.id">
        <div class="banner-content">
          <img :src="item.imgUrl" :alt="item.title">
          <div class="banner-info">
            <h2>{{ item.title }}</h2>
            <p>{{ item.description }}</p>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<style scoped>
.home-banner {
  width: 100%;
  margin-bottom: 30px;
  border-radius: 12px;
  overflow: hidden;
}

.banner-content {
  position: relative;
  height: 100%;
}

.banner-content img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-info {
  position: absolute;
  bottom: 40px;
  left: 40px;
  background: rgba(0, 0, 0, 0.5);
  padding: 15px 25px;
  border-radius: 8px;
  color: white;
}

.banner-info h2 {
  font-size: 24px;
  margin-bottom: 8px;
}

.banner-info p {
  font-size: 16px;
  opacity: 0.9;
}
</style> 