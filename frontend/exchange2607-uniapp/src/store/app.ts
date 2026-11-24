import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    currencyCode: '',
    currencyFullName: '',
    appVersion: 1.4,
    /** 资产页总资产的折算展示货币（法币代码，如 CNY/USD），由「折算货币」页设置 */
    displayFiatCode: 'CNY',
  }),
  getters: {
    /**
     * 实际生效的折算货币。
     * persist 插件会用 storage 中的旧 state 整体覆盖，老版本没有 displayFiatCode 字段，
     * 因此读取一律走本 getter 兜底，避免出现 undefined。
     */
    effectiveDisplayFiat(state): string {
      return state.displayFiatCode || 'CNY'
    },
  },
})
