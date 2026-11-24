<template>
  <t-dialog
    :visible="visible"
    header="谷歌认证登录"
    :close-on-overlay-click="false"
    :cancel-btn="googleConfigured ? '关闭' : '取消'"
    :confirm-btn="googleConfigured ? null : '保存'"
    @close="handleClose"
    @confirm="submitGoogleAuth"
  >
    <template #body>
      <div v-if="googleConfigured" class="google-auth-dialog">
        <div class="google-auth-dialog__status">已绑定谷歌验证器</div>
        <div class="google-auth-dialog__tip">绑定时间：{{ googleAuthBindTime }}</div>
        <div class="google-auth-dialog__tip">{{ resetHint }}</div>
      </div>
      <div v-else class="google-auth-dialog">
        <div>请打开谷歌验证器 APP，选择「扫描二维码」，或手动输入密钥</div>
        <div>当前账号：{{ userName }}</div>
        <div class="google-auth-dialog__secret">
          谷歌密钥：{{ googleSecretKey }}
          <copy-icon-button v-if="googleSecretKey" :text="googleSecretKey" />
        </div>
        <qrcode-display
          v-if="visible && googleSecretKey && userName"
          :text="`otpauth://totp/${userName}?secret=${googleSecretKey}`"
        />
        <div>请在下方输入 6 位数字谷歌验证码完成绑定操作</div>
        <t-input v-model="googleVerCode" placeholder="请输入谷歌验证码" />
      </div>
    </template>
  </t-dialog>
</template>

<script setup lang="ts">
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, ref, watch } from 'vue';

import { bindGoogleAuth, generateGoogleSecretKey, getAccountInfo } from '@/api/rbac';
import CopyIconButton from '@/components/copy-icon-button/index.vue';
import QrcodeDisplay from '@/components/qrcode-display/index.vue';

const props = defineProps<{
  visible: boolean;
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
  success: [];
}>();

const userName = ref('');
const superAdminFlag = ref(false);
const googleAuthBindTime = ref('');
const googleSecretKey = ref('');
const googleVerCode = ref('');

const googleConfigured = computed(() => !!googleAuthBindTime.value);

const resetHint = computed(() =>
  superAdminFlag.value ? '如需重置，请联系技术人员' : '如需重置，请联系超级管理员解绑',
);

const resetForm = () => {
  googleVerCode.value = '';
  googleSecretKey.value = '';
};

const loadAccountInfo = async () => {
  const info = await getAccountInfo();
  userName.value = info.userName;
  superAdminFlag.value = !!info.superAdminFlag;
  googleAuthBindTime.value = info.googleAuthBindTime || '';
};

const prepareBind = async () => {
  googleSecretKey.value = await generateGoogleSecretKey();
  MessagePlugin.success('已为你分配谷歌密钥');
};

const openDialog = async () => {
  resetForm();
  await loadAccountInfo();
  if (!googleConfigured.value) {
    await prepareBind();
  }
};

watch(
  () => props.visible,
  (value) => {
    if (value) {
      openDialog();
    }
  },
);

const handleClose = () => {
  emit('update:visible', false);
};

const submitGoogleAuth = async () => {
  if (googleConfigured.value) {
    return;
  }
  if (!googleVerCode.value) {
    MessagePlugin.warning('请输入谷歌验证码');
    return;
  }
  await bindGoogleAuth({ googleSecretKey: googleSecretKey.value, googleVerCode: googleVerCode.value });
  MessagePlugin.success('绑定成功');
  emit('update:visible', false);
  emit('success');
};
</script>

<style lang="less" scoped>
.google-auth-dialog {
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-size: 14px;
  line-height: 1.6;
}

.google-auth-dialog__status {
  font-weight: 600;
  color: var(--td-success-color);
}

.google-auth-dialog__tip {
  color: var(--td-text-color-secondary);
}

.google-auth-dialog__secret {
  display: flex;
  align-items: center;
  gap: 8px;
  word-break: break-all;
}
</style>
