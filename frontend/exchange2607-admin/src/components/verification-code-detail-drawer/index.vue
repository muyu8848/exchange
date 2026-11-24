<template>
  <t-drawer
    v-model:visible="drawerVisible"
    header="验证码发送详情"
    size="520px"
    :footer="false"
    destroy-on-close
  >
    <template v-if="record">
      <t-descriptions
        bordered
        :column="1"
        :table-layout="'fixed'"
        :colon="false"
        :label-style="labelStyle"
        title="基本信息"
        class="detail-block"
      >
        <t-descriptions-item label="业务类型">{{ record.bizTypeName || '—' }}</t-descriptions-item>
        <t-descriptions-item label="联系方式">
          <span class="detail-mono">{{ formatContact(record) }}</span>
        </t-descriptions-item>
        <t-descriptions-item label="验证码">
          <template v-if="record.verificationCode">
            <span class="detail-mono">{{ record.verificationCode }}</span>
            <copy-icon-button :text="record.verificationCode" />
          </template>
          <span v-else class="text-placeholder">—</span>
        </t-descriptions-item>
        <t-descriptions-item label="IP">
          <span v-if="record.ip" class="detail-mono">{{ record.ip }}</span>
          <span v-else class="text-placeholder">—</span>
        </t-descriptions-item>
      </t-descriptions>

      <t-descriptions
        bordered
        :column="1"
        :table-layout="'fixed'"
        :colon="false"
        :label-style="labelStyle"
        title="发送信息"
        class="detail-block"
      >
        <t-descriptions-item label="发送状态">
          <t-tag size="small" variant="light" :theme="statusTheme(record.sendStatus)">
            {{ record.sendStatusName || '—' }}
          </t-tag>
        </t-descriptions-item>
        <t-descriptions-item label="创建时间">{{ record.createTime || '—' }}</t-descriptions-item>
        <t-descriptions-item label="发送时间">{{ record.sendTime || '—' }}</t-descriptions-item>
      </t-descriptions>

      <t-descriptions
        bordered
        :column="1"
        :table-layout="'fixed'"
        :colon="false"
        :label-style="labelStyle"
        title="错误原因"
        class="detail-block"
      >
        <t-descriptions-item label="错误详情">
          <div v-if="record.errorMsg" class="error-wrap">
            <pre class="error-text">{{ record.errorMsg }}</pre>
            <copy-icon-button :text="record.errorMsg" />
          </div>
          <span v-else class="text-placeholder">无</span>
        </t-descriptions-item>
      </t-descriptions>
    </template>
  </t-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue';

import type { VerificationCodeSendRecordRow } from '@/api/verificationCodeSendRecord';
import CopyIconButton from '@/components/copy-icon-button/index.vue';

const props = defineProps<{
  visible: boolean;
  record: VerificationCodeSendRecordRow | null;
}>();

const emit = defineEmits<{
  'update:visible': [visible: boolean];
}>();

const labelStyle = { width: '100px' };

const drawerVisible = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value),
});

function formatContact(record: VerificationCodeSendRecordRow) {
  if (!record.contact) {
    return '—';
  }
  return record.phoneAreaCode ? `+${record.phoneAreaCode} ${record.contact}` : record.contact;
}

// sendStatus: 1 未发送 / 2 发送成功 / 3 发送失败（对应后端 Constant.验证码发送状态_*）
function statusTheme(status?: string) {
  if (status === '2') {
    return 'success';
  }
  if (status === '3') {
    return 'danger';
  }
  return 'warning';
}
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

.error-wrap {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  width: 100%;
}

.error-text {
  flex: 1;
  min-width: 0;
  max-height: 320px;
  margin: 0;
  padding: 8px 12px;
  overflow: auto;
  border-radius: 6px;
  background: var(--td-bg-color-secondarycontainer);
  color: var(--td-text-color-primary);
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
