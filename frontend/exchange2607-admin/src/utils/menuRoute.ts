import type { RouteItem } from '@/api/model/permissionModel';
import type { Router } from 'vue-router';

import { usePermissionStore } from '@/store';

// 从菜单 path 提取页面 slug（用于组件兜底映射）
function extractPageSlug(path?: string): string {
  if (!path) return '';
  const cleaned = path.replace(/^\/+/, '').replace(/\/index$/, '');
  const parts = cleaned.split('/');
  return parts[parts.length - 1] || cleaned;
}

function normalizePath(path?: string): string {
  return (path || '').replace(/^\/+/, '');
}

// 标准化后端菜单树（后端已返回 component = "/${url}/index"，此处仅处理父子关系）
export function normalizeMenuRoutes(routes: RouteItem[]): RouteItem[] {
  return (routes || []).map((route) => {
    const children =
      route.children && route.children.length ? normalizeMenuRoutes(route.children) : [];

    const slug = extractPageSlug(route.path);

    if (children.length > 0) {
      // 父菜单：不挂独立页面，仅作为分组，并重定向到首个子菜单
      // component 置空，由 asyncImportRoute 的 `else if (name) -> PARENT_LAYOUT()` 处理为穿透布局（避免双层 chrome）
      return {
        ...route,
        component: undefined,
        redirect: `/page/${normalizePath(route.path)}/${normalizePath(children[0].path)}`,
        children,
      };
    }

    // 叶子菜单：component 兜底为 /{slug}/index
    return {
      ...route,
      component: (route.component as string) || `/${slug}/index`,
    };
  });
}

// 取首个可访问菜单路径（用于 /page 根重定向）
export function resolveFirstMenuPath(routes: RouteItem[]): string {
  const first = routes?.[0];
  if (!first) return '/page';
  if (first.children && first.children.length) {
    return `/page/${normalizePath(first.path)}/${normalizePath(first.children[0].path)}`;
  }
  return `/page/${normalizePath(first.path)}`;
}

// 页面内跳转
export function gotoPage(router: Router, path: string, param?: Record<string, string>) {
  const usePermission = usePermissionStore();
  // 注意：asyncRoutes[0] 是 transformObjectToRoute 注入的 404 路由，
  // 真正的根路由为 path === '/page'，不能依赖下标 [0]
  const pageRoot = usePermission.asyncRoutes.find((r) => r.path === '/page');
  const menus = pageRoot?.children || [];
  for (const menu of menus) {
    const subMenus = menu.children || [];
    for (const subMenu of subMenus) {
      if (normalizePath(subMenu.path) === normalizePath(path)) {
        router.push({
          path: `/page/${normalizePath(menu.path)}/${normalizePath(subMenu.path)}`,
          query: param || {},
        });
        return;
      }
    }
  }
}
