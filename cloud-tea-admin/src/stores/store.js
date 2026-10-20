import { defineStore } from 'pinia'
import { getStoreStatus, setStoreStatus } from '@/api'

/**
 * 门店营业状态。后端存在 Redis 里，1 营业中 / 0 打烊。
 */
export const useStoreStore = defineStore('store', {
  state: () => ({
    status: 1
  }),

  getters: {
    isOpen: (state) => state.status === 1
  },

  actions: {
    async fetch() {
      const status = await getStoreStatus()
      this.status = status ?? 1
      return this.status
    },

    async update(status) {
      await setStoreStatus(status)
      this.status = status
      return status
    }
  }
})
