import cloneDeep from 'lodash/cloneDeep';

import type { RouteItem } from '@/api/model/permissionModel';
import {
  BLANK_LAYOUT,
  EXCEPTION_COMPONENT,
  IFRAME,
  LAYOUT,
  PAGE_NOT_FOUND_ROUTE,
} from '@/utils/route/constant';

const LayoutMap = new Map<string, () => Promise<typeof import('*.vue')>>();

LayoutMap.set('LAYOUT', LAYOUT);
LayoutMap.set('BLANK', BLANK_LAYOUT);
LayoutMap.set('IFRAME', IFRAME);

let dynamicViewsModules: Record<string, () => Promise<Recordable>>;

type Recordable = Record<string, any>;

function asyncImportRoute(routes: RouteItem[] | undefined) {
  dynamicViewsModules = dynamicViewsModules || import.meta.glob('../../pages/**/*.vue');
  if (!routes) return;

  routes.forEach(async (item) => {
    const { component, name } = item;
    const { children } = item;

    if (component) {
      const layoutFound = LayoutMap.get(component.toUpperCase());
      if (layoutFound) {
        item.component = layoutFound;
      } else {
        item.component = dynamicImport(dynamicViewsModules, component);
      }
    } else if (name) {
      // item.component = PARENT_LAYOUT();
    }

    children && asyncImportRoute(children);
  });
}

function dynamicImport(
  dynamicViewsModules: Record<string, () => Promise<Recordable>>,
  component: string,
) {
  const keys = Object.keys(dynamicViewsModules);
  const matchKeys = keys.filter((key) => {
    const k = key.replace('../../pages', '');
    const startFlag = component.startsWith('/');
    const endFlag = component.endsWith('.vue') || component.endsWith('.tsx');
    const startIndex = startFlag ? 0 : 1;
    const lastIndex = endFlag ? k.length : k.lastIndexOf('.');
    return k.substring(startIndex, lastIndex) === component;
  });
  if (matchKeys?.length === 1) {
    const matchKey = matchKeys[0];
    return dynamicViewsModules[matchKey];
  }
  if (matchKeys?.length > 1) {
    throw new Error(
      'Please do not create `.vue` and `.TSX` files with the same file name in the same hierarchical directory under the views folder. This will cause dynamic introduction failure',
    );
  }
  // 精确组件不存在时，回退到所属模块首页 pages/{module}/index.vue
  // （部分菜单子项 url 带子路径，但对应子页面未单独创建，统一落到模块首页避免 404）
  const segs = (component || '').split('/').filter(Boolean);
  if (segs.length > 1) {
    const modulePath = `/${segs[0]}/index`;
    const fbKeys = keys.filter((key) => {
      const k = key.replace('../../pages', '');
      const kPath = k.substring(0, k.lastIndexOf('.'));
      return kPath === modulePath;
    });
    if (fbKeys.length >= 1) {
      return dynamicViewsModules[fbKeys[0]];
    }
  }
  console.warn(`Can't find ${component} in pages folder`);
  return EXCEPTION_COMPONENT;
}

export function transformObjectToRoute<T = RouteItem>(routeList: RouteItem[]): T[] {
  routeList.forEach(async (route) => {
    const component = route.component as string;

    if (component) {
      if (component.toUpperCase() === 'LAYOUT') {
        route.component = LayoutMap.get(component.toUpperCase());
      } else {
        route.children = [cloneDeep(route)];
        route.component = LAYOUT;
        route.name = `${route.name}Parent`;
        route.path = '';
        route.meta = route.meta || {};
      }
    } else {
      throw new Error('component is undefined');
    }

    route.children && asyncImportRoute(route.children);
  });

  return [PAGE_NOT_FOUND_ROUTE, ...routeList] as unknown as T[];
}
