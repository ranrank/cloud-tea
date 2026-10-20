/**
 * 后端 drink_spec.spec_values 存的是 JSON 数组字符串，如 ["中杯","大杯"]。
 * 这里统一做一次容错解析，解析不出来就当空数组，避免脏数据把页面搞崩。
 */
export function parseSpecValues(raw) {
  if (!raw) return []
  try {
    const arr = JSON.parse(raw)
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}

/** 把 Date 或 ISO 串格式化成 yyyy-MM-dd HH:mm */
export function toDateTime(value) {
  if (!value) return ''
  // 后端返回的是 "2026-09-17T20:04:31"，直接替换分隔符即可，避免时区二次转换
  return String(value).replace('T', ' ').slice(0, 16)
}

/** 订单状态文案 */
export const ORDER_STATUS = {
  1: { label: '待付款', tone: 'warn' },
  2: { label: '待接单', tone: 'warn' },
  3: { label: '制作中', tone: 'primary' },
  4: { label: '配送中', tone: 'primary' },
  5: { label: '已完成', tone: 'muted' },
  6: { label: '已取消', tone: 'muted' }
}

export function statusLabel(status) {
  return ORDER_STATUS[status]?.label || '未知'
}
