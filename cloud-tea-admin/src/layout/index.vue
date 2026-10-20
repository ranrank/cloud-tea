<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">云</div>
        <div class="brand-text">
          <div class="brand-name">云栖茶饮</div>
          <div class="brand-sub">连锁点单平台</div>
        </div>
      </div>

      <el-menu
        :default-active="route.path"
        router
        class="menu"
        background-color="transparent"
        text-color="rgba(255,255,255,.72)"
        active-text-color="#ffffff"
      >
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.meta.icon" /></el-icon>
          <span>{{ item.meta.title }}</span>
        </el-menu-item>
      </el-menu>

      <div class="socket-state">
        <span class="dot" :class="{ on: connected }"></span>
        {{ connected ? '来单提醒已连接' : '提醒通道重连中…' }}
      </div>
    </aside>

    <!-- 主区 -->
    <div class="main">
      <header class="topbar">
        <div class="topbar-title">{{ route.meta?.title || '工作台' }}</div>

        <div class="topbar-right">
          <div class="store-switch">
            <span class="muted">门店状态</span>
            <el-switch
              v-model="storeOpen"
              inline-prompt
              active-text="营业"
              inactive-text="打烊"
              @change="onStoreChange"
            />
          </div>

          <el-dropdown @command="onCommand">
            <span class="who">
              <el-avatar :size="28" class="avatar">{{ displayName.slice(0, 1) }}</el-avatar>
              {{ displayName }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content">
        <router-view v-slot="{ Component }">
          <component :is="Component" />
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import { useEmployeeStore } from '@/stores/employee'
import { useStoreStore } from '@/stores/store'
import { useOrderSocket } from '@/composables/useOrderSocket'

const route = useRoute()
const router = useRouter()
const employee = useEmployeeStore()
const store = useStoreStore()

const menus = router.options.routes
  .find((r) => r.path === '/')
  .children.map((c) => ({ path: `/${c.path}`, meta: c.meta }))

const displayName = computed(() => employee.displayName)
const storeOpen = ref(true)

onMounted(async () => {
  try {
    storeOpen.value = (await store.fetch()) === 1
  } catch {
    // 门店状态读不到不影响其它功能
  }
})

async function onStoreChange(value) {
  const status = value ? 1 : 0
  try {
    await store.update(status)
    ElNotification({
      title: '门店状态已更新',
      message: status === 1 ? '已切换为营业中，用户可以点单了' : '已切换为打烊，用户端将无法下单',
      type: 'success'
    })
  } catch {
    storeOpen.value = !value // 失败回滚开关
  }
}

async function onCommand(command) {
  if (command === 'logout') {
    await employee.logout()
    router.replace('/login')
  }
}

// 来单提醒（type=1）与客户催单（type=2）
const { connected } = useOrderSocket((payload) => {
  const isNewOrder = payload.type === 1
  ElNotification({
    title: isNewOrder ? '🔔 来新单了' : '⏰ 客户催单',
    message: `${payload.content}${isNewOrder ? '，请尽快接单' : '，请优先处理'}`,
    type: isNewOrder ? 'success' : 'warning',
    duration: isNewOrder ? 8000 : 10000,
    position: 'bottom-right'
  })
})
</script>

<style scoped>
.layout {
  display: flex;
  height: 100%;
}

.sidebar {
  width: 220px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: linear-gradient(170deg, #26584e 0%, #1b3f38 60%, #16332e 100%);
  color: #fff;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 18px 18px;
}

.brand-mark {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  font-size: 19px;
  font-weight: 700;
  color: #1b3f38;
  background: linear-gradient(140deg, #d9c479, #c9a227);
  box-shadow: 0 4px 12px rgba(201, 162, 39, 0.35);
}

.brand-name {
  font-size: 15.5px;
  font-weight: 600;
  letter-spacing: 1.5px;
}

.brand-sub {
  font-size: 11px;
  letter-spacing: 0.6px;
  color: rgba(255, 255, 255, 0.5);
}

.menu {
  flex: 1;
  border-right: none;
  padding: 6px 10px;
}

.menu :deep(.el-menu-item) {
  height: 44px;
  margin-bottom: 4px;
  border-radius: 8px;
  font-size: 14px;
}

.menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
}

.menu :deep(.el-menu-item.is-active) {
  background: rgba(201, 162, 39, 0.22) !important;
  font-weight: 600;
}

.socket-state {
  margin: 12px 18px 18px;
  font-size: 11.5px;
  color: rgba(255, 255, 255, 0.5);
  display: flex;
  align-items: center;
  gap: 6px;
}

.socket-state .dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #a55;
}

.socket-state .dot.on {
  background: #5fd39a;
  box-shadow: 0 0 6px #5fd39a;
}

.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.topbar {
  height: 58px;
  flex-shrink: 0;
  background: #fff;
  border-bottom: 1px solid var(--ct-line);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.topbar-title {
  font-size: 16px;
  font-weight: 600;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 22px;
}

.store-switch {
  display: flex;
  align-items: center;
  gap: 9px;
  font-size: 13px;
}

.who {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
  cursor: pointer;
  outline: none;
}

.avatar {
  background: var(--ct-green);
  font-size: 13px;
}

.content {
  flex: 1;
  overflow: auto;
}
</style>
