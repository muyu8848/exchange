<template>
  <div>
    <t-card :bordered="false">
      <collapsible-query-form
        :data="queryForm"
        :label-width="QUERY_FORM_LABEL_WIDTH"
        :create-label="hasPermission('usingHelp.save') ? '新增帮助' : ''"
        @query="refreshTable"
        @create="openAddDialog"
      >
        <t-col :span="3">
          <t-form-item label="帮助标题"><t-input v-model="queryForm.title" clearable /></t-form-item>
        </t-col>
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
        <template #title="{ row }">
          <div class="table-stack">{{ row.title }}</div>
        </template>
        <template #action="{ row }">
          <t-space size="small">
            <t-button v-if="hasPermission('usingHelp.save')" size="small" theme="primary" @click="openEditDialog(row.id)">编辑</t-button>
            <t-button size="small" variant="outline" @click="openViewDialog(row)">查看内容</t-button>
            <t-button v-if="hasPermission('usingHelp.del')" size="small" theme="danger" variant="outline" @click="handleDelete(row.id)">删除</t-button>
          </t-space>
        </template>
      </t-table>
    </t-card>

    <t-dialog
      v-model:visible="formDialogVisible"
      :header="formMode === 'add' ? '新增' : '编辑'"
      width="920px"
      :close-on-overlay-click="false"
      :cancel-btn="null"
      confirm-btn="确定"
      @confirm="submitForm"
      placement="center"
    >
      <t-tabs v-model="activeLang">
        <t-tab-panel value="zh-CN" label="简体中文（默认）">
          <t-form v-if="activeLang === 'zh-CN'" label-align="top">
            <t-form-item label="标题">
              <t-textarea v-model="formData.title" />
            </t-form-item>
            <t-form-item label="内容">
              <rich-text-editor v-model="formData.content" :height="420" />
            </t-form-item>
          </t-form>
        </t-tab-panel>
        <t-tab-panel
          v-for="lang in otherLangs"
          :key="lang.langCode"
          :value="lang.langCode"
          :label="tabLabel(lang)"
        >
          <t-form
            v-if="activeLang === lang.langCode && i18nForm[lang.langCode]"
            label-align="top"
          >
            <t-form-item label="标题（留空表示未翻译，客户端显示简体中文）">
              <t-textarea v-model="i18nForm[lang.langCode].title" />
            </t-form-item>
            <t-form-item label="内容（可空）">
              <rich-text-editor v-model="i18nForm[lang.langCode].content" :height="420" />
            </t-form-item>
          </t-form>
        </t-tab-panel>
      </t-tabs>
    </t-dialog>

    <t-dialog v-model:visible="viewDialogVisible" :header="false" :footer="false" width="720px">
      <div class="view-title">{{ viewRecord?.title }}</div>
      <div class="view-content" v-html="viewRecord?.content" />
    </t-dialog>
  </div>
</template>

<script lang="ts">
export default {
  name: 'UsingHelp',
};
</script>

<script setup lang="ts">
import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { onMounted, ref } from 'vue';
import { hasPermission } from '@/utils/permission';

import type { MultiLangRow } from '@/api/multiLang';
import { findAllMultiLang } from '@/api/multiLang';
import type { UsingHelpForm, UsingHelpRow } from '@/api/usingHelp';
import { addOrUpdateUsingHelp, delUsingHelp, findUsingHelpById, findUsingHelpByPage } from '@/api/usingHelp';
import CollapsibleQueryForm from '@/components/collapsible-query-form/index.vue';
import RichTextEditor from '@/components/rich-text-editor/index.vue';
import { QUERY_FORM_LABEL_WIDTH } from '@/constants/form';

const DEFAULT_LANG = 'zh-CN';

const queryForm = ref({ title: '' });
const tableData = ref<UsingHelpRow[]>([]);
const loading = ref(false);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

const formDialogVisible = ref(false);
const formMode = ref<'add' | 'edit'>('add');
const formData = ref<UsingHelpForm>({ title: '', content: '' });

/** 当前激活的语言 Tab */
const activeLang = ref(DEFAULT_LANG);
/** 除默认语言外的启用语言 */
const otherLangs = ref<MultiLangRow[]>([]);
/** 各语言译文表单：key = langCode */
const i18nForm = ref<Record<string, { title: string; content: string }>>({});

const viewDialogVisible = ref(false);
const viewRecord = ref<UsingHelpRow | null>(null);

const columns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'title', title: '标题' },
  { colKey: 'latelyUpdateTime', title: '编辑时间' },
  { colKey: 'action', title: '操作' },
];

const tabLabel = (lang: MultiLangRow) => {
  const filled = (i18nForm.value[lang.langCode]?.title || '').trim();
  return filled ? lang.langName : `${lang.langName}（未翻译）`;
};

const loadLangs = async () => {
  if (otherLangs.value.length) {
    return;
  }
  const list = await findAllMultiLang({ status: '1' });
  otherLangs.value = (list || []).filter((item) => item.langCode !== DEFAULT_LANG);
};

const resetI18nForm = () => {
  const next: Record<string, { title: string; content: string }> = {};
  otherLangs.value.forEach((lang) => {
    next[lang.langCode] = { title: '', content: '' };
  });
  i18nForm.value = next;
};

const loadTableData = async () => {
  loading.value = true;
  try {
    const pageResult = await findUsingHelpByPage({
      ...queryForm.value,
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize,
    });
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

const openAddDialog = async () => {
  await loadLangs();
  formMode.value = 'add';
  formData.value = { title: '', content: '' };
  resetI18nForm();
  activeLang.value = DEFAULT_LANG;
  formDialogVisible.value = true;
};

const openEditDialog = async (id: string) => {
  await loadLangs();
  const record = await findUsingHelpById(id);
  formMode.value = 'edit';
  formData.value = { id: record.id, title: record.title, content: record.content || '' };
  resetI18nForm();
  (record.translations || []).forEach((item) => {
    if (i18nForm.value[item.langCode]) {
      i18nForm.value[item.langCode] = { title: item.title, content: item.content || '' };
    }
  });
  activeLang.value = DEFAULT_LANG;
  formDialogVisible.value = true;
};

const submitForm = async () => {
  if (!formData.value.title?.trim()) {
    MessagePlugin.warning('请输入标题');
    return;
  }
  if (!formData.value.content?.trim()) {
    MessagePlugin.warning('请输入内容');
    return;
  }
  // 其他语言译文：标题与正文要么都填、要么都不填
  const partial = otherLangs.value.find((lang) => {
    const item = i18nForm.value[lang.langCode];
    if (!item) {
      return false;
    }
    return !!(item.title || '').trim() !== !!(item.content || '').trim();
  });
  if (partial) {
    const hasTitle = !!(i18nForm.value[partial.langCode].title || '').trim();
    MessagePlugin.warning(
      `「${partial.langName}」已填写${hasTitle ? '标题' : '正文'}，请补全${hasTitle ? '正文' : '标题'}`,
    );
    activeLang.value = partial.langCode;
    return;
  }
  const translations = otherLangs.value
    .map((lang) => ({
      langCode: lang.langCode,
      title: (i18nForm.value[lang.langCode]?.title || '').trim(),
      content: i18nForm.value[lang.langCode]?.content || '',
    }))
    .filter((item) => item.title);

  await addOrUpdateUsingHelp({ ...formData.value, translations });
  MessagePlugin.success('操作成功');
  formDialogVisible.value = false;
  refreshTable();
};

const openViewDialog = (row: UsingHelpRow) => {
  viewRecord.value = row;
  viewDialogVisible.value = true;
};

const handleDelete = (id: string) => {
  const dialog = DialogPlugin.confirm({
    header: '提示',
    body: '确定要删除吗',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async () => {
      await delUsingHelp(id);
      MessagePlugin.success('操作成功');
      dialog.destroy();
      refreshTable();
    },
    onClose: () => dialog.destroy(),
  });
};

onMounted(loadTableData);
</script>

<style lang="less" scoped>
.table-stack {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  white-space: normal;
}

.view-title {
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
}
</style>
