<template>
  <t-drawer v-model:visible="drawerVisible" header="团队成员" size="680px" :footer="false" destroy-on-close>
    <t-loading :loading="loading" size="small">
      <template v-if="overview">
        <!-- 概览卡片 -->
        <div class="team-overview">
          <div class="team-overview__item">
            <span class="team-overview__num">{{ overview.teamTotalCount || 0 }}</span>
            <span class="team-overview__label">团队总人数</span>
          </div>
          <t-divider layout="vertical" />
          <div class="team-overview__item">
            <span class="team-overview__num">{{ overview.directCount || 0 }}</span>
            <span class="team-overview__label">直推人数</span>
          </div>
          <t-divider layout="vertical" />
          <div class="team-overview__item">
            <span class="team-overview__num">{{ indirectCount }}</span>
            <span class="team-overview__label">间接人数</span>
          </div>
          <t-divider layout="vertical" />
          <div class="team-overview__item">
            <span class="team-overview__num">{{ overview.todayNewCount || 0 }}</span>
            <span class="team-overview__label">今日新增</span>
          </div>
        </div>

        <!-- 层级分布 -->
        <div v-if="levelRows.length > 0" class="team-level-section">
          <div class="team-level-section__head">
            <span class="team-level-section__title">层级分布</span>
            <t-tag v-if="overview.rebateMaxLevel" size="small" theme="primary">
              {{ overview.rebateMaxLevel === 1 ? '仅直推返佣' : `返佣${overview.rebateMaxLevel}级` }}
            </t-tag>
            <span v-if="rebateSummary.over > 0" class="team-level-section__summary">
              返佣层级内 {{ rebateSummary.in }}人 · 超出 {{ rebateSummary.over }}人
            </span>
          </div>
          <div class="team-level-list">
            <div
              v-for="row in levelRows"
              :key="row.level"
              class="team-level-row"
              :class="{ 'team-level-row--active': selectedLevel === row.level, 'team-level-row--over': row.overRebate }"
              @click="toggleLevel(row.level)"
            >
              <div class="team-level-row__head">
                <span class="team-level-row__label">{{ row.label }}</span>
                <span class="team-level-row__count">{{ row.count }}人 ({{ row.percent }}%)</span>
              </div>
              <div class="team-level-row__bar">
                <div class="team-level-row__bar-fill" :style="{ width: row.percent + '%' }" />
              </div>
            </div>
          </div>
        </div>

        <!-- 筛选 -->
        <div class="team-filter">
          <t-space>
            <t-button :theme="selectedLevel === null ? 'primary' : 'default'" variant="outline" size="small" @click="toggleLevel(null)">全部</t-button>
            <t-button
              v-for="row in levelRows"
              :key="row.level"
              :theme="selectedLevel === row.level ? 'primary' : 'default'"
              variant="outline"
              size="small"
              @click="toggleLevel(row.level)"
            >{{ row.label }}</t-button>
          </t-space>
        </div>

        <!-- 成员表格 -->
        <t-table
          row-key="id"
          :loading="tableLoading"
          :data="tableData"
          :columns="columns"
          :pagination="pagination"
          :bordered="true"
          size="small"
          table-layout="auto"
          @page-change="onPageChange"
        >
          <template #accountInfo="{ row }">
            <div class="team-cell">
              <t-link v-if="row.uid != null" theme="primary" hover="color" @click="openMemberDetail(row.id)">
                {{ row.uid }}
              </t-link>
              <span v-else>—</span>
              <copy-icon-button v-if="row.uid != null" :text="String(row.uid)" />
            </div>
          </template>
          <template #levelTag="{ row }">
            <t-tag size="small" :theme="row.level === 1 ? 'success' : row.overRebate ? 'default' : 'primary'">
              {{ row.level === 1 ? '直推' : `第${row.level}级` }}
            </t-tag>
          </template>
          <template #status="{ row }">
            <t-tag size="small" :theme="statusTagTheme(row.status)">
              {{ row.statusName || row.status || '—' }}
            </t-tag>
          </template>
        </t-table>
      </template>
    </t-loading>
  </t-drawer>

  <member-account-detail-drawer v-model:visible="detailVisible" :member-id="detailMemberId" />
</template>

<script setup lang="ts">
import type { PrimaryTableCol, TableRowData, PageInfo } from 'tdesign-vue-next';
import { computed, ref, watch } from 'vue';

import type { TeamOverview, TeamMemberRow } from '@/api/member';
import { findTeamOverview, findTeamMemberByPage } from '@/api/member';
import CopyIconButton from '@/components/copy-icon-button/index.vue';
import MemberAccountDetailDrawer from '@/components/member-account-detail-drawer/index.vue';

const props = defineProps<{
  visible: boolean;
  memberId: string;
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
}>();

const loading = ref(false);
const tableLoading = ref(false);
const overview = ref<TeamOverview | null>(null);
const tableData = ref<(TeamMemberRow & { overRebate?: boolean })[]>([]);
const selectedLevel = ref<number | null>(null);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

const detailVisible = ref(false);
const detailMemberId = ref('');
const openMemberDetail = (id: string) => {
  if (!id) return;
  detailMemberId.value = id;
  detailVisible.value = true;
};

const drawerVisible = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value),
});

const indirectCount = computed(() => {
  return (overview.value?.teamTotalCount || 0) - (overview.value?.directCount || 0);
});

/** 账号状态标签配色（后端取值为 NORMAL / FROZEN，与会员列表页保持一致） */
function statusTagTheme(status?: string): 'primary' | 'warning' | 'danger' {
  if (status === 'NORMAL') {
    return 'primary';
  }
  if (status === 'FROZEN') {
    return 'warning';
  }
  return 'danger';
}

const columns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'accountInfo', title: '账号' },
  { colKey: 'nickName', title: '昵称' },
  { colKey: 'levelTag', title: '层级' },
  { colKey: 'status', title: '状态'},
  { colKey: 'createTime', title: '注册时间', cell: (h, { row }) => (row as TeamMemberRow).createTime || '—' },
];

const levelRows = computed(() => {
  if (!overview.value?.levelCounts) return [];
  const counts = overview.value.levelCounts;
  const total = overview.value?.teamTotalCount || 0;
  const rebateMax = overview.value.rebateMaxLevel || 0;
  return counts
    .slice()
    .sort((a, b) => (a.level ?? 0) - (b.level ?? 0))
    .map(c => {
      const lv = c.level ?? 0;
      return {
        level: lv,
        label: lv === 1 ? '第1级（直推）' : `第${lv}级`,
        count: c.count ?? 0,
        percent: total > 0 ? Math.round(((c.count ?? 0) / total) * 100) : 0,
        overRebate: rebateMax > 0 && lv > rebateMax,
      };
    });
});

const rebateSummary = computed(() => {
  const rebateMax = overview.value?.rebateMaxLevel || 0;
  let inCount = 0;
  let overCount = 0;
  for (const row of levelRows.value) {
    if (rebateMax > 0 && row.level > rebateMax) {
      overCount += row.count;
    } else {
      inCount += row.count;
    }
  }
  return { in: inCount, over: overCount };
});

const loadOverview = async () => {
  overview.value = await findTeamOverview(props.memberId);
};

const loadTable = async () => {
  tableLoading.value = true;
  try {
    const res = await findTeamMemberByPage({
      ancestorId: props.memberId,
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize,
      level: selectedLevel.value ?? undefined,
    });
    const rebateMax = overview.value?.rebateMaxLevel || 0;
    tableData.value = (res.content || []).map(row => ({
      ...row,
      overRebate: rebateMax > 0 && row.level > rebateMax,
    }));
    pagination.value.total = res.total || 0;
  } finally {
    tableLoading.value = false;
  }
};

const toggleLevel = (level: number | null) => {
  selectedLevel.value = level;
  pagination.value.current = 1;
  loadTable();
};

const onPageChange = (pageInfo: PageInfo) => {
  pagination.value.current = pageInfo.current;
  pagination.value.pageSize = pageInfo.pageSize;
  loadTable();
};

watch(
  () => [props.visible, props.memberId] as const,
  async ([visible, memberId]) => {
    if (!visible || !memberId) {
      overview.value = null;
      tableData.value = [];
      selectedLevel.value = null;
      return;
    }
    loading.value = true;
    selectedLevel.value = null;
    pagination.value.current = 1;
    try {
      await Promise.all([loadOverview(), loadTable()]);
    } finally {
      loading.value = false;
    }
  },
);
</script>

<style lang="less" scoped>
.team-overview {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  margin-bottom: 20px;
  background: var(--td-bg-color-secondarycontainer);
  border-radius: 6px;

  &__item {
    display: flex;
    flex-direction: column;
    align-items: center;
    flex: 1;
  }

  &__num {
    font-size: 24px;
    font-weight: 700;
    color: var(--td-text-color-primary);
    font-variant-numeric: tabular-nums;
  }

  &__label {
    margin-top: 4px;
    font-size: 13px;
    color: var(--td-text-color-secondary);
  }
}

.team-level-section {
  margin-bottom: 20px;

  &__head {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
  }

  &__title {
    font-size: 14px;
    font-weight: 600;
    color: var(--td-text-color-primary);
  }

  &__summary {
    margin-left: auto;
    font-size: 12px;
    color: var(--td-text-color-secondary);
  }
}

.team-level-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.team-level-row {
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;

  &:hover {
    background: var(--td-bg-color-secondarycontainer);
  }

  &--active {
    background: var(--td-brand-color-light);
    border-color: var(--td-brand-color);
  }

  &--over {
    opacity: 0.6;
  }

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 6px;
  }

  &__label {
    font-size: 13px;
    color: var(--td-text-color-primary);
  }

  &__count {
    font-size: 13px;
    color: var(--td-text-color-secondary);
    font-variant-numeric: tabular-nums;
  }

  &__bar {
    height: 6px;
    border-radius: 3px;
    background: var(--td-bg-color-component);
    overflow: hidden;
  }

  &__bar-fill {
    height: 100%;
    border-radius: 3px;
    background: var(--td-brand-color);
    transition: width 0.3s;
  }
}

.team-filter {
  margin-bottom: 16px;
}

.team-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
