<template>
  <div>
    <t-drawer v-model:visible="drawerVisible" header="订单详情" size="520px" :footer="false" destroy-on-close>
    <t-loading :loading="loading" size="small">
      <template v-if="order">
        <t-descriptions bordered :column="1" :table-layout="'fixed'" :colon="false" :label-style="labelStyle" title="订单信息" class="detail-block">
          <t-descriptions-item label="订单号">
            {{ order.orderNo }}
            <copy-icon-button :text="order.orderNo" />
          </t-descriptions-item>
          <t-descriptions-item label="订单状态">
            <t-tag size="small" variant="light" :theme="statusTheme">{{ order.statusName }}</t-tag>
          </t-descriptions-item>
          <t-descriptions-item label="交易对">{{ order.symbolCode || '—' }}</t-descriptions-item>
          <t-descriptions-item label="方向">
            <t-tag size="small" variant="light" :theme="directionTheme">{{ order.directionName || '—' }}</t-tag>
          </t-descriptions-item>
        </t-descriptions>

        <t-descriptions bordered :column="1" :table-layout="'fixed'" :colon="false" :label-style="labelStyle" title="会员信息" class="detail-block">
          <t-descriptions-item label="会员账号">
            <t-link v-if="order.memberUid != null" theme="primary" hover="color" @click="openMemberDetail(order.memberAccountId)">{{ order.memberUid }}</t-link>
            <span v-else class="text-placeholder">—</span>
          </t-descriptions-item>
        </t-descriptions>

        <t-descriptions bordered :column="1" :table-layout="'fixed'" :colon="false" :label-style="labelStyle" title="金额信息" class="detail-block">
          <t-descriptions-item label="下注金额">
            <span class="detail-value--highlight">{{ order.amount }}</span>
          </t-descriptions-item>
          <t-descriptions-item label="开仓价">{{ order.openPrice ?? '—' }}</t-descriptions-item>
          <t-descriptions-item v-if="order.closePrice != null" label="平仓价">{{ order.closePrice }}</t-descriptions-item>
          <t-descriptions-item label="赔率">{{ order.odds ?? '—' }}</t-descriptions-item>
          <t-descriptions-item label="合约周期">{{ order.period ? order.period + ' 秒' : '—' }}</t-descriptions-item>
          <t-descriptions-item label="盈亏">
            <span :class="pnlClass(order.pnl)">{{ order.pnl ?? '—' }}</span>
          </t-descriptions-item>
        </t-descriptions>

        <t-descriptions bordered :column="1" :table-layout="'fixed'" :colon="false" :label-style="labelStyle" title="时间信息" class="detail-block">
          <t-descriptions-item label="开仓时间">{{ order.openTime || '—' }}</t-descriptions-item>
          <t-descriptions-item label="预计平仓">{{ order.expectCloseTime || '—' }}</t-descriptions-item>
          <t-descriptions-item v-if="order.closeTime" label="平仓时间">{{ order.closeTime }}</t-descriptions-item>
        </t-descriptions>
      </template>
    </t-loading>
    </t-drawer>

    <member-account-detail-drawer v-model:visible="memberDetailVisible" :member-id="memberDetailId" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';

import type { SecondContractOrderRow } from '@/api/secondContractOrder';
import { getSecondContractOrderDetail } from '@/api/secondContractOrder';
import CopyIconButton from '@/components/copy-icon-button/index.vue';
import MemberAccountDetailDrawer from '@/components/member-account-detail-drawer/index.vue';

const labelStyle = { width: '120px' };

const props = defineProps<{
  visible: boolean;
  orderId: string;
}>();

const emit = defineEmits<{
  'update:visible': [visible: boolean];
}>();

const loading = ref(false);
const order = ref<SecondContractOrderRow | null>(null);
const memberDetailVisible = ref(false);
const memberDetailId = ref('');

const drawerVisible = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value),
});

const statusTheme = computed(() => {
  const status = order.value?.status;
  if (status === '1') {
    return 'warning';
  }
  if (status === '2') {
    return 'success';
  }
  return 'default';
});

const directionTheme = computed(() => {
  const dir = order.value?.direction;
  if (dir === 'long') {
    return 'success';
  }
  if (dir === 'short') {
    return 'danger';
  }
  return 'default';
});

const pnlClass = (pnl?: number) => {
  if (pnl == null) {
    return '';
  }
  if (pnl > 0) {
    return 'pnl-profit';
  }
  if (pnl < 0) {
    return 'pnl-loss';
  }
  return '';
};

function openMemberDetail(memberId?: string) {
  if (!memberId) return;
  memberDetailId.value = memberId;
  memberDetailVisible.value = true;
}

const loadDetail = async () => {
  if (!props.orderId) {
    order.value = null;
    return;
  }
  loading.value = true;
  try {
    order.value = await getSecondContractOrderDetail(props.orderId);
  } finally {
    loading.value = false;
  }
};

watch(
  () => [props.visible, props.orderId] as const,
  ([visible, orderId]) => {
    if (visible && orderId) {
      loadDetail();
      return;
    }
    order.value = null;
  },
);
</script>

<style lang="less" scoped>
.detail-block {
  margin-bottom: 24px;
}

.detail-value--highlight {
  font-weight: 600;
}

.pnl-profit {
  color: var(--td-success-color);
  font-weight: 600;
}

.pnl-loss {
  color: var(--td-error-color);
  font-weight: 600;
}

.text-placeholder {
  color: var(--td-text-color-placeholder);
  font-size: 13px;
}
</style>
