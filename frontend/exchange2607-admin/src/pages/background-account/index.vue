<template>
  <div>
    <t-card :bordered="false">
      <collapsible-query-form
        :data="queryFormData"
        :label-width="QUERY_FORM_LABEL_WIDTH"
        :create-label="hasPermission('backgroundAccount.add') ? '新增' : ''"
        @query="refreshTable"
        @create="shoAddDialog"
      >
        <t-col :span="3">
          <t-form-item label="账号">
            <t-input v-model="queryFormData.userName"></t-input>
          </t-form-item>
        </t-col>
        <t-col :span="3">
          <t-form-item label="状态">
            <t-select
              v-model="queryFormData.status"
              clearable
              :options="enableStatusDictItems"
              :keys="{ label: 'dictItemName', value: 'dictItemCode' }"
            ></t-select>
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
        <template #account_info="{ row }">
          <div class="table-custom-row">
            <div>
              <t-space :size="4">
                账号:{{ row.userName
                }}<t-tag size="small" :theme="row.status == '1' ? 'primary' : 'warning'">{{ row.statusName }}</t-tag>
              </t-space>
            </div>
            <div class="table-custom-row__sub">
              谷歌验证器：
              <t-tag
                v-if="row.googleAuthBindTime"
                size="small"
                theme="primary"
                :class="{ 'google-auth-tag': !row.superAdminFlag }"
                @click="!row.superAdminFlag && onUnBindGoogleAuth(row.id)"
                >已设置</t-tag
              >
              <t-tag v-else size="small" theme="warning">未设置</t-tag>
              <span v-if="row.googleAuthBindTime && row.superAdminFlag" class="table-custom-row__hint"
                >（超管不可解绑）</span
              >
            </div>
          </div>
        </template>
        <template #operate="{ row }">
          <t-space :size="4">
            <t-button v-if="hasPermission('backgroundAccount.update')" size="small" theme="primary" variant="text" @click="showUpdateDialog(row.id)">修改</t-button>
            <t-dropdown
              v-if="hasPermission(['backgroundAccount.resetPwd', 'backgroundAccount.assignRole', 'backgroundAccount.unbindGoogle', 'backgroundAccount.del'])"
              trigger="click"
              :max-column-width="150"
            >
              <t-button size="small" variant="text">
                更多
                <template #suffix><t-icon name="chevron-down" /></template>
              </t-button>
              <t-dropdown-menu>
                <t-dropdown-item v-if="hasPermission('backgroundAccount.resetPwd')" @click="showUpdateLoginPwdDialog(row.id)">修改登录密码</t-dropdown-item>
                <t-dropdown-item v-if="hasPermission('backgroundAccount.assignRole')" @click="showAssignRoleDialog(row.id)">分配角色</t-dropdown-item>
                <t-dropdown-item
                  v-if="row.googleAuthBindTime && !row.superAdminFlag && hasPermission('backgroundAccount.unbindGoogle')"
                  @click="onUnBindGoogleAuth(row.id)"
                >
                  解绑谷歌验证器
                </t-dropdown-item>
                <t-dropdown-item v-if="hasPermission('backgroundAccount.del')" @click="onDel(row.id)">删除</t-dropdown-item>
              </t-dropdown-menu>
            </t-dropdown>
          </t-space>
        </template>
      </t-table>
    </t-card>
    <t-dialog
      header="修改账号"
      :close-on-overlay-click="false"
      :visible="showUpdateDialogFlag"
      @close="showUpdateDialogFlag = false"
      @confirm="update"
    >
      <template #body>
        <t-form ref="updateForm" :data="updateFormData" :rules="updateFormRules">
          <t-form-item label="账号" name="userName">
            <t-input v-model="updateFormData.userName"></t-input>
          </t-form-item>
          <t-form-item label="账号状态">
            <t-radio-group v-model="updateFormData.status">
              <t-radio
                v-for="dictItem in enableStatusDictItems"
                :key="dictItem.dictItemCode"
                :value="dictItem.dictItemCode"
                >{{ dictItem.dictItemName }}</t-radio
              >
            </t-radio-group>
          </t-form-item>
        </t-form>
      </template>
    </t-dialog>
    <t-dialog
      header="新增"
      :close-on-overlay-click="false"
      :visible="showAddDialogFlag"
      @close="showAddDialogFlag = false"
      @confirm="add"
    >
      <template #body>
        <t-form ref="addForm" :data="addFormData" :rules="addFormRules">
          <t-form-item label="账号" name="userName">
            <t-input v-model="addFormData.userName"></t-input>
          </t-form-item>
          <t-form-item label="登录密码" name="loginPwd">
            <t-input v-model="addFormData.loginPwd"></t-input>
          </t-form-item>
        </t-form>
      </template>
    </t-dialog>
    <t-dialog
      :header="'修改登录密码'"
      :close-on-overlay-click="false"
      :visible="showUpdateLoginPwdDialogFlag"
      @close="showUpdateLoginPwdDialogFlag = false"
      @confirm="updateLoginPwd"
    >
      <template #body>
        <t-form ref="updateLoginPwdForm" :data="updateLoginPwdFormData" :rules="updateLoginPwdFormRules">
          <t-form-item label="修改后的密码" name="pwd">
            <t-input v-model="updateLoginPwdFormData.pwd"></t-input>
          </t-form-item>
        </t-form>
      </template>
    </t-dialog>
    <t-dialog
      header="分配角色"
      :close-on-overlay-click="false"
      :visible="showAssignRoleDialogFlag"
      :cancel-btn="null"
      @close="showAssignRoleDialogFlag = false"
      @confirm="doAssignRole"
    >
      <template #body
        ><t-form label-align="left">
          <t-form-item label="角色">
            <t-checkbox-group v-model="selectedRoleIds">
              <t-checkbox v-for="role in roles" :key="role.id" :value="role.id">{{ role.name }}</t-checkbox>
            </t-checkbox-group>
          </t-form-item>
        </t-form></template
      >
    </t-dialog>
  </div>
</template>
<script lang="ts">
export default {
  name: 'BackgroundAccount',
};
</script>

<script setup lang="ts">
import {
  DialogPlugin,
  type FormInstanceFunctions,
  type FormRule,
  MessagePlugin,
  PrimaryTableCol,
  TableRowData,
} from 'tdesign-vue-next';
import { onMounted, ref } from 'vue';
import { hasPermission } from '@/utils/permission';

import { findDictItemInCache } from '@/api/dict';
import { unBindBackgroundAccountGoogleAuth } from '@/api/rbac';
import CollapsibleQueryForm from '@/components/collapsible-query-form/index.vue';
import { ContentTypeEnum } from '@/constants';
import { QUERY_FORM_LABEL_WIDTH } from '@/constants/form';
import { request } from '@/utils/request';

const enableStatusDictItems = ref<any>([]);
const columns: PrimaryTableCol<TableRowData>[] = [
  {
    colKey: 'account_info',
    title: '账号信息',
  },
  {
    colKey: 'createTime',
    title: '注册时间',
  },
  {
    colKey: 'latelyLoginTime',
    title: '最近登录时间',
  },
  {
    title: '操作',
    colKey: 'operate',
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
  status: '',
});

const selectedRecordId = ref('');

const showUpdateLoginPwdDialogFlag = ref(false);
const updateLoginPwdForm = ref<FormInstanceFunctions>();
const updateLoginPwdFormData = ref({ pwd: '' });
const updateLoginPwdFormRules: Record<string, FormRule[]> = {
  pwd: [{ required: true, message: '请输入密码', type: 'error', trigger: 'submit' }],
};

const showAddDialogFlag = ref(false);
const addForm = ref<FormInstanceFunctions>();
const addFormData = ref({ userName: '', loginPwd: '' });
const addFormRules: Record<string, FormRule[]> = {
  userName: [{ required: true, message: '请输入账号', type: 'error', trigger: 'submit' }],
  loginPwd: [{ required: true, message: '请输入密码', type: 'error', trigger: 'submit' }],
};

const showUpdateDialogFlag = ref(false);
const updateForm = ref<FormInstanceFunctions>();
const updateFormData = ref({ userName: '', status: '' });
const updateFormRules: Record<string, FormRule[]> = {
  userName: [{ required: true, message: '请输入账号', type: 'error', trigger: 'submit' }],
  status: [{ required: true, message: '请选择状态', type: 'error', trigger: 'submit' }],
};

const showAssignRoleDialogFlag = ref(false);
const roles = ref<TableRowData[]>([]);
const selectedRoleIds = ref<string[]>([]);

onMounted(() => {
  initBaseData();
  loadTableData();
});

const doAssignRole = async () => {
  await request.post({
    url: '/backgroundAccount/assignRole',
    headers: {
      'Content-Type': ContentTypeEnum.Json,
    },
    data: {
      accountId: selectedRecordId.value,
      roleIds: selectedRoleIds.value,
    },
  });
  MessagePlugin.success('操作成功');
  showAssignRoleDialogFlag.value = false;
};

const fetchAllRole = async () => {
  roles.value = await request.post({
    url: '/role/findAll',
  });
};

const showAssignRoleDialog = async (id: string) => {
  const selectedRoles = await request.post({
    url: '/backgroundAccount/findRoleByAccountId',
    data: {
      accountId: id,
    },
  });
  const tmpSelectedRoleIds: string[] = [];
  for (let i = 0; i < roles.value.length; i++) {
    const role = roles.value[i];
    for (let j = 0; j < selectedRoles.length; j++) {
      if (role.id === selectedRoles[j].id) {
        tmpSelectedRoleIds.push(selectedRoles[j].id);
        break;
      }
    }
  }
  selectedRoleIds.value = tmpSelectedRoleIds;
  selectedRecordId.value = id;
  showAssignRoleDialogFlag.value = true;
};

const onUnBindGoogleAuth = (id: string) => {
  const dialogInstance = DialogPlugin.confirm({
    header: '提示',
    body: '确定解绑该账号的谷歌验证器吗？',
    onConfirm: async () => {
      await unBindBackgroundAccountGoogleAuth(id);
      dialogInstance.destroy();
      MessagePlugin.success('操作成功');
      refreshTable();
    },
  });
};

const initBaseData = async () => {
  enableStatusDictItems.value = await findDictItemInCache('enableStatus');
  fetchAllRole();
};

const update = async () => {
  updateForm.value?.submit();
  const error = await updateForm.value?.validateOnly();
  if (error !== true) {
    return;
  }
  await request.post({
    url: '/backgroundAccount/updateBackgroundAccount',
    data: updateFormData.value,
  });
  showUpdateDialogFlag.value = false;
  MessagePlugin.success('操作成功');
  refreshTable();
};

const showUpdateDialog = async (id: string) => {
  updateFormData.value = await request.post({
    url: '/backgroundAccount/findBackgroundAccountById',
    data: {
      id,
    },
  });
  showUpdateDialogFlag.value = true;
};

const add = async () => {
  addForm.value?.submit();
  const error = await addForm.value?.validateOnly();
  if (error !== true) {
    return;
  }
  await request.post({
    url: '/backgroundAccount/addBackgroundAccount',
    data: addFormData.value,
  });
  showAddDialogFlag.value = false;
  MessagePlugin.success('操作成功');
  refreshTable();
};

const shoAddDialog = () => {
  addForm.value?.reset();
  showAddDialogFlag.value = true;
};

const updateLoginPwd = async () => {
  updateLoginPwdForm.value?.submit();
  const error = await updateLoginPwdForm.value?.validateOnly();
  if (error !== true) {
    return;
  }
  request.post({
    url: '/backgroundAccount/updateLoginPwd',
    data: {
      id: selectedRecordId.value,
      newPwd: updateLoginPwdFormData.value.pwd,
    },
  });
  showUpdateLoginPwdDialogFlag.value = false;
  MessagePlugin.success('操作成功');
};

const showUpdateLoginPwdDialog = (id: string) => {
  selectedRecordId.value = id;
  updateLoginPwdForm.value?.reset();
  showUpdateLoginPwdDialogFlag.value = true;
};

const onDel = (id: string) => {
  const dialogInstance = DialogPlugin.confirm({
    header: '提示',
    body: '确认要删除吗',
    onConfirm: async () => {
      await request.post({
        url: '/backgroundAccount/delBackgroundAccount',
        data: {
          id,
        },
      });
      dialogInstance.destroy();
      MessagePlugin.success('操作成功');
      refreshTable();
    },
  });
};

const getQueryParam = () => {
  const queryParam = { ...queryFormData.value };
  return queryParam;
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
    url: '/backgroundAccount/findBackgroundAccountByPage',
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
.table-custom-row__sub {
  margin-top: 6px;
  font-size: 13px;
  color: var(--td-text-color-secondary);
}

.google-auth-tag {
  cursor: pointer;
}

.table-custom-row__hint {
  margin-left: 6px;
  font-size: 12px;
  color: var(--td-text-color-placeholder);
}
</style>
