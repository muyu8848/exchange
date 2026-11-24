export function getTodayStorageKey(prefix: string) {
  const today = new Date()
  const y = today.getFullYear()
  const m = today.getMonth() + 1
  const d = today.getDate()
  return `${prefix}${y}${m}${d}`
}

export function parseDateTime(value?: string) {
  if (!value) {
    return null
  }
  const date = new Date(value.includes('T') ? value : value.replace(' ', 'T'))
  return Number.isNaN(date.getTime()) ? null : date
}

export function stripSeconds(value?: string) {
  if (!value) return '—'
  // 后端固定返回 yyyy-MM-dd HH:mm:ss，截前 16 位去掉秒；纯字符串操作，无时区解析
  return value.slice(0, 16)
}

export function isRecentPublish(value?: string, withinDays = 7) {
  const date = parseDateTime(value)
  if (!date) {
    return false
  }
  const diffMs = Date.now() - date.getTime()
  return diffMs >= 0 && diffMs < withinDays * 24 * 60 * 60 * 1000
}

/** 格式化为 YYYY-MM-DD */
export function formatDate(d: Date): string {
  const pad = (n: number) => (n < 10 ? `0${n}` : `${n}`)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/** 近 N 天范围（含今天），days<=0 表示今天 */
export function lastNDays(days: number): { start: string; end: string } {
  const today = new Date()
  const end = formatDate(today)
  if (days <= 0) {
    return { start: end, end }
  }
  const start = new Date(today)
  start.setDate(start.getDate() - (days - 1))
  return { start: formatDate(start), end }
}
