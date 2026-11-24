import { useI18n } from 'vue-i18n'
import { useMemberNotification } from '@/composables/useMemberNotification'
import { useAppStore } from '@/store/app'
import { useAuthStore } from '@/store/auth'
import { useMemberStore } from '@/store/member'
import { openCustomerServiceAsync } from '@/utils/customerService'

export function useHomePage() {
  const router = useRouter()
  const appStore = useAppStore()
  const authStore = useAuthStore()
  const memberStore = useMemberStore()
  const { unreadCount, refreshUnreadCount } = useMemberNotification()
  const toast = useGlobalToast()
  const message = useMessage()
  const { t } = useI18n()

  const { memberInfo } = storeToRefs(memberStore)
  const todayExchangeRate = ref<number | null>(null)

  const authBadgeClass = computed(() => {
    const state = memberInfo.value?.authenticationStatus
    if (state === '3') {
      return 'home-auth-badge--verified'
    }
    if (state === '2') {
      return 'home-auth-badge--pending'
    }
    return 'home-auth-badge--unverified'
  })

  onShow(() => {
    memberStore.ensureMemberInfo()
    loadTodayExchangeRate()
    refreshUnreadCount()
  })

  async function loadTodayExchangeRate() {
    // 充值通道已不再提供浮动汇率，首页「今日汇率」不再展示
    todayExchangeRate.value = null
  }

  function checkAuthAndGo(name: string) {
    // 游客：明确跳登录并带上回跳目标（未登录时 authenticationStatus 为空，不能只靠实名校验）
    if (!authStore.isLoggedIn) {
      router.push({ name: 'login', query: { redirect: encodeURIComponent(name) } })
      return
    }
    const state = memberInfo.value?.authenticationStatus
    if (state === '1') {
      message.confirm({
        title: t('common.tip'),
        msg: t('home.verifyRequired'),
        confirmButtonText: t('home.goVerify'),
      }).then((ok) => {
        if (ok)
          router.push({ name: 'realNameAuth' })
      })
      return
    }
    if (state === '2') {
      toast.info({ msg: t('home.verifying') })
      return
    }
    router.push({ name })
  }

  function goQuickBuy() {
    checkAuthAndGo('tradeHall')
  }

  function goPublishPreOrder() {
    checkAuthAndGo('preOrderPublish')
  }

  function goNotice() {
    router.push({ name: 'noticeList' })
  }

  function goHelp() {
    router.push({ name: 'usingHelpList' })
  }

  function goMessage() {
    if (!authStore.isLoggedIn) {
      router.push({ name: 'login' })
      return
    }
    router.push({ name: 'messageList' })
  }

  async function contactService() {
    const ok = await openCustomerServiceAsync()
    if (!ok) {
      toast.error({ msg: t('home.supportBusy') })
    }
  }

  function goRealNameFromTag() {
    router.push({ name: 'realNameAuth' })
  }

  return {
    router,
    appStore,
    memberInfo,
    authBadgeClass,
    todayExchangeRate,
    unreadCount,
    checkAuthAndGo,
    goQuickBuy,
    goPublishPreOrder,
    goNotice,
    goHelp,
    goMessage,
    contactService,
    goRealNameFromTag,
  }
}
