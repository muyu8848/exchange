<template>
  <t-dialog
    :visible="visible"
    :header="isPending ? '实名审核' : '实名详情'"
    width="720px"
    :footer="isPending"
    @update:visible="onVisibleChange"
    placement="center"
  >
    <template v-if="record">
      <t-descriptions bordered :column="1" :colon="false" :label-style="labelStyle" class="audit-block">
        <t-descriptions-item label="会员账号">{{ record.memberUid ?? '—' }}</t-descriptions-item>
        <t-descriptions-item label="真实姓名">{{ record.realName || '—' }}</t-descriptions-item>
        <t-descriptions-item label="证件类型">{{ record.idTypeName || '—' }}</t-descriptions-item>
        <t-descriptions-item label="证件号码">
          <span class="audit-mono">{{ record.idNumber || '—' }}</span>
        </t-descriptions-item>
        <t-descriptions-item label="证件有效期">
          <t-tag v-if="expired" size="small" variant="light" theme="danger">
            {{ record.expiryDate }} 已过期
          </t-tag>
          <span v-else>{{ record.expiryDate || '—' }}</span>
        </t-descriptions-item>
        <t-descriptions-item label="提交时间">
          <span>{{ record.createTime || '—' }}</span>
          <span
            v-if="isPending && waitingText"
            :class="overdue ? 'audit-waiting audit-waiting--long' : 'audit-waiting'"
          >
            （已等待 {{ waitingText }}）
          </span>
        </t-descriptions-item>
        <t-descriptions-item v-if="record.failReason" label="驳回原因">
          <span class="audit-danger">{{ record.failReason }}</span>
        </t-descriptions-item>
        <t-descriptions-item v-if="record.dealTime" label="审核时间">{{ record.dealTime }}</t-descriptions-item>
      </t-descriptions>

      <div class="audit-block">
        <div class="audit-block__title">证件照片</div>
        <div v-if="picList.length" class="audit-pics">
          <div v-for="(pic, idx) in picList" :key="idx" class="audit-pic" @click="openViewer(idx)">
            <t-image :src="pic" fit="cover" class="audit-pic__img" />
            <span class="audit-pic__tag">{{ picLabel(idx) }}</span>
          </div>
        </div>
        <div v-else class="audit-empty">未上传证件照片</div>
      </div>
    </template>

    <template #footer>
      <div class="audit-footer">
        <!-- 默认态：两个入口（无审核权限时只读，弹窗从「详情」进入也看不到审核入口） -->
        <template v-if="step === 'view'">
          <div v-if="hasPermission('memberAuthenticationInfo.audit')" class="audit-footer__actions">
            <t-button theme="warning" variant="outline" @click="step = 'reject'">认证驳回</t-button>
            <t-button theme="success" @click="step = 'approve'">认证通过</t-button>
          </div>
        </template>

        <!-- 通过确认：信息与照片保留在弹窗内，便于最后核对 -->
        <template v-else-if="step === 'approve'">
          <div class="audit-footer__hint">
            通过后该会员将获得提币、法币交易等权限，操作不可撤销。
            <p v-if="expired" class="audit-footer__danger">⚠ 该证件已过期，请确认已核实有效身份。</p>
          </div>
          <div class="audit-footer__actions">
            <t-button theme="default" variant="outline" @click="step = 'view'">取消</t-button>
            <t-button theme="success" :loading="submitting" @click="confirmApprove">确认通过</t-button>
          </div>
        </template>

        <!-- 驳回确认：原因必填，会下发给用户 -->
        <template v-else>
          <div class="audit-footer__hint">
            <p class="audit-footer__label">驳回原因（用户可见，必填）</p>
            <t-textarea
              v-model="rejectReason"
              :maxlength="200"
              :autosize="{ minRows: 2, maxRows: 4 }"
              placeholder="例如：证件照片模糊，请重新上传清晰的照片"
            />
          </div>
          <div class="audit-footer__actions">
            <t-button theme="default" variant="outline" @click="step = 'view'">取消</t-button>
            <t-button theme="warning" :loading="submitting" @click="confirmReject">确认驳回</t-button>
          </div>
        </template>
      </div>
    </template>
  </t-dialog>

  <!-- 图片预览需压在 Dialog 之上 -->
  <t-image-viewer v-model:visible="viewerVisible" v-model:index="viewerIndex" :images="picList" :z-index="3000" />
</template>

<script setup lang="ts">
import dayjs from 'dayjs';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, ref, watch } from 'vue';

import type { AuthInfoRow } from '@/api/member';
import { authenticationFail, authenticationPassed } from '@/api/member';
import { formatWaitingDuration, isWaitingOverdue } from '@/utils/format/waitingDuration';
import { hasPermission } from '@/utils/permission';

const props = defineProps<{
  visible: boolean;
  record: AuthInfoRow | null;
}>();

const emit = defineEmits<{
  'update:visible': [visible: boolean];
  /** 审核完成，页面据此刷新列表 */
  updated: [];
}>();

const labelStyle = { width: '110px' };

/** view=待操作，approve=通过确认，reject=驳回填写 */
type Step = 'view' | 'approve' | 'reject';

const step = ref<Step>('view');
const rejectReason = ref('');
const submitting = ref(false);

// 每次打开都复位，避免上次的输入或确认态残留
watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      step.value = 'view';
      rejectReason.value = '';
    }
  },
);

const isPending = computed(() => props.record?.status === '2');

const expired = computed(
  () => !!props.record?.expiryDate && dayjs(props.record.expiryDate).isBefore(dayjs(), 'day'),
);

/** 只展示有值的图片：护照只有资料页一张 */
const picList = computed(
  () => [props.record?.identityCardPic1, props.record?.identityCardPic2].filter(Boolean) as string[],
);

function picLabel(index: number) {
  if (props.record?.idType === 'passport') {
    return '资料页';
  }
  return index === 0 ? '正面' : '反面';
}

/** 统一走公共函数：入参为后端返回的绝对时间戳，避免 createTime 无时区字符串的解析偏差 */
const waitingText = computed(() =>
  props.record?.createTimestamp ? formatWaitingDuration(props.record.createTimestamp) : '',
);

const overdue = computed(() => isWaitingOverdue(props.record?.createTimestamp));

const viewerVisible = ref(false);
const viewerIndex = ref(0);

function openViewer(index: number) {
  viewerIndex.value = index;
  viewerVisible.value = true;
}

function onVisibleChange(value: boolean) {
  emit('update:visible', value);
}

async function confirmApprove() {
  if (!props.record) {
    return;
  }
  submitting.value = true;
  try {
    await authenticationPassed(props.record.id);
    MessagePlugin.success('认证已通过');
    emit('updated');
    emit('update:visible', false);
  } finally {
    submitting.value = false;
  }
}

async function confirmReject() {
  if (!props.record) {
    return;
  }
  const reason = rejectReason.value.trim();
  if (!reason) {
    MessagePlugin.warning('请填写驳回原因');
    return;
  }
  submitting.value = true;
  try {
    await authenticationFail(props.record.id, reason);
    MessagePlugin.success('已驳回');
    emit('updated');
    emit('update:visible', false);
  } finally {
    submitting.value = false;
  }
}
</script>

<style lang="less" scoped>
.audit-block {
  margin-bottom: 16px;

  &:last-child {
    margin-bottom: 0;
  }

  &__title {
    margin-bottom: 12px;
    color: var(--td-text-color-primary);
    font-size: 14px;
    font-weight: 600;
  }
}

.audit-mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  word-break: break-all;
}

.audit-danger {
  color: var(--td-error-color);
  word-break: break-all;
}

.audit-waiting {
  color: var(--td-text-color-secondary);

  &--long {
    color: var(--td-error-color);
    font-weight: 500;
  }
}

.audit-pics {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.audit-pic {
  position: relative;
  width: 240px;
  height: 150px;
  overflow: hidden;
  border: 1px solid var(--td-border-level-1-color);
  border-radius: 8px;
  background: var(--td-bg-color-secondarycontainer);
  cursor: zoom-in;

  &__img {
    width: 100%;
    height: 100%;
  }

  &__tag {
    position: absolute;
    right: 0;
    bottom: 0;
    left: 0;
    background: rgba(0, 0, 0, 0.55);
    color: #fff;
    font-size: 12px;
    line-height: 22px;
    text-align: center;
  }
}

.audit-empty {
  color: var(--td-text-color-placeholder);
  font-size: 13px;
}

.audit-footer {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;

  &__hint {
    color: var(--td-text-color-secondary);
    font-size: 13px;
    line-height: 1.6;
    text-align: left;

    p {
      margin: 0;
    }
  }

  &__label {
    margin-bottom: 8px;
    color: var(--td-text-color-primary);
    font-size: 13px;
  }

  &__danger {
    margin-top: 4px !important;
    color: var(--td-error-color);
    font-weight: 500;
  }

  &__actions {
    display: flex;
    gap: 8px;
    justify-content: flex-end;
  }
}
</style>
