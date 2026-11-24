<template>
  <div>
    <t-card :bordered="false">
      <collapsible-query-form :data="queryForm" :label-width="QUERY_FORM_LABEL_WIDTH" show-export @query="refreshTable"
        @export="handleExport">
        <t-col :span="3">
          <t-form-item label="会员UID">
            <t-input v-model="queryForm.memberUid" placeholder="会员UID" clearable />
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="业务订单号">
            <t-input v-model="queryForm.bizOrderNo" placeholder="支持模糊查询" clearable />
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="币种">
            <t-select v-model="queryForm.assetCode" clearable filterable placeholder="全部" :options="currencyOptions" />
          </t-form-item>
        </t-col>
        <template #more>
          <t-col :span="3">
            <t-form-item label="变动类型">
              <t-select v-model="changeTypes" clearable multiple :min-collapsed-num="1" :options="typeDictItems"
                :keys="{ label: 'dictItemName', value: 'dictItemCode' }" />
            </t-form-item>
          </t-col>
          <t-col :span="3">
            <t-form-item label="账户类型">
              <t-select v-model="queryForm.accountType" clearable placeholder="全部" :options="accountTypeOptions"
                :keys="{ label: 'dictItemName', value: 'dictItemCode' }" />
            </t-form-item>
          </t-col>

          <t-col :span="3">
            <t-form-item label="收支方向">
              <t-select v-model="queryForm.direction" clearable placeholder="全部" :options="directionOptions" />
            </t-form-item>
          </t-col>
          <t-col :span="3">
            <t-form-item label="变动时间">
              <t-date-range-picker v-model="dateRange" class="query-date-range" :presets="tableDateOnlyRangePresets"
                allow-input clearable />
            </t-form-item>
          </t-col>
        </template>
      </collapsible-query-form>

      <div v-if="subtotal && subtotal.items.length > 0" class="subtotal-bar">
        <div v-for="item in subtotal.items" :key="item.assetCode" class="subtotal-bar__group">
          <span class="subtotal-bar__asset">{{ item.assetCode }}</span>
          <span class="subtotal-bar__item">
            <span class="subtotal-bar__label">收入</span>
            <span class="subtotal-bar__value subtotal-bar__value--in">{{ item.income }}</span>
          </span>
          <span class="subtotal-bar__item">
            <span class="subtotal-bar__label">支出</span>
            <span class="subtotal-bar__value subtotal-bar__value--out">{{ item.expenditure }}</span>
          </span>
          <span class="subtotal-bar__item">
            <span class="subtotal-bar__label">笔数</span>
            <span class="subtotal-bar__value">{{ item.recordCount }}</span>
          </span>
        </div>
      </div>

      <t-table row-key="id" :data="tableData" :columns="columns" table-layout="auto" :hover="true" :loading="loading"
        :pagination="pagination" @page-change="onPageChange">
        <template #accountInfo="{ row }">
          <div v-if="row.memberUid != null" class="table-stack">
            <div>
              <t-link theme="primary" hover="color" @click="openMemberDetail(row.memberAccountId)">{{ row.memberUid
                }}</t-link>
            </div>
          </div>
          <span v-else class="table-muted">—</span>
        </template>
        <template #bizOrderNo="{ row }">
          <div v-if="row.bizOrderNo" class="table-stack">
            <div>
              {{ row.bizOrderNo }}
              <copy-icon-button :text="row.bizOrderNo" />
            </div>
          </div>
          <span v-else class="table-muted">—</span>
        </template>
        <template #changeType="{ row }">
          <div class="table-stack change-type-cell">
            <t-tag size="small" :theme="balanceChangeTheme(row.balanceChange)">{{ row.changeTypeName }}</t-tag>
            <div v-if="row.note" class="table-muted">{{ row.note }}</div>
          </div>
        </template>
        <template #balanceChange="{ row }">
          <div class="table-stack balance-change">
            <div>
              <span class="balance-change__before">变动前 {{ row.balanceBefore }}</span>
              <span class="balance-change__delta" :class="balanceDeltaClass(row.balanceChange)">
                {{ formatBalanceDelta(row.balanceChange) }}
              </span>
            </div>
            <div class="balance-change__after">变动后 {{ row.balanceAfter }}</div>
          </div>
        </template>
      </t-table>
    </t-card>

    <member-account-detail-drawer v-model:visible="memberDetailVisible" :member-id="detailMemberId" />
  </div>
</template>

<script lang="ts">
export default { name: 'MemberAssetChangeLog' };
</script>

<script setup lang="ts">
import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

import type { DictItem } from '@/api/dict';
import { findDictItemInCache } from '@/api/dict';
import type { CurrencyRow } from '@/api/currency';
import { findAllCurrency } from '@/api/currency';
import type {
  MemberAssetChangeLogQuery,
  MemberAssetChangeLogRow,
  MemberAssetChangeSubtotal,
} from '@/api/memberAssetChangeLog';
import { findMemberAssetChangeLogByPage, memberAssetChangeLogSubtotal } from '@/api/memberAssetChangeLog';
import CollapsibleQueryForm from '@/components/collapsible-query-form/index.vue';
import CopyIconButton from '@/components/copy-icon-button/index.vue';
import MemberAccountDetailDrawer from '@/components/member-account-detail-drawer/index.vue';
import { QUERY_FORM_LABEL_WIDTH } from '@/constants/form';
import { buildDateRangeQuery, getDefaultDateRange, tableDateOnlyRangePresets } from '@/utils/dateRangePresets';
import { downloadBlobGet } from '@/utils/download';

const route = useRoute();
const queryForm = ref({
  memberUid: String(route.query.memberUid || ''),
  bizOrderNo: '',
  direction: '',
  accountType: '',
  assetCode: '',
});
const changeTypes = ref<string[]>([]);
const dateRange = ref<string[]>(getDefaultDateRange());
const typeDictItems = ref<DictItem[]>([]);
const currencies = ref<CurrencyRow[]>([]);
const tableData = ref<MemberAssetChangeLogRow[]>([]);
const subtotal = ref<MemberAssetChangeSubtotal | null>(null);
const loading = ref(false);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });
const memberDetailVisible = ref(false);
const detailMemberId = ref('');

const directionOptions = [
  { label: '收入', value: 'in' },
  { label: '支出', value: 'out' },
];

const accountTypeOptions = ref<DictItem[]>([]);

const currencyOptions = computed(() =>
  (currencies.value || []).map((c) => ({
    label: c.currencyName ? `${c.currencyName}(${c.currencyCode})` : c.currencyCode,
    value: c.currencyCode,
  })),
);

const columns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'accountInfo', title: '会员UID' },
  { colKey: 'accountTypeName', title: '账户' },
  { colKey: 'assetCode', title: '币种' },
  { colKey: 'bizOrderNo', title: '业务订单号' },
  { colKey: 'changeType', title: '变动类型' },
  { colKey: 'changeTime', title: '变动时间' },
  { colKey: 'balanceChange', title: '金额变化' },
];

const balanceChangeTheme = (change: number) => {
  if (change > 0) {
    return 'success';
  }
  if (change < 0) {
    return 'danger';
  }
  return 'default';
};

const balanceDeltaClass = (change: number) => {
  if (change > 0) {
    return 'balance-change__delta--in';
  }
  if (change < 0) {
    return 'balance-change__delta--out';
  }
  return '';
};

const formatBalanceDelta = (change: number) => {
  const prefix = change > 0 ? '+' : '';
  return `变化 ${prefix}${change}`.trim();
};

const getQueryParams = (): MemberAssetChangeLogQuery => ({
  ...queryForm.value,
  memberUid: queryForm.value.memberUid ? Number(queryForm.value.memberUid) : undefined,
  direction: queryForm.value.direction || undefined,
  accountType: queryForm.value.accountType || undefined,
  assetCode: queryForm.value.assetCode || undefined,
  changeType: changeTypes.value.length ? changeTypes.value.join(',') : undefined,
  ...buildDateRangeQuery(dateRange.value, { withTime: false }),
  pageNum: pagination.value.current,
  pageSize: pagination.value.pageSize,
});

const loadTableData = async () => {
  loading.value = true;
  try {
    const params = getQueryParams();
    const { pageNum, pageSize, ...subtotalParams } = params;
    const [pageResult, subtotalResult] = await Promise.all([
      findMemberAssetChangeLogByPage(params),
      memberAssetChangeLogSubtotal(subtotalParams),
    ]);
    tableData.value = pageResult.content;
    pagination.value.total = pageResult.total;
    subtotal.value = subtotalResult;
  } finally {
    loading.value = false;
  }
};

const refreshTable = () => {
  pagination.value.current = 1;
  loadTableData();
};

const onPageChange = (pageInfo: { current: number; pageSize: number }) => {
  pagination.value.current = pageInfo.current;
  pagination.value.pageSize = pageInfo.pageSize;
  loadTableData();
};

const handleExport = async () => {
  const { pageNum, pageSize, ...params } = getQueryParams();
  await downloadBlobGet('/memberAssetChangeLog/exportData', params);
};

function openMemberDetail(memberId?: string) {
  if (!memberId) return;
  detailMemberId.value = memberId;
  memberDetailVisible.value = true;
}

onMounted(async () => {
  [typeDictItems.value, currencies.value, accountTypeOptions.value] = await Promise.all([
    findDictItemInCache('memberAssetChangeType'),
    findAllCurrency(),
    findDictItemInCache('assetAccountType'),
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

.change-type-cell {
  align-items: flex-start;
}

.change-type-cell :deep(.t-tag) {
  width: fit-content;
  max-width: 100%;
}

.table-muted {
  color: var(--td-text-color-secondary);
  font-size: 12px;
}

.balance-change {
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.balance-change__before {
  color: var(--td-text-color-secondary);
  font-weight: 400;
}

.balance-change__delta {
  padding-left: 8px;
}

.balance-change__delta--in {
  color: var(--td-success-color);
}

.balance-change__delta--out {
  color: var(--td-error-color);
}

.balance-change__after {
  color: var(--td-text-color-primary);
  font-weight: 500;
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
}

.subtotal-bar__value--in {
  color: var(--td-success-color);
}

.subtotal-bar__value--out {
  color: var(--td-error-color);
}
</style>
