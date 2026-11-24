import { defineUniPages } from '@uni-helper/vite-plugin-uni-pages'

export default defineUniPages({
  pages: [],
  globalStyle: {
    navigationBarBackgroundColor: '#0a0a0c',
    navigationBarTextStyle: 'white',
    navigationBarTitleText: 'C2C',
    backgroundColor: '#030304',
    backgroundTextStyle: 'light',
    backgroundColorTop: '#030304',
    backgroundColorBottom: '#030304',
    enablePullDownRefresh: false,
    onReachBottomDistance: 50,
    animationType: 'pop-in',
    animationDuration: 300,
  },
  tabBar: {
    custom: true,
    // #ifdef MP-ALIPAY
    customize: true,
    overlay: true,
    // #endif
    height: '0',
    color: '#9A9690',
    selectedColor: '#C9B48A',
    backgroundColor: '#0a0a0c',
    borderStyle: 'white',
    list: [
      { pagePath: 'pages/index/index' },
      { pagePath: 'pages/markets/index' },
      { pagePath: 'subPages/spot/spotTrade' },
      { pagePath: 'subPages/perp/perpTrade' },
      { pagePath: 'pages/assets/index' },
    ],
  },
})
