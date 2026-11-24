import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import uniq from 'lodash/uniq';

import { EXCEPTION_COMPONENT } from '@/utils/route/constant';

export const homepageRouterList: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/page',
  },
];

export const defaultRouterList: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/pages/login/index.vue'),
  },
  {
    path: '/result/404',
    name: '404',
    component: () => import('@/pages/result/404/index.vue'),
  },
  {
    path: '/result/500',
    name: '500',
    component: EXCEPTION_COMPONENT,
  },
];

export const fixedRouterList: Array<RouteRecordRaw> = [...homepageRouterList, ...defaultRouterList];

// 初始展开的菜单（本项目动态路由无 expanded 标记，返回空即可）
export const getRoutesExpanded = () => {
  const expandedRoutes: Array<string> = [];

  fixedRouterList.forEach((item) => {
    if (item.meta && item.meta.expanded) {
      expandedRoutes.push(item.path);
    }
    if (item.children && item.children.length > 0) {
      item.children
        .filter((child) => child.meta && child.meta.expanded)
        .forEach((child: RouteRecordRaw) => {
          expandedRoutes.push(item.path);
          expandedRoutes.push(`${item.path}/${child.path}`);
        });
    }
  });
  return uniq(expandedRoutes);
};

// 当前激活菜单路径（供侧边栏高亮）
export const getActive = (maxLevel = 3): string => {
  const route = router.currentRoute.value;
  if (!route.path) return '';
  return route.path
    .split('/')
    .filter((_item: string, index: number) => index <= maxLevel && index > 0)
    .map((item: string) => `/${item}`)
    .join('');
};

const router = createRouter({
  history: createWebHistory(),
  routes: [...fixedRouterList],
  scrollBehavior() {
    return { el: '#app', top: 0 };
  },
});

export default router;
