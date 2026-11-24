<template>
  <t-dialog
    v-model:visible="innerVisible"
    :header="`活期账户详情 - ${detail?.assetCode || ''}`"
    width="960px"
    :footer="false"
    placement="center"
    destroy-on-close
  >
    <div v-if="loading" class="detail-empty">加载中…</div>
    <template v-else-if="detail">
      <t-descriptions bordered :column="2" :table-layout="'auto'" :colon="false" size="small">
        <t-descriptions-item label="活期账户">
          <span class="detail-id">{{ detail.id }}</span>
          <copy-icon-button :text="detail.id" />
        </t-descriptions-item>
        <t-descriptions-item label="会员账号">
          <t-link
            v-if="detail.memberAccountId && detail.memberUid != null"
            theme="primary"
            hover="color"
            @click="emit('member-click', detail.memberAccountId)"
          >
            {{ detail.memberUid }}
          </t-link>
          <span v-else>{{ detail.memberUid != null ? detail.memberUid : '—' }}</span>
        </t-descriptions-item>
        <t-descriptions-item label="币种">{{ detail.assetCode || '—' }}</t-descriptions-item>
        <t-descriptions-item label="持仓金额">{{ detail.position }}</t-descriptions-item>
        <t-descriptions-item label="累计收益">{{ detail.accruedInterest }}</t-descriptions-item>
        <t-descriptions-item label="综合年化">{{ detail.comprehensiveRate }}%</t-descriptions-item>
        <t-descriptions-item label="年化收益率">{{ detail.annualRate }}%</t-descriptions-item>
        <t-descriptions-item label="补贴年化">{{ detail.subsidizedRate || '—' }}%</t-descriptions-item>
        <t-descriptions-item label="奖励限额">{{ detail.rewardLimit || '—' }}</t-descriptions-item>
        <t-descriptions-item label="开户时间">{{ detail.createTime }}</t-descriptions-item>
        <t-descriptions-item label="上次计息">{{ detail.lastAccrualDate || '—' }}</t-descriptions-item>
        <t-descriptions-item v-if="detail.nextAccrualTime" label="下次计息">
          {{ detail.nextAccrualTime }}
        </t-descriptions-item>
        <t-descriptions-item v-if="detail.redeemTime" label="赎回时间">
          {{ detail.redeemTime }}
        </t-descriptions-item>
      </t-descriptions>

      <t-tabs v-model="activeTab" class="detail-tabs">
        <t-tab-panel value="hourly" :label="`小时收益 (${hourlyPagination.total})`">
          <t-table
            row-key="id"
            :data="hourlyTableData"
            :columns="hourlyColumns"
            :loading="hourlyLoading"
            :pagination="hourlyPagination"
            max-height="320px"
            table-layout="auto"
            @page-change="onHourlyPageChange"
          >
            <template #interestHour="{ row }">
              {{ row.interestHour != null ? `${String(row.interestHour).padStart(2, '0')}:00` : '—' }}
            </template>
          </t-table>
        </t-tab-panel>
        <t-tab-panel value="daily" :label="`日收益 (${logPagination.total})`">
          <t-table
            row-key="id"
            :data="logTableData"
            :columns="logColumns"
            :loading="logLoading"
            :pagination="logPagination"
            max-height="320px"
            table-layout="auto"
            @page-change="onLogPageChange"
          />
        </t-tab-panel>
        <t-tab-panel value="position" :label="`持仓明细 (${positionPagination.total})`">
          <t-table
            row-key="id"
            :data="positionTableData"
            :columns="positionColumns"
            :loading="positionLoading"
            :pagination="positionPagination"
            max-height="320px"
            table-layout="auto"
            @page-change="onPositionPageChange"
          />
        </t-tab-panel>
        <t-tab-panel value="record" :label="`投资记录 (${recordPagination.total})`">
          <t-table
            row-key="id"
            :data="recordTableData"
            :columns="recordColumns"
            :loading="recordLoading"
            :pagination="recordPagination"
            max-height="320px"
            table-layout="auto"
            @page-change="onRecordPageChange"
          />
        </t-tab-panel>
        <t-tab-panel value="redeem" :label="`赎回记录 (${redeemPagination.total})`">
          <t-table
            row-key="id"
            :data="redeemTableData"
            :columns="redeemColumns"
            :loading="redeemLoading"
            :pagination="redeemPagination"
            max-height="320px"
            table-layout="auto"
            @page-change="onRedeemPageChange"
          />
        </t-tab-panel>
      </t-tabs>
    </template>
    <div v-else class="detail-empty">未获取到账户信息</div>
  </t-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';

import type { PageInfo, PrimaryTableCol, TableRowData } from 'tdesign-vue-next';

import type { InvestmentFlexibleAccountRow } from '@/api/investmentFlexibleAccount';
import { findInvestmentFlexibleAccountDetail } from '@/api/investmentFlexibleAccount';
import type { InvestmentFlexiblePositionRow } from '@/api/investmentFlexiblePosition';
import { findInvestmentFlexiblePositionByPage } from '@/api/investmentFlexiblePosition';
import type { InvestmentRecordRow } from '@/api/investmentRecord';
import { findInvestmentRecordByPage } from '@/api/investmentRecord';
import type { InvestmentRedeemRecordRow } from '@/api/investmentRedeemRecord';
import { findInvestmentRedeemRecordByPage } from '@/api/investmentRedeemRecord';
import type { InvestmentHourlyInterestLogRow } from '@/api/investmentHourlyInterestLog';
import { findInvestmentHourlyInterestLogByPage } from '@/api/investmentHourlyInterestLog';
import type { InvestmentInterestLogRow } from '@/api/investmentInterestLog';
import { findInvestmentInterestLogByPage } from '@/api/investmentInterestLog';
import CopyIconButton from '@/components/copy-icon-button/index.vue';

const props = defineProps<{
  visible: boolean;
  accountId?: string;
}>();

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'member-click', memberAccountId?: string): void;
}>();

const innerVisible = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value),
});

const loading = ref(false);
const detail = ref<InvestmentFlexibleAccountRow | null>(null);

const activeTab = ref<'hourly' | 'daily' | 'position' | 'record' | 'redeem'>('daily');

// ===== 小时收益 =====
const hourlyColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'interestDate', title: '计息日' },
  { colKey: 'interestHour', title: '计息小时' },
  { colKey: 'principal', title: '计息本金', align: 'right' },
  { colKey: 'interestAmount', title: '小时收益', align: 'right' },
  { colKey: 'createTime', title: '入账时间' },
];
const hourlyLoading = ref(false);
const hourlyTableData = ref<InvestmentHourlyInterestLogRow[]>([]);
const hourlyPagination = ref({ current: 1, pageSize: 10, total: 0 });

async function loadHourlyInterestLogs() {
  if (!props.accountId) return;
  hourlyLoading.value = true;
  try {
    const page = await findInvestmentHourlyInterestLogByPage({
      flexibleAccountId: props.accountId,
      pageNum: hourlyPagination.value.current,
      pageSize: hourlyPagination.value.pageSize,
    });
    hourlyTableData.value = page?.content || [];
    hourlyPagination.value.total = page?.total || 0;
  } catch {
    hourlyTableData.value = [];
    hourlyPagination.value.total = 0;
  } finally {
    hourlyLoading.value = false;
  }
}

function onHourlyPageChange(pageInfo: PageInfo) {
  hourlyPagination.value.current = pageInfo.current;
  hourlyPagination.value.pageSize = pageInfo.pageSize;
  loadHourlyInterestLogs();
}

// ===== 日收益 =====
const logColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'interestDate', title: '计息日' },
  { colKey: 'interestTypeName', title: '收益类型' },
  { colKey: 'principal', title: '本金', align: 'right' },
  { colKey: 'interestAmount', title: '收益', align: 'right' },
  { colKey: 'createTime', title: '入账时间' },
];
const logLoading = ref(false);
const logTableData = ref<InvestmentInterestLogRow[]>([]);
const logPagination = ref({ current: 1, pageSize: 10, total: 0 });

async function loadInterestLogs() {
  if (!props.accountId) return;
  logLoading.value = true;
  try {
    const page = await findInvestmentInterestLogByPage({
      flexibleAccountId: props.accountId,
      interestType: 'COMPOUND',
      includeTodayRealtime: true,
      pageNum: logPagination.value.current,
      pageSize: logPagination.value.pageSize,
    });
    logTableData.value = page?.content || [];
    logPagination.value.total = page?.total || 0;
  } catch {
    logTableData.value = [];
    logPagination.value.total = 0;
  } finally {
    logLoading.value = false;
  }
}

function onLogPageChange(pageInfo: PageInfo) {
  logPagination.value.current = pageInfo.current;
  logPagination.value.pageSize = pageInfo.pageSize;
  loadInterestLogs();
}

// ===== 持仓明细（每次申购一行，principal 为当前剩余本金） =====
const positionColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'orderNo', title: '订单号' },
  { colKey: 'principal', title: '持仓金额', align: 'right' },
  { colKey: 'accruedInterest', title: '累计收益', align: 'right' },
  { colKey: 'statusName', title: '状态' },
  { colKey: 'investTime', title: '申购时间' },
];
const positionLoading = ref(false);
const positionTableData = ref<InvestmentFlexiblePositionRow[]>([]);
const positionPagination = ref({ current: 1, pageSize: 10, total: 0 });

async function loadPositionRecords() {
  if (!props.accountId) return;
  positionLoading.value = true;
  try {
    const page = await findInvestmentFlexiblePositionByPage({
      flexibleAccountId: props.accountId,
      pageNum: positionPagination.value.current,
      pageSize: positionPagination.value.pageSize,
    });
    positionTableData.value = page?.content || [];
    positionPagination.value.total = page?.total || 0;
  } catch {
    positionTableData.value = [];
    positionPagination.value.total = 0;
  } finally {
    positionLoading.value = false;
  }
}

function onPositionPageChange(pageInfo: PageInfo) {
  positionPagination.value.current = pageInfo.current;
  positionPagination.value.pageSize = pageInfo.pageSize;
  loadPositionRecords();
}

// ===== 投资记录（订单快照：原始申购金额与当时利率） =====
const recordColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'orderNo', title: '订单号' },
  { colKey: 'principal', title: '申购金额', align: 'right' },
  { colKey: 'annualRate', title: '年化%', align: 'right' },
  { colKey: 'subsidizedRate', title: '补贴年化%', align: 'right' },
  { colKey: 'createTime', title: '申购时间' },
];
const recordLoading = ref(false);
const recordTableData = ref<InvestmentRecordRow[]>([]);
const recordPagination = ref({ current: 1, pageSize: 10, total: 0 });

async function loadRecordList() {
  if (!props.accountId) return;
  recordLoading.value = true;
  try {
    const page = await findInvestmentRecordByPage({
      flexibleAccountId: props.accountId,
      productType: 'FLEXIBLE',
      pageNum: recordPagination.value.current,
      pageSize: recordPagination.value.pageSize,
    });
    recordTableData.value = page?.content || [];
    recordPagination.value.total = page?.total || 0;
  } catch {
    recordTableData.value = [];
    recordPagination.value.total = 0;
  } finally {
    recordLoading.value = false;
  }
}

function onRecordPageChange(pageInfo: PageInfo) {
  recordPagination.value.current = pageInfo.current;
  recordPagination.value.pageSize = pageInfo.pageSize;
  loadRecordList();
}

// ===== 赎回记录（活期均为手动赎回，故不展示赎回方式） =====
const redeemColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'orderNo', title: '赎回订单号' },
  { colKey: 'redeemAmount', title: '赎回金额', align: 'right' },
  { colKey: 'createTime', title: '赎回时间' },
];
const redeemLoading = ref(false);
const redeemTableData = ref<InvestmentRedeemRecordRow[]>([]);
const redeemPagination = ref({ current: 1, pageSize: 10, total: 0 });

async function loadRedeemList() {
  if (!props.accountId) return;
  redeemLoading.value = true;
  try {
    const page = await findInvestmentRedeemRecordByPage({
      flexibleAccountId: props.accountId,
      pageNum: redeemPagination.value.current,
      pageSize: redeemPagination.value.pageSize,
    });
    redeemTableData.value = page?.content || [];
    redeemPagination.value.total = page?.total || 0;
  } catch {
    redeemTableData.value = [];
    redeemPagination.value.total = 0;
  } finally {
    redeemLoading.value = false;
  }
}

function onRedeemPageChange(pageInfo: PageInfo) {
  redeemPagination.value.current = pageInfo.current;
  redeemPagination.value.pageSize = pageInfo.pageSize;
  loadRedeemList();
}

watch(
  () => [props.visible, props.accountId] as const,
  async ([visible, accountId]) => {
    if (!visible || !accountId) return;
    loading.value = true;
    detail.value = null;
    hourlyTableData.value = [];
    hourlyPagination.value.current = 1;
    hourlyPagination.value.total = 0;
    logTableData.value = [];
    logPagination.value.current = 1;
    logPagination.value.total = 0;
    positionTableData.value = [];
    positionPagination.value.current = 1;
    positionPagination.value.total = 0;
    recordTableData.value = [];
    recordPagination.value.current = 1;
    recordPagination.value.total = 0;
    redeemTableData.value = [];
    redeemPagination.value.current = 1;
    redeemPagination.value.total = 0;
    // 各 tab 的条数需要在打开时就可见，因此并行加载（互不阻塞、各自失败互不影响）
    await Promise.all([
      (async () => {
        try {
          detail.value = await findInvestmentFlexibleAccountDetail(accountId);
        } catch {
          detail.value = null;
        } finally {
          loading.value = false;
        }
      })(),
      loadHourlyInterestLogs(),
      loadInterestLogs(),
      loadPositionRecords(),
      loadRecordList(),
      loadRedeemList(),
    ]);
  },
  { immediate: true },
);
</script>

<style lang="less" scoped>
.detail-id {
  margin-right: 4px;
  font-variant-numeric: tabular-nums;
}

.detail-tabs {
  margin-top: 16px;
}

.detail-empty {
  padding: 40px 0;
  text-align: center;
  color: var(--td-text-color-placeholder);
}
</style>
