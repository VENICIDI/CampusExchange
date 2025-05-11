<template>
  <div class="product-publish-container">
    <div class="publish-header">
      <h1>{{ isEdit ? '编辑商品' : '发布二手商品' }}</h1>
      <p>请填写商品详情，发布后将由管理员审核</p>
      
      <div class="steps-guide">
        <div class="step-item active">
          <div class="step-number">1</div>
          <div class="step-name">填写信息</div>
        </div>
        <div class="step-line"></div>
        <div class="step-item">
          <div class="step-number">2</div>
          <div class="step-name">审核通过</div>
        </div>
        <div class="step-line"></div>
        <div class="step-item">
          <div class="step-number">3</div>
          <div class="step-name">上架销售</div>
        </div>
      </div>
    </div>

    <el-form :model="productForm" :rules="rules" ref="productFormRef" label-width="100px" class="product-form">
      <!-- 基本信息 -->
      <div class="form-section">
        <h2><i class="section-icon info-icon"></i>基本信息</h2>
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称"></el-input>
        </el-form-item>

        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="productForm.categoryId" placeholder="请选择商品分类">
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="新旧程度" prop="condition">
          <el-select v-model="productForm.condition" placeholder="请选择商品新旧程度">
            <el-option label="全新" value="NEW"></el-option>
            <el-option label="九成新" value="LIKE_NEW"></el-option>
            <el-option label="八成新" value="GOOD"></el-option>
            <el-option label="七成新" value="FAIR"></el-option>
            <el-option label="六成新及以下" value="POOR"></el-option>
          </el-select>
        </el-form-item>
      </div>

      <!-- 价格信息 -->
      <div class="form-section">
        <h2><i class="section-icon price-icon"></i>价格信息</h2>
        <div class="price-tips">
          设置合理的价格可以吸引更多买家，特别是相对于原价有明显优惠的价格
        </div>
        
        <el-form-item label="原价" prop="originalPrice">
          <el-input-number v-model="productForm.originalPrice" :precision="2" :min="0" :step="1" placeholder="请输入商品原价"></el-input-number>
          <span class="unit">元</span>
        </el-form-item>

        <el-form-item label="现价" prop="currentPrice">
          <el-input-number v-model="productForm.currentPrice" :precision="2" :min="0" :step="1" placeholder="请输入商品现价"></el-input-number>
          <span class="unit">元</span>
        </el-form-item>

        <el-form-item label="可议价" prop="negotiable">
          <el-switch v-model="productForm.negotiable" active-text="是" inactive-text="否"></el-switch>
        </el-form-item>

        <el-form-item label="库存数量" prop="stock">
          <el-input-number v-model="productForm.stock" :min="1" :step="1" placeholder="请输入库存数量"></el-input-number>
          <span class="tip">默认为1件</span>
        </el-form-item>
      </div>

      <!-- 详细描述 -->
      <div class="form-section">
        <h2><i class="section-icon desc-icon"></i>详细描述</h2>
        <div class="desc-tips">
          详细的描述可以让买家更好地了解商品情况，增加成交几率
        </div>
        
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="productForm.description" type="textarea" :rows="4" placeholder="请详细描述商品的材质、功能、用途、购买渠道等信息"></el-input>
        </el-form-item>

        <el-form-item label="商品尺寸" prop="size">
          <el-input v-model="productForm.size" placeholder="请输入商品尺寸，例如：长x宽x高"></el-input>
        </el-form-item>

        <el-form-item label="使用说明" prop="usageInstructions">
          <el-input v-model="productForm.usageInstructions" type="textarea" :rows="3" placeholder="请输入使用说明"></el-input>
        </el-form-item>
      </div>

      <!-- 商品图片 -->
      <div class="form-section">
        <h2><i class="section-icon image-icon"></i>商品图片</h2>
        <div class="upload-tip">
          <i class="tip-icon"></i>请上传清晰的商品图片，第一张图片将作为商品主图。建议上传多角度的照片，尺寸越大越清晰。
        </div>

        <el-form-item prop="imageUrls">
          <el-upload
            :action="null"
            :http-request="customUpload"
            list-type="picture-card"
            :on-preview="handlePictureCardPreview"
            :on-remove="handleRemove"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            :limit="6"
            :file-list="getInitialFileList()"
            multiple>
            <el-icon><Plus /></el-icon>
          </el-upload>
          <el-dialog v-model:visible="dialogVisible">
            <img width="100%" :src="dialogImageUrl" alt="商品图片预览">
          </el-dialog>
        </el-form-item>
      </div>

      <!-- 提交按钮 -->
      <div class="form-actions">
        <el-button @click="resetForm" class="cancel-btn">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting" class="submit-btn">
          {{ isEdit ? '保存修改' : '提交发布' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { productApi } from '@/api/product';
import { Plus } from '@element-plus/icons-vue';
import { ElMessage, ElForm } from 'element-plus';
import { fileApi } from '@/api/all';

// 引入URL处理函数
const processImageUrl = fileApi.processImageUrl;

const route = useRoute();
const router = useRouter();
const productFormRef = ref(null);

const isEdit = ref(false);
const productId = ref(null);
const categories = ref([]);
const dialogVisible = ref(false);
const dialogImageUrl = ref('');
const submitting = ref(false);

// 添加用户信息
const userInfo = reactive({
  role: ''
});

const productForm = reactive({
  name: '',
  categoryId: '',
  condition: 'LIKE_NEW',
  originalPrice: 0,
  currentPrice: 0,
  negotiable: false,
  stock: 1,
  description: '',
  size: '',
  usageInstructions: '',
  imageUrls: []
});

const rules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  condition: [
    { required: true, message: '请选择商品新旧程度', trigger: 'change' }
  ],
  originalPrice: [
    { required: true, message: '请输入商品原价', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '原价必须大于0', trigger: 'blur' }
  ],
  currentPrice: [
    { required: true, message: '请输入商品现价', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '现价必须大于0', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '库存必须大于0', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' },
    { min: 10, max: 1000, message: '长度在 10 到 1000 个字符', trigger: 'blur' }
  ],
  imageUrls: [
    { required: true, message: '请上传至少一张商品图片', trigger: 'change' }
  ]
};

// 获取用户信息
const getUserInfo = () => {
  const userInfoStr = localStorage.getItem('user');
  if (userInfoStr) {
    try {
      const userData = JSON.parse(userInfoStr);
      userInfo.role = userData.role || '';
      
      // 如果不是商家角色，提示并跳转
      if (userInfo.role !== 'MERCHANT') {
        ElMessage.warning('只有商家用户才能发布商品，请升级为商家账号');
        setTimeout(() => {
          router.push('/');
        }, 2000);
      }
    } catch (e) {
      console.error('解析用户数据失败:', e);
    }
  } else {
    // 未登录，跳转到登录页
    ElMessage.warning('请先登录');
    router.push('/login?redirect=' + encodeURIComponent(route.fullPath));
  }
};

// 获取商品分类
const fetchCategories = async () => {
  try {
    const response = await productApi.getAllCategories();
    categories.value = response.data.data || [];
  } catch (error) {
    ElMessage.error('获取商品分类失败：' + error.message);
  }
};

// 获取商品详情（编辑模式）
const fetchProductDetail = async (id) => {
  try {
    const response = await productApi.getProductById(id);
    const productData = response.data.data;
    
    if (productData) {
      console.log('获取到的商品详情:', productData);
      
      // 更新响应式数据
      productForm.name = productData.name;
      productForm.categoryId = productData.categoryId;
      productForm.condition = productData.productCondition || productData.condition; // 兼容新旧字段名
      productForm.originalPrice = productData.originalPrice;
      productForm.currentPrice = productData.currentPrice;
      productForm.negotiable = productData.negotiable;
      productForm.stock = productData.stock;
      productForm.description = productData.description;
      productForm.size = productData.size || productData.sizeInfo; // 兼容新旧字段名
      productForm.usageInstructions = productData.usageInstructions;
      
      // 图片兼容性处理
      const imageUrls = productData.imageUrls || productData.images || [];
      productForm.imageUrls = Array.isArray(imageUrls) ? imageUrls : [];
      
      console.log('设置的商品图片:', productForm.imageUrls);
    }
  } catch (error) {
    ElMessage.error('获取商品详情失败：' + error.message);
  }
};

// 图片上传成功处理
const handleUploadSuccess = (response, file, fileList) => {
  if (response.code === 200 && response.data) {
    // 添加上传的图片URL到表单
    productForm.imageUrls = productForm.imageUrls || [];
    // 使用URL处理函数
    const imageUrl = response.data;
    console.log('原始上传图片URL:', imageUrl);
    productForm.imageUrls.push(imageUrl);
    console.log('图片上传成功，当前图片列表:', productForm.imageUrls);
  } else {
    ElMessage.error('图片上传失败：' + (response.message || '未知错误'));
  }
};

// 图片上传前检查
const beforeUpload = (file) => {
  // 检查文件类型
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    ElMessage.error('只能上传图片文件！');
    return false;
  }
  
  // 检查文件大小
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！');
    return false;
  }
  
  return true;
};

// 自定义上传处理
const customUpload = async (options) => {
  try {
    const { file } = options;
    const formData = new FormData();
    formData.append('files', file);
    
    console.log('准备上传商品图片:', file.name, '大小:', file.size, '类型:', file.type);
    
    // 使用fetch API直接上传
    const response = await fetch('/api/upload/images', {
      method: 'POST',
      body: formData
    });
    
    const result = await response.json();
    console.log('商品图片上传响应:', result);
    
    if (result.code === 200 && result.data && result.data.length > 0) {
      // 记录原始URL
      const originalUrl = result.data[0];
      console.log('服务器返回的原始图片URL:', originalUrl);
      
      // 调用成功回调
      options.onSuccess({ 
        code: 200, 
        data: originalUrl
      });
    } else {
      console.error('上传失败，响应:', result);
      // 调用失败回调
      options.onError('上传失败：' + (result.message || '未知错误'));
    }
  } catch (error) {
    console.error('上传出错:', error);
    options.onError('上传出错：' + error.message);
  }
};

// 移除图片
const handleRemove = (file, fileList) => {
  // 根据文件查找URL
  const fileUrl = file.response?.data;
  if (fileUrl) {
    const index = productForm.imageUrls.indexOf(fileUrl);
    if (index !== -1) {
      productForm.imageUrls.splice(index, 1);
    }
  }
};

// 预览图片
const handlePictureCardPreview = (file) => {
  // 根据文件获取预览URL
  const rawUrl = file.url || (file.response && file.response.data);
  dialogImageUrl.value = processImageUrl(rawUrl);
  console.log('预览图片原始URL:', rawUrl);
  console.log('预览图片处理后URL:', dialogImageUrl.value);
  dialogVisible.value = true;
};

// 重置表单
const resetForm = () => {
  if (isEdit.value) {
    // 编辑模式下返回上一页
    router.back();
  } else {
    // 新建模式下清空表单
    if (productFormRef.value) {
      productFormRef.value.resetFields();
    }
    productForm.imageUrls = [];
  }
};

// 提交表单
const submitForm = async () => {
  if (!productFormRef.value) return;
  
  await productFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      
      try {
        // 使用productCondition字段名，保持后端一致
        const submitData = {
          ...productForm,
          productCondition: productForm.condition
        };
        
        let response;
        if (isEdit.value) {
          // 编辑模式下更新商品
          response = await productApi.updateProduct(productId.value, submitData);
        } else {
          // 新建模式下创建商品
          response = await productApi.createProduct(submitData);
        }
        
        if (response.data.code === 200) {
          ElMessage.success(isEdit.value ? '商品更新成功！' : '商品发布成功！');
          // 跳转到商家中心
          router.push('/merchant');
        } else {
          ElMessage.error(response.data.message || '操作失败');
        }
      } catch (error) {
        ElMessage.error('提交失败：' + error.message);
      } finally {
        submitting.value = false;
      }
    } else {
      ElMessage.warning('请完善表单信息');
    }
  });
};

// 获取初始文件列表（编辑模式下使用）
const getInitialFileList = () => {
  if (!isEdit.value || !productForm.imageUrls || productForm.imageUrls.length === 0) {
    return [];
  }
  
  // 将URL转换为文件列表格式
  return productForm.imageUrls.map((url, index) => {
    console.log('初始化商品图片URL:', url);
    const processedUrl = processImageUrl(url);
    console.log('处理后的商品图片URL:', processedUrl);
    return {
      name: `图片${index + 1}`,
      url: processedUrl
    };
  });
};

// 页面初始化
onMounted(() => {
  getUserInfo();
  fetchCategories();
  
  // 判断是否为编辑模式
  const routeProductId = route.params.id;
  if (routeProductId) {
    isEdit.value = true;
    productId.value = routeProductId;
    fetchProductDetail(routeProductId);
  }
});
</script>

<style scoped>
.product-publish-container {
  max-width: 1000px;
  margin: 30px auto;
  padding: 30px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.publish-header {
  text-align: center;
  margin-bottom: 40px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.publish-header h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
  font-weight: 700;
  position: relative;
  display: inline-block;
}

.publish-header h1::after {
  content: "";
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, #4a6ee0, #6a8fff);
  border-radius: 3px;
}

.publish-header p {
  font-size: 16px;
  color: #666;
  margin-top: 15px;
}

.steps-guide {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 30px;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.step-number {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #e0e6f7;
  color: #4a6ee0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.step-item.active .step-number {
  background-color: #4a6ee0;
  color: white;
  box-shadow: 0 0 0 4px rgba(74, 110, 224, 0.2);
}

.step-name {
  font-size: 13px;
  color: #666;
}

.step-item.active .step-name {
  color: #4a6ee0;
  font-weight: 500;
}

.step-line {
  height: 2px;
  width: 80px;
  background-color: #e0e6f7;
  margin: 0 10px;
  margin-bottom: 28px;
}

.price-tips, .desc-tips {
  background-color: #f0f4ff;
  padding: 10px 15px;
  border-radius: 6px;
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
  border-left: 3px solid #4a6ee0;
}

.section-icon {
  display: inline-block;
  width: 20px;
  height: 20px;
  margin-right: 8px;
  position: relative;
  top: 3px;
  background-size: contain;
  background-repeat: no-repeat;
}

.info-icon:before {
  content: "📋";
}

.price-icon:before {
  content: "💲";
}

.desc-icon:before {
  content: "📝";
}

.image-icon:before {
  content: "🖼️";
}

.tip-icon:before {
  content: "💡";
  margin-right: 5px;
}

.form-section {
  margin-bottom: 35px;
  padding: 25px;
  background: #f8faff;
  border-radius: 10px;
  border-left: 4px solid #4a6ee0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.form-section:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
  transform: translateY(-3px);
}

.form-section h2 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e0e6f7;
  color: #4a6ee0;
}

.unit {
  margin-left: 10px;
  color: #666;
}

.tip {
  margin-left: 10px;
  color: #666;
  font-size: 13px;
}

.upload-tip {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
  padding: 8px 12px;
  background-color: #f0f4ff;
  border-radius: 6px;
  border-left: 3px solid #4a6ee0;
}

.form-actions {
  margin-top: 40px;
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.form-actions button {
  margin: 0 10px;
  min-width: 120px;
  padding: 12px 24px;
  font-size: 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.form-actions button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.el-input, .el-input-number, .el-select, .el-textarea {
  width: 100%;
}

.el-input input, .el-textarea textarea {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.el-input input:hover, .el-textarea textarea:hover {
  border-color: #4a6ee0;
}

.el-input input:focus, .el-textarea textarea:focus {
  box-shadow: 0 0 0 2px rgba(74, 110, 224, 0.2);
}

.el-form-item {
  margin-bottom: 22px;
}

.el-upload--picture-card {
  border-radius: 10px;
  transition: all 0.3s ease;
  border-color: #e0e6f7;
}

.el-upload--picture-card:hover {
  border-color: #4a6ee0;
  transform: translateY(-2px);
}

.submit-btn {
  background: linear-gradient(90deg, #4a6ee0, #6a8fff);
  border: none;
  font-weight: 600;
  letter-spacing: 0.5px;
  padding: 12px 30px;
}

.submit-btn:hover {
  background: linear-gradient(90deg, #3d5eca, #5c7df0);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(74, 110, 224, 0.3);
}

.cancel-btn {
  border: 1px solid #dcdfe6;
  background: white;
  color: #606266;
}

.cancel-btn:hover {
  border-color: #c0c4cc;
  color: #333;
}

@media (max-width: 768px) {
  .product-publish-container {
    padding: 20px;
    margin: 20px 10px;
  }
  
  .form-section {
    padding: 15px;
  }
  
  .publish-header h1 {
    font-size: 24px;
  }
  
  .form-actions button {
    width: 100%;
    margin: 10px 0;
  }

  .submit-btn, .cancel-btn {
    width: 100%;
    margin: 10px 0;
  }

  .steps-guide {
    flex-direction: column;
    margin-top: 20px;
  }
  
  .step-line {
    width: 2px;
    height: 30px;
    margin: 5px 0;
  }
  
  .step-item {
    flex-direction: row;
    width: 100%;
    justify-content: center;
  }
  
  .step-number {
    margin-bottom: 0;
    margin-right: 10px;
  }
}
</style> 