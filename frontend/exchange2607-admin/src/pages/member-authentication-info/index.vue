<template>
  <div>
    <t-card :bordered="false">
      <div class="status-filter">
        <t-radio-group v-model="selectedStatus" variant="primary-filled" @change="refreshTable">
          <t-radio-button v-for="item in statusTabs" :key="item.dictItemCode" :value="item.dictItemCode">
            {{ item.dictItemName }}
          </t-radio-button>
        </t-radio-group>
      </div>

      <collapsible-query-form
        :data="queryForm"
        :label-width="QUERY_FORM_LABEL_WIDTH"
        style="margin-top: 16px"
        @query="refreshTable"
      >
        <t-col :span="3">
          <t-form-item label="会员账号"><t-input v-model="queryForm.memberUid" clearable placeholder="会员UID" /></t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="真实姓名"><t-input v-model="queryForm.realName" clearable /></t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="证件类型">
            <t-select
              v-model="queryForm.idType"
              clearable
              :options="idTypeDictItems"
              :keys="{ label: 'dictItemName', value: 'dictItemCode' }"
            />
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="证件号码"><t-input v-model="queryForm.idNumber" clearable /></t-form-item>
        </t-col>
        <template #more>
          <t-col :span="3">
            <t-form-item label="提交时间">
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
        <template #memberUid="{ row }">
          <t-link v-if="row.memberUid != null" theme="primary" hover="color" @click="openMemberDetail(row.memberAccountId)">{{ row.memberUid }}</t-link>
          <span v-else class="table-muted">—</span>
        </template>
        <template #status="{ row }">
          <t-tag size="small" variant="light" :theme="authStatusTheme(row.status)">{{ row.statusName }}</t-tag>
        </template>
        <template #action="{ row }">
          <t-button v-if="row.status === '2' && hasPermission('memberAuthenticationInfo.audit')" size="small" theme="primary" @click="openAudit(row)">审核</t-button>
          <t-button v-else size="small" variant="text" theme="primary" @click="openAudit(row)">详情</t-button>
        </template>
      </t-table>
    </t-card>

    <member-account-detail-drawer v-model:visible="memberDetailVisible" :member-id="detailMemberId" />

    <!-- 实名审核 / 详情弹窗（审核操作在弹窗内完成，完成后通知刷新列表） -->
    <authentication-audit-dialog v-model:visible="auditVisible" :record="auditRow" @updated="loadTableData" />

  </div>
</template>

<script lang="ts">
export default {
  name: 'MemberAuthenticationInfo',
};
</script>

<script setup lang="ts">
import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import { computed, onMounted, ref } from 'vue';
import { hasPermission } from '@/utils/permission';
import type { LocationQuery } from 'vue-router';
import { useRoute } from 'vue-router';

import type { DictItem } from '@/api/dict';
import { findDictItemInCache } from '@/api/dict';
import type { AuthInfoRow } from '@/api/member';
import { findAuthenticationInfoByPage } from '@/api/member';
import AuthenticationAuditDialog from '@/components/authentication-audit-dialog/index.vue';
import CollapsibleQueryForm from '@/components/collapsible-query-form/index.vue';
import MemberAccountDetailDrawer from '@/components/member-account-detail-drawer/index.vue';
import { QUERY_FORM_LABEL_WIDTH } from '@/constants/form';
import { useTodoQuerySync } from '@/hooks/useTodoQuerySync';
import { buildDateRangeQuery, tableDateOnlyRangePresets } from '@/utils/dateRangePresets';

const authStatusTheme = (status: string) => {
  if (status === '3') {
    return 'success';
  }
  if (status === '2') {
    return 'warning';
  }
  if (status === '4') {
    return 'danger';
  }
  return 'default';
};

const route = useRoute();

const statusFromQuery = (query: LocationQuery) => String(query.status || '2');

const selectedStatus = ref(statusFromQuery(route.query));
const statusDictItems = ref<DictItem[]>([]);
const idTypeDictItems = ref<DictItem[]>([]);
const queryForm = ref({ memberUid: '', realName: '', idNumber: '', idType: '' });
const dateRange = ref<string[]>([]);
const tableData = ref<AuthInfoRow[]>([]);
const loading = ref(false);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

const memberDetailVisible = ref(false);
const detailMemberId = ref<string>();

// 审核 / 详情弹窗
const auditVisible = ref(false);
const auditRow = ref<AuthInfoRow | null>(null);

const columns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'memberUid', title: '会员账号' },
  { colKey: 'realName', title: '真实姓名' },
  { colKey: 'idTypeName', title: '证件类型' },
  { colKey: 'idNumber', title: '证件号码', ellipsis: true },
  { colKey: 'status', title: '状态' },
  { colKey: 'createTime', title: '提交时间' },
  { colKey: 'action', title: '操作', fixed: 'right' },
];

/** 状态 tab：「全部」置空，后端 status 为空时不过滤 */
const statusTabs = computed(() => [
  { dictItemCode: '', dictItemName: '全部' },
  ...statusDictItems.value,
]);

const getQueryParams = () => ({
  ...queryForm.value,
  status: selectedStatus.value,
  ...buildDateRangeQuery(dateRange.value, { withTime: false }),
  pageNum: pagination.value.current,
  pageSize: pagination.value.pageSize,
});

const loadTableData = async () => {
  loading.value = true;
  try {
    const pageResult = await findAuthenticationInfoByPage(getQueryParams());
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

// 顶栏待办跳转时组件可能被缓存复用，靠此同步状态筛选
useTodoQuerySync('member-authentication-info', (query) => {
  selectedStatus.value = statusFromQuery(query);
  refreshTable();
});

const onPageChange = (pageInfo: { current: number; pageSize: number }) => {
  pagination.value.current = pageInfo.current;
  pagination.value.pageSize = pageInfo.pageSize;
  loadTableData();
};

const openAudit = (row: AuthInfoRow) => {
  auditRow.value = row;
  auditVisible.value = true;
};

onMounted(async () => {
  const [states, idTypes] = await Promise.all([
    findDictItemInCache('authenticationStatus'),
    findDictItemInCache('idType'),
  ]);
  statusDictItems.value = states.filter((item) => ['2', '3', '4'].includes(item.dictItemCode));
  if (!statusDictItems.value.length) {
    statusDictItems.value = states;
  }
  idTypeDictItems.value = idTypes || [];
  await loadTableData();
});

const openMemberDetail = (memberId?: string) => {
  if (!memberId) return;
  detailMemberId.value = memberId;
  memberDetailVisible.value = true;
};
</script>

<style lang="less" scoped>
.status-filter {
  margin-bottom: 4px;
}


</style>
