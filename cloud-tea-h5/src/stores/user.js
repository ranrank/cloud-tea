import { defineStore } from 'pinia'
import { login as loginApi } from '@/api'
import { TOKEN_KEY } from '@/api/request'

const PROFILE_KEY = 'cloudtea_user_profile'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    profile: JSON.parse(localStorage.getItem(PROFILE_KEY) || 'null'),
    // 门店营业状态：1 营业中 / 0 打烊
    storeStatus: 1
  }),

  getters: {
    isLogin: (state) => !!state.token,
    isStoreOpen: (state) => state.storeStatus === 1,
    displayName: (state) => state.profile?.name || '茶友'
  },

  actions: {
    /**
     * 演示登录：浏览器里拿不到微信小程序的 code，
     * 后端 cloudtea.wechat.mock=true 时会把这个 code 派生成一个固定的 openid。
     */
    async login(code) {
      const data = await loginApi(code)
      this.token = data.token
      this.profile = data
      localStorage.setItem(TOKEN_KEY, data.token)
      localStorage.setItem(PROFILE_KEY, JSON.stringify(data))
      return data
    },

    async logout() {
      this.token = ''
      this.profile = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(PROFILE_KEY)
    },

    setStoreStatus(status) {
      this.storeStatus = status ?? 1
    }
  }
})
