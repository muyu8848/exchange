<template>
  <t-card :bordered="false">
    <collapsible-query-form :data="queryForm" :label-width="QUERY_FORM_LABEL_WIDTH"
      :create-label="hasPermission('c2cCurrency.save') ? '新增币种' : ''"
      @query="refreshTable" @create="openCreate">
      <t-col :span="3">
        <t-form-item label="币种">
          <t-select v-model="queryForm.currencyCode" clearable filterable :options="currencyOptions"
            placeholder="请选择币种" />
        </t-form-item>
      </t-col>
      <t-col :span="3">
        <t-form-item label="状态">
          <t-select v-model="queryForm.status" clearable :options="statusDictItems"
            :keys="{ label: 'dictItemName', value: 'dictItemCode' }" />
        </t-form-item>
      </t-col>
    </collapsible-query-form>

    <t-table row-key="id" :data="tableData" :columns="columns" :loading="loading" :hover="true" table-layout="auto"
      :pagination="pagination" @page-change="onPageChange">
      <template #status="{ row }">
        <t-tag :theme="row.status === '1' ? 'success' : 'default'" variant="light">
          {{ statusLabel(row.status) }}
        </t-tag>
      </template>
      <template #operate="{ row }">
        <t-space>
          <t-button v-if="hasPermission('c2cCurrency.save')" size="small" variant="text" theme="primary" @click="openEdit(row)">编辑</t-button>
          <t-button v-if="hasPermission('c2cCurrency.del')" size="small" variant="text" theme="danger" @click="handleDelete(row.id)">删除</t-button>
        </t-space>
      </template>
    </t-table>

    <t-dialog v-model:visible="dialogVisible" :header="form.id ? '编辑币种' : '新增币种'" placement="center" width="560px"
      @confirm="submitForm" :close-on-overlay-click="false">
      <t-form :label-width="100">
        <t-form-item label="币种">
          <t-select v-model="form.currencyCode" :disabled="!!form.id" filterable :options="currencyOptions"
            placeholder="请选择币种" />
        </t-form-item>
        <t-form-item label="排序号">
          <t-input-number v-model="form.sortNum" theme="normal" style="width: 100%" />
        </t-form-item>
        <t-form-item label="状态">
          <t-select v-model="form.status" :options="statusDictItems"
            :keys="{ label: 'dictItemName', value: 'dictItemCode' }" />
        </t-form-item>
        <t-form-item label="备注">
          <t-textarea v-model="form.remark" :rows="2" placeholder="选填" />
        </t-form-item>
      </t-form>
    </t-dialog>
  </t-card>
</template>

<script lang="ts">
export default { name: 'C2cCurrency' };
</script>

<script setup lang="ts">
import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import { computed, onMounted, ref } from 'vue';
import { hasPermission } from '@/utils/permission';

import type { DictItem } from '@/api/dict';
import { findDictItemInCache } from '@/api/dict';
import type { CurrencyRow } from '@/api/currency';
import { findAllCurrency } from '@/api/currency';
import {
  addOrUpdateC2cCurrency,
  delC2cCurrency,
  findC2cCurrencyByPage,
  type C2cCurrencyForm,
  type C2cCurrencyRow,
} from '@/api/c2c';
import CollapsibleQueryForm from '@/components/collapsible-query-form/index.vue';
import { QUERY_FORM_LABEL_WIDTH } from '@/constants/form';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import commonUtils from '@/utils/common';

const tableData = ref<C2cCurrencyRow[]>([]);
const currencies = ref<CurrencyRow[]>([]);

const currencyOptions = computed(() =>
  currencies.value.map((c) => ({
    label: c.currencyName ? `${c.currencyCode}(${c.currencyName})` : c.currencyCode,
    value: c.currencyCode,
  })),
);

const loadCurrencies = async () => {
  const list = await findAllCurrency();
  currencies.value = list || [];
};
const loading = ref(false);
const dialogVisible = ref(false);
const queryForm = ref({ currencyCode: '', status: '', pageNum: 1, pageSize: 10 });
const statusDictItems = ref<DictItem[]>([]);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

const columns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'currencyCode', title: '币种' },
  { colKey: 'sortNum', title: '排序号' },
  { colKey: 'status', title: '状态', cell: 'status' },
  { colKey: 'remark', title: '备注' },
  { colKey: 'updateTime', title: '更新时间' },
  { colKey: 'operate', title: '操作', fixed: 'right' },
];

const form = ref<C2cCurrencyForm>({
  currencyCode: '',
  sortNum: 1,
  status: '1',
  remark: '',
});

const statusLabel = (code?: string) =>
  statusDictItems.value.find((item) => item.dictItemCode === code)?.dictItemName || '—';

const loadData = async () => {
  loading.value = true;
  try {
    const page = await findC2cCurrencyByPage(queryForm.value);
    tableData.value = page.content || [];
    pagination.value.total = page.total || 0;
  } finally {
    loading.value = false;
  }
};

const refreshTable = () => {
  pagination.value.current = 1;
  queryForm.value.pageNum = 1;
  loadData();
};

const onPageChange = (pageInfo: { current: number; pageSize: number }) => {
  pagination.value.current = pageInfo.current;
  pagination.value.pageSize = pageInfo.pageSize;
  queryForm.value.pageNum = pageInfo.current;
  queryForm.value.pageSize = pageInfo.pageSize;
  loadData();
};

const resetForm = () => {
  form.value = {
    currencyCode: '',
    sortNum: 1,
    status: '1',
    remark: '',
  };
};

const openCreate = () => {
  resetForm();
  dialogVisible.value = true;
};

const openEdit = (row: C2cCurrencyRow) => {
  form.value = {
    id: row.id,
    currencyCode: row.currencyCode || '',
    sortNum: row.sortNum,
    status: row.status || '1',
    remark: row.remark || '',
  };
  dialogVisible.value = true;
};

const submitForm = async () => {
  if (commonUtils.isBlank(form.value.currencyCode)) {
    MessagePlugin.warning('请选择币种');
    return;
  }
  if (commonUtils.isBlank(form.value.sortNum)) {
    MessagePlugin.warning('请输入排序号');
    return;
  }
  if (commonUtils.isBlank(form.value.status)) {
    MessagePlugin.warning('请选择状态');
    return;
  }
  await addOrUpdateC2cCurrency(form.value);
  MessagePlugin.success('操作成功');
  dialogVisible.value = false;
  await loadData();
};

const handleDelete = (id: string) => {
  const dialog = DialogPlugin.confirm({
    header: '提示',
    body: '确定删除该币种吗？',
    onConfirm: async () => {
      await delC2cCurrency(id);
      MessagePlugin.success('操作成功');
      dialog.destroy();
      await loadData();
    },
    onClose: () => dialog.destroy(),
  });
};

onMounted(async () => {
  statusDictItems.value = await findDictItemInCache('enableStatus');
  await loadCurrencies();
  await loadData();
});
</script>
