<template>
  <div class="merchant-management">
    <h1>商家管理</h1>
    
    <!-- 搜索区域 -->
    <div class="search-container">
      <el-input
        v-model="searchKeyword"
        placeholder="请输入店铺名称搜索"
        class="search-input"
        clearable
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </div>
    
    <!-- 数据加载中 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>正在加载商家数据...</span>
    </div>
    
    <!-- 数据为空 -->
    <el-empty v-else-if="merchants.length === 0" description="暂无商家数据"></el-empty>
    
    <!-- 商家列表 -->
    <el-table
      v-else
      :data="merchants"
      border
      style="width: 100%"
      class="merchant-table"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="storeName" label="店铺名称" width="180" />
      <el-table-column label="等级" width="180">
        <template #default="scope">
          <div class="merchant-level">
            <span>{{ getMerchantLevelName(scope.row.levelId) }}</span>
            <el-button
              type="primary"
              size="small"
              link
              @click="openChangeLevelDialog(scope.row)"
            >
              修改
            </el-button>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="totalSalesCount" label="销量" width="100" />
      <el-table-column prop="totalSalesAmount" label="销售额" width="120">
        <template #default="scope">
          {{ formatCurrency(scope.row.totalSalesAmount) }}
        </template>
      </el-table-column>
      <el-table-column label="好评率" width="120">
        <template #default="scope">
          {{ formatRate(scope.row.storePositiveRate) }}%
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="240">
        <template #default="scope">
          <el-button
            type="primary"
            size="small"
            @click="viewMerchantProducts(scope.row)"
          >
            查看商品
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="confirmTakeDownProducts(scope.row)"
          >
            下架所有商品
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalMerchants"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 修改商家等级对话框 -->
    <el-dialog
      v-model="changeLevelDialogVisible"
      title="修改商家等级"
      width="400px"
    >
      <div v-if="currentMerchant" class="change-level-form">
        <div class="merchant-info">
          <div class="info-row">
            <span class="label">商家ID:</span>
            <span>{{ currentMerchant.id }}</span>
          </div>
          <div class="info-row">
            <span class="label">店铺名称:</span>
            <span>{{ currentMerchant.storeName }}</span>
          </div>
          <div class="info-row">
            <span class="label">当前等级:</span>
            <span>{{ getMerchantLevelName(currentMerchant.levelId) }}</span>
          </div>
        </div>
        
        <div class="select-level">
          <span class="label">选择新等级:</span>
          <el-select v-model="selectedLevelId" placeholder="请选择商家等级" style="width: 100%">
            <el-option
              v-for="level in merchantLevels"
              :key="level.id"
              :label="`${level.levelName} (${formatRate(level.commissionRate)}%)`"
              :value="level.id"
            />
          </el-select>
        </div>
      </div>
      
      <template #footer>
        <span>
          <el-button @click="changeLevelDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="changingLevel" @click="handleChangeLevel">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 查看商家商品对话框 -->
    <el-dialog
      v-model="productDialogVisible"
      title="商家商品列表"
      width="900px"
    >
      <div class="product-filter">
        <el-select v-model="productStatus" placeholder="商品状态" @change="loadMerchantProducts">
          <el-option :value="null" label="全部状态" />
          <el-option value="ON_SALE" label="在售" />
          <el-option value="PENDING_APPROVAL" label="待审核" />
          <el-option value="REJECTED_RESUBMIT" label="审核不通过" />
          <el-option value="LOCKED" label="已锁定" />
          <el-option value="SOLD_OUT" label="已售罄" />
          <el-option value="REMOVED_BY_SELLER" label="商家下架" />
        </el-select>
      </div>
      
      <div v-if="loadingProducts" class="loading-products">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在加载商品数据...</span>
      </div>
      
      <el-empty v-else-if="products.length === 0" description="暂无商品数据"></el-empty>
      
      <el-table
        v-else
        :data="products"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品图片" width="100">
          <template #default="scope">
            <div class="product-image">
              <el-image
                :src="scope.row.mainImage || ''"
                fit="cover"
                :preview-src-list="scope.row.mainImage ? [scope.row.mainImage] : []"
              >
                <template #error>
                  <div class="image-error">暂无图片</div>
                </template>
              </el-image>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="currentPrice" label="价格" width="100">
          <template #default="scope">
            {{ formatCurrency(scope.row.currentPrice) }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag
              :type="getStatusTagType(scope.row.status)"
            >
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.publishTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
    
    <!-- 下架所有商品确认对话框 -->
    <el-dialog
      v-model="takeDownDialogVisible"
      title="下架商品确认"
      width="400px"
    >
      <div class="take-down-dialog">
        <el-icon class="warning-icon"><Warning /></el-icon>
        <p>确认要下架<strong>{{ currentMerchant?.storeName }}</strong>的所有在售商品吗？</p>
        <p class="warning-text">此操作将把该商家的所有在售商品状态设置为"已锁定"，无法撤销！</p>
      </div>
      
      <template #footer>
        <span>
          <el-button @click="takeDownDialogVisible = false">取消</el-button>
          <el-button type="danger" :loading="takingDown" @click="handleTakeDownProducts">
            确认下架
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Loading, Warning } from '@element-plus/icons-vue';
import { 
  getAllMerchants,
  getMerchantLevels,
  updateMerchantLevel,
  getMerchantProducts,
  takeDownAllProducts
} from '@/api/adminMerchant';

// 商家列表数据
const merchants = ref([]);
const loading = ref(false);
const totalMerchants = ref(0);
const searchKeyword = ref('');

// 商家等级数据
const merchantLevels = ref([]);

// 分页设置
const pagination = ref({
  currentPage: 1,
  pageSize: 10
});

// 当前选中的商家
const currentMerchant = ref(null);

// 变更等级对话框
const changeLevelDialogVisible = ref(false);
const selectedLevelId = ref(null);
const changingLevel = ref(false);

// 商品对话框
const productDialogVisible = ref(false);
const products = ref([]);
const loadingProducts = ref(false);
const productStatus = ref(null);

// 下架对话框
const takeDownDialogVisible = ref(false);
const takingDown = ref(false);

// 格式化货币
const formatCurrency = (value) => {
  if (value === null || value === undefined) return '0.00';
  return parseFloat(value).toFixed(2);
};

// 格式化百分比
const formatRate = (value) => {
  if (value === null || value === undefined) return '0.00';
  // 数据库中存储的是小数形式，如0.01表示1%，直接乘以100转为百分比显示
  return (value * 100).toFixed(2);
};

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleString();
};

// 加载商家列表
const loadMerchants = async () => {
  loading.value = true;
  try {
    const response = await getAllMerchants({
      page: pagination.value.currentPage,
      size: pagination.value.pageSize,
      keyword: searchKeyword.value
    });
    
    if (response.data && response.data.code === 200) {
      merchants.value = response.data.data.list || [];
      totalMerchants.value = response.data.data.total || 0;
    } else {
      ElMessage.error('获取商家列表失败');
    }
  } catch (error) {
    console.error('获取商家列表失败:', error);
    ElMessage.error('获取商家列表失败: ' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};

// 加载商家等级列表
const loadMerchantLevels = async () => {
  try {
    const response = await getMerchantLevels();
    if (response.data && response.data.code === 200) {
      merchantLevels.value = response.data.data || [];
    } else {
      console.error('获取商家等级列表失败:', response.data?.message);
    }
  } catch (error) {
    console.error('获取商家等级列表失败:', error);
  }
};

// 获取商家等级名称
const getMerchantLevelName = (levelId) => {
  if (!levelId) return '未知等级';
  const level = merchantLevels.value.find(level => level.id === levelId);
  return level ? level.levelName : '未知等级';
};

// 搜索处理
const handleSearch = () => {
  pagination.value.currentPage = 1;
  loadMerchants();
};

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.value.pageSize = size;
  loadMerchants();
};

// 页码变化
const handleCurrentChange = (page) => {
  pagination.value.currentPage = page;
  loadMerchants();
};

// 打开修改等级对话框
const openChangeLevelDialog = (merchant) => {
  currentMerchant.value = merchant;
  selectedLevelId.value = merchant.levelId;
  changeLevelDialogVisible.value = true;
};

// 修改商家等级
const handleChangeLevel = async () => {
  if (!currentMerchant.value || !selectedLevelId.value) {
    ElMessage.warning('请选择商家等级');
    return;
  }
  
  changingLevel.value = true;
  try {
    const response = await updateMerchantLevel(currentMerchant.value.id, selectedLevelId.value);
    if (response.data && response.data.code === 200) {
      ElMessage.success('商家等级修改成功');
      changeLevelDialogVisible.value = false;
      
      // 更新本地数据
      const index = merchants.value.findIndex(m => m.id === currentMerchant.value.id);
      if (index !== -1) {
        merchants.value[index].levelId = selectedLevelId.value;
      }
    } else {
      ElMessage.error(response.data?.message || '商家等级修改失败');
    }
  } catch (error) {
    console.error('商家等级修改失败:', error);
    ElMessage.error('商家等级修改失败: ' + (error.message || '未知错误'));
  } finally {
    changingLevel.value = false;
  }
};

// 查看商家商品
const viewMerchantProducts = (merchant) => {
  currentMerchant.value = merchant;
  productStatus.value = null;
  productDialogVisible.value = true;
  loadMerchantProducts();
};

// 加载商家商品列表
const loadMerchantProducts = async () => {
  if (!currentMerchant.value) return;
  
  loadingProducts.value = true;
  try {
    const response = await getMerchantProducts(currentMerchant.value.id, productStatus.value);
    if (response.data && response.data.code === 200) {
      products.value = response.data.data || [];
    } else {
      ElMessage.error('获取商家商品列表失败');
    }
  } catch (error) {
    console.error('获取商家商品列表失败:', error);
    ElMessage.error('获取商家商品列表失败: ' + (error.message || '未知错误'));
  } finally {
    loadingProducts.value = false;
  }
};

// 获取商品状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 'ON_SALE': return 'success';
    case 'PENDING_APPROVAL': return 'warning';
    case 'REJECTED_RESUBMIT': return 'danger';
    case 'LOCKED': return 'info';
    case 'SOLD_OUT': return '';
    case 'REMOVED_BY_SELLER': return 'info';
    default: return '';
  }
};

// 获取商品状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'ON_SALE': return '在售';
    case 'PENDING_APPROVAL': return '待审核';
    case 'REJECTED_RESUBMIT': return '审核不通过';
    case 'LOCKED': return '已锁定';
    case 'SOLD_OUT': return '已售罄';
    case 'REMOVED_BY_SELLER': return '商家下架';
    default: return status || '未知状态';
  }
};

// 确认下架所有商品
const confirmTakeDownProducts = (merchant) => {
  currentMerchant.value = merchant;
  takeDownDialogVisible.value = true;
};

// 下架商家所有商品
const handleTakeDownProducts = async () => {
  if (!currentMerchant.value) return;
  
  takingDown.value = true;
  try {
    const response = await takeDownAllProducts(currentMerchant.value.id);
    if (response.data && response.data.code === 200) {
      ElMessage.success(response.data.message || '已成功下架所有商品');
      takeDownDialogVisible.value = false;
      
      // 如果商品对话框是打开的，刷新商品列表
      if (productDialogVisible.value) {
        loadMerchantProducts();
      }
    } else {
      ElMessage.error(response.data?.message || '下架商品失败');
    }
  } catch (error) {
    console.error('下架商品失败:', error);
    ElMessage.error('下架商品失败: ' + (error.message || '未知错误'));
  } finally {
    takingDown.value = false;
  }
};

// 初始化加载
onMounted(async () => {
  await loadMerchantLevels();
  await loadMerchants();
});
</script>

<style scoped>
.merchant-management {
  padding: 20px;
}

.merchant-management h1 {
  margin-bottom: 24px;
}

.search-container {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
}

.search-input {
  width: 300px;
}

.loading-container, .loading-products {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #909399;
  gap: 12px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.merchant-level {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.merchant-info {
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  margin-bottom: 10px;
}

.info-row .label {
  font-weight: bold;
  width: 100px;
}

.select-level {
  display: flex;
  align-items: center;
  margin-top: 20px;
}

.select-level .label {
  font-weight: bold;
  width: 100px;
}

.product-filter {
  margin-bottom: 20px;
}

.product-image {
  width: 60px;
  height: 60px;
  overflow: hidden;
}

.product-image .el-image {
  width: 100%;
  height: 100%;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 12px;
}

.take-down-dialog {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.warning-icon {
  font-size: 48px;
  color: #e6a23c;
  margin-bottom: 16px;
}

.warning-text {
  color: #f56c6c;
  font-size: 14px;
  margin-top: 8px;
}

.merchant-table {
  margin-top: 20px;
}
</style> 