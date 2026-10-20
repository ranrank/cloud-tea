<template>
  <van-popup
    :show="show"
    position="bottom"
    round
    closeable
    :style="{ maxHeight: '82%' }"
    @update:show="(v) => emit('update:show', v)"
  >
    <div v-if="item" class="spec-popup">
      <!-- 头部 -->
      <div class="head">
        <img v-if="item.image" :src="item.image" class="hero-img" alt="" />
        <div v-else class="hero-img placeholder">🥤</div>
        <div class="head-main">
          <div class="name">{{ item.name }}</div>
          <div class="muted desc">{{ item.description || '现制饮品' }}</div>
          <div class="price">{{ Number(item.price).toFixed(2) }}</div>
        </div>
      </div>

      <!-- 规格组 -->
      <div class="body">
        <div v-for="(group, gi) in specGroups" :key="gi" class="group">
          <div class="group-title">
            {{ group.specName }}
            <span class="muted">（单选）</span>
          </div>
          <div class="chips">
            <span
              v-for="value in group.values"
              :key="value"
              class="chip"
              :class="{ on: picked[gi] === value }"
              @click="picked[gi] = value"
            >
              {{ value }}
            </span>
          </div>
        </div>

        <!-- 数量 -->
        <div class="group qty-row">
          <div class="group-title">数量</div>
          <van-stepper v-model="count" :min="1" :max="20" />
        </div>
      </div>

      <!-- 底部 -->
      <div class="foot">
        <div class="foot-price">
          合计 <span class="price">{{ total }}</span>
        </div>
        <van-button type="primary" round :disabled="!ready" @click="onConfirm">
          加入购物车
        </van-button>
      </div>
    </div>
  </van-popup>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { showToast } from 'vant'
import { parseSpecValues } from '@/utils/spec'

const props = defineProps({
  show: { type: Boolean, default: false },
  item: { type: Object, default: null }
})

const emit = defineEmits(['update:show', 'confirm'])

const picked = ref({})
const count = ref(1)

// 把后端的规格组 [{specName, specValues}] 解析成 [{specName, values:[...]}]
const specGroups = computed(() =>
  (props.item?.specs || []).map((s) => ({
    specName: s.specName,
    values: parseSpecValues(s.specValues)
  }))
)

// 每一项是否都已经选好
const ready = computed(() =>
  specGroups.value.length === 0 || specGroups.value.every((_, i) => !!picked.value[i])
)

const total = computed(() =>
  props.item ? (Number(props.item.price) * count.value).toFixed(2) : '0.00'
)

// 每次打开都重置，并把每组默认选中第一个，减少用户点击
watch(
  () => props.show,
  (open) => {
    if (!open) return
    count.value = 1
    const next = {}
    specGroups.value.forEach((g, i) => {
      next[i] = g.values[0] || ''
    })
    picked.value = next
  },
  { immediate: true }
)

function onConfirm() {
  if (!ready.value) {
    showToast('请先选完规格')
    return
  }
  const specText = specGroups.value.map((g, i) => picked.value[i]).filter(Boolean).join('/')
  emit('confirm', { drinkSpec: specText, number: count.value })
  emit('update:show', false)
}
</script>

<style scoped>
.spec-popup {
  padding-bottom: 74px;
}

.head {
  display: flex;
  gap: 14px;
  padding: 20px 18px 16px;
  border-bottom: 8px solid #f6f8f7;
}

.hero-img {
  width: 82px;
  height: 82px;
  border-radius: 12px;
  flex-shrink: 0;
  background: var(--ct-green-soft);
}

.hero-img.placeholder {
  display: grid;
  place-items: center;
  font-size: 30px;
}

.head-main {
  flex: 1;
  min-width: 0;
}

.name {
  font-size: 16.5px;
  font-weight: 600;
}

.desc {
  font-size: 12px;
  margin: 5px 0 8px;
  line-height: 1.5;
}

.price {
  color: var(--ct-green-deep);
  font-weight: 600;
  font-size: 17px;
}

.price::before {
  content: '¥';
  font-size: 0.72em;
  margin-right: 1px;
}

.body {
  padding: 4px 18px 12px;
}

.group {
  padding: 14px 0 4px;
}

.group-title {
  font-size: 13.5px;
  font-weight: 600;
  margin-bottom: 10px;
}

.group-title .muted {
  font-weight: 400;
  font-size: 11.5px;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 9px;
}

.chip {
  padding: 7px 15px;
  font-size: 13px;
  border-radius: 16px;
  background: #f4f7f6;
  color: var(--ct-ink);
  border: 1px solid transparent;
  transition: all 0.15s;
}

.chip.on {
  background: var(--ct-green-soft);
  border-color: var(--ct-green);
  color: var(--ct-green-deep);
  font-weight: 600;
}

.qty-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 16px;
}

.qty-row .group-title {
  margin-bottom: 0;
}

.foot {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 66px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 18px calc(env(safe-area-inset-bottom) / 2);
  background: #fff;
  border-top: 1px solid var(--ct-line);
}

.foot-price {
  font-size: 13px;
  color: var(--ct-ink-soft);
}

.foot-price .price {
  font-size: 21px;
  margin-left: 4px;
}
</style>
