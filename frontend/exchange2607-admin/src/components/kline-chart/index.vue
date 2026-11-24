<template>
  <div ref="chartRef" class="kline-chart-container"></div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue';
import {
  init,
  dispose,
  type Chart,
  type Nullable,
  type NeighborData,
  type KLineData,
} from 'klinecharts';
import { findKline, type KlineItem } from '@/api/kline';

const props = withDefaults(
  defineProps<{
    symbol: string;
    period?: string;
    priceScale?: number;
    bizType?: string;
  }>(),
  {
    period: '1D',
    priceScale: 2,
    bizType: 'contract',
  },
);

const chartRef = ref<HTMLDivElement>();
let chart: Chart | null = null;
let subscribeTimer: ReturnType<typeof setInterval> | null = null;

// 当前状态（供 DataLoader 闭包使用，props 变化时同步更新）
let currentSymbol = '';
let currentPeriodStr = '1D';
let currentBizType = 'contract';

function parsePeriod(periodStr: string) {
  if (!periodStr) return { type: 'minute' as const, span: 1 };
  const unit = periodStr.slice(-1).toLowerCase();
  const span = parseInt(periodStr.slice(0, -1)) || 1;
  let type: 'minute' | 'hour' | 'day' | 'week' = 'minute';
  if (unit === 'm') type = 'minute';
  else if (unit === 'h') type = 'hour';
  else if (unit === 'd') type = 'day';
  else if (unit === 'w') type = 'week';
  return { type, span };
}

function normalizeKLineData(item: KlineItem) {
  return {
    timestamp: Number(item.openTime),
    open: parseFloat(item.openPrice),
    high: parseFloat(item.highPrice),
    low: parseFloat(item.lowPrice),
    close: parseFloat(item.closePrice),
    volume: parseFloat(item.volume) || 0,
  };
}

async function fetchKline(params: {
  symbol: string;
  period: string;
  limit: number;
  enddate?: string;
  endtime?: string;
}): Promise<KlineItem[]> {
  try {
    return (await findKline({ ...params, bizType: currentBizType })) || [];
  } catch {
    return [];
  }
}

/** 格式化时间，参考OKX风格：不显示年份，统一 MM-DD HH:MM */
function formatTime(timestamp: number): string {
  const d = new Date(timestamp);
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  const hh = String(d.getHours()).padStart(2, '0');
  const mi = String(d.getMinutes()).padStart(2, '0');
  return `${mm}-${dd} ${hh}:${mi}`;
}

/** 格式化成交量：上万显示万，上亿显示亿 */
function formatVolume(v: number | string | null | undefined): string {
  if (v == null) return '--';
  const n = Number(v);
  if (!isFinite(n)) return '--';
  if (n >= 1e8) return (n / 1e8).toFixed(2) + '亿';
  if (n >= 1e4) return (n / 1e4).toFixed(2) + '万';
  return n.toFixed(2);
}

/** tooltip legend 自定义回调：在 时间/开/高/低/收 后追加涨跌幅 */
function legendTemplate(data: NeighborData<Nullable<KLineData>>) {
  const k = data.current;
  if (!k) return [];

  const precision = props.priceScale;
  const fmt = (v: number) => v.toFixed(precision);

  // 涨跌幅计算
  let chgText = '--';
  let chgColor = '#9a9690';
  if (k.open && k.open !== 0) {
    const chg = ((k.close - k.open) / k.open) * 100;
    const sign = chg > 0 ? '+' : '';
    chgText = `${sign}${chg.toFixed(2)}%`;
    chgColor = chg > 0 ? '#82b899' : chg < 0 ? '#d08080' : '#9a9690';
  }

  return [
    { title: '时间    ', value: formatTime(k.timestamp) },
    { title: '开', value: fmt(k.open) },
    { title: '高', value: fmt(k.high) },
    { title: '低', value: fmt(k.low) },
    { title: '收', value: fmt(k.close) },
    { title: '涨跌幅', value: { text: chgText, color: chgColor } },
    { title: '成交量', value: formatVolume(k.volume) },
  ];
}

function applyDarkTheme() {
  if (!chart) return;
  chart.setStyles({
    grid: {
      show: true,
      horizontal: { color: 'rgba(255,255,255,0.12)' },
      vertical: { color: 'rgba(255,255,255,0.12)' },
    },
    candle: {
      bar: {
        upColor: '#82b899',
        downColor: '#d08080',
        noChangeColor: '#9a9690',
        upBorderColor: '#82b899',
        downBorderColor: '#d08080',
        noChangeBorderColor: '#9a9690',
        upWickColor: '#82b899',
        downWickColor: '#d08080',
        noChangeWickColor: '#9a9690',
      },
      priceMark: {
        last: {
          upColor: '#82b899',
          downColor: '#d08080',
          noChangeColor: '#9a9690',
        },
      },
      tooltip: {
        showRule: 'follow_cross',
        showType: 'rect',
        title: { show: false },
        legend: {
          template: legendTemplate,
        },
        rect: {
          position: 'fixed',
          color: 'rgba(16, 16, 18, 0.92)',
          borderColor: 'rgba(201, 180, 138, 0.25)',
          borderSize: 1,
          borderRadius: 8,
          paddingLeft: 8,
          paddingRight: 8,
          paddingTop: 6,
          paddingBottom: 6,
        },
      },
    },
    xAxis: {
      axisLine: { color: 'rgba(255,255,255,0.08)' },
      tickText: { color: '#9a9690', size: 10 },
      tickLine: { color: 'rgba(255,255,255,0.06)' },
    },
    yAxis: {
      axisLine: { color: 'rgba(255,255,255,0.08)' },
      tickText: { color: '#9a9690', size: 10 },
      tickLine: { color: 'rgba(255,255,255,0.06)' },
    },
    crosshair: {
      horizontal: {
        line: { color: 'rgba(201,180,138,0.35)' },
        text: { color: '#030304', backgroundColor: '#c9b48a' },
      },
      vertical: {
        line: { color: 'rgba(201,180,138,0.35)' },
        text: { color: '#030304', backgroundColor: '#c9b48a' },
      },
    },
    indicator: {
      tooltip: {
        title: {
          showName: false,
          showParams: false,
        },
      },
    },
  });
}

function setupDataLoader() {
  if (!chart) return;

  chart.setDataLoader({
    getBars: ({ type, timestamp, callback }) => {
      if (type === 'backward') {
        // 实时数据由 subscribeBar 处理
        callback([], { forward: true, backward: false });
        return;
      }

      const limit = type === 'init' ? 150 : 100;
      const params: {
        symbol: string;
        period: string;
        limit: number;
        enddate?: string;
        endtime?: string;
      } = {
        symbol: currentSymbol,
        period: currentPeriodStr,
        limit,
      };

      // forward 分页：将 timestamp 转换为 enddate + endtime
      if (type === 'forward' && timestamp) {
        const d = new Date(timestamp);
        params.enddate =
          d.getFullYear() +
          String(d.getMonth() + 1).padStart(2, '0') +
          String(d.getDate()).padStart(2, '0');
        params.endtime =
          String(d.getHours()).padStart(2, '0') +
          String(d.getMinutes()).padStart(2, '0');
      }

      fetchKline(params).then((data) => {
        const bars = data
          .map((item) => normalizeKLineData(item))
          .sort((a, b) => a.timestamp - b.timestamp);
        // 返回条数不足 limit，说明该方向没有更多数据
        const hasMore = bars.length >= limit;
        callback(bars, { forward: hasMore, backward: false });
      });
    },

    subscribeBar: ({ callback }) => {
      // 清理上一次的定时器
      if (subscribeTimer) {
        clearInterval(subscribeTimer);
        subscribeTimer = null;
      }
      // 每 3 秒轮询最新的未闭合 K 线
      subscribeTimer = setInterval(() => {
        fetchKline({
          symbol: currentSymbol,
          period: currentPeriodStr,
          limit: 1,
        }).then((data) => {
          if (data && data.length > 0) {
            callback(normalizeKLineData(data[0]));
          }
        });
      }, 3000);
    },

    unsubscribeBar: () => {
      if (subscribeTimer) {
        clearInterval(subscribeTimer);
        subscribeTimer = null;
      }
    },
  });
}

function initChart() {
  if (!chartRef.value) return;
  currentSymbol = props.symbol;
  currentPeriodStr = props.period;
  currentBizType = props.bizType;

  chart = init(chartRef.value);
  applyDarkTheme();
  // 添加成交量副图指标（calcParams改为[5,10]去掉默认的MA20）
  chart.createIndicator('VOL', false);
  chart.overrideIndicator({ name: 'VOL', calcParams: [5, 10] });
  setupDataLoader();

  // setSymbol 和 setPeriod 必须依次调用，setPeriod 会触发 resetData -> getBars(init) -> subscribeBar
  chart.setSymbol({
    ticker: currentSymbol,
    pricePrecision: props.priceScale,
    volumePrecision: 8,
  });
  chart.setPeriod(parsePeriod(currentPeriodStr));
}

// 监听 period 变化
watch(
  () => props.period,
  (newPeriod) => {
    if (!chart) return;
    currentPeriodStr = newPeriod;
    // setPeriod 触发 resetData -> getBars(init) -> subscribeBar
    chart.setPeriod(parsePeriod(newPeriod));
  },
);

// 监听 symbol 变化
watch(
  () => props.symbol,
  (newSymbol) => {
    if (!chart) return;
    currentSymbol = newSymbol;
    // setSymbol 触发 resetData -> getBars(init) -> subscribeBar
    chart.setSymbol({
      ticker: newSymbol,
      pricePrecision: props.priceScale,
      volumePrecision: 8,
    });
  },
);

// 监听 priceScale 变化
watch(
  () => props.priceScale,
  (newScale) => {
    if (!chart) return;
    chart.setSymbol({
      ticker: currentSymbol,
      pricePrecision: newScale,
      volumePrecision: 8,
    });
  },
);

// 监听 bizType 变化
watch(
  () => props.bizType,
  (newBizType) => {
    if (!chart) return;
    currentBizType = newBizType;
    // setSymbol 触发 resetData -> getBars(init) -> subscribeBar
    chart.setSymbol({
      ticker: currentSymbol,
      pricePrecision: props.priceScale,
      volumePrecision: 8,
    });
  },
);

onMounted(() => {
  initChart();
});

onUnmounted(() => {
  if (subscribeTimer) {
    clearInterval(subscribeTimer);
    subscribeTimer = null;
  }
  if (chartRef.value) {
    dispose(chartRef.value);
  }
  chart = null;
});
</script>

<style scoped>
.kline-chart-container {
  width: 100%;
  height: 100%;
  min-height: 400px;
}
</style>
