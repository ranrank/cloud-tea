import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi } from '@/api'
import { TOKEN_KEY } from '@/api/request'

const PROFILE_KEY = 'cloudtea_admin_profile'

export const useEmployeeStore = defineStore('employee', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    profile: JSON.parse(localStorage.getItem(PROFILE_KEY) || 'null')
  }),

  getters: {
    isLogin: (state) => !!state.token,
    displayName: (state) => state.profile?.name || state.profile?.userName || '未登录'
  },

  actions: {
    async login(payload) {
      const data = await loginApi(payload)
      this.token = data.token
      this.profile = data
      localStorage.setItem(TOKEN_KEY, data.token)
      localStorage.setItem(PROFILE_KEY, JSON.stringify(data))
      return data
    },

    async logout() {
      try {
        await logoutApi()
      } catch {
        // 后端登出失败也要让本地状态清干净
      }
      this.token = ''
      this.profile = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(PROFILE_KEY)
    }
  }
})
