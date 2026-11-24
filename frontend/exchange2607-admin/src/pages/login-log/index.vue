<template>
  <div>
    <t-card :bordered="false">
      <collapsible-query-form
        :data="queryFormData"
        :label-width="QUERY_FORM_LABEL_WIDTH"
        @query="refreshTable"
      >
        <t-col :span="3">
          <t-form-item label="登录账号">
            <t-input v-model="queryFormData.userName"></t-input>
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="IP地址">
            <t-input v-model="queryFormData.ip"></t-input>
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="登录状态">
            <t-select
              v-model="queryFormData.status"
              clearable
              :options="statusDictItems"
              placeholder="请选择"
              :keys="{ label: 'dictItemName', value: 'dictItemCode' }"
            ></t-select>
          </t-form-item>
        </t-col>
        <template #more>
          <t-col :span="3">
            <t-form-item label="登录时间">
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
        :data="data"
        :columns="columns"
        table-layout="auto"
        :hover="true"
        :loading="loading"
        :pagination="pagination"
        @page-change="onPageChange"
      >
        <template #login_state="{ row }">
          <div class="table-custom-row">
            <t-tag size="small" variant="light" :theme="row.status === '1' ? 'success' : 'danger'">{{ row.statusName }}</t-tag>
            <div v-show="row.status === '0'">{{ row.msg }}</div>
          </div>
        </template>
      </t-table>
    </t-card>
  </div>
</template>
<script lang="ts">
export default {
  name: 'LoginLog',
};
</script>

<script setup lang="ts">
import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import { onMounted, ref } from 'vue';

import type { DictItem } from '@/api/dict';
import { findDictItemInCache } from '@/api/dict';
import CollapsibleQueryForm from '@/components/collapsible-query-form/index.vue';
import { QUERY_FORM_LABEL_WIDTH } from '@/constants/form';
import { buildDateRangeQuery, getDefaultDateRange, tableDateOnlyRangePresets } from '@/utils/dateRangePresets';
import { request } from '@/utils/request';

const statusDictItems = ref<DictItem[]>([]);
const columns: PrimaryTableCol<TableRowData>[] = [
  {
    colKey: 'userName',
    title: '登录账号',
  },
  {
    colKey: 'ip',
    title: 'IP地址',
  },
  {
    colKey: 'subSystemName',
    title: '子系统',
  },
  {
    colKey: 'browser',
    title: '浏览器',
  },
  {
    colKey: 'os',
    title: '操作系统',
  },
  {
    colKey: 'login_state',
    title: '登录状态',
  },
  {
    colKey: 'loginTime',
    title: '登录时间',
  },
];
const data = ref<TableRowData[]>([]);
const loading = ref(false);
const pagination = ref({
  pageSize: 10,
  current: 1,
  total: 0,
});
const queryFormData = ref({
  userName: '',
  ip: '',
  status: '',
  timeStart: '',
  timeEnd: '',
});
const dateRange = ref<string[]>(getDefaultDateRange());

onMounted(() => {
  findLoginStateDictItem();
  loadTableData();
});

const findLoginStateDictItem = async () => {
  statusDictItems.value = await findDictItemInCache('loginStatus');
};

const getQueryParam = () => {
  const range = buildDateRangeQuery(dateRange.value, { withTime: false });
  return {
    ...queryFormData.value,
    timeStart: range.timeStart,
    timeEnd: range.timeEnd,
  };
};

const onPageChange = (pageInfo: { current: number; pageSize: number }) => {
  pagination.value.current = pageInfo.current;
  pagination.value.pageSize = pageInfo.pageSize;
  loadTableData();
};

const refreshTable = () => {
  pagination.value.current = 1;
  loadTableData();
};

const loadTableData = async () => {
  loading.value = true;
  const queryParam = getQueryParam();
  const result = await request.post({
    url: '/loginLog/findLoginLogByPage',
    data: {
      ...queryParam,
      pageSize: pagination.value.pageSize,
      pageNum: pagination.value.current,
    },
  });
  data.value = result.content;
  pagination.value.total = result.total;
  loading.value = false;
};
</script>

<style lang="less" scoped></style>
