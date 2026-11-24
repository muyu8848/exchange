import i18n from '@/locale'
import { handleDeepLinkContent } from '@/utils/deepLink'

export function handleTabScan() {
  // #ifdef H5
  uni.showToast({ title: i18n.global.t('withdraw.scanApp'), icon: 'none' })
  // #endif

  // #ifndef H5
  uni.scanCode({
    scanType: ['qrCode'],
    success(res) {
      handleDeepLinkContent(res.result)
    },
    fail() {
      uni.showToast({ title: i18n.global.t('withdraw.scanFail'), icon: 'none' })
    },
  })
  // #endif
}
