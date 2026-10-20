/** 把 Date 或 ISO 串格式化成 yyyy-MM-dd */
export function toDateStr(value) {
  if (!value) return ''
  const d = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(d.getTime())) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/** 把 "2026-09-17,2026-09-18" 这样的逗号串拆成数组 */
export function splitCsv(str) {
  if (!str) return []
  return String(str)
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
}

/** 后端返回的 days 里每个值用逗号拼成字符串，这里统一转成数字数组 */
export function splitCsvNumbers(str) {
  return splitCsv(str).map((s) => {
    const n = Number(s)
    return Number.isFinite(n) ? n : 0
  })
}

/** 业务口径的最近 N 天（含今天），返回 [begin, end] 的 yyyy-MM-dd */
export function lastNDays(n) {
  const end = new Date()
  const begin = new Date()
  begin.setDate(end.getDate() - (n - 1))
  return [toDateStr(begin), toDateStr(end)]
}

/** 把规格组的 JSON 数组字符串解析出来，解析失败就当空数组 */
export function parseSpecValues(raw) {
  if (!raw) return []
  try {
    const arr = JSON.parse(raw)
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}
