import api from './index';

// 消息通知相关接口
export const messageApi = {
  // 获取消息列表
  getMessages: (params) => api.get('/messages', { params }),
  
  // 获取未读消息数量
  getUnreadCount: () => api.get('/messages/unread/count'),
  
  // 标记消息为已读
  markAsRead: (id) => api.put(`/messages/${id}/read`),
  
  // 标记所有消息为已读
  markAllAsRead: () => api.put('/messages/read/all'),
  
  // 删除消息
  deleteMessage: (id) => api.delete(`/messages/${id}`),
  
  // 获取聊天列表
  getChatList: () => api.get('/chats'),
  
  // 获取与特定用户的聊天记录
  getChatHistory: (userId, params) => api.get(`/chats/${userId}`, { params }),
  
  // 发送消息
  sendMessage: (receiverId, messageData) => api.post(`/chats/${receiverId}`, messageData)
}; 