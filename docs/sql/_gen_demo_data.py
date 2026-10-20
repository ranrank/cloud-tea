# -*- coding: utf-8 -*-
"""生成「云栖茶饮」的演示数据 SQL（最近 30 天的用户与订单）。

从库里读真实菜单，按统计接口的口径造数据：
  - 营业额 sumByMap：orders.status=5 且按 order_time 过滤
  - 销量 Top10 getSalesTop10：order_detail join orders(status=5)
  - 用户统计 countByMap：user.create_time 落在区间内
时间全部写成 CURDATE() 的相对表达式，脚本任何时候重跑都落在最近 30 天。
"""
import io
import os
import json
import random
import subprocess

DB = os.environ.get("MYSQL_DATABASE", "cloud_tea")


def q(sql):
    r = subprocess.run(
        ["mysql", "-h", os.environ.get("MYSQL_HOST", "localhost"), "-P", os.environ.get("MYSQL_PORT", "3306"), "-u", os.environ.get("MYSQL_USERNAME", "root"), "--default-character-set=utf8mb4", "-N", "-B", "-e", sql, DB],
        env={**os.environ, "MYSQL_PWD": os.environ["MYSQL_PASSWORD"]},
        capture_output=True, text=True, encoding="utf-8")
    if r.returncode:
        raise SystemExit(r.stderr)
    return [l.split("\t") for l in r.stdout.strip().split("\n") if l]


drinks = [(int(r[0]), r[1], float(r[2])) for r in q("select id,name,price from drink where status=1")]
combos = [(int(r[0]), r[1], float(r[2])) for r in q("select id,name,price from combo where status=1")]
specs = {}
for did, sname, svals in q("select drink_id,spec_name,spec_values from drink_spec order by id"):
    specs.setdefault(int(did), []).append(json.loads(svals))
print("菜单读到: %d 饮品 / %d 组合套餐 / %d 组规格" % (len(drinks), len(combos), sum(len(v) for v in specs.values())))

# 销量梯度：靠前的更热门，Top10 图才有层次
pop = [max(1, 26 - i) for i in range(len(drinks))]

SURNAME = "王李张刘陈杨黄赵吴周徐孙马朱胡林郭何高罗郑梁谢宋唐许韩冯邓曹彭曾肖田董潘袁蔡蒋余于杜叶程魏苏吕丁"
GIVEN = ["雨桐","子墨","思远","梓涵","一鸣","欣怡","浩然","若曦","嘉航","沐辰","雨欣","之谦",
         "晓彤","书瑶","天佑","静怡","博文","语彤","俊杰","佳琪","明轩","诗涵","宇轩","梦琪",
         "志远","晨曦","逸凡","书豪","悦然","宇泽","念安","景行","亦菲","南风","清和","知微"]
STREETS = ["中关村大街","知春路","学院路","建国路","望京街","西直门外大街","回龙观东大街","安定路"]
DISTRICTS = ["海淀区","朝阳区","西城区","东城区","丰台区","昌平区"]
REMARKS = ["少放冰", "不要吸管", "打包分开装", "多加珍珠", "杯盖拧紧一点", "放前台自取", ""]

random.seed(20260917)

users = []
addrs = []
aid = 1001

# 注册时间分布：越近注册越多。按天给权重（今天最重），再按权重把用户铺到 30 天里，
# 这样统计页的「用户总量」是条上翘的增长曲线，「新增用户」也不会是恒定值。
N_USERS = 120
_w = {d: (30 - d) + 12 for d in range(30)}        # d=0 是今天，权重 42；d=29 是 30 天前，权重 13
_seq = list(range(29, -1, -1))                    # 从最早到今天
_tot = float(sum(_w[d] for d in _seq))
_cum, _acc = [], 0.0
for _d in _seq:
    _acc += _w[_d]
    _cum.append(_acc / _tot)
reg_day = [next(d for d, c in zip(_seq, _cum) if c >= (i + 0.5) / N_USERS)
           for i in range(N_USERS)]

for i in range(N_USERS):
    uid = 101 + i
    name = random.choice(SURNAME) + random.choice(GIVEN)
    users.append((uid, name))
    for k in range(random.choice([1, 1, 1, 2])):
        addrs.append(dict(id=aid, user_id=uid, consignee=name, sex=random.choice("10"),
                          phone="138%08d" % random.randint(0, 99999999),
                          district=random.choice(DISTRICTS),
                          detail="%s%d号" % (random.choice(STREETS), random.randint(1, 200)),
                          label=random.choice(["家", "公司", "学校"]),
                          is_default=1 if k == 0 else 0))
        aid += 1

# 就餐方式：奶茶店堂食占比不低，按约 35% 堂食 / 65% 外卖铺
DINE_IN_RATE = 0.35

orders = []
details = []
odid = 1
oid = 0
for d in range(29, -1, -1):
    weekday = (29 - d) % 7
    busy = 1.35 if weekday in (4, 5, 6) else 1.0
    n = 9 if d == 0 else int(random.randint(7, 15) * busy)
    day_orders = []
    for _ in range(n):
        uid, uname = random.choice(users)
        candidates = [a for a in addrs if a["user_id"] == uid] or addrs[:1]
        addr = random.choice(candidates)

        # 出杯高峰：10-11 / 14-16 / 19-21
        hour = random.choice([9, 10, 10, 11, 11, 12, 13, 14, 14, 15, 15, 16, 17, 18, 19, 19, 20, 20, 21, 21, 22])
        mins = hour * 60 + random.randint(0, 59)

        order_type = 1 if random.random() < DINE_IN_RATE else 2

        if d >= 1:
            status = 5 if random.random() < 0.90 else 6
        else:
            r = random.random()
            status = 5 if r < 0.45 else (2 if r < 0.65 else (3 if r < 0.82 else (4 if r < 0.92 else 1)))
            # 堂食没有配送环节，今天的在途堂食单只能停在制作中
            if order_type == 1 and status == 4:
                status = 3

        items = []
        for _k in range(random.choice([1, 1, 2, 2, 3])):
            if combos and random.random() < 0.18:
                cid, cname, cprice = random.choice(combos)
                items.append(dict(name=cname, drink_id=None, combo_id=cid, spec="", num=random.choice([1, 1, 2]), price=cprice))
            else:
                did, dname, dprice = random.choices(drinks, weights=pop, k=1)[0]
                gs = specs.get(did, [])
                items.append(dict(name=dname, drink_id=did, combo_id=None,
                                  spec="/".join(random.choice(v) for v in gs) if gs else "",
                                  num=random.choice([1, 1, 1, 2, 3]), price=dprice))

        amount = round(sum(it["price"] * it["num"] for it in items), 2)
        pay_status = 0 if status == 1 else (2 if status == 6 and random.random() < 0.6 else 1)
        oid += 1
        day_orders.append(dict(id=oid, d=d, mins=mins, uid=uid, uname=uname, addr=addr,
                               order_type=order_type, status=status, amount=amount,
                               pay_status=pay_status, remark=random.choice(REMARKS), items=items))

    # 堂食取餐号按当天出杯时间先后发，A001 是最早那单；隔天重新从 A001 开始
    for i, o in enumerate(sorted([x for x in day_orders if x["order_type"] == 1],
                                 key=lambda x: x["mins"]), start=1):
        o["pickup_no"] = "A%03d" % i

    for o in day_orders:
        orders.append(o)
        for it in o["items"]:
            details.append(dict(id=odid, order_id=o["id"], **it))
            odid += 1

print("生成: %d 用户 / %d 地址 / %d 订单 / %d 明细" % (len(users), len(addrs), len(orders), len(details)))


def esc(s):
    return str(s).replace("'", "''")


def dt(day, mins):
    """相对 CURDATE() 的日期时间表达式"""
    return "DATE_ADD(DATE_SUB(CURDATE(), INTERVAL %d DAY), INTERVAL %d MINUTE)" % (day, mins)


out = io.StringIO()
w = out.write
w("-- =============================================================\n")
w("-- 云栖茶饮 · 演示数据（最近 30 天的用户与订单）\n")
w("-- 前置：已执行 cloud_tea_schema.sql + cloud_tea_seed.sql\n--\n")
w("-- 时间全部用 CURDATE() 的相对表达式，因此任何时候重跑这个脚本，\n")
w("-- 数据都落在「最近 30 天」，统计报表不会因为脚本变旧而空掉。\n")
w("--\n")
w("-- 注意：会清空并重建 user / address_book / orders / order_detail / shopping_cart，\n")
w("--       菜单（category / drink / drink_spec / combo / combo_drink）与员工不受影响。\n")
w("-- =============================================================\n\nUSE cloud_tea;\n\n")

w("DELETE FROM order_detail;\nDELETE FROM orders;\nDELETE FROM shopping_cart;\n"
  "DELETE FROM address_book;\nDELETE FROM user;\n\n")
w("ALTER TABLE user AUTO_INCREMENT = 1;\nALTER TABLE address_book AUTO_INCREMENT = 1;\n"
  "ALTER TABLE orders AUTO_INCREMENT = 1;\nALTER TABLE order_detail AUTO_INCREMENT = 1;\n\n")

# 用户：注册时间铺在 30 天里，越近注册越多
w("-- 用户（create_time 铺在最近 30 天，越近注册越多）\n")
rows = []
for i, (uid, name) in enumerate(users):
    day = reg_day[i]
    rows.append("  (%d, 'mock_%d', '%s', '138%08d', '%s', %s)"
                % (uid, uid, esc(name), random.randint(0, 99999999),
                   random.choice("10"), dt(day, random.randint(9 * 60, 22 * 60))))
w("INSERT INTO user (id, openid, name, phone, sex, create_time) VALUES\n" + ",\n".join(rows) + ";\n\n")

w("-- 收货地址\n")
rows = ["  (%d, %d, '%s', '%s', '%s', '北京市', '北京市', '%s', '%s', '%s', %d)"
        % (a["id"], a["user_id"], esc(a["consignee"]), a["sex"], a["phone"],
           a["district"], esc(a["detail"]), esc(a["label"]), a["is_default"])
        for a in addrs]
w("INSERT INTO address_book (id, user_id, consignee, sex, phone, province_name, city_name, "
  "district_name, detail, label, is_default) VALUES\n" + ",\n".join(rows) + ";\n\n")

w("-- 订单\n")
rows = []
for o in orders:
    a = o["addr"]
    dine_in = o["order_type"] == 1
    checkout = dt(o["d"], o["mins"] + random.randint(1, 4)) if o["pay_status"] in (1, 2) else "NULL"
    cancel_time = dt(o["d"], o["mins"] + random.randint(5, 20)) if o["status"] == 6 else "NULL"
    if o["status"] == 6:
        reason = "'用户取消'" if o["pay_status"] == 2 else "'门店繁忙，无法接单'"
    else:
        reason = "NULL"
    number = "CONCAT(DATE_FORMAT(%s, '%%Y%%m%%d%%H%%i%%s'), '%03d')" % (dt(o["d"], o["mins"]), o["id"] % 1000)
    # 堂食出杯就取走、外卖要跑一趟，完成时间分别往后铺
    done_after = random.randint(8, 20) if dine_in else random.randint(25, 45)
    delivery_time = dt(o["d"], o["mins"] + done_after) if o["status"] == 5 else "NULL"

    # 堂食没有收货地址和送达时间，这几列全部落 NULL，其余两种方式一致
    values = [
        str(o["id"]), number, str(o["status"]),
        str(o["order_type"]),
        ("'%s'" % o["pickup_no"]) if dine_in else "NULL",
        str(o["uid"]),
        "NULL" if dine_in else str(a["id"]),
        dt(o["d"], o["mins"]), checkout, "1", str(o["pay_status"]), "%.2f" % o["amount"],
        "'%s'" % esc(o["remark"]),
        "NULL" if dine_in else "'%s'" % a["phone"],
        "NULL" if dine_in else "'%s'" % esc("北京市" + a["district"] + a["detail"]),
        "'%s'" % esc(o["uname"]),
        "NULL" if dine_in else "'%s'" % esc(a["consignee"]),
        reason, reason, cancel_time,
        "NULL" if dine_in else dt(o["d"], o["mins"] + 30),
        "1", delivery_time,
        str(random.choice([0, 0, 1, 2])), str(random.choice([1, 1, 2, 3])), "1",
    ]
    rows.append("  (" + ", ".join(values) + ")")
w("INSERT INTO orders (id, number, status, order_type, pickup_no, user_id, address_book_id, order_time, "
  "checkout_time, pay_method, pay_status, amount, remark, phone, address, user_name, consignee, "
  "cancel_reason, rejection_reason, cancel_time, estimated_delivery_time, delivery_status, delivery_time, "
  "pack_amount, tableware_number, tableware_status) VALUES\n" + ",\n".join(rows) + ";\n\n")

w("-- 订单明细\n")
rows = ["  (%d, '%s', %d, %s, %s, %s, %d, %.2f)"
        % (it["id"], esc(it["name"]), it["order_id"],
           it["drink_id"] if it["drink_id"] else "NULL",
           it["combo_id"] if it["combo_id"] else "NULL",
           ("'%s'" % esc(it["spec"])) if it["spec"] else "NULL",
           it["num"], round(it["price"] * it["num"], 2))
        for it in details]
w("INSERT INTO order_detail (id, name, order_id, drink_id, combo_id, drink_spec, number, amount) VALUES\n"
  + ",\n".join(rows) + ";\n")

path = "docs/sql/cloud_tea_demo_data.sql"
open(path, "w", encoding="utf-8").write(out.getvalue())
print("已写出 %s (%d KB)" % (path, len(out.getvalue()) // 1024))
