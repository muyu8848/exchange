<script setup lang="ts">
import { initAppTheme } from '@/composables/useAppTheme'
import { syncMemberNotificationBadge } from '@/composables/useMemberNotification'
import { checkAppUpdate } from '@/utils/appUpdate'
import { loadC2cOptionFiats } from '@/utils/c2cOption'
import { loadAllCurrencies } from '@/utils/currency'
import { handleDeepLinkContent } from '@/utils/deepLink'
import { setupTradeTipNavInterceptors, syncTradeTipPolling } from '@/utils/tradeTip'

onLaunch(() => {
  initAppTheme()
  loadAllCurrencies()
  loadC2cOptionFiats()
  setupTradeTipNavInterceptors()
  syncTradeTipPolling()
})

onShow(() => {
  syncTradeTipPolling()
  syncMemberNotificationBadge()

  // #ifdef APP-PLUS
  const args = plus.runtime.arguments
  if (args && args !== '{"startFromLauncher":"shortcut"}') {
    setTimeout(() => {
      handleDeepLinkContent(args)
      plus.runtime.arguments = ''
    }, 1400)
  }
  checkAppUpdate(false)
  // #endif
})

onHide(() => {
  // #ifdef APP-PLUS
  plus.runtime.arguments = ''
  // #endif
})
</script>

<style lang="scss">
@use '@wot-ui/ui/styles/theme/index.scss' as *;
@use '@/styles/theme-vars.scss' as *;

.page-wraper {
  min-height: calc(100vh - var(--window-top));
  box-sizing: border-box;
  background: var(--page-bg);
}
</style>
