<template>
  <div>
    <t-card :bordered="false">
      <collapsible-query-form :data="queryForm" :label-width="QUERY_FORM_LABEL_WIDTH" @query="refreshTable">
        <t-col :span="3">
          <t-form-item label="会员账号">
            <t-input v-model="queryForm.memberUid" clearable placeholder="会员UID" />
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="动作">
            <t-select v-model="queryForm.action" clearable placeholder="全部" :options="actionDictItems"
              :keys="{ label: 'dictItemName', value: 'dictItemCode' }" />
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="类型">
            <t-select v-model="queryForm.contactType" clearable placeholder="全部" :options="contactTypeDictItems"
              :keys="{ label: 'dictItemName', value: 'dictItemCode' }" />
          </t-form-item>
        </t-col>
        <template #more>
          <t-col :span="3">
            <t-form-item label="联系方式">
              <div style="display: flex; align-items: center; gap: 8px; width: 100%; min-width: 0">
                <t-select v-if="queryForm.contactType === 'phone'" v-model="queryForm.phoneAreaCode" filterable
                  placeholder="区号" clearable style="width: 150px; flex-shrink: 0">
                  <t-option v-for="item in areaCodeOptions" :key="item.areaCode" :value="item.areaCode"
                    :label="`${item.countryName} +${item.areaCode}`" />
                </t-select>
                <t-input v-model="queryForm.contact"
                  :placeholder="queryForm.contactType === 'phone' ? '请输入手机号' : queryForm.contactType === 'email' ? '请输入邮箱' : '手机号或邮箱'"
                  clearable style="flex: 1; min-width: 0" />
              </div>
            </t-form-item>
          </t-col>
          <t-col :span="3">
            <t-form-item label="操作时间">
              <t-date-range-picker v-model="dateRange" class="query-date-range" :presets="tableDateOnlyRangePresets"
                allow-input clearable />
            </t-form-item>
          </t-col>
        </template>
      </collapsible-query-form>

      <div class="table-wrapper">
        <t-table row-key="id" :data="tableData" :columns="columns" table-layout="auto" :hover="true" :loading="loading"
          :pagination="pagination" @page-change="onPageChange">
          <template #contactType="{ row }">
            <t-tag size="small" :theme="row.contactType === 'phone' ? 'primary' : 'default'">{{ row.contactTypeName
            }}</t-tag>
          </template>
          <template #action="{ row }">
            <t-tag size="small" :theme="actionTheme(row.action)">{{ row.actionName }}</t-tag>
          </template>
          <template #memberUid="{ row }">
            <t-link v-if="row.memberUid != null" theme="primary" hover="color"
              @click="openMemberDetail(row.memberAccountId)">{{ row.memberUid }}</t-link>
            <span v-else class="table-muted">—</span>
          </template>
        </t-table>
      </div>
    </t-card>

    <member-account-detail-drawer v-model:visible="memberDetailVisible" :member-id="detailMemberId" />
  </div>
</template>

<script setup lang="ts">
import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import { onMounted, ref } from 'vue';

import CollapsibleQueryForm from '@/components/collapsible-query-form/index.vue';
import MemberAccountDetailDrawer from '@/components/member-account-detail-drawer/index.vue';
import { QUERY_FORM_LABEL_WIDTH } from '@/constants/form';
import { request } from '@/utils/request';
import { findByPage, MemberContactBindPageRow } from '@/api/memberContactBindLog';
import { findDictItemInCache } from '@/api/dict';
import type { DictItem } from '@/api/dict';
import { buildDateRangeQuery, getDefaultDateRange, tableDateOnlyRangePresets } from '@/utils/dateRangePresets';

interface AreaCodeOption {
  areaCode: string;
  countryName: string;
}

const queryForm = ref({
  memberUid: '',
  contactType: '',
  contact: '',
  phoneAreaCode: '86',
  action: '',
});

const memberDetailVisible = ref(false);
const detailMemberId = ref('');
function openMemberDetail(memberId?: string) {
  if (!memberId) return;
  detailMemberId.value = memberId;
  memberDetailVisible.value = true;
}
const dateRange = ref<string[]>(getDefaultDateRange());
const areaCodeOptions = ref<AreaCodeOption[]>([]);
const contactTypeDictItems = ref<DictItem[]>([]);
const actionDictItems = ref<DictItem[]>([]);
const tableData = ref<MemberContactBindPageRow[]>([]);
const loading = ref(false);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

const actionTheme = (a?: string) =>
  a === 'register' ? 'primary' : a === 'bind' ? 'success' : a === 'unbind' ? 'warning' : 'default';

const columns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'memberUid', title: '会员账号' },
  { colKey: 'action', title: '动作' },
  { colKey: 'contactType', title: '类型' },
   { colKey: 'phoneAreaCode', title: '区号' },
  { colKey: 'contact', title: '联系方式' },
  { colKey: 'createTime', title: '操作时间' },
];

const getQueryParams = () => {
  const dateQuery = buildDateRangeQuery(dateRange.value, { withTime: false });
  const p: Record<string, unknown> = {
    memberUid: queryForm.value.memberUid || undefined,
    contactType: queryForm.value.contactType || undefined,
    contact: queryForm.value.contact || undefined,
    phoneAreaCode: queryForm.value.contactType === 'phone' ? queryForm.value.phoneAreaCode : undefined,
    action: queryForm.value.action || undefined,
    startTime: dateQuery.timeStart,
    endTime: dateQuery.timeEnd,
    pageNum: pagination.value.current,
    pageSize: pagination.value.pageSize,
  };
  return p;
};

const loadTableData = async () => {
  loading.value = true;
  try {
    const page = await findByPage(getQueryParams());
    tableData.value = (page.content as unknown as MemberContactBindPageRow[]) || [];
    pagination.value.total = page.total;
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

const loadAreaCodes = async () => {
  if (areaCodeOptions.value.length) return;
  const list = await request.post<AreaCodeOption[]>({ url: '/countryAreaCode/findAll' });
  areaCodeOptions.value = list || [];
};

onMounted(async () => {
  contactTypeDictItems.value = await findDictItemInCache('contactType');
  actionDictItems.value = await findDictItemInCache('contactBindAction');
  await loadAreaCodes();
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
</style>
