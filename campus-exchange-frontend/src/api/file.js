import api from './index';
import axios from 'axios';

// 特殊处理的文件上传路径前缀
const BACKEND_URL = "http://localhost:8080"; 

// 文件上传相关接口
export const fileApi = {
  // 上传文件
  uploadFile: (formData) => {
    // 使用完整URL绕过Vite代理处理
    return axios.post(`${BACKEND_URL}/api/files/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  },
  
  // 上传多个文件
  uploadMultipleFiles: (formData) => api.post('/files/upload/multiple', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }),
  
  // 删除文件
  deleteFile: (fileUrl) => api.delete('/files/delete', { data: { url: fileUrl } }),
  
  // 处理图片URL，确保在前端正确显示
  processImageUrl: (url) => {
    if (!url) return ''; // 返回空字符串而非默认头像URL，避免404错误
    
    console.log('处理图片URL:', url);
    
    try {
      // 处理Base64格式图片数据
      if (typeof url === 'string' && url.startsWith('data:image/')) {
        console.log('  检测到base64格式图片数据，直接返回');
        return url; // 直接返回base64数据，无需处理
      }
      
      // 处理特殊情况：如果URL是完整的localhost URL
      if (typeof url === 'string' && url.includes('localhost:8080/api/')) {
        // 关键修改：从URL中提取相对路径，移除域名和端口部分
        const pathPart = url.split('localhost:8080')[1];
        console.log('  将本地URL转换为相对路径:', pathPart);
        return pathPart; // 返回类似 /api/static/... 的路径
      }
      
      // 处理完整URL
      if (typeof url === 'string' && (url.startsWith('http://') || url.startsWith('https://'))) {
        console.log('  保持完整URL不变:', url);
        return url;
      }
      
      // 处理相对路径
      if (typeof url === 'string' && url.startsWith('/')) {
        // 确保以/api开头
        const result = url.startsWith('/api') ? url : `/api${url}`;
        console.log('  相对路径处理结果:', result);
        return result;
      }
      
      // 处理不以/开头但包含static的路径
      if (typeof url === 'string' && url.includes('static/')) {
        const result = url.indexOf('/api/static/') === 0 ? url : 
                     url.startsWith('static/') ? `/api/${url}` : `/api/static/${url}`;
        console.log('  静态资源路径处理结果:', result);
        return result;
      }
      
      // 如果是短路径，可能需要拼接完整路径
      console.log('  默认处理结果:', `/api/static/${url}`);
      return typeof url === 'string' ? `/api/static/${url}` : url;
    } catch (error) {
      console.error('URL处理出错:', error, url);
      return '/images/default-avatar.png';
    }
  },
  
  // 专门处理商品图片URL的方法
  processProductImageUrl: (url) => {
    if (!url) return '/images/no-image.png';
    
    console.log('处理商品图片URL:', url);
    
    try {
      // 处理Base64格式图片数据
      if (typeof url === 'string' && url.startsWith('data:image/')) {
        console.log('  检测到base64格式图片数据，直接返回');
        return url; // 直接返回base64数据，无需处理
      }
      
      // 处理特殊情况：如果URL是完整的localhost URL
      if (typeof url === 'string' && url.includes('localhost:8080/api/static/')) {
        // 提取相对路径部分
        const pathPart = url.split('localhost:8080')[1];
        console.log('  将本地URL转换为相对路径:', pathPart);
        // 使用相对路径，让浏览器通过当前域名访问
        return pathPart;
      }
      
      // 如果已经是相对路径且包含/api/static/
      if (typeof url === 'string' && url.includes('/api/static/')) {
        console.log('  已经是正确格式的相对路径:', url);
        return url;
      }
      
      // 如果是其他格式的完整URL，不变
      if (typeof url === 'string' && (url.startsWith('http://') || url.startsWith('https://'))) {
        // 排除placeholder URL，避免网络问题
        if (url.includes('placeholder.com')) {
          return '/images/no-image.png';
        }
        console.log('  完整URL保持不变:', url);
        return url;
      }
      
      // 处理其他情况，尝试构建正确的URL
      const result = typeof url === 'string' ? 
        (url.startsWith('/') ? (url.startsWith('/api') ? url : `/api${url}`) : `/api/static/${url}`) :
        '/images/no-image.png';
      
      console.log('  最终处理结果:', result);
      return result;
    } catch (error) {
      console.error('商品图片URL处理出错:', error, url);
      return '/images/no-image.png';
    }
  }
}; 