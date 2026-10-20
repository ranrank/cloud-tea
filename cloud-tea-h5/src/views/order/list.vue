<template>
  <div class="order-list page">
    <div class="head-bar">我的订单</div>

    <van-tabs v-model:active="activeTab" sticky swipeable @change="reload">
      <van-tab v-for="t in tabs" :key="t.label" :title="t.label" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-empty v-if="!loading && !orders.length" description="还没有订单，去点一杯吧">
        <van-button type="primary" round size="small" @click="$router.replace('/menu')">
          去点单
        </van-button>
      </van-empty>

      <div v-else class="cards">
        <div v-for="order in orders" :key="order.id" class="card" @click="goDetail(order)">
          <div class="card-head">
            <span class="head-left">
              <span class="mono muted num">{{ order.number }}</span>
              <span class="dining-tag" :class="{ dinein: order.orderType === 1 }">
                {{ order.orderType === 1 ? `堂食 ${order.pickupNo || ''}` : '外卖' }}
              </span>
            </span>
            <span class="status" :class="tone(order.status)">{{ statusLabel(order.status) }}</span>
          </div>

          <div class="items">
            <div v-for="d in order.orderDetailList || []" :key="d.id" class="item">
              <img v-if="d.image" :src="d.image" class="thumb-sm" alt="" />
              <div v-else class="thumb-sm placeholder">🥤</div>
              <div class="item-main">
                <div class="item-name">{{ d.name }}</div>
                <div v-if="d.drinkSpec" class="item-spec">{{ d.drinkSpec }}</div>
              </div>
              <div class="item-qty muted">x{{ d.number }}</div>
            </div>
          </div>

          <div class="card-foot">
            <span class="muted time">{{ toDateTime(order.orderTime) }}</span>
            <span class="total">
              共 {{ totalCount(order) }} 件 合计 <span class="price">{{ Number(order.amount).toFixed(2) }}</span>
            </span>
          </div>

          <div class="actions" @click.stop>
            <template v-if="order.status === 1">
              <van-button size="small" round @click="onCancel(order)">取消订单</van-button>
              <van-button size="small" round type="primary" :loading="payingId === order.id" @click="onPay(order)">
                去支付
              </van-button>
            </template>
            <template v-else-if="order.status === 2">
              <van-button size="small" round @click="onCancel(order)">取消订单</van-button>
              <van-button size="small" round type="primary" @click="onRemind(order)">催单</van-button>
            </template>
            <template v-else-if="order.status === 3 || order.status === 4">
              <van-button size="small" round type="primary" @click="onRemind(order)">催单</van-button>
            </template>
            <template v-else>
              <van-button size="small" round type="primary" @click="onRepeat(order)">再来一单</van-button>
            </template>
          </div>
        </div>
      </div>
    </van-pull-refresh>

    <TabBar />
  </div>
</template>

<script setup>
import { onActivated, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showSuccessToast, showToast } from 'vant'
import TabBar from '@/components/TabBar.vue'
import { cancelOrder, historyOrders, payOrder, remindOrder, repeatOrder } from '@/api'
import { ORDER_STATUS, statusLabel, toDateTime } from '@/utils/spec'

defineOptions({ name: 'OrderListPage' })

const router = useRouter()

const tabs = [
  { label: '全部', status: null },
  { label: '待付款', status: 1 },
  { label: '待接单', status: 2 },
  { label: '制作中', status: 3 },
  { label: '配送中', status: 4 },
  { label: '已完成', status: 5 },
  { label: '已取消', status: 6 }
]

const activeTab = ref(0)
const orders = ref([])
const loading = ref(false)
const refreshing = ref(false)
const payingId = ref(null)

const tone = (status) => ORDER_STATUS[status]?.tone || 'muted'
const totalCount = (order) => (order.orderDetailList || []).reduce((n, d) => n + d.number, 0)

async function load() {
  loading.value = true
  try {
    const status = tabs[activeTab.value].status
    const params = { page: 1, pageSize: 20 }
    if (status !== null) params.status = status
    const data = await historyOrders(params)
    orders.value = data.records || []
  } finally {
    loading.value = false
  }
}

function reload() {
  load()
}

async function onRefresh() {
  await load()
  refreshing.value = false
}

function goDetail(order) {
  router.push(`/order/detail/${order.id}`)
}

async function onPay(order) {
  payingId.value = order.id
  try {
    await payOrder({ orderNumber: order.number, payMethod: 1 })
    showSuccessToast('支付成功')
    load()
  } finally {
    payingId.value = null
  }
}

async function onCancel(order) {
  try {
    await showConfirmDialog({ title: '取消订单', message: '确定要取消这笔订单吗？' })
  } catch {
    return
  }
  await cancelOrder(order.id)
  showSuccessToast('订单已取消')
  load()
}

async function onRemind(order) {
  await remindOrder(order.id)
  showToast('已催单，门店会尽快处理')
}

async function onRepeat(order) {
  await repeatOrder(order.id)
  showSuccessToast('已把商品加回购物车')
  router.push('/cart')
}

onMounted(load)
onActivated(load)
</script>

<style scoped>
.head-bar {
  padding: 16px 18px 10px;
  font-size: 18px;
  font-weight: 600;
}

.cards {
  padding: 12px 12px 0;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 14px 15px 12px;
  margin-bottom: 12px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 11px;
  border-bottom: 1px solid var(--ct-line);
}

.num {
  font-size: 11.5px;
}

.head-left {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

/* 堂食 / 外卖标签 */
.dining-tag {
  flex-shrink: 0;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 10.5px;
  white-space: nowrap;
  color: #6b7c78;
  background: #eef2f1;
}

.dining-tag.dinein {
  color: var(--ct-green-deep);
  background: var(--ct-green-soft);
}

.status {
  font-size: 12.5px;
  font-weight: 600;
}

.status.warn {
  color: #d98820;
}

.status.primary {
  color: var(--ct-green);
}

.status.muted {
  color: var(--ct-ink-soft);
}

.items {
  padding: 6px 0;
}

.item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
}

.thumb-sm {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
  background: var(--ct-green-soft);
}

.thumb-sm.placeholder {
  display: grid;
  place-items: center;
  font-size: 17px;
}

.item-main {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 13.5px;
  font-weight: 600;
}

.item-spec {
  font-size: 11px;
  color: var(--ct-green-deep);
  margin-top: 3px;
}

.item-qty {
  font-size: 12px;
}

.card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 10px;
  border-top: 1px solid var(--ct-line);
  font-size: 12px;
}

.time {
  font-size: 11.5px;
}

.total .price {
  font-size: 15px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 9px;
  margin-top: 11px;
}
</style>
