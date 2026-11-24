/// <reference types="vite/client" />

declare module '@/components/KLineChart/index.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{
    period?: string
    symbol?: string
    priceScale?: number
    apiBaseUrl?: string
    tokenName?: string
    tokenValue?: string
    klineApiPath?: string
    bizType?: string
  }>
  export default component
}
