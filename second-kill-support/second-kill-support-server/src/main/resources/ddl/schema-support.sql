-- seckill-support 模块建表SQL
-- 数据库: demo
-- 所有表归属 seckill-support 服务管理

-- 用户表
create table if not exists t_user (
    id bigint auto_increment comment '自增id' primary key,
    username varchar(50) not null comment '用户名',
    password varchar(100) not null comment '密码(BCrypt加密)',
    phone varchar(20) not null comment '手机号',
    nickname varchar(50) default '' not null comment '昵称',
    avatar varchar(500) default '' not null comment '头像URL',
    member_level tinyint not null default 0 comment '会员等级 0:普通 1:银卡 2:金卡 3:黑卡',
    status tinyint not null default 0 comment '账号状态 0:正常 1:禁用',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    create_by varchar(50) default '' not null comment '创建者',
    update_by varchar(50) default '' not null comment '更新者',
    is_deleted int default 0 not null comment '软删除状态,0:正常,1:已被软删除',
    unique key uk_username (username),
    unique key uk_phone (phone)
) engine=InnoDB default charset=utf8mb4 comment='用户表';

-- 主订单表(秒杀订单同步过来)
create table if not exists t_order (
    id bigint auto_increment comment '自增id' primary key,
    order_no varchar(32) not null comment '订单号',
    user_id bigint not null comment '用户ID',
    order_source varchar(20) not null default 'SECKILL' comment '订单来源 SECKILL:秒杀',
    total_amount decimal(10,2) not null comment '总金额',
    discount_amount decimal(10,2) not null default 0 comment '优惠金额',
    pay_amount decimal(10,2) not null comment '实付金额',
    order_status tinyint not null default 0 comment '订单状态 0:待支付 1:已支付 2:已取消 3:已关闭 4:已完成',
    paid_time datetime default null comment '支付时间',
    completed_time datetime default null comment '完成时间',
    transaction_no varchar(64) default null comment '支付流水号',
    remark varchar(200) default '' not null comment '订单备注',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    create_by varchar(50) default '' not null comment '创建者',
    update_by varchar(50) default '' not null comment '更新者',
    is_deleted int default 0 not null comment '软删除状态,0:正常,1:已被软删除',
    unique key uk_order_no (order_no),
    key idx_user_id (user_id),
    key idx_order_status (order_status)
) engine=InnoDB default charset=utf8mb4 comment='主订单表';

-- 支付流水表
create table if not exists t_payment (
    id bigint auto_increment comment '自增id' primary key,
    payment_no varchar(32) not null comment '支付单号',
    order_no varchar(32) not null comment '关联订单号',
    user_id bigint not null comment '用户ID',
    pay_amount decimal(10,2) not null comment '支付金额',
    pay_channel varchar(20) not null default 'WECHAT' comment '支付渠道',
    pay_status tinyint not null default 0 comment '支付状态 0:待支付 1:支付成功 2:支付失败 3:已退款',
    paid_time datetime default null comment '支付成功时间',
    transaction_no varchar(64) default null comment '三方支付流水号',
    notify_time datetime default null comment '回调通知时间',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    create_by varchar(50) default '' not null comment '创建者',
    update_by varchar(50) default '' not null comment '更新者',
    is_deleted int default 0 not null comment '软删除状态,0:正常,1:已被软删除',
    unique key uk_payment_no (payment_no),
    key idx_order_no (order_no),
    key idx_user_id (user_id)
) engine=InnoDB default charset=utf8mb4 comment='支付流水表';

-- 自由卡表
create table if not exists t_free_card (
    id bigint auto_increment comment '自增id' primary key,
    card_no varchar(32) not null comment '卡号',
    card_name varchar(100) not null comment '卡名称',
    face_value decimal(10,2) not null comment '面值',
    user_id bigint not null comment '所属用户ID',
    order_no varchar(32) not null comment '来源订单号',
    status tinyint not null default 0 comment '状态 0:未激活 1:已激活 2:已冻结 3:已过期',
    valid_days int default 365 comment '有效天数，从激活之日起算',
    activated_time datetime default null comment '激活时间',
    expire_time datetime default null comment '过期时间',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    create_by varchar(50) default '' not null comment '创建者',
    update_by varchar(50) default '' not null comment '更新者',
    is_deleted int default 0 not null comment '软删除状态,0:正常,1:已被软删除',
    unique key uk_card_no (card_no),
    key idx_user_id (user_id),
    key idx_order_no (order_no)
) engine=InnoDB default charset=utf8mb4 comment='自由卡表';

-- 风控记录表
create table if not exists t_risk_record (
    id bigint auto_increment comment '自增id' primary key,
    user_id bigint not null comment '用户ID',
    action_type varchar(20) not null comment '行为类型 SECKILL:秒杀 LOGIN:登录',
    risk_level tinyint not null default 0 comment '风控等级 0:正常 1:可疑 2:高风险',
    request_ip varchar(50) default '' not null comment '请求IP',
    request_info varchar(500) default '' not null comment '请求附加信息',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    create_by varchar(50) default '' not null comment '创建者',
    update_by varchar(50) default '' not null comment '更新者',
    is_deleted int default 0 not null comment '软删除状态,0:正常,1:已被软删除',
    key idx_user_id (user_id),
    key idx_action_type (action_type)
) engine=InnoDB default charset=utf8mb4 comment='风控记录表';
