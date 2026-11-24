<template>
  <div>
    <t-card :bordered="false">
      <template #actions>
        <t-button v-if="hasPermission('role.save')" theme="primary" @click="showAddDialog">新增</t-button>
      </template>
      <t-table row-key="id" :data="data" :columns="columns" table-layout="auto" :hover="true" :loading="dataLoading">
        <template #operate="{ row }">
          <t-space :size="4">
            <t-button v-if="hasPermission('role.save')" size="small" theme="primary" variant="text" @click="showEditDialog(row.id)">修改</t-button>
            <t-dropdown v-if="hasPermission(['role.assignMenu', 'role.del'])" trigger="click" :max-column-width="150">
              <t-button size="small" variant="text">
                更多
                <template #suffix><t-icon name="chevron-down" /></template>
              </t-button>
              <t-dropdown-menu>
                <t-dropdown-item v-if="hasPermission('role.assignMenu')" @click="showAssignMenuDialog(row.id)">分配菜单</t-dropdown-item>
                <t-dropdown-item v-if="hasPermission('role.del')" @click="onDel(row.id)">删除</t-dropdown-item>
              </t-dropdown-menu>
            </t-dropdown>
          </t-space>
        </template>
      </t-table>
    </t-card>
    <t-dialog
      :header="addOrUpdateAction == 'add' ? '新增' : '修改'"
      :close-on-overlay-click="false"
      :visible="showAddOrUpdateDialogFlag"
      @close="showAddOrUpdateDialogFlag = false"
      @confirm="addOrUpdate"
    >
      <template #body>
        <t-form ref="form" :data="formData" :rules="formRules">
          <t-form-item v-show="false" label="id" name="id">
            <t-input v-model="formData.id"></t-input>
          </t-form-item>
          <t-form-item label="角色名" name="name">
            <t-input v-model="formData.name"></t-input>
          </t-form-item>
        </t-form>
      </template>
    </t-dialog>
    <t-dialog
      header="分配菜单"
      :close-on-overlay-click="false"
      :visible="showAssignMenuDialogFlag"
      :cancel-btn="null"
      @close="showAssignMenuDialogFlag = false"
      @confirm="assignMenu"
      width="960px"
    >
      <template #body>
        <div class="assign-menu-toolbar">
          <t-checkbox v-model="selectedAllMenu" @change="toggleSelectedAllMenu">全选/全不选</t-checkbox>
          <span class="assign-menu-summary">
            已选菜单 {{ selectedMenuCount }} 个 · 已选 {{ selectedPermissionCount }} / {{ totalPermissionCount }} 个权限
          </span>
        </div>
        <div style="height: 460px; overflow: auto">
          <t-enhanced-table
            v-model:expandedTreeNodes="assignExpandedTreeNodes"
            row-key="id"
            :data="assignMenuTrees"
            :columns="assignColumns"
            :tree="assignTreeConfig"
            :selected-row-keys="menuRowSelectedKeys"
            table-layout="auto"
            max-height="440px"
            @select-change="onMenuSelectChange"
          >
            <template #type="{ row }">
              <t-tag :theme="typeMeta(row.type).theme" variant="light" size="small">
                {{ typeMeta(row.type).label }}
              </t-tag>
            </template>
            <template #permissions="{ row }">
              <div v-if="row.type === 'menu_1'" class="assign-permissions-summary">
                下级共 {{ countSubPermissions(row) }} 个权限
                <span v-if="countSubPermissions(row)" class="assign-permissions-selected">
                  （已选 {{ countSelectedSubPermissions(row) }} 个）
                </span>
              </div>
              <div v-else-if="row.type === 'menu_2'" class="assign-permissions">
                <t-checkbox
                  v-for="p in getRowPermissions(row)"
                  :key="p.id"
                  :checked="selectedIds.includes(p.id)"
                  @change="(checked) => togglePermission(p, checked)"
                >
                  {{ p.name }}
                </t-checkbox>
              </div>
              <span v-else class="assign-permissions-empty">—</span>
            </template>
          </t-enhanced-table>
        </div>
      </template>
    </t-dialog>
  </div>
</template>
<script lang="ts">
export default {
  name: 'Role',
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
import { computed, onMounted, ref } from 'vue';

import { ContentTypeEnum } from '@/constants';
import { hasPermission } from '@/utils/permission';
import { request } from '@/utils/request';

interface MenuTreeNode {
  id: string;
  name: string;
  url?: string;
  type?: string;
  sortNum?: number;
  parentId?: string;
  subMenus?: MenuTreeNode[];
  permissions?: MenuTreeNode[];
}

const columns: PrimaryTableCol<TableRowData>[] = [
  {
    colKey: 'name',
    title: '角色名',
  },
  {
    colKey: 'createTime',
    title: '创建时间',
  },
  {
    colKey: 'operate',
    title: '操作',
  },
];
const data = ref<TableRowData[]>([]);
const dataLoading = ref(false);

const showAddOrUpdateDialogFlag = ref(false);
const addOrUpdateAction = ref('');
const formRules: Record<string, FormRule[]> = {
  name: [{ required: true, message: '请输入角色名', type: 'error', trigger: 'submit' }],
};
const form = ref<FormInstanceFunctions>();
const formData = ref({ id: '', name: '' });

const menuTrees = ref<MenuTreeNode[]>([]);
const showAssignMenuDialogFlag = ref(false);
const selectedIds = ref<string[]>([]);
const selectedAllMenu = ref(false);
const selectedRecordId = ref('');
const assignExpandedTreeNodes = ref<string[]>([]);

const nodeIndex = computed(() => {
  const map = new Map<string, MenuTreeNode>();
  const walk = (nodes: MenuTreeNode[]) => {
    nodes.forEach((n) => {
      map.set(n.id, n);
      if (n.subMenus) walk(n.subMenus);
    });
  };
  walk(menuTrees.value);
  return map;
});

const assignMenuTrees = computed(() =>
  menuTrees.value.map((m1) => ({
    ...m1,
    subMenus: (m1.subMenus || [])
      .filter((m) => m.type === 'menu_2')
      .map((m2) => ({
        ...m2,
        permissions: (m2.subMenus || []).filter((m) => m.type === 'menu_3'),
        subMenus: [],
      })),
  })),
);

const assignTreeConfig = {
  childrenKey: 'subMenus',
  treeNodeColumnIndex: 1,
};

const assignColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'row-select', type: 'multiple', width: 46 },
  { colKey: 'name', title: '菜单名', ellipsis: true },
  { colKey: 'type', title: '类型', width: 100 },
  { colKey: 'permissions', title: '权限', width: 380 },
];

const typeMeta = (type?: string) => {
  if (type === 'menu_1') return { label: '目录', theme: 'primary' as const };
  if (type === 'menu_2') return { label: '页面', theme: 'success' as const };
  if (type === 'menu_3') return { label: '权限', theme: 'warning' as const };
  return { label: '未知', theme: 'default' as const };
};

const getRowPermissions = (row: MenuTreeNode): MenuTreeNode[] => row.permissions || [];

const totalPermissionCount = computed(
  () => Array.from(nodeIndex.value.values()).filter((n) => n.type === 'menu_3').length,
);

const selectedPermissionCount = computed(
  () => selectedIds.value.filter((id) => nodeIndex.value.get(id)?.type === 'menu_3').length,
);

const selectedMenuCount = computed(
  () => selectedIds.value.filter((id) => nodeIndex.value.get(id)?.type !== 'menu_3').length,
);

const countSelectedRowPermissions = (row: MenuTreeNode): number =>
  getRowPermissions(row).filter((p) => selectedIds.value.includes(p.id)).length;

const countSubPermissions = (row: MenuTreeNode): number =>
  (row.subMenus || [])
    .filter((m) => m.type === 'menu_2')
    .reduce((sum, m) => sum + getRowPermissions(m).length, 0);

const countSelectedSubPermissions = (row: MenuTreeNode): number =>
  (row.subMenus || [])
    .filter((m) => m.type === 'menu_2')
    .reduce((sum, m) => sum + countSelectedRowPermissions(m), 0);

const menuRowSelectedKeys = computed(() =>
  selectedIds.value.filter((id) => nodeIndex.value.get(id)?.type !== 'menu_3'),
);

const collectSelfAndDescendants = (node: MenuTreeNode): string[] => {
  const ids: string[] = [node.id];
  (node.subMenus || []).forEach((child) => ids.push(...collectSelfAndDescendants(child)));
  return ids;
};

const onMenuSelectChange = (keys: (string | number)[]) => {
  const newKeys = keys as string[];
  const oldKeys = menuRowSelectedKeys.value;
  const added = newKeys.filter((k) => !oldKeys.includes(k));
  const removed = oldKeys.filter((k) => !newKeys.includes(k));
  const next = new Set(selectedIds.value);
  added.forEach((id) => {
    const node = nodeIndex.value.get(id);
    if (node) collectSelfAndDescendants(node).forEach((x) => next.add(x));
  });
  removed.forEach((id) => {
    const node = nodeIndex.value.get(id);
    if (node) collectSelfAndDescendants(node).forEach((x) => next.delete(x));
  });
  selectedIds.value = Array.from(next);
};

const togglePermission = (permission: MenuTreeNode, checked: boolean) => {
  const next = new Set(selectedIds.value);
  if (checked) {
    next.add(permission.id);
    let parentId = permission.parentId;
    while (parentId) {
      next.add(parentId);
      parentId = nodeIndex.value.get(parentId)?.parentId;
    }
  } else {
    next.delete(permission.id);
  }
  selectedIds.value = Array.from(next);
};

onMounted(() => {
  fetchMenuTree();
  fetchData();
});

const toggleSelectedAllMenu = () => {
  selectedIds.value = selectedAllMenu.value ? Array.from(nodeIndex.value.keys()) : [];
};

const assignMenu = async () => {
  await request.post({
    url: '/role/assignMenu',
    headers: {
      'Content-Type': ContentTypeEnum.Json,
    },
    data: {
      roleId: selectedRecordId.value,
      menuIds: selectedIds.value,
    },
  });
  MessagePlugin.success('操作成功');
  showAssignMenuDialogFlag.value = false;
};

const showAssignMenuDialog = async (id: string) => {
  const selectedMenus: MenuTreeNode[] = await request.post({
    url: '/role/findMenuByRoleId',
    data: {
      roleId: id,
    },
  });
  selectedIds.value = selectedMenus.map((m) => m.id);
  assignExpandedTreeNodes.value = [];
  showAssignMenuDialogFlag.value = true;
  selectedAllMenu.value = false;
  selectedRecordId.value = id;
};

const fetchMenuTree = async () => {
  menuTrees.value = await request.post<MenuTreeNode[]>({
    url: '/menu/findMenuTree',
  });
};

const fetchData = async () => {
  data.value = await request.post({
    url: '/role/findAll',
  });
};

const addOrUpdate = async () => {
  form.value?.submit();
  const error = await form.value?.validateOnly();
  if (error !== true) {
    return;
  }
  await request.post({
    url: '/role/addOrUpdate',
    data: formData.value,
  });
  showAddOrUpdateDialogFlag.value = false;
  MessagePlugin.success('操作成功');
  fetchData();
};

const showEditDialog = async (id: string) => {
  form.value?.reset();
  formData.value = await request.post({
    url: '/role/findById',
    data: {
      id,
    },
  });
  addOrUpdateAction.value = 'update';
  showAddOrUpdateDialogFlag.value = true;
};

const showAddDialog = () => {
  form.value?.reset();
  addOrUpdateAction.value = 'add';
  showAddOrUpdateDialogFlag.value = true;
};
const onDel = (id: string) => {
  const dialogInstance = DialogPlugin.confirm({
    header: '提示',
    body: '确认要删除吗',
    onConfirm: async () => {
      await request.post({
        url: '/role/del',
        data: {
          id,
        },
      });
      dialogInstance.destroy();
      MessagePlugin.success('操作成功');
      fetchData();
    },
  });
};
</script>

<style lang="less" scoped>
.assign-menu-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.assign-menu-summary {
  color: var(--td-text-color-secondary);
  font-size: 13px;
}

.assign-permissions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px 12px;
}

.assign-permissions-summary {
  color: var(--td-text-color-placeholder);
  font-size: 12px;
}

.assign-permissions-selected {
  color: var(--td-text-color-secondary);
}

.assign-permissions-empty {
  color: var(--td-text-color-placeholder);
}
</style>
