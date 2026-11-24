import { RouteRecordName, LocationQueryRaw } from 'vue-router';
import STYLE_CONFIG from '@/config/style';

export interface MenuRoute {
  path: string;
  title?: string;
  name?: string;
  icon?:
    | string
    | {
        render: () => void;
      };
  redirect?: string;
  children: MenuRoute[];
  meta: any;
}

export type ModeType = 'dark' | 'light';

export type SettingType = typeof STYLE_CONFIG;

export type ClassName = { [className: string]: any } | ClassName[] | string;

export type CommonObjType = {
  [key: string]: string | number;
};

export interface TRouterInfo {
  path: string;
  query?: LocationQueryRaw;
  routeIdx?: number;
  title?: string;
  name?: RouteRecordName;
  isAlive?: boolean;
  isHome?: boolean;
  meta?: any;
}

export interface TTabRouterType {
  isRefreshing: boolean;
  tabRouterList: Array<TRouterInfo>;
}

// ===== 迁移 admin 契约所需 =====
// 登录用户信息（后端返回）
export interface UserInfo {
  name: string;
  accountLevel?: number;
  invitationCode?: string;
  roles?: string[];
}

// 路由元信息（后端菜单驱动），对 vue-router 的 RouteMeta 做模块扩充
declare module 'vue-router' {
  interface RouteMeta {
    title?: string;
    icon?: string;
    expanded?: boolean;
    sortNum?: number;
    hidden?: boolean;
    hiddenBreadcrumb?: boolean;
    single?: boolean;
    keepAlive?: boolean;
    frameSrc?: string;
    frameBlank?: boolean;
  }
}
