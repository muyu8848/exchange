<template>
  <div>
    <t-card :bordered="false">
      <collapsible-query-form
        :data="queryForm"
        :label-width="QUERY_FORM_LABEL_WIDTH"
        show-export
        @query="refreshTable"
        @export="handleExport"
      >
        <t-col :span="3">
          <t-form-item label="会员账号"><t-input v-model="queryForm.memberUid" clearable placeholder="会员UID" /></t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="币种">
            <t-select v-model="queryForm.assetCode" clearable filterable :options="assetCodeOptions" placeholder="请选择币种" />
          </t-form-item>
        </t-col>
        <t-col :span="3">
            <t-form-item label="开户时间">
              <t-date-range-picker
                v-model="createDateRange"
                class="query-date-range"
                :presets="tableDateOnlyRangePresets"
                allow-input
                clearable
              />
            </t-form-item>
          </t-col>
      </collapsible-query-form>

      <div v-if="subtotal && subtotal.items.length > 0" class="subtotal-bar">
        <div v-for="item in subtotal.items" :key="item.assetCode" class="subtotal-bar__group">
          <span class="subtotal-bar__asset">{{ item.assetCode }}</span>
          <span class="subtotal-bar__item">
            <span class="subtotal-bar__label">账户数</span>
            <span class="subtotal-bar__value">{{ item.accountCount }}</span>
          </span>
          <span class="subtotal-bar__item">
            <span class="subtotal-bar__label">持仓金额</span>
            <span class="subtotal-bar__value subtotal-bar__value--gold">{{ item.totalPrincipal }}</span>
          </span>
          <span class="subtotal-bar__item">
            <span class="subtotal-bar__label">累计收益</span>
            <span class="subtotal-bar__value subtotal-bar__value--gold">{{ item.totalAccruedInterest }}</span>
          </span>
        </div>
      </div>
      <t-table
        row-key="id"
        :data="tableData"
        :columns="columns"
        table-layout="auto"
        :hover="true"
        :loading="loading"
        :pagination="pagination"
        @page-change="onPageChange"
      >
        <template #member="{ row }">
          <div v-if="row.memberUid != null" class="table-stack">
            <div>
              <t-link theme="primary" hover="color" @click="openMemberDetail(row.memberAccountId)">{{ row.memberUid }}</t-link>
            </div>
          </div>
        </template>

        <template #assetInfo="{ row }">
          <div class="table-stack">
            <div class="table-line">
              <span class="table-value">{{ row.assetCode }}</span>
              <t-tag size="small" variant="light-outline">年化{{ row.annualRate }}%</t-tag>
            </div>
            <div class="table-line table-muted">
              <template v-if="row.subsidizedRate && row.rewardLimit">
                补贴{{ row.subsidizedRate }}% · 奖励限额{{ row.rewardLimit }}
              </template>
            </div>
          </div>
        </template>

        <template #principalInfo="{ row }">
          <span class="table-value">{{ row.position }}</span>
        </template>

        <template #rateInfo="{ row }">
          <span class="table-value">{{ row.comprehensiveRate }}%</span>
        </template>

        <template #dateInfo="{ row }">
          <span class="table-value">{{ row.createTime }}</span>
        </template>

        <template #operate="{ row }">
          <t-button size="small" variant="text" theme="primary" @click="openDetail(row)">详情</t-button>
        </template>
      </t-table>
    </t-card>

    <investment-flexible-account-detail-dialog
      v-model:visible="detailDialogVisible"
      :account-id="detailAccountId"
      @member-click="openMemberDetail"
    />

    <member-account-detail-drawer v-model:visible="memberDetailVisible" :member-id="detailMemberId" />
  </div>
</template>
<script lang="ts">
export default { name: 'InvestmentFlexibleAccount' };
</script>
<script setup lang="ts">
import type { PageInfo, PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import { computed, onMounted, ref } from 'vue';

import { findAllCurrency, type CurrencyRow } from '@/api/currency';
import type {
  InvestmentFlexibleAccountQuery,
  InvestmentFlexibleAccountRow,
  InvestmentFlexibleAccountSubtotal,
} from '@/api/investmentFlexibleAccount';
import {
  findInvestmentFlexibleAccountByPage,
  investmentFlexibleAccountSubtotal,
} from '@/api/investmentFlexibleAccount';
import CollapsibleQueryForm from '@/components/collapsible-query-form/index.vue';
import InvestmentFlexibleAccountDetailDialog from '@/components/investment-flexible-account-detail-dialog/index.vue';
import MemberAccountDetailDrawer from '@/components/member-account-detail-drawer/index.vue';
import { QUERY_FORM_LABEL_WIDTH } from '@/constants/form';
import { tableDateOnlyRangePresets } from '@/utils/dateRangePresets';
import { downloadBlobGet } from '@/utils/download';

const currencies = ref<CurrencyRow[]>([]);
const assetCodeOptions = computed(() =>
  currencies.value.map((c) => ({
    label: c.currencyName ? `${c.currencyCode}(${c.currencyName})` : c.currencyCode,
    value: c.currencyCode,
  })),
);
const queryForm = ref({
  memberUid: '',
  assetCode: '',
});
const createDateRange = ref<string[]>([]);
const tableData = ref<InvestmentFlexibleAccountRow[]>([]);
const subtotal = ref<InvestmentFlexibleAccountSubtotal | null>(null);
const loading = ref(false);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

const detailDialogVisible = ref(false);
const detailAccountId = ref('');
const memberDetailVisible = ref(false);
const detailMemberId = ref('');

const columns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'member', title: '会员账号' },
  { colKey: 'assetInfo', title: '币种/利率' },
  { colKey: 'principalInfo', title: '持仓金额', align: 'right' },
  { colKey: 'accruedInterest', title: '累计收益', align: 'right' },
  { colKey: 'rateInfo', title: '综合年化', align: 'right' },
  { colKey: 'dateInfo', title: '开户时间' },
  { colKey: 'operate', title: '操作' },
];

function buildDateTimeRange(dateRange: string[], startKey: string, endKey: string) {
  if (!dateRange?.length) {
    return {};
  }
  return {
    [startKey]: `${dateRange[0].slice(0, 10)} 00:00:00`,
    [endKey]: `${dateRange[1].slice(0, 10)} 23:59:59`,
  };
}

function getQueryParams() {
  return {
    ...queryForm.value,
    memberUid: queryForm.value.memberUid || undefined,
    ...buildDateTimeRange(createDateRange.value, 'createTimeStart', 'createTimeEnd'),
    pageNum: pagination.value.current,
    pageSize: pagination.value.pageSize,
  };
}

async function loadTableData() {
  loading.value = true;
  try {
    const params = getQueryParams();
    const { pageNum, pageSize, ...subtotalParams } = params;
    const [page, sum] = await Promise.all([
      findInvestmentFlexibleAccountByPage(params),
      investmentFlexibleAccountSubtotal(subtotalParams),
    ]);
    tableData.value = page.content || [];
    pagination.value.total = page.total || 0;
    subtotal.value = sum;
  } finally {
    loading.value = false;
  }
}

function refreshTable() {
  pagination.value.current = 1;
  loadTableData();
}

function onPageChange(pageInfo: PageInfo) {
  pagination.value.current = pageInfo.current;
  pagination.value.pageSize = pageInfo.pageSize;
  loadTableData();
}

async function handleExport() {
  const params = { ...getQueryParams() } as InvestmentFlexibleAccountQuery;
  delete (params as { pageNum?: number }).pageNum;
  delete (params as { pageSize?: number }).pageSize;
  await downloadBlobGet('/investmentFlexibleAccount/exportData', params, '活期赚币账户.xlsx');
}

function openMemberDetail(memberId?: string) {
  if (!memberId) return;
  detailMemberId.value = memberId;
  memberDetailVisible.value = true;
}

function openDetail(row: InvestmentFlexibleAccountRow) {
  detailAccountId.value = row.id;
  detailDialogVisible.value = true;
}

onMounted(async () => {
  currencies.value = await findAllCurrency();
  await loadTableData();
});
</script>

<style lang="less" scoped>
.table-stack {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
}

.table-line {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.table-muted {
  color: var(--td-text-color-secondary);
  font-size: 12px;
}

.table-value {
  color: var(--td-text-color-primary);
}

.subtotal-bar {
  display: inline-flex;
  flex-wrap: wrap;
  align-items: stretch;
  gap: 8px 24px;
  width: fit-content;
  max-width: 100%;
  margin-top: 20px;
  margin-bottom: 16px;
  padding: 12px 18px;
  border-radius: var(--td-radius-medium);
  background: var(--td-bg-color-secondarycontainer);
  border: 1px solid var(--td-component-border);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6);
}

.subtotal-bar__group {
  display: inline-flex;
  align-items: baseline;
  gap: 8px;

  &:not(:last-child) {
    padding-right: 24px;
    border-right: 1px solid var(--td-component-stroke);
  }
}

.subtotal-bar__asset {
  font-size: 14px;
  font-weight: 700;
  color: var(--td-brand-color);
}

.subtotal-bar__item {
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
  white-space: nowrap;
}

.subtotal-bar__label {
  font-size: 13px;
  color: var(--td-text-color-secondary);
}

.subtotal-bar__value {
  font-size: 14px;
  font-weight: 600;
  color: var(--td-text-color-primary);
  font-variant-numeric: tabular-nums;

  &--gold {
    color: var(--td-warning-color);
  }
}
</style>
