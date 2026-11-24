export interface TabbarItem {
  name: string
  value?: number
  active: boolean
  title: string
  icon: string
}

const tabbarItems = ref<TabbarItem[]>([
  { name: 'home', active: true, title: 'tabbar.home', icon: 'home' },
  { name: 'markets', active: false, title: 'tabbar.markets', icon: 'arrow-rise' },
  { name: 'spotTrade', active: false, title: 'tabbar.spot', icon: 'sort-fill-1' },
  { name: 'perpTrade', active: false, title: 'tabbar.perp', icon: 'lightning-circle-fill' },
  { name: 'assets', active: false, title: 'tabbar.assets', icon: 'safe' },
])

export const TABBAR_ICON_INACTIVE = '#9A9690'
export const TABBAR_ICON_ACTIVE = '#C9B48A'

export function useTabbar() {
  const tabbarList = computed(() => tabbarItems.value)

  const activeTabbar = computed(() => {
    const item = tabbarItems.value.find(item => item.active)
    return item || tabbarItems.value[0]
  })

  const getTabbarItemValue = (name: string) => {
    const item = tabbarItems.value.find(item => item.name === name)
    return item?.value
  }

  const setTabbarItem = (name: string, value: number) => {
    const tabbarItem = tabbarItems.value.find(item => item.name === name)
    if (tabbarItem) {
      tabbarItem.value = value
    }
  }

  const setTabbarItemActive = (name: string) => {
    tabbarItems.value.forEach((item) => {
      item.active = item.name === name
    })
  }

  return {
    tabbarList,
    activeTabbar,
    getTabbarItemValue,
    setTabbarItem,
    setTabbarItemActive,
  }
}
