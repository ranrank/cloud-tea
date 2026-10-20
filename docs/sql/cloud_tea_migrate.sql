-- =============================================================
-- 云栖茶饮 · 连锁点单平台 —— 从旧库 sky_take_out 迁移
--
-- 前置：先执行 cloud_tea_schema.sql 建库建表
-- 执行：mysql -uroot -p < cloud_tea_migrate.sql
--
-- 特点：
--   1. 全程只读 sky_take_out，原库原封不动，可随时回退
--   2. 显式列映射，不用 SELECT *，表名/列名对应关系一目了然
--   3. 末尾自带行数比对，迁移是否完整一眼可见
-- =============================================================

USE cloud_tea;

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;

-- -------------------------------------------------------------
-- 1. 菜单：category → category，dish → drink，dish_flavor → drink_spec
-- -------------------------------------------------------------
INSERT INTO category (id, type, name, sort, status, create_time, update_time, create_user, update_user)
SELECT id, type, name, sort, status, create_time, update_time, create_user, update_user
FROM sky_take_out.category;

INSERT INTO drink (id, name, category_id, price, image, description, status, create_time, update_time, create_user, update_user)
SELECT id, name, category_id, price, image, description, status, create_time, update_time, create_user, update_user
FROM sky_take_out.dish;

INSERT INTO drink_spec (id, drink_id, spec_name, spec_values)
SELECT id, dish_id, name, `value`
FROM sky_take_out.dish_flavor;

-- -------------------------------------------------------------
-- 2. 组合套餐：setmeal → combo，setmeal_dish → combo_drink
-- -------------------------------------------------------------
INSERT INTO combo (id, category_id, name, price, status, description, image, create_time, update_time, create_user, update_user)
SELECT id, category_id, name, price, status, description, image, create_time, update_time, create_user, update_user
FROM sky_take_out.setmeal;

INSERT INTO combo_drink (id, combo_id, drink_id, name, price, copies)
SELECT id, setmeal_id, dish_id, name, price, copies
FROM sky_take_out.setmeal_dish;

-- -------------------------------------------------------------
-- 3. 交易：购物车 / 订单 / 订单明细
-- -------------------------------------------------------------
INSERT INTO shopping_cart (id, name, image, user_id, drink_id, combo_id, drink_spec, number, amount, create_time)
SELECT id, name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time
FROM sky_take_out.shopping_cart;

INSERT INTO order_detail (id, name, image, order_id, drink_id, combo_id, drink_spec, number, amount)
SELECT id, name, image, order_id, dish_id, setmeal_id, dish_flavor, number, amount
FROM sky_take_out.order_detail;

INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status,
                    amount, remark, phone, address, user_name, consignee, cancel_reason, rejection_reason,
                    cancel_time, estimated_delivery_time, delivery_status, delivery_time,
                    pack_amount, tableware_number, tableware_status)
SELECT id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status,
       amount, remark, phone, address, user_name, consignee, cancel_reason, rejection_reason,
       cancel_time, estimated_delivery_time, delivery_status, delivery_time,
       pack_amount, tableware_number, tableware_status
FROM sky_take_out.orders;

-- -------------------------------------------------------------
-- 4. 基础资料：用户 / 地址 / 员工（结构未变，直接搬）
-- -------------------------------------------------------------
INSERT INTO user (id, openid, name, phone, sex, id_number, avatar, create_time)
SELECT id, openid, name, phone, sex, id_number, avatar, create_time
FROM sky_take_out.user;

INSERT INTO address_book (id, user_id, consignee, sex, phone, province_code, province_name,
                          city_code, city_name, district_code, district_name, detail, label, is_default)
SELECT id, user_id, consignee, sex, phone, province_code, province_name,
       city_code, city_name, district_code, district_name, detail, label, is_default
FROM sky_take_out.address_book;

INSERT INTO employee (id, name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user)
SELECT id, name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user
FROM sky_take_out.employee;

SET FOREIGN_KEY_CHECKS = 1;

-- -------------------------------------------------------------
-- 5. 迁移结果校验：两边行数必须一致
-- -------------------------------------------------------------
SELECT 'category       ← category'     AS 表, (SELECT COUNT(*) FROM sky_take_out.category)     AS 原库, (SELECT COUNT(*) FROM cloud_tea.category)       AS 新库
UNION ALL SELECT 'drink        ← dish',        (SELECT COUNT(*) FROM sky_take_out.dish),         (SELECT COUNT(*) FROM cloud_tea.drink)
UNION ALL SELECT 'drink_spec   ← dish_flavor', (SELECT COUNT(*) FROM sky_take_out.dish_flavor),  (SELECT COUNT(*) FROM cloud_tea.drink_spec)
UNION ALL SELECT 'combo        ← setmeal',     (SELECT COUNT(*) FROM sky_take_out.setmeal),      (SELECT COUNT(*) FROM cloud_tea.combo)
UNION ALL SELECT 'combo_drink  ← setmeal_dish',(SELECT COUNT(*) FROM sky_take_out.setmeal_dish), (SELECT COUNT(*) FROM cloud_tea.combo_drink)
UNION ALL SELECT 'shopping_cart',              (SELECT COUNT(*) FROM sky_take_out.shopping_cart),(SELECT COUNT(*) FROM cloud_tea.shopping_cart)
UNION ALL SELECT 'order_detail',               (SELECT COUNT(*) FROM sky_take_out.order_detail), (SELECT COUNT(*) FROM cloud_tea.order_detail)
UNION ALL SELECT 'orders',                     (SELECT COUNT(*) FROM sky_take_out.orders),       (SELECT COUNT(*) FROM cloud_tea.orders)
UNION ALL SELECT 'address_book',               (SELECT COUNT(*) FROM sky_take_out.address_book), (SELECT COUNT(*) FROM cloud_tea.address_book)
UNION ALL SELECT 'employee',                   (SELECT COUNT(*) FROM sky_take_out.employee),     (SELECT COUNT(*) FROM cloud_tea.employee)
UNION ALL SELECT 'user',                       (SELECT COUNT(*) FROM sky_take_out.user),         (SELECT COUNT(*) FROM cloud_tea.user);

-- =============================================================
-- 注意：迁移过来的菜单仍是旧库的「菜品」数据，与茶饮业务对不上。
-- 想换成茶饮菜单（cloud_tea_seed.sql 里的 18 款饮品 + 4 个组合套餐），执行：
--
--   USE cloud_tea;
--   DELETE FROM drink_spec; DELETE FROM drink; DELETE FROM category;
--   DELETE FROM combo_drink; DELETE FROM combo;
--   ALTER TABLE category AUTO_INCREMENT = 1;
--   ALTER TABLE drink  AUTO_INCREMENT = 1;
--   -- 然后执行 cloud_tea_seed.sql 中「饮品分类」及之后的部分
--   -- （不要重复执行开头的员工账号 INSERT，会主键冲突）
--
-- 历史订单明细里存的是下单时的饮品名称快照，不受菜单替换影响。
-- =============================================================
