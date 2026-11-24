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
          <t-form-item label="订单号"><t-input v-model="queryForm.orderNo" clearable /></t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="会员账号"><t-input v-model="queryForm.memberUid" clearable placeholder="会员UID" /></t-form-item>
        </t-col>
        <t-col :span="3">
            <t-form-item label="产品">
              <t-select
                v-model="queryForm.productId"
                clearable
                filterable
                :options="productSelectOptions"
              />
            </t-form-item>
          </t-col>
        <t-col :span="3">
          <t-form-item label="币种">
            <t-select v-model="queryForm.assetCode" clearable filterable :options="assetCodeOptions" placeholder="请选择币种" />
          </t-form-item>
        </t-col>
        <template #more>
          <t-col :span="3">
            <t-form-item label="状态">
              <t-select
                v-model="queryForm.status"
                clearable
                :options="statusDictItems"
                :keys="{ label: 'dictItemName', value: 'dictItemCode' }"
              />
            </t-form-item>
          </t-col>
          <t-col :span="3">
            <t-form-item label="申购时间">
              <t-date-range-picker
                v-model="investDateRange"
                class="query-date-range"
                :presets="tableDateOnlyRangePresets"
                allow-input
                clearable
              />
            </t-form-item>
          </t-col>
          <t-col :span="3">
            <t-form-item label="到期日">
              <t-date-range-picker
                v-model="maturityDateRange"
                class="query-date-range"
                :presets="tableDateOnlyRangePresets"
                allow-input
                clearable
              />
            </t-form-item>
          </t-col>
        </template>
      </collapsible-query-form>

      <div v-if="subtotal && subtotal.items.length > 0" class="subtotal-bar">
        <div v-for="item in subtotal.items" :key="item.assetCode" class="subtotal-bar__group">
          <span class="subtotal-bar__asset">{{ item.assetCode }}</span>
          <span class="subtotal-bar__item">
            <span class="subtotal-bar__label">持仓笔数</span>
            <span class="subtotal-bar__value">{{ item.orderCount }}</span>
          </span>
          <span class="subtotal-bar__item">
            <span class="subtotal-bar__label">总本金</span>
            <span class="subtotal-bar__value subtotal-bar__value--gold">{{ item.totalPrincipal }}</span>
          </span>
          <span class="subtotal-bar__item">
            <span class="subtotal-bar__label">在投笔数</span>
            <span class="subtotal-bar__value">{{ item.investingCount }}</span>
          </span>
          <span class="subtotal-bar__item">
            <span class="subtotal-bar__label">在投金额</span>
            <span class="subtotal-bar__value subtotal-bar__value--gold">{{ item.totalInvestingPrincipal }}</span>
          </span>
          <span class="subtotal-bar__item">
            <span class="subtotal-bar__label">估算总收益</span>
            <span class="subtotal-bar__value subtotal-bar__value--gold">{{ item.totalEstimatedInterest }}</span>
          </span>
          <span class="subtotal-bar__item">
            <span class="subtotal-bar__label">已确认收益</span>
            <span class="subtotal-bar__value subtotal-bar__value--gold">{{ item.totalConfirmedInterest }}</span>
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
        <template #orderNo="{ row }">
          <div class="table-line">
            <span class="table-value table-ellipsis" :title="row.orderNo">{{ row.orderNo }}</span>
            <copy-icon-button :text="row.orderNo" />
          </div>
        </template>

        <template #member="{ row }">
          <div v-if="row.memberUid != null" class="table-stack">
            <div>
              <t-link theme="primary" hover="color" @click="openMemberDetail(row.memberAccountId)">{{ row.memberUid }}</t-link>
            </div>
          </div>
        </template>

        <template #productInfo="{ row }">
          <div class="table-stack">
            <div class="table-line">
              <span class="table-value">{{ row.assetCode }}</span>
            </div>
            <div class="table-line table-muted">
              锁{{ row.lockDays }}天 · 年化{{ row.annualRate }}%
            </div>
          </div>
        </template>

        <template #dateInfo="{ row }">
          <div class="table-stack">
            <div class="table-muted">申购：{{ row.investTime || '—' }}</div>
            <div class="table-value--highlight">到期：{{ row.maturityDate || '—' }}</div>
          </div>
        </template>

        <template #status="{ row }">
          <t-tag size="small" variant="light" :theme="positionStatusTheme(row.status)">{{ row.statusName }}</t-tag>
        </template>

        <template #operate="{ row }">
          <t-button size="small" variant="text" theme="primary" @click="openDetail(row)">详情</t-button>
        </template>
      </t-table>
    </t-card>

    <investment-fixed-position-detail-dialog
      v-model:visible="logDialogVisible"
      :position-id="detailPositionId"
      @member-click="openMemberDetail"
    />

    <member-account-detail-drawer v-model:visible="memberDetailVisible" :member-id="detailMemberId" />
  </div>
</template>
<script lang="ts">
export default { name: 'InvestmentFixedPosition' };
</script>
<script setup lang="ts">
import type { PageInfo, PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import { computed, onMounted, ref } from 'vue';

import { findAllCurrency, type CurrencyRow } from '@/api/currency';
import type { DictItem } from '@/api/dict';
import { findDictItemInCache } from '@/api/dict';
import type {
  InvestmentFixedPositionRow,
  InvestmentFixedPositionSubtotal,
} from '@/api/investmentFixedPosition';
import {
  findInvestmentFixedPositionByPage,
  investmentFixedPositionSubtotal,
} from '@/api/investmentFixedPosition';
import type { InvestmentProductRow } from '@/api/investmentProduct';
import { findAllInvestmentProducts } from '@/api/investmentProduct';
import CopyIconButton from '@/components/copy-icon-button/index.vue';
import CollapsibleQueryForm from '@/components/collapsible-query-form/index.vue';
import InvestmentFixedPositionDetailDialog from '@/components/investment-fixed-position-detail-dialog/index.vue';
import MemberAccountDetailDrawer from '@/components/member-account-detail-drawer/index.vue';
import { QUERY_FORM_LABEL_WIDTH } from '@/constants/form';
import { tableDateOnlyRangePresets } from '@/utils/dateRangePresets';
import { downloadBlobGet } from '@/utils/download';
import { buildProductOptions } from '@/utils/investmentProduct';
import { positionStatusTheme } from '@/utils/investmentPositionStatus';

const statusDictItems = ref<DictItem[]>([]);
const productOptions = ref<InvestmentProductRow[]>([]);
const productSelectOptions = computed(() => buildProductOptions(productOptions.value, 'FIXED'));
const currencies = ref<CurrencyRow[]>([]);
const assetCodeOptions = computed(() =>
  currencies.value.map((c) => ({
    label: c.currencyName ? `${c.currencyCode}(${c.currencyName})` : c.currencyCode,
    value: c.currencyCode,
  })),
);
const queryForm = ref({
  orderNo: '',
  memberUid: '',
  productId: '',
  status: '',
  assetCode: '',
});
const investDateRange = ref<string[]>([]);
const maturityDateRange = ref<string[]>([]);
const tableData = ref<InvestmentFixedPositionRow[]>([]);
const subtotal = ref<InvestmentFixedPositionSubtotal | null>(null);
const loading = ref(false);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

const logDialogVisible = ref(false);
const detailPositionId = ref('');
const memberDetailVisible = ref(false);
const detailMemberId = ref('');

const columns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'orderNo', title: '订单号' },
  { colKey: 'member', title: '会员账号' },
  { colKey: 'productInfo', title: '产品' },
  { colKey: 'principal', title: '本金', align: 'right' },
  { colKey: 'estimatedTotalInterest', title: '估算总收益', align: 'right' },
  { colKey: 'confirmedInterest', title: '已确认收益', align: 'right' },
  { colKey: 'status', title: '状态' },
  { colKey: 'dateInfo', title: '时间' },
  { colKey: 'operate', title: '操作' },
];



function buildInvestTimeRange(dateRange: string[]) {
  if (!dateRange?.length) {
    return {};
  }
  const startDate = dateRange[0].slice(0, 10);
  const endDate = dateRange[1].slice(0, 10);
  return {
    investTimeStart: `${startDate} 00:00:00`,
    investTimeEnd: `${endDate} 23:59:59`,
  };
}

function buildLocalDateRange(dateRange: string[], startKey: string, endKey: string) {
  if (!dateRange?.length) {
    return {};
  }
  return {
    [startKey]: dateRange[0].slice(0, 10),
    [endKey]: dateRange[1].slice(0, 10),
  };
}

function getQueryParams() {
  return {
    ...queryForm.value,
    memberUid: queryForm.value.memberUid || undefined,
    ...buildInvestTimeRange(investDateRange.value),
    ...buildLocalDateRange(maturityDateRange.value, 'maturityDateStart', 'maturityDateEnd'),
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
      findInvestmentFixedPositionByPage(params),
      investmentFixedPositionSubtotal(subtotalParams),
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
  const params = { ...getQueryParams() };
  delete (params as { pageNum?: number }).pageNum;
  delete (params as { pageSize?: number }).pageSize;
  await downloadBlobGet('/investmentFixedPosition/exportData', params, '定期持仓.xlsx');
}

function openMemberDetail(memberId?: string) {
  if (!memberId) return;
  detailMemberId.value = memberId;
  memberDetailVisible.value = true;
}

function openDetail(row: InvestmentFixedPositionRow) {
  detailPositionId.value = row.id;
  logDialogVisible.value = true;
}

onMounted(async () => {
  [statusDictItems.value, productOptions.value, currencies.value] = await Promise.all([
    findDictItemInCache('investmentPositionStatus'),
    findAllInvestmentProducts(),
    findAllCurrency(),
  ]);
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

.table-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.table-value--highlight {
  font-weight: 600;
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
