<template>
  <div class="mine page">
    <div class="hero">
      <div class="avatar">{{ user.displayName.slice(0, 1) }}</div>
      <div class="who">
        <div class="name">{{ user.displayName }}</div>
        <div class="sub">{{ user.isLogin ? '已登录 · 演示账号' : '未登录' }}</div>
      </div>
    </div>

    <van-cell-group inset class="block">
      <van-cell title="我的订单" is-link icon="orders-o" @click="$router.push('/order')" />
      <van-cell title="收货地址" is-link icon="location-o" @click="$router.push('/address')" />
      <van-cell title="去点单" is-link icon="shop-o" @click="$router.push('/menu')" />
    </van-cell-group>

    <van-cell-group inset title="门店" class="block">
      <van-cell title="营业状态">
        <template #value>
          <van-tag :type="user.isStoreOpen ? 'primary' : 'default'">
            {{ user.isStoreOpen ? '营业中' : '已打烊' }}
          </van-tag>
        </template>
      </van-cell>
      <van-cell title="门店地址" value="北京市海淀区中关村软件园" />
    </van-cell-group>

    <div class="actions">
      <van-button block round @click="onLogout">退出登录</van-button>
    </div>

    <div class="foot muted">云栖茶饮 · 连锁点单平台</div>

    <TabBar />
  </div>
</template>

<script setup>
import { onActivated, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import TabBar from '@/components/TabBar.vue'
import { getStoreStatus } from '@/api'
import { useUserStore } from '@/stores/user'

defineOptions({ name: 'MinePage' })

const router = useRouter()
const user = useUserStore()

async function loadStoreStatus() {
  try {
    user.setStoreStatus(await getStoreStatus())
  } catch {
    // 拿不到不影响其它功能
  }
}

function onLogout() {
  user.logout()
  showSuccessToast('已退出登录')
  router.replace('/login')
}

onMounted(loadStoreStatus)
onActivated(loadStoreStatus)
</script>

<style scoped>
.hero {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 34px 22px 40px;
  color: #fff;
  background: linear-gradient(150deg, #2e7d6b 0%, #1f5b4d 100%);
}

.avatar {
  width: 54px;
  height: 54px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 22px;
  font-weight: 700;
  color: #1b3f38;
  background: linear-gradient(140deg, #d9c479, #c9a227);
}

.name {
  font-size: 17px;
  font-weight: 600;
}

.sub {
  margin-top: 5px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.block {
  margin-top: 12px;
}

.block:first-of-type {
  margin-top: -18px;
}

.actions {
  padding: 26px 16px 10px;
}

.foot {
  text-align: center;
  font-size: 11.5px;
  padding: 20px 0 10px;
}
</style>
