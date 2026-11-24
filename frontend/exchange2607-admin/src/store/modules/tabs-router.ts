import { defineStore } from 'pinia';
import type { TRouterInfo, TTabRouterType } from '@/types/interface';

const homeRoute: Array<TRouterInfo> = [];

const state = {
  tabRouterList: homeRoute,
  isRefreshing: false,
};

// 不需要做多标签tabs页缓存的列表 值为每个页面对应的name 如 DashboardDetail
// const ignoreCacheRoutes = ['DashboardDetail'];
const ignoreCacheRoutes = ['login'];

export const useTabsRouterStore = defineStore('tabsRouter', {
  state: () => state,
  getters: {
    tabRouters: (state: TTabRouterType) => state.tabRouterList,
    refreshing: (state: TTabRouterType) => state.isRefreshing,
  },
  actions: {
    // 处理刷新
    toggleTabRouterAlive(routeIdx: number) {
      this.isRefreshing = !this.isRefreshing;
      this.tabRouters[routeIdx].isAlive = !this.tabRouters[routeIdx].isAlive;
    },
    // 处理新增
    appendTabRouterList(newRoute: TRouterInfo) {
      const needAlive = !ignoreCacheRoutes.includes(newRoute.name as string);
      if (!this.tabRouters.find((route: TRouterInfo) => route.path === newRoute.path)) {
        // 登录后第一个打开的标签作为首页（不可关闭、始终显示）
        const isHome = this.tabRouterList.length === 0;
        // eslint-disable-next-line no-param-reassign
        this.tabRouterList = this.tabRouterList.concat({ ...newRoute, isAlive: needAlive, isHome });
      }
    },
    // 刷新后从持久化恢复时，确保只有一个 isHome 且在第一位
    sanitizeTabRouterList() {
      if (this.tabRouterList.length === 0) return;
      // 找到标记为 isHome 的路由，如果没有则取第一个
      const homeIdx = this.tabRouterList.findIndex((r) => r.isHome);
      if (homeIdx > 0) {
        // isHome 不在第一位，移到第一位
        const home = this.tabRouterList.splice(homeIdx, 1)[0];
        this.tabRouterList.unshift({ ...home, isHome: true });
      } else if (homeIdx === -1) {
        // 没有 isHome，标记第一个
        this.tabRouterList[0] = { ...this.tabRouterList[0], isHome: true };
      }
      // 确保只有第一个是 isHome
      this.tabRouterList.forEach((r, idx) => {
        if (idx > 0 && r.isHome) {
          // eslint-disable-next-line no-param-reassign
          r.isHome = false;
        }
      });
    },
    // 处理关闭当前
    subtractCurrentTabRouter(newRoute: TRouterInfo) {
      const { routeIdx } = newRoute;
      this.tabRouterList = this.tabRouterList.slice(0, routeIdx).concat(this.tabRouterList.slice(routeIdx + 1));
    },
    // 处理关闭右侧
    subtractTabRouterBehind(newRoute: TRouterInfo) {
      const { routeIdx } = newRoute;
      this.tabRouterList = this.tabRouterList.slice(0, routeIdx + 1);
    },
    // 处理关闭左侧（保留首页）
    subtractTabRouterAhead(newRoute: TRouterInfo) {
      const { routeIdx } = newRoute;
      const home = this.tabRouters.find((r) => r.isHome);
      const rest = this.tabRouterList
        .slice(routeIdx)
        .filter((r: TRouterInfo) => r.path !== (home as TRouterInfo)?.path);
      this.tabRouterList = (home ? [home] : []).concat(rest);
    },
    // 处理关闭其他（保留首页与当前标签）
    subtractTabRouterOther(newRoute: TRouterInfo) {
      const { routeIdx } = newRoute;
      const home = this.tabRouters.find((r) => r.isHome);
      const current = this.tabRouterList?.[routeIdx];
      if (!home || routeIdx === 0) {
        this.tabRouterList = home ? [home] : current ? [current] : [];
      } else {
        this.tabRouterList = [home].concat(current ? [current] : []);
      }
    },
    removeTabRouterList() {
      this.tabRouterList = [];
    },
    initTabRouterList(newRoutes: TRouterInfo[]) {
      newRoutes?.forEach((route: TRouterInfo) => this.appendTabRouterList(route));
    },
  },
  persist: true,
});
