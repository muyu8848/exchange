import { memberNotificationApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { useAuthStore } from '@/store/auth'
import { useTabbar } from '@/composables/useTabbar'

const unreadCount = ref(0)

export function useMemberNotification() {
  const authStore = useAuthStore()
  const { tabbarList } = useTabbar()

  function updateTabbarBadge(count: number) {
    const item = tabbarList.value.find(tab => tab.name === 'my')
    if (!item) {
      return
    }
    if (count > 0) {
      item.value = count
    }
    else {
      delete item.value
    }
  }

  async function refreshUnreadCount() {
    authStore.loadFromStorage()
    if (!authStore.isLoggedIn) {
      unreadCount.value = 0
      updateTabbarBadge(0)
      return 0
    }
    try {
      const count = Number(await apiData(memberNotificationApi.countUnread())) || 0
      unreadCount.value = count
      updateTabbarBadge(count)
      return count
    }
    catch {
      return unreadCount.value
    }
  }

  return {
    unreadCount,
    refreshUnreadCount,
  }
}

export function syncMemberNotificationBadge() {
  const { refreshUnreadCount } = useMemberNotification()
  return refreshUnreadCount()
}
