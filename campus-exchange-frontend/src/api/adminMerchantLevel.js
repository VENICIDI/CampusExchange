import request from '@/utils/request';

/**
 * 获取所有商家等级
 * @returns {Promise}
 */
export function getAllMerchantLevels() {
  return request({
    url: '/api/admin/merchant-levels',
    method: 'get'
  });
}

/**
 * 获取特定商家等级信息
 * @param {number} id - 商家等级ID
 * @returns {Promise}
 */
export function getMerchantLevelById(id) {
  return request({
    url: `/api/admin/merchant-levels/${id}`,
    method: 'get'
  });
}

/**
 * 添加商家等级
 * @param {Object} levelData - 商家等级数据
 * @returns {Promise}
 */
export function addMerchantLevel(levelData) {
  return request({
    url: '/api/admin/merchant-levels',
    method: 'post',
    data: levelData
  });
}

/**
 * 更新商家等级
 * @param {number} id - 商家等级ID
 * @param {Object} levelData - 更新的商家等级数据
 * @returns {Promise}
 */
export function updateMerchantLevel(id, levelData) {
  return request({
    url: `/api/admin/merchant-levels/${id}`,
    method: 'put',
    data: levelData
  });
}

/**
 * 更新商家等级的手续费率
 * @param {number} id - 商家等级ID
 * @param {number} rate - 手续费率 (0-1之间的小数)
 * @returns {Promise}
 */
export function updateCommissionRate(id, rate) {
  return request({
    url: `/api/admin/merchant-levels/${id}/commission-rate`,
    method: 'put',
    params: { rate }
  });
}

/**
 * 删除商家等级
 * @param {number} id - 商家等级ID
 * @returns {Promise}
 */
export function deleteMerchantLevel(id) {
  return request({
    url: `/api/admin/merchant-levels/${id}`,
    method: 'delete'
  });
} 