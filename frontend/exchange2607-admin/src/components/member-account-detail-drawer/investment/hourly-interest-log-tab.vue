<script setup lang="ts">
import { ref, watch } from 'vue';

import type { PrimaryTableCol } from 'tdesign-vue-next';
import { findInvestmentHourlyInterestLogByPage } from '@/api/investmentHourlyInterestLog';
import type { InvestmentHourlyInterestLogRow } from '@/api/investmentHourlyInterestLog';

import CopyIconButton from '@/components/copy-icon-button/index.vue';
import { usePaged } from '../use-paged';

const props = defineProps<{
  memberUid?: number | string;
}>();

const columns: PrimaryTableCol<InvestmentHourlyInterestLogRow>[] = [
  { colKey: 'assetCode', title: '币种' },
  { colKey: 'flexibleAccountId', title: '活期账户' },
  { colKey: 'interestDate', title: '计息日' },
  { colKey: 'interestHour', title: '计息小时' },
  { colKey: 'principal', title: '计息本金', align: 'right' },
  { colKey: 'interestAmount', title: '小时收益', align: 'right' },
  { colKey: 'createTime', title: '入账时间' },
];

const { rows, loading, pagination, load, reset, onPageChange } =
  usePaged<InvestmentHourlyInterestLogRow>(async (page, pageSize) => {
    const uid = String(props.memberUid ?? '');
    if (!uid) {
      return { content: [], total: 0 };
    }
    return findInvestmentHourlyInterestLogByPage({ memberUid: uid, pageNum: page, pageSize });
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
    <template #flexibleAccountId="{ row }">
      <div v-if="row.flexibleAccountId" class="table-line">
        <span class="table-value table-ellipsis" :title="row.flexibleAccountId">{{ row.flexibleAccountId }}</span>
        <CopyIconButton :text="row.flexibleAccountId" />
      </div>
      <span v-else class="table-muted">—</span>
    </template>
    <template #interestHour="{ row }">
      <span class="table-value">{{ row.interestHour }}:00</span>
    </template>
  </t-table>
</template>

<style lang="less" scoped>
@import '../investment-shared.less';
</style>
