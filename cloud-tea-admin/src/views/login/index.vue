<template>
  <div class="login">
    <!-- 左侧品牌区 -->
    <div class="brand-side">
      <div class="brand-inner">
        <div class="mark">云</div>
        <h1>云栖茶饮</h1>
        <p class="slogan">连锁门店 · 统一点单 · 数据经营</p>

        <ul class="points">
          <li><span></span>分类 / 饮品 / 组合套餐一体化管理</li>
          <li><span></span>订单实时来单提醒与催单响应</li>
          <li><span></span>营业额、用户、销量多维统计</li>
        </ul>

        <div class="foot">云栖茶饮 · 连锁点单平台</div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="form-side">
      <div class="form-inner">
        <h2>管理端登录</h2>
        <p class="muted sub">请使用门店员工账号登录</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          size="large"
          @keyup.enter="onSubmit"
        >
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" clearable>
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" show-password>
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            class="submit"
            :loading="loading"
            @click="onSubmit"
          >
            登 录
          </el-button>
        </el-form>

        <div class="tip">
          <el-icon><InfoFilled /></el-icon>
          演示账号：<b>admin</b> / <b>123456</b>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useEmployeeStore } from '@/stores/employee'

const router = useRouter()
const employee = useEmployeeStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: '123456' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = await employee.login({ ...form })
    ElMessage.success(`欢迎回来，${data.name || data.userName}`)
    router.replace('/dashboard')
  } catch {
    // 错误提示已由 axios 拦截器统一弹出
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login {
  display: flex;
  height: 100%;
  background: #f4f7f6;
}

.brand-side {
  flex: 1.05;
  background: linear-gradient(160deg, #26584e 0%, #1b3f38 55%, #102823 100%);
  color: #fff;
  display: grid;
  place-items: center;
  position: relative;
  overflow: hidden;
}

/* 背景上一圈淡淡的光晕，避免大面积纯色发闷 */
.brand-side::after {
  content: '';
  position: absolute;
  width: 520px;
  height: 520px;
  right: -160px;
  bottom: -200px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(201, 162, 39, 0.24), transparent 68%);
}

.brand-inner {
  position: relative;
  z-index: 1;
  width: 340px;
}

.mark {
  width: 54px;
  height: 54px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-size: 27px;
  font-weight: 700;
  color: #1b3f38;
  background: linear-gradient(140deg, #d9c479, #c9a227);
  box-shadow: 0 8px 22px rgba(201, 162, 39, 0.4);
  margin-bottom: 22px;
}

.brand-inner h1 {
  margin: 0;
  font-size: 34px;
  letter-spacing: 5px;
  font-weight: 600;
}

.slogan {
  margin: 12px 0 34px;
  font-size: 13.5px;
  letter-spacing: 1.2px;
  color: rgba(255, 255, 255, 0.6);
}

.points {
  list-style: none;
  padding: 0;
  margin: 0;
}

.points li {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13.5px;
  color: rgba(255, 255, 255, 0.78);
  margin-bottom: 13px;
}

.points span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #c9a227;
  box-shadow: 0 0 8px rgba(201, 162, 39, 0.8);
}

.foot {
  margin-top: 44px;
  font-size: 11.5px;
  letter-spacing: 1px;
  color: rgba(255, 255, 255, 0.3);
}

.form-side {
  flex: 1;
  display: grid;
  place-items: center;
}

.form-inner {
  width: 330px;
}

.form-inner h2 {
  margin: 0 0 6px;
  font-size: 23px;
  font-weight: 600;
}

.sub {
  margin: 0 0 28px;
  font-size: 13px;
}

.submit {
  width: 100%;
  margin-top: 6px;
  letter-spacing: 3px;
}

.tip {
  margin-top: 22px;
  padding: 10px 12px;
  font-size: 12.5px;
  color: var(--ct-ink-soft);
  background: var(--ct-green-soft);
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 7px;
}

.tip b {
  color: var(--ct-green-deep);
}

@media (max-width: 900px) {
  .brand-side {
    display: none;
  }
}
</style>
