import type { LocationQuery, RouteLocationRaw, RouteParams } from '@wot-ui/router'
import { pages, subPackages } from 'virtual:uni-pages'
import { isAuthRoute } from '@/api/core/handlers'
import { useAuthStore } from '@/store/auth'

function generateRoutes() {
  const routes = pages.map((page) => {
    const newPath = `/${page.path}`
    return { ...page, path: newPath }
  })
  if (subPackages && subPackages.length > 0) {
    subPackages.forEach((subPackage) => {
      const subRoutes = subPackage.pages.map((page: { path: string }) => {
        const newPath = `/${subPackage.root}/${page.path}`
        return { ...page, path: newPath }
      })
      routes.push(...subRoutes)
    })
  }
  return routes
}

function toRouteParams(query: LocationQuery): RouteParams {
  return Object.fromEntries(
    Object.entries(query).map(([key, value]) => {
      if (Array.isArray(value)) {
        return [key, value.map(item => (item == null ? '' : String(item)))]
      }
      return [key, value == null ? '' : String(value)]
    }),
  )
}

/**
 * @wot-ui/router 按 name 跳转时会把 params 拼进 URL，但不会处理 query。
 * 业务侧统一使用 query 传参，这里在导航前合并到 params。
 */
export function normalizeRouteLocation(to: RouteLocationRaw): RouteLocationRaw {
  if (typeof to === 'string' || !to.query || Object.keys(to.query).length === 0) {
    return to
  }
  if (!to.name) {
    return to
  }
  const { query, params, ...rest } = to
  return {
    ...rest,
    params: {
      ...(params || {}),
      ...toRouteParams(query),
    },
  }
}

const router = createRouter({
  routes: generateRoutes(),
})

function wrapNavigate<T extends (to: RouteLocationRaw) => Promise<unknown>>(fn: T) {
  return ((to: RouteLocationRaw) => fn(normalizeRouteLocation(to))) as T
}

// 必须直接改 createRouter 返回的实例：install 注入的是内部 router，
// 展开运算符包一层的话 useRouter() 仍拿到未包装的 push，query 不会进 URL。
router.push = wrapNavigate(router.push.bind(router))
router.replace = wrapNavigate(router.replace.bind(router))
router.replaceAll = wrapNavigate(router.replaceAll.bind(router))
router.pushTab = wrapNavigate(router.pushTab.bind(router))

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  authStore.loadFromStorage()

  const navigateNext: typeof next = (location) => {
    if (location && typeof location === 'object') {
      next(normalizeRouteLocation(location))
      return
    }
    next(location)
  }

  if (!isAuthRoute(to.name as string) && !authStore.isLoggedIn) {
    // 带上原目标，登录成功后由 login.vue 回跳；游客访问首页（白名单）不会走到这里
    const redirect = to.name ? encodeURIComponent(String(to.name)) : undefined
    navigateNext({ name: 'login', query: redirect ? { redirect } : undefined, navType: 'replaceAll' })
    return
  }

  if (authStore.isLoggedIn && (to.name === 'login' || to.name === 'register')) {
    navigateNext({ name: 'home', navType: 'replaceAll' })
    return
  }

  navigateNext()
})

export default router
