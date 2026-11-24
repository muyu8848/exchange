<script setup lang="ts">
import { ref } from 'vue';

import ContactBindLogTab from './contact-bind-log-tab.vue';
import LoginLogTab from './login-log-tab.vue';
import LoginSessionTab from './login-session-tab.vue';

defineProps<{
  memberUid?: number | string;
  memberId?: string;
}>();

const activeSubTab = ref('loginSession');
</script>

<template>
  <div class="tab-body">
    <t-tabs v-model="activeSubTab">
      <t-tab-panel value="loginSession" label="登录会话">
        <LoginSessionTab :member-uid="memberUid" />
      </t-tab-panel>
      <t-tab-panel value="loginLog" label="登录日志" lazy>
        <LoginLogTab :member-id="memberId" />
      </t-tab-panel>
      <t-tab-panel value="contact" label="联系方式绑定日志" lazy>
        <ContactBindLogTab :member-uid="memberUid" />
      </t-tab-panel>
    </t-tabs>
  </div>
</template>

<style lang="less" scoped>
.tab-body {
  min-height: 200px;
}

/* 二级 tab 与表格之间留出间距 */
.tab-body :deep(.t-tabs__content) {
  padding-top: 12px;
}
</style>
