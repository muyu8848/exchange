<template>
  <div class="dashboard-page">
    <t-card :bordered="false" class="filter-card">
      <t-form layout="inline" :label-width="70">
        <t-form-item label="统计时间">
          <t-date-range-picker
            v-model="dateRange"
            class="date-range"
            :presets="tableDateOnlyRangePresets"
            allow-input
            clearable
          />
        </t-form-item>
        <t-form-item label="交易对">
          <t-select
            v-model="queryForm.symbolCode"
            clearable
            filterable
            :options="symbolOptions"
            placeholder="全部"
            style="width: 160px"
          />
        </t-form-item>
        <t-form-item label="方向">
          <t-select
            v-model="queryForm.side"
            clearable
            :options="sideOptions"
            placeholder="全部"
            style="width: 120px"
          />
        </t-form-item>
        <t-form-item>
          <t-button theme="primary" :loading="loading" @click="loadData">查询</t-button>
        </t-form-item>
      </t-form>
    </t-card>

    <div class="metric-grid">
      <t-card v-for="metric in metrics" :key="metric.key" :bordered="false" class="metric-card">
        <div class="metric-card__title">{{ metric.title }}</div>
        <div class="metric-card__value" :class="metric.valueClass">{{ metric.value }}</div>
        <div class="metric-card__hint">{{ metric.hint }}</div>
      </t-card>
    </div>

    <t-card :bordered="false" class="chart-card">
      <template #title>现货趋势</template>
      <div ref="chartRef" class="chart"></div>
    </t-card>

    <t-card :bordered="false" class="table-card">
      <template #title>按日明细</template>
      <t-table
        row-key="date"
        :data="dailyList"
        :columns="dailyColumns"
        table-layout="auto"
        :hover="true"
        :loading="loading"
      />
    </t-card>
  </div>
</template>

<script lang="ts">
export default { name: 'SpotDashboard' };
</script>

<script setup lang="ts">
import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import dayjs from 'dayjs';
import * as echarts from 'echarts/core';
import type { ECharts, EChartsCoreOption } from 'echarts/core';
import { LineChart } from 'echarts/charts';
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';

import type { SpotSymbolRow } from '@/api/spotSymbol';
import { findAllSpotSymbol } from '@/api/spotSymbol';
import type {
  SpotDashboardDailyItem,
  SpotDashboardOverview,
  SpotDashboardTrendItem,
} from '@/api/spotDashboard';
import { getSpotDashboardDaily, getSpotDashboardSummary } from '@/api/spotDashboard';
import { buildDateRangeQuery, tableDateOnlyRangePresets } from '@/utils/dateRangePresets';

echarts.use([LineChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer]);

const loading = ref(false);
const dateRange = ref<string[]>([
  dayjs().subtract(6, 'day').format('YYYY-MM-DD'),
  dayjs().format('YYYY-MM-DD'),
]);
const queryForm = ref({ symbolCode: '', side: '' });
const sideOptions = [
  { label: '买入', value: 'buy' },
  { label: '卖出', value: 'sell' },
];
const symbolOptions = ref<{ label: string; value: string }[]>([]);

const overview = ref<SpotDashboardOverview | null>(null);
const trend = ref<SpotDashboardTrendItem[]>([]);
const dailyList = ref<SpotDashboardDailyItem[]>([]);

const chartRef = ref<HTMLDivElement | null>(null);
let chart: ECharts | null = null;

function formatAmount(value?: number | string | null): string {
  const num = Number(value ?? 0);
  if (!Number.isFinite(num)) {
    return '0.00';
  }
  return num.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

const metrics = computed(() => {
  const data = overview.value;
  return [
    {
      key: 'dealCount',
      title: '成交笔数',
      value: `${data?.dealCount ?? 0} 笔`,
      hint: '按成交时间统计',
      valueClass: '',
    },
    {
      key: 'buyDealTotal',
      title: '买入成交额 (USDT)',
      value: formatAmount(data?.buyDealTotal),
      hint: '双边口径',
      valueClass: '',
    },
    {
      key: 'sellDealTotal',
      title: '卖出成交额 (USDT)',
      value: formatAmount(data?.sellDealTotal),
      hint: '双边口径',
      valueClass: '',
    },
    {
      key: 'totalDealTotal',
      title: '总成交额 (USDT)',
      value: formatAmount(data?.totalDealTotal),
      hint: '买入 + 卖出',
      valueClass: '',
    },
    {
      key: 'feeUsdtAmount',
      title: '手续费收入 (USDT)',
      value: formatAmount(data?.feeUsdtAmount),
      hint: '按成交时间统计',
      valueClass: '',
    },
    {
      key: 'pendingPlanCount',
      title: '计划委托中',
      value: `${data?.pendingPlanCount ?? 0} 笔`,
      hint: '当前快照',
      valueClass: '',
    },
    {
      key: 'pendingPlanAmount',
      title: '待触发金额 (USDT)',
      value: formatAmount(data?.pendingPlanAmount),
      hint: '当前快照，触发价×数量估算',
      valueClass: '',
    },
  ];
});

const dailyColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'date', title: '时间' },
  { colKey: 'dealCount', title: '成交笔数', align: 'right' },
  {
    colKey: 'buyDealTotal',
    title: '买入成交额 (USDT)',
    align: 'right',
    cell: (_h, { row }) => formatAmount(row.buyDealTotal),
  },
  {
    colKey: 'sellDealTotal',
    title: '卖出成交额 (USDT)',
    align: 'right',
    cell: (_h, { row }) => formatAmount(row.sellDealTotal),
  },
  {
    colKey: 'totalDealTotal',
    title: '总成交额 (USDT)',
    align: 'right',
    cell: (_h, { row }) => formatAmount(row.totalDealTotal),
  },
  {
    colKey: 'feeUsdtAmount',
    title: '手续费 (USDT)',
    align: 'right',
    cell: (_h, { row }) => formatAmount(row.feeUsdtAmount),
  },
];

function renderChart() {
  if (!chartRef.value) {
    return;
  }
  if (!chart) {
    chart = echarts.init(chartRef.value);
  }
  const option: EChartsCoreOption = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['买入成交额', '卖出成交额', '手续费收入'] },
    grid: { left: 64, right: 24, top: 44, bottom: 40 },
    xAxis: { type: 'category', boundaryGap: false, data: trend.value.map((item) => item.date) },
    yAxis: { type: 'value' },
    series: [
      {
        name: '买入成交额',
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: trend.value.map((item) => Number(item.buyDealTotal ?? 0)),
      },
      {
        name: '卖出成交额',
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: trend.value.map((item) => Number(item.sellDealTotal ?? 0)),
      },
      {
        name: '手续费收入',
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: trend.value.map((item) => Number(item.feeUsdtAmount ?? 0)),
      },
    ],
  };
  chart.setOption(option, true);
}

async function loadData() {
  loading.value = true;
  try {
    const params = {
      ...buildDateRangeQuery(dateRange.value, { withTime: false }),
      symbolCode: queryForm.value.symbolCode || undefined,
      side: queryForm.value.side || undefined,
    };
    const [summary, daily] = await Promise.all([
      getSpotDashboardSummary(params),
      getSpotDashboardDaily(params),
    ]);
    overview.value = summary?.overview ?? null;
    trend.value = summary?.trend ?? [];
    dailyList.value = daily ?? [];
    await nextTick();
    renderChart();
  } finally {
    loading.value = false;
  }
}

function handleResize() {
  chart?.resize();
}

async function loadOptions() {
  const symbols = await findAllSpotSymbol();
  symbolOptions.value = (symbols as SpotSymbolRow[])
    .filter((item) => item.symbolCode)
    .map((item) => ({ label: item.symbolCode as string, value: item.symbolCode as string }));
}

onMounted(async () => {
  window.addEventListener('resize', handleResize);
  await loadOptions();
  await loadData();
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  chart?.dispose();
  chart = null;
});
</script>

<style lang="less" scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card,
.chart-card,
.table-card {
  border-radius: var(--td-radius-medium);
}

.date-range {
  width: 320px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 12px;
}

.metric-card {
  min-height: 116px;
  border-radius: var(--td-radius-medium);
}

.metric-card__title {
  font-size: 13px;
  color: var(--td-text-color-secondary);
  margin-bottom: 8px;
}

.metric-card__value {
  font-size: 24px;
  font-weight: 600;
  color: var(--td-text-color-primary);
  font-variant-numeric: tabular-nums;
}

.metric-card__hint {
  margin-top: 8px;
  font-size: 12px;
  color: var(--td-text-color-placeholder);
}

.chart {
  width: 100%;
  height: 320px;
}

@media (max-width: 1600px) {
  .metric-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
