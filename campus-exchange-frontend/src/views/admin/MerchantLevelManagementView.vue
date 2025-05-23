<template>
  <div class="merchant-level-management">
    <div class="page-header">
      <h1>商家等级与费率管理</h1>
    </div>
    
    <div class="page-content">
      <!-- 加载中 -->
      <div v-if="loading" class="loading-container">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在加载商家等级数据...</span>
      </div>
      
      <!-- 数据为空 -->
      <el-empty v-else-if="levels.length === 0" description="暂无商家等级数据"></el-empty>
      
      <!-- 商家等级表格 -->
      <el-table
        v-else
        :data="levels"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="levelName" label="等级名称" width="180" />
        <el-table-column label="手续费率" width="180">
          <template #default="scope">
            <div class="rate-display">
              {{ formatCommissionRate(scope.row.commissionRate) }}%
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="等级描述" />
        <el-table-column label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="最近更新" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="openEditLevelDialog(scope.row)"
            >
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 编辑商家等级对话框 -->
    <el-dialog
      v-model="levelDialogVisible"
      title="编辑商家等级"
      width="500px"
      @close="resetForm"
    >
      <el-form
        ref="levelFormRef"
        :model="levelForm"
        :rules="levelRules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="等级名称" prop="levelName">
          <el-input v-model="levelForm.levelName" placeholder="请输入等级名称"></el-input>
        </el-form-item>
        
        <el-form-item label="手续费率" prop="commissionRate">
          <div class="rate-input">
            <el-slider
              v-model="sliderRate"
              :min="0"
              :max="10"
              :step="0.01"
              :format-tooltip="formatRateTooltip"
              @input="handleSliderChange"
            ></el-slider>
            <div class="rate-value">
              <el-input-number
                v-model="sliderRate"
                :min="0"
                :max="10"
                :step="0.01"
                :precision="2"
                controls-position="right"
                @change="handleInputChange"
              ></el-input-number>
              <span class="rate-unit">%</span>
            </div>
          </div>
          <div class="rate-tips">手续费率范围: 0% - 10%</div>
        </el-form-item>
        
        <el-form-item label="等级描述" prop="description">
          <el-input
            v-model="levelForm.description"
            type="textarea"
            rows="4"
            placeholder="请输入等级描述"
          ></el-input>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="levelDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitLevelForm" :loading="submitting">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Loading } from '@element-plus/icons-vue';
import { 
  getAllMerchantLevels, 
  updateMerchantLevel
} from '@/api/adminMerchantLevel';

// 数据列表
const levels = ref([]);
const loading = ref(true);

// 对话框控制
const levelDialogVisible = ref(false);
const currentLevel = ref(null);
const submitting = ref(false);

// 表单相关
const levelFormRef = ref(null);
const levelForm = ref({
  id: null,
  levelName: '',
  commissionRate: 0.01,
  description: ''
});
const sliderRate = ref(1.0); // 手续费率滑块值，表示为百分比

// 表单验证规则
const levelRules = {
  levelName: [
    { required: true, message: '请输入等级名称', trigger: 'blur' },
    { min: 2, max: 20, message: '等级名称长度应为2-20个字符', trigger: 'blur' }
  ],
  commissionRate: [
    { required: true, message: '请设置手续费率', trigger: 'change' }
  ],
  description: [
    { max: 200, message: '描述最多200个字符', trigger: 'blur' }
  ]
};

// 格式化手续费率显示（转为百分比）
const formatCommissionRate = (rate) => {
  if (rate === null || rate === undefined) return '0.00';
  // 将小数转换为百分比表示（数据库存储的是0.0075表示0.75%）
  return (rate * 100).toFixed(2);
};

// 格式化滑块提示
const formatRateTooltip = (val) => {
  return val.toFixed(2) + '%';
};

// 处理滑块值变化
const handleSliderChange = (val) => {
  // 将百分比转换为小数存储到表单
  levelForm.value.commissionRate = val / 100;
};

// 处理输入框值变化
const handleInputChange = (val) => {
  sliderRate.value = val;
  // 将百分比转换为小数存储到表单
  levelForm.value.commissionRate = val / 100;
};

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleString();
};

// 加载商家等级数据
const loadMerchantLevels = async () => {
  loading.value = true;
  try {
    const response = await getAllMerchantLevels();
    if (response.data && response.data.code === 200) {
      levels.value = response.data.data || [];
      console.log('商家等级数据:', levels.value);
    } else {
      ElMessage.error('获取商家等级数据失败');
    }
  } catch (error) {
    console.error('获取商家等级数据出错:', error);
    ElMessage.error('获取商家等级数据失败: ' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};

// 打开编辑等级对话框
const openEditLevelDialog = (level) => {
  currentLevel.value = level;
  
  // 填充表单数据
  levelForm.value = {
    id: level.id,
    levelName: level.levelName,
    commissionRate: level.commissionRate,
    description: level.description || ''
  };
  
  // 设置滑块值（转换为百分比）
  sliderRate.value = level.commissionRate * 100;
  
  levelDialogVisible.value = true;
};

// 重置表单
const resetForm = () => {
  if (levelFormRef.value) {
    levelFormRef.value.resetFields();
  }
  
  levelForm.value = {
    id: null,
    levelName: '',
    commissionRate: 0.01,
    description: ''
  };
  
  sliderRate.value = 1.0;
  currentLevel.value = null;
};

// 提交表单
const submitLevelForm = async () => {
  if (!levelFormRef.value) return;
  
  await levelFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    submitting.value = true;
    try {
      // 更新商家等级所有信息
      const response = await updateMerchantLevel(currentLevel.value.id, levelForm.value);
      if (response.data && response.data.code === 200) {
        ElMessage.success('商家等级更新成功');
        levelDialogVisible.value = false;
        await loadMerchantLevels(); // 重新加载数据
      } else {
        ElMessage.error(response.data?.message || '更新商家等级失败');
      }
    } catch (error) {
      console.error('提交商家等级数据失败:', error);
      ElMessage.error('操作失败: ' + (error.message || '未知错误'));
    } finally {
      submitting.value = false;
    }
  });
};

// 页面加载时获取数据
onMounted(() => {
  loadMerchantLevels();
});
</script>

<style scoped>
.merchant-level-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #909399;
  gap: 12px;
}

.loading-container .el-icon {
  font-size: 24px;
}

.rate-display {
  font-weight: bold;
  color: #e6a23c;
}

.rate-input {
  display: flex;
  align-items: center;
  gap: 16px;
}

.rate-input .el-slider {
  flex: 1;
  margin-right: 12px;
}

.rate-value {
  width: 120px;
  display: flex;
  align-items: center;
}

.rate-unit {
  margin-left: 8px;
  font-size: 14px;
  color: #606266;
}

.rate-tips {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}
</style> 