<template>
  <div :class="layoutCls">
    <t-head-menu :class="menuCls" :theme="theme as 'light' | 'dark'" expand-type="popup" :value="active">
      <template #logo>
        <span v-if="showLogo" class="header-logo-container" @click="goHome">
          <logo-full class="t-logo" />
        </span>
        <div v-else class="header-operate-left">
          <t-button theme="default" shape="square" variant="text" @click="changeCollapsed">
            <t-icon class="collapsed-icon" name="view-list" />
          </t-button>
        </div>
      </template>
      <template v-if="layout !== 'side'" #default>
        <menu-content class="header-menu" :nav-data="menu" />
      </template>
      <template #operations>
        <div class="operations-container">
          <!-- 待办条（常驻平铺） -->
          <todo-bar />

          <t-dropdown :min-column-width="135" trigger="click">
            <template #dropdown>
              <t-dropdown-menu>
                <t-dropdown-item class="operations-dropdown-container-item" @click="showGoogleAuthDialog">
                  <t-icon name="secured"></t-icon>谷歌验证器
                </t-dropdown-item>
                <t-dropdown-item class="operations-dropdown-container-item" @click="showUpdateLoginPwdDialog">
                  <t-icon name="edit"></t-icon>修改登录密码
                </t-dropdown-item>
                <t-dropdown-item
            v-if="hasPermission('setting.refreshCache')"
            class="operations-dropdown-container-item"
            @click="doRefreshCache">
                  <t-icon name="refresh"></t-icon>刷新缓存
                </t-dropdown-item>
                <t-dropdown-item class="operations-dropdown-container-item" @click="handleLogout">
                  <t-icon name="poweroff"></t-icon>退出登录
                </t-dropdown-item>
              </t-dropdown-menu>
            </template>
            <t-button class="header-user-btn" theme="default" variant="text">
              <template #icon>
                <t-icon class="header-user-avatar" name="user-circle" />
              </template>
              <div class="header-user-account">{{ userInfo.name || '未登录' }}</div>
              <template #suffix><t-icon name="chevron-down" /></template>
            </t-button>
          </t-dropdown>
          <t-tooltip placement="bottom" content="系统设置">
            <t-button theme="default" shape="square" variant="text" @click="toggleSettingPanel">
              <t-icon name="setting" />
            </t-button>
          </t-tooltip>
        </div>
      </template>
    </t-head-menu>
  </div>
  <t-dialog
    header="修改登录密码"
    :close-on-overlay-click="false"
    :visible="showUpdateLoginPwdDialogFlag"
    @close="showUpdateLoginPwdDialogFlag = false"
    @confirm="updateLoginPwd"
  >
    <template #body>
      <t-form ref="updateLoginPwdForm" :data="updateLoginPwdFormData" :rules="updateLoginPwdFormRules">
        <t-form-item label="旧密码" name="oldPwd">
          <t-input v-model="updateLoginPwdFormData.oldPwd"></t-input>
        </t-form-item>
        <t-form-item label="新密码" name="newPwd">
          <t-input v-model="updateLoginPwdFormData.newPwd"></t-input>
        </t-form-item>
      </t-form>
    </template>
  </t-dialog>

  <google-auth-bind-dialog v-model:visible="googleDialogVisible" />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import type { PropType } from 'vue';
import { useRouter } from 'vue-router';
import { FormInstanceFunctions, FormRule, MessagePlugin } from 'tdesign-vue-next';
import { useSettingStore, useUserStore, usePermissionStore, getAdminTodoStore } from '@/store';
import { hasPermission } from '@/utils/permission';
import { getActive } from '@/router';
import { prefix } from '@/config/global';
import LogoFull from '@/assets/assets-logo-full.svg?component';
import type { MenuRoute } from '@/types/interface';
import { request } from '@/utils/request';

import GoogleAuthBindDialog from '@/components/google-auth-bind-dialog/index.vue';
import TodoBar from './TodoBar.vue';
import MenuContent from './MenuContent.vue';

const props = defineProps({
  theme: {
    type: String,
    default: '',
  },
  layout: {
    type: String,
    default: 'top',
  },
  showLogo: {
    type: Boolean,
    default: true,
  },
  menu: {
    type: Array as PropType<MenuRoute[]>,
    default: () => [],
  },
  isFixed: {
    type: Boolean,
    default: false,
  },
  isCompact: {
    type: Boolean,
    default: false,
  },
  maxLevel: {
    type: Number,
    default: 3,
  },
});

const router = useRouter();
const settingStore = useSettingStore();
const userStore = useUserStore();

const userInfo = computed(() => userStore.userInfo);

const showUpdateLoginPwdDialogFlag = ref(false);
const googleDialogVisible = ref(false);
const updateLoginPwdForm = ref<FormInstanceFunctions>();
const updateLoginPwdFormData = ref({ oldPwd: '', newPwd: '' });
const updateLoginPwdFormRules: Record<string, FormRule[]> = {
  oldPwd: [{ required: true, message: '请输入旧密码', type: 'error', trigger: 'submit' }],
  newPwd: [{ required: true, message: '请输入新密码', type: 'error', trigger: 'submit' }],
};

const showUpdateLoginPwdDialog = () => {
  updateLoginPwdForm.value?.reset();
  showUpdateLoginPwdDialogFlag.value = true;
};

const updateLoginPwd = async () => {
  updateLoginPwdForm.value?.submit();
  const error = await updateLoginPwdForm.value?.validateOnly();
  if (error !== true) {
    return;
  }
  await request.post({
    url: '/backgroundAccount/updateCurrentAccountLoginPwd',
    data: updateLoginPwdFormData.value,
  });
  showUpdateLoginPwdDialogFlag.value = false;
  MessagePlugin.success('密码已修改，请重新登录');
  // 后端已强制下线该账号所有会话，这里同步停止待办轮询、清理本地登录态并跳登录页
  getAdminTodoStore().stopPolling();
  userStore.tokenName = '';
  userStore.tokenValue = '';
  usePermissionStore().restoreRoutes();
  router.push({ path: '/login' });
};

const doRefreshCache = async () => {
  await request.post({
    url: '/setting/refreshCache',
  });
  MessagePlugin.success('操作成功');
};

const showGoogleAuthDialog = () => {
  googleDialogVisible.value = true;
};

const toggleSettingPanel = () => {
  settingStore.updateConfig({
    showSettingPanel: true,
  });
};

const active = computed(() => getActive());

const layoutCls = computed(() => [`${prefix}-header-layout`]);

const menuCls = computed(() => {
  const { isFixed, layout, isCompact } = props;
  return [
    {
      [`${prefix}-header-menu`]: !isFixed,
      [`${prefix}-header-menu-fixed`]: isFixed,
      [`${prefix}-header-menu-fixed-side`]: layout === 'side' && isFixed,
      [`${prefix}-header-menu-fixed-side-compact`]: layout === 'side' && isFixed && isCompact,
    },
  ];
});

const changeCollapsed = () => {
  settingStore.updateConfig({
    isSidebarCompact: !settingStore.isSidebarCompact,
  });
};

const handleNav = (url) => {
  router.push(url);
};

// 跳转到当前账号首个有权限菜单，避免无仪表盘权限的账号落到 /dashboard/base
const goHome = () => {
  const permissionStore = usePermissionStore();
  const pageRoute = permissionStore.asyncRoutes.find((item) => item.path === '/page');
  const firstPath = (pageRoute?.redirect as string) || permissionStore.asyncRoutes[0]?.redirect;
  router.push((firstPath as string) || '/');
};

const handleLogout = () => {
  router.push({
    path: '/login',
    query: { redirect: encodeURIComponent(router.currentRoute.value.fullPath) },
  });
};


</script>
<style lang="less" scoped>
.@{starter-prefix}-header {
  &-menu-fixed {
    position: fixed;
    top: 0;
    z-index: 1001;

    &-side {
      left: 232px;
      right: 0;
      z-index: 10;
      width: auto;
      transition: all 0.3s;
      &-compact {
        left: 64px;
      }
    }
  }

  &-logo-container {
    cursor: pointer;
    display: inline-flex;
  }
}
.header-menu {
  flex: 1 1 1;
  display: inline-flex;

  :deep(.t-menu__item) {
    min-width: unset;
    padding: 0px 16px;
  }
}

.operations-container {
  display: flex;
  align-items: center;
  margin-right: 12px;

  .t-popup__reference {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .t-button {
    margin: 0 8px;
    &.header-user-btn {
      margin: 0;
    }
  }

  .t-icon {
    font-size: 20px;
    &.general {
      margin-right: 16px;
    }
  }
}

.header-operate-left {
  display: flex;
  margin-left: 20px;
  align-items: normal;
  line-height: 0;

  .collapsed-icon {
    font-size: 20px;
  }
}

.header-logo-container {
  width: 184px;
  height: 26px;
  display: flex;
  margin-left: 24px;
  color: var(--td-text-color-primary);

  .t-logo {
    width: 100%;
    height: 100%;
    &:hover {
      cursor: pointer;
    }
  }

  &:hover {
    cursor: pointer;
  }
}

.header-user-account {
  display: inline-flex;
  align-items: center;
  color: var(--td-text-color-primary);
  .t-icon {
    margin-left: 4px;
    font-size: 16px;
  }
}

:deep(.t-head-menu__inner) {
  border-bottom: 1px solid var(--td-border-level-1-color);
}

.t-menu--light {
  .header-user-account {
    color: var(--td-text-color-primary);
  }
}
.t-menu--dark {
  .t-head-menu__inner {
    border-bottom: 1px solid var(--td-gray-color-10);
  }
  .header-user-account {
    color: rgba(255, 255, 255, 0.55);
  }
  .t-button {
    --ripple-color: var(--td-gray-color-10) !important;
    &:hover {
      background: var(--td-gray-color-12) !important;
    }
  }
}

.operations-dropdown-container-item {
  width: 100%;
  display: flex;
  align-items: center;

  .t-icon {
    margin-right: 8px;
  }

  :deep(.t-dropdown__item) {
    .t-dropdown__item__content {
      display: flex;
      justify-content: center;
    }
    .t-dropdown__item__content__text {
      display: flex;
      align-items: center;
      font-size: 14px;
    }
  }

  :deep(.t-dropdown__item) {
    width: 100%;
    margin-bottom: 0px;
  }
  &:last-child {
    :deep(.t-dropdown__item) {
      margin-bottom: 8px;
    }
  }
}
</style>
