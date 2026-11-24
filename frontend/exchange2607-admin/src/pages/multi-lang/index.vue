<template>
  <div>
    <t-card :bordered="false">
      <collapsible-query-form
        :data="queryForm"
        :label-width="QUERY_FORM_LABEL_WIDTH"
        :create-label="hasPermission('multiLang.save') ? '新增语言' : ''"
        @query="refreshTable"
        @create="openAdd"
      >
        <t-col :span="3">
          <t-form-item label="语言编码">
            <t-input v-model="queryForm.langCode" clearable placeholder="如 zh-TW" />
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="语言名称">
            <t-input v-model="queryForm.langName" clearable placeholder="如 繁体中文" />
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="状态">
            <t-select
              v-model="queryForm.status"
              clearable
              :options="statusDictItems"
              :keys="{ label: 'dictItemName', value: 'dictItemCode' }"
            />
          </t-form-item>
        </t-col>
      </collapsible-query-form>

      <t-table
        row-key="id"
        :data="tableData"
        :columns="columns"
        :loading="loading"
        table-layout="auto"
        :hover="true"
        :pagination="pagination"
        @page-change="onPageChange"
      >
        <template #nativeName="{ row }">
          <span v-if="row.nativeName">{{ row.nativeName }}</span>
          <span v-else class="table-muted">—</span>
        </template>
        <template #status="{ row }">
          <t-tag :theme="row.status === '1' ? 'success' : 'default'">
            {{ getStatusName(row.status) }}
          </t-tag>
        </template>
        <template #operate="{ row }">
          <t-space>
            <t-button v-if="hasPermission('multiLang.save')" size="small" variant="text" theme="primary" @click="openEdit(row)">编辑</t-button>
            <t-button
              v-if="row.langCode !== DEFAULT_LANG && hasPermission('multiLang.del')"
              size="small"
              variant="text"
              theme="danger"
              @click="handleDelete(row)"
            >
              删除
            </t-button>
          </t-space>
        </template>
      </t-table>
    </t-card>

    <t-dialog
      v-model:visible="dialogVisible"
      :header="form.id ? '编辑语言' : '新增语言'"
      placement="center"
      @confirm="submit"
    >
      <t-alert theme="info" style="margin-bottom: 16px">
        <template #message>
          语言编码需与文案文件名后缀一致（如 en-US 对应 messages_en_US.properties）。新增语言后须提供对应文案文件，否则该语言的界面文案将回落默认语言。
        </template>
      </t-alert>
      <t-form label-align="left" :label-width="100">
        <t-form-item label="语言编码">
          <t-input v-model="form.langCode" :disabled="!!form.id" placeholder="如 zh-TW" />
        </t-form-item>
        <t-form-item label="语言名称">
          <t-input v-model="form.langName" placeholder="后台展示用，如 繁体中文" />
        </t-form-item>
        <t-form-item label="母语名称">
          <t-input v-model="form.nativeName" placeholder="客户端展示用，如 繁體中文" />
        </t-form-item>
        <t-form-item label="状态">
          <t-select
            v-model="form.status"
            :options="statusDictItems"
            :keys="{ label: 'dictItemName', value: 'dictItemCode' }"
          />
        </t-form-item>
        <t-form-item label="排序">
          <t-input-number v-model="form.sortNum" theme="normal" :min="0" :decimal-places="0" style="width: 100%" />
        </t-form-item>
      </t-form>
    </t-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { hasPermission } from '@/utils/permission';
import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import CollapsibleQueryForm from '@/components/collapsible-query-form/index.vue';
import { QUERY_FORM_LABEL_WIDTH } from '@/constants/form';
import { request } from '@/utils/request';
import { findDictItemInCache } from '@/api/dict';
import type { DictItem } from '@/api/dict';

interface Row {
  id?: string;
  langCode: string;
  langName: string;
  nativeName?: string;
  status: string;
  sortNum?: number;
}

interface ListResult {
  content: Row[];
  total: number;
  pageNum: number;
  pageSize: number;
}

/** 默认语言：不允许删除（后端同样有兜底校验） */
const DEFAULT_LANG = 'zh-CN';

const tableData = ref<Row[]>([]);
const statusDictItems = ref<DictItem[]>([]);
const loading = ref(false);
const dialogVisible = ref(false);
const form = ref<Row>({ langCode: '', langName: '', nativeName: '', status: '1', sortNum: 0 });
const queryForm = ref({ langCode: '', langName: '', status: '' });
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

const columns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'langCode', title: '语言编码', width: 140 },
  { colKey: 'langName', title: '语言名称', width: 160 },
  { colKey: 'nativeName', title: '母语名称', width: 180 },
  { colKey: 'sortNum', title: '排序', width: 100 },
  { colKey: 'status', title: '状态', width: 120 },
  { colKey: 'operate', title: '操作', width: 160 },
];

const getStatusName = (code?: string) =>
  statusDictItems.value.find((item) => item.dictItemCode === code)?.dictItemName ?? code ?? '—';

const getQueryParams = () => {
  const params: Record<string, unknown> = {
    pageNum: pagination.value.current,
    pageSize: pagination.value.pageSize,
  };
  if (queryForm.value.langCode) {
    params.langCode = queryForm.value.langCode;
  }
  if (queryForm.value.langName) {
    params.langName = queryForm.value.langName;
  }
  if (queryForm.value.status) {
    params.status = queryForm.value.status;
  }
  return params;
};

const loadData = async () => {
  loading.value = true;
  try {
    const page = await request.post<ListResult>({ url: '/multiLang/findByPage', data: getQueryParams() });
    tableData.value = page.content || [];
    pagination.value.total = page.total || 0;
  } finally {
    loading.value = false;
  }
};

const refreshTable = () => {
  pagination.value.current = 1;
  loadData();
};

const onPageChange = (pageInfo: { current: number; pageSize: number }) => {
  pagination.value.current = pageInfo.current;
  pagination.value.pageSize = pageInfo.pageSize;
  loadData();
};

const openAdd = () => {
  form.value = { langCode: '', langName: '', nativeName: '', status: '1', sortNum: 0 };
  dialogVisible.value = true;
};

const openEdit = (row: Row) => {
  form.value = { ...row };
  dialogVisible.value = true;
};

const submit = async () => {
  if (!form.value.langCode || !form.value.langName || !form.value.nativeName) {
    MessagePlugin.warning('请填写语言编码、语言名称与母语名称');
    return;
  }
  if (!form.value.status) {
    MessagePlugin.warning('请选择状态');
    return;
  }
  if (form.value.sortNum === undefined || form.value.sortNum === null) {
    MessagePlugin.warning('请填写排序号');
    return;
  }
  const tips = await request.post<string | null>({ url: '/multiLang/addOrUpdate', data: form.value });
  if (tips) {
    MessagePlugin.warning(tips);
  } else {
    MessagePlugin.success('保存成功');
  }
  dialogVisible.value = false;
  await loadData();
};

const handleDelete = (row: Row) => {
  const dialog = DialogPlugin.confirm({
    header: '删除语言',
    body: `确定删除「${row.langName}（${row.langCode}）」吗？删除后客户端将不再下发该语言。`,
    onConfirm: async () => {
      await request.post({ url: '/multiLang/del', data: { id: row.id } });
      MessagePlugin.success('删除成功');
      dialog.destroy();
      await loadData();
    },
  });
};

onMounted(async () => {
  statusDictItems.value = await findDictItemInCache('enableStatus');
  await loadData();
});
</script>

<style lang="less" scoped>
.table-muted {
  color: var(--td-text-color-placeholder);
}
</style>
