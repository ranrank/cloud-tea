# 云栖茶饮 · 连锁点单平台

面向连锁茶饮门店的线上点单与门店经营管理系统。顾客在小程序/H5 端浏览菜单、选规格、下单支付；门店在管理端维护菜单、接单出杯、查看经营数据。

> 这是一个个人练习项目，用来把 Spring Boot 全栈开发的各个环节串起来：分层架构、缓存、实时推送、报表导出。

## 技术栈

| 层次 | 选型 |
|---|---|
| 框架 | Spring Boot 2.7、Spring MVC、MyBatis、PageHelper |
| 数据库 | MySQL 8（Druid 连接池） |
| 缓存 | Redis（菜单缓存、门店营业状态） |
| 实时通信 | WebSocket（来单提醒、客户催单） |
| 鉴权 | JWT（管理端 `token` / 用户端 `authentication` 双令牌） |
| 切面 | Spring AOP（公共字段自动填充 create_time / update_time 等） |
| 报表 | Apache POI（营业额 Excel 导出，基于模板填充） |
| 第三方 | 阿里云 OSS（图片存储）、百度地图开放平台（配送范围校验）、微信支付（已做模拟开关） |
| 接口文档 | Knife4j / Swagger |
| 前端 | Vue 3 + Vite + Element Plus + Pinia + ECharts（管理端）<br>Vue 3 + Vite + Vant（用户端 H5） |

## 模块划分

```
cloud-tea/
├── cloudtea-common/     通用层：常量、异常、JWT/OSS/微信支付工具、公共配置属性
├── cloudtea-pojo/       模型层：entity / dto / vo
├── cloudtea-server/     业务层：controller / service / mapper / config / websocket / task
├── cloud-tea-admin/     管理端前端（Vue3 + Element Plus）
├── cloud-tea-h5/        用户端 H5（Vue3 + Vant）
└── docs/sql/            数据库脚本
```

> 项目后端基于黑马程序员《苍穹外卖》课程工程改造，业务调整为连锁茶饮点单。

## 领域模型

| 概念 | 说明 |
|---|---|
| 分类 `category` | 菜单一级分类。`type=1` 饮品分类（经典奶茶/鲜果茶/纯茶/咖啡/奶盖茶），`type=2` 组合套餐分类 |
| 饮品 `drink` | 单品。每款饮品可配多组规格 |
| 规格 `drink_spec` | 规格组，一条记录一组。如 `杯型 → ["中杯","大杯"]`、`糖度 → ["无糖","三分糖","五分糖","七分糖","全糖"]` |
| 组合套餐 `combo` / `combo_drink` | 多款饮品打包售卖，价格通常低于单点之和 |
| 购物车 `shopping_cart` | 存服务端，按「饮品 + 规格」维度合并数量 |
| 订单 `orders` / `order_detail` | 两种就餐方式：**外卖**状态机 1待付款 → 2待接单 → 3制作中 → 4配送中 → 5已完成 / 6已取消；**堂食**没有配送环节，3制作中之后直接 5已完成 |

## 快速开始

### 1. 准备中间件

- **MySQL**：建库并灌入结构与菜单数据

```bash
mysql -uroot -p < docs/sql/cloud_tea_schema.sql
mysql -uroot -p < docs/sql/cloud_tea_seed.sql

# 可选：灌一批演示数据（120 用户 / 154 地址 / 378 订单，其中约 1/3 是堂食单），
# 数据统计页和工作台才有东西看。
# 时间用 CURDATE() 相对表达式生成，任何时候重跑都落在「最近 30 天」。
# 会先清空 user / address_book / orders / order_detail / shopping_cart 再重建，菜单和员工不受影响。
mysql -uroot -p --default-character-set=utf8mb4 < docs/sql/cloud_tea_demo_data.sql
```

> 演示数据脚本里的中文用 `utf8mb4` 连接字符集导入，否则 Windows 下默认字符集会把中文灌成乱码。
> 想换一批（比如用户数、订单量不同），改 `docs/sql/_gen_demo_data.py` 顶部的参数后重跑 `python docs/sql/_gen_demo_data.py` 重新生成。

> **已经建过库、要补上「堂食/外卖」这一版的**，跑这三条即可，老订单会自动落成外卖：
> ```sql
> ALTER TABLE orders ADD COLUMN order_type tinyint NOT NULL DEFAULT 2 COMMENT '就餐方式 1堂食 2外卖';
> ALTER TABLE orders ADD COLUMN pickup_no varchar(10) DEFAULT NULL COMMENT '取餐号，仅堂食有值，形如 A018';
> ALTER TABLE orders MODIFY COLUMN address_book_id bigint DEFAULT NULL COMMENT '地址id（堂食到店自取，为 NULL）';
> ```

- **Redis**：本地起一个即可（默认 `127.0.0.1:6379`，使用 db 10）

> 如果手上已有旧库要迁移，改跑 `docs/sql/cloud_tea_migrate.sql`，它只读旧库、不动原数据，末尾自带行数比对。

### 2. 改配置

公开版通过环境变量读取数据库密码、JWT 签名密钥和第三方服务凭证，变量清单见 [`.env.example`](.env.example)。
请在终端或 IDE 运行配置中设置这些变量；Spring Boot 不会自动读取 `.env.example` 或 `.env`。

```powershell
$env:MYSQL_PASSWORD = '你的本机数据库密码'
$env:JWT_ADMIN_SECRET = '请替换为随机生成的管理员签名密钥'
$env:JWT_USER_SECRET = '请替换为另一个随机签名密钥'
$env:ALIYUN_OSS_ACCESS_KEY_ID = '你的 OSS AccessKey ID'
$env:ALIYUN_OSS_ACCESS_KEY_SECRET = '你的 OSS AccessKey Secret'
$env:WECHAT_APP_ID = '你的微信 App ID（模拟模式可使用占位值）'
$env:WECHAT_SECRET = '你的微信 Secret（模拟模式可使用占位值）'
$env:BAIDU_MAP_AK = '你的百度地图服务端 AK'
```

OSS 图片上传和外卖配送范围校验需要相应真实凭证；微信登录/支付默认使用模拟模式。

### 3. 启动后端

```bash
mvn clean package -DskipTests
java -jar cloudtea-server/target/cloudtea-server-1.0-SNAPSHOT.jar
```

接口文档：<http://localhost:8080/doc.html>

### 4. 启动前端

```bash
# 管理端
cd cloud-tea-admin && npm install && npm run dev     # http://localhost:5173

# 用户端 H5
cd cloud-tea-h5 && npm install && npm run dev        # http://localhost:5174
```

两个前端都通过 Vite 代理把 `/api` 转发到 `localhost:8080` 并去掉前缀，所以后端不需要配 CORS。

### 5. 演示账号

| 端 | 账号 | 密码 |
|---|---|---|
| 管理端 | `admin` | `123456` |
| 用户端 H5 | 点「一键登录（演示模式）」即可 | — |

## 关键实现说明

**菜单缓存与失效**：用户端 `/user/drink/list` 把结果按 `drink_<分类id>` 缓存在 Redis，管理端增删改饮品时用 `redisTemplate.keys("drink_*")` 批量清理；组合套餐走 Spring Cache 的 `comboCache`，新增时按 `categoryId` 精准失效、其余操作 `allEntries` 全清。

> **改过 VO/DTO 的字段后要清一次 Redis**：`RedisTemplate` 的 value 序列化器是默认的 JDK 序列化，缓存里连着 `serialVersionUID` 一起存。类结构调整后旧缓存反序列化会抛 `InvalidClassException`，`/user/drink/list` 直接 500、用户端菜单空白。删掉 `drink_*` 这几个 key（或管理端随便改一次饮品触发 `cleanCache`）即可，缓存会从库里重建。

**公共字段自动填充**：`@AutoFill` 注解 + `AutoFillAspect`，在 INSERT/UPDATE 前统一补 `create_time`、`update_time`、`create_user`、`update_user`，业务代码里不用重复写。

**配送范围校验**：下单时把门店地址和收货地址分别做地理编码，再调百度地图路线规划拿驾车距离，超过 `cloudtea.store.max-delivery-distance` 则拒绝下单。**只有外卖单走这一步**，堂食顾客人就在店里，不查地址也不校验距离。

**堂食 / 外卖**：`orders.order_type` 区分（1堂食 2外卖，默认 2）。两条链路在下单时分开——外卖要地址簿 + 配送范围校验，堂食跳过这两步、`address_book_id`/`address`/`estimated_delivery_time` 全部落空。堂食会生成**取餐号**（`pickup_no`，形如 `A018`）：`OrderServiceImpl.nextPickupNo()` 复用 `OrderMapper.countByMap` 按「今天 + order_type=1」计数再 +1，所以每天从 A001 重新开始，取消单也占号不回收，和门店真实叫号一致；用订单表计数而不是单独维护计数器，重启或多实例都不会把号发重。状态流转上，堂食跳过「配送中」，管理端对堂食单只给「完成订单」、不给「配送」，后端 `delivery()` 也硬拦一道防止前端漏改。

**支付与登录的模拟开关**：`cloudtea.wechat.mock=true` 时，`WeChatPayUtil.pay()/refund()` 直接返回格式合法的模拟参数，`UserServiceImpl` 也用 `mock_<code>` 派生 openid 而不请求微信。拿到真实商户号和小程序后改成 `false` 即可切回。

## 已知注意点

- **LocalDateTime 入参与返回值格式不一致**：反序列化要求 `yyyy-MM-dd HH:mm:ss`（空格分隔、必须带秒），序列化输出 `yyyy-MM-dd HH:mm`。前端提交含时间的字段时要按入参格式发，写成 ISO 的 `T` 分隔会被 400 拒绝。
- `orders.status` 只有 6 个状态，没有独立的「已退款」状态，退款通过 `pay_status=2` 表达。
- 用户端订单列表接口返回的是 `orderDetailList`，管理端列表返回的是拼好的 `orderItems` 字符串——两边取数方式不同。
