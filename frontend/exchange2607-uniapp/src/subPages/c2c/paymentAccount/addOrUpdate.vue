<script setup lang="ts">
import type { PaymentAccountType } from '@/api/types'
import { apiData } from '@/api/http'
import { paymentAccountApi, paymentAccountTypeApi } from '@/api/modules'
import { useMemberStore } from '@/store/member'
import { useI18n } from 'vue-i18n'
import { chooseAndUploadImage } from '@/utils/upload'

definePage({
  name: 'addOrUpdatePaymentAccount',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
  },
})

const router = useRouter()
const toast = useGlobalToast()
const memberStore = useMemberStore()
const { t } = useI18n()

const editId = ref('')
const typeCode = ref('')
const paymentTypes = ref<PaymentAccountType[]>([])
const selectedType = ref('')
const formValues = ref<Record<string, string>>({})
const realName = ref('')
const pageLoading = ref(true)
const typesLoadError = ref(false)
const loading = ref(false)
const uploadingKey = ref('')

const isEdit = computed(() => !!editId.value)
const currentType = computed(() => paymentTypes.value.find(t => t.code === selectedType.value))
/** 导航标题：添加支付宝 / 编辑支付宝 */
const pageTitle = computed(() => {
  const name = currentType.value?.name || ''
  if (isEdit.value) {
    return name ? t('paymentAccountEdit.titleEditOf', { name }) : t('paymentAccountEdit.titleEdit')
  }
  if (typeCode.value) {
    return name ? t('paymentAccountEdit.titleAddOf', { name }) : t('paymentAccountEdit.titleAdd')
  }
  return t('paymentAccountEdit.titleAdd')
})
const fields = computed(() => currentType.value?.formFields || [])

const canSubmit = computed(() => {
  if (loading.value || pageLoading.value || typesLoadError.value) {
    return false
  }
  if (!selectedType.value) {
    return false
  }
  for (const field of fields.value) {
    const value = String(formValues.value[field.key] || '').trim()
    if (field.required && !value) {
      return false
    }
    // 长度上限只约束用户输入型字段；image 的值是上传生成的 URL，不受长度限制
    if (field.type !== 'image' && field.maxLength && field.maxLength > 0 && value.length > field.maxLength) {
      return false
    }
  }
  return true
})

onLoad((query: any) => {
  if (query?.id) {
    editId.value = String(query.id)
  }
  else if (query?.typeCode) {
    typeCode.value = String(query.typeCode)
  }
  else {
    // 本页只支持两种入口：选择支付方式(typeCode)新增 / 详情(id)编辑
    pageLoading.value = false
    typesLoadError.value = true
    toast.error({ msg: t('paymentAccountEdit.paramsMissing') })
    setTimeout(() => router.back(), 600)
    return
  }
  loadPageData()
})

async function loadPageData() {
  pageLoading.value = true
  typesLoadError.value = false
  try {
    if (editId.value) {
      // 编辑：账户展示/回填用 fieldLines；类型模板经 findByCode 获取（不拦截停用类型，停用类型的旧账户也可编辑）
      const account = await apiData(paymentAccountApi.findById(editId.value))
      const type = await apiData(paymentAccountTypeApi.findByCode(account.paymentAccountTypeCode))
      paymentTypes.value = [type]
      selectedType.value = account.paymentAccountTypeCode
      formValues.value = {}
      for (const line of account.fieldLines || []) {
        formValues.value[line.key] = line.value
      }
      realName.value = account.realName || ''
    }
    else {
      // 新增（onLoad 已保证带 typeCode）：按 code 精确获取该支付方式类型模板
      const type = await apiData(paymentAccountTypeApi.findByCode(typeCode.value))
      paymentTypes.value = [type]
      selectedType.value = type.code
      // 新增：展示用户实名（只读确认），提交仍以后端实名快照为准
      try {
        const info = await memberStore.ensureMemberInfo()
        realName.value = info.realName || ''
      }
      catch {
        // 获取失败仅影响展示，不阻塞
      }
    }
  }
  catch {
    typesLoadError.value = true
  }
  finally {
    pageLoading.value = false
    applyTitle()
  }
}

async function pickImage(key: string) {
  if (uploadingKey.value || loading.value) {
    return
  }
  try {
    uploadingKey.value = key
    const [url] = await chooseAndUploadImage(1)
    if (url) {
      formValues.value = { ...formValues.value, [key]: url }
    }
  }
  catch {
    toast.error({ msg: t('paymentAccountEdit.uploadFail') })
  }
  finally {
    uploadingKey.value = ''
  }
}

function previewImage(key: string) {
  const url = formValues.value[key]
  if (url) {
    uni.previewImage({ urls: [url] })
  }
}

function inputType(type: string) {
  return type === 'number' ? 'digit' : type === 'textarea' ? 'textarea' : 'text'
}

function updateField(key: string, value: string) {
  formValues.value = { ...formValues.value, [key]: value }
}

/** 保存成功后返回收款账户列表页：
 *  - 新增流程 index → selectType → addOrUpdate，需退 2 层回 index
 *  - 编辑流程（详情 redirectTo 进入，栈为 index → addOrUpdate）退 1 层即可
 *  - 用 getCurrentPages 兜底，避免入口变化导致 delta 越界
 */
function backToList() {
  const pages = getCurrentPages()
  const delta = typeCode.value && pages.length >= 3 ? 2 : 1
  uni.navigateBack({ delta })
}

async function handleSubmit() {
  if (!canSubmit.value || !currentType.value) {
    return
  }
  loading.value = true
  try {
    const fieldValues: Record<string, string> = {}
    for (const field of fields.value) {
      const value = String(formValues.value[field.key] || '').trim()
      if (value) {
        fieldValues[field.key] = value
      }
    }
    const payload: Record<string, unknown> = {
      paymentAccountTypeCode: selectedType.value,
      fieldValues,
    }
    if (editId.value) {
      payload.id = editId.value
      await apiData(paymentAccountApi.update(payload))
    }
    else {
      await apiData(paymentAccountApi.add(payload))
    }
    toast.success({ msg: isEdit.value ? t('paymentAccountEdit.saveSuccess') : t('paymentAccountEdit.submitSuccess') })
    backToList()
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <view class="rpa-page">
    <AppNavBar :title="t('paymentAccountEdit.heroTitle')" :nav-title="pageTitle" />
    <view class="rpa-page__glow" />
    <view class="rpa-page__mesh" />

    <view v-if="pageLoading" class="rpa-skeleton">
      <view class="rpa-skeleton__hero" />
      <view class="rpa-skeleton__types" />
      <view class="rpa-skeleton__card" />
    </view>

    <view v-else-if="typesLoadError" class="rpa-state-card">
      <text class="rpa-state-card__title">{{ t('common.loadFail') }}</text>
      <text class="rpa-state-card__desc">
        {{ t('paymentAccountEdit.loadErrorDesc') }}
      </text>
      <view class="rpa-state-card__btn" @click="router.back()">
        {{ t('common.back') }}
      </view>
    </view>

    <template v-else>
      <view class="rpa-hero rpa-hero--compact">
        <view class="rpa-hero__glow" />
        <view class="rpa-hero__head">
          <view class="rpa-hero__icon-ring">
            <view class="rpa-wallet-icon" />
          </view>
          <view class="rpa-hero__head-text">
            <text class="rpa-hero__title">{{ t('paymentAccountEdit.heroTitle') }}</text>
            <text class="rpa-hero__desc">{{ t('paymentAccountEdit.heroDesc') }}</text>
          </view>
        </view>
      </view>

      <view class="rpa-main-card">
        <view class="rpa-main-card__hairline" />

        <view class="rpa-field rpa-field--input">
          <text class="rpa-field__label">{{ t('paymentAccountEdit.realName') }}</text>
          <wd-input :model-value="realName" disabled :placeholder="t('paymentAccountEdit.realNamePlaceholder')" />
        </view>

        <template v-for="field in fields" :key="field.key">
          <view v-if="field.type === 'image'" class="rpa-qrcode-block">
            <text class="rpa-field__label">{{ field.label }}</text>

            <view v-if="uploadingKey === field.key" class="rpa-qrcode-dropzone rpa-qrcode-dropzone--loading">
              <wd-loading color="#C9B48A" size="36rpx" />
              <text class="rpa-qrcode-dropzone__loading-text">{{ t('paymentAccountEdit.uploading') }}</text>
            </view>
            <view
              v-else-if="formValues[field.key]"
              class="rpa-qrcode-preview"
              @click="previewImage(field.key)"
            >
              <wd-img :src="formValues[field.key]" width="240rpx" height="240rpx" mode="aspectFit" radius="12rpx" />
              <view class="rpa-qrcode-preview__bar">
                <text class="rpa-qrcode-preview__hint">{{ t('paymentAccountEdit.viewLarger') }}</text>
                <text class="rpa-qrcode-preview__action" @click.stop="pickImage(field.key)">{{ t('paymentAccountEdit.replace') }}</text>
              </view>
            </view>
            <view v-else class="rpa-qrcode-dropzone" @click="pickImage(field.key)">
              <view class="rpa-qrcode-dropzone__plus" />
              <text class="rpa-qrcode-dropzone__title">{{ field.label }}</text>
              <text class="rpa-qrcode-dropzone__hint">{{ t('paymentAccountEdit.pickImageHint') }}</text>
            </view>
          </view>
          <view v-else class="rpa-field rpa-field--input">
            <text class="rpa-field__label">{{ field.label }}</text>
            <wd-input
              :model-value="formValues[field.key] || ''"
              :type="inputType(field.type)"
              :maxlength="field.maxLength || undefined"
              :placeholder="field.placeholder || t('paymentAccountEdit.inputPlaceholder', { label: field.label })"
              clearable
              @update:model-value="(value: string) => updateField(field.key, value)"
            />
          </view>
        </template>

      </view>

      <view class="rpa-submit-bar rpa-submit-bar--fixed rpa-submit-bar--multi">
        <view class="rpa-submit-btn rpa-submit-btn--ghost" @click="router.back()">
          {{ t('common.cancel') }}
        </view>
        <view
          class="rpa-submit-btn"
          :class="{ 'rpa-submit-btn--disabled': !canSubmit || loading }"
          @click="handleSubmit"
        >
          {{ loading ? t('paymentAccountEdit.saving') : t('common.save') }}
        </view>
      </view>
    </template>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.rpa-page {
  @include page-shell;
  position: relative;
  overflow-x: hidden;
  min-height: 100vh;
  padding-bottom: calc(128rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.rpa-page__glow {
  @include page-top-glow;
}

.rpa-page__mesh {
  position: absolute;
  top: 80rpx;
  left: 50%;
  width: 560rpx;
  height: 560rpx;
  transform: translateX(-50%);
  background: radial-gradient(circle, rgba(201, 180, 138, 0.08) 0%, transparent 68%);
  pointer-events: none;
}

.rpa-skeleton {
  position: relative;
  z-index: 1;
  padding: 16rpx 28rpx 0;
}

.rpa-skeleton__hero,
.rpa-skeleton__types,
.rpa-skeleton__card {
  border-radius: 20rpx;
  background: linear-gradient(90deg, $surface-2 0%, $surface-3 50%, $surface-2 100%);
  background-size: 200% 100%;
  animation: rpa-shimmer 1.4s ease-in-out infinite;
}

.rpa-skeleton__hero {
  height: 140rpx;
  margin-bottom: 16rpx;
}

.rpa-skeleton__types {
  height: 200rpx;
  margin-bottom: 16rpx;
}

.rpa-skeleton__card {
  height: 520rpx;
}

@keyframes rpa-shimmer {
  0% {
    background-position: 100% 0;
  }

  100% {
    background-position: -100% 0;
  }
}

.rpa-state-card {
  @include dark-card;
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 16rpx 28rpx 0;
  padding: 72rpx 32rpx;
  border-radius: 20rpx;
  text-align: center;

  &__title {
    font-size: 28rpx;
    font-weight: 500;
    color: $text-primary;
  }

  &__desc {
    margin-top: 12rpx;
    font-size: 22rpx;
    line-height: 1.6;
    color: $text-muted;
  }

  &__btn {
    margin-top: 28rpx;
    padding: 16rpx 40rpx;
    border-radius: 999rpx;
    font-size: 24rpx;
    color: $gold-light;
    background: $gold-soft;
    border: 1rpx solid rgba(201, 180, 138, 0.22);
  }
}

.rpa-hero {
  position: relative;
  z-index: 1;
  margin: 16rpx 28rpx 0;
  padding: 20rpx 24rpx;
  border-radius: 20rpx;
  background: linear-gradient(165deg, rgba(var(--surface-3-rgb), 0.96) 0%, rgba(var(--surface-1-rgb), 0.98) 100%);
  border: 1rpx solid $border-subtle;
  box-shadow:
    inset 0 1rpx 0 $highlight-edge,
    0 16rpx 48rpx rgba(0, 0, 0, 0.28);
  overflow: hidden;

  &__glow {
    position: absolute;
    top: -40rpx;
    right: -20rpx;
    width: 240rpx;
    height: 240rpx;
    background: radial-gradient(circle, rgba(201, 180, 138, 0.14) 0%, transparent 70%);
    pointer-events: none;
  }

  &__head {
    display: flex;
    align-items: flex-start;
    gap: 20rpx;
  }

  &__icon-ring {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    background: linear-gradient(145deg, rgba(201, 180, 138, 0.16) 0%, rgba(201, 180, 138, 0.04) 100%);
    border: 1rpx solid rgba(201, 180, 138, 0.22);
    box-shadow: 0 0 24rpx rgba(201, 180, 138, 0.12);
  }

  &__head-text {
    flex: 1;
    min-width: 0;
  }

  &__title {
    display: block;
    font-size: 32rpx;
    font-weight: 600;
    color: $text-highlight;
    letter-spacing: 0.04em;
    line-height: 1.35;
  }

  &__desc {
    display: block;
    margin-top: 8rpx;
    font-size: 22rpx;
    line-height: 1.55;
    color: $text-secondary;
  }
}

.rpa-wallet-icon {
  position: relative;
  width: 32rpx;
  height: 24rpx;
  border: 3rpx solid $gold-light;
  border-radius: 6rpx;
  box-sizing: border-box;

  &::before {
    content: '';
    position: absolute;
    top: 6rpx;
    left: -3rpx;
    right: -3rpx;
    height: 3rpx;
    background: $gold-light;
  }

  &::after {
    content: '';
    position: absolute;
    right: 6rpx;
    top: 12rpx;
    width: 8rpx;
    height: 8rpx;
    border-radius: 50%;
    background: $gold-light;
  }
}

.rpa-type-card,
.rpa-main-card {
  @include dark-card;
  position: relative;
  z-index: 1;
  margin: 16rpx 28rpx 0;
  padding: 20rpx 24rpx 24rpx;
  border-radius: 20rpx;

  &__hairline {
    position: absolute;
    top: 0;
    left: 24rpx;
    right: 24rpx;
    height: 1rpx;
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(201, 180, 138, 0.35) 50%,
      transparent 100%
    );
  }
}

.rpa-section-label {
  display: block;
  margin-bottom: 16rpx;
  font-size: 24rpx;
  font-weight: 500;
  color: $text-secondary;
  letter-spacing: 0.06em;
}

.rpa-type-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}

.rpa-type-chip {
  @include chip-item;
}

.rpa-field {
  margin-bottom: 20rpx;

  &--input {
    @include form-input-deep;
  }

  &__head {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: 12rpx;
    margin-bottom: 12rpx;
  }

  &__label {
    display: block;
    margin-bottom: 12rpx;
    font-size: 24rpx;
    color: $text-secondary;
    letter-spacing: 0.04em;
  }

  &__head &__label {
    margin-bottom: 0;
  }

  &__hint {
    font-size: 20rpx;
    color: $text-muted;
  }
}

.rpa-inline-tip {
  margin: -8rpx 0 16rpx;
  font-size: 20rpx;
  line-height: 1.5;

  &--warn {
    @include danger-hint;
    padding: 0;
  }
}

.rpa-qrcode-block {
  margin-bottom: 8rpx;

  &__tip {
    display: block;
    margin: -4rpx 0 16rpx;
    font-size: 20rpx;
    color: $text-muted;
    line-height: 1.5;
  }
}

.rpa-qrcode-dropzone {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  min-height: 280rpx;
  padding: 24rpx;
  border-radius: 14rpx;
  border: 2rpx dashed rgba(201, 180, 138, 0.28);
  background: rgba(201, 180, 138, 0.04);
  box-sizing: border-box;

  &--loading {
    border-style: solid;
    border-color: $border-subtle;
    background: $surface-3;
  }

  &__plus {
    position: relative;
    width: 40rpx;
    height: 40rpx;
    margin-bottom: 4rpx;

    &::before,
    &::after {
      content: '';
      position: absolute;
      left: 50%;
      top: 50%;
      background: $gold;
      border-radius: 2rpx;
      transform: translate(-50%, -50%);
    }

    &::before {
      width: 28rpx;
      height: 3rpx;
    }

    &::after {
      width: 3rpx;
      height: 28rpx;
    }
  }

  &__title {
    font-size: 26rpx;
    color: $text-primary;
  }

  &__hint {
    font-size: 20rpx;
    color: $text-muted;
  }

  &__loading-text {
    margin-top: 8rpx;
    font-size: 22rpx;
    color: $text-muted;
  }
}

.rpa-qrcode-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  border-radius: 14rpx;
  overflow: hidden;
  border: 1rpx solid $border-subtle;
  background: rgba(0, 0, 0, 0.2);

  &__bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16rpx;
    width: 100%;
    padding: 12rpx 16rpx;
    background: $surface-3;
    box-sizing: border-box;
  }

  &__hint {
    font-size: 20rpx;
    color: $text-muted;
  }

  &__action {
    font-size: 22rpx;
    color: $gold;
  }
}

.rpa-main-card__warn {
  display: block;
  margin-top: 16rpx;
  font-size: 20rpx;
  line-height: 1.6;
  color: #d08080;
  text-align: center;
}

.rpa-main-card__tip {
  display: block;
  margin-top: 12rpx;
  font-size: 20rpx;
  line-height: 1.5;
  color: $text-muted;
  text-align: center;
}

.rpa-submit-bar {
  position: relative;
  z-index: 1;
  padding: 32rpx 28rpx calc(32rpx + env(safe-area-inset-bottom));

  &--fixed {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 20;
    padding: 16rpx 28rpx calc(16rpx + env(safe-area-inset-bottom));
    background: linear-gradient(
      180deg,
      rgba(var(--page-bg-rgb), 0) 0%,
      rgba(var(--page-bg-rgb), 0.94) 28%,
      $page-bg 100%
    );
    border-top: 1rpx solid $border-subtle;
    backdrop-filter: blur(12px);
  }
}

.rpa-submit-btn {
  @include btn-gold-block;

  &--disabled {
    opacity: 0.42;
    pointer-events: none;
  }
}

.rpa-submit-bar--multi {
  display: flex;
  gap: 16rpx;

  .rpa-submit-btn {
    flex: 1;
    text-align: center;
    font-size: 24rpx;
  }
}

.rpa-submit-btn--ghost {
  background: $surface-3;
  color: $text-secondary;
  border: 1rpx solid $border-subtle;
}
</style>
