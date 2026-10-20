import axios from 'axios'
import { ElMessage } from 'element-plus'

export const TOKEN_KEY = 'cloudtea_admin_token'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 20000
})

// 请求拦截：带上 JWT。后端管理端约定的请求头名就是 token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.token = token
  }
  return config
})

// 响应拦截：后端统一返回 { code, msg, data }，code 为 1 表示成功
request.interceptors.response.use(
  (response) => {
    // 导出 Excel 走 blob，交给调用方自己处理
    if (response.config.responseType === 'blob') {
      return response
    }
    const { code, msg, data } = response.data
    if (code === 1) {
      return data
    }
    ElMessage.error(msg || '操作失败')
    return Promise.reject(new Error(msg || '操作失败'))
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      ElMessage.error('登录已失效，请重新登录')
      // 直接改 hash，避免在这里 import router 造成循环依赖
      window.location.hash = '#/login'
    } else {
      ElMessage.error(error.response?.data?.msg || error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default request
