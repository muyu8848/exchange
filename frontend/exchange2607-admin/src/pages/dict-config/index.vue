<template>
  <div>
    <t-card :bordered="false">
      <collapsible-query-form
        :data="queryFormData"
        :label-width="QUERY_FORM_LABEL_WIDTH"
        :create-label="hasPermission('dictConfig.save') ? '新增字典' : ''"
        @query="refreshTable"
        @create="shoAddDialog"
      >
        <t-col :span="3">
          <t-form-item label="字典code">
            <t-input v-model="queryFormData.dictTypeCode"></t-input>
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="字典名称">
            <t-input v-model="queryFormData.dictTypeName"></t-input>
          </t-form-item>
        </t-col>
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
        <template #operate="{ row }">
          <t-space :size="4">
            <t-button v-if="hasPermission('dictConfig.save')" size="small" theme="primary" variant="text" @click="showUpdateDialog(row)">修改</t-button>
            <t-dropdown v-if="hasPermission(['dictConfig.save', 'dictConfig.del'])" trigger="click" :max-column-width="150">
              <t-button size="small" variant="text">
                更多
                <template #suffix><t-icon name="chevron-down" /></template>
              </t-button>
              <t-dropdown-menu>
                <t-dropdown-item v-if="hasPermission('dictConfig.save')" @click="showEditDictDataDialog(row)">维护字典数据</t-dropdown-item>
                <t-dropdown-item v-if="hasPermission('dictConfig.del')" @click="onDel(row)">删除</t-dropdown-item>
              </t-dropdown-menu>
            </t-dropdown>
          </t-space>
        </template>
      </t-table>
    </t-card>
    <t-dialog
      header="维护字典数据"
      width="600px"
      :close-on-overlay-click="false"
      :visible="showEditDictDataDialogFlag"
      @close="showEditDictDataDialogFlag = false"
    >
      <template #body>
        <div class="t-table t-table--hoverable" style="max-height: 400px">
          <div class="t-table__content">
            <table class="t-table--layout-fixed">
              <thead class="t-table__header">
                <tr>
                  <th></th>
                  <th style="width: 140px">字典项code</th>
                  <th style="width: 200px">字典项名称</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody class="t-table__body">
                <tr v-for="(dictItem, index) in dictItems" :key="index">
                  <td>
                    <t-button
                      theme="primary"
                      size="small"
                      shape="circle"
                      :disabled="index == 0"
                      @click="moveUpDictItem(index)"
                    >
                      <t-icon name="arrow-up"></t-icon>
                    </t-button>
                    <t-button
                      theme="primary"
                      size="small"
                      shape="circle"
                      :disabled="index == dictItems.length - 1"
                      @click="moveDownDictItem(index)"
                    >
                      <t-icon name="arrow-down"></t-icon>
                    </t-button>
                  </td>
                  <td>
                    <t-input v-model="dictItem.dictItemCode"></t-input>
                  </td>
                  <td>
                    <t-input v-model="dictItem.dictItemName"></t-input>
                  </td>
                  <td>
                    <t-button theme="primary" @click="delDictItem(index)">删除</t-button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </template>
      <template #footer>
        <t-button theme="default" @click="addDictItem">添加</t-button>
        <t-button theme="primary" @click="onUpdateDictData">保存</t-button>
      </template>
    </t-dialog>
    <t-dialog
      :header="addOrUpdateAction == 'add' ? '新增字典' : '修改字典'"
      :close-on-overlay-click="false"
      :visible="showAddOrUpdateDialogFlag"
      @close="showAddOrUpdateDialogFlag = false"
      @confirm="addOrUpdate"
    >
      <template #body>
        <t-form ref="addOrUpdateForm" :data="addOrUpdateFormData" :rules="addOrUpdateFormRules">
          <t-form-item label="字典code" name="dictTypeCode">
            <t-input v-model="addOrUpdateFormData.dictTypeCode"></t-input>
          </t-form-item>
          <t-form-item label="字典名称" name="dictTypeName">
            <t-input v-model="addOrUpdateFormData.dictTypeName"></t-input>
          </t-form-item>
          <t-form-item label="备注" name="note">
            <t-input v-model="addOrUpdateFormData.note"></t-input>
          </t-form-item>
        </t-form>
      </template>
    </t-dialog>
  </div>
</template>

<script lang="ts">
export default {
  name: 'DictConfig',
};
</script>
<script setup lang="ts">
import {
  DialogPlugin,
  type FormInstanceFunctions,
  type FormRule,
  MessagePlugin,
  type PrimaryTableCol,
  type TableRowData,
} from 'tdesign-vue-next';
import { onMounted, ref } from 'vue';
import { hasPermission } from '@/utils/permission';

import type { DictItem } from '@/api/dict';
import {
  addOrUpdateDictType,
  delDictType,
  findDictItemByDictTypeId,
  findDictTypeById,
  findDictTypeByPage,
  updateDictData,
} from '@/api/dict';
import CollapsibleQueryForm from '@/components/collapsible-query-form/index.vue';
import { QUERY_FORM_LABEL_WIDTH } from '@/constants/form';

const columns: PrimaryTableCol<TableRowData>[] = [
  {
    colKey: 'dictTypeCode',
    title: '字典code',
  },
  {
    colKey: 'dictTypeName',
    title: '字典名称',
  },
  {
    colKey: 'note',
    title: '备注',
  },
  {
    colKey: 'lastUpdateTime',
    title: '最后修改时间',
  },
  {
    colKey: 'operate',
    title: '操作',
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
  pageSize: pagination.value.pageSize,
  pageNum: pagination.value.current,
  dictTypeCode: '',
  dictTypeName: '',
});

const selectedRecordId = ref('');

const showAddOrUpdateDialogFlag = ref(false);
const addOrUpdateAction = ref('');
const addOrUpdateForm = ref<FormInstanceFunctions>();
const addOrUpdateFormData = ref({ dictTypeCode: '', dictTypeName: '', note: '' });
const addOrUpdateFormRules: Record<string, FormRule[]> = {
  dictTypeCode: [{ required: true, message: '请输入字典code', type: 'error', trigger: 'submit' }],
  dictTypeName: [{ required: true, message: '请输入字典名称', type: 'error', trigger: 'submit' }],
};

const showEditDictDataDialogFlag = ref(false);
const dictItems = ref<DictItem[]>([]);

onMounted(() => {
  loadTableData();
});

const onUpdateDictData = async () => {
  for (let i = 0; i < dictItems.value.length; i++) {
    const dictItem = dictItems.value[i];
    if (dictItem.dictItemCode === null || dictItem.dictItemCode === '') {
      MessagePlugin.warning('请输入字典项code');
      return;
    }
    if (dictItem.dictItemName === null || dictItem.dictItemName === '') {
      MessagePlugin.warning('请输入字典项名称');
      return;
    }
  }
  await updateDictData({
    dictTypeId: selectedRecordId.value,
    dictDatas: dictItems.value,
  });
  showEditDictDataDialogFlag.value = false;
  MessagePlugin.success('操作成功');
  refreshTable();
};

const addDictItem = () => {
  dictItems.value.push({
    dictItemCode: '',
    dictItemName: '',
  });
};

const moveUpDictItem = (index: number) => {
  const temp = dictItems.value[index - 1];
  dictItems.value[index - 1] = dictItems.value[index];
  dictItems.value[index] = temp;
};

const moveDownDictItem = (index: number) => {
  const temp = dictItems.value[index + 1];
  dictItems.value[index + 1] = dictItems.value[index];
  dictItems.value[index] = temp;
};

const delDictItem = (index: number) => {
  dictItems.value.splice(index, 1);
};

const showEditDictDataDialog = async (row: TableRowData) => {
  selectedRecordId.value = row.id;
  dictItems.value = await findDictItemByDictTypeId(row.id);
  showEditDictDataDialogFlag.value = true;
};

const showUpdateDialog = async (row: TableRowData) => {
  addOrUpdateAction.value = 'update';
  addOrUpdateFormData.value = await findDictTypeById(row.id);
  showAddOrUpdateDialogFlag.value = true;
};

const addOrUpdate = async () => {
  addOrUpdateForm.value?.submit();
  const error = await addOrUpdateForm.value?.validateOnly();
  if (error !== true) {
    return;
  }
  await addOrUpdateDictType(addOrUpdateFormData.value);
  showAddOrUpdateDialogFlag.value = false;
  MessagePlugin.success('操作成功');
  refreshTable();
};

const shoAddDialog = () => {
  addOrUpdateAction.value = 'add';
  addOrUpdateFormData.value = { dictTypeCode: '', dictTypeName: '', note: '' };
  showAddOrUpdateDialogFlag.value = true;
};

const onDel = (row: TableRowData) => {
  const dialogInstance = DialogPlugin.confirm({
    header: '提示',
    body: '确认要删除吗',
    onConfirm: async () => {
      await delDictType(row.id);
      dialogInstance.destroy();
      MessagePlugin.success('操作成功');
      refreshTable();
    },
  });
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
  queryFormData.value.pageSize = pagination.value.pageSize;
  queryFormData.value.pageNum = pagination.value.current;
  const result = await findDictTypeByPage(queryFormData.value);
  data.value = result.content;
  pagination.value.total = result.total;
  loading.value = false;
};
</script>

<style lang="less" scoped></style>
