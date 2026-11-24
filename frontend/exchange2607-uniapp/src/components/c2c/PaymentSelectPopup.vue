<!--
  支付方式选择底部弹窗（多选 + 单选双模式）

  用法示例：
    <!-- 多选（发布广告页 -- 选支付方式） -->
    <PaymentSelectPopup
      v-model="payPopupVisible"
      :accounts="paymentAccounts"
      :selected-ids="selectedPayIds"
      :max-limit="maxPayCount"
      :show-empty-add="true"
      title="选择支付方式"
      @update:selected-ids="selectedPayIds = $event"
      @add="goAddPaymentAccount"
    />

    <!-- 单选（下单页 -- 出售时选具体收款账户） -->
    <PaymentSelectPopup
      v-model="accountPopupVisible"
      :accounts="accountsOfChannel(code)"
      title="选择收款账户"
      :multiple="false"
      :show-empty-add="true"
      add-text="添加收款账户"
      @select="(pa: PaymentAccount) => doSubmit(pa)"
      @add="goAddPaymentAccount"
    />

  Props：
    accounts            PaymentAccount[]          账户列表
    selectedIds         string[]                  已选账户 id（多选模式）
    multiple            boolean                   多选模式（默认 true）
    maxLimit            number | null             多选模式下的上限（null=不限）
    title               string                    弹窗标题
    showEmptyAdd        boolean                   空态是否显示"去添加"按钮
    addText             string                    空态按钮文案
    confirmText         string                    确定按钮文案（仅多选模式有）
    showConfirm         boolean                   是否显示底部"确定"按钮（多选默认 true；单选可 false）
    disabledFunc        (acc) => boolean         自定义禁用函数，返回 true 则该项禁用

  Slots：
    item    自定义项渲染，scope: { item, selected, disabled, toggle }
            默认渲染：logo + 类型名 + 字段行 + 选中图标
    empty   自定义空态；默认：icon + "暂无可用" + "去添加" 按钮

  Emits：
    update:modelValue    显隐变化
    update:selectedIds   多选模式下选中项变化
    select               单选模式：用户点某项（payload = PaymentAccount）
    add                  用户点"去添加"（空态）
    confirm              用户点确定按钮
-->
<template>
  <wd-popup
    :model-value="modelValue"
    position="bottom"
    closable
    root-portal
    :z-index="100"
    modal-style="background: rgba(0, 0, 0, 0.6)"
    :custom-style="popupStyle"
    :custom-class="`c2c-pay-popup theme-${theme}`"
    @update:model-value="emit('update:modelValue', $event)"
    @close="emit('update:modelValue', false)"
  >
    <view class="c2c-pay-sheet">
      <!-- 头部 -->
      <view class="c2c-pay-sheet__head">
        <text class="c2c-pay-sheet__title">{{ resolvedTitle }}</text>
      </view>

      <!-- 滚动列表 -->
      <scroll-view scroll-y class="c2c-pay-sheet__scroll">
        <view v-if="accounts.length === 0" class="c2c-pay-sheet__empty">
          <slot name="empty">
            <wd-icon name="ordered-list" size="48rpx" color="#6a6660" />
            <text class="c2c-pay-sheet__empty-text">{{ resolvedEmptyText }}</text>
            <view v-if="showEmptyAdd" class="c2c-pay-sheet__empty-btn" @click="emit('add')">
              {{ resolvedAddText }}
            </view>
          </slot>
        </view>

        <view
          v-for="item in accounts"
          :key="item.id"
          class="c2c-pay-sheet__card"
          :class="{
            'c2c-pay-sheet__card--active': isSelected(item),
            'c2c-pay-sheet__card--disabled': isDisabled(item),
          }"
          @click="onItemClick(item)"
        >
          <slot name="item" :item="item" :selected="isSelected(item)" :disabled="isDisabled(item)" :toggle="() => toggle(item)">
            <!-- 默认项渲染 -->
            <image
              v-if="item.paymentAccountTypeLogo"
              :src="item.paymentAccountTypeLogo"
              class="c2c-pay-sheet__card-logo"
              mode="aspectFill"
            />
            <view v-else class="c2c-pay-sheet__card-logo c2c-pay-sheet__card-logo--fallback" />
            <view class="c2c-pay-sheet__card-body">
              <!-- 主行：类型名 + 选中图标 -->
              <view class="c2c-pay-sheet__card-row c2c-pay-sheet__card-row--header">
                <text class="c2c-pay-sheet__card-name">{{ accountTitle(item) }}</text>
                <slot name="card-tail" :item="item" :selected="isSelected(item)">
                  <wd-icon
                    v-if="multiple"
                    :name="isSelected(item) ? 'check-square' : 'uncheck-square'"
                    size="36rpx"
                    :color="isSelected(item) ? '#c9b48a' : '#6a6660'"
                  />
                </slot>
              </view>
              <!-- 详情行 -->
              <view
                v-for="line in paymentAccountDetailLines(item, t('paymentAccount.realName'))"
                :key="line.label"
                class="c2c-pay-sheet__card-row"
              >
                <text class="c2c-pay-sheet__card-label">{{ line.label }}</text>
                <text class="c2c-pay-sheet__card-value">{{ line.value }}</text>
              </view>
            </view>
          </slot>
        </view>
      </scroll-view>

      <!-- 底部确定按钮（多选显示 or 显式 showConfirm） -->
      <view v-if="showFooter" class="c2c-pay-sheet__foot">
        <view class="c2c-pay-sheet__foot-btn" @click="onConfirm">
          {{ resolvedConfirmText }}
        </view>
      </view>
    </view>
  </wd-popup>
</template>

<script lang="ts" setup generic="T extends PaymentAccount">
import type { PaymentAccount } from '@/api/types'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { paymentAccountDetailLines } from '@/utils/paymentAccount'
import { useAppTheme } from '@/composables/useAppTheme'

const { theme } = useAppTheme()

const popupStyle = computed(() => `background: ${theme.value === 'dark' ? '#101012' : '#ffffff'};`)

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    accounts: T[]
    selectedIds?: string[]
    multiple?: boolean
    maxLimit?: number | null
    title?: string
    showEmptyAdd?: boolean
    addText?: string
    confirmText?: string
    showConfirm?: boolean
    disabledFunc?: (acc: T) => boolean
    emptyText?: string
  }>(),
  {
    selectedIds: () => [],
    multiple: true,
    maxLimit: null,
    title: '',
    showEmptyAdd: false,
    addText: '',
    confirmText: '',
    showConfirm: true,
    disabledFunc: undefined,
    emptyText: '',
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'update:selectedIds', val: string[]): void
  (e: 'select', item: T): void
  (e: 'add'): void
  (e: 'confirm'): void
}>()

const { t } = useI18n()

/** 默认文案解析（外部传入优先） */
const resolvedTitle = computed(() => props.title || t('c2cCommon.selectPayment'))
const resolvedAddText = computed(() => props.addText || t('c2cPaymentSelect.add'))
const resolvedConfirmText = computed(() => props.confirmText || t('common.confirm'))
const resolvedEmptyText = computed(() => props.emptyText || t('c2cPaymentSelect.empty'))

/** 是否显示底部 footer */
const showFooter = computed(() => props.multiple && props.showConfirm)

/** 账户标题 */
function accountTitle(account: T): string {
  return account.paymentAccountTypeName || account.paymentAccountTypeCode || t('c2cCommon.paymentMethod')
}

/** 选中判断 */
function isSelected(account: T): boolean {
  return props.selectedIds.includes(account.id)
}

/** 禁用判断 */
function isDisabled(account: T): boolean {
  if (props.disabledFunc?.(account)) {
    return true
  }
  // 多选模式下，已达上限且当前项未选中 → 禁用
  if (props.multiple && props.maxLimit != null && !isSelected(account)) {
    return props.selectedIds.length >= props.maxLimit
  }
  return false
}

/** 点击项 */
function onItemClick(account: T) {
  if (isDisabled(account)) {
    return
  }
  if (props.multiple) {
    toggle(account)
    return
  }
  emit('select', account)
}

/** 点击确定 */
function onConfirm() {
  emit('confirm')
  emit('update:modelValue', false)
}

/** 多选切换 */
function toggle(account: T) {
  const next = isSelected(account)
    ? props.selectedIds.filter(id => id !== account.id)
    : [...props.selectedIds, account.id]
  emit('update:selectedIds', next)
}
</script>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.c2c-pay-sheet {
  display: flex;
  flex-direction: column;
  max-height: 80vh;
  padding: 28rpx 28rpx calc(28rpx + env(safe-area-inset-bottom));
  border-radius: 24rpx 24rpx 0 0;
  background: $surface-2;

  &__head {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 20rpx;
  }

  &__title {
    font-size: 30rpx;
    font-weight: 500;
    color: $text-highlight;
  }

  &__scroll {
    flex: 1;
    min-height: 0;
    max-height: 60vh;
  }

  &__empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
    padding: 36rpx 20rpx;
    border-radius: 16rpx;
    background: rgba(255, 255, 255, 0.02);
    border: 1rpx dashed rgba(255, 255, 255, 0.08);
    font-size: 24rpx;
    color: $text-muted;
  }

  &__empty-btn {
    margin-top: 8rpx;
    padding: 12rpx 28rpx;
    border-radius: 999rpx;
    font-size: 24rpx;
    color: $gold;
    background: $gold-soft;
    border: 1rpx solid rgba(184, 160, 120, 0.14);
  }

  &__card {
    margin-bottom: 10rpx;
    padding: 14rpx 18rpx;
    border-radius: 12rpx;
    background: rgba(255, 255, 255, 0.02);
    border: 1rpx solid $border-subtle;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;

    &:last-child {
      margin-bottom: 0;
    }

    &--active {
      background: linear-gradient(135deg, rgba(201, 180, 138, 0.08) 0%, rgba(255, 255, 255, 0.02) 100%);
      border-color: rgba(201, 180, 138, 0.26);
      box-shadow: 0 0 0 1rpx rgba(201, 180, 138, 0.06);
    }

    &--disabled {
      opacity: 0.45;
    }
  }

  &__card-logo {
    width: 40rpx;
    height: 40rpx;
    border-radius: 50%;
    flex-shrink: 0;
    background: rgba(255, 255, 255, 0.05);
    border: 1rpx solid rgba(255, 255, 255, 0.08);
  }

  &__card-body {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
  }

  &__card-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16rpx;
    min-height: 40rpx;

    &--header {
      min-height: 48rpx;
    }
  }

  &__card-name {
    flex: 1;
    min-width: 0;
    font-size: 28rpx;
    font-weight: 500;
    color: $text-highlight;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__card-label {
    flex-shrink: 0;
    font-size: 24rpx;
    color: $text-muted;
  }

  &__card-value {
    flex: 1;
    min-width: 0;
    text-align: right;
    font-size: 24rpx;
    color: $text-secondary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    @include amount-text;
  }

  &__foot {
    margin-top: 20rpx;
  }

  &__foot-btn {
    @include btn-gold-block;
  }
}
</style>
