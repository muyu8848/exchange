import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { AddFundOption } from '@/components/AddFundsSheet.vue'

/**
 * 首页 / 资产页共用的「添加资金」快捷入口选项。
 * 文案走 i18n，语言切换时 computed 自动重建。
 */
export function useAddFundsOptions() {
  const { t } = useI18n()

  return computed<AddFundOption[]>(() => [
    {
      key: 'c2c',
      title: t('addFunds.c2cBuy'),
      desc: t('addFunds.c2cBuyDesc'),
      icon: 'store',
      route: { name: 'tradeHall' },
    },
    {
      key: 'deposit',
      title: t('addFunds.deposit'),
      desc: t('addFunds.depositDesc'),
      icon: 'download',
      route: { name: 'deposit' },
    },
  ])
}
