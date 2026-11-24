<script setup lang="ts">
import { ref, watch } from 'vue';

import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import type { ConvertOrderRow } from '@/api/convertOrder';
import { findConvertOrderByPage } from '@/api/convertOrder';
import type { SpotOrderRow } from '@/api/spotOrder';
import { findSpotOrderByPage } from '@/api/spotOrder';
import type { SpotPlanOrderRow } from '@/api/spotPlanOrder';
import { findSpotPlanOrderByPage } from '@/api/spotPlanOrder';

import OrderNoCell from './order-no-cell.vue';
import { usePaged } from './use-paged';

const props = defineProps<{
  memberUid?: number | string;
}>();

type SubTab = 'order' | 'plan' | 'convert';
const activeSubTab = ref<SubTab>('order');

function uid(): number | undefined {
  const v = Number(props.memberUid);
  return v > 0 ? v : undefined;
}

/* ===== 订单 ===== */
const order = usePaged<SpotOrderRow>(async (page, pageSize) => {
  const id = uid();
  if (!id) {
    return { content: [], total: 0 };
  }
  return findSpotOrderByPage({ memberUid: id, pageNum: page, pageSize });
});

/** 现货订单状态标签配色（与 spot-order 页面一致）：1 已成交，其余灰色 */
function orderStatusTheme(status?: string): 'success' | 'default' {
  return status === '1' ? 'success' : 'default';
}

/** 与 spot-order 页面一致：交易对/方向合并，价格与数量为对比列 */
const orderColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'orderNo', title: '订单号', width: 180, ellipsis: true },
  { colKey: 'symbolDir', title: '交易对/方向' },
  { colKey: 'priceInfo', title: '价格' },
  { colKey: 'amountInfo', title: '数量' },
  { colKey: 'dealTotal', title: '成交额', align: 'right' },
  { colKey: 'statusInfo', title: '状态' },
  { colKey: 'createTime', title: '委托时间' },
];

/* ===== 计划委托 ===== */
const plan = usePaged<SpotPlanOrderRow>(async (page, pageSize) => {
  const id = uid();
  if (!id) {
    return { content: [], total: 0 };
  }
  return findSpotPlanOrderByPage({ memberUid: id, pageNum: page, pageSize });
});

/** 计划委托状态标签配色（与 spot-plan-order 页面一致）：1 已触发 / 2 已撤销 / 3 失败 / 其他 待触发 */
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

/** 与 spot-plan-order 页面一致：交易对/方向合并，触发条件 / 委托量 / 状态 / 委托时间为合并列 */
const planColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'orderNo', title: '订单号', width: 180, ellipsis: true },
  { colKey: 'symbolDir', title: '交易对/方向' },
  { colKey: 'triggerInfo', title: '触发条件' },
  { colKey: 'amountInfo', title: '委托量', align: 'right' },
  { colKey: 'statusInfo', title: '状态' },
  { colKey: 'timeInfo', title: '委托时间' },
];

/* ===== 闪兑订单 ===== */
const convert = usePaged<ConvertOrderRow>(async (page, pageSize) => {
  const id = uid();
  if (!id) {
    return { content: [], total: 0 };
  }
  return findConvertOrderByPage({ memberUid: id, pageNum: page, pageSize });
});

/** 与 convert-order 页面一致：兑换信息两行显示（金额行 + 汇率行） */
const convertColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'orderNo', title: '订单号', width: 180, ellipsis: true },
  { colKey: 'convertInfo', title: '兑换信息' },
  { colKey: 'feeInfo', title: '手续费' },
  { colKey: 'usdtValue', title: 'USDT价值', align: 'right' },
  { colKey: 'createTime', title: '兑换时间' },
];

/* ===== 懒加载 ===== */
const loadedMap = ref<Record<SubTab, boolean>>({ order: false, plan: false, convert: false });

const loaders: Record<SubTab, () => Promise<void>> = {
  order: () => order.load(),
  plan: () => plan.load(),
  convert: () => convert.load(),
};

const resetters: Record<SubTab, () => void> = {
  order: () => order.reset(),
  plan: () => plan.reset(),
  convert: () => convert.reset(),
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
      <t-tab-panel value="order" label="现货订单">
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
          <template #symbolDir="{ row }">
            <div class="symbol-dir">
              <span>{{ row.symbolCode || '—' }}</span>
              <t-tag size="small" variant="light" :theme="row.side === 'buy' ? 'success' : 'danger'">
                {{ row.sideName || row.side || '—' }}
              </t-tag>
            </div>
          </template>
          <template #priceInfo="{ row }">
            <div class="plan-stack">
              <div>委托价：{{ row.entrustType === 'market' ? '市价' : (row.price ?? '—') }}</div>
              <div>成交价：{{ row.dealPrice || '—' }}</div>
            </div>
          </template>
          <template #amountInfo="{ row }">
            <div class="plan-stack">
              <div>委托量：{{ row.amount ?? '—' }} {{ row.baseCurrencyCode || '' }}</div>
              <div>成交量：{{ row.dealAmount || '—' }} {{ row.baseCurrencyCode || '' }}</div>
            </div>
          </template>
          <template #dealTotal="{ row }">
            <span>{{ row.dealTotal || '—' }} {{ row.quoteCurrencyCode || '' }}</span>
          </template>
          <template #statusInfo="{ row }">
            <t-tag size="small" variant="light" :theme="orderStatusTheme(row.status)">
              {{ row.statusName || row.status || '—' }}
            </t-tag>
          </template>
        </t-table>
      </t-tab-panel>

      <t-tab-panel value="plan" label="现货计划委托" lazy>
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
          <template #symbolDir="{ row }">
            <div class="symbol-dir">
              <span>{{ row.symbolCode || '—' }}</span>
              <t-tag size="small" variant="light" :theme="row.side === 'buy' ? 'success' : 'danger'">
                {{ row.sideName || row.side || '—' }}
              </t-tag>
            </div>
          </template>
          <template #triggerInfo="{ row }">
            <div class="plan-stack">
              <div>触发价：{{ row.triggerType === 'ge' ? '≥' : '≤' }} {{ row.triggerPrice ?? '—' }}</div>
              <div>委托价：{{ row.execType === 'market' ? '市价' : `${row.price ?? '—'} ${row.quoteCurrencyCode || ''}` }}</div>
            </div>
          </template>
          <template #amountInfo="{ row }">
            <div class="plan-stack">
              <div>{{ row.amount ?? '—' }} {{ row.baseCurrencyCode || '' }}</div>
            </div>
          </template>
          <template #statusInfo="{ row }">
            <t-tag size="small" variant="light" :theme="planStatusTheme(row.status)">
              {{ row.statusName || row.status || '—' }}
            </t-tag>
          </template>
          <template #timeInfo="{ row }">
            <span>{{ row.createTime || '—' }}</span>
          </template>
        </t-table>
      </t-tab-panel>

      <t-tab-panel value="convert" label="闪兑订单" lazy>
        <t-table
          :data="convert.rows.value"
          :columns="convertColumns"
          row-key="id"
          size="small"
          table-layout="auto"
          bordered
          :loading="convert.loading.value"
          :pagination="convert.pagination.value"
          @page-change="convert.onPageChange"
        >
          <template #orderNo="{ row }">
            <OrderNoCell :value="row.orderNo" />
          </template>
          <template #convertInfo="{ row }">
            <div class="convert-info">
              <div class="convert-info__main">
                <span>{{ row.fromAmount }}</span>
                <span class="convert-info__unit">{{ row.fromAssetCode }}</span>
                <span class="convert-info__arrow">→</span>
                <span>{{ row.toAmount }}</span>
                <span class="convert-info__unit">{{ row.toAssetCode }}</span>
              </div>
              <div class="convert-info__sub">
                1 {{ row.fromAssetCode }} = {{ row.exchangeRate }} {{ row.toAssetCode }}
              </div>
            </div>
          </template>
          <template #feeInfo="{ row }">
            <div class="convert-fee">
              <div>
                <span class="convert-fee__amount">{{ row.feeInAsset }}</span>
                <span class="convert-info__unit">{{ row.toAssetCode }}</span>
              </div>
              <div class="convert-fee__usdt">= {{ row.feeUsdtAmount }} USDT</div>
            </div>
          </template>
          <template #usdtValue="{ row }">
            <span>{{ row.usdtValue }}</span>
            <span class="convert-info__unit">USDT</span>
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

/* 计划委托：合并列多行堆叠 */
.plan-stack {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  font-size: 13px;
}

/* 闪兑：兑换信息两行式（金额行 + 汇率行） */
.convert-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.convert-info__main {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
  white-space: nowrap;
}

.convert-info__unit {
  font-size: 12px;
  color: var(--td-text-color-secondary);
}

.convert-info__arrow {
  color: var(--td-text-color-placeholder);
}

.convert-info__sub {
  font-size: 12px;
  color: var(--td-text-color-secondary);
  white-space: nowrap;
}

/* 闪兑：手续费两行（资产数量 + USDT 等价） */
.convert-fee {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.convert-fee__amount {
  font-weight: 500;
}

.convert-fee__usdt {
  font-size: 12px;
  color: var(--td-text-color-placeholder);
}
</style>
