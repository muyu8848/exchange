import { MessagePlugin } from 'tdesign-vue-next';

import { useUserStore, usePermissionStore, getAdminTodoStore } from '@/store';
import { resetUnauthorizedHandled } from '@/utils/request';
import router from '@/router';

const allowList = ['/login'];
const loginPath = '/login';

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore();

  // 未登录 -> 仅放白名单，其余跳转登录
  if (!userStore.tokenName) {
    // 进入登录页前复位“未登录一次性处理锁”，保证下次能正常触发
    resetUnauthorizedHandled();
    if (allowList.includes(to.path)) {
      next();
    } else {
      next({ path: loginPath });
    }
    return;
  }

  // 已登录 -> 动态路由未构建则构建一次
  const permissionStore = usePermissionStore();
  if (!permissionStore.asyncRoutes.length) {
    try {
      getAdminTodoStore().init().catch(() => {});
      const asyncRoutes = await permissionStore.buildAsyncRoutes();
      asyncRoutes.forEach((item) => router.addRoute(item));
      next({ ...to, replace: true });
      return;
    } catch (error) {
      // token 失效（后端返回 999 等）时构建路由会抛错，清登录态并跳回登录页
      MessagePlugin.error((error as Error)?.message || '登录已失效，请重新登录');
      getAdminTodoStore().stopPolling();
      userStore.logout();
      permissionStore.restoreRoutes();
      next({ path: loginPath });
      return;
    }
  }

  const redirect = to.redirectedFrom?.name === '404Page' ? to.path : undefined;
  if (redirect) {
    next({ path: redirect, replace: true });
  } else {
    next();
  }
});

router.afterEach((to) => {
  const userStore = useUserStore();
  if (to.name !== 'login') {
    return;
  }
  // 进入登录页即停止待办轮询：不论 token 是主动清空（退出/改密）还是被 999 清空，
  // 都不能让 setInterval 残留在登录页继续请求 todo/findTodoList
  getAdminTodoStore().stopPolling();
  if (userStore.tokenName) {
    userStore.logout();
  }
  usePermissionStore().restoreRoutes();
});
