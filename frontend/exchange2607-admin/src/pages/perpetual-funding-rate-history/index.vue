<template>
  <div>
    <t-card :bordered="false">
      <collapsible-query-form :data="queryForm" :label-width="QUERY_FORM_LABEL_WIDTH" @query="refreshTable">
        <t-col :span="3">
          <t-form-item label="交易对">
            <t-select v-model="queryForm.symbolCode" clearable :options="symbolOptions" />
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="结算时间">
            <t-date-range-picker v-model="dateRange" class="query-date-range" :presets="tableDateOnlyRangePresets"
              allow-input clearable />
          </t-form-item>
        </t-col>
      </collapsible-query-form>

      <div class="table-wrapper">
        <t-table row-key="id" :data="tableData" :columns="columns" table-layout="auto" :hover="true" :loading="loading"
          :pagination="pagination" @page-change="onPageChange">
          <template #fundingRate="{ row }">
            <span :class="rateClass(row.fundingRate)">{{ formatFundingRate(row.fundingRate) }}</span>
          </template>
        </t-table>
      </div>
    </t-card>
  </div>
</template>
<script lang="ts">
export default { name: 'PerpetualFundingRateHistory' };
</script>
<script setup lang="ts">
import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import { onMounted, ref } from 'vue';

import type { FundingRateHistoryRow } from '@/api/perpetualFundingRate';
import { findFundingRateHistoryByPage } from '@/api/perpetualFundingRate';
import { findAllPerpetualContractSymbol } from '@/api/perpetualContractSymbol';
import CollapsibleQueryForm from '@/components/collapsible-query-form/index.vue';
import { QUERY_FORM_LABEL_WIDTH } from '@/constants/form';
import { buildDateRangeQuery, getDefaultDateRange, tableDateOnlyRangePresets } from '@/utils/dateRangePresets';

const queryForm = ref({ symbolCode: '' });
const dateRange = ref<string[]>(getDefaultDateRange());
const tableData = ref<FundingRateHistoryRow[]>([]);
const loading = ref(false);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });
const symbolOptions = ref<{ label: string; value: string }[]>([]);

const columns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'symbolCode', title: '交易对' },
  { colKey: 'fundingRate', title: '资金费率' },
  { colKey: 'fundingTime', title: '结算时间', fixed: 'right' },
];

function formatFundingRate(rate?: number) {
  if (rate == null) return '—';
  const pct = rate * 100;
  return `${pct >= 0 ? '+' : ''}${pct.toFixed(4)}%`;
}

function rateClass(rate?: number) {
  if (rate == null) return '';
  return rate >= 0 ? 'pnl-profit' : 'pnl-loss';
}

function getQueryParams() {
  const range = buildDateRangeQuery(dateRange.value, { withTime: false });
  return {
    symbolCode: queryForm.value.symbolCode || undefined,
    ...range,
    pageNum: pagination.value.current,
    pageSize: pagination.value.pageSize,
  };
}

async function loadTableData() {
  loading.value = true;
  try {
    const page = await findFundingRateHistoryByPage(getQueryParams());
    tableData.value = page.content || [];
    pagination.value.total = page.total || 0;
  } finally {
    loading.value = false;
  }
}

function refreshTable() {
  pagination.value.current = 1;
  loadTableData();
}

function onPageChange(pageInfo: { current: number; pageSize: number }) {
  pagination.value.current = pageInfo.current;
  pagination.value.pageSize = pageInfo.pageSize;
  loadTableData();
}

onMounted(async () => {
  const symbols = await findAllPerpetualContractSymbol();
  symbolOptions.value = (symbols || []).map((it: any) => ({ label: it.symbolCode || '', value: it.symbolCode || '' }));
  loadTableData();
});
</script>
