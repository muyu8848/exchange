<template>
  <div>
    <t-dialog v-model:visible="dialogVisible" header="永续持仓详情" width="860px" placement="center" :footer="false" destroy-on-close>
      <t-loading :loading="loading" size="small">
        <template v-if="detail">
          <t-descriptions bordered :column="2" :table-layout="'auto'" :colon="false" size="small" class="detail-block">
            <t-descriptions-item label="订单号" :span="2">
              <span class="detail-mono">{{ detail.orderNo }}</span>
              <copy-icon-button :text="detail.orderNo" />
            </t-descriptions-item>
            <t-descriptions-item label="会员账号" :span="2">
              <t-link v-if="detail.memberUid != null" theme="primary" hover="color" @click="openMemberDetail(detail.memberAccountId)">{{ detail.memberUid }}</t-link>
              <span v-else class="text-placeholder">—</span>
            </t-descriptions-item>
            <t-descriptions-item label="交易对">{{ detail.symbolCode }}</t-descriptions-item>
            <t-descriptions-item label="方向">
              <t-tag size="small" variant="light" :theme="detail.direction === 'long' ? 'success' : 'danger'">{{ detail.directionName || '—' }}</t-tag>
            </t-descriptions-item>
            <t-descriptions-item label="杠杆">{{ detail.leverage }}x</t-descriptions-item>
            <t-descriptions-item label="状态">
              <t-tag size="small" variant="light" :theme="statusTheme(detail.status)">{{ detail.statusName || '—' }}</t-tag>
            </t-descriptions-item>
            <t-descriptions-item label="开仓均价">{{ detail.openPrice ?? '—' }} USDT</t-descriptions-item>
            <t-descriptions-item label="标记价">{{ detail.markPrice ?? '—' }} USDT</t-descriptions-item>
            <t-descriptions-item label="强平价">{{ detail.liquidationPrice ?? '—' }} USDT</t-descriptions-item>
            <t-descriptions-item label="保证金率">
              <span :class="ratioClass(detail.marginRatio)">{{ detail.marginRatio != null ? (detail.marginRatio * 100).toFixed(2) + '%' : '—' }}</span>
            </t-descriptions-item>
            <t-descriptions-item label="保证金">{{ detail.margin ?? '—' }} USDT</t-descriptions-item>
            <t-descriptions-item label="仓位大小">{{ detail.size ?? '—' }}</t-descriptions-item>
            <t-descriptions-item label="未实现盈亏">
              <span :class="pnlClass(detail.unrealizedPnl)">{{ detail.unrealizedPnl ?? '—' }} USDT</span>
            </t-descriptions-item>
            <t-descriptions-item label="收益率">
              <span :class="pnlClass(detail.unrealizedPnl)">{{ detail.pnlRate != null ? (detail.pnlRate >= 0 ? '+' : '') + detail.pnlRate.toFixed(2) + '%' : '—' }}</span>
            </t-descriptions-item>
            <t-descriptions-item label="已实现盈亏">
              <span :class="pnlClass(detail.realizedPnl)">{{ detail.realizedPnl ?? '—' }} USDT</span>
            </t-descriptions-item>
            <t-descriptions-item label="累计资金费">{{ detail.accumulatedFunding ?? '—' }} USDT</t-descriptions-item>
            <t-descriptions-item label="开仓时间">{{ detail.openTime || '—' }}</t-descriptions-item>
            <t-descriptions-item v-if="detail.closeTime" label="平仓时间">{{ detail.closeTime }}</t-descriptions-item>
            <t-descriptions-item v-if="detail.closePrice" label="平仓价" :span="2">{{ detail.closePrice }} USDT</t-descriptions-item>
          </t-descriptions>
        </template>
      </t-loading>
    </t-dialog>

    <member-account-detail-drawer v-model:visible="memberDetailVisible" :member-id="memberDetailId" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';

import type { PerpetualPositionRow } from '@/api/perpetualPosition';
import { findPerpetualPositionById } from '@/api/perpetualPosition';
import CopyIconButton from '@/components/copy-icon-button/index.vue';
import MemberAccountDetailDrawer from '@/components/member-account-detail-drawer/index.vue';

const props = defineProps<{
  visible: boolean;
  positionId: string;
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
}>();

const loading = ref(false);
const detail = ref<PerpetualPositionRow | null>(null);
const memberDetailVisible = ref(false);
const memberDetailId = ref('');

const dialogVisible = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value),
});

const statusTheme = (status?: string) => {
  if (status === '1') return 'warning';
  if (status === '2') return 'success';
  if (status === '3') return 'danger';
  return 'warning';
};

const pnlClass = (val?: number) => {
  if (val == null) return '';
  if (val > 0) return 'pnl-profit';
  if (val < 0) return 'pnl-loss';
  return '';
};

const ratioClass = (val?: number) => {
  if (val == null) return '';
  if (val > 0.1) return 'pnl-profit';
  if (val > 0.05) return '';
  return 'pnl-loss';
};

function openMemberDetail(memberId?: string) {
  if (!memberId) return;
  memberDetailId.value = memberId;
  memberDetailVisible.value = true;
}

watch(
  () => [props.visible, props.positionId] as const,
  async ([visible, positionId]) => {
    if (!visible || !positionId) {
      detail.value = null;
      return;
    }
    loading.value = true;
    try {
      detail.value = await findPerpetualPositionById(positionId);
    } finally {
      loading.value = false;
    }
  },
);
</script>

<style lang="less" scoped>
.detail-block {
  margin-bottom: 24px;
}

.detail-mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
  word-break: break-all;
}

.text-placeholder {
  color: var(--td-text-color-placeholder);
  font-size: 13px;
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
