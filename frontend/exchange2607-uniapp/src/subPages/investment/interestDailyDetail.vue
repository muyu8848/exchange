<script setup lang="ts">
import type { MyDailyInterestDetail } from '@/api/types'
import { investmentApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { currencyLogo, loadAllCurrencies } from '@/utils/currency'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'

definePage({
  name: 'investmentInterestDailyDetail',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
  },
})

const { t } = useI18n()
const { currentLang } = useI18nSync()



const assetCode = ref('')
const flexibleAccountId = ref('')
const interestDate = ref('')

const detail = ref<MyDailyInterestDetail | null>(null)
const loading = ref(true)

const pickerVisible = ref(false)
const pickerValue = ref<number>(Date.now())

onLoad((query) => {
  assetCode.value = (query?.assetCode as string) || ''
  flexibleAccountId.value = (query?.flexibleAccountId as string) || ''
  interestDate.value = (query?.interestDate as string) || formatDate(new Date())
  pickerValue.value = parseDateToTimestamp(interestDate.value)
  loadData()
})

onShow(() => {
  loadAllCurrencies()
})

async function loadData() {
  loading.value = true
  try {
    detail.value = await apiData(investmentApi.findMyDailyInterestDetail({
      assetCode: assetCode.value || undefined,
      interestDate: interestDate.value,
      flexibleAccountId: flexibleAccountId.value || undefined,
    }))
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

function openPicker() {
  pickerValue.value = parseDateToTimestamp(interestDate.value)
  pickerVisible.value = true
}

function onPickerConfirm(e: { value: number }) {
  pickerVisible.value = false
  const d = new Date(e.value)
  interestDate.value = formatDate(d)
  loadData()
}

function onPickerCancel() {
  pickerVisible.value = false
}

function onPickerUpdateVisible(val: boolean) {
  pickerVisible.value = val
}

/** 将 yyyy-MM-dd 字符串解析为本地时间戳（避免 UTC 偏移） */
function parseDateToTimestamp(dateStr: string): number {
  const parts = dateStr.split('-')
  if (parts.length === 3) {
    const y = parseInt(parts[0])
    const m = parseInt(parts[1]) - 1
    const d = parseInt(parts[2])
    return new Date(y, m, d).getTime()
  }
  return Date.now()
}

/** 将 Date 对象格式化为 yyyy-MM-dd（本地时间） */
function formatDate(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function formatHour(hour: number | undefined): string {
  if (hour == null) return '—'
  return String(hour).padStart(2, '0') + ':00'
}

function formatNum(val: string | number | undefined | null) {
  if (val == null || val === '') return '0'
  return String(val)
}

const tableData = computed(() => {
  return detail.value?.hourlyRecords || []
})
</script>

<template>
  <view class="idd-page">
    <AppNavBar :title="t('investmentInterestDailyDetail.title')" />
    <view class="idd-glow" />

    <!-- 头部卡片 -->
    <view class="idd-header">
      <view class="idd-header__deco" />
      <view class="idd-header__coin">
        <image
          v-if="currencyLogo(assetCode)"
          :src="currencyLogo(assetCode)"
          class="idd-header__logo"
          mode="aspectFit"
        />
        <view v-else class="idd-header__logo-placeholder">
          <text>{{ assetCode.charAt(0) }}</text>
        </view>
        <text class="idd-header__code">{{ assetCode }}</text>
      </view>
      <view class="idd-header__interest-row">
        <text class="idd-header__label">{{ t('investmentInterestDailyDetail.dateInterest') }}</text>
        <view class="idd-header__amount-row">
          <text class="idd-header__value">+{{ formatNum(detail?.dailyInterestAmount) }}</text>
          <text class="idd-header__unit">{{ assetCode }}</text>
        </view>
      </view>
    </view>

    <!-- 日期选择 -->
    <view class="idd-date-bar" @click="openPicker">
      <text class="idd-date-bar__label">{{ t('investmentInterestDailyDetail.date') }}</text>
      <view class="idd-date-bar__value-row">
        <text class="idd-date-bar__value">{{ interestDate }}</text>
        <wd-icon name="caret-down" size="24rpx" color="#C9B48A" />
      </view>
    </view>

    <wd-datetime-picker
      v-model="pickerValue"
      :visible="pickerVisible"
      type="date"
      root-portal
      :z-index="100"
      :label="t('investmentInterestDailyDetail.pickDate')"
      @confirm="onPickerConfirm"
      @cancel="onPickerCancel"
      @update:visible="onPickerUpdateVisible"
    />

    <!-- 表格 -->
    <view class="idd-table-wrap">
      <wd-table v-if="tableData.length > 0" :data="tableData" border :stripe="false">
        <wd-table-column prop="principal" :label="t('investmentCommon.principal')" align="center" width="25%">
          <template #value="{ row }">
            <text class="idd-cell">{{ formatNum(row.principal) }}</text>
          </template>
        </wd-table-column>
        <wd-table-column prop="interestAmount" :label="t('investmentCommon.interest')" align="center" width="25%">
          <template #value="{ row }">
            <text class="idd-cell idd-cell--gold">+{{ formatNum(row.interestAmount) }}</text>
          </template>
        </wd-table-column>
         <wd-table-column prop="interestHour" :label="t('common.time')" align="center" width="25%">
          <template #value="{ row }">
            <text class="idd-cell idd-cell--time">{{ formatHour(row.interestHour) }}</text>
          </template>
        </wd-table-column>
        <wd-table-column prop="comprehensiveRate" :label="t('investmentCommon.apy')" align="center" width="25%">
          <template #value="{ row }">
            <text class="idd-cell idd-cell--rate">{{ formatNum(row.comprehensiveRate) }}%</text>
          </template>
        </wd-table-column>
      </wd-table>

      <view v-else-if="!loading" class="idd-empty">
        <wd-icon name="list" size="64rpx" color="#C9B48A" />
        <text class="idd-empty__title">{{ t('investmentCommon.empty') }}</text>
      </view>

      <view v-else class="idd-empty">
        <text class="idd-empty__title">{{ t('common.loading') }}</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.idd-page {
  @include page-shell;
  padding: 0 0 24rpx;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
}

.idd-glow {
  position: absolute;
  top: -120rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 600rpx;
  height: 400rpx;
  background: radial-gradient(ellipse at center, rgba(201, 180, 138, 0.08) 0%, transparent 70%);
  pointer-events: none;
  z-index: 0;
}

/* 头部 */
.idd-header {
  position: relative;
  z-index: 1;
  padding: 40rpx 32rpx;
  border-radius: 20rpx;
  background: linear-gradient(145deg, rgba(var(--gold-rgb), 0.08) 0%, $surface-2 100%);
  border: 1rpx solid $border-subtle;
  box-shadow: inset 0 1rpx 0 $highlight-edge;
  margin: 24rpx 24rpx 16rpx;
  overflow: hidden;
}

.idd-header__deco {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2rpx;
  background: linear-gradient(90deg, transparent, rgba(201, 180, 138, 0.4), transparent);
}

.idd-header__coin {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 24rpx;
}

.idd-header__logo {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  border: 1rpx solid $border-subtle;
}

.idd-header__logo-placeholder {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: $surface-3;
  border: 1rpx solid $border-subtle;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 600;
  color: $gold;
}

.idd-header__code {
  font-size: 36rpx;
  color: $text-highlight;
  font-weight: 700;
  letter-spacing: 0.02em;
}

/* 当日收益 — 同一行 */
.idd-header__interest-row {
  display: flex;
  align-items: baseline;
  gap: 16rpx;
}

.idd-header__label {
  font-size: 24rpx;
  letter-spacing: 0.1em;
  color: $text-muted;
  white-space: nowrap;
}

.idd-header__amount-row {
  display: flex;
  align-items: baseline;
  gap: 10rpx;
}

.idd-header__value {
  @include amount-text;
  font-size: 40rpx;
  color: $gold;
  font-weight: 700;
  line-height: 1.1;
}

.idd-header__unit {
  font-size: 24rpx;
  color: $text-muted;
}

/* 日期选择栏 */
.idd-date-bar {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 28rpx;
  border-radius: 14rpx;
  background: linear-gradient(145deg, $surface-2 0%, $surface-1 100%);
  border: 1rpx solid $border-subtle;
  margin: 0 24rpx 20rpx;
  box-shadow: inset 0 1rpx 0 $highlight-edge;
}

.idd-date-bar__label {
  font-size: 24rpx;
  color: $text-muted;
  letter-spacing: 0.1em;
}

.idd-date-bar__value-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.idd-date-bar__value {
  font-size: 28rpx;
  color: $gold-light;
  font-weight: 500;
}

/* 表格 */
.idd-table-wrap {
  position: relative;
  z-index: 1;
  margin: 0 24rpx;
  border-radius: 16rpx;
  overflow: hidden;
  border: 1rpx solid $border-subtle;
  background: $surface-1;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.3);
}

.idd-cell {
  font-size: 24rpx;
  color: $text-primary;

  &--gold {
    color: $gold;
    font-weight: 600;
  }

  &--time {
    color: $text-muted;
    font-weight: 500;
  }

  &--rate {
    color: $gold-light;
    font-size: 22rpx;
  }
}

/* 空状态 */
.idd-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 24rpx;
  gap: 20rpx;
}

.idd-empty__title {
  font-size: 28rpx;
  color: $text-muted;
}

/* wd-table 主题适配 */
:deep(.wd-table) {
  --wot-table-border-color: #{$border-subtle};
  --wot-table-bg: transparent;
  --wot-table-color: #{$text-primary};
  --wot-table-stripe-bg: #{$surface-3};
  background: transparent;
}

:deep(.wd-table__header) {
  background: $surface-3;
  border-bottom: 1rpx solid $border-subtle;
}

:deep(.wd-table__cell--header) {
  padding: 20rpx 0 !important;
}

:deep(.wd-table__cell--header .wd-table__value) {
  font-size: 22rpx;
  color: $gold;
  font-weight: 600;
  letter-spacing: 0.05em;
}

:deep(.wd-table__cell) {
  padding: 18rpx 0 !important;
  background: transparent;
  border-bottom: 1rpx solid $border-subtle;
}

:deep(.wd-table__cell.is-stripe) {
  background: $surface-3;
}

:deep(.wd-table__cell.is-last) {
  border-bottom: none;
}

/* 日期选择器深色适配 */
@include dark-datetime-picker-deep;
</style>
