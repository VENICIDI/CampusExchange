import request from '@/utils/request'

/**
 * 获取管理员仪表盘数据
 */
export function getDashboardStats() {
  return request({
    url: '/api/admin/dashboard/stats',
    method: 'get'
  })
}
