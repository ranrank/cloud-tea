<template>
  <div class="menu-page page with-cart-bar">
    <!-- 顶部门店状态 -->
    <div class="banner" :class="{ closed: !user.isStoreOpen }">
      <div class="banner-left">
        <div class="store-name">云栖茶饮 · 中关村店</div>
        <div class="store-sub">
          {{ user.isStoreOpen ? '营业中 · 预计 20 分钟出餐' : '已打烊 · 暂不接单' }}
        </div>
      </div>
      <div class="store-badge">{{ user.isStoreOpen ? '营业中' : '打烊' }}</div>
    </div>

    <!-- 分类 + 商品 -->
    <div class="body">
      <aside class="nav">
        <div
          v-for="s in categoryList"
          :key="s.id"
          class="nav-item"
          :class="{ on: s.id === activeCategoryId }"
          @click="selectCategory(s)"
        >
          {{ s.name }}
        </div>
      </aside>

      <section class="list">
        <div v-if="loading" class="empty">加载中…</div>
        <template v-else>
          <div v-if="!items.length" class="empty">这个分类还没有上架的商品</div>
          <div v-for="item in items" :key="itemKey(item)" class="item">
            <img v-if="item.image" :src="item.image" class="thumb" alt="" />
            <div v-else class="thumb placeholder">🥤</div>

            <div class="item-main">
              <div class="item-name">{{ item.name }}</div>
              <div class="item-desc muted">{{ item.description || '—' }}</div>
              <div class="item-price">
                <span class="price">{{ Number(item.price).toFixed(2) }}</span>
                <span v-if="isCombo" class="tag muted">套餐</span>
              </div>
            </div>

            <van-button
              type="primary"
              size="small"
              round
              icon="plus"
              class="add-btn"
              @click="onAdd(item)"
            />
          </div>
        </template>
      </section>
    </div>

    <!-- 底部购物车栏 -->
    <van-submit-bar
      v-if="cartCount > 0"
      :price="cartAmount"
      :button-text="`去结算(${cartCount})`"
      button-color="#2e7d6b"
      @submit="goCheckout"
    >
      <template #tip>
        <span class="cart-tip" @click="goCart">
          已选 {{ cartCount }} 件 · 查看购物车
        </span>
      </template>
    </van-submit-bar>

    <TabBar />

    <!-- 规格选择 -->
    <SpecPopup v-model:show="specVisible" :item="specItem" @confirm="onSpecConfirm" />
  </div>
</template>

<script setup>
import { computed, onActivated, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showToast } from 'vant'
import TabBar from '@/components/TabBar.vue'
import SpecPopup from '@/components/SpecPopup.vue'
import { addToCart, getStoreStatus, listCart, listCombo, listDrink, listCategory } from '@/api'
import { useUserStore } from '@/stores/user'

defineOptions({ name: 'MenuPage' })

const router = useRouter()
const user = useUserStore()

const categoryList = ref([])
const activeCategoryId = ref(null)
const activeType = ref(1)
const items = ref([])
const loading = ref(false)

const cartCount = ref(0)
// van-submit-bar 的 price 以「分」为单位
const cartAmount = ref(0)

const specVisible = ref(false)
const specItem = ref(null)

const isCombo = computed(() => activeType.value === 2)
const itemKey = (item) => (isCombo.value ? `c${item.id}` : `d${item.id}`)

async function loadStoreStatus() {
  try {
    user.setStoreStatus(await getStoreStatus())
  } catch {
    // 门店状态拿不到就当营业中，不挡用户浏览
  }
}

async function loadCategory() {
  const [drinks, combos] = await Promise.all([listCategory(1), listCategory(2)])
  categoryList.value = [...(drinks || []), ...(combos || [])]
  if (categoryList.value.length && activeCategoryId.value === null) {
    selectCategory(categoryList.value[0])
  }
}

async function selectCategory(category) {
  activeCategoryId.value = category.id
  activeType.value = category.type
  loading.value = true
  try {
    items.value =
      category.type === 2 ? (await listCombo(category.id)) || [] : (await listDrink(category.id)) || []
  } finally {
    loading.value = false
  }
}

async function refreshCart() {
  try {
    const list = (await listCart()) || []
    cartCount.value = list.reduce((n, c) => n + c.number, 0)
    cartAmount.value = Math.round(list.reduce((s, c) => s + Number(c.amount) * c.number, 0) * 100)
  } catch {
    cartCount.value = 0
    cartAmount.value = 0
  }
}

function onAdd(item) {
  if (!user.isStoreOpen) {
    showToast('门店已打烊，暂时无法下单')
    return
  }
  if (isCombo.value) {
    // 组合套餐没有规格，直接加一份
    addComboToCart(item)
    return
  }
  if (!item.specs || !item.specs.length) {
    // 理论上饮品都有规格，兜底按默认加一份
    addDrinkToCart(item, '', 1)
    return
  }
  specItem.value = item
  specVisible.value = true
}

async function addComboToCart(item) {
  await addToCart({ comboId: item.id })
  showSuccessToast('已加入购物车')
  refreshCart()
}

async function addDrinkToCart(item, drinkSpec, number) {
  // 后端 /add 每次只加一份，选多份就调多次
  for (let i = 0; i < number; i++) {
    await addToCart({ drinkId: item.id, drinkSpec })
  }
  showSuccessToast('已加入购物车')
  refreshCart()
}

function onSpecConfirm({ drinkSpec, number }) {
  addDrinkToCart(specItem.value, drinkSpec, number)
}

function goCart() {
  router.push('/cart')
}

function goCheckout() {
  if (!user.isStoreOpen) {
    showToast('门店已打烊，暂时无法下单')
    return
  }
  router.push('/order/confirm')
}

onMounted(async () => {
  await Promise.all([loadStoreStatus(), loadCategory()])
  refreshCart()
})

// 从购物车/下单页返回时刷新数量和门店状态
onActivated(() => {
  refreshCart()
  loadStoreStatus()
})
</script>

<style scoped>
.banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 18px 22px;
  color: #fff;
  background: linear-gradient(150deg, #2e7d6b 0%, #1f5b4d 100%);
}

.banner.closed {
  background: linear-gradient(150deg, #7c8a87 0%, #5d6b68 100%);
}

.store-name {
  font-size: 16.5px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.store-sub {
  margin-top: 6px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.72);
}

.store-badge {
  font-size: 11.5px;
  padding: 4px 11px;
  border-radius: 11px;
  background: rgba(255, 255, 255, 0.18);
}

.body {
  display: flex;
  align-items: flex-start;
  margin-top: -12px;
  border-radius: 14px 14px 0 0;
  background: #fff;
  min-height: calc(100vh - 200px);
  overflow: hidden;
}

.nav {
  width: 92px;
  flex-shrink: 0;
  background: #f6f8f7;
  align-self: stretch;
}

.nav-item {
  padding: 15px 8px;
  font-size: 13px;
  text-align: center;
  color: var(--ct-ink-soft);
  position: relative;
  line-height: 1.4;
}

.nav-item.on {
  background: #fff;
  color: var(--ct-green-deep);
  font-weight: 600;
}

.nav-item.on::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 18px;
  border-radius: 0 3px 3px 0;
  background: var(--ct-green);
}

.list {
  flex: 1;
  min-width: 0;
  padding: 6px 14px 14px;
}

.item {
  display: flex;
  gap: 11px;
  padding: 14px 0;
  border-bottom: 1px solid var(--ct-line);
}

.item:last-child {
  border-bottom: none;
}

.item-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.item-name {
  font-size: 14.5px;
  font-weight: 600;
  line-height: 1.35;
}

.item-desc {
  font-size: 11.5px;
  margin: 4px 0 8px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-price {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 7px;
}

.item-price .price {
  font-size: 16px;
}

.item-price .tag {
  font-size: 10.5px;
  padding: 1px 6px;
  border-radius: 8px;
  background: #fdf6e4;
  color: #a8842a;
}

.add-btn {
  align-self: flex-end;
  width: 28px;
  height: 28px;
  padding: 0;
}

.cart-tip {
  font-size: 12px;
  color: var(--ct-green-deep);
}
</style>
