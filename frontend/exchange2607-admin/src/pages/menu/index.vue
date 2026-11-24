<template>
  <div>
    <t-card :bordered="false">
      <template #actions>
        <t-space :size="8" align="center">
          <t-button variant="outline" @click="toggleExpandAll">
            {{ expandAll ? '全部折叠' : '全部展开' }}
          </t-button>
          <t-button v-if="hasPermission('menu.save')" variant="outline" @click="openSyncDialog">同步权限码</t-button>
          <t-button v-if="hasPermission('menu.save')" theme="primary" @click="showAddDialog('', '')">新增主菜单</t-button>
        </t-space>
      </template>

      <t-enhanced-table
        :key="tableKey"
        v-model:expandedTreeNodes="expandedTreeNodes"
        row-key="id"
        :data="menuTrees"
        :columns="columns"
        table-layout="auto"
        :hover="true"
        :loading="loading || savingOrder"
        :tree="treeConfig"
        max-height="calc(100vh - 260px)"
      >
        <template #type="{ row }">
          <t-tag :theme="typeMeta(row.type).theme" variant="light" size="small">
            {{ typeMeta(row.type).label }}
          </t-tag>
        </template>
        <template #permissions="{ row }">
          <div v-if="row.type === 'menu_2'" class="menu-permissions-cell">
            <t-space v-if="getRowPermissions(row).length" :size="4" break-line>
              <t-tag v-for="p in getRowPermissions(row)" :key="p.id" size="small" variant="light" :title="p.url">
                {{ p.name }}
              </t-tag>
            </t-space>
            <span v-else class="menu-permissions-empty">—</span>
          </div>
          <span v-else-if="row.type === 'menu_1' && countSubPermissions(row)" class="menu-permissions-count">
            下级共 {{ countSubPermissions(row) }} 个权限
          </span>
        </template>
        <template #url="{ row }">
          <div class="menu-url-cell">
            <span class="menu-url-cell__value">{{ row.url || '—' }}</span>
            <span class="menu-url-cell__hint">{{ getUrlHint(row) }}</span>
          </div>
        </template>
        <template #sortNum="{ row }">
          {{ row.sortNum ?? '—' }}
        </template>
        <template #operate="{ row }">
          <t-space :size="4">
            <t-button
              v-if="hasPermission('menu.save')"
              size="small"
              theme="default"
              variant="text"
              shape="square"
              title="上移"
              :disabled="!canMoveUp(row)"
              @click="onMoveUp(row)"
            >
              <t-icon name="arrow-up" />
            </t-button>
            <t-button
              v-if="hasPermission('menu.save')"
              size="small"
              theme="default"
              variant="text"
              shape="square"
              title="下移"
              :disabled="!canMoveDown(row)"
              @click="onMoveDown(row)"
            >
              <t-icon name="arrow-down" />
            </t-button>
            <t-button
              v-if="row.type === 'menu_1' && hasPermission('menu.save')"
              size="small"
              theme="primary"
              variant="text"
              @click="showAddDialog(row.id, row.name)"
            >
              新增下级
            </t-button>
            <t-button
              v-if="row.type === 'menu_2' && hasPermission('menu.save')"
              size="small"
              theme="primary"
              variant="text"
              @click="showAddPermissionDialog(row.id, row.name)"
            >
              新增权限
            </t-button>
            <t-button v-if="hasPermission('menu.save')" size="small" theme="default" variant="text" @click="showEditDialog(row.id)">编辑</t-button>
            <t-button v-if="hasPermission('menu.del')" size="small" theme="danger" variant="text" @click="onDel(row.id)">删除</t-button>
          </t-space>
        </template>
      </t-enhanced-table>
    </t-card>

    <t-dialog
      v-model:visible="showAddOrUpdateDialogFlag"
      :header="dialogTitle"
      :close-on-overlay-click="false"
      width="520px"
      @confirm="addOrUpdate"
    >
      <template #body>
        <t-form v-if="formData.type === 'menu_1' || formData.type === 'menu_2'" ref="formRef" :data="formData">
          <t-form-item v-if="formData.parentName" label="上级菜单">
            <t-input v-model="formData.parentName" disabled />
          </t-form-item>
          <t-form-item label="菜单名" name="name">
            <t-input v-model="formData.name" placeholder="侧栏展示名称" />
          </t-form-item>
          <t-form-item label="URL" name="url">
            <t-input v-model="formData.url" :placeholder="urlInputPlaceholder" />
            <template #tips>
              <span class="form-tip">{{ urlFormTip }}</span>
            </template>
          </t-form-item>
          <t-form-item label="排序号" name="sortNum">
            <t-input-number v-model="formData.sortNum" theme="column" :min="0" style="width: 100%" />
            <template #tips>
              <span class="form-tip">留空则自动追加到同级末尾</span>
            </template>
          </t-form-item>
        </t-form>
        <t-form v-if="formData.type === 'menu_3'" ref="formRef" :data="formData">
          <t-form-item v-if="formData.parentName" label="上级菜单">
            <t-input v-model="formData.parentName" disabled />
          </t-form-item>
          <t-form-item label="权限名" name="name">
            <t-input v-model="formData.name" placeholder="如：删除角色" />
          </t-form-item>
          <t-form-item label="权限标识" name="url">
            <t-input v-model="formData.url" placeholder="如：role.del" />
            <template #tips>
              <span class="form-tip">用于按钮/接口鉴权，不是页面路径</span>
            </template>
          </t-form-item>
          <t-form-item label="排序号" name="sortNum">
            <t-input-number v-model="formData.sortNum" theme="column" :min="0" style="width: 100%" />
            <template #tips>
              <span class="form-tip">留空则自动追加到同级末尾</span>
            </template>
          </t-form-item>
        </t-form>
      </template>
    </t-dialog>

    <t-dialog
      v-model:visible="syncDialogVisible"
      header="同步权限码"
      :close-on-overlay-click="false"
      width="720px"
      :footer="false"
    >
      <div class="sync-permission-tip">以下为代码中已声明、但尚未录入菜单的按钮权限，勾选后批量导入</div>
      <t-table
        row-key="code"
        :data="unsyncedList"
        :columns="syncColumns"
        :selected-row-keys="selectedCodes"
        :loading="syncLoading"
        max-height="420px"
        @select-change="onSyncSelectChange"
      >
        <template #targetMenuName="{ row }">
          <span v-if="row.matched">{{ row.targetMenuName }}</span>
          <span v-else class="sync-permission-unmatched">未找到目标菜单</span>
        </template>
      </t-table>
      <div class="sync-permission-footer">
        <span class="sync-permission-count">已选 {{ selectedCodes.length }} 条</span>
        <t-button theme="primary" :disabled="selectedCodes.length === 0" @click="doSync">导入选中</t-button>
      </div>
    </t-dialog>
  </div>
</template>

<script lang="ts">
export default {
  name: 'Menu',
};
</script>

<script setup lang="ts">
import { DialogPlugin, MessagePlugin, type PrimaryTableCol, type TableRowData } from 'tdesign-vue-next';
import { computed, onMounted, ref } from 'vue';
import { hasPermission } from '@/utils/permission';

import common from '@/utils/common';
import { request } from '@/utils/request';
import { ContentTypeEnum } from '@/constants';

interface MenuTreeNode {
  id: string;
  name: string;
  url: string;
  type: string;
  sortNum?: number;
  parentId?: string;
  parentName?: string;
  subMenus?: MenuTreeNode[];
}

const loading = ref(false);
const savingOrder = ref(false);
const menuTrees = ref<MenuTreeNode[]>([]);
const expandAll = ref(false);
const expandedTreeNodes = ref<string[]>([]);
const tableKey = ref(0);
const menu2ParentUrlMap = ref<Map<string, string>>(new Map());

const treeConfig = computed(() => ({
  childrenKey: 'subMenus',
  treeNodeColumnIndex: 0,
}));

const collectExpandableIds = (nodes: MenuTreeNode[]): string[] => {
  const ids: string[] = [];
  const walk = (list: MenuTreeNode[]) => {
    for (const node of list) {
      if (node.subMenus?.length) {
        ids.push(node.id);
        walk(node.subMenus);
      }
    }
  };
  walk(nodes);
  return ids;
};

const syncExpandedTreeNodes = () => {
  expandedTreeNodes.value = expandAll.value ? collectExpandableIds(menuTrees.value) : [];
};

const columns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'name', title: '菜单/权限名', ellipsis: true },
  { colKey: 'type', title: '类型' },
  { colKey: 'permissions', title: '权限' },
  { colKey: 'url', title: 'URL / 标识' },
  { colKey: 'sortNum', title: '排序', align: 'center' },
  { colKey: 'operate', title: '操作' },
];

const getRowPermissions = (row: MenuTreeNode): MenuTreeNode[] =>
  (row.subMenus || []).filter((m) => m.type === 'menu_3');

const countSubPermissions = (row: MenuTreeNode): number =>
  (row.subMenus || [])
    .filter((m) => m.type === 'menu_2')
    .reduce((sum, m) => sum + getRowPermissions(m).length, 0);

interface PermissionCodeVO {
  code: string;
  name?: string;
  targetMenuId?: string;
  targetMenuName?: string;
  targetMenuUrl?: string;
  matched: boolean;
}

const syncColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'row-select', type: 'multiple', width: 46 },
  { colKey: 'code', title: '权限码', ellipsis: true },
  { colKey: 'name', title: '权限名', ellipsis: true },
  { colKey: 'targetMenuName', title: '目标菜单', ellipsis: true },
];

const syncDialogVisible = ref(false);
const syncLoading = ref(false);
const unsyncedList = ref<PermissionCodeVO[]>([]);
const selectedCodes = ref<string[]>([]);

const openSyncDialog = async () => {
  syncDialogVisible.value = true;
  syncLoading.value = true;
  selectedCodes.value = [];
  try {
    unsyncedList.value = await request.post<PermissionCodeVO[]>({
      url: '/menu/findUnsyncedPermissions',
    });
  } finally {
    syncLoading.value = false;
  }
};

const onSyncSelectChange = (selectedRowKeys: (string | number)[]) => {
  selectedCodes.value = selectedRowKeys as string[];
};

const doSync = async () => {
  if (selectedCodes.value.length === 0) return;
  await request.post({
    url: '/menu/syncPermissions',
    headers: {
      'Content-Type': ContentTypeEnum.Json,
    },
    data: { codes: selectedCodes.value },
  });
  MessagePlugin.success(`已导入 ${selectedCodes.value.length} 条权限`);
  syncDialogVisible.value = false;
  fetchData();
};

const showAddOrUpdateDialogFlag = ref(false);
const addOrUpdateAction = ref<'add' | 'update'>('add');
const emptyFormData = (): MenuTreeNode => ({
  id: '',
  name: '',
  url: '',
  type: '',
  sortNum: undefined,
  parentId: '',
  parentName: '',
});
const formData = ref<MenuTreeNode>(emptyFormData());

const dialogTitle = computed(() => {
  const action = addOrUpdateAction.value === 'add' ? '新增' : '编辑';
  const typeLabel = typeMeta(formData.value.type).label;
  return `${action}${typeLabel}`;
});

const urlInputPlaceholder = computed(() => {
  if (formData.value.type === 'menu_1') return '如：marketing';
  if (formData.value.type === 'menu_2') return '如：marketing-campaign';
  return '';
});

const urlFormTip = computed(() => {
  if (formData.value.type === 'menu_1') {
    return '一级目录路由段，对应侧栏分组路径 /page/{url}';
  }
  if (formData.value.type === 'menu_2') {
    return '须与 pages 下文件夹名一致，如 pages/marketing-campaign/index.vue';
  }
  return '';
});

onMounted(() => {
  fetchData();
});

const typeMeta = (type: string) => {
  if (type === 'menu_1') return { label: '目录', theme: 'primary' as const };
  if (type === 'menu_2') return { label: '页面', theme: 'success' as const };
  if (type === 'menu_3') return { label: '权限', theme: 'warning' as const };
  return { label: '未知', theme: 'default' as const };
};

const findSiblingList = (tree: MenuTreeNode[], nodeId: string): MenuTreeNode[] | null => {
  const search = (list: MenuTreeNode[]): MenuTreeNode[] | null => {
    for (const node of list) {
      if (node.id === nodeId) return list;
      if (node.subMenus?.length) {
        const found = search(node.subMenus);
        if (found) return found;
      }
    }
    return null;
  };
  return search(tree);
};

const getSameTypeSiblings = (row: MenuTreeNode): MenuTreeNode[] => {
  const siblings = findSiblingList(menuTrees.value, row.id);
  if (!siblings) return [];
  return siblings.filter((s) => s.type === row.type).sort((a, b) => (a.sortNum ?? 0) - (b.sortNum ?? 0));
};

const getSameTypeIndex = (row: MenuTreeNode) => {
  const list = getSameTypeSiblings(row);
  return list.findIndex((s) => s.id === row.id);
};

const canMoveUp = (row: MenuTreeNode) => getSameTypeIndex(row) > 0;

const canMoveDown = (row: MenuTreeNode) => {
  const list = getSameTypeSiblings(row);
  const idx = getSameTypeIndex(row);
  return idx >= 0 && idx < list.length - 1;
};

const rebuildMenu2ParentUrlMap = (nodes: MenuTreeNode[]) => {
  const map = new Map<string, string>();
  const walk = (list: MenuTreeNode[], parentMenu1Url?: string) => {
    for (const node of list) {
      if (node.type === 'menu_1') {
        if (node.subMenus?.length) walk(node.subMenus, node.url);
      } else if (node.type === 'menu_2') {
        if (parentMenu1Url) map.set(node.id, parentMenu1Url);
        if (node.subMenus?.length) walk(node.subMenus, parentMenu1Url);
      } else if (node.subMenus?.length) {
        walk(node.subMenus, parentMenu1Url);
      }
    }
  };
  walk(nodes);
  menu2ParentUrlMap.value = map;
};

const getUrlHint = (row: MenuTreeNode) => {
  if (row.type === 'menu_1') {
    return `侧栏目录 → /page/${row.url || '…'}`;
  }
  if (row.type === 'menu_2') {
    const parentUrl = menu2ParentUrlMap.value.get(row.id);
    if (parentUrl && row.url) {
      return `页面 pages/${row.url}/index.vue → /page/${parentUrl}/${row.url}`;
    }
    return `页面路径 pages/${row.url || '…'}/index.vue`;
  }
  if (row.type === 'menu_3') {
    return '按钮权限标识，非页面路径';
  }
  return '';
};

const fetchData = async (silent = false) => {
  if (!silent) loading.value = true;
  const expanded = [...expandedTreeNodes.value];
  try {
    menuTrees.value = await request.post<MenuTreeNode[]>({
      url: '/menu/findMenuTree',
    });
    rebuildMenu2ParentUrlMap(menuTrees.value);
    expandedTreeNodes.value = expanded;
    if (expandAll.value) {
      syncExpandedTreeNodes();
    }
  } finally {
    if (!silent) loading.value = false;
  }
};

const toggleExpandAll = () => {
  expandAll.value = !expandAll.value;
  syncExpandedTreeNodes();
  tableKey.value += 1;
};

const moveOrder = async (row: MenuTreeNode, url: '/menu/moveUp' | '/menu/moveDown') => {
  savingOrder.value = true;
  try {
    await request.post({
      url,
      data: { id: row.id },
    });
    MessagePlugin.success('操作成功');
    await fetchData(true);
  } catch {
    await fetchData(true);
  } finally {
    savingOrder.value = false;
  }
};

const onMoveUp = (row: MenuTreeNode) => {
  if (!canMoveUp(row)) return;
  moveOrder(row, '/menu/moveUp');
};

const onMoveDown = (row: MenuTreeNode) => {
  if (!canMoveDown(row)) return;
  moveOrder(row, '/menu/moveDown');
};

const addOrUpdate = async () => {
  if (formData.value.type === 'menu_3') {
    if (common.isBlank(formData.value.name)) {
      MessagePlugin.warning('请输入权限名');
      return;
    }
    if (common.isBlank(formData.value.url)) {
      MessagePlugin.warning('请输入权限标识');
      return;
    }
  } else {
    if (common.isBlank(formData.value.name)) {
      MessagePlugin.warning('请输入菜单名');
      return;
    }
    if (common.isBlank(formData.value.url)) {
      MessagePlugin.warning('请输入 URL');
      return;
    }
  }

  await request.post({
    url: '/menu/addOrUpdate',
    data: formData.value,
  });
  showAddOrUpdateDialogFlag.value = false;
  MessagePlugin.success('操作成功');
  fetchData();
};

const showEditDialog = async (id: string) => {
  formData.value = await request.post<MenuTreeNode>({
    url: '/menu/findById',
    data: { id },
  });
  addOrUpdateAction.value = 'update';
  showAddOrUpdateDialogFlag.value = true;
};

const showAddDialog = (parentId: string, parentName: string) => {
  formData.value = emptyFormData();
  formData.value.parentId = parentId;
  formData.value.parentName = parentName;
  formData.value.type = parentId === '' ? 'menu_1' : 'menu_2';
  addOrUpdateAction.value = 'add';
  showAddOrUpdateDialogFlag.value = true;
};

const showAddPermissionDialog = (parentId: string, parentName: string) => {
  formData.value = emptyFormData();
  formData.value.parentId = parentId;
  formData.value.parentName = parentName;
  formData.value.type = 'menu_3';
  addOrUpdateAction.value = 'add';
  showAddOrUpdateDialogFlag.value = true;
};

const onDel = (id: string) => {
  const dialogInstance = DialogPlugin.confirm({
    header: '提示',
    body: '确认要删除吗',
    onConfirm: async () => {
      await request.post({
        url: '/menu/del',
        data: { id },
      });
      dialogInstance.destroy();
      MessagePlugin.success('操作成功');
      fetchData();
    },
  });
};
</script>

<style lang="less" scoped>
.menu-url-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1.4;
}

.menu-url-cell__value {
  color: var(--td-text-color-primary);
  word-break: break-all;
}

.menu-url-cell__hint {
  color: var(--td-text-color-placeholder);
  font-size: 12px;
}

.menu-permissions-cell {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}

.menu-permissions-empty {
  color: var(--td-text-color-placeholder);
}

.menu-permissions-count {
  color: var(--td-text-color-placeholder);
  font-size: 12px;
}

.form-tip {
  color: var(--td-text-color-placeholder);
  font-size: 12px;
}

.sync-permission-tip {
  color: var(--td-text-color-placeholder);
  font-size: 13px;
  margin-bottom: 12px;
}

.sync-permission-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}

.sync-permission-count {
  color: var(--td-text-color-secondary);
  font-size: 13px;
}

.sync-permission-unmatched {
  color: var(--td-error-color);
}
</style>
