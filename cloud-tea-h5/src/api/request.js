import axios from 'axios'
import { showToast } from 'vant'

export const TOKEN_KEY = 'cloudtea_user_token'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 20000
})

// 用户端的 JWT 约定放在 authentication 这个请求头里
request.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.authentication = token
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const { code, msg, data } = response.data
    if (code === 1) {
      return data
    }
    showToast(msg || '操作失败')
    return Promise.reject(new Error(msg || '操作失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      showToast('请先登录')
      window.location.hash = '#/login'
    } else {
      showToast(error.response?.data?.msg || error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default request
