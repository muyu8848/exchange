<template>
  <div class="dashboard-page">
    <t-card :bordered="false" class="filter-card">
      <t-form layout="inline" :label-width="70">
        <t-form-item label="币种">
          <t-select
            v-model="queryForm.assetCode"
            clearable
            filterable
            :options="assetCodeOptions"
            placeholder="全部"
            style="width: 140px"
          />
        </t-form-item>
        <t-form-item label="账户类型">
          <t-select
            v-model="queryForm.accountType"
            clearable
            :options="accountTypeOptions"
            placeholder="全部"
            style="width: 140px"
          />
        </t-form-item>
        <t-form-item label="账号状态">
          <t-select
            v-model="queryForm.memberAccountStatus"
            multiple
            clearable
            :options="memberAccountStatusDictItems"
            :keys="{ label: 'dictItemName', value: 'dictItemCode' }"
            placeholder="全部"
            style="width: 180px"
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

    <t-card :bordered="false" class="table-card">
      <template #title>币种分布</template>
      <t-table
        row-key="assetCode"
        :data="assetDistribution"
        :columns="assetColumns"
        table-layout="auto"
        :hover="true"
        :loading="loading"
      />
    </t-card>

    <t-card :bordered="false" class="table-card">
      <template #title>账户类型分布</template>
      <t-table
        row-key="accountType"
        :data="accountTypeDistribution"
        :columns="typeColumns"
        table-layout="auto"
        :hover="true"
        :loading="loading"
      />
    </t-card>
  </div>
</template>

<script lang="ts">
export default { name: 'MemberAssetDashboard' };
</script>

<script setup lang="ts">
import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import { computed, onMounted, ref } from 'vue';

import type { CurrencyRow } from '@/api/currency';
import { findAllCurrency } from '@/api/currency';
import type { DictItem } from '@/api/dict';
import { findDictItemInCache } from '@/api/dict';
import type {
  MemberAssetDashboardAssetItem,
  MemberAssetDashboardOverview,
  MemberAssetDashboardTypeItem,
} from '@/api/memberAssetDashboard';
import { getMemberAssetDashboardSummary } from '@/api/memberAssetDashboard';

const loading = ref(false);
const queryForm = ref({ assetCode: '', accountType: '', memberAccountStatus: ['NORMAL', 'FROZEN'] });
const assetCodeOptions = ref<{ label: string; value: string }[]>([]);
const memberAccountStatusDictItems = ref<DictItem[]>([]);
const accountTypeOptions = [
  { label: '现货', value: 'SPOT' },
  { label: '合约', value: 'FUTURES' },
  { label: '法币', value: 'FIAT' },
];

const overview = ref<MemberAssetDashboardOverview | null>(null);
const assetDistribution = ref<MemberAssetDashboardAssetItem[]>([]);
const accountTypeDistribution = ref<MemberAssetDashboardTypeItem[]>([]);

function formatAmount(value?: number | string | null): string {
  const num = Number(value ?? 0);
  if (!Number.isFinite(num)) {
    return '0.00';
  }
  return num.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatRate(value?: number | string | null): string {
  const num = Number(value ?? 0);
  return Number.isFinite(num) ? `${num.toFixed(2)}%` : '0.00%';
}

const metrics = computed(() => {
  const data = overview.value;
  return [
    {
      key: 'totalUsdtValue',
      title: '总资产 (USDT)',
      value: formatAmount(data?.totalUsdtValue),
      hint: '当前快照，可用 + 冻结',
      valueClass: '',
    },
    {
      key: 'availableUsdtValue',
      title: '可用资产 (USDT)',
      value: formatAmount(data?.availableUsdtValue),
      hint: '当前快照',
      valueClass: '',
    },
    {
      key: 'frozenUsdtValue',
      title: '冻结资产 (USDT)',
      value: formatAmount(data?.frozenUsdtValue),
      hint: '当前快照',
      valueClass: '',
    },
    {
      key: 'memberCount',
      title: '有资产会员',
      value: `${data?.memberCount ?? 0} 人`,
      hint: '余额 > 0，去重',
      valueClass: '',
    },
    {
      key: 'accountCount',
      title: '资产账户数',
      value: `${data?.accountCount ?? 0} 条`,
      hint: '余额 > 0 的记录数',
      valueClass: '',
    },
    {
      key: 'avgUsdtValue',
      title: '人均资产 (USDT)',
      value: formatAmount(data?.avgUsdtValue),
      hint: '总资产 / 有资产会员',
      valueClass: '',
    },
    {
      key: 'assetCount',
      title: '覆盖币种数',
      value: `${data?.assetCount ?? 0} 个`,
      hint: '余额 > 0 的币种',
      valueClass: '',
    },
  ];
});

const assetColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'assetCode', title: '币种' },
  { colKey: 'available', title: '可用', align: 'right' },
  { colKey: 'frozen', title: '冻结', align: 'right' },
  { colKey: 'total', title: '合计', align: 'right' },
  {
    colKey: 'usdtValue',
    title: '折合 USDT',
    align: 'right',
    cell: (_h, { row }) => formatAmount(row.usdtValue),
  },
  {
    colKey: 'ratio',
    title: '占比',
    align: 'right',
    cell: (_h, { row }) => formatRate(row.ratio),
  },
  { colKey: 'accountCount', title: '账户数', align: 'right' },
];

const typeColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'accountTypeName', title: '账户类型' },
  {
    colKey: 'usdtValue',
    title: '折合 USDT',
    align: 'right',
    cell: (_h, { row }) => formatAmount(row.usdtValue),
  },
  {
    colKey: 'ratio',
    title: '占比',
    align: 'right',
    cell: (_h, { row }) => formatRate(row.ratio),
  },
  { colKey: 'accountCount', title: '账户数', align: 'right' },
];

async function loadData() {
  loading.value = true;
  try {
    const params = {
      assetCode: queryForm.value.assetCode || undefined,
      accountType: queryForm.value.accountType || undefined,
      memberAccountStatus: queryForm.value.memberAccountStatus?.length
        ? queryForm.value.memberAccountStatus.join(',')
        : undefined,
    };
    const summary = await getMemberAssetDashboardSummary(params);
    overview.value = summary?.overview ?? null;
    assetDistribution.value = summary?.assetDistribution ?? [];
    accountTypeDistribution.value = summary?.accountTypeDistribution ?? [];
  } finally {
    loading.value = false;
  }
}

async function loadOptions() {
  const [currencies, statusItems] = await Promise.all([
    findAllCurrency(),
    findDictItemInCache('memberAccountStatus'),
  ]);
  assetCodeOptions.value = (currencies as CurrencyRow[]).map((item) => ({
    label: item.currencyCode,
    value: item.currencyCode,
  }));
  memberAccountStatusDictItems.value = statusItems || [];
}

onMounted(async () => {
  await loadOptions();
  await loadData();
});
</script>

<style lang="less" scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card,
.table-card {
  border-radius: var(--td-radius-medium);
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
