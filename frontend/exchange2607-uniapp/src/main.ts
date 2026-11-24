import { createSSRApp } from 'vue'
import { applyNavigationBar, useAppTheme } from '@/composables/useAppTheme'
import App from './App.vue'
import router from './router'
import 'uno.css'
import '@/styles/dialog-theme.scss'
import i18n from '@/locale'

const pinia = createPinia()
pinia.use(persistPlugin)

const { theme } = useAppTheme()

export function createApp() {
  const app = createSSRApp(App)
  app.mixin({
    onShow() {
      applyNavigationBar(theme.value)
    },
  })
  app.use(router)
  app.use(pinia)
  app.use(i18n)
  return {
    app,
  }
}
