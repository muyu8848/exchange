import type { LocationQuery } from 'vue-router';

import { watch } from 'vue';
import { useRoute } from 'vue-router';

/**
 * 顶栏待办跳转同步。
 * <p>
 * 页面被 keep-alive 缓存且 router-view 未设 key，同一路由仅 query 变化时组件不会重建，
 * 因此无法靠 setup 里的初始值感知后续跳转，需要监听 fullPath。
 * <p>
 * 仅当「当前路由确实是本页」且「URL 带 from=todo」时回调：
 * 前者避免页面失活期间误响应其他页面的导航，后者避免干扰正常菜单进入。
 *
 * @param pagePath 本页路由尾段（与菜单 path 一致），如 withdraw-order
 * @param onSync   同步回调，入参为最新 query
 */
export function useTodoQuerySync(pagePath: string, onSync: (query: LocationQuery) => void) {
  const route = useRoute();

  watch(
    () => route.fullPath,
    () => {
      if (!route.path.endsWith(`/${pagePath}`)) return;
      if (route.query.from !== 'todo') return;
      onSync(route.query);
    },
  );
}
