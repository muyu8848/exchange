<template>
  <wd-popup v-model="visible" position="bottom" closable root-portal :safe-area-inset-bottom="true" :z-index="101" modal-style="background: rgba(0, 0, 0, 0.6)" :custom-class="`ptp-popup theme-${theme}`">
    <view class="ptp">
      <view class="ptp__header">
        <text class="ptp__title">{{ t('perpTpsl.title') }}</text>
      </view>
      <view class="ptp__body">
        <!-- 行1：交易对 方向 逐仓 杠杆 -->
        <view class="ptp__hero">
          <text class="ptp__hero-text">{{ symbolText }}</text>
          <text class="ptp__hero-dir" :class="dirClass">{{ dirName }}</text>
          <text class="ptp__hero-text">{{ t('perpCommon.isolatedWithLeverage', { leverage: position?.leverage ?? '' }) }}</text>
        </view>
        <!-- 行2：开仓均价 -->
        <view class="ptp__row">
          <text class="ptp__label">{{ t('perpCommon.openAvgPrice') }}</text>
          <text class="ptp__value">{{ formatNum(position?.openPrice) }} USDT</text>
        </view>
        <!-- 行3：最新价（同交易对用实时价，跨交易对退化为持仓自带 markPrice） -->
        <view class="ptp__row">
          <text class="ptp__label">{{ t('perpCommon.latestPrice') }}</text>
          <text class="ptp__value">{{ formatNum(displayLastPrice) }} USDT</text>
        </view>
        <!-- 行4：预估强平价 -->
        <view class="ptp__row">
          <text class="ptp__label">{{ t('perpCommon.liqPrice') }}</text>
          <text class="ptp__value">{{ formatNum(position?.liquidationPrice) }} USDT</text>
        </view>

        <!-- 止盈区 -->
        <view class="ptp__side">
          <wd-checkbox v-model="tpEnabled" type="square" :checked-color="'#C9B48A'" :unchecked-color="'#4a4a55'" />
          <text class="ptp__side-label">{{ t('perpCommon.tp') }}</text>
          <text v-if="tpEnabled" class="ptp__side-clear" @click="clearTp">{{ t('perpTpsl.clear') }}</text>
        </view>
        <template v-if="tpEnabled">
          <view class="ptp__input">
            <text class="ptp__input-label">{{ t('perpTpsl.tpTriggerPrice') }}</text>
            <PrecisionInput v-model="tpPrice" :precision="priceScale" :placeholder="t('perpTpsl.triggerPlaceholder')" :after="onTpPriceAfter" />
            <text class="ptp__input-unit">USDT</text>
          </view>
          <view class="ptp__input">
            <text class="ptp__input-label">{{ t('perpTpsl.roi') }}</text>
            <PrecisionInput v-model="tpRoi" :precision="2" :placeholder="t('perpTpsl.roiPlaceholder')" :after="onTpRoiAfter" />
            <text class="ptp__input-unit">%</text>
          </view>
          <view class="ptp__hint">
            <text>{{ t('perpTpsl.hintTriggerPrefix', { price: formatNum(parseFloat(tpPrice)) }) }}</text>
            <text class="ptp__hint-profit" :class="profitClass(tpPrice)">{{ estProfit(tpPrice) }}</text>
            <text> USDT</text>
          </view>
          <view class="ptp__qty">
            <text class="ptp__input-label">{{ t('common.amount') }}</text>
            <text class="ptp__qty-val">{{ position?.size ?? '0' }} {{ baseCurrency }}</text>
          </view>
        </template>

        <!-- 止损区 -->
        <view class="ptp__side">
          <wd-checkbox v-model="slEnabled" type="square" :checked-color="'#C9B48A'" :unchecked-color="'#4a4a55'" />
          <text class="ptp__side-label">{{ t('perpCommon.sl') }}</text>
          <text v-if="slEnabled" class="ptp__side-clear" @click="clearSl">{{ t('perpTpsl.clear') }}</text>
        </view>
        <template v-if="slEnabled">
          <view class="ptp__input">
            <text class="ptp__input-label">{{ t('perpTpsl.slTriggerPrice') }}</text>
            <PrecisionInput v-model="slPrice" :precision="priceScale" :placeholder="t('perpTpsl.triggerPlaceholder')" :after="onSlPriceAfter" />
            <text class="ptp__input-unit">USDT</text>
          </view>
          <view class="ptp__input">
            <text class="ptp__input-label">{{ t('perpTpsl.roi') }}</text>
            <PrecisionInput v-model="slRoi" :precision="2" :placeholder="t('perpTpsl.roiPlaceholder')" :after="onSlRoiAfter" />
            <text class="ptp__input-unit">%</text>
          </view>
          <view class="ptp__hint">
            <text>{{ t('perpTpsl.hintTriggerPrefix', { price: formatNum(parseFloat(slPrice)) }) }}</text>
            <text class="ptp__hint-profit" :class="profitClass(slPrice)">{{ estProfit(slPrice) }}</text>
            <text> USDT</text>
          </view>
          <view class="ptp__qty">
            <text class="ptp__input-label">{{ t('common.amount') }}</text>
            <text class="ptp__qty-val">{{ position?.size ?? '0' }} {{ baseCurrency }}</text>
          </view>
        </template>
      </view>
      <view class="ptp__footer">
        <view class="ptp__btn" @click="confirm">
          <text class="ptp__btn-text">{{ t('perpCommon.confirm') }}</text>
        </view>
      </view>
    </view>
  </wd-popup>
</template>

<script setup lang="ts">
import { useAppTheme } from '@/composables/useAppTheme'

const { theme } = useAppTheme()

const popupStyle = computed(() => `background: ${theme.value === 'dark' ? '#101012' : '#ffffff'};`)
import type { PerpetualPositionItem } from '@/api/types'
import { perpetualApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { formatSymbol } from '@/utils/symbol'
import { useI18n } from 'vue-i18n'
import PrecisionInput from '@/components/PrecisionInput.vue'

const props = withDefaults(defineProps<{
  visible: boolean
  position?: PerpetualPositionItem | null
}>(), {
  position: null,
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const toast = useGlobalToast()
const { t } = useI18n()
const submitting = ref(false)

const tpEnabled = ref(false)
const slEnabled = ref(false)
const tpPrice = ref('')
const tpRoi = ref('')
const slPrice = ref('')
const slRoi = ref('')

const visible = computed({
  get: () => props.visible,
  set: (v) => emit('update:visible', v),
})

const symbolText = computed(() => formatSymbol(props.position?.symbolCode) || '')
const dirName = computed(() => props.position?.direction === 'long' ? t('perpCommon.longPosition') : t('perpCommon.shortPosition'))
const dirClass = computed(() =>
  props.position?.direction === 'long' ? 'ptp__value--long' : 'ptp__value--short')
const baseCurrency = computed(() => symbolText.value.split('/')[0] || '')

// 最新价：直接取持仓自带 markPrice（服务端按持仓交易对算的 close 价，随持仓轮询每 3s 刷新、实时跳动）
const displayLastPrice = computed(() => props.position?.markPrice ?? 0)

function decimalsOf(n?: number): number {
  if (n == null) return 2
  const s = String(n)
  const i = s.indexOf('.')
  return i < 0 ? 2 : Math.min(s.length - i - 1, 8)
}

/** 价格精度：优先取持仓所属交易对的配置，缺失时按开仓价小数位兜底 */
const priceScale = computed(() => {
  const scale = props.position?.priceScale
  return typeof scale === 'number' && scale >= 0 ? scale : decimalsOf(props.position?.openPrice)
})

/** 由触发价反算收益率（含杠杆，输出绝对值：收益率输入框不接受负号） */
function computeRoi(price: number): string {
  const open = props.position?.openPrice
  const lev = props.position?.leverage
  if (!open || !lev || !(price > 0)) return ''
  const roi = props.position.direction === 'long'
    ? (price - open) / open * 100 * lev
    : (open - price) / open * 100 * lev
  return Math.abs(roi).toFixed(2)
}

/** 由收益率（正数幅度）反算触发价：止盈取盈利方向，止损取亏损方向 */
function computePriceFromRoi(roi: number, isTakeProfit: boolean): string {
  const open = props.position?.openPrice
  const lev = props.position?.leverage
  if (!open || !lev || Number.isNaN(roi)) return ''
  const pct = Math.abs(roi) / 100 / lev
  const isLong = props.position.direction === 'long'
  // 盈利方向：多头向上、空头向下；止损取反
  const upward = isTakeProfit === isLong
  const price = upward ? open * (1 + pct) : open * (1 - pct)
  return price.toFixed(priceScale.value)
}

/** 收益率框不接受负号：粘贴进来的负值直接剥离 */
function stripMinus(v: string) {
  return v.replace(/-/g, '')
}

function onTpPriceAfter(cleaned: string) {
  tpRoi.value = computeRoi(Number.parseFloat(cleaned))
}

function onTpRoiAfter(cleaned: string) {
  const v = stripMinus(cleaned)
  if (v !== cleaned) tpRoi.value = v
  tpPrice.value = computePriceFromRoi(Number.parseFloat(v), true)
}

function onSlPriceAfter(cleaned: string) {
  slRoi.value = computeRoi(Number.parseFloat(cleaned))
}

function onSlRoiAfter(cleaned: string) {
  const v = stripMinus(cleaned)
  if (v !== cleaned) slRoi.value = v
  slPrice.value = computePriceFromRoi(Number.parseFloat(v), false)
}

function clearTp() { tpPrice.value = ''; tpRoi.value = '' }
function clearSl() { slPrice.value = ''; slRoi.value = '' }

/** 预估盈亏数值（USDT，带方向：盈利为正、亏损为负） */
function estProfitValue(priceStr: string): number | null {
  const price = Number.parseFloat(priceStr)
  const open = props.position?.openPrice
  const size = props.position?.size
  if (Number.isNaN(price) || !open || !size) return null
  const diff = props.position?.direction === 'long' ? price - open : open - price
  return diff * size
}

/** 预估盈亏文案（盈利补 + 号，亏损自带 - 号） */
function estProfit(priceStr: string): string {
  const v = estProfitValue(priceStr)
  if (v == null) return '0.00'
  return `${v > 0 ? '+' : ''}${v.toFixed(2)}`
}

/** 预估盈亏颜色：盈利绿、亏损红 */
function profitClass(priceStr: string): string {
  const v = estProfitValue(priceStr)
  if (v == null || v === 0) return ''
  return v > 0 ? 'ptp__hint-profit--up' : 'ptp__hint-profit--down'
}

function formatNum(v?: number) {
  return v != null && !isNaN(Number(v)) ? Number(v).toFixed(2) : '—'
}

watch(() => props.visible, (v) => {
  if (v && props.position) {
    const p = props.position
    tpEnabled.value = p.takeProfitPrice != null
    slEnabled.value = p.stopLossPrice != null
    tpPrice.value = p.takeProfitPrice != null ? String(p.takeProfitPrice) : ''
    slPrice.value = p.stopLossPrice != null ? String(p.stopLossPrice) : ''
    tpRoi.value = computeRoi(parseFloat(tpPrice.value))
    slRoi.value = computeRoi(parseFloat(slPrice.value))
  } else if (!v) {
    tpEnabled.value = false
    slEnabled.value = false
    tpPrice.value = ''; slPrice.value = ''; tpRoi.value = ''; slRoi.value = ''
  }
})

async function confirm() {
  if (!props.position) return
  let tp: number | undefined
  let sl: number | undefined
  if (tpEnabled.value) {
    tp = tpPrice.value.trim() === '' ? undefined : parseFloat(tpPrice.value)
    if (tp == null || isNaN(tp) || tp <= 0) {
      toast.warning({ msg: t('perpTpsl.toastTpInvalid') })
      return
    }
  }
  if (slEnabled.value) {
    sl = slPrice.value.trim() === '' ? undefined : parseFloat(slPrice.value)
    if (sl == null || isNaN(sl) || sl <= 0) {
      toast.warning({ msg: t('perpTpsl.toastSlInvalid') })
      return
    }
  }
  if (!tpEnabled.value && !slEnabled.value) {
    toast.warning({ msg: t('perpTpsl.toastEnableOne') })
    return
  }
  if (tp == null && sl == null) {
    toast.warning({ msg: t('perpTpsl.toastPriceRequired') })
    return
  }
  submitting.value = true
  try {
    await apiData(perpetualApi.setPositionTpsl({
      positionId: props.position.id,
      takeProfitPrice: tp,
      stopLossPrice: sl,
    }))
    toast.success({ msg: t('perpTpsl.toastSuccess') })
    visible.value = false
    emit('success')
  } catch {
    // 错误由 http 拦截器统一处理
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

:global(.ptp-popup) {
  background: $surface-2 !important;
}

.ptp {
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

  &__side {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-top: 24rpx;
  }

  &__side-label {
    font-size: 26rpx;
    font-weight: 500;
    color: $text-highlight;
  }

  &__side-clear {
    margin-left: auto;
    font-size: 22rpx;
    color: $text-muted;
    padding: 4rpx 14rpx;
    border-radius: 6rpx;
    background: rgba(255, 255, 255, 0.06);
  }

  &__hint {
    margin-top: 12rpx;
    font-size: 22rpx;
    line-height: 1.5;
    color: $text-muted;
  }

  &__hint-profit {
    &--up {
      color: $success;
      font-weight: 500;
    }

    &--down {
      color: #d08080;
      font-weight: 500;
    }
  }

  &__qty {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 72rpx;
    padding: 0 20rpx;
    margin-top: 16rpx;
    border-radius: 10rpx;
    background: $surface-3;
    border: 1rpx solid $border-subtle;

    &-val {
      font-size: 26rpx;
      color: $text-primary;
      font-weight: 500;
    }
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

    /* PrecisionInput 内部为 wd-input，文本样式需穿透对齐 */
    :deep(.precision-input uni-input),
    :deep(.precision-input .wd-input__value) {
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
    background: linear-gradient(135deg, #95c9ad 0%, #6fa888 100%);

    &-text {
      font-size: 30rpx;
      font-weight: 500;
      color: $page-bg;
      letter-spacing: 0.08em;
    }
  }
}
</style>
