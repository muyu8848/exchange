<script setup lang="ts">
import { computed, ref, watch } from 'vue';

import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import { findTeamMemberByPage, findTeamOverview } from '@/api/member';
import type { TeamMemberRow, TeamOverview } from '@/api/member';
import CopyIconButton from '@/components/copy-icon-button/index.vue';

import { usePaged } from './use-paged';

const props = defineProps<{
  /** 会员账号 ID（memberAccountId），团队接口按它查询 */
  memberId?: string;
}>();

const overview = ref<TeamOverview | null>(null);
const overviewLoading = ref(false);

/** 层级筛选，null 表示全部 */
const selectedLevel = ref<number | null>(null);

/** 概览指标 */
const summaryItems = computed(() => {
  const teamTotal = overview.value?.teamTotalCount || 0;
  const direct = overview.value?.directCount || 0;
  return [
    { label: '团队总人数', value: teamTotal },
    { label: '直推人数', value: direct },
    { label: '间接人数', value: Math.max(teamTotal - direct, 0) },
    { label: '今日新增', value: overview.value?.todayNewCount || 0 },
  ];
});

/** 层级分布（含返佣层级外的标记） */
const levelRows = computed(() => {
  const counts = overview.value?.levelCounts;
  if (!counts?.length) {
    return [];
  }
  const teamTotal = overview.value?.teamTotalCount || 0;
  const rebateMax = overview.value?.rebateMaxLevel || 0;
  return counts
    .slice()
    .sort((a, b) => (a.level ?? 0) - (b.level ?? 0))
    .map((c) => {
      const lv = c.level ?? 0;
      return {
        level: lv,
        label: lv === 1 ? '第1级（直推）' : `第${lv}级`,
        count: c.count ?? 0,
        percent: teamTotal > 0 ? Math.round(((c.count ?? 0) / teamTotal) * 100) : 0,
        overRebate: rebateMax > 0 && lv > rebateMax,
      };
    });
});

/** 返佣层级内外人数汇总 */
const rebateSummary = computed(() => {
  const rebateMax = overview.value?.rebateMaxLevel || 0;
  let inCount = 0;
  let overCount = 0;
  for (const row of levelRows.value) {
    if (rebateMax > 0 && row.level > rebateMax) {
      overCount += row.count;
    }
    else {
      inCount += row.count;
    }
  }
  return { in: inCount, over: overCount };
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
  { colKey: 'uid', title: '账号' },
  { colKey: 'nickName', title: '昵称'},
  { colKey: 'levelTag', title: '层级' },
  { colKey: 'status', title: '状态' },
  { colKey: 'createTime', title: '注册时间' },
];

async function loadOverview() {
  if (!props.memberId) {
    overview.value = null;
    return;
  }
  overviewLoading.value = true;
  try {
    overview.value = await findTeamOverview(props.memberId);
  }
  finally {
    overviewLoading.value = false;
  }
}

const {
  rows,
  loading,
  pagination,
  load: loadMembers,
  onPageChange,
  reset: resetMembers,
} = usePaged<TeamMemberRow & { overRebate?: boolean }>(async (page, pageSize) => {
  if (!props.memberId) {
    return { content: [], total: 0 };
  }
  const res = await findTeamMemberByPage({
    ancestorId: props.memberId,
    pageNum: page,
    pageSize,
    level: selectedLevel.value ?? undefined,
  });
  const rebateMax = overview.value?.rebateMaxLevel || 0;
  return {
    content: (res?.content || []).map(r => ({
      ...r,
      overRebate: rebateMax > 0 && r.level > rebateMax,
    })),
    total: res?.total || 0,
  };
});

function toggleLevel(level: number | null) {
  selectedLevel.value = level;
  resetMembers();
  loadMembers();
}

watch(
  () => props.memberId,
  () => {
    resetMembers();
    selectedLevel.value = null;
    loadOverview();
    loadMembers();
  },
  { immediate: true },
);
</script>

<template>
  <div class="tab-body">
    <t-loading :loading="overviewLoading" size="small">
      <div class="summary">
        <div v-for="item in summaryItems" :key="item.label" class="summary__item">
          <span class="summary__label">{{ item.label }}</span>
          <span class="summary__value">{{ item.value }}</span>
        </div>
      </div>
    </t-loading>

    <!-- 层级分布 -->
    <div v-if="levelRows.length" class="level-section">
      <div class="level-section__head">
        <span class="level-section__title">层级分布</span>
        <t-tag v-if="overview?.rebateMaxLevel" size="small" theme="primary">
          {{ overview.rebateMaxLevel === 1 ? '仅直推返佣' : `返佣${overview.rebateMaxLevel}级` }}
        </t-tag>
        <span v-if="rebateSummary.over > 0" class="level-section__summary">
          返佣层级内 {{ rebateSummary.in }}人 · 超出 {{ rebateSummary.over }}人
        </span>
      </div>
      <div class="level-list">
        <div
          v-for="row in levelRows"
          :key="row.level"
          class="level-row"
          :class="{ 'level-row--active': selectedLevel === row.level, 'level-row--over': row.overRebate }"
          @click="toggleLevel(row.level)"
        >
          <div class="level-row__head">
            <span class="level-row__label">{{ row.label }}</span>
            <span class="level-row__count">{{ row.count }}人（{{ row.percent }}%）</span>
          </div>
          <div class="level-row__bar">
            <div class="level-row__bar-fill" :style="{ width: `${row.percent}%` }" />
          </div>
        </div>
      </div>
    </div>

    <!-- 层级筛选 -->
    <div class="filter-row">
      <t-space>
        <t-button
          :theme="selectedLevel === null ? 'primary' : 'default'"
          variant="outline"
          size="small"
          @click="toggleLevel(null)"
        >
          全部
        </t-button>
        <t-button
          v-for="row in levelRows"
          :key="row.level"
          :theme="selectedLevel === row.level ? 'primary' : 'default'"
          variant="outline"
          size="small"
          @click="toggleLevel(row.level)"
        >
          {{ row.label }}
        </t-button>
      </t-space>
    </div>

    <!-- 成员表格 -->
    <t-table
      :data="rows"
      :columns="columns"
      row-key="id"
      size="small"
      table-layout="auto"
      bordered
      :loading="loading"
      :pagination="pagination"
      @page-change="onPageChange"
    >
      <template #uid="{ row }">
        <div class="team-cell">
          <span>{{ row.uid ?? '—' }}</span>
          <CopyIconButton v-if="row.uid != null" :text="String(row.uid)" />
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
  </div>
</template>

<style lang="less" scoped>
.tab-body {
  min-height: 200px;
}

/* ===== 概览 ===== */
.summary {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.summary__item {
  flex: 1;
  min-width: 140px;
  padding: 12px 16px;
  border: 1px solid var(--td-component-stroke);
  border-radius: 6px;
  background: var(--td-bg-color-container);
}

.summary__label {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--td-text-color-secondary);
}

.summary__value {
  font-size: 20px;
  font-weight: 600;
  color: var(--td-text-color-primary);
  font-variant-numeric: tabular-nums;
}

/* ===== 层级分布 ===== */
.level-section {
  margin-bottom: 20px;
}

.level-section__head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.level-section__title {
  font-size: 14px;
  font-weight: 600;
  color: var(--td-text-color-primary);
}

.level-section__summary {
  margin-left: auto;
  font-size: 12px;
  color: var(--td-text-color-secondary);
}

.level-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
}

.level-row {
  flex: 1;
  min-width: 220px;
  padding: 8px 12px;
  border: 1px solid transparent;
  border-radius: 4px;
  cursor: pointer;
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

/* ===== 筛选 ===== */
.filter-row {
  margin-bottom: 16px;
}

/* ===== 表格 ===== */
.team-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
