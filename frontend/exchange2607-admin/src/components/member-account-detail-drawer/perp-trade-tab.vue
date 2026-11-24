<script setup lang="ts">
import { ref, watch } from 'vue';

import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import type { PerpetualFundingFeeRow } from '@/api/perpetualFundingFee';
import { findPerpetualFundingFeeByPage } from '@/api/perpetualFundingFee';
import type { PerpetualOrderRow } from '@/api/perpetualOrder';
import { findPerpetualOrdersByPage } from '@/api/perpetualOrder';
import type { PerpetualPlanOrderRow } from '@/api/perpetualPlanOrder';
import { findPerpetualPlanOrderByPage } from '@/api/perpetualPlanOrder';
import type { PerpetualPositionRow } from '@/api/perpetualPosition';
import { findPerpetualPositionsByPage } from '@/api/perpetualPosition';

import OrderNoCell from './order-no-cell.vue';
import { usePaged } from './use-paged';

const props = defineProps<{
  memberUid?: number | string;
}>();

type SubTab = 'position' | 'order' | 'plan' | 'funding';
const activeSubTab = ref<SubTab>('position');

function uid(): number | undefined {
  const v = Number(props.memberUid);
  return v > 0 ? v : undefined;
}

/** 订单类型标签配色，与 perpetual-order 页面保持一致 */
function orderTypeTheme(type?: string): 'primary' | 'success' | 'danger' | 'default' {
  if (type === 'open') {
    return 'primary';
  }
  if (type === 'close') {
    return 'success';
  }
  if (type === 'liquidate') {
    return 'danger';
  }
  return 'default';
}



/** 持仓状态标签配色：1 持仓中 / 2 已平仓 / 3 已强平（与 perpetual-position 页面一致） */
function statusTheme(status?: string): 'warning' | 'success' | 'danger' {
  if (status === '1') {
    return 'warning';
  }
  if (status === '2') {
    return 'success';
  }
  if (status === '3') {
    return 'danger';
  }
  return 'warning';
}

/** 订单状态配色（与 perpetual-order 页面一致）：0 待成交 / 1 已成交 / 2 已撤销 */
function orderStatusTheme(status?: string): 'warning' | 'success' | 'default' {
  if (status === '0') {
    return 'warning';
  }
  if (status === '1') {
    return 'success';
  }
  if (status === '2') {
    return 'default';
  }
  return 'warning';
}

/** 计划委托状态配色（与 perpetual-plan-order 页面一致）：1 已触发 / 2 已撤销 / 3 失败 / 其他 待触发 */
function planStatusTheme(status?: string): 'success' | 'default' | 'danger' | 'warning' {
  if (status === '1') {
    return 'success';
  }
  if (status === '2') {
    return 'default';
  }
  if (status === '3') {
    return 'danger';
  }
  return 'warning';
}

/** 方向标签配色：long 绿 / short 红 */
function directionTheme(direction?: string): 'success' | 'danger' | 'default' {
  if (direction === 'long') {
    return 'success';
  }
  if (direction === 'short') {
    return 'danger';
  }
  return 'default';
}

/** 费率格式化：小数转百分比（如 0.0001 → 0.0100%） */
function formatRate(value?: number): string {
  const num = Number(value ?? 0);
  return Number.isFinite(num) ? `${(num * 100).toFixed(4)}%` : '0.0000%';
}

/** 计划委托来源配色：止盈绿、止损红、普通计划单中性 */
function sourceTheme(source?: string): 'success' | 'danger' | 'default' {
  if (source === 'tp') {
    return 'success';
  }
  if (source === 'sl') {
    return 'danger';
  }
  return 'default';
}

/** 盈亏配色：正绿负红 */
function pnlClass(val?: number | null): string {
  if (val == null) {
    return '';
  }
  if (val > 0) {
    return 'pnl-profit';
  }
  if (val < 0) {
    return 'pnl-loss';
  }
  return '';
}

/* ===== 0. 永续持仓 ===== */
const position = usePaged<PerpetualPositionRow>(async (page, pageSize) => {
  const id = uid();
  if (!id) {
    return { content: [], total: 0 };
  }
  // 显示全部状态（含已平仓 / 已强平），便于判断历史风险
  return findPerpetualPositionsByPage({ memberUid: id, pageNum: page, pageSize });
});

const positionColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'orderNo', title: '订单号', width: 180, ellipsis: true },
  { colKey: 'symbolCode', title: '交易对' },
  { colKey: 'direction', title: '方向/杠杆/状态' },
  { colKey: 'openPrice', title: '开仓/标记价' },
  { colKey: 'marginRatio', title: '仓位/保证金' },
  { colKey: 'unrealizedPnl', title: '盈亏' },
  { colKey: 'openTime', title: '开仓时间' },
];

/* ===== 1. 永续订单 ===== */
const order = usePaged<PerpetualOrderRow>(async (page, pageSize) => {
  const id = uid();
  if (!id) {
    return { content: [], total: 0 };
  }
  return findPerpetualOrdersByPage({ memberUid: id, pageNum: page, pageSize });
});

const orderColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'orderNo', title: '订单号', width: 180, ellipsis: true },
  { colKey: 'symbolCode', title: '交易对' },
  { colKey: 'type', title: '订单类型' },
  { colKey: 'direction', title: '方向/委托/杠杆' },
  { colKey: 'filledSize', title: '成交量', align: 'right' },
  { colKey: 'avgPrice', title: '成交均价', align: 'right' },
  { colKey: 'status', title: '状态' },
  { colKey: 'createTime', title: '下单时间' },
];

/* ===== 2. 永续计划委托 ===== */
const plan = usePaged<PerpetualPlanOrderRow>(async (page, pageSize) => {
  const id = uid();
  if (!id) {
    return { content: [], total: 0 };
  }
  return findPerpetualPlanOrderByPage({ memberUid: id, pageNum: page, pageSize });
});

/** 与 perpetual-plan-order 页面一致：类型/方向合并、新增来源、触发条件合并、状态 tag */
const planColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'orderNo', title: '订单号', width: 180 },
  { colKey: 'symbolCode', title: '交易对' },
  { colKey: 'direction', title: '类型/方向' },
  { colKey: 'source', title: '来源' },
  { colKey: 'triggerInfo', title: '触发条件', align: 'right' },
  { colKey: 'statusInfo', title: '状态' },
  { colKey: 'createTime', title: '委托时间' },
];

/* ===== 3. 资金费记录 ===== */
const funding = usePaged<PerpetualFundingFeeRow>(async (page, pageSize) => {
  const id = uid();
  if (!id) {
    return { content: [], total: 0 };
  }
  return findPerpetualFundingFeeByPage({ memberUid: id, pageNum: page, pageSize });
});

/** 与 perpetual-funding-fee 页面一致：时间列置末，金额/费率做格式化 */
const fundingColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'symbolCode', title: '交易对' },
  { colKey: 'directionName', title: '方向' },
  {
    colKey: 'notional',
    title: '名义价值',
    align: 'right',
    cell: (_h, { row }) => row.notional ?? '—',
  },
  {
    colKey: 'fundingRate',
    title: '费率',
    align: 'right',
    cell: (_h, { row }) => formatRate(row.fundingRate),
  },
  { colKey: 'fundingAmount', title: '资金费', align: 'right' },
  { colKey: 'fundingTime', title: '结算时间' },
];

/* ===== 懒加载 ===== */
const loadedMap = ref<Record<SubTab, boolean>>({
  position: false,
  order: false,
  plan: false,
  funding: false,
});

const loaders: Record<SubTab, () => Promise<void>> = {
  position: () => position.load(),
  order: () => order.load(),
  plan: () => plan.load(),
  funding: () => funding.load(),
};

const resetters: Record<SubTab, () => void> = {
  position: () => position.reset(),
  order: () => order.reset(),
  plan: () => plan.reset(),
  funding: () => funding.reset(),
};

function ensureLoaded(tab: SubTab) {
  if (!loadedMap.value[tab]) {
    loadedMap.value[tab] = true;
    loaders[tab]();
  }
}

watch(activeSubTab, tab => ensureLoaded(tab));

watch(
  () => props.memberUid,
  () => {
    (Object.keys(loadedMap.value) as SubTab[]).forEach((tab) => {
      loadedMap.value[tab] = false;
      resetters[tab]();
    });
    ensureLoaded(activeSubTab.value);
  },
  { immediate: true },
);
</script>

<template>
  <div class="tab-body">
    <t-tabs v-model="activeSubTab">
      <!-- 持仓 -->
      <t-tab-panel value="position" label="永续持仓">
        <t-table
          :data="position.rows.value"
          :columns="positionColumns"
          row-key="id"
          size="small"
          table-layout="auto"
          bordered
          :loading="position.loading.value"
          :pagination="position.pagination.value"
          @page-change="position.onPageChange"
        >
          <template #orderNo="{ row }">
            <OrderNoCell :value="row.orderNo" />
          </template>
          <template #direction="{ row }">
            <div class="order-tags">
              <t-tag size="small" variant="light" :theme="row.direction === 'long' ? 'success' : 'danger'">
                {{ row.directionName || '—' }}
              </t-tag>
              <t-tag size="small" variant="outline">{{ row.leverage ? `${row.leverage}x` : '—' }}</t-tag>
              <t-tag size="small" variant="light" :theme="statusTheme(row.status)">
                {{ row.statusName || '—' }}
              </t-tag>
            </div>
          </template>
          <template #openPrice="{ row }">
            <div class="stack-cell">
              <div class="stack-row">
                <span class="stack-label">开仓</span>
                <span class="stack-value">{{ row.openPrice ?? '—' }}</span>
              </div>
              <div class="stack-row">
                <span class="stack-label">标记</span>
                <span class="stack-value">{{ row.markPrice ?? '—' }}</span>
              </div>
            </div>
          </template>
          <template #marginRatio="{ row }">
            <div class="stack-cell">
              <div class="stack-row">
                <span class="stack-label">仓位大小</span>
                <span class="stack-value">{{ row.size ?? '—' }}</span>
              </div>
              <div class="stack-row">
                <span class="stack-label">保证金</span>
                <span class="stack-value">{{ row.margin ?? '—' }}</span>
              </div>
            </div>
          </template>
          <template #unrealizedPnl="{ row }">
            <div class="stack-cell">
              <div class="stack-row">
                <span class="stack-label">未实现</span>
                <span class="stack-value" :class="pnlClass(row.unrealizedPnl)">
                  {{ row.unrealizedPnl ?? '—' }}
                </span>
              </div>
              <div class="stack-row">
                <span class="stack-label">已实现</span>
                <span class="stack-value" :class="pnlClass(row.realizedPnl)">
                  {{ row.realizedPnl ?? '—' }}
                </span>
              </div>
            </div>
          </template>
        </t-table>
      </t-tab-panel>

      <!-- 订单 -->
      <t-tab-panel value="order" label="永续合约订单" lazy>
        <t-table
          :data="order.rows.value"
          :columns="orderColumns"
          row-key="id"
          size="small"
          table-layout="auto"
          bordered
          :loading="order.loading.value"
          :pagination="order.pagination.value"
          @page-change="order.onPageChange"
        >
          <template #orderNo="{ row }">
            <OrderNoCell :value="row.orderNo" />
          </template>
          <template #type="{ row }">
            <t-tag size="small" variant="light" :theme="orderTypeTheme(row.type)">
              {{ row.typeName || '—' }}
            </t-tag>
          </template>
          <template #direction="{ row }">
            <div class="order-tags">
              <t-tag size="small" variant="light" :theme="row.direction === 'long' ? 'success' : 'danger'">
                {{ row.directionName || '—' }}
              </t-tag>
              <t-tag size="small" variant="outline" theme="warning">{{ row.entrustTypeName || '—' }}</t-tag>
              <t-tag size="small" variant="outline">{{ row.leverage ? `${row.leverage}x` : '—' }}</t-tag>
            </div>
          </template>
          <template #filledSize="{ row }">
            {{ row.filledSize == null ? '—' : row.filledSize }}
          </template>
          <template #pnl="{ row }">
            <span v-if="row.pnl != null" :style="{ color: Number(row.pnl) >= 0 ? '#2ba471' : '#d54941' }">
              {{ row.pnl }}
            </span>
            <span v-else>—</span>
          </template>
          <template #status="{ row }">
            <t-tag size="small" variant="light" :theme="orderStatusTheme(row.status)">
              {{ row.statusName || row.status || '—' }}
            </t-tag>
          </template>
        </t-table>
      </t-tab-panel>

      <!-- 计划委托 -->
      <t-tab-panel value="plan" label="永续计划委托" lazy>
        <t-table
          :data="plan.rows.value"
          :columns="planColumns"
          row-key="id"
          size="small"
          table-layout="auto"
          bordered
          :loading="plan.loading.value"
          :pagination="plan.pagination.value"
          @page-change="plan.onPageChange"
        >
          <template #orderNo="{ row }">
            <OrderNoCell :value="row.orderNo" />
          </template>
          <template #direction="{ row }">
            <div class="order-tags">
              <t-tag size="small" variant="light" :theme="row.type === 'open' ? 'primary' : 'warning'">
                {{ row.typeName || '—' }}
              </t-tag>
              <t-tag size="small" variant="light" :theme="row.direction === 'long' ? 'success' : 'danger'">
                {{ row.directionName || '—' }}
              </t-tag>
            </div>
          </template>
          <template #source="{ row }">
            <t-tag v-if="row.sourceName" size="small" variant="light" :theme="sourceTheme(row.source)">
              {{ row.sourceName }}
            </t-tag>
            <span v-else class="table-muted">—</span>
          </template>
          <template #triggerInfo="{ row }">
            <div class="plan-stack">
              <div>触发价：{{ row.triggerType === 'ge' ? '≥' : '≤' }} {{ row.triggerPrice ?? '—' }}</div>
              <div>执行：{{ row.execType === 'market' ? '市价' : `限价 ${row.price ?? '—'}` }}</div>
            </div>
          </template>
          <template #statusInfo="{ row }">
            <t-tag size="small" variant="light" :theme="planStatusTheme(row.status)">
              {{ row.statusName || row.status || '—' }}
            </t-tag>
          </template>
        </t-table>
      </t-tab-panel>

      <!-- 资金费 -->
      <t-tab-panel value="funding" label="资金费记录" lazy>
        <t-table
          :data="funding.rows.value"
          :columns="fundingColumns"
          row-key="id"
          size="small"
          table-layout="auto"
          bordered
          :loading="funding.loading.value"
          :pagination="funding.pagination.value"
          @page-change="funding.onPageChange"
        >
          <template #directionName="{ row }">
            <t-tag size="small" variant="light" :theme="directionTheme(row.direction)">
              {{ row.directionName || '—' }}
            </t-tag>
          </template>
          <template #fundingAmount="{ row }">
            <span :class="Number(row.fundingAmount) > 0 ? 'pnl-profit' : Number(row.fundingAmount) < 0 ? 'pnl-loss' : ''">
              {{ row.fundingAmount ?? '—' }}
            </span>
          </template>
          <template #fundingTime="{ row }">{{ row.fundingTime || row.createTime || '—' }}</template>
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

.order-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}

/* 计划委托：触发条件多行堆叠 */
.plan-stack {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  font-size: 13px;
}

.table-muted {
  color: var(--td-text-color-placeholder);
}

/* 持仓：多行单元格（与 perpetual-position 页面一致） */
.stack-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stack-row {
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.stack-label {
  color: var(--td-text-color-placeholder);
  font-size: 12px;
}

.stack-value {
  color: var(--td-text-color-primary);
  font-weight: 500;
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
