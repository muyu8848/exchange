<template>
  <div>
    <t-card :bordered="false">
      <collapsible-query-form :data="queryFormData" :label-width="QUERY_FORM_LABEL_WIDTH" @query="refreshTable">
        <t-col :span="3">
          <t-form-item label="操作账号">
            <t-input v-model="queryFormData.operName"></t-input>
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="IP地址">
            <t-input v-model="queryFormData.ip"></t-input>
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="子系统">
            <t-select v-model="queryFormData.subSystem" clearable>
              <t-option v-for="item in subSystemOptions" :key="item.dictItemCode" :label="item.dictItemName"
                :value="item.dictItemCode" />
            </t-select>
          </t-form-item>
        </t-col>
        <template #more>
          <t-col :span="3">
            <t-form-item label="操作状态">
              <t-select v-model="queryFormData.status" clearable :options="statusOptions"
                :keys="{ label: 'dictItemName', value: 'dictItemCode' }" placeholder="全部" />
            </t-form-item>
          </t-col>
          <t-col :span="3">
            <t-form-item label="模块">
              <t-select v-model="queryFormData.module" clearable>
                <t-option v-for="item in moduleOptions" :key="item.dictItemCode" :label="item.dictItemName"
                  :value="item.dictItemCode" />
              </t-select>
            </t-form-item>
          </t-col>
          <t-col :span="3">
            <t-form-item label="操作时间">
              <t-date-range-picker v-model="dateRange" :presets="tableDateRangePresets" allow-input
                clearable></t-date-range-picker>
            </t-form-item>
          </t-col>
        </template>
      </collapsible-query-form>
      <t-table row-key="id" :data="data" :columns="columns" table-layout="auto" :hover="true" :loading="loading"
        :pagination="pagination" @page-change="onPageChange">
        <template #status="{ row }">
          <t-tag size="small" variant="light" :theme="row.status === 'SUCCESS' ? 'success' : 'danger'">
            {{ row.statusName || row.status || '—' }}
          </t-tag>
        </template>
        <template #costTime="{ row }">
          {{ row.costTime != null ? `${row.costTime} ms` : '—' }}
        </template>
        <template #op="{ row }">
          <t-link theme="primary" hover="color" @click="openDetail(row)">详情</t-link>
        </template>
      </t-table>
    </t-card>

    <t-dialog v-model:visible="detailVisible" header="操作详情" width="720px" :footer="false">
      <t-descriptions :column="2" bordered size="small">
        <t-descriptions-item label="操作账号">{{ detailRow?.operName || '—' }}</t-descriptions-item>
        <t-descriptions-item label="IP地址">{{ detailRow?.ip || '—' }}</t-descriptions-item>
        <t-descriptions-item label="子系统">{{ detailRow?.subSystemName || '—' }}</t-descriptions-item>
        <t-descriptions-item label="模块">{{ detailRow?.moduleName || '—' }}</t-descriptions-item>
        <t-descriptions-item label="操作">{{ detailRow?.operate || '—' }}</t-descriptions-item>
        <t-descriptions-item label="状态">
          {{ detailRow?.statusName || detailRow?.status || '—' }}
        </t-descriptions-item>
        <t-descriptions-item label="耗时">
          {{ detailRow?.costTime != null ? `${detailRow.costTime} ms` : '—' }}
        </t-descriptions-item>
        <t-descriptions-item label="操作时间">{{ detailRow?.operTime || '—' }}</t-descriptions-item>
        <t-descriptions-item label="请求地址" :span="2">
          {{ detailRow?.requestUrl || '—' }}
        </t-descriptions-item>
      </t-descriptions>

      <div class="detail-block">
        <div class="detail-block__title">请求参数</div>
        <pre class="detail-pre">{{ detailRow?.requestParam || '—' }}</pre>
      </div>

      <div v-if="detailRow?.errorMsg" class="detail-block">
        <div class="detail-block__title">异常信息</div>
        <pre class="detail-pre detail-pre--error">{{ detailRow.errorMsg }}</pre>
      </div>
    </t-dialog>
  </div>
</template>
<script lang="ts">
export default {
  name: 'OperLog',
};
</script>

<script setup lang="ts">
import { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import { onMounted, ref } from 'vue';

import { DictItem } from '@/api/dict';
import CollapsibleQueryForm from '@/components/collapsible-query-form/index.vue';
import { QUERY_FORM_LABEL_WIDTH } from '@/constants/form';
import { getDefaultDateTimeRange, tableDateRangePresets } from '@/utils/dateRangePresets';
import { request } from '@/utils/request';

const subSystemOptions = ref<DictItem[]>([]);
const moduleOptions = ref<DictItem[]>([]);
const statusOptions = ref<DictItem[]>([]);

const loadDictData = async () => {
  const subSystemResult = await request.post({
    url: '/dictconfig/findDictItemInCache',
    data: { dictTypeCode: 'subSystem' },
  });
  subSystemOptions.value = subSystemResult;

  const moduleResult = await request.post({
    url: '/dictconfig/findDictItemInCache',
    data: { dictTypeCode: 'operModule' },
  });
  moduleOptions.value = moduleResult;

  const statusResult = await request.post({
    url: '/dictconfig/findDictItemInCache',
    data: { dictTypeCode: 'operLogStatus' },
  });
  statusOptions.value = statusResult;
};

const columns: PrimaryTableCol<TableRowData>[] = [
  {
    colKey: 'operName',
    title: '操作账号',
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
    colKey: 'moduleName',
    title: '模块',
  },
  {
    colKey: 'operate',
    title: '操作',
  },
  {
    colKey: 'operTime',
    title: '操作时间',
  },
  {
    colKey: 'status',
    title: '状态',
  },
  {
    colKey: 'costTime',
    title: '耗时',
  },
  {
    colKey: 'op',
    title: '操作',
  },
];
const data = ref([]);
const loading = ref(false);
const pagination = ref({
  pageSize: 10,
  current: 1,
  total: 0,
});
const queryFormData = ref({
  operName: '',
  ip: '',
  subSystem: '',
  module: '',
  status: '',
  timeStart: '',
  timeEnd: '',
});
const dateRange = ref(getDefaultDateTimeRange());

const detailVisible = ref(false);
const detailRow = ref<Record<string, any> | null>(null);

function openDetail(row: Record<string, any>) {
  detailRow.value = row;
  detailVisible.value = true;
}

onMounted(() => {
  loadDictData();
  loadTableData();
});

const getQueryParam = () => {
  const queryParam = { ...queryFormData.value };
  queryParam.timeStart = '';
  queryParam.timeEnd = '';
  if (dateRange.value.length > 0) {
    const startDate = (dateRange.value[0] || '').slice(0, 10);
    const endDate = (dateRange.value[1] || '').slice(0, 10);
    queryParam.timeStart = `${startDate} 00:00:00`;
    queryParam.timeEnd = `${endDate} 23:59:59`;
  }
  return queryParam;
};

const onPageChange = (pageInfo) => {
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
    url: '/operLog/findOperLogByPage',
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

<style lang="less" scoped>
.detail-block {
  margin-top: 16px;
}

.detail-block__title {
  font-weight: 600;
  margin-bottom: 8px;
}

.detail-pre {
  margin: 0;
  padding: 12px;
  background: var(--td-bg-color-secondarycontainer);
  border-radius: 6px;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 240px;
  overflow: auto;
}

.detail-pre--error {
  color: var(--td-error-color);
}
</style>
