import { createRouter, createWebHashHistory } from 'vue-router'
import { TOKEN_KEY } from '@/api/request'

const routes = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'Odometer' }
      },
      {
        path: 'statistics',
        component: () => import('@/views/statistics/index.vue'),
        meta: { title: '数据统计', icon: 'TrendCharts' }
      },
      {
        path: 'order',
        component: () => import('@/views/order/index.vue'),
        meta: { title: '订单管理', icon: 'List' }
      },
      {
        path: 'drink',
        component: () => import('@/views/drink/index.vue'),
        meta: { title: '饮品管理', icon: 'ColdDrink' }
      },
      {
        path: 'combo',
        component: () => import('@/views/combo/index.vue'),
        meta: { title: '组合套餐', icon: 'ShoppingBag' }
      },
      {
        path: 'category',
        component: () => import('@/views/category/index.vue'),
        meta: { title: '分类管理', icon: 'Collection' }
      },
      {
        path: 'employee',
        component: () => import('@/views/employee/index.vue'),
        meta: { title: '员工管理', icon: 'User' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  // 用 hash 模式：不管用什么静态服务器托管 dist 都不会 404
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (!token && to.path !== '/login') {
    return '/login'
  }
  if (token && to.path === '/login') {
    return '/dashboard'
  }
  document.title = to.meta?.title ? `${to.meta.title} · 云栖茶饮` : '云栖茶饮'
  return true
})

export default router
