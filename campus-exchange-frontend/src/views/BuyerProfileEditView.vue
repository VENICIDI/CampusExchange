<template>
  <div class="buyer-profile-edit">
    <div class="container">
      <div class="page-header">
        <h1>个人信息管理</h1>
        <p>您可以在此页面修改个人资料</p>
      </div>

      <el-card class="form-container" v-loading="isLoading">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
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
            <el-input v-model="form.username"></el-input>
          </el-form-item>

          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" disabled></el-input>
            <small>真实姓名已经过审核，无法修改</small>
          </el-form-item>

          <el-form-item label="手机号码" prop="phone">
            <el-input v-model="form.phone"></el-input>
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email"></el-input>
          </el-form-item>

          <el-form-item label="微信号" prop="wechat">
            <el-input v-model="form.wechat"></el-input>
          </el-form-item>

          <el-form-item label="所在城市" prop="city">
            <el-input v-model="form.city"></el-input>
          </el-form-item>

          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="form.gender">
              <el-radio :value="'MALE'">男</el-radio>
              <el-radio :value="'FEMALE'">女</el-radio>
              <el-radio :value="'UNKNOWN'">不公开</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="收货地址" prop="defaultAddress">
            <el-input v-model="form.defaultAddress"></el-input>
          </el-form-item>

          <el-form-item label="个人介绍" prop="personalIntro">
            <el-input type="textarea" v-model="form.personalIntro" :rows="4"></el-input>
          </el-form-item>

          <div class="form-actions">
            <el-button @click="resetForm">取消修改</el-button>
            <el-button type="primary" @click="saveForm" :loading="isSaving">保存修改</el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { userApi, fileApi } from '@/api/all'

// 路由和状态
const router = useRouter()
const isLoading = ref(false)
const isSaving = ref(false)

// 表单引用
const formRef = ref(null)

// 用户数据
const userData = ref(null)

// 表单数据
const form = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  wechat: '',
  city: '',
  gender: 'UNKNOWN',
  defaultAddress: '',
  personalIntro: '',
  avatar: '',
})

// 图片预览URL
const avatarUrl = ref('');

// 文件上传引用
const avatarInput = ref(null)

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
}

// 获取用户数据
const fetchUserData = async () => {
  isLoading.value = true;
  try {
    // 获取当前用户信息
    const userResponse = await userApi.getUserProfile();
    if (userResponse.data.code === 200) {
      userData.value = userResponse.data.data;

      // 填充表单
      form.username = userData.value.username || '';
      form.realName = userData.value.realName || '';
      form.phone = userData.value.phone || '';
      form.email = userData.value.email || '';
      form.wechat = userData.value.wechat || '';
      form.city = userData.value.city || '';
      form.gender = userData.value.gender || 'UNKNOWN';
      form.defaultAddress = userData.value.defaultAddress || '';
      form.personalIntro = userData.value.personalIntro || '';
      form.avatar = userData.value.avatar || '';

      // 设置头像URL
      if (userData.value.avatar) {
        avatarUrl.value = userData.value.avatar;
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

    if (response.data && response.data.code === 200 && typeof response.data.message === 'string' && response.data.message.startsWith('http')) {
      const newUploadedFullUrl = response.data.message;
      console.log('成功从后端获取新头像的完整URL:', newUploadedFullUrl);

      // 直接使用后端返回的完整URL，不做任何处理，确保与商家版本一致
      avatarUrl.value = newUploadedFullUrl;
      form.avatar = newUploadedFullUrl;

      console.log('设置后的预览头像URL (avatarUrl.value):', avatarUrl.value);
      console.log('设置到表单的头像URL (form.avatar):', form.avatar);

      ElMessage.success('头像上传成功');
    } else {
      const errorMessage = response.data && response.data.message ? response.data.message : '头像上传成功，但未能获取有效图片URL';
      console.error('上传响应问题:', errorMessage, response.data);
      ElMessage.error(errorMessage);
    }
  } catch (error) {
    console.error('上传头像出错详情:', error);
    ElMessage.error('上传头像失败，请检查网络或联系管理员');
  } finally {
    // 清空文件选择器，允许重新选择同一文件
    if(event.target) {
      event.target.value = '';
    }
  }
};

// 保存表单
const saveForm = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.error('请正确填写表单信息');
      return;
    }

    await saveUserData();
  });
};

// 保存用户数据
const saveUserData = async () => {
  if (!userData.value || !userData.value.id) {
    ElMessage.error('用户信息异常，无法保存');
    return;
  }

  isSaving.value = true;
  console.log('准备保存用户数据，avatar值为:', avatarUrl.value);
  console.log('表单中的avatar值为:', form.avatar);

  // 确保头像URL不为undefined
  const avatarToSave = form.avatar || avatarUrl.value || '';
  console.log('最终使用的头像URL:', avatarToSave);
  
  // 检查是否为完整URL，如果是，提取相对路径部分
  let processedAvatarUrl = avatarToSave;
  if (processedAvatarUrl && processedAvatarUrl.includes('localhost:8080')) {
    processedAvatarUrl = processedAvatarUrl.split('localhost:8080')[1];
    console.log('处理后的头像URL (提取相对路径):', processedAvatarUrl);
  }

  try {
    const updateUserData = {
      id: userData.value.id,
      username: form.username,
      realName: form.realName,
      phone: form.phone,
      email: form.email,
      wechat: form.wechat,
      city: form.city,
      gender: form.gender,
      defaultAddress: form.defaultAddress,
      personalIntro: form.personalIntro,
      avatar: processedAvatarUrl
    };

    console.log('发送到后端的用户数据:', updateUserData);
    console.log('发送到后端的头像URL:', updateUserData.avatar);
    console.log('发送到后端的性别值:', updateUserData.gender);
    console.log('发送到后端的城市值:', updateUserData.city);

    const response = await userApi.updateUserProfile(updateUserData);
    console.log('更新用户资料响应:', response);
    console.log('更新用户资料完整响应数据:', JSON.stringify(response.data));

    if (response.data && response.data.code === 200) {
      ElMessage.success('用户信息保存成功');

      // 更新localStorage中的用户头像信息
      const userJson = localStorage.getItem('user');
      if (userJson) {
        const user = JSON.parse(userJson);
        user.avatar = processedAvatarUrl;
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
    ElMessage.error('保存用户信息失败，请稍后重试');
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
  if (!url) return ''; // 返回空字符串而非默认头像URL，避免404错误
  
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
});
</script>

<style scoped>
.buyer-profile-edit {
  padding: 40px 0;
  background-color: #f8f9fa;
  min-height: 100vh;
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 16px;
  color: #666;
}

.form-container {
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background-color: #fff;
}

.avatar-container {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
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
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-wrapper:hover .avatar-upload-mask {
  opacity: 1;
}

.avatar-upload-mask i {
  font-size: 24px;
  margin-bottom: 5px;
}

.avatar-upload-mask span {
  font-size: 14px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
  gap: 15px;
}

/* 适配移动设备 */
@media (max-width: 768px) {
  .container {
    padding: 0 15px;
  }
  
  .form-container {
    padding: 20px;
  }
  
  .form-actions {
    flex-direction: column;
    gap: 10px;
  }
  
  .form-actions button {
    width: 100%;
  }
}
</style> 