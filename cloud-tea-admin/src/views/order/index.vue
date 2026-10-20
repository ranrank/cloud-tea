<template>
  <div class="page">
    <div class="page-head">
      <div>
        <div class="page-title">订单管理</div>
        <div class="page-sub">接单、拒单、配送、完成，以及查看订单明细</div>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
    </div>

    <!-- 状态卡片 -->
    <div class="status-cards">
      <div
        v-for="s in statusCards"
        :key="s.label"
        class="status-card"
        :class="{ active: activeTab === s.status, alert: s.alert && s.count > 0 }"
        @click="switchTab(s.status)"
      >
        <div class="status-count">{{ s.count }}</div>
        <div class="status-label">{{ s.label }}</div>
      </div>
    </div>

    <div class="card">
      <div class="toolbar">
        <el-input v-model="query.number" placeholder="订单号" clearable style="width: 190px" @keyup.enter="reload" />
        <el-input v-model="query.phone" placeholder="手机号" clearable style="width: 160px" @keyup.enter="reload" />
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          range-separator="至"
          start-placeholder="下单开始"
          end-placeholder="下单结束"
          style="width: 260px"
        />
        <el-button type="primary" :icon="Search" @click="reload">查询</el-button>
        <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="number" label="订单号" width="170">
          <template #default="{ row }">
            <span class="mono">{{ row.number }}</span>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" width="170">
          <template #default="{ row }">
            <span class="mono muted">{{ row.orderTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="86">
          <template #default="{ row }">
            <el-tag :type="isDineIn(row) ? 'success' : 'info'" size="small" effect="light">
              {{ isDineIn(row) ? '堂食' : '外卖' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="收货人 / 取餐号" width="150">
          <template #default="{ row }">
            <!-- 堂食没有收货人，取餐号就是叫号的凭据 -->
            <template v-if="isDineIn(row)">
              <div class="pickup-cell mono">{{ row.pickupNo || '—' }}</div>
              <div class="muted small">到店自取</div>
            </template>
            <template v-else>
              <div>{{ row.consignee }}</div>
              <div class="mono muted small">{{ row.phone }}</div>
            </template>
          </template>
        </el-table-column>
        <el-table-column prop="orderItems" label="饮品" min-width="200">
          <template #default="{ row }">
            <span class="items">{{ row.orderItems || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="100">
          <template #default="{ row }">
            <span class="price">{{ Number(row.amount).toFixed(2) }}</span> 元
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMeta(row.status).type" size="small" effect="light">
              {{ statusMeta(row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 2" link type="success" @click="onConfirm(row)">接单</el-button>
            <el-button v-if="row.status === 2" link type="danger" @click="onReject(row)">拒单</el-button>
            <!-- 外卖走「配送」；堂食到店自取没有配送环节，制作中直接完成 -->
            <el-button v-if="row.status === 3 && !isDineIn(row)" link type="primary" @click="onDelivery(row)">配送</el-button>
            <el-button v-if="canComplete(row)" link type="success" @click="onComplete(row)">完成</el-button>
            <el-button v-if="[1, 2, 3, 4].includes(row.status)" link type="warning" @click="onCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="reload"
        />
      </div>
    </div>

    <!-- 订单详情 -->
    <el-drawer v-model="detailVisible" title="订单详情" size="520px">
      <div v-if="detail" class="detail">
        <div class="detail-head">
          <el-tag :type="statusMeta(detail.status).type" effect="dark" size="large">
            {{ statusMeta(detail.status).label }}
          </el-tag>
          <el-tag :type="isDineIn(detail) ? 'success' : 'info'" effect="plain" size="large">
            {{ isDineIn(detail) ? '堂食' : '外卖' }}
          </el-tag>
          <span class="mono muted">{{ detail.number }}</span>
        </div>

        <el-descriptions :column="1" border size="small" class="detail-desc">
          <el-descriptions-item label="下单时间">{{ detail.orderTime }}</el-descriptions-item>
          <el-descriptions-item label="结账时间">{{ detail.checkoutTime || '—' }}</el-descriptions-item>
          <!-- 堂食没有收货人和收货地址，换成取餐号 -->
          <el-descriptions-item v-if="isDineIn(detail)" label="取餐号">
            <span class="pickup-cell mono">{{ detail.pickupNo || '—' }}</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="!isDineIn(detail)" label="收货人">
            {{ detail.consignee }} · {{ detail.phone }}
          </el-descriptions-item>
          <el-descriptions-item v-if="!isDineIn(detail)" label="收货地址">{{ detail.address }}</el-descriptions-item>
          <el-descriptions-item label="订单备注">{{ detail.remark || '—' }}</el-descriptions-item>
          <el-descriptions-item label="餐具数量">
            {{ detail.tablewareStatus === 1 ? '按餐量提供' : `${detail.tablewareNumber} 份` }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.cancelReason" label="取消原因">
            {{ detail.cancelReason }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.rejectionReason" label="拒单原因">
            {{ detail.rejectionReason }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-title">饮品明细</div>
        <div class="detail-items">
          <div v-for="item in detail.orderDetailList || []" :key="item.id" class="detail-item">
            <el-image v-if="item.image" :src="item.image" class="thumb" fit="cover" />
            <div v-else class="thumb placeholder"><el-icon><ColdDrink /></el-icon></div>
            <div class="detail-item-main">
              <div class="detail-item-name">{{ item.name }}</div>
              <div class="muted small">{{ item.drinkSpec || '默认规格' }}</div>
            </div>
            <div class="detail-item-num">x{{ item.number }}</div>
            <div class="price">{{ Number(item.amount).toFixed(2) }}</div>
          </div>
          <div v-if="!(detail.orderDetailList || []).length" class="muted">无明细</div>
        </div>

        <div class="detail-total">
          <span>实收金额</span>
          <span class="total-num">{{ Number(detail.amount).toFixed(2) }} 元</span>
        </div>

        <!-- 当前状态下可执行的动作 -->
        <div class="detail-actions">
          <el-button v-if="detail.status === 2" type="success" @click="onConfirm(detail)">接单</el-button>
          <el-button v-if="detail.status === 2" type="danger" plain @click="onReject(detail)">拒单</el-button>
          <el-button
            v-if="detail.status === 3 && !isDineIn(detail)"
            type="primary"
            @click="onDelivery(detail)"
          >
            开始配送
          </el-button>
          <!-- 堂食到店自取，制作中就能直接完成，没有配送这一步 -->
          <el-button v-if="canComplete(detail)" type="success" @click="onComplete(detail)">
            完成订单
          </el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, RefreshLeft, Search } from '@element-plus/icons-vue'
import {
  cancelOrder,
  completeOrder,
  confirmOrder,
  deliveryOrder,
  orderDetails,
  orderStatistics,
  rejectOrder,
  searchOrders
} from '@/api'

const STATUS_MAP = {
  1: { label: '待付款', type: 'info' },
  2: { label: '待接单', type: 'warning' },
  3: { label: '制作中', type: 'primary' },
  4: { label: '配送中', type: 'primary' },
  5: { label: '已完成', type: 'success' },
  6: { label: '已取消', type: 'danger' }
}

const statusMeta = (status) => STATUS_MAP[status] || { label: '未知', type: 'info' }

// 堂食（1）到店自取，外卖（2）配送到家。老数据没有这个字段时按外卖处理
const isDineIn = (row) => row?.orderType === 1

// 外卖要送到「配送中」才能完成；堂食没有配送环节，制作中即可完成
const canComplete = (row) => row?.status === 4 || (isDineIn(row) && row?.status === 3)

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const activeTab = ref(null)
const dateRange = ref([])

const statistics = reactive({ toBeConfirmed: 0, confirmed: 0, deliveryInProgress: 0 })
const overview = reactive({ allOrders: 0, completedOrders: 0, cancelledOrders: 0 })

const query = reactive({ page: 1, pageSize: 10, number: '', phone: '', status: null })

const statusCards = ref([])

function buildCards() {
  statusCards.value = [
    { label: '全部订单', status: null, count: overview.allOrders, alert: false },
    { label: '待接单', status: 2, count: statistics.toBeConfirmed, alert: true },
    { label: '制作中', status: 3, count: statistics.confirmed, alert: false },
    { label: '配送中', status: 4, count: statistics.deliveryInProgress, alert: false },
    { label: '已完成', status: 5, count: overview.completedOrders, alert: false },
    { label: '已取消', status: 6, count: overview.cancelledOrders, alert: false }
  ]
}

async function loadStats() {
  const [stats, all, done, cancelled] = await Promise.allSettled([
    orderStatistics(),
    searchOrders({ page: 1, pageSize: 1 }),
    searchOrders({ page: 1, pageSize: 1, status: 5 }),
    searchOrders({ page: 1, pageSize: 1, status: 6 })
  ])
  if (stats.status === 'fulfilled') Object.assign(statistics, stats.value || {})
  // 概览总数直接用分页接口的 total，各状态数量由统计接口提供
  if (all.status === 'fulfilled') overview.allOrders = all.value?.total || 0
  // 统计接口只给待接单/制作中/配送中三个数，已完成和已取消同样取分页 total
  if (done.status === 'fulfilled') overview.completedOrders = done.value?.total || 0
  if (cancelled.status === 'fulfilled') overview.cancelledOrders = cancelled.value?.total || 0
  buildCards()
}

async function load() {
  loading.value = true
  try {
    const params = { ...query }
    if (activeTab.value !== null) params.status = activeTab.value
    if (dateRange.value?.length === 2) {
      // 后端对 LocalDateTime 入参要求 "yyyy-MM-dd HH:mm:ss"（空格分隔 + 必须带秒），
      // 写成 ISO 的 'T' 分隔会被 400 拒绝
      params.beginTime = `${dateRange.value[0]} 00:00:00`
      params.endTime = `${dateRange.value[1]} 23:59:59`
    }
    const data = await searchOrders(params)
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function reload() {
  query.page = 1
  load()
}

function switchTab(status) {
  activeTab.value = status
  reload()
}

function reset() {
  Object.assign(query, { page: 1, number: '', phone: '', status: null })
  dateRange.value = []
  activeTab.value = null
  load()
}

/* -------- 详情 -------- */
const detailVisible = ref(false)
const detail = ref(null)

async function openDetail(row) {
  detail.value = await orderDetails(row.id)
  detailVisible.value = true
}

/* -------- 状态流转 -------- */
async function refreshAfter(action) {
  await Promise.all([load(), loadStats()])
  if (detailVisible.value && detail.value) {
    detail.value = await orderDetails(detail.value.id)
  }
}

async function onConfirm(row) {
  await confirmOrder({ id: row.id, status: 3 })
  ElMessage.success('已接单，开始制作')
  refreshAfter()
}

async function onReject(row) {
  let reason
  try {
    const res = await ElMessageBox.prompt('请输入拒单原因', '拒单', {
      inputPlaceholder: '如：原料售罄，暂时无法制作',
      inputValidator: (v) => (v && v.trim() ? true : '请填写拒单原因')
    })
    reason = res.value
  } catch {
    return
  }
  await rejectOrder({ id: row.id, rejectionReason: reason })
  ElMessage.success('已拒单，款项将退回用户')
  refreshAfter()
}

async function onDelivery(row) {
  await deliveryOrder(row.id)
  ElMessage.success('已开始配送')
  refreshAfter()
}

async function onComplete(row) {
  await completeOrder(row.id)
  ElMessage.success('订单已完成')
  refreshAfter()
}

async function onCancel(row) {
  let reason
  try {
    const res = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      inputPlaceholder: '如：用户电话联系要求取消',
      inputValidator: (v) => (v && v.trim() ? true : '请填写取消原因')
    })
    reason = res.value
  } catch {
    return
  }
  await cancelOrder({ id: row.id, cancelReason: reason })
  ElMessage.success('订单已取消')
  refreshAfter()
}

// 每 30 秒静默刷新一次待接单数量，配合 WebSocket 提醒形成双保险
let timer = null

onMounted(() => {
  load()
  loadStats()
  timer = setInterval(loadStats, 30000)
})

onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.status-cards {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 14px;
}

.status-card {
  background: #fff;
  border: 1px solid var(--ct-line);
  border-radius: 12px;
  padding: 15px 16px;
  cursor: pointer;
  transition: all 0.18s;
}

.status-card:hover {
  border-color: var(--ct-green);
  transform: translateY(-1px);
}

.status-card.active {
  border-color: var(--ct-green);
  background: var(--ct-green-soft);
}

.status-card.alert {
  border-color: #eec78a;
  background: #fdf7ee;
}

.status-count {
  font-size: 23px;
  font-weight: 600;
  color: var(--ct-green-deep);
}

.status-card.alert .status-count {
  color: #d98820;
}

.status-label {
  margin-top: 3px;
  font-size: 12.5px;
  color: var(--ct-ink-soft);
}

.small {
  font-size: 11.5px;
}

.price {
  font-weight: 600;
  color: var(--ct-green-deep);
}

/* 堂食取餐号，叫号时要一眼看清 */
.pickup-cell {
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1px;
  color: var(--ct-green-deep);
}

.items {
  font-size: 12.5px;
  color: #4a5c58;
}

/* 详情抽屉 */
.detail-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.detail-desc {
  margin-bottom: 18px;
}

.detail-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
}

.detail-items {
  border: 1px solid var(--ct-line);
  border-radius: 10px;
  padding: 6px 12px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f2f6f5;
}

.detail-item:last-child {
  border-bottom: none;
}

.thumb {
  width: 42px;
  height: 42px;
  border-radius: 8px;
  flex-shrink: 0;
}

.thumb.placeholder {
  display: grid;
  place-items: center;
  background: var(--ct-green-soft);
  color: var(--ct-green);
  font-size: 18px;
}

.detail-item-main {
  flex: 1;
  min-width: 0;
}

.detail-item-name {
  font-size: 13.5px;
  font-weight: 600;
}

.detail-item-num {
  font-size: 13px;
  color: var(--ct-ink-soft);
}

.detail-total {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed var(--ct-line);
  font-size: 13.5px;
  color: var(--ct-ink-soft);
}

.total-num {
  font-size: 20px;
  font-weight: 600;
  color: var(--ct-green-deep);
}

.detail-actions {
  margin-top: 22px;
  display: flex;
  gap: 10px;
}

@media (max-width: 1200px) {
  .status-cards {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
