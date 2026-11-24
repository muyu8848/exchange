import type { MemberNotification } from '@/api/types'
import { memberNotificationApi } from '@/api/modules'
import { apiData } from '@/api/http'
import i18n from '@/locale'
import router from '@/router'
import { useAuthStore } from '@/store/auth'

let tradeTipTimer: ReturnType<typeof setInterval> | null = null

function isOnOrderDetailPage() {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1] as { route?: string } | undefined
  return !!page?.route?.includes('orderDetail')
}

function playTipSound() {
  const ctx = uni.createInnerAudioContext()
  ctx.autoplay = true
  ctx.src = '/static/ring.wav'
}

function showTradeTipModal(popup: MemberNotification) {
  playTipSound()
  uni.showModal({
    title: i18n.global.t('tradeTip.title'),
    content: popup.content || popup.title || '',
    confirmText: i18n.global.t('tradeTip.goNow'),
    cancelText: i18n.global.t('tradeTip.cancel'),
    success(res) {
      if (res.confirm && popup.bizId) {
        router.push({ name: 'orderDetail', query: { orderId: popup.bizId } })
      }
    },
    complete() {
      apiData(memberNotificationApi.markRead(popup.id))
    },
  })
}

async function pollTradeTip() {
  if (isOnOrderDetailPage()) {
    return
  }
  try {
    const popup = await apiData(memberNotificationApi.findPendingPopup())
    if (popup) {
      showTradeTipModal(popup)
    }
  }
  catch {
    // ignore polling errors
  }
}

export function stopTradeTipPolling() {
  if (tradeTipTimer) {
    clearInterval(tradeTipTimer)
    tradeTipTimer = null
  }
}

export function startTradeTipPolling() {
  stopTradeTipPolling()
  pollTradeTip()
  tradeTipTimer = setInterval(pollTradeTip, 7000)
}

export function syncTradeTipPolling() {
  const authStore = useAuthStore()
  authStore.loadFromStorage()
  if (authStore.isLoggedIn) {
    startTradeTipPolling()
  }
  else {
    stopTradeTipPolling()
  }
}

const NAV_INTERCEPTORS = ['navigateTo', 'redirectTo', 'reLaunch', 'switchTab', 'navigateBack'] as const

export function setupTradeTipNavInterceptors() {
  for (const method of NAV_INTERCEPTORS) {
    uni.addInterceptor(method, {
      success() {
        syncTradeTipPolling()
      },
    })
  }
}
