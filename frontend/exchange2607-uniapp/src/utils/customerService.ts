import { apiData } from '@/api/http'
import { settingApi } from '@/api/modules'
import router from '@/router'

export function openCustomerService(url: string) {
  if (!url) {
    return false
  }

  // #ifdef H5
  window.open(url, '_blank')
  return true
  // #endif

  // #ifndef H5
  // 注意：不要在这里手动 encodeURIComponent。
  // uni-app 的 router.push 会对 query 自动序列化编码，页面 onLoad 拿到的是解码后的值；
  // 若此处再编码一次，页面只能解一层，web-view 会拿到 "https%3A%2F%2F..." 并被当成
  // 本地相对路径（file:///.../www/https%3A%2F%2F...），触发 dcloud_error。
  router.push({
    name: 'customerService',
    query: { url },
  })
  return true
  // #endif
}

let fetching = false

// 直接拉取客服链接并打开（按需请求，不缓存）
export async function openCustomerServiceAsync(): Promise<boolean> {
  if (fetching) {
    return false
  }
  fetching = true
  try {
    const url = await apiData(settingApi.getCustomerServiceUrl())
    if (!url) {
      return false
    }
    return openCustomerService(url)
  }
  finally {
    fetching = false
  }
}
