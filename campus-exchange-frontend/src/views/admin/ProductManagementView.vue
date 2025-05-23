<template>
  <div class="product-management">
    <h1>商品管理</h1>
    
    <!-- 标签切换 -->
    <div class="tabs">
      <div
        @click="activeTab = 'pending'"
        :class="['tab-item', { active: activeTab === 'pending' }]"
      >
        待审核商品
      </div>
      <div
        @click="activeTab = 'all'"
        :class="['tab-item', { active: activeTab === 'all' }]"
      >
        所有商品
      </div>
    </div>
    
    <!-- 待审核商品列表 -->
    <div v-if="activeTab === 'pending'" class="product-table-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading">
        <el-icon class="is-loading"><Loading /></el-icon> 正在加载...
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="pendingProducts.length === 0" class="empty-state">
        <el-empty description="没有待审核的商品"></el-empty>
      </div>
      
      <!-- 商品列表 -->
      <el-table v-else :data="pendingProducts" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品图片" width="120">
          <template #default="scope">
            <el-image 
              :src="scope.row.mainImage || ''" 
              fit="cover"
              style="width: 100px; height: 100px"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" />
        <el-table-column label="价格" width="120">
          <template #default="scope">
            <div>
              <div class="price current-price">¥{{ scope.row.currentPrice }}</div>
              <div v-if="scope.row.originalPrice && scope.row.originalPrice !== scope.row.currentPrice" class="price original-price">¥{{ scope.row.originalPrice }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="商家" width="120">
          <template #default="scope">
            {{ scope.row.storeName || '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="productCondition" label="新旧程度" width="100">
          <template #default="scope">
            {{ getConditionText(scope.row.productCondition) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="handleApprove(scope.row)"
            >
              通过
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleReject(scope.row)"
            >
              拒绝
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="handleViewDetails(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 待审核商品分页 -->
      <div class="pagination-container" v-if="pendingProducts.length > 0">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="pendingTotal"
          :page-size="pendingPagination.pageSize"
          :current-page="pendingPagination.currentPage"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handlePendingSizeChange"
          @current-change="handlePendingCurrentChange"
        />
      </div>
    </div>
    
    <!-- 所有商品列表 -->
    <div v-else-if="activeTab === 'all'" class="product-table-container">
      <!-- 筛选表单 -->
      <div class="filter-container">
        <el-form :inline="true" class="filter-form">
          <el-form-item label="状态">
            <el-select 
              v-model="filters.status" 
              placeholder="选择状态" 
              clearable
              popper-class="status-select-dropdown"
              @change="handleStatusChange">
              <el-option label="待审核" value="PENDING_APPROVAL" />
              <el-option label="审核不通过" value="REJECTED_RESUBMIT" />
              <el-option label="在售" value="ON_SALE" />
              <el-option label="已锁定" value="LOCKED" />
              <el-option label="已售罄" value="SOLD_OUT" />
              <el-option label="商家下架" value="REMOVED_BY_SELLER" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="filters.keyword" placeholder="商品名称/描述" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="allProductsLoading" class="loading">
        <el-icon class="is-loading"><Loading /></el-icon> 正在加载...
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="allProducts.length === 0" class="empty-state">
        <el-empty description="没有符合条件的商品"></el-empty>
      </div>
      
      <!-- 商品列表 -->
      <el-table v-else :data="allProducts" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品图片" width="120">
          <template #default="scope">
            <el-image 
              :src="scope.row.mainImage || ''" 
              fit="cover"
              style="width: 100px; height: 100px"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" />
        <el-table-column label="价格" width="120">
          <template #default="scope">
            <div>
              <div class="price current-price">¥{{ scope.row.currentPrice }}</div>
              <div v-if="scope.row.originalPrice && scope.row.originalPrice !== scope.row.currentPrice" class="price original-price">¥{{ scope.row.originalPrice }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="商家" width="120">
          <template #default="scope">
            {{ scope.row.storeName || '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'PENDING_APPROVAL'" type="warning">待审核</el-tag>
            <el-tag v-else-if="scope.row.status === 'REJECTED_RESUBMIT'" type="danger">不通过</el-tag>
            <el-tag v-else-if="scope.row.status === 'ON_SALE'" type="success">在售</el-tag>
            <el-tag v-else-if="scope.row.status === 'LOCKED'" type="info">已锁定</el-tag>
            <el-tag v-else-if="scope.row.status === 'SOLD_OUT'" type="info">已售罄</el-tag>
            <el-tag v-else-if="scope.row.status === 'REMOVED_BY_SELLER'" type="warning">商家下架</el-tag>
            <span v-else>{{ scope.row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.publishTime || scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'PENDING_APPROVAL'"
              type="primary"
              size="small"
              @click="handleApprove(scope.row)"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.status === 'PENDING_APPROVAL'"
              type="danger"
              size="small"
              @click="handleReject(scope.row)"
            >
              拒绝
            </el-button>
            <el-button
              v-if="scope.row.status === 'ON_SALE'"
              type="warning"
              size="small"
              @click="handleOffShelf(scope.row)"
            >
              下架
            </el-button>
            <el-button
              v-if="['REMOVED_BY_SELLER', 'REJECTED_RESUBMIT', 'LOCKED'].includes(scope.row.status)"
              type="success"
              size="small"
              @click="handleApprove(scope.row)"
            >
              上架
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="handleViewDetails(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalProducts"
          :page-size="pagination.pageSize"
          :current-page="pagination.currentPage"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 操作确认对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="30%"
      center
    >
      <span>{{ dialogMessage }}</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAction">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 商品详情对话框 -->
    <el-dialog
      v-model="productDetailsVisible"
      title="商品详情"
      width="70%"
      top="5vh"
      :destroy-on-close="true"
    >
      <div v-if="selectedProduct" class="product-details">
        <!-- 商品基本信息 -->
        <div class="product-info-section">
          <div class="product-images">
            <el-carousel height="300px" indicator-position="outside" arrow="always">
              <el-carousel-item v-for="(image, index) in selectedProduct.images" :key="index">
                <el-image :src="image" fit="contain" class="carousel-image" />
              </el-carousel-item>
            </el-carousel>
          </div>
          
          <div class="product-base-info">
            <h2>{{ selectedProduct.name }}</h2>
            
            <div class="info-item">
              <span class="label">价格:</span>
              <span class="current-price">¥{{ selectedProduct.currentPrice }}</span>
              <span v-if="selectedProduct.originalPrice && selectedProduct.originalPrice !== selectedProduct.currentPrice" class="original-price">¥{{ selectedProduct.originalPrice }}</span>
            </div>
            
            <div class="info-item">
              <span class="label">商家:</span>
              <span>{{ selectedProduct.storeName || '未知' }}</span>
            </div>
            
            <div class="info-item">
              <span class="label">分类:</span>
              <span>{{ selectedProduct.categoryName || '未知' }}</span>
            </div>
            
            <div class="info-item">
              <span class="label">新旧程度:</span>
              <span>{{ getConditionText(selectedProduct.productCondition) }}</span>
            </div>
            
            <div class="info-item">
              <span class="label">库存:</span>
              <span>{{ selectedProduct.stock }}</span>
            </div>
            
            <div class="info-item">
              <span class="label">是否可议价:</span>
              <span>{{ selectedProduct.negotiable ? '是' : '否' }}</span>
            </div>
            
            <div class="info-item">
              <span class="label">尺寸信息:</span>
              <span>{{ selectedProduct.size || '无' }}</span>
            </div>
            
            <div class="info-item">
              <span class="label">状态:</span>
              <span>
                <el-tag v-if="selectedProduct.status === 'PENDING_APPROVAL'" type="warning">待审核</el-tag>
                <el-tag v-else-if="selectedProduct.status === 'REJECTED_RESUBMIT'" type="danger">不通过</el-tag>
                <el-tag v-else-if="selectedProduct.status === 'ON_SALE'" type="success">在售</el-tag>
                <el-tag v-else-if="selectedProduct.status === 'LOCKED'" type="info">已锁定</el-tag>
                <el-tag v-else-if="selectedProduct.status === 'SOLD_OUT'" type="info">已售罄</el-tag>
                <el-tag v-else-if="selectedProduct.status === 'REMOVED_BY_SELLER'" type="info">已下架</el-tag>
              </span>
            </div>
          </div>
        </div>
        
        <!-- 商品描述 -->
        <div class="product-description-section">
          <h3>商品描述</h3>
          <div class="description-content">{{ selectedProduct.description || '暂无描述' }}</div>
        </div>
        
        <!-- 使用说明 -->
        <div v-if="selectedProduct.usageInstructions" class="product-usage-section">
          <h3>使用说明</h3>
          <div class="usage-content">{{ selectedProduct.usageInstructions }}</div>
        </div>
      </div>
      
      <template #footer>
        <span v-if="selectedProduct && selectedProduct.status === 'PENDING_APPROVAL'" class="dialog-footer">
          <el-button @click="productDetailsVisible = false">关闭</el-button>
          <el-button type="primary" @click="handleApprove(selectedProduct)">通过审核</el-button>
          <el-button type="danger" @click="handleReject(selectedProduct)">拒绝商品</el-button>
        </span>
        <span v-else class="dialog-footer">
          <el-button @click="productDetailsVisible = false">关闭</el-button>
          <span v-if="selectedProduct && selectedProduct.status === 'ON_SALE'">
            <el-button type="warning" @click="handleOffShelf(selectedProduct)">下架商品</el-button>
          </span>
          <span v-if="selectedProduct && ['REMOVED_BY_SELLER', 'REJECTED_RESUBMIT', 'LOCKED'].includes(selectedProduct.status)">
            <el-button type="success" @click="handleApprove(selectedProduct)">上架商品</el-button>
          </span>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, Picture } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import { 
  getPendingProducts, 
  getAllProducts,
  approveProduct,
  rejectProduct,
  offShelfProduct,
  getProductDetail
} from '@/api/adminProduct'
import request from '@/utils/request'

// setup中添加路由相关代码
const route = useRoute()

// 状态变量
const activeTab = ref('pending')
const pendingProducts = ref([])
const allProducts = ref([])
const loading = ref(false)
const allProductsLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMessage = ref('')
const currentProduct = ref(null)
const currentAction = ref('')
const totalProducts = ref(0)
const pendingTotal = ref(0)
const productDetailsVisible = ref(false)
const selectedProduct = ref(null)

// 过滤条件
const filters = ref({
  status: '',
  keyword: ''
})

// 分页
const pagination = ref({
  currentPage: 1,
  pageSize: 10
})

// 待审核商品分页
const pendingPagination = ref({
  currentPage: 1,
  pageSize: 10
})

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  const date = new Date(dateTimeStr)
  return date.toLocaleString()
}

// 获取商品新旧程度文本
const getConditionText = (condition) => {
  switch (condition) {
    case 'NEW': return '全新';
    case 'LIKE_NEW': return '九成新';
    case 'GOOD': return '八成新';
    case 'FAIR': return '七成新';
    case 'POOR': return '六成新及以下';
    default: return condition || '未知';
  }
}

// 获取状态显示文本
const getStatusText = (status) => {
  switch (status) {
    case 'PENDING_APPROVAL': return '待审核';
    case 'REJECTED_RESUBMIT': return '审核不通过';
    case 'ON_SALE': return '在售';
    case 'LOCKED': return '已锁定';
    case 'SOLD_OUT': return '已售罄';
    case 'REMOVED_BY_SELLER': return '商家下架';
    default: return '全部';
  }
}

// 加载待审核商品
const loadPendingProducts = async () => {
  loading.value = true
  try {
    const response = await getPendingProducts({
      pageNum: pendingPagination.value.currentPage,
      pageSize: pendingPagination.value.pageSize
    })
    
    // 从响应中提取数据
    const data = response.data?.data || {}
    pendingProducts.value = data.records || []
    pendingTotal.value = data.total || 0
    
    console.log('加载待审核商品成功:', pendingProducts.value)
  } catch (error) {
    console.error('获取待审核商品失败:', error)
    ElMessage.error('获取待审核商品失败')
  } finally {
    loading.value = false
  }
}

// 加载所有商品
const loadAllProducts = async () => {
  allProductsLoading.value = true
  try {
    const params = {
      page: pagination.value.currentPage,
      size: pagination.value.pageSize,
      status: filters.value.status || undefined,
      keyword: filters.value.keyword || undefined
    }
    
    console.log('查询商品列表参数:', params)
    const response = await getAllProducts(params)
    
    // 确保从正确的路径提取商品列表数据
    const responseData = response.data?.data || {}
    
    // 如果返回的是Page对象，处理方式有所不同
    if (responseData.records) {
      allProducts.value = responseData.records || []
      totalProducts.value = responseData.total || 0
    } else {
      // 兼容返回Map结构
      allProducts.value = responseData.list || []
      totalProducts.value = responseData.total || 0
    }
    
    console.log('加载所有商品成功:', allProducts.value)
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
  } finally {
    allProductsLoading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.value.currentPage = 1
  console.log('执行搜索，当前筛选条件:', filters.value)
  loadAllProducts()
}

// 重置过滤条件
const resetFilters = () => {
  filters.value = {
    status: '',
    keyword: ''
  }
  console.log('重置筛选条件:', filters.value)
  handleSearch()
}

// 分页大小改变
const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  loadAllProducts()
}

// 页码改变
const handleCurrentChange = (page) => {
  pagination.value.currentPage = page
  loadAllProducts()
}

// 待审核商品分页大小改变
const handlePendingSizeChange = (size) => {
  pendingPagination.value.pageSize = size
  loadPendingProducts()
}

// 待审核商品页码改变
const handlePendingCurrentChange = (page) => {
  pendingPagination.value.currentPage = page
  loadPendingProducts()
}

// 处理审核通过
const handleApprove = (product) => {
  currentProduct.value = product
  currentAction.value = 'approve'
  dialogTitle.value = '审核通过'
  dialogMessage.value = `确认通过商品 "${product.name}" 的审核吗？`
  dialogVisible.value = true
}

// 处理拒绝审核
const handleReject = (product) => {
  currentProduct.value = product
  currentAction.value = 'reject'
  dialogTitle.value = '拒绝商品'
  dialogMessage.value = `确认拒绝商品 "${product.name}" 的审核申请吗？`
  dialogVisible.value = true
}

// 处理下架商品
const handleOffShelf = (product) => {
  currentProduct.value = product
  currentAction.value = 'offShelf'
  dialogTitle.value = '下架商品'
  dialogMessage.value = `确认下架商品 "${product.name}" 吗？`
  dialogVisible.value = true
}

// 查看商品详情
const handleViewDetails = async (product) => {
  selectedProduct.value = product
  
  try {
    // 获取完整的商品详情
    const response = await getProductDetail(product.id)
    if (response.data?.data) {
      selectedProduct.value = response.data.data
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
  }
  
  productDetailsVisible.value = true
}

// 确认操作
const confirmAction = async () => {
  if (!currentProduct.value || !currentAction.value) return
  
  try {
    if (currentAction.value === 'approve') {
      await approveProduct(currentProduct.value.id)
      ElMessage.success('已通过商品审核')
    } else if (currentAction.value === 'reject') {
      await rejectProduct(currentProduct.value.id)
      ElMessage.success('已拒绝商品审核')
    } else if (currentAction.value === 'offShelf') {
      await offShelfProduct(currentProduct.value.id)
      ElMessage.success('商品已下架')
    }
    
    // 重新加载相应的商品列表
    if (activeTab.value === 'pending') {
      loadPendingProducts()
    } else {
      // 维持当前筛选条件重新加载数据
      loadAllProducts()
    }
    
    // 关闭商品详情弹窗（如果打开的话）
    if (productDetailsVisible.value) {
      productDetailsVisible.value = false
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败: ' + (error.message || '未知错误'))
  } finally {
    dialogVisible.value = false
  }
}

// 状态选择变化处理
const handleStatusChange = (status) => {
  console.log('状态选择变为:', status, getStatusText(status))
}

// 监听标签切换
watch(activeTab, (newTab) => {
  if (newTab === 'all') {
    loadAllProducts()
  } else if (newTab === 'pending') {
    loadPendingProducts()
  }
})

// 监视筛选条件变化
watch(filters, (newFilters) => {
  console.log('筛选条件变化:', newFilters)
}, { deep: true })

// 在onMounted中处理路由参数
onMounted(() => {
  // 处理URL参数，如果有status=pending参数，则切换到待审核标签
  if (route.query.status === 'pending') {
    activeTab.value = 'pending'
  }
  
  loadPendingProducts()
})
</script>

<style scoped>
.product-management {
  padding: 20px;
}

.product-management h1 {
  margin-bottom: 24px;
  font-size: 24px;
}

.tabs {
  display: flex;
  margin-bottom: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.tab-item {
  padding: 10px 20px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
}

.tab-item.active {
  color: #1890ff;
  border-bottom: 2px solid #1890ff;
}

.tab-item:hover:not(.active) {
  color: #40a9ff;
}

.product-table-container {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
  color: #909399;
}

.empty-state {
  padding: 40px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.filter-container {
  margin-bottom: 20px;
}

.el-select {
  width: 150px;
}

.status-select-dropdown {
  min-width: 150px !important;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 商品价格样式 */
.price {
  display: inline-block;
  margin-right: 8px;
}

.current-price {
  color: #f56c6c;
  font-weight: bold;
}

.original-price {
  color: #909399;
  text-decoration: line-through;
  font-size: 12px;
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
}

/* 商品详情样式 */
.product-info-section {
  display: flex;
  margin-bottom: 24px;
  gap: 24px;
}

.product-images {
  width: 400px;
}

.carousel-image {
  width: 100%;
  height: 300px;
  object-fit: contain;
}

.product-base-info {
  flex: 1;
}

.product-base-info h2 {
  margin-top: 0;
  margin-bottom: 16px;
}

.info-item {
  margin-bottom: 12px;
  line-height: 24px;
}

.info-item .label {
  font-weight: bold;
  margin-right: 8px;
  color: #606266;
}

.product-description-section,
.product-usage-section {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.product-description-section h3,
.product-usage-section h3 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 18px;
  color: #303133;
}

.description-content,
.usage-content {
  white-space: pre-line;
  line-height: 1.6;
  color: #606266;
}
</style>

<style>
/* 覆盖Element Plus默认样式 - 这里必须是非scoped的样式 */
.el-select__input {
  margin-left: 0 !important;
}

.el-select__tags {
  max-width: 120px;
}
</style> 