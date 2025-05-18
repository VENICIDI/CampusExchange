import request from '@/utils/request';

/**
 * 分类相关API
 */
export const categoryApi = {
  /**
   * 获取所有分类
   */
  getAllCategories() {
    return request({
      url: '/api/categories',
      method: 'get'
    });
  },

  /**
   * 获取顶级分类
   */
  getTopCategories() {
    return request({
      url: '/api/categories/top',
      method: 'get'
    });
  },

  /**
   * 获取子分类
   * @param {Number} parentId 父分类ID
   */
  getChildCategories(parentId) {
    return request({
      url: '/api/categories/children',
      method: 'get',
      params: { parentId }
    });
  },

  /**
   * 获取分类详情
   * @param {Number} id 分类ID
   */
  getCategoryById(id) {
    return request({
      url: `/api/categories/${id}`,
      method: 'get'
    });
  }
}; 