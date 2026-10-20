/**
 * 订单 WebSocket：接收后端推送的来单提醒与客户催单。
 * 后端在 com.cloudtea.websocket.WebSocketServer 里群发：
 *   { type: 1, orderId, content }  —— 来单提醒
 *   { type: 2, orderId, content }  —— 客户催单
 */
import { onMounted, onUnmounted, ref } from 'vue'

const RECONNECT_DELAY = 3000

export function useOrderSocket(onMessage) {
  const connected = ref(false)
  let socket = null
  let timer = null
  let disposed = false

  function connect() {
    const base = import.meta.env.VITE_WS_BASE || 'ws://localhost:8080'
    const sid = `admin-${Date.now()}`

    socket = new WebSocket(`${base}/ws/${sid}`)

    socket.onopen = () => {
      connected.value = true
    }

    socket.onmessage = (event) => {
      let payload
      try {
        payload = JSON.parse(event.data)
      } catch {
        return // 非 JSON 消息直接忽略
      }
      onMessage?.(payload)
    }

    socket.onclose = () => {
      connected.value = false
      if (!disposed) {
        // 后端重启或网络抖动时自动重连
        timer = setTimeout(connect, RECONNECT_DELAY)
      }
    }

    socket.onerror = () => {
      socket?.close()
    }
  }

  onMounted(connect)

  onUnmounted(() => {
    disposed = true
    clearTimeout(timer)
    socket?.close()
  })

  return { connected }
}
