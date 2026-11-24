<script setup lang="ts">
import { ref } from 'vue';

import AssetChangeLogTab from './asset-change-log-tab.vue';
import AssetTransferTab from './asset-transfer-tab.vue';
import RebateTab from './rebate-tab.vue';

defineProps<{
  memberUid?: number | string;
}>();

const activeSubTab = ref('changeLog');
</script>

<template>
  <div class="tab-body">
    <t-tabs v-model="activeSubTab">
      <t-tab-panel value="changeLog" label="账变流水">
        <AssetChangeLogTab :member-uid="memberUid" />
      </t-tab-panel>
      <t-tab-panel value="assetTransfer" label="资产划转" lazy>
        <AssetTransferTab :member-uid="memberUid" />
      </t-tab-panel>
      <t-tab-panel value="rebate" label="返佣记录" lazy>
        <RebateTab :member-uid="memberUid" />
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
