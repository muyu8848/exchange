import { defineStore } from 'pinia';

import { login, logout } from '@/api/login';
import { usePermissionStore } from '@/store';
import type { UserInfo } from '@/types/interface';
import { request } from '@/utils/request';

const InitUserInfo: UserInfo = {
  name: '', // 用户名，用于展示在页面右上角头像处
  accountLevel: 0,
  invitationCode: '',
};

export const useUserStore = defineStore('user', {
  state: () => ({
    tokenName: '',
    tokenValue: '',
    userInfo: { ...InitUserInfo },
    permissions: [],
  }),
  getters: {
    uploadHeaders(state): Record<string, string> {
      if (state.tokenName && state.tokenValue) {
        return { [state.tokenName]: state.tokenValue };
      }
      return {};
    },
  },
  actions: {
    async login(userInfo: Record<string, unknown>) {
      const res = await login(userInfo);
      this.tokenName = res.tokenName;
      this.tokenValue = res.tokenValue;
      this.permissions = res.permissions;
      // 登录新账号前，先清空上一个账号的动态路由与多标签缓存，避免跨账号残留
      // （不依赖"访问 /login 才清空"的时序，已登录态下直接切换账号也能正确重置）
      usePermissionStore().restoreRoutes();
      this.getUserInfo();
    },
    async getUserInfo() {
      const res = await request.post({
        url: '/backgroundAccount/getAccountInfo',
      });
      this.userInfo = {
        name: res.userName,
        accountLevel: res.accountLevel,
        invitationCode: res.invitationCode,
      };
    },
    async logout() {
      if (this.tokenName) {
        await logout();
      }
      this.tokenName = '';
      this.tokenValue = '';
      this.userInfo = { ...InitUserInfo };
    },
  },
  persist: {
    afterRestore: () => {
      const permissionStore = usePermissionStore();
      permissionStore.initRoutes();
    },
    key: 'user',
    paths: ['token', 'tokenName', 'tokenValue', 'userInfo', 'permissions'],
  },
});
