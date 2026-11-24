<template>
  <div>
    <t-dialog v-model:visible="dialogVisible" header="永续订单详情" width="860px" placement="center" :footer="false" destroy-on-close>
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
            <t-descriptions-item label="订单类型">
              <t-tag size="small" variant="light" :theme="typeTheme(detail.type)">{{ detail.typeName || '—' }}</t-tag>
            </t-descriptions-item>
            <t-descriptions-item label="方向/委托/杠杆" :span="2">
              <div class="order-tags">
                <t-tag size="small" variant="light" :theme="detail.direction === 'long' ? 'success' : 'danger'">{{ detail.directionName || '—' }}</t-tag>
                <t-tag size="small" variant="outline" theme="warning">{{ detail.entrustTypeName || '—' }}</t-tag>
                <t-tag size="small" variant="outline">{{ detail.leverage ? `${detail.leverage}x` : '—' }}</t-tag>
              </div>
            </t-descriptions-item>
            <t-descriptions-item label="订单状态" :span="2">
              <t-tag size="small" variant="light" :theme="statusTheme(detail.status)">{{ detail.statusName || '—' }}</t-tag>
            </t-descriptions-item>
            <t-descriptions-item label="保证金">{{ detail.margin ?? '—' }} USDT</t-descriptions-item>
            <t-descriptions-item label="手续费">{{ detail.fee ?? '—' }} USDT</t-descriptions-item>
            <t-descriptions-item label="委托量">{{ detail.size ?? '—' }}</t-descriptions-item>
            <t-descriptions-item label="成交量">{{ detail.filledSize ?? '—' }}</t-descriptions-item>
            <t-descriptions-item label="委托价">{{ detail.price ?? '—' }} USDT</t-descriptions-item>
            <t-descriptions-item label="成交均价">{{ detail.avgPrice ?? '—' }} USDT</t-descriptions-item>
            <t-descriptions-item label="盈亏" :span="2">
              <span :class="pnlClass(detail.pnl)">{{ detail.pnl ?? '—' }}</span>
            </t-descriptions-item>
            <t-descriptions-item label="下单时间" :span="2">{{ detail.createTime || '—' }}</t-descriptions-item>
            <t-descriptions-item label="成交时间" :span="2">{{ detail.fillTime || '—' }}</t-descriptions-item>
            <t-descriptions-item v-if="detail.positionId" label="关联持仓ID" :span="2">
              <span class="detail-mono">{{ detail.positionId }}</span>
              <copy-icon-button :text="detail.positionId" />
            </t-descriptions-item>
          </t-descriptions>
        </template>
      </t-loading>
    </t-dialog>

    <member-account-detail-drawer v-model:visible="memberDetailVisible" :member-id="memberDetailId" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';

import type { PerpetualOrderRow } from '@/api/perpetualOrder';
import { findPerpetualOrderById } from '@/api/perpetualOrder';
import CopyIconButton from '@/components/copy-icon-button/index.vue';
import MemberAccountDetailDrawer from '@/components/member-account-detail-drawer/index.vue';

const props = defineProps<{
  visible: boolean;
  orderId: string;
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
}>();

const loading = ref(false);
const detail = ref<PerpetualOrderRow | null>(null);
const memberDetailVisible = ref(false);
const memberDetailId = ref('');

const dialogVisible = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value),
});

const statusTheme = (status?: string) => {
  if (status === '0') return 'warning';
  if (status === '1') return 'success';
  if (status === '2') return 'default';
  return 'warning';
};

const typeTheme = (type?: string) => {
  if (type === 'open') return 'primary';
  if (type === 'close') return 'success';
  if (type === 'liquidate') return 'danger';
  return 'default';
};

const pnlClass = (pnl?: number) => {
  if (pnl == null) return '';
  if (pnl > 0) return 'pnl-profit';
  if (pnl < 0) return 'pnl-loss';
  return '';
};

function openMemberDetail(memberId?: string) {
  if (!memberId) return;
  memberDetailId.value = memberId;
  memberDetailVisible.value = true;
}

watch(
  () => [props.visible, props.orderId] as const,
  async ([visible, orderId]) => {
    if (!visible || !orderId) {
      detail.value = null;
      return;
    }
    loading.value = true;
    try {
      detail.value = await findPerpetualOrderById(orderId);
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

.order-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
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
