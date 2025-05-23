<template>
  <div class="admin-dashboard">
    <div class="welcome-banner">
      <h1>欢迎使用校园交易系统管理后台</h1>
      <p>您可以在这里管理用户、商家、商品和系统设置</p>
    </div>
    
    <div class="stats-overview">
      <div class="stat-card">
        <div class="stat-title">待审核用户</div>
        <div class="stat-value">{{ stats.pendingUsers }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">待审核商品</div>
        <div class="stat-value">{{ stats.pendingProducts }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">总用户数</div>
        <div class="stat-value">{{ stats.totalUsers }}</div>
        <div v-if="loading.users" class="stat-loading">加载中...</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">总商品数</div>
        <div class="stat-value">{{ stats.totalProducts }}</div>
        <div v-if="loading.products" class="stat-loading">加载中...</div>
      </div>
    </div>
    
    <div class="quick-actions">
      <h2>快捷操作</h2>
      <div class="action-links">
        <router-link to="/admin/users?status=pending" class="action-btn">
          审核用户注册
        </router-link>
        <router-link to="/admin/products?status=pending" class="action-btn">
          审核商品发布
        </router-link>
        <router-link to="/admin/merchants" class="action-btn">
          管理商家等级
        </router-link>
        <router-link to="/admin/settings" class="action-btn">
          平台参数设置
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { getPendingUsers, getUserStats } from '@/api/adminUser';
import { getPendingProducts, getProductStats } from '@/api/adminProduct';

export default {
  setup() {
    // 状态数据
    const stats = ref({
      pendingUsers: 0,
      pendingProducts: 0,
      totalUsers: 0,
      totalProducts: 0
    });
    
    // 加载状态
    const loading = ref({
      users: false,
      products: false
    });
    
    // 加载待审核用户数据
    const loadPendingUsers = async () => {
      try {
        const response = await getPendingUsers();
        // 正确处理响应数据，确保显示数字0而不是空白
        if (response) {
          const pendingUsersData = Array.isArray(response.data) ? 
            response.data : (response.data?.data || []);
          
          stats.value.pendingUsers = pendingUsersData.length;
          console.log('待审核用户数量:', stats.value.pendingUsers);
        } else {
          stats.value.pendingUsers = 0;
        }
      } catch (error) {
        console.error('获取待审核用户数据失败:', error);
        stats.value.pendingUsers = 0; // 确保出错时也设置为0
      }
    };

    // 加载待审核商品数据
    const loadPendingProducts = async () => {
      try {
        // 调用API获取待审核商品
        const response = await getPendingProducts({
          pageNum: 1,
          pageSize: 1  // 只需要知道总数，所以只请求一条记录即可
        });
        
        // 正确处理响应数据
        if (response && response.data && response.data.data) {
          const pendingProductsData = response.data.data;
          // 从返回的分页数据中获取total字段作为待审核商品总数
          stats.value.pendingProducts = pendingProductsData.total || 0;
          console.log('待审核商品数量:', stats.value.pendingProducts);
        } else {
          stats.value.pendingProducts = 0;
        }
      } catch (error) {
        console.error('获取待审核商品数据失败:', error);
        stats.value.pendingProducts = 0;
      }
    };
    
    // 加载总用户数
    const loadTotalUsers = async () => {
      loading.value.users = true;
      try {
        const response = await getUserStats();
        if (response && response.data && response.data.code === 200) {
          stats.value.totalUsers = response.data.data.totalUsers || 0;
          console.log('总用户数:', stats.value.totalUsers);
        } else {
          console.warn('获取总用户数失败:', response);
          stats.value.totalUsers = 0;
        }
      } catch (error) {
        console.error('获取总用户数失败:', error);
        stats.value.totalUsers = 0;
      } finally {
        loading.value.users = false;
      }
    };
    
    // 加载总商品数
    const loadTotalProducts = async () => {
      loading.value.products = true;
      try {
        const response = await getProductStats();
        if (response && response.data && response.data.code === 200) {
          stats.value.totalProducts = response.data.data.totalProducts || 0;
          console.log('总商品数:', stats.value.totalProducts);
        } else {
          console.warn('获取总商品数失败:', response);
          stats.value.totalProducts = 0;
        }
      } catch (error) {
        console.error('获取总商品数失败:', error);
        stats.value.totalProducts = 0;
      } finally {
        loading.value.products = false;
      }
    };
    
    onMounted(() => {
      // 加载实际数据
      loadPendingUsers();
      loadPendingProducts();
      loadTotalUsers();
      loadTotalProducts();
      console.log('管理员仪表盘已加载');
    });
    
    return {
      stats,
      loading
    };
  }
}
</script>

<style scoped>
.admin-dashboard {
  padding: 0 20px;
}

.welcome-banner {
  background: linear-gradient(120deg, #1890ff, #6dd5ed);
  border-radius: 8px;
  padding: 30px;
  color: white;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.welcome-banner h1 {
  margin: 0;
  margin-bottom: 10px;
  font-size: 24px;
}

.welcome-banner p {
  margin: 0;
  opacity: 0.9;
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  text-align: center;
  transition: all 0.3s;
  position: relative;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #1890ff;
}

.stat-loading {
  position: absolute;
  bottom: 5px;
  left: 0;
  right: 0;
  font-size: 12px;
  color: #999;
}

.quick-actions {
  background-color: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.quick-actions h2 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 18px;
  color: #333;
}

.action-links {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.action-btn {
  display: block;
  background-color: #f0f5ff;
  color: #1890ff;
  padding: 14px 20px;
  text-decoration: none;
  border-radius: 6px;
  text-align: center;
  transition: all 0.3s;
  border: 1px solid #d6e4ff;
}

.action-btn:hover {
  background-color: #e6f7ff;
  color: #096dd9;
}
</style> 