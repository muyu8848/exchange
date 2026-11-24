<template>
  <div>
    <t-card :bordered="false">
      <collapsible-query-form
        :data="queryForm"
        :label-width="QUERY_FORM_LABEL_WIDTH"
        @query="refreshTable"
      >
        <t-col :span="3">
          <t-form-item label="订单号"><t-input v-model="queryForm.orderNo" clearable /></t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="会员账号"><t-input v-model="queryForm.memberUid" clearable placeholder="会员UID" /></t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="产品类型">
            <t-select
              v-model="queryForm.productType"
              clearable
              :options="productTypeOptions"
            />
          </t-form-item>
        </t-col>
        <template #more>
          <t-col :span="3">
          <t-form-item label="赎回方式">
            <t-select
              v-model="queryForm.redeemType"
              clearable
              :options="redeemTypeDictItems"
              :keys="{ label: 'dictItemName', value: 'dictItemCode' }"
            />
          </t-form-item>
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
          <t-col :span="3">
            <t-form-item label="赎回时间">
              <t-date-range-picker
                v-model="dateRange"
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
            <span class="subtotal-bar__label">笔数</span>
            <span class="subtotal-bar__value">{{ item.recordCount }}</span>
          </span>
          <span class="subtotal-bar__item">
            <span class="subtotal-bar__label">赎回合计</span>
            <span class="subtotal-bar__value subtotal-bar__value--gold">{{ item.totalRedeemAmount }}</span>
          </span>
        </div>
      </div>

      <div class="table-wrapper">
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
                <t-link theme="primary" hover="color" @click="openMemberDetail(row.memberAccountId)">{{
                  row.memberUid
                }}</t-link>
              </div>
            </div>
            <span v-else class="table-muted">—</span>
          </template>
          <template #productInfo="{ row }">
            <div class="table-stack">
              <div class="table-line">
                <span class="table-value">{{ row.assetCode }}</span>
                <t-tag size="small" variant="light-outline">{{ row.productTypeName }}</t-tag>
              </div>
              <div v-if="row.productType === 'FIXED'" class="table-line table-muted">
                锁{{ row.lockDays }}天
              </div>
            </div>
          </template>
          <template #orderNo="{ row }">
            <div class="table-line">
              <span class="table-value table-ellipsis" :title="row.orderNo">{{ row.orderNo }}</span>
              <copy-icon-button :text="row.orderNo" />
            </div>
          </template>
          <template #amountInfo="{ row }">
            <div class="table-stack table-stack--amount">
              <div>{{ row.redeemAmount }}</div>
            </div>
          </template>
          <template #operate="{ row }">
            <t-button size="small" variant="text" theme="primary" @click="openDetailDrawer(row.id)">详情</t-button>
          </template>
        </t-table>
      </div>
    </t-card>

    <investment-redeem-record-detail-drawer v-model:visible="drawerVisible" :log-id="detailLogId" />

    <member-account-detail-drawer v-model:visible="memberDetailVisible" :member-id="detailMemberId" />
  </div>
</template>

<script lang="ts">
export default { name: 'InvestmentRedeemRecord' };
</script>
<script setup lang="ts">
import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import { computed, onMounted, ref } from 'vue';

import type { DictItem } from '@/api/dict';
import { findDictItemInCache } from '@/api/dict';
import type { InvestmentRedeemRecordRow, InvestmentRedeemRecordQuery, InvestmentRedeemRecordSubtotal } from '@/api/investmentRedeemRecord';
import { findInvestmentRedeemRecordByPage, investmentRedeemRecordSubtotal } from '@/api/investmentRedeemRecord';
import { findAllCurrency, type CurrencyRow } from '@/api/currency';
import type { InvestmentProductRow } from '@/api/investmentProduct';
import { findAllInvestmentProducts } from '@/api/investmentProduct';
import CopyIconButton from '@/components/copy-icon-button/index.vue';
import CollapsibleQueryForm from '@/components/collapsible-query-form/index.vue';
import InvestmentRedeemRecordDetailDrawer from '@/components/investment-redeem-record-detail-drawer/index.vue';
import MemberAccountDetailDrawer from '@/components/member-account-detail-drawer/index.vue';
import { QUERY_FORM_LABEL_WIDTH } from '@/constants/form';
import { buildDateRangeQuery, getDefaultDateRange, tableDateOnlyRangePresets } from '@/utils/dateRangePresets';
import { buildProductOptions } from '@/utils/investmentProduct';

const queryForm = ref({
  orderNo: '',
  memberUid: '',
  productType: '',
  productId: '',
  redeemType: '',
  assetCode: '',
});
const dateRange = ref<string[]>(getDefaultDateRange());
const currencies = ref<CurrencyRow[]>([]);
const assetCodeOptions = computed(() =>
  currencies.value.map((c) => ({
    label: c.currencyName ? `${c.currencyCode}(${c.currencyName})` : c.currencyCode,
    value: c.currencyCode,
  })),
);
const products = ref<InvestmentProductRow[]>([]);
const productSelectOptions = computed(() => buildProductOptions(products.value));
const redeemTypeDictItems = ref<DictItem[]>([]);
const productTypeOptions = [
  { label: '活期', value: 'FLEXIBLE' },
  { label: '定期', value: 'FIXED' },
];
const tableData = ref<InvestmentRedeemRecordRow[]>([]);
const subtotal = ref<InvestmentRedeemRecordSubtotal | null>(null);
const loading = ref(false);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

const drawerVisible = ref(false);
const detailLogId = ref('');
const memberDetailVisible = ref(false);
const detailMemberId = ref('');

const columns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'orderNo', title: '订单号' },
  { colKey: 'member', title: '会员账号' },
  { colKey: 'productInfo', title: '产品' },
  { colKey: 'amountInfo', title: '赎回金额', align: 'right' },
  { colKey: 'redeemTypeName', title: '赎回方式' },
  { colKey: 'createTime', title: '赎回时间' },
  { colKey: 'operate', title: '操作' },
];

function getQueryParams(): InvestmentRedeemRecordQuery {
  const range = buildDateRangeQuery(dateRange.value, { withTime: false });
  return {
    orderNo: queryForm.value.orderNo || undefined,
    memberUid: queryForm.value.memberUid || undefined,
    productType: queryForm.value.productType || undefined,
    productId: queryForm.value.productId || undefined,
    redeemType: queryForm.value.redeemType || undefined,
    assetCode: queryForm.value.assetCode || undefined,
    createTimeStart: range.timeStart || undefined,
    createTimeEnd: range.timeEnd || undefined,
    pageNum: pagination.value.current,
    pageSize: pagination.value.pageSize,
  };
}

async function loadTableData() {
  loading.value = true;
  try {
    const params = getQueryParams();
    const { pageNum, pageSize, ...subtotalParams } = params;
    const [page, subtotalResult] = await Promise.all([
      findInvestmentRedeemRecordByPage(params),
      investmentRedeemRecordSubtotal(subtotalParams),
    ]);
    tableData.value = page.content || [];
    pagination.value.total = page.total || 0;
    subtotal.value = subtotalResult;
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

function openDetailDrawer(id: string) {
  detailLogId.value = id;
  drawerVisible.value = true;
}

function openMemberDetail(memberId?: string) {
  if (!memberId) return;
  detailMemberId.value = memberId;
  memberDetailVisible.value = true;
}

onMounted(async () => {
  const [currencyList, redeemTypes, allProducts] = await Promise.all([
    findAllCurrency(),
    findDictItemInCache('investmentRedeemType'),
    findAllInvestmentProducts(),
  ]);
  currencies.value = currencyList;
  redeemTypeDictItems.value = redeemTypes || [];
  products.value = allProducts || [];
  await loadTableData();
});
</script>

<style lang="less" scoped>
.table-wrapper {
  width: 100%;
  overflow-x: auto;
  :deep(.t-table) {
    width: 100%;
  }
}
.table-stack {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  &--amount {
    align-items: flex-end;
    text-align: right;
  }
}
.table-line {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
}
.table-muted {
  color: var(--td-text-color-placeholder);
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
