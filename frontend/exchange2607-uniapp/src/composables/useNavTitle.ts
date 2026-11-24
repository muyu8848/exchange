import type { MaybeRefOrGetter } from 'vue'

/**
 * 同步浏览器标签（H5 的 document.title）与原生导航栏标题。
 *
 * 只有「所属页面」处于页面栈顶时才写入：页面被缓存后其组件实例仍存活，
 * 若不加判断，语言切换时所有访问过的页面会同时写入、互相覆盖
 * （典型现象：在「我的」页切语言，标题却变成其他 tab 页的名字）。
 *
 * @param title 标题来源，支持 ref / computed / getter；返回空值时跳过写入
 * @returns sync 手动同步一次（如页面 onShow 时需要重设）
 */
export function useNavTitle(title: MaybeRefOrGetter<string>) {
  let ownerPage: ReturnType<typeof getCurrentPages>[number] | null = null

  const readTitle = typeof title === 'function' ? title : () => unref(title)

  /** 所属页面是否仍处于栈顶 */
  function isOwnerActive() {
    if (!ownerPage) {
      return true
    }
    const pages = getCurrentPages()
    if (pages.length === 0) {
      return true
    }
    const current = pages[pages.length - 1]
    // 优先比对实例引用；部分端上缓存后引用可能重建，故同时按路由路径兜底比对
    return current === ownerPage || (!!current.route && current.route === ownerPage.route)
  }

  function sync() {
    const value = readTitle()
    if (value) {
      uni.setNavigationBarTitle({ title: value })
    }
  }

  onMounted(() => {
    const pages = getCurrentPages()
    ownerPage = pages.length > 0 ? pages[pages.length - 1] : null
    sync()
  })

  // 标题变化时同步：语言切换、动态标题（订单号/公告标题等）都会走到这里
  watch(readTitle, () => {
    if (isOwnerActive()) {
      sync()
    }
  })

  return { sync }
}
