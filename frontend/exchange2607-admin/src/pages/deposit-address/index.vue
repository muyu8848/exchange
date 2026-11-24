<template>
  <t-card :bordered="false">
    <collapsible-query-form
      :data="queryForm"
      :label-width="QUERY_FORM_LABEL_WIDTH"
      :create-label="hasPermission('depositAddress.save') ? '新增地址' : ''"
      @query="refreshTable"
      @create="openCreate"
    >
      <template #default>
        <t-col :span="3">
          <t-form-item label="链类型">
            <t-select v-model="queryForm.chainType" clearable :options="chainTypeOptions" />
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="地址">
            <t-input v-model="queryForm.address" clearable />
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="状态">
            <t-select v-model="queryForm.status" clearable :options="addressStatusDictItems" :keys="{ label: 'dictItemName', value: 'dictItemCode' }" />
          </t-form-item>
        </t-col>
      </template>
      <template #dropdownItems>
        <t-dropdown-item v-if="hasPermission('depositAddress.import')" @click="importVisible = true">导入地址</t-dropdown-item>
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
          <t-button v-if="row.status === '1' && hasPermission('depositAddress.del')" size="small" variant="text" theme="danger" @click="handleDelete(row.id)">删除</t-button>
          <t-button v-if="row.status === '2' && hasPermission('depositAddress.release')" size="small" variant="text" theme="primary" @click="handleRelease(row.id)">释放</t-button>
          <t-button v-if="(row.status === '1' || row.status === '2') && hasPermission('depositAddress.disable')" size="small" variant="text" theme="warning" @click="handleDisable(row.id)">停用</t-button>
          <t-button v-if="row.status === '3' && hasPermission('depositAddress.enable')" size="small" variant="text" theme="primary" @click="handleEnable(row.id)">启用</t-button>
        </t-space>
      </template>
      <template #status="{ row }">
        <t-tag size="small" variant="light" :theme="addressStatusTheme(row.status)">{{ row.statusName }}</t-tag>
      </template>
      <template #address="{ row }">
        <div v-if="row.address" class="table-line">
          <t-link
            v-if="row.addressExplorerUrl"
            class="table-value table-mono table-break address-link"
            theme="primary"
            :href="row.addressExplorerUrl"
            target="_blank"
          >
            {{ row.address }}
          </t-link>
          <span v-else class="table-value table-mono table-break">{{ row.address }}</span>
          <copy-icon-button :text="row.address" />
        </div>
        <span v-else class="table-muted">—</span>
      </template>
    </t-table>

    <t-dialog v-model:visible="createVisible" header="新增地址" width="560px" @confirm="submitCreate">
      <t-form :label-width="100">
        <t-form-item label="链类型">
          <t-select v-model="form.chainType" :options="chainTypeOptions" />
        </t-form-item>
        <t-form-item label="地址">
          <t-input v-model="form.address" />
        </t-form-item>
        <t-form-item label="状态">
          <t-select v-model="form.status" :options="addressStatusDictItems" :keys="{ label: 'dictItemName', value: 'dictItemCode' }" />
        </t-form-item>
      </t-form>
    </t-dialog>

    <t-dialog v-model:visible="importVisible" header="批量导入充币地址" width="640px" @confirm="submitImport">
      <t-form :label-width="100">
        <t-form-item label="链类型">
          <t-select v-model="importForm.chainType" :options="chainTypeOptions" />
        </t-form-item>
        <t-form-item label="地址列表">
          <t-textarea v-model="importForm.addresses" :autosize="{ minRows: 6, maxRows: 12 }" placeholder="每行一个地址" />
        </t-form-item>
      </t-form>
    </t-dialog>
  </t-card>
</template>

<script lang="ts">
export default {
  name: 'DepositAddress',
}
</script>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next'
import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next'
import { hasPermission } from '@/utils/permission'
import { QUERY_FORM_LABEL_WIDTH } from '@/constants/form'
import CollapsibleQueryForm from '@/components/collapsible-query-form/index.vue'
import CopyIconButton from '@/components/copy-icon-button/index.vue'
import { findDictItemInCache } from '@/api/dict'
import type { DictItem } from '@/api/dict'
import { findAllChainNetworks, type ChainNetworkRow } from '@/api/chainNetwork'
import {
  addDepositAddress,
  batchImportDepositAddress,
  delDepositAddress,
  disableDepositAddress,
  enableDepositAddress,
  findDepositAddressByPage,
  releaseDepositAddress,
  type AddDepositAddressForm,
  type BatchImportDepositAddressForm,
  type DepositAddressQueryParam,
  type DepositAddressRow,
} from '@/api/depositAddress'

const tableData = ref<DepositAddressRow[]>([])
const loading = ref(false)
const queryForm = ref<DepositAddressQueryParam>({
  chainType: '',
  address: '',
  status: '',
})
const createVisible = ref(false)
const importVisible = ref(false)
const addressStatusDictItems = ref<DictItem[]>([])
const chainTypeOptions = ref<{ label: string; value: string }[]>([])
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
})

// 充币地址状态：1=空闲，2=占用中，3=停用
const addressStatusTheme = (status: string): 'success' | 'primary' | 'warning' | 'danger' | 'default' => {
  if (status === '1') return 'success'
  if (status === '2') return 'primary'
  if (status === '3') return 'default'
  return 'default'
}

const columns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'chainType', title: '链类型' },
  { colKey: 'address', title: '地址' },
  { colKey: 'status', title: '状态'},
  { colKey: 'totalReceived', title: '累计收款' },
  { colKey: 'createTime', title: '创建时间' },
  { colKey: 'lastUseTime', title: '最后使用时间' },
  { colKey: 'operate', title: '操作', fixed: 'right' },
]

const form = ref<AddDepositAddressForm>({
  chainType: '',
  address: '',
  status: '1',
})

const importForm = ref<BatchImportDepositAddressForm>({
  chainType: '',
  addresses: '',
})

const loadData = async () => {
  loading.value = true
  try {
    const page = await findDepositAddressByPage(getQueryParams())
    tableData.value = page.content
    pagination.value.total = page.total
  } finally {
    loading.value = false
  }
}

const getQueryParams = () => ({
  ...queryForm.value,
  pageNum: pagination.value.current,
  pageSize: pagination.value.pageSize,
})

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
    address: '',
    status: '1',
  }
}

const openCreate = () => {
  resetForm()
  createVisible.value = true
}

const submitCreate = async () => {
  if (!form.value.chainType) {
    MessagePlugin.warning('请选择所属充币链')
    return
  }
  if (!form.value.address) {
    MessagePlugin.warning('请输入地址')
    return
  }
  await addDepositAddress(form.value)
  MessagePlugin.success('操作成功')
  createVisible.value = false
  loadData()
}

const submitImport = async () => {
  if (!importForm.value.chainType) {
    MessagePlugin.warning('请选择所属充币链')
    return
  }
  if (!importForm.value.addresses) {
    MessagePlugin.warning('请输入地址列表')
    return
  }
  const result = await batchImportDepositAddress(importForm.value)
  MessagePlugin.success(`导入完成，成功 ${result.successCount} 条` + (result.skipExistsCount || result.skipDuplicateCount ? `，跳过 ${result.skipExistsCount + result.skipDuplicateCount} 条` : ''))
  importVisible.value = false
  loadData()
}

const handleDelete = (id: string) => {
  const confirmDialog = DialogPlugin.confirm({
    header: '提示',
    body: '确认删除该地址？',
    onConfirm: async () => {
      await delDepositAddress(id)
      MessagePlugin.success('操作成功')
      confirmDialog.destroy()
      loadData()
    },
  })
}

const handleRelease = (id: string) => {
  const confirmDialog = DialogPlugin.confirm({
    header: '提示',
    body: '确认释放该地址？释放后状态变为空闲',
    onConfirm: async () => {
      await releaseDepositAddress(id)
      MessagePlugin.success('操作成功')
      confirmDialog.destroy()
      loadData()
    },
  })
}

const handleDisable = (id: string) => {
  const confirmDialog = DialogPlugin.confirm({
    header: '提示',
    body: '确认停用该地址？',
    onConfirm: async () => {
      await disableDepositAddress(id)
      MessagePlugin.success('操作成功')
      confirmDialog.destroy()
      loadData()
    },
  })
}

const handleEnable = (id: string) => {
  const confirmDialog = DialogPlugin.confirm({
    header: '提示',
    body: '确认启用该地址？',
    onConfirm: async () => {
      await enableDepositAddress(id)
      MessagePlugin.success('操作成功')
      confirmDialog.destroy()
      loadData()
    },
  })
}

onMounted(async () => {
  addressStatusDictItems.value = await findDictItemInCache('depositAddressStatus')
  const networks = await findAllChainNetworks()
  chainTypeOptions.value = networks.map((n: ChainNetworkRow) => ({
    label: n.chainType,
    value: n.chainType,
  }))
  loadData()
})
</script>

<style scoped>
.table-line {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  min-width: 0;
}

.address-link {
  flex: 1;
  min-width: 0;
  word-break: break-all;
  white-space: normal;
}

.table-value {
  flex: 1;
  min-width: 0;
  color: var(--td-text-color-primary);
}

.table-mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
}

.table-break {
  word-break: break-all;
  white-space: normal;
}

.table-muted {
  color: var(--td-text-color-placeholder);
}
</style>
