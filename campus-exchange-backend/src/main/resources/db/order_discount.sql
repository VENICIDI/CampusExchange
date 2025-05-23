-- 创建订单折扣表
CREATE TABLE `order_discount` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '折扣ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `discount_amount` decimal(10,2) NOT NULL COMMENT '折扣金额',
  `admin_id` bigint NOT NULL COMMENT '发放管理员ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_id` (`order_id`) COMMENT '一个订单只能有一个折扣'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单折扣表';

-- 修改订单表，添加折扣金额字段
ALTER TABLE `order` 
ADD COLUMN `discount_amount` decimal(10,2) DEFAULT '0.00' COMMENT '折扣金额' AFTER `points_deduction_amount`; 