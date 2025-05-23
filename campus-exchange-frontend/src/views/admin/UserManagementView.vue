<template>
  <div class="user-management">
    <h1>用户管理</h1>
    
    <!-- 标签切换 -->
    <div class="tabs">
      <div
        @click="activeTab = 'pending'"
        :class="['tab-item', { active: activeTab === 'pending' }]"
      >
        待审核用户
      </div>
      <div
        @click="activeTab = 'all'"
        :class="['tab-item', { active: activeTab === 'all' }]"
      >
        所有用户
      </div>
    </div>
    
    <!-- 待审核用户列表 -->
    <div v-if="activeTab === 'pending'" class="user-table-container">
      <div v-if="loading" class="loading">
        <el-icon class="is-loading"><Loading /></el-icon> 正在加载...
      </div>
      
      <div v-else-if="pendingUsers.length === 0" class="empty-state">
        <el-empty description="没有待审核的用户"></el-empty>
      </div>
      
      <el-table v-else :data="pendingUsers" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.role === 'MERCHANT'" type="warning">商家</el-tag>
            <el-tag v-else-if="scope.row.role === 'USER'" type="success">普通用户</el-tag>
            <el-tag v-else-if="scope.row.role === 'ADMIN'" type="danger">管理员</el-tag>
            <span v-else>{{ scope.row.role }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180">
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
              v-if="scope.row.role === 'MERCHANT'"
              type="warning"
              size="small"
              @click="handleViewMerchantDetails(scope.row)"
            >
              查看资质
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 所有用户列表 -->
    <div v-else-if="activeTab === 'all'" class="user-table-container">
      <div class="filter-container">
        <div class="filter-item">
          <span class="filter-label">角色</span>
          <el-select 
            v-model="filters.role" 
            placeholder="选择角色" 
            clearable
            @change="handleSearch">
            <el-option label="普通用户" value="USER" />
            <el-option label="商家" value="MERCHANT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </div>
        
        <div class="filter-item">
          <span class="filter-label">状态</span>
          <el-select 
            v-model="filters.status" 
            placeholder="选择状态" 
            clearable
            @change="handleSearch">
            <el-option label="正常" value="NORMAL" />
            <el-option label="待审核" value="PENDING" />
            <el-option label="已禁用" value="DISABLED" />
          </el-select>
        </div>
        
        <div class="filter-item keyword">
          <span class="filter-label">搜索</span>
          <el-input v-model="filters.keyword" placeholder="用户名/邮箱/手机号" clearable />
        </div>
        
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>
      
      <div v-if="allUsersLoading" class="loading">
        <el-icon class="is-loading"><Loading /></el-icon> 正在加载...
      </div>
      
      <div v-else-if="allUsers.length === 0" class="empty-state">
        <el-empty description="没有符合条件的用户"></el-empty>
      </div>
      
      <el-table v-else :data="allUsers" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.role === 'MERCHANT'" type="warning">商家</el-tag>
            <el-tag v-else-if="scope.row.role === 'USER'" type="success">普通用户</el-tag>
            <el-tag v-else-if="scope.row.role === 'ADMIN'" type="danger">管理员</el-tag>
            <span v-else>{{ scope.row.role }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'NORMAL'" type="success">正常</el-tag>
            <el-tag v-else-if="scope.row.status === 'PENDING'" type="warning">待审核</el-tag>
            <el-tag v-else-if="scope.row.status === 'DISABLED'" type="danger">已禁用</el-tag>
            <span v-else>{{ getUserStatusText(scope.row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'PENDING'"
              type="primary"
              size="small"
              @click="handleApprove(scope.row)"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.status === 'NORMAL' && scope.row.role !== 'ADMIN'"
              type="danger"
              size="small"
              @click="handleDisable(scope.row)"
            >
              禁用
            </el-button>
            <el-button
              v-if="scope.row.status === 'DISABLED'"
              type="success"
              size="small"
              @click="handleEnable(scope.row)"
            >
              启用
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="handleViewDetails(scope.row)"
            >
              详情
            </el-button>
            <el-button
              v-if="scope.row.role === 'MERCHANT'"
              type="warning"
              size="small"
              @click="handleViewMerchantDetails(scope.row)"
            >
              查看资质
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalUsers"
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
    
    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="userDetailsVisible"
      :title="isEditMode ? '编辑用户信息' : '用户详情'"
      width="50%"
    >
      <div v-if="selectedUser" class="user-details">
        <div class="user-info-item">
          <span class="label">ID:</span>
          <span>{{ selectedUser.id }}</span>
        </div>
        <div class="user-info-item">
          <span class="label">用户名:</span>
          <span>{{ selectedUser.username }}</span>
        </div>
        <div class="user-info-item">
          <span class="label">真实姓名:</span>
          <span v-if="!isEditMode">{{ selectedUser.realName || '-' }}</span>
          <el-input v-else v-model="editingUser.realName" placeholder="请输入真实姓名" />
        </div>
        <div class="user-info-item">
          <span class="label">手机号:</span>
          <span v-if="!isEditMode">{{ selectedUser.phone || '-' }}</span>
          <el-input v-else v-model="editingUser.phone" placeholder="请输入手机号" />
        </div>
        <div class="user-info-item">
          <span class="label">邮箱:</span>
          <span v-if="!isEditMode">{{ selectedUser.email || '-' }}</span>
          <el-input v-else v-model="editingUser.email" placeholder="请输入邮箱" />
        </div>
        <div class="user-info-item">
          <span class="label">城市:</span>
          <span v-if="!isEditMode">{{ selectedUser.city || '-' }}</span>
          <el-input v-else v-model="editingUser.city" placeholder="请输入城市" />
        </div>
        <div class="user-info-item">
          <span class="label">性别:</span>
          <span v-if="!isEditMode">
            {{ 
              selectedUser.gender === 'MALE' ? '男' : 
              selectedUser.gender === 'FEMALE' ? '女' : '未知'
            }}
          </span>
          <el-select v-else v-model="editingUser.gender" placeholder="请选择性别">
            <el-option label="男" value="MALE" />
            <el-option label="女" value="FEMALE" />
            <el-option label="未知" value="" />
          </el-select>
        </div>
        <div class="user-info-item">
          <span class="label">微信:</span>
          <span v-if="!isEditMode">{{ selectedUser.wechat || '-' }}</span>
          <el-input v-else v-model="editingUser.wechat" placeholder="请输入微信号" />
        </div>
        <div class="user-info-item">
          <span class="label">个人介绍:</span>
          <span v-if="!isEditMode">{{ selectedUser.personalIntro || '-' }}</span>
          <el-input v-else v-model="editingUser.personalIntro" type="textarea" placeholder="请输入个人介绍" />
        </div>
        <div class="user-info-item">
          <span class="label">默认收货地址:</span>
          <span v-if="!isEditMode">{{ selectedUser.defaultAddress || '-' }}</span>
          <el-input v-else v-model="editingUser.defaultAddress" type="textarea" placeholder="请输入默认收货地址" />
        </div>
        <div class="user-info-item">
          <span class="label">注册时间:</span>
          <span>{{ formatDateTime(selectedUser.createTime) }}</span>
        </div>
        <div class="user-info-item">
          <span class="label">最近更新:</span>
          <span>{{ formatDateTime(selectedUser.updateTime) }}</span>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <div v-if="!isEditMode">
            <el-button @click="userDetailsVisible = false">关闭</el-button>
            <el-button type="primary" @click="switchToEditMode">编辑</el-button>
          </div>
          <div v-else>
            <el-button @click="cancelEdit">取消</el-button>
            <el-button type="primary" @click="saveUserInfo" :loading="isSaving">保存</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
    
    <!-- 商家详情对话框 -->
    <el-dialog
      v-model="merchantDetailsVisible"
      title="商家资质详情"
      width="80%"
      top="5vh"
      :destroy-on-close="true"
      class="merchant-detail-dialog"
    >
      <div v-if="loadingMerchant" class="loading">
        <el-icon class="is-loading"><Loading /></el-icon> 正在加载商家资质信息...
      </div>
      <div v-else-if="merchantDetails" class="merchant-details">
        <h3>基本信息</h3>
        <div class="info-section">
          <div class="info-row">
            <span class="info-label">商家ID:</span>
            <span>{{ merchantDetails.id }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">店铺名称:</span>
            <span>{{ merchantDetails.storeName }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">店铺简介:</span>
            <span>{{ merchantDetails.description || '-' }}</span>
          </div>
        </div>
        
        <el-divider />
        
        <h3>资质证明</h3>
        <div class="document-section">
          <div class="document-preview">
            <h4>营业执照</h4>
            <div class="image-container">
              <el-image 
                :src="merchantDetails.businessLicense" 
                :preview-src-list="[merchantDetails.businessLicense]"
                fit="contain"
                :z-index="9999"
                class="document-image"
                @click="handleImageClick(merchantDetails.businessLicense)"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                    <div>无法加载图片</div>
                  </div>
                </template>
              </el-image>
            </div>
            <div class="image-actions">
              <el-button type="primary" size="small" @click="handleImageClick(merchantDetails.businessLicense)">
                <el-icon><ZoomIn /></el-icon> 查看大图
              </el-button>
            </div>
          </div>
          
          <div class="document-preview">
            <h4>身份证照片</h4>
            <div class="image-container">
              <el-image 
                :src="merchantDetails.idCard" 
                :preview-src-list="[merchantDetails.idCard]"
                fit="contain"
                :z-index="9999"
                class="document-image"
                @click="handleImageClick(merchantDetails.idCard)"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                    <div>无法加载图片</div>
                  </div>
                </template>
              </el-image>
            </div>
            <div class="image-actions">
              <el-button type="primary" size="small" @click="handleImageClick(merchantDetails.idCard)">
                <el-icon><ZoomIn /></el-icon> 查看大图
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="!loadingMerchant" class="empty-state">
        <el-empty description="未找到该用户的商家资质信息"></el-empty>
      </div>
      
      <template #footer>
        <span v-if="currentUser && currentUser.status === 'PENDING'" class="dialog-footer">
          <el-button @click="merchantDetailsVisible = false">取消</el-button>
          <el-button type="primary" @click="handleApprove(currentUser)">通过审核</el-button>
          <el-button type="danger" @click="handleReject(currentUser)">拒绝申请</el-button>
        </span>
        <span v-else class="dialog-footer">
          <el-button @click="merchantDetailsVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 添加图片预览弹窗 -->
    <el-dialog
      v-model="imagePreviewVisible"
      :title="imagePreviewTitle"
      append-to-body
      width="90%"
      top="2vh"
      class="image-preview-dialog"
      :destroy-on-close="true"
    >
      <div class="image-preview-container">
        <el-image
          :src="previewImageUrl"
          fit="contain"
          class="preview-image"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, Picture, ZoomIn } from '@element-plus/icons-vue'
import { 
  getPendingUsers, 
  approveUser, 
  rejectUser, 
  getAllUsers, 
  updateUserStatus,
  getMerchantDetails,
  updateUserInfo
} from '@/api/adminUser'

// 状态变量
const activeTab = ref('pending')
const pendingUsers = ref([])
const allUsers = ref([])
const loading = ref(false)
const allUsersLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMessage = ref('')
const currentUser = ref(null)
const currentAction = ref('')
const totalUsers = ref(0)
const userDetailsVisible = ref(false)
const selectedUser = ref(null)

// 商家详情相关
const merchantDetailsVisible = ref(false)
const merchantDetails = ref(null)
const loadingMerchant = ref(false)

// 过滤条件
const filters = ref({
  role: '',
  status: '',
  keyword: ''
})

// 分页
const pagination = ref({
  currentPage: 1,
  pageSize: 10
})

// 图片预览相关
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')
const imagePreviewTitle = ref('')

// 编辑相关
const isEditMode = ref(false)
const editingUser = ref({})
const isSaving = ref(false)

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  const date = new Date(dateTimeStr)
  return date.toLocaleString()
}

// 加载待审核用户
const loadPendingUsers = async () => {
  loading.value = true
  try {
    const response = await getPendingUsers()
    pendingUsers.value = Array.isArray(response.data) ? response.data : (response.data?.data || [])
    console.log('加载待审核用户成功:', pendingUsers.value)
  } catch (error) {
    console.error('获取待审核用户失败:', error)
    ElMessage.error('获取待审核用户失败')
  } finally {
    loading.value = false
  }
}

// 加载所有用户
const loadAllUsers = async () => {
  allUsersLoading.value = true
  try {
    const params = {
      page: pagination.value.currentPage,
      size: pagination.value.pageSize,
      role: filters.value.role || undefined,
      status: filters.value.status || undefined,
      keyword: filters.value.keyword || undefined
    }
    
    const response = await getAllUsers(params)
    // 确保从正确的路径提取用户列表数据
    const responseData = response.data?.data || {}
    allUsers.value = responseData.list || []
    totalUsers.value = responseData.total || 0
    console.log('加载所有用户成功:', allUsers.value)
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    allUsersLoading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.value.currentPage = 1
  loadAllUsers()
}

// 重置过滤条件
const resetFilters = () => {
  filters.value = {
    role: '',
    status: '',
    keyword: ''
  }
  handleSearch()
}

// 分页大小改变
const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  loadAllUsers()
}

// 页码改变
const handleCurrentChange = (page) => {
  pagination.value.currentPage = page
  loadAllUsers()
}

// 处理通过审核
const handleApprove = (user) => {
  currentUser.value = user
  currentAction.value = 'approve'
  dialogTitle.value = '审核通过'
  dialogMessage.value = `确认通过用户 ${user.username} 的注册申请吗？`
  dialogVisible.value = true
}

// 处理拒绝审核
const handleReject = (user) => {
  currentUser.value = user
  currentAction.value = 'reject'
  dialogTitle.value = '拒绝注册'
  dialogMessage.value = `确认拒绝用户 ${user.username} 的注册申请吗？`
  dialogVisible.value = true
}

// 处理禁用用户
const handleDisable = (user) => {
  currentUser.value = user
  currentAction.value = 'disable'
  dialogTitle.value = '禁用用户'
  dialogMessage.value = `确认禁用用户 ${user.username} 吗？`
  dialogVisible.value = true
}

// 处理启用用户
const handleEnable = (user) => {
  currentUser.value = user
  currentAction.value = 'enable'
  dialogTitle.value = '启用用户'
  dialogMessage.value = `确认启用用户 ${user.username} 吗？`
  dialogVisible.value = true
}

// 查看用户详情
const handleViewDetails = (user) => {
  selectedUser.value = user
  userDetailsVisible.value = true
}

// 查看商家详情
const handleViewMerchantDetails = async (user) => {
  currentUser.value = user
  merchantDetailsVisible.value = true
  loadingMerchant.value = true
  merchantDetails.value = null
  
  try {
    const response = await getMerchantDetails(user.id)
    const merchantData = response.data?.data || null
    
    if (merchantData) {
      // 确保使用真实数据
      merchantDetails.value = merchantData
      console.log('获取商家详情成功:', merchantDetails.value)
    } else {
      ElMessage.warning('未找到商家详情信息')
    }
  } catch (error) {
    console.error('获取商家详情失败:', error)
    ElMessage.error('获取商家详情失败: ' + (error.message || '未知错误'))
  } finally {
    loadingMerchant.value = false
  }
}

// 确认操作
const confirmAction = async () => {
  if (!currentUser.value || !currentAction.value) return
  
  try {
    if (currentAction.value === 'approve') {
      await approveUser(currentUser.value.id)
      ElMessage.success('已通过用户注册申请')
    } else if (currentAction.value === 'reject') {
      await rejectUser(currentUser.value.id)
      ElMessage.success('已拒绝用户注册申请')
    } else if (currentAction.value === 'disable') {
      await updateUserStatus(currentUser.value.id, 'DISABLED')
      ElMessage.success('用户已禁用')
    } else if (currentAction.value === 'enable') {
      await updateUserStatus(currentUser.value.id, 'NORMAL')
      ElMessage.success('用户已启用')
    }
    
    // 重新加载相应的用户列表
    if (activeTab.value === 'pending') {
      loadPendingUsers()
    } else {
      loadAllUsers()
    }
    
    // 关闭商家详情弹窗（如果打开的话）
    if (merchantDetailsVisible.value) {
      merchantDetailsVisible.value = false
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败: ' + (error.message || '未知错误'))
  } finally {
    dialogVisible.value = false
  }
}

// 监听标签切换
watch(activeTab, (newTab) => {
  if (newTab === 'all') {
    loadAllUsers()
  } else if (newTab === 'pending') {
    loadPendingUsers()
  }
})

// 组件挂载时加载待审核用户
onMounted(() => {
  loadPendingUsers()
})

// 获取用户状态文本
const getUserStatusText = (status) => {
  switch (status) {
    case 'NORMAL': return '正常';
    case 'PENDING': return '待审核';
    case 'DISABLED': return '已禁用';
    default: return status;
  }
}

// 获取用户角色文本
const getUserRoleText = (role) => {
  switch (role) {
    case 'USER': return '普通用户';
    case 'MERCHANT': return '商家';
    case 'ADMIN': return '管理员';
    default: return role;
  }
}

// 处理图片点击，打开大图预览
const handleImageClick = (imageUrl) => {
  previewImageUrl.value = imageUrl
  imagePreviewTitle.value = imageUrl.includes('businessLicense') ? '营业执照' : '身份证照片'
  imagePreviewVisible.value = true
}

// 切换到编辑模式
const switchToEditMode = () => {
  isEditMode.value = true
  editingUser.value = { ...selectedUser.value }
}

// 取消编辑
const cancelEdit = () => {
  isEditMode.value = false
  editingUser.value = {}
}

// 保存用户信息
const saveUserInfo = async () => {
  // 基本数据验证
  if (editingUser.value.email && !validateEmail(editingUser.value.email)) {
    ElMessage.warning('请输入有效的邮箱地址');
    return;
  }
  
  if (editingUser.value.phone && !validatePhone(editingUser.value.phone)) {
    ElMessage.warning('请输入有效的手机号码');
    return;
  }
  
  isSaving.value = true;
  try {
    // 确保带有ID
    const userData = {
      id: selectedUser.value.id,
      ...editingUser.value
    };
    
    console.log('正在提交用户数据:', userData);
    
    const response = await updateUserInfo(selectedUser.value.id, userData);
    
    if (response.data && (response.data.code === 200 || response.data.code === 0)) {
      ElMessage.success('用户信息更新成功');
      
      // 更新本地数据
      Object.assign(selectedUser.value, editingUser.value);
      
      // 刷新用户列表以更新显示
      if (activeTab.value === 'all') {
        loadAllUsers();
      }
      
      // 切换回查看模式
      isEditMode.value = false;
    } else {
      ElMessage.error('更新用户信息失败: ' + (response.data?.message || '未知错误'));
    }
  } catch (error) {
    console.error('更新用户信息失败:', error);
    ElMessage.error('更新用户信息失败: ' + (error.message || '未知错误'));
  } finally {
    isSaving.value = false;
  }
};

// 验证邮箱
const validateEmail = (email) => {
  const regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  return regex.test(email);
};

// 验证手机号
const validatePhone = (phone) => {
  const regex = /^1[3-9]\d{9}$/;
  return regex.test(phone);
};
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.user-management h1 {
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

.user-table-container {
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
  justify-content: space-between;
}

.filter-container {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #fff;
  border-radius: 4px;
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: center;
}

.filter-item {
  display: flex;
  align-items: center;
}

.filter-label {
  margin-right: 8px;
  font-weight: 500;
  color: #606266;
}

.filter-item .el-select {
  width: 180px;
}

.filter-item.keyword .el-input {
  width: 220px;
}

.filter-actions {
  margin-left: auto;
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.user-details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.user-info-item {
  padding: 8px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
}

.user-info-item .label {
  font-weight: bold;
  margin-right: 10px;
  color: #606266;
  width: 110px;
  flex-shrink: 0;
}

.user-info-item .el-input,
.user-info-item .el-select {
  width: 100%;
}

.user-info-item .el-textarea {
  width: 100%;
}

/* 新增商家详情样式 */
.merchant-details h3 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 18px;
  color: #303133;
}

.merchant-details h4 {
  margin-top: 0;
  margin-bottom: 12px;
  color: #606266;
}

.info-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.info-row {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-label {
  font-weight: bold;
  margin-right: 10px;
  color: #606266;
}

.document-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  margin-bottom: 20px;
}

.document-preview {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 20px;
}

.image-container {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 15px;
  cursor: zoom-in;
}

.document-image {
  max-height: 380px;
  max-width: 100%;
  object-fit: contain;
}

.image-preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 80vh;
  background-color: #f5f7fa;
}

.preview-image {
  max-width: 100%;
  max-height: 80vh;
  object-fit: contain;
}

.image-actions {
  display: flex;
  justify-content: center;
}

.image-preview-dialog .el-dialog__body {
  padding: 0;
}

.merchant-detail-dialog .el-dialog__body {
  padding: 20px;
  max-height: calc(90vh - 130px);
  overflow-y: auto;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  height: 100%;
}

.image-error .el-icon {
  font-size: 28px;
  margin-bottom: 8px;
}
</style> 