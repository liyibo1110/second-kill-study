-- seckill-base 模块建表SQL
-- 数据库: demo
-- 所有表归属 seckill-base 服务管理

-- 秒杀活动表
create table if not exists sk_activity (
    id bigint auto_increment comment '自增id' primary key,
    activity_no varchar(32) not null comment '活动编号',
    activity_name varchar(50) not null comment '活动名称',
    start_time datetime not null comment '活动开始时间',
    end_time datetime not null comment '活动结束时间',
    effective_type tinyint not null default 0 comment '生效时段类型 0:不限 1:部分时段',
    effective_days varchar(20) default '' not null comment '生效星期,1-7逗号隔开',
    effective_start time default null comment '每日生效开始时间',
    effective_end time default null comment '每日生效结束时间',
    activity_status tinyint not null default 0 comment '活动状态 0:待开始 1:进行中 2:已暂停 3:已结束',
    purchase_limit int not null default 0 comment '每人限购次数,0表示不限',
    remark varchar(500) default '' not null comment '活动说明',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    create_by varchar(50) default '' not null comment '创建者',
    update_by varchar(50) default '' not null comment '更新者',
    is_deleted int default 0 not null comment '软删除状态,0:正常,1:已被软删除',
    unique key uk_activity_no (activity_no),
    key idx_activity_status (activity_status)
    ) engine=InnoDB default charset=utf8mb4 comment='秒杀活动表';

-- 活动商品关联表(管理端配置)
create table if not exists sk_activity_product (
    id bigint auto_increment comment '自增id' primary key,
    activity_no varchar(32) not null comment '活动编号',
    product_name varchar(100) not null comment '商品名称',
    product_image varchar(500) default '' not null comment '商品图片URL',
    original_price decimal(10,2) not null comment '商品原价',
    discount_type tinyint not null default 0 comment '优惠方式 0:固定价格 1:固定折扣',
    discount_price decimal(10,2) default null comment '优惠价格(固定价格时使用)',
    sort_order int not null default 0 comment '排序序号',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    create_by varchar(50) default '' not null comment '创建者',
    update_by varchar(50) default '' not null comment '更新者',
    is_deleted int default 0 not null comment '软删除状态,0:正常,1:已被软删除',
    key idx_activity_no (activity_no)
    ) engine=InnoDB default charset=utf8mb4 comment='活动商品关联表';

-- 活动SKU配置表(管理端配置)
create table if not exists sk_activity_product_sku (
    id bigint auto_increment comment '自增id' primary key,
    activity_no varchar(32) not null comment '活动编号',
    product_id bigint not null comment '关联sk_activity_product.id',
    sku_no varchar(32) not null comment 'SKU编号',
    activity_stock int not null comment '活动库存',
    discount_type tinyint not null default 0 comment '优惠方式 0:固定价格 1:固定折扣',
    discount_percent decimal(4,2) default null comment '折扣比例(折)',
    discount_price decimal(10,2) default null comment '秒杀价',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    create_by varchar(50) default '' not null comment '创建者',
    update_by varchar(50) default '' not null comment '更新者',
    is_deleted int default 0 not null comment '软删除状态,0:正常,1:已被软删除',
    key idx_activity_no (activity_no),
    key idx_product_id (product_id),
    key idx_sku_no (sku_no)
    ) engine=InnoDB default charset=utf8mb4 comment='活动SKU配置表';

-- 秒杀商品运行时表(活动上线后从配置表生成)
create table if not exists sk_product (
    id bigint auto_increment comment '自增id' primary key,
    activity_no varchar(32) not null comment '活动编号',
    product_name varchar(100) not null comment '商品名称',
    product_image varchar(500) default '' not null comment '商品图片URL',
    sku_no varchar(32) not null comment 'SKU编号',
    original_price decimal(10,2) not null comment '原价',
    seckill_price decimal(10,2) not null comment '秒杀价',
    total_stock int not null comment '总库存',
    available_stock int not null comment '可用库存',
    limit_quantity int not null default 1 comment '单人限购数量',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    create_by varchar(50) default '' not null comment '创建者',
    update_by varchar(50) default '' not null comment '更新者',
    is_deleted int default 0 not null comment '软删除状态,0:正常,1:已被软删除',
    unique key uk_sku_no (sku_no),
    key idx_activity_no (activity_no)
    ) engine=InnoDB default charset=utf8mb4 comment='秒杀商品运行时表';

-- 秒杀订单表(user_id%4分片, 共4张物理表)
create table if not exists sk_order_0 (
    id bigint auto_increment comment '自增id' primary key,
    order_no varchar(32) not null comment '订单号',
    user_id bigint not null comment '用户ID',
    activity_no varchar(32) not null comment '活动编号',
    total_amount decimal(10,2) not null comment '订单总金额',
    discount_amount decimal(10,2) not null default 0 comment '优惠金额',
    pay_amount decimal(10,2) not null comment '实付金额',
    order_status tinyint not null default 0 comment '订单状态 0:待支付 1:已支付 2:已取消 3:已关闭 4:已完成',
    paid_time datetime default null comment '支付时间',
    closed_time datetime default null comment '关闭时间',
    transaction_no varchar(64) default null comment '支付流水号',
    trace_id varchar(64) default null comment '秒杀追踪ID',
    remark varchar(200) default '' not null comment '订单备注',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    create_by varchar(50) default '' not null comment '创建者',
    update_by varchar(50) default '' not null comment '更新者',
    is_deleted int default 0 not null comment '软删除状态,0:正常,1:已被软删除',
    unique key uk_order_no (order_no),
    key idx_user_id (user_id),
    key idx_activity_no (activity_no),
    key idx_order_status (order_status),
    key idx_create_time (create_time)
    ) engine=InnoDB default charset=utf8mb4 comment='秒杀订单表';

create table if not exists sk_order_1 like sk_order_0;
create table if not exists sk_order_2 like sk_order_0;
create table if not exists sk_order_3 like sk_order_0;

-- 秒杀订单项表(跟随order分片, 共4张物理表)
create table if not exists sk_order_item_0 (
    id bigint auto_increment comment '自增id' primary key,
    order_no varchar(32) not null comment '订单号',
    activity_no varchar(32) not null comment '活动编号',
    sku_no varchar(32) not null comment 'SKU编号',
    product_name varchar(100) not null comment '商品名称',
    quantity int not null comment '购买数量',
    price decimal(10,2) not null comment '成交单价',
    total_amount decimal(10,2) not null comment '该项总金额',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    create_by varchar(50) default '' not null comment '创建者',
    update_by varchar(50) default '' not null comment '更新者',
    is_deleted int default 0 not null comment '软删除状态,0:正常,1:已被软删除',
    key idx_order_no (order_no),
    key idx_sku_no (sku_no)
    ) engine=InnoDB default charset=utf8mb4 comment='秒杀订单项表';

create table if not exists sk_order_item_1 like sk_order_item_0;
create table if not exists sk_order_item_2 like sk_order_item_0;
create table if not exists sk_order_item_3 like sk_order_item_0;
