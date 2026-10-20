import { createRouter, createWebHashHistory } from 'vue-router'
import { TOKEN_KEY } from '@/api/request'

const routes = [
  { path: '/', redirect: '/menu' },
  {
    path: '/menu',
    name: 'MenuPage',
    component: () => import('@/views/menu/index.vue'),
    meta: { title: '点单', tab: true }
  },
  {
    path: '/order',
    name: 'OrderListPage',
    component: () => import('@/views/order/list.vue'),
    meta: { title: '我的订单', tab: true }
  },
  {
    path: '/mine',
    name: 'MinePage',
    component: () => import('@/views/mine/index.vue'),
    meta: { title: '我的', tab: true }
  },
  { path: '/login', name: 'LoginPage', component: () => import('@/views/login/index.vue'), meta: { title: '登录' } },
  { path: '/cart', name: 'CartPage', component: () => import('@/views/cart/index.vue'), meta: { title: '购物车' } },
  {
    path: '/order/confirm',
    name: 'OrderConfirmPage',
    component: () => import('@/views/order/confirm.vue'),
    meta: { title: '确认订单' }
  },
  {
    path: '/order/detail/:id',
    name: 'OrderDetailPage',
    component: () => import('@/views/order/detail.vue'),
    meta: { title: '订单详情' }
  },
  {
    path: '/address',
    name: 'AddressListPage',
    component: () => import('@/views/address/list.vue'),
    meta: { title: '收货地址' }
  },
  {
    path: '/address/edit/:id?',
    name: 'AddressEditPage',
    component: () => import('@/views/address/edit.vue'),
    meta: { title: '编辑地址' }
  },
  { path: '/:pathMatch(.*)*', redirect: '/menu' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

const WHITE_LIST = ['/login']

router.beforeEach((to) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (!token && !WHITE_LIST.includes(to.path)) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  document.title = to.meta?.title ? `${to.meta.title} · 云栖茶饮` : '云栖茶饮'
  return true
})

export default router
