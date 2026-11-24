<template>
  <div class="login-page">
    <!-- 科技背景层 -->
    <div class="login-page__blob login-page__blob--a" />
    <div class="login-page__blob login-page__blob--b" />
    <div class="login-page__grid" />
    <div class="login-page__scan" />
    <div class="login-page__particles">
      <span v-for="n in 12" :key="n" :style="particleStyle(n)" />
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <span class="corner corner--tl" />
      <span class="corner corner--tr" />
      <span class="corner corner--bl" />
      <span class="corner corner--br" />

      <header class="login-card__header">
        <div class="brand-logo"><t-icon name="dashboard" /></div>
        <h1 class="brand-title">运营后台</h1>
      </header>

        <t-form ref="formRef" :data="formData" :rules="rules" class="login-form" label-align="top" @submit="onSubmit">
          <t-form-item name="userName" label="账号">
            <t-input v-model="formData.userName" placeholder="请输入账号" size="large" clearable>
              <template #prefix-icon><t-icon name="user" /></template>
            </t-input>
          </t-form-item>

          <t-form-item name="loginPwd" label="密码">
            <t-input
              v-model="formData.loginPwd"
              :type="showPsw ? 'text' : 'password'"
              placeholder="请输入密码"
              size="large"
              clearable
              @enter="() => onSubmit()"
            >
              <template #prefix-icon><t-icon name="lock-on" /></template>
              <template #suffix-icon>
                <t-icon :name="showPsw ? 'browse' : 'browse-off'" @click="showPsw = !showPsw" />
              </template>
            </t-input>
          </t-form-item>

          <!-- 谷歌验证码：账号已绑定谷歌验证器时必填 -->
          <t-form-item name="googleVerCode" label="谷歌验证码">
            <t-input v-model="formData.googleVerCode" placeholder="账号已绑定时必填" size="large" clearable>
              <template #prefix-icon><t-icon name="secured" /></template>
            </t-input>
          </t-form-item>

          <t-button theme="primary" type="submit" size="large" block :loading="loading">登 录</t-button>
        </t-form>
    </div>

    <div class="login-page__copyright">All rights reserved.</div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import type { FormRules, SubmitContext } from 'tdesign-vue-next';

import { useUserStore } from '@/store';

const router = useRouter();
const userStore = useUserStore();

const loading = ref(false);
const showPsw = ref(false);
const formData = reactive({
  userName: '',
  loginPwd: '',
  googleVerCode: '',
});
const rules: FormRules = {
  userName: [{ required: true, message: '请输入账号', type: 'error' }],
  loginPwd: [{ required: true, message: '请输入密码', type: 'error' }],
  // 谷歌验证码非必填：后端仅在账号已绑定谷歌验证器时校验
};

// 漂浮光点随机位置/时长
const particleStyle = (n: number) => {
  const left = (n * 37) % 100;
  const top = (n * 53) % 100;
  const dur = 6 + (n % 5);
  const delay = (n % 4) * 1.2;
  const size = 2 + (n % 3);
  return {
    left: `${left}%`,
    top: `${top}%`,
    width: `${size}px`,
    height: `${size}px`,
    animationDuration: `${dur}s`,
    animationDelay: `${delay}s`,
  };
};

async function onSubmit(ctx?: SubmitContext) {
  if (ctx && ctx.validateResult !== true) return;
  loading.value = true;
  try {
    await userStore.login({ ...formData });
    MessagePlugin.success('登录成功');
    router.push('/page');
  } catch (e) {
    // 错误提示已由请求层统一处理
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  overflow: hidden;
  background: radial-gradient(ellipse at 50% 28%, #101216 0%, #08090c 55%, #040506 100%);
}

/* 模糊光斑：缓慢漂浮，营造空间纵深 */
.login-page__blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.45;
  pointer-events: none;
}
.login-page__blob--a {
  width: 460px;
  height: 460px;
  top: -120px;
  left: -80px;
  background: radial-gradient(circle, rgba(45, 212, 191, 0.5), transparent 70%);
  animation: drift-a 18s ease-in-out infinite;
}
.login-page__blob--b {
  width: 520px;
  height: 520px;
  bottom: -160px;
  right: -120px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.4), transparent 70%);
  animation: drift-b 22s ease-in-out infinite;
}
@keyframes drift-a {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(60px, 40px); }
}
@keyframes drift-b {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-50px, -30px); }
}

/* 网格层 */
.login-page__grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.035) 1px, transparent 1px);
  background-size: 48px 48px;
  -webkit-mask-image: radial-gradient(ellipse at 50% 50%, #000 25%, transparent 78%);
  mask-image: radial-gradient(ellipse at 50% 50%, #000 25%, transparent 78%);
  pointer-events: none;
}

/* 顶部扫描线 */
.login-page__scan {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(45, 212, 191, 0.55), transparent);
  pointer-events: none;
}

/* 漂浮光点 */
.login-page__particles span {
  position: absolute;
  border-radius: 50%;
  background: #2dd4bf;
  box-shadow: 0 0 8px rgba(45, 212, 191, 0.8);
  opacity: 0;
  animation-name: float;
  animation-timing-function: ease-in-out;
  animation-iteration-count: infinite;
  pointer-events: none;
}
@keyframes float {
  0% { opacity: 0; transform: translateY(10px); }
  50% { opacity: 0.8; transform: translateY(-12px); }
  100% { opacity: 0; transform: translateY(10px); }
}

/* 登录卡片：中性深炭黑，去蓝调 */
.login-card {
  position: relative;
  z-index: 1;
  width: min(412px, 92vw);
  padding: 40px 40px 32px;
  background: rgba(18, 19, 22, 0.82);
  backdrop-filter: blur(22px);
  border-radius: 18px;
  box-shadow:
    0 30px 80px rgba(0, 0, 0, 0.65),
    0 0 60px rgba(45, 212, 191, 0.08);
}

/* 渐变描边：青绿低饱和，克制高级 */
.login-card::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  padding: 1px;
  background: linear-gradient(135deg, rgba(45, 212, 191, 0.55) 0%, rgba(255, 255, 255, 0.08) 50%, rgba(45, 212, 191, 0.55) 100%);
  -webkit-mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
  -webkit-mask-composite: xor;
  mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
  mask-composite: exclude;
  pointer-events: none;
}

/* HUD 四角装饰 */
.corner {
  position: absolute;
  width: 14px;
  height: 14px;
  border: 2px solid #2dd4bf;
  opacity: 0.9;
  z-index: 2;
}
.corner--tl { top: -1px; left: -1px; border-right: none; border-bottom: none; border-top-left-radius: 6px; }
.corner--tr { top: -1px; right: -1px; border-left: none; border-bottom: none; border-top-right-radius: 6px; }
.corner--bl { bottom: -1px; left: -1px; border-right: none; border-top: none; border-bottom-left-radius: 6px; }
.corner--br { bottom: -1px; right: -1px; border-left: none; border-top: none; border-bottom-right-radius: 6px; }

/* 卡片顶部 HUD 标识 */
.login-card__tag {
  margin-bottom: 22px;
  font-size: 11px;
  letter-spacing: 2px;
  color: rgba(45, 212, 191, 0.8);
  font-family: 'SF Mono', 'Consolas', monospace;
}

.login-card__header {
  margin-bottom: 28px;
  text-align: center;
}

.brand-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  margin-bottom: 16px;
  border-radius: 13px;
  background: rgba(45, 212, 191, 0.1);
  border: 1px solid rgba(45, 212, 191, 0.4);
  box-shadow: 0 0 22px rgba(45, 212, 191, 0.22);
  font-size: 26px;
}
.brand-logo :deep(.t-icon) {
  color: #2dd4bf;
}

.brand-title {
  display: inline-block;
  margin: 0;
  padding: 2px 8px;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 1px;
  white-space: nowrap;
  color: #f5f7ff;
}

/* 表单标签：显示在输入框上方 */
.login-form :deep(.t-form__label) {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.78);
  padding-bottom: 6px;
  letter-spacing: 0.5px;
}
.login-form :deep(.t-form__label--top) {
  text-align: left;
}
.login-form :deep(.t-form-item) {
  margin-bottom: 20px;
}
.login-form :deep(.t-form-item:last-of-type) {
  margin-bottom: 24px;
}

/* 深色输入框 */
.login-form :deep(.t-input) {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.14);
  transition: border-color 0.2s, box-shadow 0.2s;
}
.login-form :deep(.t-input__inner) {
  color: #f5f7ff;
}
.login-form :deep(.t-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.65);
}
.login-form :deep(.t-input):hover {
  border-color: rgba(45, 212, 191, 0.5);
}
.login-form :deep(.t-input.t-is-focused) {
  border-color: #2dd4bf;
  box-shadow: 0 0 0 3px rgba(45, 212, 191, 0.18);
}
.login-form :deep(.t-icon) {
  color: rgba(255, 255, 255, 0.55);
}
/* 聚焦/悬停时，左侧前缀图标同步切换为青绿主题色，与边框保持一致 */
.login-form :deep(.t-input:hover) .t-input__prefix .t-icon,
.login-form :deep(.t-input.t-is-focused) .t-input__prefix .t-icon {
  color: #2dd4bf;
}

/* 青绿渐变按钮 + 扫光 */
.login-form :deep(.t-button--theme-primary) {
  position: relative;
  margin-top: 16px;
  letter-spacing: 4px;
  border: none;
  overflow: hidden;
  background: linear-gradient(92deg, #2dd4bf 0%, #14b8a6 100%);
  box-shadow: 0 8px 24px rgba(45, 212, 191, 0.3);
  transition: box-shadow 0.2s, transform 0.1s;
}
.login-form :deep(.t-button--theme-primary)::after {
  content: '';
  position: absolute;
  top: 0;
  left: -120%;
  width: 60%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.35), transparent);
  transform: skewX(-20deg);
  transition: left 0.6s ease;
}
.login-form :deep(.t-button--theme-primary:hover) {
  box-shadow: 0 10px 32px rgba(45, 212, 191, 0.45);
}
.login-form :deep(.t-button--theme-primary:hover)::after {
  left: 130%;
}
.login-form :deep(.t-button--theme-primary:active) {
  transform: translateY(1px);
}

.login-page__copyright {
  position: absolute;
  bottom: 18px;
  left: 0;
  right: 0;
  z-index: 1;
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.3);
}
</style>
