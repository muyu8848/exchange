<template>
  <div :class="sideNavCls">
    <t-menu :class="menuCls" :theme="theme" :value="active" :collapsed="collapsed" :expanded="expanded" @expand="onExpanded">
      <template #logo>
        <span v-if="showLogo" :class="`${prefix}-side-nav-logo-wrapper`" @click="goHome">
          <component :is="getLogo()" :class="`${prefix}-side-nav-logo-${collapsed ? 't' : 'tdesign'}-logo`" />
        </span>
      </template>
      <menu-content :nav-data="menu" />
      <template #operations>
        <span class="version-container"> {{ !collapsed ? 'TDesign Starter' : '' }} {{ pgk.version }} </span>
      </template>
    </t-menu>
    <div :class="`${prefix}-side-nav-placeholder${collapsed ? '-hidden' : ''}`"></div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import type { PropType } from 'vue';
import { useRouter } from 'vue-router';
import union from 'lodash/union';

import { useSettingStore, usePermissionStore } from '@/store';
import { prefix } from '@/config/global';
import pgk from '../../../package.json';
import type { MenuRoute } from '@/types/interface';
import { getActive } from '@/router';

import AssetLogo from '@/assets/assets-t-logo.svg?component';
import AssetLogoFull from '@/assets/assets-logo-full.svg?component';
import MenuContent from './MenuContent.vue';

const MIN_POINT = 992 - 1;

const props = defineProps({
  menu: {
    type: Array as PropType<MenuRoute[]>,
    default: () => [],
  },
  showLogo: {
    type: Boolean as PropType<boolean>,
    default: true,
  },
  isFixed: {
    type: Boolean as PropType<boolean>,
    default: true,
  },
  layout: {
    type: String as PropType<string>,
    default: '',
  },
  headerHeight: {
    type: String as PropType<string>,
    default: '64px',
  },
  theme: {
    type: String as PropType<'light' | 'dark'>,
    default: 'light',
  },
  isCompact: {
    type: Boolean as PropType<boolean>,
    default: false,
  },
});

const collapsed = computed(() => useSettingStore().isSidebarCompact);

const active = computed(() => getActive());

// 受控展开：默认不展开全部，仅展开当前路由所在的父级分组（见 onMounted）。
// 不能用一次性 default-expanded，否则动态填充的子菜单不会展开（需手动折叠才显示）。
const expanded = ref<Array<string>>([]);

watch(
  () => active.value,
  () => {
    const path = getActive();
    const parentPath = path.substring(0, path.lastIndexOf('/'));
    expanded.value = union([parentPath], expanded.value);
  },
);

const onExpanded = (value: Array<string>) => {
  expanded.value = value;
};

const sideNavCls = computed(() => {
  const { isCompact } = props;
  return [
    `${prefix}-sidebar-layout`,
    {
      [`${prefix}-sidebar-compact`]: isCompact,
    },
  ];
});

const menuCls = computed(() => {
  const { showLogo, isFixed, layout } = props;
  return [
    `${prefix}-side-nav`,
    {
      [`${prefix}-side-nav-no-logo`]: !showLogo,
      [`${prefix}-side-nav-no-fixed`]: !isFixed,
      [`${prefix}-side-nav-mix-fixed`]: layout === 'mix' && isFixed,
    },
  ];
});

const router = useRouter();
const settingStore = useSettingStore();

const autoCollapsed = () => {
  const isCompact = window.innerWidth <= MIN_POINT;
  settingStore.updateConfig({
    isSidebarCompact: isCompact,
  });
};

onMounted(() => {
  const path = getActive();
  const parentPath = path.substring(0, path.lastIndexOf('/'));
  // 默认不展开全部：仅展开当前路由所在的父级分组，便于定位当前页面
  expanded.value = [parentPath];
  autoCollapsed();
  window.onresize = () => {
    autoCollapsed();
  };
});

// 跳转到当前账号首个有权限菜单，避免无仪表盘权限的账号落到 /dashboard/base
const goHome = () => {
  const permissionStore = usePermissionStore();
  const pageRoute = permissionStore.asyncRoutes.find((item) => item.path === '/page');
  const firstPath = (pageRoute?.redirect as string) || permissionStore.asyncRoutes[0]?.redirect;
  router.push((firstPath as string) || '/');
};

const getLogo = () => {
  if (collapsed.value) return AssetLogo;
  return AssetLogoFull;
};
</script>

<style lang="less" scoped></style>
