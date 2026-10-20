<template>
  <van-tabbar v-model="active" active-color="#2e7d6b" inactive-color="#9aa8a4" route safe-area-inset-bottom>
    <van-tabbar-item to="/menu" icon="shop-o">点单</van-tabbar-item>
    <van-tabbar-item to="/order" icon="orders-o">订单</van-tabbar-item>
    <van-tabbar-item to="/mine" icon="user-o">我的</van-tabbar-item>
  </van-tabbar>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const active = ref(0)

const order = ['/menu', '/order', '/mine']
watch(
  () => route.path,
  (path) => {
    const idx = order.indexOf(path)
    if (idx >= 0) active.value = idx
  },
  { immediate: true }
)
</script>
