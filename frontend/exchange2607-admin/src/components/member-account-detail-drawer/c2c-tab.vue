<script setup lang="ts">
import { ref, watch } from 'vue';

import type { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import type { C2cAdminReviewVO, C2cOrderRow, PreOrderRow, RiskRecordRow } from '@/api/c2c';
import { findC2cOrderByPage, findPreOrderByPage, findReviewsByPage, findRiskRecordByPage } from '@/api/c2c';
import type { C2cAppealRecordRow } from '@/api/c2cAppealRecord';
import { findC2cAppealRecordByPage } from '@/api/c2cAppealRecord';
import type { PaymentAccountRow } from '@/api/paymentAccount';
import { findPaymentAccountByPage } from '@/api/paymentAccount';
import PaymentAccountCell from '@/components/payment-account-cell/index.vue';
import { c2cOrderStatusTheme } from '@/utils/c2cOrderStatus';

import OrderNoCell from './order-no-cell.vue';
import { usePaged } from './use-paged';

const props = defineProps<{
  /** 会员账号 ID（memberAccountId）：C2C 订单按买家 OR 卖家查询 */
  memberId?: string;
  /** 会员 UID：广告 / 申诉 / 评价 / 收款账户 / 风控按它查询 */
  memberUid?: number | string;
}>();

type SubTab = 'order' | 'preOrder' | 'appeal' | 'review' | 'payment' | 'risk';
const activeSubTab = ref<SubTab>('order');

function uidOrUndefined(): number | undefined {
  const uid = Number(props.memberUid);
  return uid > 0 ? uid : undefined;
}

/* ===== 1. C2C 订单 ===== */
const order = usePaged<C2cOrderRow>(async (page, pageSize) => {
  if (!props.memberId) {
    return { content: [], total: 0 };
  }
  return findC2cOrderByPage({
    memberAccountId: props.memberId,
    pageNum: page,
    pageSize,
  });
});

/** 与 c2c-order 页面一致：订单信息合并订单号/角色/买卖双方，新增单价与支付方式 */
const orderColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'orderInfo', title: '订单信息', width: 220 },
  { colKey: 'symbol', title: '交易对' },
  { colKey: 'amount', title: '交易数量', align: 'right' },
  { colKey: 'price', title: '单价', align: 'right' },
  { colKey: 'fiatTotal', title: '法币总额', align: 'right' },
  { colKey: 'paymentMethod', title: '支付方式' },
  { colKey: 'status', title: '状态' },
  { colKey: 'createTime', title: '提交时间' },
];

/* ===== 2. 广告 ===== */
const preOrder = usePaged<PreOrderRow>(async (page, pageSize) => {
  const uid = uidOrUndefined();
  if (!uid) {
    return { content: [], total: 0 };
  }
  return findPreOrderByPage({ memberUid: uid, pageNum: page, pageSize });
});

/** 广告状态配色（与 c2c-pre-order 页面一致）：2 已完成 / 3、4 已取消或关闭 */
function preOrderStatusTheme(status?: string): 'success' | 'default' | 'warning' {
  if (status === '2') {
    return 'success';
  }
  if (status === '3' || status === '4') {
    return 'default';
  }
  return 'warning';
}

/** 与 c2c-pre-order 页面一致：广告信息合并订单号/交易对，新增数量进度与支付方式 */
const preOrderColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'orderInfo', title: '广告信息', width: 200 },
  { colKey: 'price', title: '单价', align: 'right' },
  { colKey: 'fiatTotal', title: '法币总额', align: 'right' },
  { colKey: 'amountInfo', title: '数量进度', align: 'right' },
  { colKey: 'paymentTypes', title: '支付方式' },
  { colKey: 'status', title: '状态' },
  { colKey: 'createTime', title: '提交时间' },
];

/* ===== 3. 申诉 ===== */
const appeal = usePaged<C2cAppealRecordRow>(async (page, pageSize) => {
  const uid = uidOrUndefined();
  if (!uid) {
    return { content: [], total: 0 };
  }
  return findC2cAppealRecordByPage({ memberUid: uid, pageNum: page, pageSize });
});

/** 与 c2c-appeal-record 页面一致：处理方式与处理结果合并为一列 */
const appealColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'orderNo', title: '订单号', width: 190, ellipsis: true },
  { colKey: 'appealTypeName', title: '申诉类型' },
  { colKey: 'statusName', title: '状态' },
  { colKey: 'processInfo', title: '处理结果' },
  { colKey: 'createTime', title: '申诉时间' },
];

/* ===== 4. 评价（该会员收到的） ===== */
const review = usePaged<C2cAdminReviewVO>(async (page, pageSize) => {
  const uid = uidOrUndefined();
  if (!uid) {
    return { content: [], total: 0 };
  }
  // 注意：评价接口的分页参数是 page（不是 pageNum）
  return findReviewsByPage({ revieweeUid: uid, page, pageSize });
});

/** 与 c2c-review 页面一致：4 个维度星级合并为一列 4 行 */
/** 与 c2c-review 页面列顺序一致：订单 → 评价方 → 交易类型 → 星级 → 内容 → 时间 */
const reviewColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'orderId', title: '订单号', width: 190, ellipsis: true },
  { colKey: 'reviewerUid', title: '评价人 UID' },
  { colKey: 'orderType', title: '交易类型' },
  { colKey: 'ratingDetail', title: '评分明细', width: 200 },
  { colKey: 'content', title: '评价内容' },
  { colKey: 'createTime', title: '评价时间' },
];

/* ===== 5. 收款账户 ===== */
const payment = usePaged<PaymentAccountRow>(async (page, pageSize) => {
  const uid = uidOrUndefined();
  if (!uid) {
    return { content: [], total: 0 };
  }
  return findPaymentAccountByPage({ memberUid: uid, pageNum: page, pageSize });
});

/** 与 payment-account 页面保持一致：支付方式 + 账户信息（多行） */
const paymentColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'paymentAccountTypeName', title: '支付方式' },
  { colKey: 'infoDetail', title: '账户信息' },
  { colKey: 'status', title: '状态' },
  { colKey: 'createTime', title: '创建时间' },
];

/* ===== 6. 风控记录 ===== */
const risk = usePaged<RiskRecordRow>(async (page, pageSize) => {
  const uid = uidOrUndefined();
  if (!uid) {
    return { content: [], total: 0 };
  }
  return findRiskRecordByPage({ memberUid: uid, pageNum: page, pageSize });
});

/** 与 c2c-risk-record 页面一致：原因 / 命中次数 / 处罚 / 违规层级 / 触发 / 结束 */
const riskColumns: PrimaryTableCol<TableRowData>[] = [
  { colKey: 'riskCauseName', title: '风控原因' },
  { colKey: 'hitCount', title: '命中次数', align: 'right' },
  { colKey: 'riskPunishName', title: '处罚' },
  { colKey: 'riskLevel', title: '违规层级' },
  { colKey: 'createTime', title: '触发时间' },
  { colKey: 'riskFinishTime', title: '风控结束' },
];

/* ===== 懒加载：每个二级 tab 首次进入才请求 ===== */
const loadedMap = ref<Record<SubTab, boolean>>({
  order: false,
  preOrder: false,
  appeal: false,
  review: false,
  payment: false,
  risk: false,
});

const loaders: Record<SubTab, () => Promise<void>> = {
  order: () => order.load(),
  preOrder: () => preOrder.load(),
  appeal: () => appeal.load(),
  review: () => review.load(),
  payment: () => payment.load(),
  risk: () => risk.load(),
};

const resetters: Record<SubTab, () => void> = {
  order: () => order.reset(),
  preOrder: () => preOrder.reset(),
  appeal: () => appeal.reset(),
  review: () => review.reset(),
  payment: () => payment.reset(),
  risk: () => risk.reset(),
};

function ensureLoaded(tab: SubTab) {
  if (!loadedMap.value[tab]) {
    loadedMap.value[tab] = true;
    loaders[tab]();
  }
}

watch(activeSubTab, tab => ensureLoaded(tab));

watch(
  () => [props.memberId, props.memberUid] as const,
  () => {
    (Object.keys(loadedMap.value) as SubTab[]).forEach((tab) => {
      loadedMap.value[tab] = false;
      resetters[tab]();
    });
    ensureLoaded(activeSubTab.value);
  },
  { immediate: true },
);

/* ===== 单元格渲染 ===== */
function orderSymbol(row: C2cOrderRow): string {
  if (row.fiatCode && row.assetCode) {
    return `${row.fiatCode}-${row.assetCode}`;
  }
  return row.assetCode || '—';
}

/** 平台是否认可该申诉：以权威字段 approveAppeal 为准（approveAppealName 后端不返回） */
function appealVerdictTag(row: C2cAppealRecordRow): { text: string, theme: 'success' | 'danger' } | null {
  if (row.approveAppeal === true) {
    return { text: '认可申诉', theme: 'success' };
  }
  if (row.approveAppeal === false) {
    return { text: '不认可申诉', theme: 'danger' };
  }
  return null;
}

function preOrderSymbol(row: PreOrderRow): string {
  if (row.fiatCode && row.assetCode) {
    return `${row.fiatCode}-${row.assetCode}`;
  }
  return row.assetCode || '—';
}

/** 广告数量进度（与 c2c-pre-order 页面 amountSummary 逻辑一致） */
function amountSummary(row: PreOrderRow): { primary: string, secondary: string } {
  const amount = row.amount ?? 0;
  const available = row.availableAmount ?? amount;
  const completed = row.completedAmount ?? 0;
  const trading = row.tradingAmount ?? 0;

  if (row.status === '1') {
    return {
      primary: `可交易 ${available}`,
      secondary: `总量 ${amount} · 已交易 ${completed} · 交易中 ${trading}`,
    };
  }
  if (row.status === '2') {
    return {
      primary: `已交易 ${completed}`,
      secondary: `总量 ${amount}`,
    };
  }
  return {
    primary: `总量 ${amount}`,
    secondary: completed > 0 ? `已交易 ${completed}` : '',
  };
}


</script>

<template>
  <div class="tab-body">
    <t-tabs v-model="activeSubTab">
      <!-- 订单 -->
      <t-tab-panel value="order" label="订单">
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
          <template #orderInfo="{ row }">
            <div class="table-stack">
              <OrderNoCell :value="row.orderNo" />
              <div class="table-muted">
                买家 {{ row.buyerUid ?? '—' }} · 卖家 {{ row.sellerUid ?? '—' }}
              </div>
            </div>
          </template>
          <template #symbol="{ row }">{{ orderSymbol(row) }}</template>
          <template #amount="{ row }">
            {{ row.amount ?? '—' }} <span class="unit">{{ row.assetCode || '' }}</span>
          </template>
          <template #price="{ row }">
            {{ row.price ?? '—' }} <span class="unit">{{ row.fiatCode || '' }}</span>
          </template>
          <template #fiatTotal="{ row }">
            共 {{ row.fiatAmount ?? '—' }} <span class="unit">{{ row.fiatCode || '' }}</span>
          </template>
          <template #paymentMethod="{ row }">
            <t-tag v-if="row.paymentAccountTypeName" size="small" variant="light" theme="primary">
              {{ row.paymentAccountTypeName }}
            </t-tag>
            <span v-else class="table-muted">—</span>
          </template>
          <template #status="{ row }">
            <t-tag size="small" variant="light" :theme="c2cOrderStatusTheme(row.status)">
              {{ row.statusName || row.status || '—' }}
            </t-tag>
          </template>
        </t-table>
      </t-tab-panel>

      <!-- 广告 -->
      <t-tab-panel value="preOrder" label="广告" lazy>
        <t-table
          :data="preOrder.rows.value"
          :columns="preOrderColumns"
          row-key="id"
          size="small"
          table-layout="auto"
          bordered
          :loading="preOrder.loading.value"
          :pagination="preOrder.pagination.value"
          @page-change="preOrder.onPageChange"
        >
          <template #orderInfo="{ row }">
            <div class="table-stack">
              <OrderNoCell :value="row.orderNo" />
              <div class="table-muted">{{ preOrderSymbol(row) }}</div>
            </div>
          </template>
          <template #price="{ row }">
            <div class="table-stack">
              <div class="order-tags">
                <t-tag size="small" variant="light" :theme="row.orderType === 'buy' ? 'success' : 'danger'">
                  {{ row.orderTypeName || '—' }}
                </t-tag>
                <t-tag v-if="row.assetCode" size="small" variant="light" theme="primary">
                  {{ row.assetCode }}
                </t-tag>
              </div>
              <div>
                {{ row.price ?? '—' }} <span class="unit">{{ row.fiatCode || '' }}</span>
                <t-tag
                  v-if="row.priceType === 'float' && row.premiumRate != null"
                  size="small"
                  variant="light"
                  theme="warning"
                  style="margin-left: 6px"
                >
                  {{ row.premiumRate > 0 ? '+' : '' }}{{ row.premiumRate }}%
                </t-tag>
              </div>
            </div>
          </template>
          <template #fiatTotal="{ row }">
            {{ row.fiatAmount ?? '—' }} <span class="unit">{{ row.fiatCode || '' }}</span>
          </template>
          <template #amountInfo="{ row }">
            <div class="table-stack">
              <div>{{ amountSummary(row).primary }}</div>
              <div v-if="amountSummary(row).secondary" class="table-muted">{{ amountSummary(row).secondary }}</div>
            </div>
          </template>
          <template #paymentTypes="{ row }">
            <div v-if="!row.supportedPaymentAccountTypes?.length" class="table-muted">—</div>
            <div v-else class="payment-tags">
              <t-tag v-for="pt in row.supportedPaymentAccountTypes" :key="pt.code" size="small" variant="light">
                <template #icon>
                  <img v-if="pt.logo" :src="pt.logo" class="payment-tag__icon" :alt="pt.name">
                </template>
                {{ pt.name || pt.code }}
              </t-tag>
            </div>
          </template>
          <template #status="{ row }">
            <t-tag size="small" variant="light" :theme="preOrderStatusTheme(row.status)">
              {{ row.statusName || row.status || '—' }}
            </t-tag>
          </template>
        </t-table>
      </t-tab-panel>

      <!-- 申诉 -->
      <t-tab-panel value="appeal" label="申诉" lazy>
        <t-table
          :data="appeal.rows.value"
          :columns="appealColumns"
          row-key="appealId"
          size="small"
          table-layout="auto"
          bordered
          :loading="appeal.loading.value"
          :pagination="appeal.pagination.value"
          @page-change="appeal.onPageChange"
        >
          <template #orderNo="{ row }">
            <OrderNoCell :value="row.orderNo" />
          </template>
          <template #processInfo="{ row }">
            <div class="table-stack">
              <t-tag
                v-if="appealVerdictTag(row)"
                size="small"
                variant="light"
                :theme="appealVerdictTag(row)!.theme"
              >
                {{ appealVerdictTag(row)!.text }}
              </t-tag>
              <div v-if="row.processWayName" class="table-muted">{{ row.processWayName }}</div>
              <div v-if="!appealVerdictTag(row) && !row.processWayName" class="table-muted">—</div>
            </div>
          </template>
        </t-table>
      </t-tab-panel>

      <!-- 评价 -->
      <t-tab-panel value="review" label="评价" lazy>
        <div class="hint">展示该会员作为被评价人收到的评价</div>
        <t-table
          :data="review.rows.value"
          :columns="reviewColumns"
          row-key="id"
          size="small"
          table-layout="auto"
          bordered
          :loading="review.loading.value"
          :pagination="review.pagination.value"
          @page-change="review.onPageChange"
        >
          <template #orderId="{ row }">
            <OrderNoCell :value="row.orderId" />
          </template>
          <template #ratingDetail="{ row }">
            <div class="rating-detail">
              <div class="rating-detail__row">
                <span class="rating-detail__label">交易速度</span>
                <t-rate :model-value="row.transactionSpeed" disabled size="small" />
              </div>
              <div class="rating-detail__row">
                <span class="rating-detail__label">服务态度</span>
                <t-rate :model-value="row.serviceAttitude" disabled size="small" />
              </div>
              <div class="rating-detail__row">
                <span class="rating-detail__label">价格合理</span>
                <t-rate :model-value="row.priceReasonable" disabled size="small" />
              </div>
              <div class="rating-detail__row">
                <span class="rating-detail__label">资金安全</span>
                <t-rate :model-value="row.fundSafety" disabled size="small" />
              </div>
            </div>
          </template>
          <template #content="{ row }">
            <span v-if="row.content" class="table-ellipsis" :title="row.content">{{ row.content }}</span>
            <span v-else class="table-muted">—</span>
          </template>
          <template #orderType="{ row }">
            <t-tag size="small" variant="light" :theme="row.orderType === 'buy' ? 'success' : 'danger'">
              {{ row.orderTypeName || '—' }}
            </t-tag>
          </template>
        </t-table>
      </t-tab-panel>

      <!-- 收款账户 -->
      <t-tab-panel value="payment" label="收款账户" lazy>
        <t-table
          :data="payment.rows.value"
          :columns="paymentColumns"
          row-key="id"
          size="small"
          table-layout="auto"
          bordered
          :loading="payment.loading.value"
          :pagination="payment.pagination.value"
          @page-change="payment.onPageChange"
        >
          <template #infoDetail="{ row }">
            <PaymentAccountCell :info="row" />
          </template>
          <template #status="{ row }">
            <t-tag size="small" :theme="row.status === '1' ? 'success' : 'default'">
              {{ row.statusName || row.status || '—' }}
            </t-tag>
          </template>
        </t-table>
      </t-tab-panel>

      <!-- 风控记录 -->
      <t-tab-panel value="risk" label="风控记录" lazy>
        <t-table
          :data="risk.rows.value"
          :columns="riskColumns"
          row-key="id"
          size="small"
          table-layout="auto"
          bordered
          :loading="risk.loading.value"
          :pagination="risk.pagination.value"
          @page-change="risk.onPageChange"
        >
          <template #riskPunishName="{ row }">
            <span>{{ row.riskPunishName || '—' }}</span>
            <t-tag
              v-if="row.manualRelievedFlag"
              theme="success"
              variant="light"
              size="small"
              style="margin-left: 6px"
            >
              已手动解除
            </t-tag>
          </template>
          <template #riskLevel="{ row }">
            <t-tag v-if="row.riskLevel" theme="warning" variant="light" size="small">
              第 {{ row.riskLevel }} 级
            </t-tag>
            <span v-else>—</span>
          </template>
          <template #riskFinishTime="{ row }">{{ row.riskFinishTime || '—' }}</template>
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

.hint {
  margin-bottom: 12px;
  font-size: 12px;
  color: var(--td-text-color-secondary);
}

/* 广告单价：方向 + 币种标签 */
.order-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}

/* 金额单位（币种/法币） */
.unit {
  font-size: 12px;
  color: var(--td-text-color-secondary);
}

/* 广告支持的支付方式（多个 tag） */
.payment-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

/* 支付方式 logo */
.payment-tag__icon {
  width: 14px;
  height: 14px;
  object-fit: contain;
  vertical-align: middle;
}

/* 评价评分明细：4 行星级 */
.rating-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rating-detail__row {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.rating-detail__label {
  flex-shrink: 0;
  width: 56px;
  font-size: 12px;
  color: var(--td-text-color-secondary);
}

/* 星级组件在 small 表格里略大，整体缩小一档 */
.rating-detail :deep(.t-rate) {
  transform: scale(0.85);
  transform-origin: left center;
}

/* 单元格内多行堆叠（申诉处理结果等） */
.table-stack {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: flex-start;
}

.table-muted {
  font-size: 12px;
  color: var(--td-text-color-secondary);
}

/* 评价内容：与 c2c-review 页面一致，单行省略 */
:deep(.table-ellipsis) {
  display: block;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
