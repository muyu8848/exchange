/**
 * 格式化「已等待时长」。
 * <p>
 * 入参必须是绝对时间戳（epoch 毫秒）。不要传 yyyy-MM-dd HH:mm:ss 字符串：
 * 无时区字符串会被 dayjs 按浏览器本地时区解析，跨时区会算错，甚至出现负数
 * （负数又会被 `minutes < 1` 判成「刚刚」，把数小时积压显示成刚提交）。
 */
export function formatWaitingDuration(timestamp?: number | null): string {
  if (!timestamp) {
    return '-';
  }
  // 兜住客户端时钟快于服务器的情况，避免出现负时长
  const minutes = Math.floor(Math.max(0, Date.now() - timestamp) / 60000);
  if (minutes < 1) {
    return '刚刚';
  }
  if (minutes < 60) {
    return `${minutes}分钟`;
  }
  const hours = Math.floor(minutes / 60);
  const remainMinutes = minutes % 60;
  if (hours < 24) {
    return remainMinutes > 0 ? `${hours}小时${remainMinutes}分` : `${hours}小时`;
  }
  const days = Math.floor(hours / 24);
  const remainHours = hours % 24;
  return remainHours > 0 ? `${days}天${remainHours}小时` : `${days}天`;
}

/** 已等待是否超过指定小时数（默认 24 小时），用于超时标红 */
export function isWaitingOverdue(timestamp?: number | null, hours = 24): boolean {
  if (!timestamp) {
    return false;
  }
  return Date.now() - timestamp >= hours * 60 * 60 * 1000;
}
