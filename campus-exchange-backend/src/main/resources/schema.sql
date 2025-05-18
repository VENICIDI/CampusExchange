-- 1. 用户表 (user)
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID (主键)',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名 (唯一)',
  `password` VARCHAR(255) NOT NULL COMMENT '加密后的密码哈希值',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `phone` VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号 (登录标识, 唯一)',
  `email` VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱 (唯一)',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
  `gender` ENUM('FEMALE', 'MALE', 'UNKNOWN') DEFAULT 'UNKNOWN' COMMENT '性别：FEMALE-女，MALE-男, UNKNOWN-未知',
  `bank_account` VARCHAR(20) DEFAULT NULL COMMENT '银行账号（16数字）',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `role` ENUM('USER', 'MERCHANT', 'ADMIN') NOT NULL DEFAULT 'USER' COMMENT '角色：USER-普通用户，MERCHANT-商家，ADMIN-管理员',
  `status` ENUM('PENDING', 'NORMAL', 'DISABLED') NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待审核，NORMAL-正常，DISABLED-禁用',
  `default_address` VARCHAR(500) DEFAULT NULL COMMENT '默认收货地址', -- 新增：默认收货地址
  `personal_intro` VARCHAR(500) DEFAULT NULL COMMENT '个人介绍',
  `wechat` VARCHAR(50) DEFAULT NULL COMMENT '微信号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_username` (`username`),
  INDEX `idx_phone` (`phone`),
  INDEX `idx_email` (`email`),
  INDEX `idx_role_status` (`role`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- 2. 商家信息表 (merchant)
CREATE TABLE `merchant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商家ID (主键)',
  `user_id` BIGINT NOT NULL COMMENT '关联的用户ID (外键)',
  `business_license` TEXT NOT NULL COMMENT '营业执照图片URL',
  `id_card` TEXT NOT NULL COMMENT '身份证图片URL',
  `store_name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
  `level_id` BIGINT NOT NULL COMMENT '商家等级ID (外键, 关联merchant_level表)', -- 修改: 关联等级表ID
  `total_sales_count` INT NOT NULL DEFAULT 0 COMMENT '总销量（商品件数）', -- 修改: 更明确为件数
  `total_sales_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '总销售额',
  `store_positive_rate` DECIMAL(5,2) NOT NULL DEFAULT 100.00 COMMENT '店铺好评率 (基于商家服务评价)', -- 修改: 更明确来源
  `description` TEXT DEFAULT NULL COMMENT '店铺描述/公告',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  INDEX `idx_level_id` (`level_id`),
  CONSTRAINT `fk_merchant_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_merchant_level` FOREIGN KEY (`level_id`) REFERENCES `merchant_level` (`id`) -- 新增外键
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家信息表';


-- 修改merchant表中的字段类型，以支持更大的图片数据（Base64编码）[修改使用的，已经改完了]
-- ALTER TABLE merchant 
-- MODIFY COLUMN business_license MEDIUMTEXT NOT NULL COMMENT '营业执照图片URL',
-- MODIFY COLUMN id_card MEDIUMTEXT NOT NULL COMMENT '身份证图片URL';

-- 3. 商家等级配置表 (merchant_level)
CREATE TABLE `merchant_level` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '等级ID (主键)',
  `level_name` VARCHAR(50) NOT NULL UNIQUE COMMENT '等级名称 (如: 一级商家)', -- 修改: 更通用的名称
  `commission_rate` DECIMAL(5,4) NOT NULL COMMENT '手续费率 (如: 0.0100 代表 1%)',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '等级描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家等级配置表';

-- 4. 商品分类表 (category)
CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID (主键)',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父分类ID',
  `level` INT NOT NULL DEFAULT 1 COMMENT '分类层级',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_parent_id` (`parent_id`),
  CONSTRAINT `fk_category_parent` FOREIGN KEY (`parent_id`) REFERENCES `category` (`id`) ON DELETE SET NULL -- 修改: 父分类删除时设为NULL，避免级联删除子分类
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类表';

-- 5. 商品表 (product)
CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID (主键)',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID (外键)',
  `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `category_id` BIGINT NOT NULL COMMENT '分类ID (外键)',
  `original_price` DECIMAL(10,2) NOT NULL COMMENT '原价',
  `current_price` DECIMAL(10,2) NOT NULL COMMENT '现价/折扣价',
  `description` TEXT DEFAULT NULL COMMENT '商品描述',
  `condition_desc` ENUM('NEW', 'LIKE_NEW', 'GOOD', 'FAIR', 'POOR') NOT NULL COMMENT '新旧程度：NEW-全新，LIKE_NEW-九成新，GOOD-八成新，FAIR-七成新，POOR-六成新及以下', -- 字段名修改
  `negotiable` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否可议价：0-否，1-是',
  `stock` INT NOT NULL DEFAULT 1 COMMENT '库存数量',
  `sales_count` INT NOT NULL DEFAULT 0 COMMENT '销量（件数）', -- 字段名修改
  `size_info` VARCHAR(100) DEFAULT NULL COMMENT '商品尺寸', -- 字段名修改
  `usage_instructions` TEXT DEFAULT NULL COMMENT '使用说明',
  `status` ENUM('PENDING_APPROVAL', 'REJECTED_RESUBMIT', 'ON_SALE', 'LOCKED', 'SOLD_OUT', 'REMOVED_BY_SELLER') NOT NULL DEFAULT 'PENDING_APPROVAL' COMMENT '状态：PENDING_APPROVAL-待审核, REJECTED_RESUBMIT-审核不通过可修改, ON_SALE-在售, LOCKED-已锁定, SOLD_OUT-已售罄, REMOVED_BY_SELLER-商家下架', -- 修改: 状态枚举更新
  `average_rating` DECIMAL(2,1) DEFAULT 5.0 COMMENT '平均评分 (基于商品评价)', -- 修改: 更明确来源
  `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间 (审核通过时间)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_merchant_id` (`merchant_id`),
  INDEX `idx_category_id` (`category_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_name` (`name`(50)), -- 增加name索引长度限制
  INDEX `idx_current_price` (`current_price`),
  INDEX `idx_average_rating` (`average_rating`),
  CONSTRAINT `fk_product_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE RESTRICT -- 修改: 分类被删除时，若有商品关联则阻止删除
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品表';

-- 6. 商品图片表 (product_image)
CREATE TABLE `product_image` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID (主键)',
  `product_id` BIGINT NOT NULL COMMENT '商品ID (外键)',
  `image_url` VARCHAR(255) NOT NULL COMMENT '图片URL',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号', -- 字段名修改
  `is_main` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主图：0-否，1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_product_id_sort` (`product_id`, `sort_order`), -- 修改: 组合索引
  CONSTRAINT `fk_image_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品图片表';

-- 7. 购物车表 (cart) - 保持原样，一个用户一个购物车实例
CREATE TABLE `cart` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '购物车ID (主键)',
  `user_id` BIGINT NOT NULL COMMENT '用户ID (外键)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购物车表';

-- 8. 购物车项表 (cart_item)
CREATE TABLE `cart_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '购物车项ID (主键)',
  `cart_id` BIGINT NOT NULL COMMENT '购物车ID (外键)',
  `product_id` BIGINT NOT NULL COMMENT '商品ID (外键)',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '商品数量', -- 新增: 商品数量
  `selected` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否选中下单：0-否，1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cart_product` (`cart_id`,`product_id`),
  INDEX `idx_product_id` (`product_id`),
  CONSTRAINT `fk_cart_item_cart` FOREIGN KEY (`cart_id`) REFERENCES `cart` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cart_item_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购物车项表';

-- 9. 订单表 (order)
CREATE TABLE `order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID (主键)',
  `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号 (唯一)',
  `user_id` BIGINT NOT NULL COMMENT '用户ID (买家, 外键)',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID (外键)', -- 新增: 商家ID
  `total_product_amount` DECIMAL(10,2) NOT NULL COMMENT '订单商品总金额 (优惠前, 所有订单项价格*数量之和)', -- 新增: 商品原始总价
  `points_used` INT NOT NULL DEFAULT 0 COMMENT '使用的积分数量',
  `points_deduction_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '积分抵扣金额', -- 字段名修改
  `actual_payment_amount` DECIMAL(10,2) NOT NULL COMMENT '实际支付金额 (商品总金额 - 积分抵扣)', -- 字段名修改
  `platform_commission_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '平台手续费总额', -- 新增
  `status` ENUM('PENDING_PAYMENT', 'PENDING_SHIPMENT', 'SHIPPED', 'RECEIVED', 'COMPLETED', 'CANCELLED', 'RETURN_REQUESTED', 'RETURN_APPROVED', 'RETURN_GOODS_RECEIVED', 'RETURNED', 'RETURN_REJECTED') NOT NULL DEFAULT 'PENDING_PAYMENT' COMMENT '订单状态', -- 修改: 状态枚举更新
  `trade_type` ENUM('EXPRESS', 'OFFLINE') NOT NULL DEFAULT 'EXPRESS' COMMENT '交易方式：EXPRESS-快递，OFFLINE-线下交易',
  `offline_meeting_location` VARCHAR(255) DEFAULT NULL COMMENT '线下交易地点', -- 字段名修改
  `offline_meeting_time` DATETIME DEFAULT NULL COMMENT '线下交易时间', -- 字段名修改
  `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间 (模拟)', -- 字段名修改
  `shipping_time` DATETIME DEFAULT NULL COMMENT '发货时间', -- 字段名修改
  `receipt_confirmation_time` DATETIME DEFAULT NULL COMMENT '确认收货时间', -- 字段名修改
  `completion_time` DATETIME DEFAULT NULL COMMENT '订单完成时间', -- 字段名修改
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id_status` (`user_id`, `status`), -- 修改: 组合索引
  INDEX `idx_merchant_id_status` (`merchant_id`, `status`), -- 新增: 商家订单索引
  INDEX `idx_status_createtime` (`status`, `create_time`), -- 新增: 状态和创建时间索引，用于定时任务
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT, -- 修改: 用户删除时，若有订单则阻止
  CONSTRAINT `fk_order_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE RESTRICT -- 新增: 商家删除时，若有订单则阻止
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';

-- 10. 订单项表 (order_item)
CREATE TABLE `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单项ID (主键)',
  `order_id` BIGINT NOT NULL COMMENT '订单ID (外键)',
  `product_id` BIGINT NOT NULL COMMENT '商品ID (外键)',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID (冗余, 便于查询)',
  `product_name_snapshot` VARCHAR(100) NOT NULL COMMENT '商品名称快照', -- 字段名修改
  `product_image_snapshot` VARCHAR(255) DEFAULT NULL COMMENT '商品主图快照', -- 字段名修改
  `price_at_purchase` DECIMAL(10,2) NOT NULL COMMENT '购买时商品单价', -- 字段名修改
  `quantity` INT NOT NULL COMMENT '购买数量', -- 新增: 购买数量
  `item_total_amount` DECIMAL(10,2) NOT NULL COMMENT '该项商品总金额 (单价*数量)', -- 新增
  `commission_rate_snapshot` DECIMAL(5,4) NOT NULL COMMENT '下单时手续费率快照', -- 字段名修改
  `commission_amount_snapshot` DECIMAL(10,2) NOT NULL COMMENT '该项商品产生的手续费金额快照', -- 字段名修改
  `review_status` ENUM('NOT_REVIEWED', 'REVIEWED') NOT NULL DEFAULT 'NOT_REVIEWED' COMMENT '商品评价状态：NOT_REVIEWED-未评价，REVIEWED-已评价',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_product_id` (`product_id`),
  INDEX `idx_merchant_id` (`merchant_id`),
  CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_order_item_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT -- 修改: 商品被删除时，若订单项关联则阻止
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单项表';

-- 11. 钱包表 (wallet)
CREATE TABLE `wallet` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '钱包ID (主键)',
  `user_id` BIGINT NOT NULL COMMENT '用户ID (外键)',
  `balance` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '可用余额',
  `frozen_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '冻结金额 (可选, 用于支付中间态或提现)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `fk_wallet_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='钱包表';

-- 12. 钱包交易记录表 (wallet_transaction)
CREATE TABLE `wallet_transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '交易ID (主键)',
  `wallet_id` BIGINT NOT NULL COMMENT '钱包ID (外键)',
  `related_order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID', -- 字段名修改
  `amount` DECIMAL(12,2) NOT NULL COMMENT '交易金额 (正为入账, 负为出账)',
  `type` ENUM('RECHARGE', 'CONSUMPTION', 'REFUND', 'MERCHANT_INCOME', 'PLATFORM_FEE', 'SYSTEM_ADJUSTMENT') NOT NULL COMMENT '交易类型：RECHARGE-充值，CONSUMPTION-消费，REFUND-退款，MERCHANT_INCOME-商家收入，PLATFORM_FEE-平台手续费支出，SYSTEM_ADJUSTMENT-系统调整', -- 修改: 类型更明确
  `description` VARCHAR(255) DEFAULT NULL COMMENT '交易描述',
  `balance_after_transaction` DECIMAL(12,2) NOT NULL COMMENT '交易后钱包可用余额', -- 字段名修改
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_wallet_id_type` (`wallet_id`, `type`), -- 修改: 组合索引
  INDEX `idx_related_order_id` (`related_order_id`),
  INDEX `idx_create_time` (`create_time`),
  CONSTRAINT `fk_transaction_wallet` FOREIGN KEY (`wallet_id`) REFERENCES `wallet` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_transaction_order` FOREIGN KEY (`related_order_id`) REFERENCES `order`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='钱包交易记录表';

-- 13. 积分账户表 (points_account) -- 表名修改
CREATE TABLE `points_account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '积分账户ID (主键)',
  `user_id` BIGINT NOT NULL COMMENT '用户ID (外键)',
  `total_points` INT NOT NULL DEFAULT 0 COMMENT '积分余额', -- 字段名修改
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `fk_points_account_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分账户表';

-- 14. 积分交易记录表 (points_transaction)
CREATE TABLE `points_transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '交易ID (主键)',
  `points_account_id` BIGINT NOT NULL COMMENT '积分账户ID (外键)', -- 字段名修改
  `related_order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID', -- 字段名修改
  `points_change` INT NOT NULL COMMENT '积分变动数量 (正为获得, 负为消耗)', -- 字段名修改
  `type` ENUM('PURCHASE_EARNED', 'ORDER_DEDUCTION_USED', 'SYSTEM_REWARD', 'SYSTEM_DEDUCTION', 'REFUND_RETURNED') NOT NULL COMMENT '交易类型：PURCHASE_EARNED-消费获得，ORDER_DEDUCTION_USED-订单抵扣使用，SYSTEM_REWARD-系统奖励，SYSTEM_DEDUCTION-系统扣减, REFUND_RETURNED-退款返还积分', -- 修改: 类型更新
  `description` VARCHAR(255) DEFAULT NULL COMMENT '交易描述',
  `balance_after_transaction` INT NOT NULL COMMENT '交易后积分余额', -- 字段名修改
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_points_account_id_type` (`points_account_id`, `type`), -- 修改: 组合索引
  INDEX `idx_related_order_id` (`related_order_id`),
  INDEX `idx_create_time` (`create_time`),
  CONSTRAINT `fk_points_transaction_account` FOREIGN KEY (`points_account_id`) REFERENCES `points_account` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_points_transaction_order` FOREIGN KEY (`related_order_id`) REFERENCES `order`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分交易记录表';

-- 15. 商品评价表 (product_review)
DROP TABLE IF EXISTS `product_review`;
CREATE TABLE `product_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID (主键)',
  `order_item_id` BIGINT NOT NULL COMMENT '订单项ID (外键, 唯一确定评价对象)',
  `user_id` BIGINT NOT NULL COMMENT '评价用户ID (买家)',
  `product_id` BIGINT NOT NULL COMMENT '商品ID (冗余)',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID (冗余)',
  `rating_score` TINYINT NOT NULL COMMENT '评分：1-5星',
  `content` VARCHAR(1000) DEFAULT NULL COMMENT '评价内容',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_item_id` (`order_item_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_product_id` (`product_id`),
  INDEX `idx_merchant_id` (`merchant_id`),
  CONSTRAINT `fk_product_review_order_item`
          FOREIGN KEY (`order_item_id`) REFERENCES `order_item` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_product_review_user`
          FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_product_review_product`
          FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_product_review_merchant`
          FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品评价表';

-- 16. 商家服务评价表 (merchant_service_review)
DROP TABLE IF EXISTS `merchant_service_review`;
CREATE TABLE `merchant_service_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID (主键)',
  `order_id` BIGINT NOT NULL COMMENT '订单ID (外键)',
  `user_id` BIGINT NOT NULL COMMENT '评价用户ID (买家)',
  `merchant_id` BIGINT NOT NULL COMMENT '被评价商家ID',
  `service_attitude_rating` TINYINT NOT NULL COMMENT '服务态度评分：1-5星',
  `content` VARCHAR(1000) DEFAULT NULL COMMENT '评价内容',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_merchant_user` (`order_id`, `merchant_id`, `user_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_merchant_id` (`merchant_id`),
  CONSTRAINT `fk_merchant_review_order`
          FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_merchant_review_user`
          FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_merchant_review_merchant`
          FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家服务评价表';
-- 17. 买家评价表 (由商家发起) (buyer_review_by_merchant) -- 表名修改
CREATE TABLE `buyer_review_by_merchant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID (主键)',
  `order_id` BIGINT NOT NULL COMMENT '订单ID (外键)',
  `buyer_user_id` BIGINT NOT NULL COMMENT '被评价买家用户ID', -- 字段名修改
  `merchant_id` BIGINT NOT NULL COMMENT '评价商家ID',
  `rating_score` TINYINT NOT NULL COMMENT '评分：1-5星', -- 字段名修改
  `content` VARCHAR(1000) DEFAULT NULL COMMENT '评价内容',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_buyer_merchant` (`order_id`,`buyer_user_id`,`merchant_id`), -- 一个商家对一个订单中的一个买家只能评价一次
  INDEX `idx_buyer_user_id` (`buyer_user_id`),
  INDEX `idx_merchant_id` (`merchant_id`),
  CONSTRAINT `fk_buyer_review_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_buyer_review_buyer` FOREIGN KEY (`buyer_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE, -- 买家被删除，其被评价记录也删除
  CONSTRAINT `fk_buyer_review_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='买家评价表 (由商家发起)';

-- 18. 退货申请表 (return_request)
CREATE TABLE `return_request` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '退货申请ID (主键)',
  `order_id` BIGINT NOT NULL COMMENT '订单ID (外键, 针对整个订单退货)',
  `user_id` BIGINT NOT NULL COMMENT '申请用户ID (买家)',
  `merchant_id` BIGINT NOT NULL COMMENT '对应商家ID', -- 新增: 商家ID
  `reason` VARCHAR(500) NOT NULL COMMENT '退货原因',
  `status` ENUM('PENDING_APPROVAL', 'APPROVED_PENDING_RETURN', 'GOODS_RECEIVED_PENDING_REFUND', 'COMPLETED_REFUNDED', 'REJECTED') NOT NULL DEFAULT 'PENDING_APPROVAL' COMMENT '退货状态: PENDING_APPROVAL-待商家审核, APPROVED_PENDING_RETURN-同意待退货, GOODS_RECEIVED_PENDING_REFUND-收到货待退款, COMPLETED_REFUNDED-已完成退款, REJECTED-已拒绝', -- 修改: 状态枚举更新
  `rejection_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因 (若被拒绝)', -- 字段名修改
  `application_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间', -- 字段名修改
  `audit_time` DATETIME DEFAULT NULL COMMENT '商家审核时间',
  `goods_returned_time` DATETIME DEFAULT NULL COMMENT '买家退回商品时间 (物流信息或线下确认)',
  `refund_completed_time` DATETIME DEFAULT NULL COMMENT '退款完成时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`), -- 一个订单只能发起一次退货申请
  INDEX `idx_user_id_status` (`user_id`, `status`),
  INDEX `idx_merchant_id_status` (`merchant_id`, `status`),
  CONSTRAINT `fk_return_request_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_return_request_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_return_request_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='退货申请表';

-- 19. 黑名单表 (user_blacklist) -- 表名修改
CREATE TABLE `user_blacklist` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '黑名单ID (主键)',
  `user_id` BIGINT NOT NULL COMMENT '被拉黑用户ID',
  `scope` ENUM('PLATFORM', 'MERCHANT_SPECIFIC') NOT NULL DEFAULT 'MERCHANT_SPECIFIC' COMMENT '拉黑范围: PLATFORM-平台级, MERCHANT_SPECIFIC-商家级', -- 新增: 拉黑范围
  `operator_id` BIGINT NOT NULL COMMENT '操作者ID (管理员或商家用户ID)',
  `operator_role` ENUM('ADMIN', 'MERCHANT') NOT NULL COMMENT '操作者角色', -- 新增
  `target_merchant_id` BIGINT DEFAULT NULL COMMENT '目标商家ID (当scope为MERCHANT_SPECIFIC时填写)',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '拉黑原因',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否生效：1-是，0-否', -- 字段名修改
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_scope_target` (`user_id`, `scope`, `target_merchant_id`),
  INDEX `idx_user_id_active` (`user_id`, `is_active`),
  INDEX `idx_target_merchant_id` (`target_merchant_id`),
  CONSTRAINT `