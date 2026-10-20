<template>
  <div class="address-list page">
    <van-nav-bar title="收货地址" left-arrow @click-left="$router.back()">
      <template #right>
        <span class="nav-add" @click="$router.push('/address/edit')">新增</span>
      </template>
    </van-nav-bar>

    <van-empty v-if="!loading && !list.length" description="还没有收货地址">
      <van-button type="primary" round size="small" @click="$router.push('/address/edit')">
        新增地址
      </van-button>
    </van-empty>

    <div v-else class="cards">
      <div
        v-for="item in list"
        :key="item.id"
        class="card"
        :class="{ current: item.isDefault === 1 }"
        @click="onPick(item)"
      >
        <div class="card-main">
          <div class="line1">
            <span class="name">{{ item.consignee }}</span>
            <span class="muted phone">{{ item.phone }}</span>
            <van-tag v-if="item.label" plain type="primary" class="tag">{{ item.label }}</van-tag>
            <van-tag v-if="item.isDefault === 1" type="primary" class="tag">默认</van-tag>
          </div>
          <div class="detail muted">
            {{ item.provinceName }}{{ item.cityName }}{{ item.districtName }}{{ item.detail }}
          </div>
        </div>

        <div class="card-ops" @click.stop>
          <van-icon name="edit" @click="$router.push(`/address/edit/${item.id}`)" />
          <van-icon name="delete-o" @click="onDelete(item)" />
        </div>
      </div>
    </div>

    <div class="hint muted">点击地址卡片可将其设为默认收货地址</div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { showConfirmDialog, showSuccessToast } from 'vant'
import { deleteAddress, listAddress, setDefaultAddress } from '@/api'

defineOptions({ name: 'AddressListPage' })

const list = ref([])
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    list.value = (await listAddress()) || []
  } finally {
    loading.value = false
  }
}

async function onPick(item) {
  if (item.isDefault === 1) return
  await setDefaultAddress({ id: item.id })
  showSuccessToast('已设为默认地址')
  load()
}

async function onDelete(item) {
  try {
    await showConfirmDialog({ title: '删除地址', message: `确定删除「${item.consignee}」这条地址吗？` })
  } catch {
    return
  }
  await deleteAddress(item.id)
  showSuccessToast('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.nav-add {
  font-size: 13.5px;
  color: var(--ct-green-deep);
}

.cards {
  padding: 12px 12px 0;
}

.card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 15px 15px;
  margin-bottom: 11px;
  border: 1.5px solid transparent;
  transition: border-color 0.18s;
}

.card.current {
  border-color: var(--ct-green);
  background: #fbfdfc;
}

.card-main {
  flex: 1;
  min-width: 0;
}

.line1 {
  display: flex;
  align-items: center;
  gap: 9px;
  flex-wrap: wrap;
}

.name {
  font-size: 15px;
  font-weight: 600;
}

.phone {
  font-size: 12.5px;
}

.tag {
  font-size: 10px;
}

.detail {
  font-size: 12.5px;
  margin-top: 7px;
  line-height: 1.55;
}

.card-ops {
  display: flex;
  gap: 16px;
  padding-left: 12px;
  font-size: 17px;
  color: var(--ct-ink-soft);
}

.hint {
  text-align: center;
  font-size: 11.5px;
  padding: 18px;
}
</style>
