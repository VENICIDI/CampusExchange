<template>
  <div class="merchant-profile-edit">
    <div class="container">
      <div class="page-header">
        <h1>商家信息管理</h1>
        <p>您可以在此页面修改个人资料和店铺信息</p>
      </div>

      <el-card class="form-container" v-loading="isLoading">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-form
              ref="basicFormRef"
              :model="basicForm"
              :rules="basicRules"
              label-position="top">

              <div class="avatar-container">
                <div class="avatar-wrapper">
                  <img :src="processImageUrl(avatarUrl)" alt="用户头像" class="avatar-preview">
                  <div class="avatar-upload-mask" @click="triggerAvatarUpload">
                    <i class="el-icon-camera"></i>
                    <span>更换头像</span>
                  </div>
                </div>
                <input
                  type="file"
                  ref="avatarInput"
                  style="display: none"
                  accept="image/*"
                  @change="handleAvatarChange"
                />
              </div>

              <el-form-item label="用户名" prop="username">
                <el-input v-model="basicForm.username"></el-input>
              </el-form-item>

              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="basicForm.realName" disabled></el-input>
                <small>真实姓名已经过审核，无法修改</small>
              </el-form-item>

              <el-form-item label="手机号码" prop="phone">
                <el-input v-model="basicForm.phone"></el-input>
              </el-form-item>

              <el-form-item label="邮箱" prop="email">
                <el-input v-model="basicForm.email"></el-input>
              </el-form-item>

              <el-form-item label="微信号" prop="wechat">
                <el-input v-model="basicForm.wechat"></el-input>
              </el-form-item>

              <el-form-item label="收货地址" prop="defaultAddress">
                <el-input v-model="basicForm.defaultAddress"></el-input>
              </el-form-item>

              <el-form-item label="个人介绍" prop="personalIntro">
                <el-input type="textarea" v-model="basicForm.personalIntro" :rows="4"></el-input>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="店铺信息" name="store">
            <el-form
              ref="storeFormRef"
              :model="storeForm"
              :rules="storeRules"
              label-position="top">

              <el-form-item label="店铺名称" prop="storeName">
                <el-input v-model="storeForm.storeName"></el-input>
              </el-form-item>

              <el-form-item label="店铺描述" prop="description">
                <el-input type="textarea" v-model="storeForm.description" :rows="4"></el-input>
              </el-form-item>

              <el-form-item label="营业执照" prop="businessLicense">
                <div class="license-container">
                  <div class="document-preview">
                    <img v-if="businessLicenseUrl" :src="processImageUrl(businessLicenseUrl)" class="document-image" />
                    <div v-else class="empty-document">
                      <i class="el-icon-picture"></i>
                      <span>暂无图片</span>
                    </div>
                  </div>
                  <el-button type="primary" size="small" @click="triggerLicenseUpload">
                    {{ businessLicenseUrl ? '更新图片' : '上传图片' }}
                  </el-button>
                  <input
                    type="file"
                    ref="licenseInput"
                    style="display: none"
                    accept="image/*"
                    @change="handleLicenseChange"
                  />
                </div>
              </el-form-item>

              <el-form-item label="身份证照片" prop="idCard">
                <div class="license-container">
                  <div class="document-preview">
                    <img v-if="idCardUrl" :src="processImageUrl(idCardUrl)" class="document-image" />
                    <div v-else class="empty-document">
                      <i class="el-icon-picture"></i>
                      <span>暂无图片</span>
                    </div>
                  </div>
                  <el-button type="primary" size="small" @click="triggerIdCardUpload">
                    {{ idCardUrl ? '更新图片' : '上传图片' }}
                  </el-button>
                  <input
                    type="file"
                    ref="idCardInput"
                    style="display: none"
                    accept="image/*"
                    @change="handleIdCardChange"
                  />
                </div>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="店铺统计" name="stats" v-if="merchantData">
            <div class="stats-container">
              <div class="stat-card">
                <div class="stat-value">{{ merchantData.totalSalesCount || 0 }}</div>
                <div class="stat-label">总销售量</div>
              </div>
              <div class="stat-card">
                <div class="stat-value">{{ formatCurrency(merchantData.totalSalesAmount) }}</div>
                <div class="stat-label">总销售额</div>
              </div>
              <div class="stat-card">
                <div class="stat-value">{{ (merchantData.storePositiveRate || 0) + '%' }}</div>
                <div class="stat-label">好评率</div>
              </div>
              <div class="stat-card level-card">
                <div class="stat-value level-tag">{{ levelName }}</div>
                <div class="stat-label">商家等级</div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>

        <div class="form-actions">
          <el-button @click="resetForm">取消修改</el-button>
          <el-button type="primary" @click="saveForm" :loading="isSaving">保存修改</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { merchantApi, userApi, fileApi } from '@/api/all'
import api from '@/api/index'

// 路由和状态
const router = useRouter()
const isLoading = ref(false)
const isSaving = ref(false)
const activeTab = ref('basic')

// 表单引用
const basicFormRef = ref(null)
const storeFormRef = ref(null)

// 用户和商家数据
const userData = ref(null)
const merchantData = ref(null)

// 表单数据
const basicForm = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  wechat: '',
  defaultAddress: '',
  personalIntro: '',
  avatar: '',
})

const storeForm = reactive({
  storeName: '',
  description: '',
  businessLicense: '',
  idCard: '',
})

// 图片预览URL
const avatarUrl = ref('/default-avatar.png')
const businessLicenseUrl = ref('')
const idCardUrl = ref('')

// 文件上传引用
const avatarInput = ref(null)
const licenseInput = ref(null)
const idCardInput = ref(null)

// 表单验证规则
const basicRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
}

const storeRules = {
  storeName: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入店铺描述', trigger: 'blur' }],
}

// 商家等级名称
const levelName = computed(() => {
  if (!merchantData.value || !merchantData.value.levelId) return '未知等级';

  // 实际项目中应该从后端获取等级名称，这里简化处理
  const levelMap = {
    1: '钻石商家',
    2: '金牌商家',
    3: '银牌商家',
    4: '铜牌商家',
    5: '普通商家'
  };

  return levelMap[merchantData.value.levelId] || '普通商家';
});

// 格式化货币
const formatCurrency = (value) => {
  if (!value) return '¥0.00';
  return '¥' + parseFloat(value).toFixed(2);
};

// 获取用户数据和商家数据
const fetchUserData = async () => {
  isLoading.value = true;
  try {
    // 获取当前用户信息
    const userResponse = await userApi.getUserProfile();
    if (userResponse.data.code === 200) {
      userData.value = userResponse.data.data;

      // 填充基本表单
      basicForm.username = userData.value.username || '';
      basicForm.realName = userData.value.realName || '';
      basicForm.phone = userData.value.phone || '';
      basicForm.email = userData.value.email || '';
      basicForm.wechat = userData.value.wechat || '';
      basicForm.defaultAddress = userData.value.defaultAddress || '';
      basicForm.personalIntro = userData.value.personalIntro || '';
      basicForm.avatar = userData.value.avatar || '';

      // 设置头像URL
      if (userData.value.avatar) {
        avatarUrl.value = userData.value.avatar;
      }

      // 获取商家信息
      const merchantResponse = await merchantApi.getMerchantByUserId(userData.value.id);
      if (merchantResponse.data.code === 200) {
        merchantData.value = merchantResponse.data.data;

        // 填充商家表单
        storeForm.storeName = merchantData.value.storeName || '';
        storeForm.description = merchantData.value.description || '';
        storeForm.businessLicense = merchantData.value.businessLicense || '';
        storeForm.idCard = merchantData.value.idCard || '';

        // 设置证件图片URL
        if (merchantData.value.businessLicense && merchantData.value.businessLicense !== '待上传') {
          businessLicenseUrl.value = merchantData.value.businessLicense;
        }

        if (merchantData.value.idCard && merchantData.value.idCard !== '待上传') {
          idCardUrl.value = merchantData.value.idCard;
        }
      }
    } else {
      ElMessage.error('获取用户信息失败');
    }
  } catch (error) {
    console.error('获取数据出错:', error);
    ElMessage.error('获取数据失败，请稍后重试');
  } finally {
    isLoading.value = false;
  }
};

// 触发头像上传
const triggerAvatarUpload = () => {
  avatarInput.value.click();
};

// 处理头像变更
const handleAvatarChange = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  // 验证文件类型和大小
  if (!file.type.includes('image/')) {
    ElMessage.error('请上传图片文件');
    event.target.value = ''; // 清空选择，以便可以重新选择同一文件
    return;
  }
  if (file.size > 5 * 1024 * 1024) { // 假设限制为5MB
    ElMessage.error('图片大小不能超过5MB');
    event.target.value = '';
    return;
  }

  const formData = new FormData();
  formData.append('file', file);

  try {
    console.log('开始上传头像文件:', file.name, '大小:', file.size, '类型:', file.type);
    const response = await fileApi.uploadFile(formData);
    console.log('头像上传响应:', response);
    console.log('完整头像上传响应数据:', JSON.stringify(response.data));

    // ***** 关键修改在这里 *****
    if (response.data && response.data.code === 200 && typeof response.data.message === 'string' && response.data.message.startsWith('http')) {
      const newUploadedFullUrl = response.data.message; // 从 response.data.message 获取完整 URL
      console.log('成功从后端获取新头像的完整URL:', newUploadedFullUrl);

      // 更新用于预览的 avatarUrl (它会经过 processImageUrl 转换为相对路径或保持原样)
      avatarUrl.value = processImageUrl(newUploadedFullUrl);

      // 更新 basicForm.avatar，存储的是后端返回的、未经 processImageUrl 处理的、用于持久化的 URL
      // 后端用户表中的 avatar 字段期望存储的是可以通过 /api/static/... 访问的相对路径，
      // 或者是一个完整的可访问的 URL。fileApi.processProductImageUrl 或 processImageUrl
      // 的作用是确保前端能通过代理或直接访问。
      // 如果后端存储的是 /api/static/... 这种相对路径，那么这里也应该存这种。
      // fileApi.processProductImageUrl 里面有逻辑：
      // if (typeof url === 'string' && url.includes('localhost:8080/api/static/')) {
      //   const pathPart = url.split('localhost:8080')[1]; return pathPart; }
      // 这意味着 processImageUrl(newUploadedFullUrl) 会返回 /api/static/...
      // 所以，basicForm.avatar 也应该存这个处理后的路径，以便与用户数据中 avatar 字段的期望格式一致
      basicForm.avatar = processImageUrl(newUploadedFullUrl);
      // 如果你希望basicForm.avatar存完整URL，然后由processImageUrl在显示时处理，那也可以，但要统一。
      // 从你之前的用户更新日志看，后端返回的data.avatar是 /images/default-avatar.png，这暗示后端可能期望一个相对路径。
      // 并且 saveUserData 中 avatarToSave 的逻辑也用了 processImageUrl 处理后的 avatarUrl.value 作为回退
      // 因此，这里将 basicForm.avatar 也设置为 processImageUrl 处理后的结果，以保持一致性。

      console.log('设置后的预览头像URL (avatarUrl.value):', avatarUrl.value);
      console.log('设置到表单的头像URL (basicForm.avatar):', basicForm.avatar);

      ElMessage.success('头像上传成功');

      // 可选：如果希望上传后立即保存整个用户profile，可以在这里调用
      // await saveUserData();
      // 但通常是用户编辑完所有信息后，点击"保存"按钮才统一保存
    } else {
      // 检查 response.data 是否存在，以及 message 是否为有效字符串
      const errorMessage = response.data && response.data.message ? response.data.message : '头像上传成功，但未能获取有效图片URL';
      console.error('上传响应问题:', errorMessage, response.data);
      ElMessage.error(errorMessage);
      // 即使上传成功但URL无效，也可能需要将 avatarUrl.value 和 basicForm.avatar 设为默认值或保持不变
    }
  } catch (error) {
    console.error('上传头像出错详情:', error);
    if (error.response) {
      console.error('响应状态:', error.response.status);
      console.error('响应数据:', error.response.data);
    } else if (error.request) {
      console.error('请求已发出但无响应:', error.request);
    } else {
      console.error('请求设置错误:', error.message);
    }
    ElMessage.error('上传头像失败，请检查网络或联系管理员');
  } finally {
    // 清空文件选择器，允许重新选择同一文件
    if(event.target) { // 确保 event.target 存在
      event.target.value = '';
    }
  }
};

// 触发营业执照上传
const triggerLicenseUpload = () => {
  licenseInput.value.click();
};

// 处理营业执照变更
const handleLicenseChange = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  // 验证文件类型和大小
  if (!file.type.includes('image/')) {
    ElMessage.error('请上传图片文件');
    return;
  }

  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过10MB');
    return;
  }

  // 如果文件较大，可以选择转换为base64直接在前端显示
  if (file.size < 2 * 1024 * 1024) { // 小于2MB的图片直接用base64显示
    const reader = new FileReader();
    reader.onload = async (e) => {
      const base64Data = e.target.result;
      // 显示base64数据
      businessLicenseUrl.value = base64Data;
      // 商家表单中存储base64数据
      storeForm.businessLicense = base64Data;
      ElMessage.success('营业执照上传成功');
      
      // 尝试立即保存营业执照更改
      if (merchantData.value && merchantData.value.id) {
        try {
          const updateData = {
            id: merchantData.value.id,
            businessLicense: base64Data
          };
          await merchantApi.updateMerchant(updateData);
          console.log('营业执照信息已自动保存');
        } catch (saveError) {
          console.error('自动保存营业执照信息失败:', saveError);
        }
      }
    };
    reader.readAsDataURL(file);
    // 清空文件选择器
    event.target.value = '';
    return;
  }
  
  // 继续原来的上传逻辑，适用于较大的文件
  const formData = new FormData();
  formData.append('file', file);
  
  try {
    console.log('开始上传营业执照文件:', file.name, '大小:', file.size, '类型:', file.type);
    const response = await fileApi.uploadFile(formData);
    console.log('营业执照上传响应:', response);
    
    if (response.data && response.data.code === 200) {
      // 确定获取的图片URL (可能在data或message中)
      let imageUrl = '';
      if (response.data.data) {
        imageUrl = response.data.data;
      } else if (response.data.message && typeof response.data.message === 'string' && 
                (response.data.message.startsWith('http') || response.data.message.startsWith('/'))) {
        imageUrl = response.data.message;
      }
      
      if (imageUrl) {
        console.log('获取到的营业执照URL:', imageUrl);
        // 使用处理过的URL进行显示
        businessLicenseUrl.value = imageUrl;
        // 商家表单中存储原始的完整URL
        storeForm.businessLicense = imageUrl;
        ElMessage.success('营业执照上传成功');
        
        // 尝试立即保存营业执照更改
        if (merchantData.value && merchantData.value.id) {
          try {
            const updateData = {
              id: merchantData.value.id,
              businessLicense: imageUrl
            };
            await merchantApi.updateMerchant(updateData);
            console.log('营业执照信息已自动保存');
          } catch (saveError) {
            console.error('自动保存营业执照信息失败:', saveError);
          }
        }
      } else {
        console.error('未能从响应中获取有效的图片URL:', response.data);
        ElMessage.error('营业执照上传成功，但未能获取图片地址');
      }
    } else {
      ElMessage.error(response.data.message || '营业执照上传失败');
    }
  } catch (error) {
    console.error('上传营业执照出错:', error);
    if (error.response) {
      console.error('服务器响应:', error.response.status, error.response.data);
    }
    ElMessage.error('上传营业执照失败，请稍后重试');
  }

  // 清空文件选择器
  event.target.value = '';
};

// 触发身份证上传
const triggerIdCardUpload = () => {
  idCardInput.value.click();
};

// 处理身份证变更
const handleIdCardChange = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  // 验证文件类型和大小
  if (!file.type.includes('image/')) {
    ElMessage.error('请上传图片文件');
    return;
  }

  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过10MB');
    return;
  }

  // 如果文件较大，可以选择转换为base64直接在前端显示
  if (file.size < 2 * 1024 * 1024) { // 小于2MB的图片直接用base64显示
    const reader = new FileReader();
    reader.onload = async (e) => {
      const base64Data = e.target.result;
      // 显示base64数据
      idCardUrl.value = base64Data;
      // 商家表单中存储base64数据
      storeForm.idCard = base64Data;
      ElMessage.success('身份证上传成功');
      
      // 尝试立即保存身份证更改
      if (merchantData.value && merchantData.value.id) {
        try {
          const updateData = {
            id: merchantData.value.id,
            idCard: base64Data
          };
          await merchantApi.updateMerchant(updateData);
          console.log('身份证信息已自动保存');
        } catch (saveError) {
          console.error('自动保存身份证信息失败:', saveError);
        }
      }
    };
    reader.readAsDataURL(file);
    // 清空文件选择器
    event.target.value = '';
    return;
  }

  // 上传文件
  const formData = new FormData();
  formData.append('file', file);

  try {
    console.log('开始上传身份证文件:', file.name, '大小:', file.size, '类型:', file.type);
    const response = await fileApi.uploadFile(formData);
    console.log('身份证上传响应:', response);
    
    if (response.data && response.data.code === 200) {
      // 确定获取的图片URL (可能在data或message中)
      let imageUrl = '';
      if (response.data.data) {
        imageUrl = response.data.data;
      } else if (response.data.message && typeof response.data.message === 'string' && 
                (response.data.message.startsWith('http') || response.data.message.startsWith('/'))) {
        imageUrl = response.data.message;
      }
      
      if (imageUrl) {
        console.log('获取到的身份证URL:', imageUrl);
        // 使用处理过的URL进行显示
        idCardUrl.value = imageUrl;
        // 商家表单中存储原始的完整URL
        storeForm.idCard = imageUrl;
        ElMessage.success('身份证上传成功');
        
        // 尝试立即保存身份证更改
        if (merchantData.value && merchantData.value.id) {
          try {
            const updateData = {
              id: merchantData.value.id,
              idCard: imageUrl
            };
            await merchantApi.updateMerchant(updateData);
            console.log('身份证信息已自动保存');
          } catch (saveError) {
            console.error('自动保存身份证信息失败:', saveError);
          }
        }
      } else {
        console.error('未能从响应中获取有效的图片URL:', response.data);
        ElMessage.error('身份证上传成功，但未能获取图片地址');
      }
    } else {
      ElMessage.error(response.data.message || '身份证上传失败');
    }
  } catch (error) {
    console.error('上传身份证出错:', error);
    if (error.response) {
      console.error('服务器响应:', error.response.status, error.response.data);
    }
    ElMessage.error('上传身份证失败，请稍后重试');
  }

  // 清空文件选择器
  event.target.value = '';
};

// 保存表单
const saveForm = async () => {
  // 根据当前激活的标签页验证表单
  if (activeTab.value === 'basic') {
    if (!basicFormRef.value) return;

    await basicFormRef.value.validate(async (valid) => {
      if (!valid) {
        ElMessage.error('请正确填写表单信息');
        return;
      }

      await saveUserData();
    });
  } else if (activeTab.value === 'store') {
    if (!storeFormRef.value) return;

    await storeFormRef.value.validate(async (valid) => {
      if (!valid) {
        ElMessage.error('请正确填写表单信息');
        return;
      }

      await saveStoreData();
    });
  }
};

// 保存用户数据
const saveUserData = async () => {
  if (!userData.value || !userData.value.id) {
    ElMessage.error('用户信息异常，无法保存');
    return;
  }

  isSaving.value = true;
  console.log('准备保存用户数据，avatar值为:', avatarUrl.value);
  console.log('表单中的avatar值为:', basicForm.avatar);

  // 确保头像URL不为undefined
  const avatarToSave = basicForm.avatar || avatarUrl.value || '';
  console.log('最终使用的头像URL:', avatarToSave);

  try {
    const updateUserData = {
      id: userData.value.id,
      realName: basicForm.realName,
      phone: basicForm.phone,
      email: basicForm.email,
      wechat: basicForm.wechat,
      defaultAddress: basicForm.defaultAddress,
      personalIntro: basicForm.personalIntro,
      avatar: avatarToSave
    };

    console.log('发送到后端的用户数据:', updateUserData);

    const response = await userApi.updateUserProfile(updateUserData);
    console.log('更新用户资料响应:', response);
    console.log('更新用户资料完整响应数据:', JSON.stringify(response.data));

    if (response.data.code === 200) {
      ElMessage.success('用户信息保存成功');

      // 更新localStorage中的用户头像信息
      const userJson = localStorage.getItem('user');
      if (userJson) {
        const user = JSON.parse(userJson);
        user.avatar = avatarToSave;
        console.log('准备更新本地存储的用户信息，新头像URL:', user.avatar);
        localStorage.setItem('user', JSON.stringify(user));

        // 触发存储事件，通知其他组件用户信息已更新
        window.dispatchEvent(new Event('storage'));
      }
    } else {
      console.error('保存失败，错误码:', response.data.code, '错误信息:', response.data.message);
      ElMessage.error(response.data.message || '保存失败');
    }
  } catch (error) {
    console.error('保存用户信息出错详情:', error);
    if (error.response) {
      console.error('响应状态:', error.response.status);
      console.error('响应数据:', error.response.data);
    }
    ElMessage.error('保存用户信息失败，请稍后重试');
  } finally {
    isSaving.value = false;
  }
};

// 保存商家数据
const saveStoreData = async () => {
  if (!merchantData.value || !merchantData.value.id) {
    ElMessage.error('商家信息异常，无法保存');
    return;
  }

  isSaving.value = true;

  try {
    const updateMerchantData = {
      id: merchantData.value.id,
      storeName: storeForm.storeName,
      description: storeForm.description,
      businessLicense: storeForm.businessLicense,
      idCard: storeForm.idCard
    };

    const response = await merchantApi.updateMerchant(updateMerchantData);
    if (response.data.code === 200) {
      ElMessage.success('商家信息保存成功');
    } else {
      ElMessage.error(response.data.message || '保存失败');
    }
  } catch (error) {
    console.error('保存商家信息出错:', error);
    ElMessage.error('保存商家信息失败，请稍后重试');
  } finally {
    isSaving.value = false;
  }
};

// 重置表单
const resetForm = () => {
  ElMessageBox.confirm('确定要取消当前的修改吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    fetchUserData();
    ElMessage.info('已重置表单');
  }).catch(() => {});
};

// 处理图片URL，支持base64格式和常规URL
const processImageUrl = (url) => {
  if (!url) return '/default-avatar.png';
  
  // 检查是否为base64格式
  if (typeof url === 'string' && url.startsWith('data:image/')) {
    // 直接返回base64数据，无需处理
    return url;
  }
  
  // 使用原有的fileApi.processImageUrl处理常规URL
  return fileApi.processImageUrl(url);
};

// 页面加载时获取数据
onMounted(async () => {
  // 获取用户数据
  await fetchUserData();

  // 处理图片URL
  if (userData.value && userData.value.avatar) {
    avatarUrl.value = processImageUrl(userData.value.avatar);
  }

  if (merchantData.value) {
    if (merchantData.value.businessLicense) {
      businessLicenseUrl.value = processImageUrl(merchantData.value.businessLicense);
    }

    if (merchantData.value.idCard) {
      idCardUrl.value = processImageUrl(merchantData.value.idCard);
    }
  }
});
</script>

<style scoped>
.merchant-profile-edit {
  padding: 40px 0;
  background-color: #f8f9fa;
  min-height: 100vh;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #333;
}

.page-header p {
  color: #666;
  font-size: 16px;
}

.form-container {
  margin-bottom: 40px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.avatar-container {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.avatar-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #ddd;
  cursor: pointer;
}

.avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-upload-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-wrapper:hover .avatar-upload-mask {
  opacity: 1;
}

.license-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

.document-preview {
  width: 200px;
  height: 150px;
  border: 1px dashed #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background-color: #f9f9f9;
}

.document-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.empty-document {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #aaa;
}

.empty-document i {
  font-size: 36px;
  margin-bottom: 8px;
}

.stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.stat-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  text-align: center;
  transition: transform 0.3s;
  border: 1px solid #eee;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.level-card {
  border-color: #4a6ee0;
}

.level-tag {
  color: #4a6ee0;
}
</style>
