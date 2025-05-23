import api from './index';

/**
 * 钱包API文档
 * 
 * 后端需要实现以下API路径:
 * 1. GET  /api/wallet/info - 获取用户钱包信息
 *    响应格式: { code: 200, message: "success", data: { balance: 100.00, frozenAmount: 0, points: 500 } }
 * 
 * 2. POST /api/wallet/recharge - 钱包充值
 *    请求参数: { amount: 100 }
 *    响应格式: { code: 200, message: "success", data: { balance: 200.00 } }
 * 
 * 3. POST /api/wallet/withdraw - 钱包提现
 *    请求参数: { amount: 50 }
 *    响应格式: { code: 200, message: "success", data: { balance: 150.00 } }
 * 
 * 4. GET  /api/wallet/transactions - 获取钱包交易记录
 *    请求参数: ?page=1&size=10
 *    响应格式: { 
 *      code: 200, 
 *      message: "success", 
 *      data: { 
 *        records: [
 *          { 
 *            id: 1, 
 *            createTime: "2024-01-01T12:00:00", 
 *            type: "RECHARGE", // 类型: RECHARGE(充值), WITHDRAW(提现), PAYMENT(支付), REFUND(退款), REWARD(奖励), COMMISSION(佣金)
 *            amount: 100.00, // 正数表示收入，负数表示支出
 *            balanceAfter: 200.00, 
 *            description: "充值" 
 *          }
 *        ],
 *        total: 10,
 *        pages: 1,
 *        current: 1,
 *        size: 10
 *      } 
 *    }
 * 
 * 5. POST /api/wallet/wallet-pay - 使用钱包支付订单
 *    请求参数: { orderNo: "ORD123456" }
 *    响应格式: { code: 200, message: "success", data: { orderNo: "ORD123456", status: "PAID" } }
 * 
 * 6. POST /api/wallet/init - 初始化用户钱包
 *    响应格式: { code: 200, message: "success", data: { balance: 0.00, frozenAmount: 0, points: 0 } }
 */

/**
 * 获取用户钱包信息
 * @returns {Promise} 钱包信息（余额、积分等）
 */
export const getUserWalletAPI = () => {
  return api.get('/wallet/info');
};

/**
 * 初始化用户钱包
 * @returns {Promise} 初始化结果
 */
export const initUserWalletAPI = () => {
  return api.post('/wallet/init');
};

/**
 * 钱包充值
 * @param {number} amount - 充值金额
 * @returns {Promise} 充值结果
 */
export const rechargeWalletAPI = (amount) => {
  return api.post('/wallet/recharge', { amount });
};

/**
 * 钱包提现
 * @param {number} amount - 提现金额
 * @returns {Promise} 提现结果
 */
export const withdrawWalletAPI = (amount) => {
  return api.post('/wallet/withdraw', { amount });
};

/**
 * 获取钱包交易记录
 * @param {number} page - 页码
 * @param {number} size - 每页数量
 * @param {string} type - 交易记录类型，可选值：BALANCE（余额）, POINTS（积分）
 * @returns {Promise} 钱包交易记录
 */
export const getWalletTransactionsAPI = (page = 1, size = 10, type = 'BALANCE') => {
  return api.get('/wallet/transactions', { params: { page, size, type } });
};

/**
 * 使用钱包支付订单
 * @param {string} orderNo - 订单号
 * @returns {Promise} 支付结果
 */
export const payOrderWithWalletAPI = (orderNo) => {
  return api.post('/wallet/wallet-pay', { orderNo });
}; 