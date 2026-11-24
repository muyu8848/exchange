<script setup lang="ts">
import { ref, watch } from 'vue';

import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import type { SecondContractOrderRow } from '@/api/secondContractOrder';
import { findSecondContractOrderByPage } from '@/api/secondContractOrder';

import OrderNoCell from './order-no-cell.vue';
import { usePaged } from './use-paged';

const props = defineProps<{
  memberUid?: number | string;
}>();

/** 与其他交易 tab 保持一致：即使只有一个二级项，也保留二级 tab 结构 */
const activeSubTab = ref<'order'>('order');

const sc = usePaged<SecondContractOrderRow>(async (page, pageSize) => {
  const uid = Number(props.memberUid);
  if (!uid) {
    return { content: [], total: 0 };
  }
  return findSecondContractOrderByPage({ memberUid: uid, pageNum: page, pageSize });
});

/** 方向标签配色：long 绿 / short 红（与 second-contract-order 页面一致） */
function directionTheme(direction?: string): 'success' | 'danger' | 'default' {
  if (direction === 'long') {
    return 'success';
  }
  if (direction === 'short') {
    return 'danger';
  }
  return 'default';
}

/** 订单状态配色：1 持仓中 / 2 已平仓 / 其他灰色 */
function statusTheme(status?: string): 'warning' | 'success' | 'default' {
  if (status === '1') {
    return 'warning';
  }
  if (status === '2') {
    return 'success';
  }
  return 'default';
}

/** 盈亏配色：正绿负红 */
function pnlClass(pnl?: number | null): string {
  if (pnl == null) {
    return '';
  }
  if (pnl > 0) {
    return 'pnl-profit';
  }
  if (pnl < 0) {
    return 'pnl-loss';
  }
  return '';
}

/** 与 second-contract-order 页面一致：交易对/方向合并，开平仓价格合并，去掉赔率与平仓时间 */
const columns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'orderNo', title: '订单号', width: 180 },
  { colKey: 'symbolDir', title: '交易对/方向' },
  { colKey: 'amount', title: '下注金额', align: 'right' },
  { colKey: 'priceFlow', title: '开仓→平仓', align: 'right' },
  { colKey: 'period', title: '合约周期', align: 'right' },
  { colKey: 'pnlInfo', title: '盈亏', align: 'right' },
  { colKey: 'statusInfo', title: '状态' },
  { colKey: 'openTime', title: '开仓时间' },
];

watch(
  () => props.memberUid,
  () => {
    sc.reset();
    sc.load();
  },
  { immediate: true },
);
</script>

<template>
  <div class="tab-body">
    <t-tabs v-model="activeSubTab">
      <t-tab-panel value="order" label="秒合约订单">
        <t-table
          :data="sc.rows.value"
          :columns="columns"
          row-key="id"
          size="small"
          table-layout="auto"
          bordered
          :loading="sc.loading.value"
          :pagination="sc.pagination.value"
          @page-change="sc.onPageChange"
        >
          <template #orderNo="{ row }">
            <OrderNoCell :value="row.orderNo" />
          </template>
          <template #symbolDir="{ row }">
            <div class="symbol-dir">
              <span>{{ row.symbolCode || '—' }}</span>
              <t-tag size="small" variant="light" :theme="directionTheme(row.direction)">
                {{ row.directionName || '—' }}
              </t-tag>
            </div>
          </template>
          <template #amount="{ row }">
            <span class="mono">{{ row.amount ?? '—' }}</span>
          </template>
          <template #priceFlow="{ row }">
            <span class="price-flow">
              <span class="mono">{{ row.openPrice ?? '—' }}</span>
              <span class="price-flow__arrow">→</span>
              <span v-if="row.closePrice != null" class="mono">{{ row.closePrice }}</span>
              <span v-else class="muted">—</span>
            </span>
          </template>
          <template #period="{ row }">
            <span class="mono">{{ row.period ? `${row.period}s` : '—' }}</span>
          </template>
          <template #pnlInfo="{ row }">
            <span :class="pnlClass(row.pnl)">{{ row.pnl ?? '—' }}</span>
          </template>
          <template #statusInfo="{ row }">
            <t-tag size="small" variant="light" :theme="statusTheme(row.status)">
              {{ row.statusName || row.status || '—' }}
            </t-tag>
          </template>
        </t-table>
      </t-tab-panel>
    </t-tabs>
  </div>
</template>

<style lang="less" scoped>
.tab-body {
  min-height: 200px;
}

/* 二级 tab 与表格之间留出间距 */
.tab-body :deep(.t-tabs__content) {
  padding-top: 12px;
}

/* 交易对 + 方向合并列 */
.symbol-dir {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 开仓 → 平仓 */
.price-flow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.price-flow__arrow {
  font-size: 12px;
  color: var(--td-text-color-placeholder);
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
}

.muted {
  color: var(--td-text-color-placeholder);
}

.pnl-profit {
  color: var(--td-success-color);
  font-weight: 600;
}

.pnl-loss {
  color: var(--td-error-color);
  font-weight: 600;
}
</style>
