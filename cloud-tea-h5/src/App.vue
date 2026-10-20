<template>
  <router-view v-slot="{ Component }">
    <keep-alive :include="['MenuPage', 'MinePage']">
      <component :is="Component" />
    </keep-alive>
  </router-view>
</template>
