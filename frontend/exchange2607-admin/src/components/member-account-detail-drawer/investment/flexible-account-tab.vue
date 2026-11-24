<script setup lang="ts">
import { ref, watch } from 'vue';

import type { PrimaryTableCol } from 'tdesign-vue-next';
import { findInvestmentFlexibleAccountByPage } from '@/api/investmentFlexibleAccount';
import type { InvestmentFlexibleAccountRow } from '@/api/investmentFlexibleAccount';

import { usePaged } from '../use-paged';

const props = defineProps<{
  memberUid?: number | string;
}>();

const columns: PrimaryTableCol<InvestmentFlexibleAccountRow>[] = [
  { colKey: 'assetInfo', title: '币种/利率' },
  { colKey: 'position', title: '持仓金额', align: 'right' },
  { colKey: 'accruedInterest', title: '累计收益', align: 'right' },
  { colKey: 'comprehensiveRate', title: '综合年化', align: 'right' },
  { colKey: 'createTime', title: '开户时间' },
];

const { rows, loading, pagination, load, reset, onPageChange } =
  usePaged<InvestmentFlexibleAccountRow>(async (page, pageSize) => {
    const uid = String(props.memberUid ?? '');
    if (!uid) {
      return { content: [], total: 0 };
    }
    return findInvestmentFlexibleAccountByPage({ memberUid: uid, pageNum: page, pageSize });
  });

const loaded = ref(false);
watch(
  () => props.memberUid,
  () => {
    reset();
    loaded.value = false;
    load();
    loaded.value = true;
  },
  { immediate: true },
);
</script>

<template>
  <t-table
    :data="rows"
    :columns="columns"
    row-key="id"
    size="small"
    table-layout="auto"
    bordered
    :loading="loading"
    :pagination="pagination"
    @page-change="onPageChange"
  >
    <template #assetInfo="{ row }">
      <div class="table-stack">
        <div class="table-line">
          <span class="table-value">{{ row.assetCode }}</span>
          <t-tag size="small" variant="light-outline">年化{{ row.annualRate }}%</t-tag>
        </div>
        <div v-if="row.subsidizedRate && row.rewardLimit" class="table-line table-muted">
          补贴{{ row.subsidizedRate }}% · 限额{{ row.rewardLimit }}
        </div>
      </div>
    </template>
    <template #comprehensiveRate="{ row }">
      <span class="table-value">{{ row.comprehensiveRate }}%</span>
    </template>
  </t-table>
</template>

<style lang="less" scoped>
@import '../investment-shared.less';
</style>
