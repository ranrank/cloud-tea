-- =============================================================
-- 云栖茶饮 · 连锁点单平台 —— 数据库结构（仅结构，不含业务数据）
-- 适用：全新安装
-- 执行：mysql -uroot -p < cloud_tea_schema.sql
-- 后续：全新安装再执行 cloud_tea_seed.sql 灌入员工账号与茶饮菜单
--       若要从旧库导入历史数据，改执行 cloud_tea_migrate.sql
-- =============================================================

DROP DATABASE IF EXISTS cloud_tea;
CREATE DATABASE cloud_tea DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;
USE cloud_tea;

-- -------------------------------------------------------------
-- 员工
-- -------------------------------------------------------------
CREATE TABLE employee (
  id          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
  name        varchar(32) NOT NULL COMMENT '姓名',
  username    varchar(32) NOT NULL COMMENT '用户名',
  password    varchar(64) NOT NULL COMMENT '密码',
  phone       varchar(11) NOT NULL COMMENT '手机号',
  sex         varchar(2)  NOT NULL COMMENT '性别',
  id_number   varchar(18) NOT NULL COMMENT '身份证号',
  status      int         NOT NULL DEFAULT '1' COMMENT '状态 0:禁用，1:启用',
  create_time datetime             DEFAULT NULL COMMENT '创建时间',
  update_time datetime             DEFAULT NULL COMMENT '更新时间',
  create_user bigint               DEFAULT NULL COMMENT '创建人',
  update_user bigint               DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (id),
  UNIQUE KEY idx_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin COMMENT ='员工信息';

-- -------------------------------------------------------------
-- 用户（顾客）
-- -------------------------------------------------------------
CREATE TABLE user (
  id          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
  openid      varchar(45)          DEFAULT NULL COMMENT '微信用户唯一标识',
  name        varchar(32)          DEFAULT NULL COMMENT '姓名',
  phone       varchar(11)          DEFAULT NULL COMMENT '手机号',
  sex         varchar(2)           DEFAULT NULL COMMENT '性别',
  id_number   varchar(18)          DEFAULT NULL COMMENT '身份证号',
  avatar      varchar(500)         DEFAULT NULL COMMENT '头像',
  create_time datetime             DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin COMMENT ='用户信息';

-- -------------------------------------------------------------
-- 收货地址
-- -------------------------------------------------------------
CREATE TABLE address_book (
  id            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id       bigint       NOT NULL COMMENT '用户id',
  consignee     varchar(50)           DEFAULT NULL COMMENT '收货人',
  sex           varchar(2)            DEFAULT NULL COMMENT '性别',
  phone         varchar(11)  NOT NULL COMMENT '手机号',
  province_code varchar(12)           DEFAULT NULL COMMENT '省级区划编号',
  province_name varchar(32)           DEFAULT NULL COMMENT '省级名称',
  city_code     varchar(12)           DEFAULT NULL COMMENT '市级区划编号',
  city_name     varchar(32)           DEFAULT NULL COMMENT '市级名称',
  district_code varchar(12)           DEFAULT NULL COMMENT '区级区划编号',
  district_name varchar(32)           DEFAULT NULL COMMENT '区级名称',
  detail        varchar(200)          DEFAULT NULL COMMENT '详细地址',
  label         varchar(100)          DEFAULT NULL COMMENT '标签',
  is_default    tinyint(1)   NOT NULL DEFAULT '0' COMMENT '默认 0 否 1是',
  PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin COMMENT ='收货地址';

-- -------------------------------------------------------------
-- 饮品分类（菜单一级分类）
-- -------------------------------------------------------------
CREATE TABLE category (
  id          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
  type        int                  DEFAULT NULL COMMENT '类型 1 饮品分类 2 组合套餐分类',
  name        varchar(32) NOT NULL COMMENT '分类名称',
  sort        int         NOT NULL DEFAULT '0' COMMENT '顺序',
  status      int                  DEFAULT NULL COMMENT '分类状态 0:禁用，1:启用',
  create_time datetime             DEFAULT NULL COMMENT '创建时间',
  update_time datetime             DEFAULT NULL COMMENT '更新时间',
  create_user bigint               DEFAULT NULL COMMENT '创建人',
  update_user bigint               DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (id),
  UNIQUE KEY idx_category_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin COMMENT ='饮品分类';

-- -------------------------------------------------------------
-- 饮品
-- -------------------------------------------------------------
CREATE TABLE drink (
  id          bigint        NOT NULL AUTO_INCREMENT COMMENT '主键',
  name        varchar(32)   NOT NULL COMMENT '饮品名称',
  category_id   bigint        NOT NULL COMMENT '饮品分类id',
  price       decimal(10,2)          DEFAULT NULL COMMENT '饮品价格',
  image       varchar(255)           DEFAULT NULL COMMENT '图片',
  description varchar(255)           DEFAULT NULL COMMENT '描述信息',
  status      int                    DEFAULT '1' COMMENT '0 下架 1 上架',
  create_time datetime               DEFAULT NULL COMMENT '创建时间',
  update_time datetime               DEFAULT NULL COMMENT '更新时间',
  create_user bigint                 DEFAULT NULL COMMENT '创建人',
  update_user bigint                 DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (id),
  UNIQUE KEY idx_drink_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin COMMENT ='饮品';

-- -------------------------------------------------------------
-- 饮品规格（杯型 / 温度 / 糖度 / 加料，一条记录是一组规格）
-- -------------------------------------------------------------
CREATE TABLE drink_spec (
  id          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  drink_id    bigint       NOT NULL COMMENT '饮品id',
  spec_name   varchar(32)           DEFAULT NULL COMMENT '规格组名称，如 杯型 / 温度 / 糖度',
  spec_values varchar(255)          DEFAULT NULL COMMENT '规格值列表（JSON 数组，如 ["中杯","大杯"]）',
  PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin COMMENT ='饮品规格表';

-- -------------------------------------------------------------
-- 组合套餐
-- -------------------------------------------------------------
CREATE TABLE combo (
  id          bigint        NOT NULL AUTO_INCREMENT COMMENT '主键',
  category_id   bigint        NOT NULL COMMENT '组合套餐分类id',
  name        varchar(32)   NOT NULL COMMENT '组合套餐名称',
  price       decimal(10,2) NOT NULL COMMENT '组合套餐价格',
  status      int                    DEFAULT '1' COMMENT '售卖状态 0:下架 1:上架',
  description varchar(255)           DEFAULT NULL COMMENT '描述信息',
  image       varchar(255)           DEFAULT NULL COMMENT '图片',
  create_time datetime               DEFAULT NULL COMMENT '创建时间',
  update_time datetime               DEFAULT NULL COMMENT '更新时间',
  create_user bigint                 DEFAULT NULL COMMENT '创建人',
  update_user bigint                 DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (id),
  UNIQUE KEY idx_combo_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin COMMENT ='组合套餐';

-- -------------------------------------------------------------
-- 组合套餐饮品关系
-- -------------------------------------------------------------
CREATE TABLE combo_drink (
  id       bigint        NOT NULL AUTO_INCREMENT COMMENT '主键',
  combo_id bigint                 DEFAULT NULL COMMENT '组合套餐id',
  drink_id bigint                 DEFAULT NULL COMMENT '饮品id',
  name     varchar(32)            DEFAULT NULL COMMENT '饮品名称（冗余字段）',
  price    decimal(10,2)          DEFAULT NULL COMMENT '饮品单价（冗余字段）',
  copies   int                    DEFAULT NULL COMMENT '饮品份数',
  PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin COMMENT ='组合套餐饮品关系';

-- -------------------------------------------------------------
-- 购物车
-- -------------------------------------------------------------
CREATE TABLE shopping_cart (
  id          bigint        NOT NULL AUTO_INCREMENT COMMENT '主键',
  name        varchar(32)            DEFAULT NULL COMMENT '饮品名称',
  image       varchar(255)           DEFAULT NULL COMMENT '图片',
  user_id     bigint        NOT NULL COMMENT '用户id',
  drink_id    bigint                 DEFAULT NULL COMMENT '饮品id',
  combo_id    bigint                 DEFAULT NULL COMMENT '组合套餐id',
  drink_spec  varchar(50)            DEFAULT NULL COMMENT '规格快照，如 大杯/少冰/三分糖',
  number      int           NOT NULL DEFAULT '1' COMMENT '数量',
  amount      decimal(10,2) NOT NULL COMMENT '金额',
  create_time datetime               DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin COMMENT ='购物车';

-- -------------------------------------------------------------
-- 订单
-- -------------------------------------------------------------
CREATE TABLE orders (
  id                      bigint        NOT NULL AUTO_INCREMENT COMMENT '主键',
  number                  varchar(50)            DEFAULT NULL COMMENT '订单号',
  status                  int           NOT NULL DEFAULT '1' COMMENT '订单状态 1待付款 2待接单 3制作中 4配送中 5已完成 6已取消',
  order_type              tinyint       NOT NULL DEFAULT '2' COMMENT '就餐方式 1堂食 2外卖',
  pickup_no               varchar(10)            DEFAULT NULL COMMENT '取餐号，仅堂食有值，形如 A018',
  user_id                 bigint        NOT NULL COMMENT '下单用户',
  address_book_id         bigint                 DEFAULT NULL COMMENT '地址id（堂食到店自取，为 NULL）',
  order_time              datetime      NOT NULL COMMENT '下单时间',
  checkout_time           datetime               DEFAULT NULL COMMENT '结账时间',
  pay_method              int           NOT NULL DEFAULT '1' COMMENT '支付方式 1微信,2支付宝',
  pay_status              tinyint       NOT NULL DEFAULT '0' COMMENT '支付状态 0未支付 1已支付 2退款',
  amount                  decimal(10,2) NOT NULL COMMENT '实收金额',
  remark                  varchar(100)           DEFAULT NULL COMMENT '备注',
  phone                   varchar(11)            DEFAULT NULL COMMENT '手机号',
  address                 varchar(255)           DEFAULT NULL COMMENT '地址',
  user_name               varchar(32)            DEFAULT NULL COMMENT '用户名称',
  consignee               varchar(32)            DEFAULT NULL COMMENT '收货人',
  cancel_reason           varchar(255)           DEFAULT NULL COMMENT '订单取消原因',
  rejection_reason        varchar(255)           DEFAULT NULL COMMENT '订单拒绝原因',
  cancel_time             datetime               DEFAULT NULL COMMENT '订单取消时间',
  estimated_delivery_time datetime               DEFAULT NULL COMMENT '预计送达时间',
  delivery_status         tinyint(1)    NOT NULL DEFAULT '1' COMMENT '配送状态 1立即送出 0选择具体时间',
  delivery_time           datetime               DEFAULT NULL COMMENT '送达时间',
  pack_amount             int                    DEFAULT NULL COMMENT '打包费',
  tableware_number        int                    DEFAULT NULL COMMENT '餐具数量',
  tableware_status        tinyint(1)    NOT NULL DEFAULT '1' COMMENT '餐具数量状态 1按餐量提供 0选择具体数量',
  PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin COMMENT ='订单表';

-- -------------------------------------------------------------
-- 订单明细
-- -------------------------------------------------------------
CREATE TABLE order_detail (
  id         bigint        NOT NULL AUTO_INCREMENT COMMENT '主键',
  name       varchar(32)            DEFAULT NULL COMMENT '饮品名称',
  image      varchar(255)           DEFAULT NULL COMMENT '图片',
  order_id   bigint        NOT NULL COMMENT '订单id',
  drink_id   bigint                 DEFAULT NULL COMMENT '饮品id',
  combo_id   bigint                 DEFAULT NULL COMMENT '组合套餐id',
  drink_spec varchar(50)            DEFAULT NULL COMMENT '规格快照，如 大杯/少冰/三分糖',
  number     int           NOT NULL DEFAULT '1' COMMENT '数量',
  amount     decimal(10,2) NOT NULL COMMENT '金额',
  PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin COMMENT ='订单明细表';
