<template>
  <div>
    <t-card :bordered="false">
      <collapsible-query-form
        :data="queryForm"
        :label-width="QUERY_FORM_LABEL_WIDTH"
        @query="refreshTable"
      >
        <t-col :span="3">
          <t-form-item label="会员账号"><t-input v-model="queryForm.accountUid" clearable placeholder="会员UID" /></t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="设备类型">
            <t-select
              v-model="queryForm.deviceType"
              clearable
              :options="deviceTypeDictItems"
              :keys="{ label: 'dictItemName', value: 'dictItemCode' }"
              placeholder="全部"
            />
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="状态">
            <t-select
              v-model="statusList"
              clearable
              multiple
              :min-collapsed-num="1"
              :options="statusDictItems"
              :keys="{ label: 'dictItemName', value: 'dictItemCode' }"
              placeholder="全部"
            />
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="IP地址"><t-input v-model="queryForm.ip" clearable placeholder="请输入IP地址" /></t-form-item>
        </t-col>
        <template #more>
          <t-col :span="3">
            <t-form-item label="最后活跃">
              <t-date-range-picker v-model="dateRange" class="query-date-range" :presets="tableDateOnlyRangePresets" allow-input clearable />
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
          <template #member="{ row }">
            <t-link v-if="row.accountUid" theme="primary" hover="color" @click="openMemberDetail(row.accountId)">{{ row.accountUid }}</t-link>
            <span v-else class="table-muted">—</span>
          </template>
          <template #status="{ row }">
            <t-tag size="small" variant="light" :theme="statusTheme(row.status)">
              {{ row.statusName || row.status }}
            </t-tag>
          </template>
          <template #op="{ row }">
            <t-link v-if="row.status === 'ONLINE' && hasPermission('memberAccountLoginSession.kickout')" theme="primary" hover="color" @click="handleKickout(row)">强制下线</t-link>
            <span v-else class="table-muted">—</span>
          </template>
        </t-table>
      </div>
    </t-card>

    <member-account-detail-drawer v-model:visible="memberDetailVisible" :member-id="detailMemberId" />
  </div>
</template>
<script lang="ts">
export default { name: 'MemberAccountLoginSession' };
</script>
<script setup lang="ts">
import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { onMounted, ref } from 'vue';
import { hasPermission } from '@/utils/permission';

import type { AccountLoginSession, AccountLoginSessionQuery } from '@/api/accountLoginSession';
import type { DictItem } from '@/api/dict';
import { findDictItemInCache } from '@/api/dict';
import { findMemberAccountLoginSessionByPage, kickoutMemberAccountLoginSession } from '@/api/accountLoginSession';
import CollapsibleQueryForm from '@/components/collapsible-query-form/index.vue';
import MemberAccountDetailDrawer from '@/components/member-account-detail-drawer/index.vue';
import { QUERY_FORM_LABEL_WIDTH } from '@/constants/form';
import { buildDateRangeQuery, tableDateOnlyRangePresets } from '@/utils/dateRangePresets';

const deviceTypeDictItems = ref<DictItem[]>([]);
const statusDictItems = ref<DictItem[]>([]);

const queryForm = ref({
  accountUid: '',
  deviceType: '',
  ip: '',
});
const statusList = ref<string[]>(['ONLINE']);
const tableData = ref<AccountLoginSession[]>([]);
const loading = ref(false);
const dateRange = ref<string[]>([]);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });
const memberDetailVisible = ref(false);
const detailMemberId = ref('');

const columns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'member', title: '会员账号' },
  { colKey: 'deviceTypeName', title: '设备类型' },
  { colKey: 'ip', title: 'IP地址' },
  { colKey: 'loginTime', title: '登录时间' },
  { colKey: 'lastActiveTime', title: '最后活跃' },
  { colKey: 'logoutTime', title: '结束时间' },
  { colKey: 'status', title: '状态' },
  { colKey: 'op', title: '操作' },
];

function getQueryParams(): AccountLoginSessionQuery {
  return {
    ...queryForm.value,
    accountUid: queryForm.value.accountUid ? Number(queryForm.value.accountUid) : undefined,
    ip: queryForm.value.ip || undefined,
    status: statusList.value.length ? statusList.value.join(',') : undefined,
    ...buildDateRangeQuery(dateRange.value, { withTime: false }),
    pageNum: pagination.value.current,
    pageSize: pagination.value.pageSize,
  };
}

async function loadTableData() {
  loading.value = true;
  try {
    const page = await findMemberAccountLoginSessionByPage(getQueryParams());
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

function openMemberDetail(memberId?: string) {
  if (!memberId) return;
  detailMemberId.value = memberId;
  memberDetailVisible.value = true;
}

function statusTheme(status?: string): 'success' | 'default' | 'warning' | 'danger' {
  if (status === 'ONLINE') return 'success';
  if (status === 'KICKED' || status === 'REPLACED') return 'warning';
  if (status === 'EXPIRED') return 'default';
  return 'danger';
}

function handleKickout(row: AccountLoginSession) {
  const dialog = DialogPlugin.confirm({
    header: '提示',
    body: '确定要强制下线该会话吗？下线后该设备需要重新登录。',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async () => {
      await kickoutMemberAccountLoginSession(row.id);
      MessagePlugin.success('操作成功');
      dialog.destroy();
      await loadTableData();
    },
    onClose: () => dialog.destroy(),
  });
}

onMounted(async () => {
  const [statusItems, deviceItems] = await Promise.all([
    findDictItemInCache('accountLoginSessionStatus'),
    findDictItemInCache('loginDeviceType'),
  ]);
  statusDictItems.value = statusItems || [];
  deviceTypeDictItems.value = deviceItems || [];
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

.table-muted {
  color: var(--td-text-color-placeholder);
}
</style>
