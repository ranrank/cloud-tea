<template>
  <div class="confirm-page page with-cart-bar">
    <van-nav-bar title="确认订单" left-arrow @click-left="$router.back()" />

    <!-- 就餐方式：堂食到店喝，外卖送上门，两条链路的必填项不一样 -->
    <div class="dining-switch">
      <div class="opt" :class="{ active: isDineIn }" @click="orderType = 1">
        <span class="opt-title">堂食</span>
        <span class="opt-sub">店内饮用 · 凭取餐号取</span>
      </div>
      <div class="opt" :class="{ active: isDelivery }" @click="orderType = 2">
        <span class="opt-title">外卖</span>
        <span class="opt-sub">配送到家</span>
      </div>
    </div>

    <!-- 收货地址：只有外卖需要 -->
    <van-cell-group v-if="isDelivery" inset class="block">
      <van-cell is-link :border="false" @click="$router.push('/address')">
        <template #title>
          <div v-if="address" class="addr">
            <div class="addr-line1">
              <span class="addr-name">{{ address.consignee }}</span>
              <span class="muted">{{ address.phone }}</span>
              <van-tag v-if="address.label" plain type="primary" class="addr-tag">
                {{ address.label }}
              </van-tag>
            </div>
            <div class="addr-detail muted">
              {{ address.provinceName }}{{ address.cityName }}{{ address.districtName }}{{ address.detail }}
            </div>
          </div>
          <div v-else class="addr-empty">请选择收货地址</div>
        </template>
      </van-cell>
    </van-cell-group>

    <!-- 堂食不需要地址，顾客人就在店里 -->
    <van-cell-group v-else inset class="block">
      <van-cell :border="false">
        <template #title>
          <div class="addr">
            <div class="addr-name">云栖茶饮 · 中关村店</div>
            <div class="addr-detail muted">下单后生成取餐号，出杯后凭号到吧台取餐</div>
          </div>
        </template>
      </van-cell>
    </van-cell-group>

    <!-- 商品 -->
    <van-cell-group inset title="商品清单" class="block">
      <div v-for="item in items" :key="item.id" class="row">
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

    <!-- 其他信息 -->
    <van-cell-group inset class="block">
      <!-- 送达时间是外卖概念，堂食没有 -->
      <van-cell v-if="isDelivery" title="送达时间">
        <template #value>
          <van-radio-group v-model="deliveryStatus" direction="horizontal">
            <van-radio :name="1">立即送出</van-radio>
            <van-radio :name="0">预约</van-radio>
          </van-radio-group>
        </template>
      </van-cell>

      <van-cell
        v-if="isDelivery && deliveryStatus === 0"
        title="预约时间"
        is-link
        :value="estimateText"
        @click="showTimePicker = true"
      />

      <van-cell title="餐具份数">
        <template #value>
          <van-stepper v-model="tablewareNumber" :min="0" :max="20" />
        </template>
      </van-cell>

      <van-field
        v-model="remark"
        label="订单备注"
        type="textarea"
        rows="2"
        maxlength="80"
        show-word-limit
        placeholder="如：少冰、不要吸管、放门口"
      />
    </van-cell-group>

    <van-submit-bar
      :price="amount"
      button-text="提交订单"
      button-color="#2e7d6b"
      :loading="submitting"
      @submit="onSubmit"
    />

    <van-popup v-model:show="showTimePicker" position="bottom" round>
      <van-time-picker
        v-model="estimate"
        title="选择送达时间"
        :min-hour="9"
        :max-hour="22"
        @confirm="showTimePicker = false"
        @cancel="showTimePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getDefaultAddress, listCart, payOrder, submitOrder } from '@/api'
import { useUserStore } from '@/stores/user'

defineOptions({ name: 'OrderConfirmPage' })

const router = useRouter()
const user = useUserStore()

const items = ref([])
const address = ref(null)

// 就餐方式 1 堂食 2 外卖，默认外卖（保持原来的下单习惯）
const orderType = ref(2)
const isDineIn = computed(() => orderType.value === 1)
const isDelivery = computed(() => orderType.value === 2)

const deliveryStatus = ref(1) // 1 立即送出 0 预约
const estimate = ref(['12', '00'])
const tablewareNumber = ref(1)
const remark = ref('')
const showTimePicker = ref(false)
const submitting = ref(false)

const amount = computed(() =>
  Math.round(items.value.reduce((s, c) => s + Number(c.amount) * c.number, 0) * 100)
)

const totalYuan = computed(() => Number((amount.value / 100).toFixed(2)))

const estimateText = computed(() => `${estimate.value[0]}:${estimate.value[1]}`)

/**
 * 注意：后端对 LocalDateTime 的入参和返回值格式不一致（实测确认）——
 *   入参（反序列化）必须是 "yyyy-MM-dd HH:mm:ss"，带秒；
 *   返回值（序列化）是 "yyyy-MM-dd HH:mm"，不带秒。
 * 这里按入参格式发，少写秒或写成 ISO 的 'T' 分隔都会被 400 拒绝。
 */
function formatDateTime(d) {
  const pad = (n) => String(n).padStart(2, '0')
  return (
    `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}` +
    ` ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  )
}

/** 立即送出：按当前时间往后 30 分钟预估；预约：用用户选的时间 */
function estimateDateTime() {
  if (deliveryStatus.value === 1) {
    return formatDateTime(new Date(Date.now() + 30 * 60 * 1000))
  }
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  const date = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
  return `${date} ${estimate.value[0]}:${estimate.value[1]}:00`
}

async function load() {
  const [cart, addr] = await Promise.allSettled([listCart(), getDefaultAddress()])
  items.value = cart.status === 'fulfilled' ? cart.value || [] : []
  // 没有默认地址时后端会抛异常，这里按 null 处理，让用户去选地址
  address.value = addr.status === 'fulfilled' ? addr.value : null

  if (!items.value.length) {
    showToast('购物车是空的')
    router.replace('/menu')
  }
}

async function onSubmit() {
  if (!user.isStoreOpen) {
    showToast('门店已打烊，暂时无法下单')
    return
  }
  if (!address.value && isDelivery.value) {
    showToast('请先选择收货地址')
    return
  }

  submitting.value = true
  try {
    const submitted = await submitOrder({
      orderType: orderType.value,
      // 堂食没有收货地址，传 null；后端也不会去查地址簿、更不校验配送范围
      addressBookId: isDineIn.value ? null : address.value.id,
      payMethod: 1,
      remark: remark.value,
      deliveryStatus: deliveryStatus.value,
      // 送达时间是外卖概念，堂食不传（留 undefined，axios 不会把它发出去）
      estimatedDeliveryTime: isDineIn.value ? undefined : estimateDateTime(),
      tablewareNumber: tablewareNumber.value,
      tablewareStatus: 1,
      packAmount: 0,
      amount: totalYuan.value
    })

    // 后端配置了 cloudtea.wechat.mock=true，支付接口会直接按已支付处理
    await payOrder({ orderNumber: submitted.orderNumber, payMethod: 1 })

    showToast('下单成功')
    router.replace('/order')
  } catch {
    // 拦截器已提示具体原因（如超出配送范围）
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.block {
  margin-top: 12px;
}

/* 堂食 / 外卖切换 */
.dining-switch {
  display: flex;
  gap: 10px;
  margin: 12px 16px 0;
}

.dining-switch .opt {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid var(--ct-line);
  border-radius: 10px;
  background: #fff;
  transition: all 0.15s;
}

.dining-switch .opt.active {
  border-color: var(--ct-green-deep);
  background: var(--ct-green-soft);
}

.dining-switch .opt-title {
  display: block;
  font-size: 15px;
  font-weight: 600;
}

.dining-switch .opt.active .opt-title {
  color: var(--ct-green-deep);
}

.dining-switch .opt-sub {
  display: block;
  margin-top: 3px;
  font-size: 11.5px;
  color: #8b9a96;
}

.addr-line1 {
  display: flex;
  align-items: center;
  gap: 9px;
}

.addr-name {
  font-size: 15px;
  font-weight: 600;
}

.addr-tag {
  font-size: 10px;
}

.addr-detail {
  font-size: 12.5px;
  margin-top: 6px;
  line-height: 1.5;
}

.addr-empty {
  font-size: 14px;
  color: var(--ct-green-deep);
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

.row-right .price {
  font-size: 14.5px;
}

.qty {
  font-size: 11.5px;
  margin-top: 3px;
}
</style>
