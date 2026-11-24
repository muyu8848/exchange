<template>
  <div class="admin-home">
    <t-card :bordered="false" class="admin-home__toolbar">
      <div class="admin-home__toolbar-inner">
        <div class="admin-home__heading">
          <span class="admin-home__heading-main">全站运营概览</span>
          <span class="admin-home__heading-sub">数据聚合自各业务看板，点击卡片进入对应看板</span>
        </div>
        <div class="admin-home__actions">
          <t-date-range-picker
            v-model="dateRange"
            class="admin-home__date-range"
            :presets="tableDateOnlyRangePresets"
            allow-input
            clearable
            @change="loadData"
          />
          <t-button theme="primary" :loading="loading" @click="loadData">刷新</t-button>
        </div>
      </div>
    </t-card>

    <div class="overview-grid">
      <t-card
        v-for="card in cards"
        :key="card.key"
        :bordered="false"
        class="overview-card"
        :class="{ 'overview-card--clickable': card.menuUrl }"
        @click="go(card.menuUrl)"
      >
        <div class="overview-card__head">
          <span class="overview-card__title">{{ card.title }}</span>
          <span v-if="card.failed" class="overview-card__error">加载失败</span>
          <span v-else-if="card.snapshot" class="overview-card__tag">快照</span>
        </div>

        <div class="overview-card__primary">
          <span class="overview-card__primary-value">{{ card.primaryValue }}</span>
          <span class="overview-card__primary-unit">{{ card.primaryUnit }}</span>
        </div>
        <div class="overview-card__primary-label">{{ card.primaryLabel }}</div>

        <div class="overview-card__items">
          <div v-for="item in card.items" :key="item.label" class="overview-card__item">
            <span class="overview-card__item-label">{{ item.label }}</span>
            <span class="overview-card__item-value">{{ item.value }}</span>
          </div>
        </div>
      </t-card>
    </div>
  </div>
</template>

<script lang="ts">
export default {
  name: 'Home',
};
</script>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { getC2cDashboardSummary } from '@/api/c2cDashboard';
import { getConvertDashboardSummary } from '@/api/convertDashboard';
import { getDepositWithdrawSummary } from '@/api/depositWithdrawDashboard';
import { getInvestmentDashboardSummary } from '@/api/investmentDashboard';
import { getMemberAssetDashboardSummary } from '@/api/memberAssetDashboard';
import { getMemberDashboardSummary } from '@/api/memberDashboard';
import { getPerpetualDashboardSummary } from '@/api/perpetualDashboard';
import { getSecondContractDashboardSummary } from '@/api/secondContractDashboard';
import { getSpotDashboardSummary } from '@/api/spotDashboard';
import router from '@/router';
import { buildDateRangeQuery, staticTableDateRangePresets, tableDateOnlyRangePresets } from '@/utils/dateRangePresets';
import { gotoPage } from '@/utils/menuRoute';

interface OverviewItem {
  label: string;
  value: string;
}

interface OverviewCard {
  key: string;
  title: string;
  menuUrl: string;
  primaryLabel: string;
  primaryValue: string;
  primaryUnit: string;
  items: OverviewItem[];
  /** 资产类指标为当前快照，不受时间范围影响 */
  snapshot?: boolean;
  failed?: boolean;
}

const loading = ref(false);
const dateRange = ref<string[]>([...staticTableDateRangePresets['今天']]);

const formatCount = (value?: number | null) => {
  if (value === null || value === undefined || Number.isNaN(value)) return '-';
  return value.toLocaleString('zh-CN', { maximumFractionDigits: 0 });
};

const formatAmount = (value?: number | null) => {
  if (value === null || value === undefined || Number.isNaN(value)) return '-';
  return value.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 });
};

const formatPercent = (value?: number | null) => {
  if (value === null || value === undefined || Number.isNaN(value)) return '-';
  return `${value.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })}%`;
};

const cards = ref<OverviewCard[]>(buildCards());

function buildCards(
  member?: Awaited<ReturnType<typeof getMemberDashboardSummary>>,
  asset?: Awaited<ReturnType<typeof getMemberAssetDashboardSummary>>,
  fund?: Awaited<ReturnType<typeof getDepositWithdrawSummary>>,
  spot?: Awaited<ReturnType<typeof getSpotDashboardSummary>>,
  perpetual?: Awaited<ReturnType<typeof getPerpetualDashboardSummary>>,
  secondContract?: Awaited<ReturnType<typeof getSecondContractDashboardSummary>>,
  c2c?: Awaited<ReturnType<typeof getC2cDashboardSummary>>,
  investment?: Awaited<ReturnType<typeof getInvestmentDashboardSummary>>,
  convert?: Awaited<ReturnType<typeof getConvertDashboardSummary>>,
): OverviewCard[] {
  const memberOverview = member?.overview;
  const assetOverview = asset?.overview;
  const fundOverview = fund?.overview;
  const spotOverview = spot?.overview;
  const perpetualOverview = perpetual?.overview;
  const secondContractOverview = secondContract?.overview;
  const c2cOverview = c2c?.overview;
  const investmentOverview = investment?.overview;
  const convertOverview = convert?.overview;

  return [
    {
      key: 'member',
      title: '会员',
      menuUrl: 'member-dashboard',
      primaryLabel: '新增注册',
      primaryValue: formatCount(memberOverview?.newMemberCount),
      primaryUnit: '人',
      items: [
        { label: '累计注册', value: formatCount(memberOverview?.totalRegisteredCount) },
        { label: '活跃会员', value: formatCount(memberOverview?.activeMemberCount) },
        { label: '实名会员', value: formatCount(memberOverview?.verifiedMemberCount) },
      ],
      failed: !memberOverview,
    },
    {
      key: 'asset',
      title: '会员资产',
      menuUrl: 'member-asset-dashboard',
      primaryLabel: '总资产折合',
      primaryValue: formatAmount(assetOverview?.totalUsdtValue),
      primaryUnit: 'USDT',
      items: [
        { label: '可用', value: formatAmount(assetOverview?.availableUsdtValue) },
        { label: '冻结', value: formatAmount(assetOverview?.frozenUsdtValue) },
        { label: '持仓用户', value: formatCount(assetOverview?.memberCount) },
      ],
      snapshot: true,
      failed: !assetOverview,
    },
    {
      key: 'fund',
      title: '充币 / 提币',
      menuUrl: 'deposit-withdraw-dashboard',
      primaryLabel: '净流入',
      primaryValue: formatAmount(fundOverview?.netInflow),
      primaryUnit: 'USDT',
      items: [
        { label: '充币金额', value: formatAmount(fundOverview?.depositAmount) },
        { label: '提币金额', value: formatAmount(fundOverview?.withdrawAmount) },
        { label: '待审核提币', value: formatCount(fundOverview?.pendingAuditCount) },
      ],
      failed: !fundOverview,
    },
    {
      key: 'spot',
      title: '现货交易',
      menuUrl: 'spot-dashboard',
      primaryLabel: '成交额',
      primaryValue: formatAmount(spotOverview?.totalDealTotal),
      primaryUnit: 'USDT',
      items: [
        { label: '成交笔数', value: formatCount(spotOverview?.dealCount) },
        { label: '手续费', value: formatAmount(spotOverview?.feeUsdtAmount) },
        { label: '待成交计划', value: formatCount(spotOverview?.pendingPlanCount) },
      ],
      failed: !spotOverview,
    },
    {
      key: 'perpetual',
      title: '永续合约',
      menuUrl: 'perpetual-dashboard',
      primaryLabel: '成交额',
      primaryValue: formatAmount(perpetualOverview?.dealAmount),
      primaryUnit: 'USDT',
      items: [
        { label: '订单数', value: formatCount(perpetualOverview?.orderCount) },
        { label: '手续费', value: formatAmount(perpetualOverview?.feeUsdtAmount) },
        { label: '强平次数', value: formatCount(perpetualOverview?.liquidateCount) },
      ],
      failed: !perpetualOverview,
    },
    {
      key: 'secondContract',
      title: '秒合约',
      menuUrl: 'second-contract-dashboard',
      primaryLabel: '下注总额',
      primaryValue: formatAmount(secondContractOverview?.totalAmount),
      primaryUnit: 'USDT',
      items: [
        { label: '平台净收益', value: formatAmount(secondContractOverview?.platformNetIncome) },
        { label: '用户胜率', value: formatPercent(secondContractOverview?.winRate) },
        { label: '未结算敞口', value: formatAmount(secondContractOverview?.runningAmount) },
      ],
      failed: !secondContractOverview,
    },
    {
      key: 'c2c',
      title: 'C2C 交易',
      menuUrl: 'c2c-dashboard',
      primaryLabel: '成交额',
      primaryValue: formatAmount(c2cOverview?.dealAmount),
      primaryUnit: '',
      items: [
        { label: '完成订单', value: formatCount(c2cOverview?.completedCount) },
        { label: '完成率', value: formatPercent(c2cOverview?.completionRate) },
        { label: '申诉中', value: formatCount(c2cOverview?.appealingCount) },
      ],
      failed: !c2cOverview,
    },
    {
      key: 'investment',
      title: '理财',
      menuUrl: 'investment-dashboard',
      primaryLabel: '持仓规模',
      primaryValue: formatAmount(investmentOverview?.holdingAmount),
      primaryUnit: '',
      items: [
        { label: '申购金额', value: formatAmount(investmentOverview?.subscribeAmount) },
        { label: '赎回金额', value: formatAmount(investmentOverview?.redeemAmount) },
        { label: '发放收益', value: formatAmount(investmentOverview?.interestAmount) },
      ],
      failed: !investmentOverview,
    },
    {
      key: 'convert',
      title: '闪兑',
      menuUrl: 'convert-dashboard',
      primaryLabel: '兑换额',
      primaryValue: formatAmount(convertOverview?.convertUsdtValue),
      primaryUnit: 'USDT',
      items: [
        { label: '兑换笔数', value: formatCount(convertOverview?.convertCount) },
        { label: '手续费', value: formatAmount(convertOverview?.feeUsdtAmount) },
        { label: '参与人数', value: formatCount(convertOverview?.memberCount) },
      ],
      failed: !convertOverview,
    },
  ];
}

const loadData = async () => {
  loading.value = true;
  const timeQuery = buildDateRangeQuery(dateRange.value, { withTime: false });
  try {
    // 单个看板失败不影响其余卡片，失败卡片展示「加载失败」
    const [member, asset, fund, spot, perpetual, secondContract, c2c, investment, convert] = await Promise.allSettled([
      getMemberDashboardSummary(timeQuery),
      getMemberAssetDashboardSummary({}),
      getDepositWithdrawSummary(timeQuery),
      getSpotDashboardSummary(timeQuery),
      getPerpetualDashboardSummary(timeQuery),
      getSecondContractDashboardSummary(timeQuery),
      getC2cDashboardSummary(timeQuery),
      getInvestmentDashboardSummary(timeQuery),
      getConvertDashboardSummary(timeQuery),
    ]);
    cards.value = buildCards(
      member.status === 'fulfilled' ? member.value : undefined,
      asset.status === 'fulfilled' ? asset.value : undefined,
      fund.status === 'fulfilled' ? fund.value : undefined,
      spot.status === 'fulfilled' ? spot.value : undefined,
      perpetual.status === 'fulfilled' ? perpetual.value : undefined,
      secondContract.status === 'fulfilled' ? secondContract.value : undefined,
      c2c.status === 'fulfilled' ? c2c.value : undefined,
      investment.status === 'fulfilled' ? investment.value : undefined,
      convert.status === 'fulfilled' ? convert.value : undefined,
    );
  } finally {
    loading.value = false;
  }
};

const go = (menuUrl: string) => {
  if (!menuUrl) return;
  gotoPage(router, menuUrl);
};

onMounted(loadData);
</script>

<style lang="less" scoped>
.admin-home__toolbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.admin-home__heading {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.admin-home__heading-main {
  font-size: 16px;
  font-weight: 600;
  color: var(--td-text-color-primary);
}

.admin-home__heading-sub {
  font-size: 12px;
  color: var(--td-text-color-placeholder);
}

.admin-home__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-home__date-range {
  width: 280px;
}

.overview-grid {
  display: grid;
  /* 9 个业务看板 + 首页概览，3 列正好 3 行 */
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.overview-card {
  min-height: 172px;
  transition: box-shadow 0.2s ease;
}

.overview-card--clickable {
  cursor: pointer;
}

.overview-card--clickable:hover {
  box-shadow: var(--td-shadow-2);
}

.overview-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.overview-card__title {
  font-size: 14px;
  font-weight: 500;
  color: var(--td-text-color-primary);
}

.overview-card__tag {
  font-size: 12px;
  color: var(--td-text-color-placeholder);
  background: var(--td-bg-color-container-hover);
  border-radius: 2px;
  padding: 0 4px;
}

.overview-card__error {
  font-size: 12px;
  color: var(--td-error-color);
}

.overview-card__primary {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.overview-card__primary-value {
  font-size: 26px;
  font-weight: 600;
  line-height: 1.2;
  color: var(--td-text-color-primary);
  word-break: break-all;
}

.overview-card__primary-unit {
  font-size: 12px;
  color: var(--td-text-color-secondary);
}

.overview-card__primary-label {
  margin-top: 2px;
  font-size: 12px;
  color: var(--td-text-color-placeholder);
}

.overview-card__items {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid var(--td-component-stroke);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.overview-card__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  gap: 8px;
}

.overview-card__item-label {
  color: var(--td-text-color-secondary);
  white-space: nowrap;
}

.overview-card__item-value {
  color: var(--td-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 1400px) {
  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
