<script setup lang="ts">
import { ref } from 'vue';

import FixedPositionTab from './investment/fixed-position-tab.vue';
import FlexiblePositionTab from './investment/flexible-position-tab.vue';
import FlexibleAccountTab from './investment/flexible-account-tab.vue';
import InvestmentRecordTab from './investment/investment-record-tab.vue';
import RedeemRecordTab from './investment/redeem-record-tab.vue';
import InterestLogTab from './investment/interest-log-tab.vue';
import HourlyInterestLogTab from './investment/hourly-interest-log-tab.vue';

defineProps<{
  memberUid?: number | string;
}>();

const activeSubTab = ref('fixedPosition');
</script>

<template>
  <div class="tab-body">
    <t-tabs v-model="activeSubTab">
      <t-tab-panel value="fixedPosition" label="定期持仓">
        <FixedPositionTab :member-uid="memberUid" />
      </t-tab-panel>
      <t-tab-panel value="flexiblePosition" label="活期持仓" lazy>
        <FlexiblePositionTab :member-uid="memberUid" />
      </t-tab-panel>
      <t-tab-panel value="flexibleAccount" label="活期账户" lazy>
        <FlexibleAccountTab :member-uid="memberUid" />
      </t-tab-panel>
      <t-tab-panel value="investmentRecord" label="投资记录" lazy>
        <InvestmentRecordTab :member-uid="memberUid" />
      </t-tab-panel>
      <t-tab-panel value="redeemRecord" label="赎回记录" lazy>
        <RedeemRecordTab :member-uid="memberUid" />
      </t-tab-panel>
      <t-tab-panel value="interestLog" label="日收益流水" lazy>
        <InterestLogTab :member-uid="memberUid" />
      </t-tab-panel>
      <t-tab-panel value="hourlyInterestLog" label="小时收益流水" lazy>
        <HourlyInterestLogTab :member-uid="memberUid" />
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
