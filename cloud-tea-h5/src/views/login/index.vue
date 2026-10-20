<template>
  <div class="login">
    <div class="hero">
      <div class="mark">云</div>
      <h1>云栖茶饮</h1>
      <p>现制茶饮 · 到店自取 · 外卖配送</p>
    </div>

    <div class="panel">
      <h2>欢迎光临</h2>
      <p class="muted tip">
        小程序正式环境走微信授权登录；这里跑在浏览器里拿不到微信 code，
        所以用演示登录。同一个设备每次都会登录成同一个人。
      </p>

      <van-button type="primary" block round size="large" :loading="loading" @click="onLogin">
        一键登录（演示模式）
      </van-button>

      <div class="foot muted">登录后可浏览菜单、加购下单、查看订单</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import { useUserStore } from '@/stores/user'

defineOptions({ name: 'LoginPage' })

const router = useRouter()
const route = useRoute()
const user = useUserStore()
const loading = ref(false)

// 固定的演示 code：后端 mock 分支会把它派生为稳定的 openid，
// 因此每次登录都是同一个用户，购物车和订单不会丢。
const DEMO_CODE = 'h5demo001'

async function onLogin() {
  loading.value = true
  try {
    await user.login(DEMO_CODE)
    showSuccessToast('登录成功')
    router.replace(route.query.redirect || '/menu')
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.hero {
  padding: 68px 30px 52px;
  color: #fff;
  background: linear-gradient(160deg, #26584e 0%, #1b3f38 60%, #14302b 100%);
  position: relative;
  overflow: hidden;
}

.hero::after {
  content: '';
  position: absolute;
  width: 260px;
  height: 260px;
  right: -90px;
  bottom: -120px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(201, 162, 39, 0.3), transparent 68%);
}

.mark {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  font-size: 28px;
  font-weight: 700;
  color: #1b3f38;
  background: linear-gradient(140deg, #d9c479, #c9a227);
  box-shadow: 0 8px 20px rgba(201, 162, 39, 0.35);
  margin-bottom: 20px;
}

.hero h1 {
  margin: 0;
  font-size: 27px;
  letter-spacing: 5px;
  font-weight: 600;
}

.hero p {
  margin: 10px 0 0;
  font-size: 12.5px;
  letter-spacing: 1px;
  color: rgba(255, 255, 255, 0.62);
}

.panel {
  flex: 1;
  padding: 32px 26px;
  border-radius: 22px 22px 0 0;
  margin-top: -22px;
  background: #fff;
  position: relative;
  z-index: 1;
}

.panel h2 {
  margin: 0 0 8px;
  font-size: 19px;
  font-weight: 600;
}

.tip {
  font-size: 12.5px;
  line-height: 1.75;
  margin: 0 0 26px;
}

.foot {
  margin-top: 20px;
  text-align: center;
  font-size: 12px;
}
</style>
