import { useAuthStore } from '@/store/auth'

/**
 * 需要登录的操作前置校验（游客模式）。
 * <p>
 * 已登录返回 true；未登录则跳登录页并带上当前页面作为回跳目标，返回 false。
 * 用法：
 * ```ts
 * const requireLogin = useRequireLogin()
 * if (!requireLogin()) return
 * ```
 */
export function useRequireLogin() {
  const authStore = useAuthStore()
  const router = useRouter()
  const route = useRoute()

  return (): boolean => {
    if (authStore.isLoggedIn) {
      return true
    }
    const name = route.name ? String(route.name) : ''
    router.push({
      name: 'login',
      query: name ? { redirect: encodeURIComponent(name) } : undefined,
    })
    return false
  }
}
