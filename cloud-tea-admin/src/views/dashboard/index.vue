<template>
  <div class="page">
    <div class="page-head">
      <div>
        <div class="page-title">工作台</div>
        <div class="page-sub">{{ today }} · 今日经营概览</div>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadAll">刷新</el-button>
    </div>

    <!-- 今日核心指标 -->
    <div class="metrics" v-loading="loading">
      <div v-for="m in metrics" :key="m.label" class="metric">
        <div class="metric-label">{{ m.label }}</div>
        <div class="metric-value">
          {{ m.value }}<span v-if="m.unit" class="metric-unit">{{ m.unit }}</span>
        </div>
        <div class="metric-hint">{{ m.hint }}</div>
      </div>
    </div>

    <div class="grid">
      <!-- 待处理订单 -->
      <div class="card">
        <div class="card-head">
          <span class="card-title">待处理订单</span>
          <el-button link type="primary" @click="$router.push('/order')">去处理 →</el-button>
        </div>
        <div class="stat-row">
          <div class="stat" :class="{ alert: statistics.toBeConfirmed > 0 }">
            <div class="stat-num">{{ statistics.toBeConfirmed }}</div>
            <div class="stat-label">待接单</div>
          </div>
          <div class="stat">
            <div class="stat-num">{{ statistics.confirmed }}</div>
            <div class="stat-label">待配送</div>
          </div>
          <div class="stat">
            <div class="stat-num">{{ statistics.deliveryInProgress }}</div>
            <div class="stat-label">配送中</div>
          </div>
        </div>
      </div>

      <!-- 订单总览 -->
      <div class="card">
        <div class="card-head"><span class="card-title">订单总览</span></div>
        <div class="stat-row">
          <div class="stat">
            <div class="stat-num">{{ orderOverview.allOrders }}</div>
            <div class="stat-label">全部订单</div>
          </div>
          <div class="stat">
            <div class="stat-num">{{ orderOverview.waitingOrders }}</div>
            <div class="stat-label">待接单</div>
          </div>
          <div class="stat">
            <div class="stat-num">{{ orderOverview.deliveredOrders }}</div>
            <div class="stat-label">待配送</div>
          </div>
          <div class="stat">
            <div class="stat-num">{{ orderOverview.completedOrders }}</div>
            <div class="stat-label">已完成</div>
          </div>
          <div class="stat">
            <div class="stat-num">{{ orderOverview.cancelledOrders }}</div>
            <div class="stat-label">已取消</div>
          </div>
        </div>
      </div>

      <!-- 商品总览 -->
      <div class="card">
        <div class="card-head"><span class="card-title">菜单总览</span></div>
        <div class="menu-overview">
          <div class="menu-item">
            <el-icon class="ico"><ColdDrink /></el-icon>
            <div>
              <div class="menu-name">饮品</div>
              <div class="menu-nums">
                <span class="on">上架 {{ drinkOverview.sold }}</span>
                <span class="off">下架 {{ drinkOverview.discontinued }}</span>
              </div>
            </div>
          </div>
          <div class="menu-item">
            <el-icon class="ico"><ShoppingBag /></el-icon>
            <div>
              <div class="menu-name">组合套餐</div>
              <div class="menu-nums">
                <span class="on">上架 {{ comboOverview.sold }}</span>
                <span class="off">下架 {{ comboOverview.discontinued }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import {
  businessData,
  overviewCombos,
  overviewDrinks,
  overviewOrders,
  orderStatistics
} from '@/api'
import { toDateStr } from '@/utils/format'

const loading = ref(false)
const today = toDateStr(new Date())

const business = ref({})
const statistics = ref({ toBeConfirmed: 0, confirmed: 0, deliveryInProgress: 0 })
const orderOverview = ref({})
const drinkOverview = ref({ sold: 0, discontinued: 0 })
const comboOverview = ref({ sold: 0, discontinued: 0 })

const metrics = computed(() => [
  { label: '今日营业额', value: (business.value.turnover ?? 0).toFixed(2), unit: '元', hint: '按已完成订单统计' },
  { label: '有效订单', value: business.value.validOrderCount ?? 0, unit: '单', hint: '今日已完成订单' },
  {
    label: '订单完成率',
    value: ((business.value.orderCompletionRate ?? 0) * 100).toFixed(1),
    unit: '%',
    hint: '已完成 / 全部订单'
  },
  { label: '平均客单价', value: (business.value.unitPrice ?? 0).toFixed(2), unit: '元', hint: '营业额 / 有效订单' },
  { label: '新增用户', value: business.value.newUsers ?? 0, unit: '人', hint: '今日新注册用户' }
])

async function loadAll() {
  loading.value = true
  // 任一接口失败不影响其余卡片渲染
  const results = await Promise.allSettled([
    businessData(),
    orderStatistics(),
    overviewOrders(),
    overviewDrinks(),
    overviewCombos()
  ])
  const [b, s, o, d, c] = results
  if (b.status === 'fulfilled') business.value = b.value || {}
  if (s.status === 'fulfilled') statistics.value = s.value || statistics.value
  if (o.status === 'fulfilled') orderOverview.value = o.value || {}
  if (d.status === 'fulfilled') drinkOverview.value = d.value || drinkOverview.value
  if (c.status === 'fulfilled') comboOverview.value = c.value || comboOverview.value
  loading.value = false
}

onMounted(loadAll)
</script>

<style scoped>
.metrics {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
  margin-bottom: 16px;
}

.metric {
  background: #fff;
  border: 1px solid var(--ct-line);
  border-radius: 12px;
  padding: 18px 18px 16px;
  position: relative;
  overflow: hidden;
}

.metric::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 3px;
  height: 100%;
  background: linear-gradient(180deg, var(--ct-green), var(--ct-amber));
}

.metric-label {
  font-size: 13px;
  color: var(--ct-ink-soft);
}

.metric-value {
  margin: 10px 0 6px;
  font-size: 27px;
  font-weight: 600;
  letter-spacing: -0.5px;
  color: var(--ct-green-deep);
}

.metric-unit {
  font-size: 13px;
  font-weight: 400;
  margin-left: 3px;
  color: var(--ct-ink-soft);
}

.metric-hint {
  font-size: 11.5px;
  color: #9aa8a4;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1.35fr;
  grid-template-rows: auto auto;
  gap: 14px;
}

.grid > :nth-child(3) {
  grid-column: 1 / -1;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
}

.stat-row {
  display: flex;
  gap: 10px;
}

.stat {
  flex: 1;
  text-align: center;
  padding: 14px 6px;
  border-radius: 10px;
  background: #f7faf9;
}

.stat.alert {
  background: #fdf3e6;
}

.stat.alert .stat-num {
  color: #d98820;
}

.stat-num {
  font-size: 24px;
  font-weight: 600;
  color: var(--ct-green-deep);
}

.stat-label {
  margin-top: 4px;
  font-size: 12px;
  color: var(--ct-ink-soft);
}

.menu-overview {
  display: flex;
  gap: 14px;
}

.menu-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  border-radius: 10px;
  background: #f7faf9;
}

.menu-item .ico {
  font-size: 26px;
  color: var(--ct-green);
}

.menu-name {
  font-size: 13.5px;
  font-weight: 600;
  margin-bottom: 5px;
}

.menu-nums {
  display: flex;
  gap: 14px;
  font-size: 12.5px;
}

.menu-nums .on {
  color: var(--ct-green);
}

.menu-nums .off {
  color: var(--ct-ink-soft);
}

@media (max-width: 1200px) {
  .metrics {
    grid-template-columns: repeat(3, 1fr);
  }
  .grid {
    grid-template-columns: 1fr;
  }
  .grid > :nth-child(3) {
    grid-column: auto;
  }
}
</style>
