# 商家个人信息页（店铺主页）设计方案

## 一、数据来源与字段梳理

### 1. 商家基础信息（merchant表）
- 店铺名称：`store_name`
- 店铺公告/简介：`description`
- 店铺好评率：`store_positive_rate`
- 总销量：`total_sales_count`
- 总销售额：`total_sales_amount`
- 商家等级ID：`level_id`
- 营业执照、身份证图片：`business_license`、`id_card`
- 创建时间：`create_time`
- 更新时间：`update_time`

### 2. 商家等级（merchant_level表）
- 等级名称：`level_name`
- 手续费率：`commission_rate`
- 等级描述：`description`

### 3. 商家头像
- merchant表没有头像字段，通过`user_id`外键，关联user表的`avatar`字段获取头像。

### 4. 联系方式
- 通过user表的`phone`、`wechat`、`email`等字段获取。

### 5. 商品列表
- 通过product表，筛选`merchant_id`为当前商家的商品。

### 6. 评价
- 商品评价：`product_review`表
- 商家服务评价：`merchant_service_review`表

---

## 二、页面内容结构建议

### 顶部区域（店铺名片）
- 店铺头像（user.avatar）
- 店铺名称（merchant.store_name）
- 商家等级（merchant_level.level_name）
- 店铺好评率、总销量、总销售额
- 联系方式（可选，视权限展示）
- 店铺公告/简介

### 主要内容区
- 在售商品列表（product.status=ON_SALE）
- 商家评价（好评率、评价列表）
- 商家认证信息（营业执照、身份证图片，管理员或本人可见）

### 其它
- 店铺创建时间
- "进入商家中心"按钮（如果是本人登录）

---

## 三、页面美观建议
- 顶部用卡片式设计，头像圆形，店铺名大号字体，等级用徽章/标签展示。
- 商品列表用卡片或瀑布流。
- 评价区可用评分星星+评价内容。
- 公告/简介用信息块展示。

---

## 四、后端接口设计建议

### 1. 获取商家主页信息
- 输入：商家ID（merchant.id）
- 输出：包含商家基础信息、等级信息、用户头像、联系方式、商品列表、评价等

### 2. 主要SQL/查询思路
- merchant表 join user表（取头像、联系方式）
- merchant表 join merchant_level表（取等级信息）
- product表（取商品列表）
- merchant_service_review表（统计好评率、取评价内容）

---

## 五、与商家中心的衔接
- "商家中心"是商家自己的后台管理页（如商品管理、订单管理、资料编辑等）。
- "商家主页/个人信息页"是面向所有用户的公开展示页（如淘宝店铺首页），可从商品卡片、平台商家列表等入口进入。
- 两者可通过"进入商家中心"按钮（仅本人可见）互相跳转。

---

## 六、下一步计划
1. 后端：实现获取商家主页信息的接口（聚合merchant、user、merchant_level、product、评价等信息）。
2. 前端：实现商家主页页面，美观展示上述信息。
3. 商品卡片/商品详情页增加"进入店铺"按钮，跳转到商家主页。

如有补充需求请随时告知，否则将按此思路推进。 