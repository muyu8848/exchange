import { defineStore } from 'pinia';
import { RouteRecordRaw } from 'vue-router';

import { store, useTabsRouterStore } from '@/store';
import router, { fixedRouterList } from '@/router';
import { getMenuList } from '@/api/permission';
import type { RouteItem } from '@/api/model/permissionModel';
import { transformObjectToRoute } from '@/utils/route';
import { normalizeMenuRoutes, resolveFirstMenuPath } from '@/utils/menuRoute';

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    whiteListRouters: ['/login'],
    routers: [] as RouteRecordRaw[],
    asyncRoutes: [] as RouteRecordRaw[],
    removeRoutes: [] as RouteRecordRaw[],
  }),
  actions: {
    // 构建后端驱动的动态路由：菜单由 /backgroundAccount/findMyMenuRoute 下发
    async buildAsyncRoutes() {
      const list = (await getMenuList()) as RouteItem[];

      // 1) 标准化后端菜单树（父菜单重定向、叶子菜单 component 兜底）
      const normalized = normalizeMenuRoutes(list);

      // 2) 统一挂到 /page 布局根下（参考 TDesign Starter 动态路由规范）
      const root: RouteItem = {
        path: '/page',
        name: 'page',
        component: 'LAYOUT',
        redirect: resolveFirstMenuPath(normalized),
        meta: { title: '运营后台', icon: 'dashboard' },
        children: normalized,
      };

      // 3) 官方 Starter 的 transformObjectToRoute：字符串 component -> import.meta.glob 懒加载
      this.asyncRoutes = transformObjectToRoute([root]);

      await this.initRoutes();
      return this.asyncRoutes;
    },
    async initRoutes() {
      this.routers = [...this.asyncRoutes, ...fixedRouterList];
    },
    async restoreRoutes() {
      // 移除上一个账号已注册到 Vue Router 的动态路由，避免菜单/路由残留
      this.asyncRoutes.forEach((item) => {
        if (item.name) router.removeRoute(item.name);
      });
      this.asyncRoutes = [];
      this.routers = [...fixedRouterList];
      // 清空多标签，避免上一个账号的标签（如仪表盘）残留到下一个账号
      useTabsRouterStore(store).removeTabRouterList();
    },
  },
});
