// 导入所有API模块
import api from './index';
import { captchaApi, authApi } from './auth';
import { userApi } from './user';
import { productApi } from './product';
import { orderApi } from './order';
import { messageApi } from './message';
import { merchantApi } from './merchant';
import { fileApi } from './file';

// 统一导出
export {
  api,          // 基础axios实例
  captchaApi,   // 验证码相关
  authApi,      // 身份验证相关
  userApi,      // 用户相关
  productApi,   // 商品相关
  orderApi,     // 订单相关
  messageApi,   // 消息通知相关
  merchantApi,  // 商家相关
  fileApi       // 文件上传相关
}; 