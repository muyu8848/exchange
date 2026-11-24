<template>
  <t-card :bordered="false">
    <collapsible-query-form
      :data="queryForm"
      :label-width="QUERY_FORM_LABEL_WIDTH"
      :create-label="hasPermission('chainNetwork.save') ? '新增区块链网络' : ''"
      @query="refreshTable"
      @create="openCreate"
    >
      <template #default>
        <t-col :span="3">
          <t-form-item label="链类型">
            <t-input v-model="queryForm.chainType" clearable placeholder="如 TRC20" />
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="状态">
            <t-select v-model="queryForm.status" clearable :options="statusDictItems" :keys="{ label: 'dictItemName', value: 'dictItemCode' }" />
          </t-form-item>
        </t-col>
      </template>
    </collapsible-query-form>

    <t-table
      row-key="id"
      :data="tableData"
      :columns="columns"
      :loading="loading"
      :hover="true"
      table-layout="auto"
      :pagination="pagination"
      @page-change="onPageChange"
    >
      <template #operate="{ row }">
        <t-space>
          <t-button v-if="hasPermission('chainNetwork.save')" size="small" variant="text" theme="primary" @click="openEdit(row.id)">编辑</t-button>
          <t-button v-if="hasPermission('chainNetwork.del')" size="small" variant="text" theme="danger" @click="handleDelete(row.id)">删除</t-button>
        </t-space>
      </template>
    </t-table>

    <t-dialog
      v-model:visible="dialogVisible"
      :header="form.id ? '编辑区块链网络' : '新增区块链网络'"
      width="560px"
      @confirm="submitForm"
    >
      <t-form :label-width="100">
        <t-form-item label="链类型">
          <t-input v-model="form.chainType" :disabled="!!form.id" placeholder="如 TRC20" />
        </t-form-item>
        <t-form-item label="查询地址">
          <t-input v-model="form.addressExplorerUrlTemplate" placeholder="如 https://tronscan.org/#/address/{address}" />
        </t-form-item>
        <t-form-item label="排序号">
          <t-input-number v-model="form.sortNum" style="width: 100%;" theme="normal" />
        </t-form-item>
        <t-form-item label="状态">
          <t-select v-model="form.status" :options="statusDictItems" :keys="{ label: 'dictItemName', value: 'dictItemCode' }" />
        </t-form-item>
      </t-form>
    </t-dialog>
  </t-card>
</template>

<script lang="ts">
export default {
  name: 'ChainNetwork',
}
</script>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next'
import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next'
import { hasPermission } from '@/utils/permission'
import { QUERY_FORM_LABEL_WIDTH } from '@/constants/form'
import CollapsibleQueryForm from '@/components/collapsible-query-form/index.vue'
import commonUtils from '@/utils/common'
import { findDictItemInCache } from '@/api/dict'
import type { DictItem } from '@/api/dict'
import {
  addOrUpdateChainNetwork,
  delChainNetwork,
  findChainNetworkById,
  findChainNetworkByPage,
  type ChainNetworkForm,
  type ChainNetworkQueryParam,
  type ChainNetworkRow,
} from '@/api/chainNetwork'

const tableData = ref<ChainNetworkRow[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const pagination = ref({ current: 1, pageSize: 10, total: 0 })
const queryForm = ref<ChainNetworkQueryParam>({
  chainType: '',
  status: '',
  pageNum: 1,
  pageSize: 10,
})
const statusDictItems = ref<DictItem[]>([])

const columns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'chainType', title: '链类型'},
  { colKey: 'addressExplorerUrlTemplate', title: '查询地址' },
  { colKey: 'sortNum', title: '排序号' },
  { colKey: 'statusName', title: '状态' },
  { colKey: 'operate', title: '操作', fixed: 'right' },
]

const form = ref<ChainNetworkForm>({
  chainType: '',
  addressExplorerUrlTemplate: '',
  status: '1',
  sortNum: 0,
})

const loadData = async () => {
  loading.value = true
  try {
    queryForm.value.pageNum = pagination.value.current
    queryForm.value.pageSize = pagination.value.pageSize
    const page = await findChainNetworkByPage(queryForm.value)
    tableData.value = page.content || []
    pagination.value.total = page.total || 0
  } finally {
    loading.value = false
  }
}

const refreshTable = () => {
  pagination.value.current = 1
  loadData()
}

const onPageChange = (pageInfo: { current: number; pageSize: number }) => {
  pagination.value.current = pageInfo.current
  pagination.value.pageSize = pageInfo.pageSize
  loadData()
}

const resetForm = () => {
  form.value = {
    chainType: '',
    addressExplorerUrlTemplate: '',
    status: '1',
    sortNum: 1,
  }
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = async (id: string) => {
  const detail = await findChainNetworkById(id)
  form.value = {
    id: detail.id,
    chainType: detail.chainType,
    addressExplorerUrlTemplate: detail.addressExplorerUrlTemplate,
    status: detail.status,
    sortNum: detail.sortNum,
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (commonUtils.isBlank(form.value.chainType)) {
    MessagePlugin.warning('请输入链类型')
    return
  }
  if (commonUtils.isBlank(form.value.addressExplorerUrlTemplate)) {
    MessagePlugin.warning('请输入查询地址')
    return
  }
  if (commonUtils.isBlank(form.value.sortNum)) {
    MessagePlugin.warning('请输入排序号')
    return
  }
  if (commonUtils.isBlank(form.value.status)) {
    MessagePlugin.warning('请选择状态')
    return
  }
  await addOrUpdateChainNetwork(form.value)
  MessagePlugin.success('操作成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = (id: string) => {
  const confirmDialog = DialogPlugin.confirm({
    header: '提示',
    body: '确认删除该区块链网络？',
    onConfirm: async () => {
      await delChainNetwork(id)
      MessagePlugin.success('操作成功')
      confirmDialog.destroy()
      loadData()
    },
  })
}

onMounted(async () => {
  statusDictItems.value = await findDictItemInCache('enableStatus')
  loadData()
})
</script>
