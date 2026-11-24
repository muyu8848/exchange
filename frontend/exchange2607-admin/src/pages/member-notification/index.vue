<template>
  <div>
    <t-card :bordered="false">
      <collapsible-query-form
        :data="queryForm"
        :label-width="QUERY_FORM_LABEL_WIDTH"
        :create-label="hasPermission('memberNotification.send') ? '发送消息' : ''"
        @query="refreshTable"
        @create="openSendDialog"
      >
        <t-col :span="3">
          <t-form-item label="会员账号"><t-input v-model="queryForm.memberUid" clearable placeholder="会员UID" /></t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="消息类型">
            <t-select
              v-model="queryForm.type"
              clearable
              :options="typeDictItems"
              :keys="{ label: 'dictItemName', value: 'dictItemCode' }"
            />
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="已读状态">
            <t-select v-model="readFlag" clearable placeholder="全部" :options="readFlagOptions" />
          </t-form-item>
        </t-col>
        <template #more>
          <t-col :span="3">
            <t-form-item label="创建时间">
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
        <template #accountInfo="{ row }">
          <div v-if="row.memberUid != null" class="table-stack">
            <div>
              <t-link theme="primary" hover="color" @click="openMemberDetail(row.memberAccountId)">{{
                row.memberUid
              }}</t-link>
            </div>
          </div>
          <span v-else class="table-muted">—</span>
        </template>
        <template #content="{ row }">
          <div class="table-content">{{ row.content || '—' }}</div>
        </template>
        <template #readFlag="{ row }">
          <t-tag size="small" :theme="row.readFlag ? 'default' : 'warning'">{{ row.readFlag ? '已读' : '未读' }}</t-tag>
        </template>
        <template #readTime="{ row }">
          <span :class="{ 'table-muted': !row.readTime }">{{ row.readTime || '—' }}</span>
        </template>
      </t-table>
    </t-card>

    <t-dialog v-model:visible="sendVisible" header="发送系统消息" width="520px" @confirm="submitSend">
      <t-form :label-width="100">
        <t-form-item label="会员账号"><t-input v-model="sendForm.memberUid" clearable placeholder="会员UID" /></t-form-item>
        <t-form-item label="标题"><t-input v-model="sendForm.title" clearable /></t-form-item>
        <t-form-item label="内容"><t-textarea v-model="sendForm.content" :autosize="{ minRows: 4 }" /></t-form-item>
      </t-form>
    </t-dialog>

    <member-account-detail-drawer v-model:visible="memberDetailVisible" :member-id="detailMemberId" />
  </div>
</template>

<script lang="ts">
export default { name: 'MemberNotification' };
</script>

<script setup lang="ts">
import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, ref } from 'vue';

import type { DictItem } from '@/api/dict';
import { findDictItemInCache } from '@/api/dict';
import type { MemberNotificationRow } from '@/api/memberNotification';
import {
  findMemberNotificationByPage,
  sendMemberSystemMessage,
} from '@/api/memberNotification';
import CollapsibleQueryForm from '@/components/collapsible-query-form/index.vue';
import { QUERY_FORM_LABEL_WIDTH } from '@/constants/form';
import MemberAccountDetailDrawer from '@/components/member-account-detail-drawer/index.vue';
import { buildDateRangeQuery, tableDateOnlyRangePresets } from '@/utils/dateRangePresets';
import { hasPermission } from '@/utils/permission';

const queryForm = ref({ memberUid: '', type: '' });
const readFlag = ref<string | undefined>('');
const dateRange = ref<string[]>([]);
const typeDictItems = ref<DictItem[]>([]);
const tableData = ref<MemberNotificationRow[]>([]);
const loading = ref(false);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });
const sendVisible = ref(false);
const sendForm = ref({ memberUid: '', title: '', content: '' });

const memberDetailVisible = ref(false);
const detailMemberId = ref<string>();

const readFlagOptions = [
  { label: '未读', value: 'false' },
  { label: '已读', value: 'true' },
];

const columns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'accountInfo', title: '会员账号' },
  { colKey: 'typeName', title: '消息类型' },
  { colKey: 'title', title: '标题', ellipsis: true },
  { colKey: 'content', title: '内容', width: 360 },
  { colKey: 'readFlag', title: '已读状态' },
  { colKey: 'readTime', title: '已读时间' },
  { colKey: 'createTime', title: '创建时间' },
];

const getQueryParams = () => {
  const dateQuery = buildDateRangeQuery(dateRange.value, { withTime: true });
  return {
    memberUid: queryForm.value.memberUid ? Number(queryForm.value.memberUid) : undefined,
    type: queryForm.value.type || undefined,
    readFlag: readFlag.value == null || readFlag.value === ''
      ? undefined
      : readFlag.value === 'true',
    createTimeStart: dateQuery.timeStart || undefined,
    createTimeEnd: dateQuery.timeEnd || undefined,
    pageNum: pagination.value.current,
    pageSize: pagination.value.pageSize,
  };
};

const loadTableData = async () => {
  loading.value = true;
  try {
    const pageResult = await findMemberNotificationByPage(getQueryParams());
    tableData.value = pageResult.content;
    pagination.value.total = pageResult.total;
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

function openSendDialog() {
  sendForm.value = { memberUid: '', title: '', content: '' };
  sendVisible.value = true;
}

async function submitSend() {
  await sendMemberSystemMessage({
    ...sendForm.value,
    memberUid: Number(sendForm.value.memberUid),
  });
  MessagePlugin.success('发送成功');
  sendVisible.value = false;
  await loadTableData();
}

const openMemberDetail = (memberId?: string) => {
  if (!memberId) return;
  detailMemberId.value = memberId;
  memberDetailVisible.value = true;
};

onMounted(async () => {
  typeDictItems.value = await findDictItemInCache('memberNotificationType');
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

.table-muted {
  color: var(--td-text-color-secondary);
  font-size: 12px;
}

.table-content {
  white-space: normal;
  word-break: break-word;
  line-height: 1.6;
  font-size: 13px;
  color: var(--td-text-color-primary);
}
</style>
