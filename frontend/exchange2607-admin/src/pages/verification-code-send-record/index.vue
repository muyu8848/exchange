<template>
  <div>
    <t-card :bordered="false">
      <collapsible-query-form
        :data="queryForm"
        :label-width="QUERY_FORM_LABEL_WIDTH"
        @query="refreshTable"
      >
        <t-col :span="3">
          <t-form-item label="联系方式">
            <t-input v-model="queryForm.contact" clearable placeholder="手机号或邮箱" />
          </t-form-item>
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
        <t-col :span="3">
          <t-form-item label="发送状态">
            <t-select
              v-model="queryForm.sendStatus"
              clearable
              :options="sendStatusDictItems"
              :keys="{ label: 'dictItemName', value: 'dictItemCode' }"
            />
          </t-form-item>
        </t-col>
        <template #more>
          <t-col :span="3">
            <t-form-item label="发送时间">
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
          <template #contact="{ row }">
            <span v-if="row.contact" class="table-mono">{{ formatContact(row) }}</span>
            <span v-else class="table-muted">—</span>
          </template>
          <template #verificationCode="{ row }">
            <span v-if="row.verificationCode" class="table-mono">{{ row.verificationCode }}</span>
            <span v-else class="table-muted">—</span>
          </template>
          <template #ip="{ row }">
            <span v-if="row.ip" class="table-mono">{{ row.ip }}</span>
            <span v-else class="table-muted">—</span>
          </template>
          <template #statusInfo="{ row }">
            <t-tag size="small" variant="light" :theme="statusTheme(row.sendStatus)">
              {{ row.sendStatusName || '—' }}
            </t-tag>
          </template>
          <template #operate="{ row }">
            <t-button size="small" variant="text" theme="primary" @click="openDetail(row)">详情</t-button>
          </template>
        </t-table>
      </div>
    </t-card>

    <verification-code-detail-drawer v-model:visible="detailVisible" :record="detailRecord" />
  </div>
</template>
<script lang="ts">
export default {
  name: 'VerificationCodeSendRecord',
};
</script>

<script setup lang="ts">
import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import { onMounted, ref } from 'vue';

import type { DictItem } from '@/api/dict';
import { findDictItemInCache } from '@/api/dict';
import {
  findVerificationCodeSendRecordByPage,
  type VerificationCodeSendRecordRow,
} from '@/api/verificationCodeSendRecord';
import CollapsibleQueryForm from '@/components/collapsible-query-form/index.vue';
import VerificationCodeDetailDrawer from '@/components/verification-code-detail-drawer/index.vue';
import { QUERY_FORM_LABEL_WIDTH } from '@/constants/form';
import { buildDateRangeQuery, getDefaultDateRange, tableDateOnlyRangePresets } from '@/utils/dateRangePresets';

const queryForm = ref({
  contact: '',
  bizType: '',
  sendStatus: '',
});

const dateRange = ref<string[]>(getDefaultDateRange());
const tableData = ref<VerificationCodeSendRecordRow[]>([]);
const loading = ref(false);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });
const bizTypeDictItems = ref<DictItem[]>([]);
const sendStatusDictItems = ref<DictItem[]>([]);

const detailVisible = ref(false);
const detailRecord = ref<VerificationCodeSendRecordRow | null>(null);

const columns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'contact', title: '联系方式' },
  { colKey: 'bizTypeName', title: '业务类型' },
  { colKey: 'verificationCode', title: '验证码' },
  { colKey: 'statusInfo', title: '发送状态' },
  { colKey: 'ip', title: 'IP' },
  { colKey: 'createTime', title: '创建时间' },
  { colKey: 'sendTime', title: '发送时间' },
  // 错误原因可能长达数百字符（含堆栈），不占用表格列宽，统一在详情抽屉中查看
  { colKey: 'operate', title: '操作', fixed: 'right' },
];

function getQueryParams() {
  const range = buildDateRangeQuery(dateRange.value, { withTime: false });
  return {
    ...queryForm.value,
    ...range,
    pageNum: pagination.value.current,
    pageSize: pagination.value.pageSize,
  };
}

async function loadTableData() {
  loading.value = true;
  try {
    const result = await findVerificationCodeSendRecordByPage(getQueryParams());
    tableData.value = result.content || [];
    pagination.value.total = result.total || 0;
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

function formatContact(row: VerificationCodeSendRecordRow) {
  if (!row.contact) {
    return '—';
  }
  if (row.phoneAreaCode) {
    return `+${row.phoneAreaCode} ${row.contact}`;
  }
  return row.contact;
}

function openDetail(row: VerificationCodeSendRecordRow) {
  detailRecord.value = row;
  detailVisible.value = true;
}

// sendStatus: 1 未发送 / 2 发送成功 / 3 发送失败（对应后端 Constant.验证码发送状态_*）
const statusTheme = (status?: string) => {
  if (status === '2') {
    return 'success';
  }
  if (status === '3') {
    return 'danger';
  }
  return 'warning';
};

onMounted(async () => {
  [bizTypeDictItems.value, sendStatusDictItems.value] = await Promise.all([
    findDictItemInCache('verificationCodeType'),
    findDictItemInCache('verificationCodeSendStatus'),
  ]);
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
.table-mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
}
.table-muted {
  color: var(--td-text-color-placeholder);
}
</style>
