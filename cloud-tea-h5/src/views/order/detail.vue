<template>
  <div class="order-detail page">
    <van-nav-bar title="订单详情" left-arrow @click-left="$router.back()" />

    <template v-if="order">
      <!-- 状态条 -->
      <div class="status-hero" :class="tone">
        <div class="status-text">{{ statusLabel(order.status) }}</div>
        <div class="status-sub">{{ statusHint }}</div>
      </div>

      <!-- 堂食取餐号：堂食最要紧的信息，放最上面 -->
      <div v-if="isDineIn" class="pickup-card">
        <div class="pickup-label">取餐号</div>
        <div class="pickup-no">{{ order.pickupNo || '—' }}</div>
        <div class="pickup-tip">云栖茶饮 · 中关村店 · 出杯后凭号到吧台取餐</div>
      </div>

      <!-- 商品 -->
      <van-cell-group inset title="商品清单" class="block">
        <div v-for="item in order.orderDetailList || []" :key="item.id" class="row">
          <img v-if="item.image" :src="item.image" class="thumb" alt="" />
          <div v-else class="thumb placeholder">🥤</div>
          <div class="row-main">
            <div class="row-name">{{ item.name }}</div>
            <div v-if="item.drinkSpec" class="row-spec">{{ item.drinkSpec }}</div>
          </div>
          <div class="row-right">
            <div class="price">{{ Number(item.amount).toFixed(2) }}</div>
            <div class="muted qty">x{{ item.number }}</div>
          </div>
        </div>
      </van-cell-group>

      <!-- 配送信息：只有外卖有，堂食看上面的取餐号 -->
      <van-cell-group v-if="isDelivery" inset title="配送信息" class="block">
        <van-cell title="收货人" :value="order.consignee || '—'" />
        <van-cell title="联系电话" :value="order.phone || '—'" />
        <van-cell title="收货地址" :value="order.address || '—'" />
        <van-cell title="预计送达" :value="toDateTime(order.estimatedDeliveryTime) || '—'" />
        <van-cell v-if="order.deliveryTime" title="送达时间" :value="toDateTime(order.deliveryTime)" />
      </van-cell-group>

      <!-- 订单信息 -->
      <van-cell-group inset title="订单信息" class="block">
        <van-cell title="就餐方式" :value="isDineIn ? '堂食' : '外卖'" />
        <van-cell title="订单号" :value="order.number" />
        <van-cell title="下单时间" :value="toDateTime(order.orderTime)" />
        <van-cell v-if="order.checkoutTime" title="支付时间" :value="toDateTime(order.checkoutTime)" />
        <van-cell title="支付方式" value="微信支付" />
        <!-- 外卖叫送达时间，堂食叫取餐时间，共用 delivery_time 字段 -->
        <van-cell
          v-if="isDineIn && order.deliveryTime"
          title="取餐时间"
          :value="toDateTime(order.deliveryTime)"
        />
        <van-cell title="备注" :value="order.remark || '无'" />
        <van-cell
          v-if="order.cancelReason"
          title="取消原因"
          :value="order.cancelReason"
        />
        <van-cell
          v-if="order.rejectionReason"
          title="拒单原因"
          :value="order.rejectionReason"
        />
        <van-cell title="实收金额">
          <template #value>
            <span class="price big">{{ Number(order.amount).toFixed(2) }}</span>
          </template>
        </van-cell>
      </van-cell-group>

      <!-- 操作 -->
      <div class="actions">
        <van-button v-if="order.status === 1" block round @click="onCancel">取消订单</van-button>
        <van-button
          v-if="order.status === 1"
          block
          round
          type="primary"
          :loading="paying"
          @click="onPay"
        >
          去支付
        </van-button>
        <van-button v-if="[2, 3, 4].includes(order.status)" block round type="primary" @click="onRemind">
          催单
        </van-button>
        <van-button v-if="[5, 6].includes(order.status)" block round type="primary" @click="onRepeat">
          再来一单
        </van-button>
      </div>
    </template>

    <van-empty v-else-if="!loading" description="订单不存在" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showSuccessToast, showToast } from 'vant'
import { cancelOrder, orderDetail, payOrder, remindOrder, repeatOrder } from '@/api'
import { ORDER_STATUS, statusLabel, toDateTime } from '@/utils/spec'

defineOptions({ name: 'OrderDetailPage' })

const route = useRoute()
const router = useRouter()

const order = ref(null)
const loading = ref(true)
const paying = ref(false)

const tone = computed(() => ORDER_STATUS[order.value?.status]?.tone || 'muted')

// 就餐方式：只有堂食才走取餐号那一套，其余（含历史数据）都按外卖显示
const isDineIn = computed(() => order.value?.orderType === 1)
const isDelivery = computed(() => !isDineIn.value)

const statusHint = computed(() => {
  switch (order.value?.status) {
    case 1:
      return '请在 15 分钟内完成支付，超时会自动取消'
    case 2:
      return '门店正在确认订单'
    case 3:
      return isDineIn.value ? '茶饮制作中，出杯后凭取餐号到吧台取' : '茶饮制作中，请稍候'
    case 4:
      return '骑手正在配送，请保持电话畅通'
    case 5:
      return '订单已完成，感谢光临'
    case 6:
      return '订单已取消'
    default:
      return ''
  }
})

async function load() {
  loading.value = true
  try {
    order.value = await orderDetail(route.params.id)
  } finally {
    loading.value = false
  }
}

async function onPay() {
  paying.value = true
  try {
    await payOrder({ orderNumber: order.value.number, payMethod: 1 })
    showSuccessToast('支付成功')
    load()
  } finally {
    paying.value = false
  }
}

async function onCancel() {
  try {
    await showConfirmDialog({ title: '取消订单', message: '确定要取消这笔订单吗？' })
  } catch {
    return
  }
  await cancelOrder(order.value.id)
  showSuccessToast('订单已取消')
  load()
}

async function onRemind() {
  await remindOrder(order.value.id)
  showToast('已催单，门店会尽快处理')
}

async function onRepeat() {
  await repeatOrder(order.value.id)
  showSuccessToast('已把商品加回购物车')
  router.push('/cart')
}

onMounted(load)
</script>

<style scoped>
.status-hero {
  padding: 24px 20px 28px;
  color: #fff;
  background: linear-gradient(150deg, #2e7d6b 0%, #1f5b4d 100%);
}

.status-hero.muted {
  background: linear-gradient(150deg, #7c8a87 0%, #5d6b68 100%);
}

.status-hero.warn {
  background: linear-gradient(150deg, #d9a13f 0%, #b5801f 100%);
}

.status-text {
  font-size: 21px;
  font-weight: 600;
  letter-spacing: 1px;
}

.status-sub {
  margin-top: 7px;
  font-size: 12.5px;
  color: rgba(255, 255, 255, 0.78);
}

/* 堂食取餐号，做成叫号牌的样子 */
.pickup-card {
  margin: 12px 16px 0;
  padding: 18px 16px;
  border: 1px dashed var(--ct-green-deep);
  border-radius: 12px;
  background: var(--ct-green-soft);
  text-align: center;
}

.pickup-label {
  font-size: 12px;
  letter-spacing: 2px;
  color: var(--ct-green-deep);
}

.pickup-no {
  margin-top: 6px;
  font-size: 40px;
  font-weight: 700;
  line-height: 1.1;
  letter-spacing: 3px;
  color: var(--ct-green-deep);
}

.pickup-tip {
  margin-top: 8px;
  font-size: 11.5px;
  color: #6b7c78;
}

.block {
  margin-top: 12px;
}

.row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 13px 16px;
  border-bottom: 1px solid var(--ct-line);
}

.row:last-child {
  border-bottom: none;
}

.row-main {
  flex: 1;
  min-width: 0;
}

.row-name {
  font-size: 14px;
  font-weight: 600;
}

.row-spec {
  font-size: 11.5px;
  color: var(--ct-green-deep);
  background: var(--ct-green-soft);
  display: inline-block;
  padding: 2px 8px;
  border-radius: 9px;
  margin-top: 5px;
}

.row-right {
  text-align: right;
}

.qty {
  font-size: 11.5px;
  margin-top: 3px;
}

.price.big {
  font-size: 17px;
}

.actions {
  padding: 22px 16px 30px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>
