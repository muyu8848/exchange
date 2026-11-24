<script setup lang="ts">
import { ref, watch } from 'vue';

import type { PrimaryTableCol } from 'tdesign-vue-next';
import { findDepositOrderByPage } from '@/api/depositOrder';
import type { DepositOrderRow } from '@/api/depositOrder';
import { findWithdrawOrderByPage } from '@/api/withdrawOrder';
import type { WithdrawOrderRow } from '@/api/withdrawOrder';

import CopyIconButton from '@/components/copy-icon-button/index.vue';
import OrderNoCell from './order-no-cell.vue';
import { usePaged } from './use-paged';

const props = defineProps<{
  memberUid?: number | string;
}>();

const activeSubTab = ref<'deposit' | 'withdraw'>('deposit');

/* ===== 充币 ===== */
/** 与 deposit-order 页面一致的状态标签配色 */
const depositStatusTheme = (status: string) => {
  if (status === '3') return 'success';
  if (status === '4' || status === '5') return 'default';
  if (status === '2') return 'primary';
  return 'warning';
};

const depositColumns: PrimaryTableCol<DepositOrderRow>[] = [
  { colKey: 'orderNo', title: '订单号', width: 180, ellipsis: true },
  { colKey: 'chainType', title: '链类型' },
  { colKey: 'depositAddress', title: '充币地址' },
  { colKey: 'amountInfo', title: '金额', align: 'right' },
  { colKey: 'statusInfo', title: '状态' },
  { colKey: 'timeInfo', title: '提交时间' },
];

const {
  rows: depositRows,
  loading: depositLoading,
  pagination: depositPagination,
  load: loadDeposit,
  onPageChange: onDepositPageChange,
  reset: resetDeposit,
} = usePaged<DepositOrderRow>(async (page, pageSize) => {
  const uid = String(props.memberUid ?? '');
  if (!uid) {
    return { content: [], total: 0 };
  }
  return findDepositOrderByPage({ memberUid: uid, pageNum: page, pageSize });
});

/* ===== 提币 ===== */
/** 与 withdraw-order 页面一致的状态标签配色 */
const withdrawStatusTheme = (status: string) => {
  if (status === '1') return 'warning';
  if (status === '2') return 'primary';
  if (status === '3') return 'success';
  if (status === '4') return 'danger';
  return 'default';
};

const withdrawColumns: PrimaryTableCol<WithdrawOrderRow>[] = [
  { colKey: 'orderNo', title: '订单号', width: 180, ellipsis: true },
  { colKey: 'amount', title: '提币数量', align: 'right' },
  { colKey: 'actualAmount', title: '实际到账', align: 'right' },
  { colKey: 'chainType', title: '链类型' },
  { colKey: 'withdrawalAddress', title: '提币地址' },
  { colKey: 'statusInfo', title: '状态' },
  { colKey: 'timeInfo', title: '提交时间' },
];

const {
  rows: withdrawRows,
  loading: withdrawLoading,
  pagination: withdrawPagination,
  load: loadWithdraw,
  onPageChange: onWithdrawPageChange,
  reset: resetWithdraw,
} = usePaged<WithdrawOrderRow>(async (page, pageSize) => {
  const uid = String(props.memberUid ?? '');
  if (!uid) {
    return { content: [], total: 0 };
  }
  return findWithdrawOrderByPage({ memberUid: uid, pageNum: page, pageSize });
});

/* ===== 二级 tab 切换：首次进入才加载 ===== */
const depositLoaded = ref(false);
const withdrawLoaded = ref(false);

watch(
  () => props.memberUid,
  () => {
    resetDeposit();
    resetWithdraw();
    depositLoaded.value = false;
    withdrawLoaded.value = false;
    if (activeSubTab.value === 'deposit') {
      loadDeposit();
      depositLoaded.value = true;
    }
    else {
      loadWithdraw();
      withdrawLoaded.value = true;
    }
  },
  { immediate: true },
);

watch(activeSubTab, (tab) => {
  if (tab === 'deposit' && !depositLoaded.value) {
    loadDeposit();
    depositLoaded.value = true;
  }
  if (tab === 'withdraw' && !withdrawLoaded.value) {
    loadWithdraw();
    withdrawLoaded.value = true;
  }
});
</script>

<template>
  <div class="tab-body">
    <t-tabs v-model="activeSubTab">
      <t-tab-panel value="deposit" label="充币记录">
        <t-table
          :data="depositRows"
          :columns="depositColumns"
          row-key="id"
          size="small"
          table-layout="auto"
          bordered
          :loading="depositLoading"
          :pagination="depositPagination"
          @page-change="onDepositPageChange"
        >
          <template #orderNo="{ row }">
            <OrderNoCell :value="row.orderNo" />
          </template>
          <template #depositAddress="{ row }">
            <div v-if="row.depositAddress" class="table-line">
              <t-link
                v-if="row.addressExplorerUrl"
                class="table-value table-mono table-break address-link"
                theme="primary"
                :href="row.addressExplorerUrl"
                target="_blank"
              >
                {{ row.depositAddress }}
              </t-link>
              <span v-else class="table-value table-mono table-break">{{ row.depositAddress }}</span>
              <CopyIconButton :text="row.depositAddress" />
            </div>
            <span v-else class="table-muted">—</span>
          </template>
          <template #amountInfo="{ row }">
            <div class="table-stack table-stack--amount">
              <div>充值：{{ row.amount }} {{ row.assetCode }}</div>
              <div v-if="row.actualAmount">实收：{{ row.actualAmount }} {{ row.assetCode }}</div>
            </div>
          </template>
          <template #statusInfo="{ row }">
            <t-tag size="small" variant="light" :theme="depositStatusTheme(row.status)">
              {{ row.statusName || '—' }}
            </t-tag>
          </template>
          <template #timeInfo="{ row }">
            <span>{{ row.createTime }}</span>
          </template>
        </t-table>
      </t-tab-panel>
      <t-tab-panel value="withdraw" label="提币记录" lazy>
        <t-table
          :data="withdrawRows"
          :columns="withdrawColumns"
          row-key="id"
          size="small"
          table-layout="auto"
          bordered
          :loading="withdrawLoading"
          :pagination="withdrawPagination"
          @page-change="onWithdrawPageChange"
        >
          <template #orderNo="{ row }">
            <OrderNoCell :value="row.orderNo" />
          </template>
          <template #amount="{ row }">
            {{ row.amount }} {{ row.assetCode }}
          </template>
          <template #actualAmount="{ row }">
            <div class="table-stack table-stack--amount">
              <div v-if="row.actualAmount != null">{{ row.actualAmount }} {{ row.assetCode }}</div>
              <div v-else class="table-muted">—</div>
              <div v-if="row.fee != null" class="amount-fee">手续费：{{ row.fee }}</div>
            </div>
          </template>
          <template #withdrawalAddress="{ row }">
            <div v-if="row.withdrawalAddress" class="table-line">
              <t-link
                v-if="row.addressExplorerUrl"
                class="table-value table-mono table-break address-link"
                theme="primary"
                :href="row.addressExplorerUrl"
                target="_blank"
              >
                {{ row.withdrawalAddress }}
              </t-link>
              <span v-else class="table-value table-mono table-break">{{ row.withdrawalAddress }}</span>
              <CopyIconButton :text="row.withdrawalAddress" />
            </div>
            <span v-else class="table-muted">—</span>
          </template>
          <template #statusInfo="{ row }">
            <t-tag size="small" variant="light" :theme="withdrawStatusTheme(row.status)">
              {{ row.statusName || '—' }}
            </t-tag>
          </template>
          <template #timeInfo="{ row }">
            <span>{{ row.createTime }}</span>
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

.table-stack {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  font-size: 13px;
  &--amount {
    align-items: flex-end;
    text-align: right;
  }
}

.table-line {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  min-width: 0;
}

.address-link {
  flex: 1;
  min-width: 0;
  word-break: break-all;
  white-space: normal;
}

.table-value {
  flex: 1;
  min-width: 0;
  color: var(--td-text-color-primary);
}

.table-mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
}

.table-break {
  word-break: break-all;
  white-space: normal;
}

.table-muted {
  color: var(--td-text-color-placeholder);
}

.amount-fee {
  color: var(--td-text-color-placeholder);
  font-size: 12px;
}
</style>
