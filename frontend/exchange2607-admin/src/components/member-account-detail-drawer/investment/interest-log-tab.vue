<script setup lang="ts">
import { ref, watch } from 'vue';

import type { PrimaryTableCol } from 'tdesign-vue-next';
import { findInvestmentInterestLogByPage } from '@/api/investmentInterestLog';
import type { InvestmentInterestLogRow } from '@/api/investmentInterestLog';

import CopyIconButton from '@/components/copy-icon-button/index.vue';
import { usePaged } from '../use-paged';

const props = defineProps<{
  memberUid?: number | string;
}>();

const columns: PrimaryTableCol<InvestmentInterestLogRow>[] = [
  { colKey: 'productInfo', title: '币种/产品' },
  { colKey: 'interestTypeName', title: '收益类型' },
  { colKey: 'accountRef', title: '持仓·账户' },
  { colKey: 'interestDate', title: '计息日' },
  { colKey: 'principal', title: '本金', align: 'right' },
  { colKey: 'interestAmount', title: '收益金额', align: 'right' },
  { colKey: 'createTime', title: '入账时间' },
];

const { rows, loading, pagination, load, reset, onPageChange } =
  usePaged<InvestmentInterestLogRow>(async (page, pageSize) => {
    const uid = String(props.memberUid ?? '');
    if (!uid) {
      return { content: [], total: 0 };
    }
    return findInvestmentInterestLogByPage({ memberUid: uid, pageNum: page, pageSize });
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
    <template #productInfo="{ row }">
      <div class="table-stack">
        <div class="table-line">
          <span class="table-value">{{ row.assetCode }}</span>
          <t-tag size="small" variant="light-outline">{{ row.productTypeName }}</t-tag>
        </div>
      </div>
    </template>
    <template #accountRef="{ row }">
      <div v-if="row.flexibleAccountId || row.positionId" class="table-line">
        <span class="table-value table-ellipsis" :title="row.flexibleAccountId || row.positionId">
          {{ row.flexibleAccountId || row.positionId }}
        </span>
        <CopyIconButton :text="row.flexibleAccountId || row.positionId" />
      </div>
      <span v-else class="table-muted">—</span>
    </template>
  </t-table>
</template>

<style lang="less" scoped>
@import '../investment-shared.less';
</style>
