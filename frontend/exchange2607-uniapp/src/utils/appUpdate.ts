import { apiData } from '@/api/http'
import { settingApi } from '@/api/modules'
import i18n from '@/locale'
import { useAppStore } from '@/store/app'

export async function checkAppUpdate(manual = false) {
  // #ifdef APP-PLUS
  try {
    const info = await apiData(settingApi.getLatestAppInfo())
    const appStore = useAppStore()
    const latest = info.appVersion || 0
    if (appStore.appVersion < latest) {
      uni.showModal({
        title: i18n.global.t('appUpdate.title'),
        content: i18n.global.t('appUpdate.newVersion'),
        confirmText: i18n.global.t('appUpdate.download'),
        showCancel: false,
        success(res) {
          if (res.confirm && info.appDownloadUrl) {
            plus.runtime.openURL(info.appDownloadUrl)
          }
        },
      })
      return
    }
    if (manual) {
      uni.showToast({ title: i18n.global.t('appUpdate.latest'), icon: 'none' })
    }
  }
  catch {
    if (manual) {
      uni.showToast({ title: i18n.global.t('appUpdate.checkFailed'), icon: 'none' })
    }
  }
  // #endif

  // #ifndef APP-PLUS
  if (manual) {
    uni.showToast({ title: i18n.global.t('appUpdate.checkInApp'), icon: 'none' })
  }
  // #endif
}

export function formatAppVersion() {
  const appStore = useAppStore()
  return `Version ${appStore.appVersion}`
}
