<template>
  <div class="cart-page page with-cart-bar">
    <van-nav-bar title="购物车" left-arrow @click-left="$router.back()" />

    <van-empty v-if="!loading && !items.length" description="购物车还是空的，去挑一杯吧">
      <van-button type="primary" round size="small" @click="$router.replace('/menu')">
        去点单
      </van-button>
    </van-empty>

    <template v-else>
      <div class="head">
        <span class="muted">共 {{ count }} 件商品</span>
        <span class="clear" @click="onClear">清空</span>
      </div>

      <van-cell-group inset class="group">
        <div v-for="item in items" :key="item.id" class="row">
          <img v-if="item.image" :src="item.image" class="thumb" alt="" />
          <div v-else class="thumb placeholder">🥤</div>

          <div class="row-main">
            <div class="row-name">{{ item.name }}</div>
            <div v-if="item.drinkSpec" class="row-spec">{{ item.drinkSpec }}</div>
            <div class="row-price">
              <span class="price">{{ Number(item.amount).toFixed(2) }}</span>
            </div>
          </div>

          <van-stepper
            :model-value="item.number"
            :min="0"
            integer
            @plus="onPlus(item)"
            @minus="onMinus(item)"
          />
        </div>
      </van-cell-group>

      <van-submit-bar
        :price="amount"
        button-text="去结算"
        button-color="#2e7d6b"
        @submit="$router.push('/order/confirm')"
      />
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { showConfirmDialog, showSuccessToast, showToast } from 'vant'
import { addToCart, cleanCart, listCart, subFromCart } from '@/api'
import { useUserStore } from '@/stores/user'

defineOptions({ name: 'CartPage' })

const user = useUserStore()
const items = ref([])
const loading = ref(true)

const count = computed(() => items.value.reduce((n, c) => n + c.number, 0))
// van-submit-bar 的 price 单位是分
const amount = computed(() =>
  Math.round(items.value.reduce((s, c) => s + Number(c.amount) * c.number, 0) * 100)
)

async function load() {
  loading.value = true
  try {
    items.value = (await listCart()) || []
  } finally {
    loading.value = false
  }
}

/** 购物车行的身份：同一饮品 + 同一规格才算同一行 */
function keyOf(item) {
  return { drinkId: item.drinkId, comboId: item.comboId, drinkSpec: item.drinkSpec }
}

async function onPlus(item) {
  await addToCart(keyOf(item))
  await load()
}

async function onMinus(item) {
  await subFromCart(keyOf(item))
  await load()
}

async function onClear() {
  try {
    await showConfirmDialog({ title: '清空购物车', message: '确定要清空购物车吗？' })
  } catch {
    return
  }
  await cleanCart()
  showSuccessToast('已清空')
  load()
}

onMounted(load)
</script>

<style scoped>
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px 8px;
  font-size: 12.5px;
}

.clear {
  color: var(--van-danger-color, #d9534f);
}

.group {
  margin-top: 0;
}

.row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 0;
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
  font-size: 14.5px;
  font-weight: 600;
}

.row-spec {
  font-size: 11.5px;
  color: var(--ct-green-deep);
  background: var(--ct-green-soft);
  display: inline-block;
  padding: 2px 8px;
  border-radius: 9px;
  margin: 5px 0 6px;
}

.row-price .price {
  font-size: 15px;
}
</style>
