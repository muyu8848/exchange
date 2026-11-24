<template>
  <div>
    <t-drawer v-model:visible="drawerVisible" header="投资记录详情" size="520px" :footer="false" destroy-on-close>
      <t-loading :loading="loading" size="small">
        <template v-if="detail">
          <t-descriptions
            bordered
            :column="1"
            :table-layout="'fixed'"
            :colon="false"
            :label-style="labelStyle"
            title="订单信息"
            class="detail-block"
          >
            <t-descriptions-item label="订单号">
              <span class="detail-mono">{{ detail.orderNo }}</span>
              <copy-icon-button :text="detail.orderNo" />
            </t-descriptions-item>
            <t-descriptions-item label="申购时间">{{ detail.createTime || '—' }}</t-descriptions-item>
          </t-descriptions>

          <t-descriptions
            bordered
            :column="1"
            :table-layout="'fixed'"
            :colon="false"
            :label-style="labelStyle"
            title="会员信息"
            class="detail-block"
          >
            <t-descriptions-item label="会员账号">
              <t-link v-if="detail.memberUid != null" theme="primary" hover="color" @click="openMemberDetail(detail.memberAccountId)">
                {{ detail.memberUid }}
              </t-link>
              <span v-else class="text-placeholder">—</span>
            </t-descriptions-item>
          </t-descriptions>

          <t-descriptions
            bordered
            :column="1"
            :table-layout="'fixed'"
            :colon="false"
            :label-style="labelStyle"
            title="产品信息"
            class="detail-block"
          >
            <t-descriptions-item label="币种">{{ detail.assetCode || '—' }}</t-descriptions-item>
            <t-descriptions-item label="产品类型">
              <t-tag size="small" variant="light-outline">{{ detail.productTypeName || '—' }}</t-tag>
              <t-tag v-if="detail.productType === 'FIXED' && detail.lockDays != null" size="small" variant="light" theme="warning" style="margin-left: 4px">
                锁{{ detail.lockDays }}天
              </t-tag>
            </t-descriptions-item>
            <t-descriptions-item label="年化收益率">{{ detail.annualRate }}%</t-descriptions-item>
            <t-descriptions-item v-if="detail.productType === 'FLEXIBLE'" label="补贴年化">{{ detail.subsidizedRate || '—' }}%</t-descriptions-item>
            <t-descriptions-item v-if="detail.productType === 'FLEXIBLE'" label="奖励限额">{{ detail.rewardLimit || '—' }}</t-descriptions-item>
          </t-descriptions>

          <t-descriptions
            bordered
            :column="1"
            :table-layout="'fixed'"
            :colon="false"
            :label-style="labelStyle"
            title="金额信息"
            class="detail-block"
          >
            <t-descriptions-item label="申购本金">
              <span class="detail-value--highlight">{{ detail.principal }}</span>
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

import type { InvestmentRecordRow } from '@/api/investmentRecord';
import { getInvestmentRecordDetail } from '@/api/investmentRecord';
import CopyIconButton from '@/components/copy-icon-button/index.vue';
import MemberAccountDetailDrawer from '@/components/member-account-detail-drawer/index.vue';

const labelStyle = { width: '120px' };

const props = defineProps<{
  visible: boolean;
  recordId: string;
}>();

const emit = defineEmits<{
  'update:visible': [visible: boolean];
}>();

const loading = ref(false);
const detail = ref<InvestmentRecordRow | null>(null);
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

const loadDetail = async () => {
  if (!props.recordId) {
    detail.value = null;
    return;
  }
  loading.value = true;
  try {
    detail.value = await getInvestmentRecordDetail(props.recordId);
  } finally {
    loading.value = false;
  }
};

watch(
  () => [props.visible, props.recordId] as const,
  ([visible, recordId]) => {
    if (visible && recordId) {
      loadDetail();
      return;
    }
    detail.value = null;
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

.detail-mono {
  font-family: monospace;
}

.text-placeholder {
  color: var(--td-text-color-placeholder);
  font-size: 13px;
}
</style>
