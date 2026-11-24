<template>
  <wd-popup v-model="visible" position="bottom" closable :root-portal="rootPortal" :safe-area-inset-bottom="true" :z-index="101" modal-style="background: rgba(0, 0, 0, 0.6)" :custom-class="`mop-popup theme-${theme}`">
    <view class="mop">
      <view class="mop__header">
        <text class="mop__title">{{ t('perpModifyOrder.title') }}</text>
      </view>
      <view class="mop__body">
        <!-- 行1：交易对 方向 逐仓 杠杆 -->
        <view class="mop__hero">
          <text class="mop__hero-text">{{ formatSymbol(orderInfo?.symbolCode) }}</text>
          <text class="mop__hero-dir"
            :class="orderInfo?.direction === 'long' ? 'mop__value--long' : 'mop__value--short'">
            {{ dictItemText('perpetualOrderDirection', orderInfo?.direction ? `${orderInfo?.type || 'open'}-${orderInfo.direction}` : '', orderInfo?.orderDirectionName) }}</text>
          <text class="mop__hero-text">{{ t('perpCommon.isolatedWithLeverage', { leverage: orderInfo?.leverage ?? '' }) }}</text>
        </view>
        <!-- 行2：最新价 -->
        <view class="mop__row">
          <text class="mop__label">{{ t('perpCommon.latestPrice') }}</text>
          <text class="mop__value">{{ orderInfo?.latestPrice || '—' }} USDT</text>
        </view>
        <!-- 行3：价格输入 -->
        <view class="mop__input">
          <text class="mop__input-label">{{ t('perpCommon.price') }}</text>
          <input v-model="modifyPrice" type="digit" :placeholder="t('perpModifyOrder.pricePlaceholder')" placeholder-class="mop__placeholder" />
          <text class="mop__input-unit">USDT</text>
        </view>
        <!-- 行4：数量输入 -->
        <view class="mop__input">
          <text class="mop__input-label">{{ t('common.amount') }}</text>
          <input v-model="modifySize" type="digit" :placeholder="t('perpModifyOrder.amountPlaceholder')" placeholder-class="mop__placeholder" />
          <text class="mop__input-unit">{{ baseCode }}</text>
        </view>
      </view>
      <view class="mop__footer">
        <view class="mop__btn"
          :class="orderInfo?.direction === 'long' ? 'mop__btn--long' : 'mop__btn--short'"
          @click="confirmModify">
          <text class="mop__btn-text">{{ t('perpModifyOrder.confirm') }}</text>
        </view>
      </view>
    </view>
  </wd-popup>
</template>

<script setup lang="ts">
import { useAppTheme } from '@/composables/useAppTheme'

const { theme } = useAppTheme()

const popupStyle = computed(() => `background: ${theme.value === 'dark' ? '#101012' : '#ffffff'};`)
import type { ModifyOrderItem } from '@/api/types'
import { perpetualApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { formatSymbol, formatBaseSymbol } from '@/utils/symbol'
import { useI18n } from 'vue-i18n'
import { useDictItemText } from '@/utils/dictItem'

const props = withDefaults(defineProps<{
  visible: boolean
  orderId?: string
  /** 是否传送到根节点；App 端 renderjs 会移动 DOM，可能引发 patch 报错，可按页面关闭 */
  rootPortal?: boolean
}>(), {
  orderId: '',
  rootPortal: true,
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const toast = useGlobalToast()
const { t } = useI18n()
const dictItemText = useDictItemText()
const orderInfo = ref<ModifyOrderItem | null>(null)
const modifyPrice = ref('')
const modifySize = ref('')
const submitting = ref(false)

const baseCode = computed(() => formatBaseSymbol(orderInfo.value?.symbolCode) || t('perpCommon.baseFallback'))

const visible = computed({
  get: () => props.visible,
  set: (v) => emit('update:visible', v),
})

watch(() => props.visible, (v) => {
  if (v && props.orderId) {
    loadOrder()
  } else if (!v) {
    orderInfo.value = null
  }
})

async function loadOrder() {
  try {
    const data = await apiData(perpetualApi.findPerpetualLimitModifyOrderById(props.orderId))
    if (!data || data.status !== '0') {
      toast.warning({ msg: t('perpModifyOrder.toastExpired') })
      visible.value = false
      return
    }
    orderInfo.value = data
    modifyPrice.value = data.price != null ? String(data.price) : ''
    modifySize.value = data.size != null ? String(data.size) : ''
  } catch {
    visible.value = false
  }
}

async function confirmModify() {
  if (!orderInfo.value) return
  const price = parseFloat(modifyPrice.value)
  const size = parseFloat(modifySize.value)
  if (isNaN(price) || price <= 0) {
    toast.warning({ msg: t('perpModifyOrder.toastPriceRequired') })
    return
  }
  if (isNaN(size) || size <= 0) {
    toast.warning({ msg: t('perpModifyOrder.toastAmountRequired') })
    return
  }
  submitting.value = true
  try {
    await apiData(perpetualApi.changeOrder({ orderId: orderInfo.value.id, price, size }))
    toast.success({ msg: t('perpModifyOrder.toastSuccess') })
    visible.value = false
    emit('success')
  } catch {
    // error handled by http interceptor
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

:global(.mop-popup) {
  background: $surface-2 !important;
}

.mop {
  padding: 32rpx 28rpx calc(32rpx + env(safe-area-inset-bottom));

  &__header {
    display: flex;
    justify-content: center;
    margin-bottom: 24rpx;
  }

  &__title {
    font-size: 32rpx;
    font-weight: 600;
    color: $text-highlight;
  }

  &__hero {
    display: flex;
    align-items: center;
    gap: 16rpx;
    padding: 14rpx 0;
    border-bottom: 1rpx solid rgba(255, 255, 255, 0.04);
  }

  &__hero-text {
    font-size: 26rpx;
    color: $text-highlight;
    font-weight: 500;
  }

  &__hero-dir {
    font-size: 26rpx;
    font-weight: 500;
  }

  &__row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14rpx 0;
  }

  &__label {
    font-size: 24rpx;
    color: $text-muted;
  }

  &__value {
    font-size: 24rpx;
    color: $text-primary;
    font-weight: 500;
  }

  &__value--long {
    color: $success;
  }

  &__value--short {
    color: #d08080;
  }

  &__input {
    display: flex;
    align-items: center;
    gap: 12rpx;
    height: 80rpx;
    padding: 0 20rpx;
    margin-top: 16rpx;
    border-radius: 10rpx;
    background: $surface-3;
    border: 1rpx solid $border-subtle;

    &-label {
      flex-shrink: 0;
      font-size: 24rpx;
      color: $text-muted;
    }

    input {
      flex: 1;
      font-size: 26rpx;
      color: $text-primary;
    }

    &-unit {
      flex-shrink: 0;
      font-size: 24rpx;
      color: $text-muted;
    }
  }

  &__placeholder {
    color: $text-muted;
    opacity: 0.6;
  }

  &__footer {
    margin-top: 32rpx;
  }

  &__btn {
    height: 88rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 12rpx;

    &--long {
      background: linear-gradient(135deg, #95c9ad 0%, #6fa888 100%);
    }

    &--short {
      background: linear-gradient(135deg, #d49292 0%, #b07070 100%);
    }

    &-text {
      font-size: 30rpx;
      font-weight: 500;
      color: $page-bg;
      letter-spacing: 0.08em;
    }
  }
}
</style>
