<template>
  <div>
    <t-drawer v-model:visible="drawerVisible" header="永续计划委托详情" size="480px" :footer="false" destroy-on-close>
      <t-loading :loading="loading" size="small">
        <template v-if="detail">
          <t-descriptions bordered :column="1" :table-layout="'fixed'" :colon="false" :label-style="labelStyle" title="订单信息" class="detail-block">
            <t-descriptions-item label="订单号">
              <span class="detail-mono">{{ detail.orderNo }}</span>
              <copy-icon-button :text="detail.orderNo" />
            </t-descriptions-item>
            <t-descriptions-item label="会员账号">
              <t-link v-if="detail.memberUid != null" theme="primary" hover="color" @click="openMemberDetail(detail.memberAccountId)">{{ detail.memberUid }}</t-link>
              <span v-else class="text-placeholder">—</span>
            </t-descriptions-item>
            <t-descriptions-item label="交易对">{{ detail.symbolCode }}</t-descriptions-item>
            <t-descriptions-item label="类型">
              <t-tag size="small" variant="light" :theme="detail.type === 'open' ? 'primary' : 'warning'">{{ detail.typeName }}</t-tag>
            </t-descriptions-item>
            <t-descriptions-item label="方向">
              <t-tag size="small" variant="light" :theme="detail.direction === 'long' ? 'success' : 'danger'">{{ detail.directionName }}</t-tag>
            </t-descriptions-item>
            <t-descriptions-item label="来源">
              <t-tag v-if="detail.sourceName" size="small" variant="light" :theme="sourceTheme(detail.source)">{{ detail.sourceName }}</t-tag>
              <span v-else class="text-placeholder">—</span>
            </t-descriptions-item>
            <t-descriptions-item label="状态">
              <t-tag size="small" variant="light" :theme="statusTheme(detail.status)">{{ detail.statusName }}</t-tag>
            </t-descriptions-item>
          </t-descriptions>

          <t-descriptions bordered :column="1" :table-layout="'fixed'" :colon="false" :label-style="labelStyle" title="委托信息" class="detail-block">
            <t-descriptions-item label="触发价">
              {{ detail.triggerType === 'ge' ? '≥' : '≤' }} {{ detail.triggerPrice }}
            </t-descriptions-item>
            <t-descriptions-item label="执行方式">
              {{ detail.execType === 'market' ? '触发市价' : `触发限价 ${detail.price ?? '—'}` }}
            </t-descriptions-item>
            <t-descriptions-item v-if="detail.type === 'open'" label="杠杆">{{ detail.leverage }}x</t-descriptions-item>
            <t-descriptions-item v-if="detail.type === 'open'" label="保证金">{{ detail.margin ?? '—' }} USDT</t-descriptions-item>
          </t-descriptions>

          <t-descriptions bordered :column="1" :table-layout="'fixed'" :colon="false" :label-style="labelStyle" title="时间信息" class="detail-block">
            <t-descriptions-item label="委托时间">{{ detail.createTime || '—' }}</t-descriptions-item>
            <t-descriptions-item label="触发时间">{{ detail.triggerTime || '—' }}</t-descriptions-item>
          </t-descriptions>

          <t-descriptions v-if="detail.perpetualOrderId" bordered :column="1" :table-layout="'fixed'" :colon="false" :label-style="labelStyle" title="关联订单" class="detail-block">
            <t-descriptions-item label="永续订单ID">
              <span class="detail-mono">{{ detail.perpetualOrderId }}</span>
              <copy-icon-button :text="detail.perpetualOrderId" />
            </t-descriptions-item>
          </t-descriptions>
        </template>
      </t-loading>
    </t-drawer>

    <member-account-detail-drawer v-model:visible="memberDetailVisible" :member-id="memberDetailId" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';

import type { PerpetualPlanOrderRow } from '@/api/perpetualPlanOrder';
import { findPerpetualPlanOrderById } from '@/api/perpetualPlanOrder';
import CopyIconButton from '@/components/copy-icon-button/index.vue';
import MemberAccountDetailDrawer from '@/components/member-account-detail-drawer/index.vue';

const labelStyle = { width: '120px' };

const props = defineProps<{
  visible: boolean;
  orderId: string;
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
}>();

const loading = ref(false);
const detail = ref<PerpetualPlanOrderRow | null>(null);
const memberDetailVisible = ref(false);
const memberDetailId = ref('');

const drawerVisible = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value),
});

function openMemberDetail(memberId?: string) {
  if (!memberId) return;
  memberDetailId.value = memberId;
  memberDetailVisible.value = true;
}

const statusTheme = (status?: string) => {
  if (status === '1') return 'success';
  if (status === '2') return 'default';
  if (status === '3') return 'danger';
  return 'warning';
};

/** 来源配色：止盈绿、止损红、普通计划单中性 */
const sourceTheme = (source?: string) => {
  if (source === 'tp') return 'success';
  if (source === 'sl') return 'danger';
  return 'default';
};

watch(
  () => [props.visible, props.orderId] as const,
  async ([visible, orderId]) => {
    if (!visible || !orderId) {
      detail.value = null;
      return;
    }
    loading.value = true;
    try {
      detail.value = await findPerpetualPlanOrderById(orderId);
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
</style>
