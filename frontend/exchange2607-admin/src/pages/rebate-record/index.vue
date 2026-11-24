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
          <t-form-item label="受益人账号"
            ><t-input v-model="queryForm.beneficiaryUid" placeholder="会员UID" clearable
          /></t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="来源会员"
            ><t-input v-model="queryForm.sourceMemberUid" placeholder="会员UID" clearable
          /></t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="业务类型">
            <t-select
              v-model="queryForm.bizType"
              clearable
              :options="bizTypeDictItems"
              :keys="{ label: 'dictItemName', value: 'dictItemCode' }"
            />
          </t-form-item>
        </t-col>
        <template #more>
          <t-col :span="3">
            <t-form-item label="币种">
              <t-select v-model="queryForm.assetCode" clearable :options="assetCodeOptions" />
            </t-form-item>
          </t-col>
          <t-col :span="3">
            <t-form-item label="层级">
              <t-input-number v-model="queryForm.level" :min="1" theme="normal" clearable style="width: 100%" />
            </t-form-item>
          </t-col>
          <t-col :span="3">
            <t-form-item label="返佣时间">
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
            <span class="subtotal-bar__label">返佣总额</span>
            <span class="subtotal-bar__value subtotal-bar__value--gold">{{ item.totalAmount }}</span>
          </span>
          <span class="subtotal-bar__item">
            <span class="subtotal-bar__label">笔数</span>
            <span class="subtotal-bar__value">{{ item.count }}</span>
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
        <template #beneficiaryInfo="{ row }">
          <t-link v-if="row.beneficiaryUid != null" theme="primary" hover="color" @click="openMemberDetail(row.beneficiaryId)">{{ row.beneficiaryUid }}</t-link>
          <span v-else class="table-muted">—</span>
        </template>
        <template #sourceMemberInfo="{ row }">
          <t-link v-if="row.sourceMemberUid != null" theme="primary" hover="color" @click="openMemberDetail(row.sourceMemberId)">{{ row.sourceMemberUid }}</t-link>
          <span v-else class="table-muted">—</span>
        </template>
        <template #level="{ row }">
          <t-tag size="small" theme="primary" variant="light">第{{ row.level }}级</t-tag>
        </template>
        <template #amount="{ row }">
          <span class="amount-main">{{ row.amount }}</span>
          <span class="amount-asset">{{ row.assetCode }}</span>
        </template>
        <template #bizType="{ row }">
          <t-tag size="small" variant="light">{{ row.bizTypeName }}</t-tag>
        </template>
        <template #sourceOrderId="{ row }">
          <div v-if="row.sourceOrderId" class="table-stack">
            <div>
              {{ row.sourceOrderId }}
              <copy-icon-button :text="row.sourceOrderId" />
            </div>
          </div>
          <span v-else class="table-muted">—</span>
        </template>
        <template #createTime="{ row }">
          <span>{{ row.createTime }}</span>
        </template>
      </t-table>
    </t-card>

    <member-account-detail-drawer v-model:visible="memberDetailVisible" :member-id="detailMemberId" />
  </div>
</template>

<script lang="ts">
export default {
  name: 'RebateRecord',
};
</script>

<script setup lang="ts">
import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import { onMounted, ref } from 'vue';

import type { RebateRecordQuery, RebateRecordRow, RebateRecordSubtotal } from '@/api/rebateRecord';
import { findRebateRecordByPage, rebateRecordSubtotal } from '@/api/rebateRecord';
import { findAllCurrency } from '@/api/currency';
import CollapsibleQueryForm from '@/components/collapsible-query-form/index.vue';
import CopyIconButton from '@/components/copy-icon-button/index.vue';
import { findDictItemInCache } from '@/api/dict';
import type { DictItem } from '@/api/dict';
import { QUERY_FORM_LABEL_WIDTH } from '@/constants/form';
import { buildDateRangeQuery, getDefaultDateRange, tableDateOnlyRangePresets } from '@/utils/dateRangePresets';
import { downloadBlobGet } from '@/utils/download';
import MemberAccountDetailDrawer from '@/components/member-account-detail-drawer/index.vue';

const queryForm = ref<RebateRecordQuery>({
  beneficiaryUid: '',
  sourceMemberUid: '',
  bizType: '',
  assetCode: '',
  level: undefined,
});

const assetCodeOptions = ref<{ label: string; value: string }[]>([]);
const dateRange = ref<string[]>(getDefaultDateRange());
const tableData = ref<RebateRecordRow[]>([]);
const subtotal = ref<RebateRecordSubtotal | null>(null);
const loading = ref(false);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

const memberDetailVisible = ref(false);
const detailMemberId = ref<string>('');

const bizTypeDictItems = ref<DictItem[]>([]);

const columns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'beneficiaryInfo', title: '受益人账号' },
  { colKey: 'sourceMemberInfo', title: '来源会员' },
  { colKey: 'level', title: '层级' },
  { colKey: 'amount', title: '返佣金额' },
  { colKey: 'bizType', title: '业务类型' },
  { colKey: 'sourceOrderId', title: '来源订单号' },
  { colKey: 'createTime', title: '返佣时间' },
];

const getQueryParams = (): RebateRecordQuery => ({
  ...queryForm.value,
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
      findRebateRecordByPage(params),
      rebateRecordSubtotal(subtotalParams),
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
  await downloadBlobGet('/rebateRecord/exportData', params);
};

onMounted(async () => {
  const [bizTypes, currencies] = await Promise.all([
    findDictItemInCache('rebateBizType'),
    findAllCurrency(),
  ]);
  bizTypeDictItems.value = bizTypes || [];
  assetCodeOptions.value = (currencies || []).map((c) => ({
    label: c.currencyName ? `${c.currencyCode}(${c.currencyName})` : c.currencyCode,
    value: c.currencyCode,
  }));
  await loadTableData();
});

const openMemberDetail = (memberId?: string) => {
  if (!memberId) return;
  detailMemberId.value = memberId;
  memberDetailVisible.value = true;
};
</script>

<style lang="less" scoped>
.table-stack {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  font-size: 13px;
}

.table-muted {
  font-size: 12px;
  color: var(--td-text-color-placeholder);
}

.amount-main {
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.amount-asset {
  margin-left: 4px;
  font-size: 12px;
  font-weight: 500;
  color: var(--td-text-color-secondary);
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
