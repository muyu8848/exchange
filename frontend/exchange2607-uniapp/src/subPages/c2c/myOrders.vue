<script setup lang="ts">
import type { C2cOrderItem } from '@/api/types'
import { c2cApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import { useDictItemText } from '@/utils/dictItem'
import { useAppStore } from '@/store/app'
import { useAuthStore } from '@/store/auth'
import AppTabs from '@/components/AppTabs.vue'
import type { AppTabItem } from '@/components/AppTabs.vue'

definePage({
  name: 'c2cOrders',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
    enablePullDownRefresh: true,
  },
})

const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()
const { t } = useI18n()
const { currentLang } = useI18nSync()
const dictItemText = useDictItemText()

type OrderStatus = '' | '1,2,5' | '4' | '3'

const tabItems = computed<AppTabItem[]>(() => [
  { key: '', label: t('c2cMyOrders.tabAll') },
  { key: '1,2,5', label: t('c2cMyOrders.tabInProgress') },
  { key: '4', label: t('c2cMyOrders.tabCanceled') },
  { key: '3', label: t('c2cMyOrders.tabCompleted') },
])
const currentStatus = ref<OrderStatus>('')
const orders = ref<C2cOrderItem[]>([])
const pageNum = ref(1)
const loadingState = ref<'loading' | 'loadmore' | 'nomore'>('loadmore')
const noData = computed(() => orders.value.length === 0 && loadingState.value === 'nomore')



onShow(() => {
  authStore.loadFromStorage()
  refreshData()
})

onReachBottom(() => {
  nextPage()
})

onPullDownRefresh(() => {
  refreshData(true)
})

function refreshData(fromPull = false) {
  pageNum.value = 1
  orders.value = []
  loadingState.value = 'loading'
  loadOrders(fromPull)
}

function nextPage() {
  if (loadingState.value === 'nomore') {
    return
  }
  pageNum.value += 1
  loadOrders()
}

async function loadOrders(fromPull = false) {
  loadingState.value = 'loading'
  try {
    const page = await apiData(c2cApi.findMyOrderByPage({
      pageNum: pageNum.value,
      pageSize: 10,
      status: currentStatus.value,
    }))
    const list = page.content || []
    if (fromPull) {
      uni.stopPullDownRefresh()
    }
    if (list.length === 0 && pageNum.value === 1) {
      loadingState.value = 'nomore'
      return
    }
    if (pageNum.value >= page.totalPage) {
      loadingState.value = 'nomore'
    }
    else {
      loadingState.value = 'loadmore'
    }
    const ids = new Set(orders.value.map(o => o.id))
    orders.value.push(...list.filter(o => !ids.has(o.id)))
  }
  catch {
    loadingState.value = 'loadmore'
    if (fromPull) {
      uni.stopPullDownRefresh()
    }
  }
}

function onTabChange(key: string | number) {
  currentStatus.value = String(key) as OrderStatus
  refreshData()
}

function openDetail(order: C2cOrderItem) {
  router.push({ name: 'orderDetail', query: { orderId: order.id } })
}

function goHall() {
  router.push({ name: 'tradeHall' })
}

function goChat(order: C2cOrderItem) {
  router.push({ name: 'tradeChat', query: { orderId: order.id } })
}

/** 跳转商家主页 */
function goMerchantProfile(order: C2cOrderItem) {
  const id = authStore.accountId === order.sellerId ? order.buyerId : order.sellerId
  if (id) {
    router.push({ name: 'merchantProfile', query: { userId: id } })
  }
}

/** 当前用户角色 */
function isBuyer(order: C2cOrderItem): boolean {
  return authStore.accountId === order.buyerId
}

/** 是否为卖方视角（模板样式判断用，避免依赖翻译文案） */
function isSellSide(order: C2cOrderItem): boolean {
  return authStore.accountId === order.sellerId
}

/** 订单方向文案 */
function sideText(order: C2cOrderItem): string {
  return isSellSide(order) ? t('c2cCommon.sell') : t('c2cCommon.buy')
}

/** 交易方昵称 */
function counterpartyName(order: C2cOrderItem): string {
  if (isSellSide(order)) {
    return order.buyerRealName || order.payerRealName || t('c2cCommon.buyer')
  }
  return order.sellerRealName || t('c2cCommon.seller')
}

</script>

<template>
  <view class="list-page">
    <AppNavBar :title="t('c2cMyOrders.title')" />
    <C2cNavBar />
    <view class="list-page__tabs">
      <AppTabs
        variant="capsule"
        :scrollable="false"
        :model-value="currentStatus"
        :items="tabItems"
        @update:model-value="onTabChange"
      />
    </view>

    <view class="list-page__body">
      <view v-if="noData" class="trade-list-empty">
        <wd-icon name="list" size="72rpx" color="#4A4846" />
        <text class="trade-list-empty__title">{{ t('c2cMyOrders.empty') }}</text>
        <text class="trade-list-empty__hint">{{ t('c2cMyOrders.emptyHint') }}</text>
        <view class="trade-list-empty__btn" @click="goHall">{{ t('c2cMyOrders.goHall') }}</view>
      </view>

      <view
        v-for="order in orders"
        :key="order.id"
        class="order-card"
        @click="openDetail(order)"
      >
        <view class="order-card__hairline" />

        <!-- 第 1 行：方向 + 币种 / 状态 + 箭头 -->
        <view class="order-card__row order-card__row--head">
          <view class="order-card__side-wrap">
            <view
              class="order-card__side"
              :class="isSellSide(order) ? 'order-card__side--sell' : 'order-card__side--buy'"
            >{{ sideText(order) }}</view>
            <text v-if="order.assetCode" class="order-card__asset">{{ order.assetCode }}</text>
          </view>
          <view class="order-card__status-wrap">
            <view class="order-card__badge">{{ dictItemText('c2cOrderStatus', order.status, order.statusName) }}</view>
            <wd-icon name="right" size="28rpx" color="#9A9690" />
          </view>
        </view>

        <!-- 第 2-3 行：单价、数量（左） + 交易总额（垂直居中右） -->
        <view class="order-card__row order-card__row--price">
          <view class="order-card__left">
            <view class="order-card__kv">
              <text class="order-card__label">{{ t('c2cCommon.unitPrice') }}</text>
              <text class="order-card__value">{{ order.price || '—' }}<text class="order-card__unit">{{ order.fiatCode || '' }}</text></text>
            </view>
            <view class="order-card__kv">
              <text class="order-card__label">{{ t('common.amount') }}</text>
              <text class="order-card__value">{{ order.amount }}</text>
            </view>
          </view>
          <view class="order-card__spacer" />
          <view class="order-card__total">
            <text class="order-card__value order-card__value--total">{{ order.fiatAmount || '—' }}<text class="order-card__unit">{{ order.fiatCode || '' }}</text></text>
          </view>
        </view>

        <!-- 第 4 行：时间 -->
        <view class="order-card__row order-card__row--time">
          <view class="order-card__kv">
            <text class="order-card__label">{{ t('common.time') }}</text>
            <text class="order-card__value">{{ order.createTime }}</text>
          </view>
        </view>

        <!-- 分隔线 -->
        <view class="order-card__divider" />

        <!-- 第 5 行：交易方 / 聊天 -->
        <view class="order-card__row order-card__row--foot">
          <view class="order-card__counterparty" @click.stop="goMerchantProfile(order)">
            <text class="order-card__counterparty-name">{{ counterpartyName(order) }}</text>
            <wd-icon name="right" size="22rpx" color="#9A9690" />
          </view>
          <view class="order-card__chat" @click.stop="goChat(order)">
            <wd-icon name="message" size="28rpx" color="#C9B48A" />
            <text class="order-card__chat-text">{{ t('c2cCommon.chat') }}</text>
          </view>
        </view>
      </view>

      <view v-if="!noData" class="trade-list-page__more" @click="nextPage">
        {{ loadingState === 'nomore' ? t('common.noMore') : t('common.loadMore') }}
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.list-page {
  @include page-shell;
  padding-bottom: 180rpx;
}

.list-page__tabs {
  position: sticky;
  top: 0;
  z-index: 10;
}

.list-page__body {
  padding: 16rpx 28rpx 0;
}

.trade-list-page__more {
  padding: 32rpx 0 16rpx;
  text-align: center;
  font-size: 24rpx;
  color: $text-muted;
}

/* ===== 空状态 ===== */
.trade-list-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
  text-align: center;
}

.trade-list-empty__title {
  margin-top: 24rpx;
  font-size: 28rpx;
  color: $text-secondary;
}

.trade-list-empty__hint {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: $text-muted;
}

.trade-list-empty__btn {
  margin-top: 12rpx;
  padding: 14rpx 36rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  color: $gold-light;
  background: $gold-soft;
  border: 1rpx solid rgba(201, 180, 138, 0.28);
}

/* ===== 订单卡片 ===== */
.order-card {
  @include dark-card;
  position: relative;
  margin-bottom: 16rpx;
  padding: 24rpx 28rpx 22rpx;
  overflow: hidden;
}

.order-card__hairline {
  position: absolute;
  top: 0;
  left: 24rpx;
  right: 24rpx;
  height: 1rpx;
  background: linear-gradient(90deg, transparent, rgba(201, 180, 138, 0.35), transparent);
}

.order-card__row {
  display: flex;
  align-items: center;
  gap: 12rpx;

  & + & {
    margin-top: 12rpx;
  }

  &--head {
    justify-content: space-between;
    margin-bottom: 16rpx;
  }

  &--price {
    align-items: center;
    margin-bottom: 8rpx;
  }

  &--time {
    margin-top: 12rpx;
  }

  &--foot {
    justify-content: space-between;
  }
}

.order-card__side-wrap {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.order-card__side {
  padding: 4rpx 14rpx;
  border-radius: 6rpx;
  font-size: 22rpx;
  border: 1rpx solid transparent;

  &--buy {
    color: #9cc9a8;
    background: rgba(130, 184, 153, 0.12);
    border-color: rgba(130, 184, 153, 0.28);
  }

  &--sell {
    color: #d89090;
    background: rgba(208, 128, 128, 0.14);
    border-color: rgba(208, 128, 128, 0.3);
  }
}

.order-card__asset {
  font-size: 28rpx;
  font-weight: 500;
  color: $text-highlight;
}

.order-card__status-wrap {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;
  padding: 8rpx 8rpx 8rpx 16rpx;
  border-radius: 8rpx;
  cursor: pointer;

  &:active {
    background: rgba(255, 255, 255, 0.04);
  }
}

.order-card__badge {
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  font-weight: 500;
  color: #a0a0a0;
  background: rgba(160, 160, 160, 0.12);
  border: 1rpx solid rgba(160, 160, 160, 0.25);
  white-space: nowrap;
}

.order-card__spacer {
  flex: 1;
}

.order-card__kv {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
  min-width: 0;
}

.order-card__left {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  min-width: 0;
}

.order-card__total {
  flex-shrink: 0;
}

.order-card__label {
  font-size: 22rpx;
  color: $text-muted;
  flex-shrink: 0;
}

.order-card__value {
  font-size: 22rpx;
  color: $text-muted;

  &--total {
    @include amount-text;
    font-size: 32rpx;
    font-weight: 500;
    color: $gold-light;
  }
}

.order-card__unit {
  font-size: 22rpx;
  font-weight: 400;
  color: $text-muted;
  margin-left: 6rpx;
}

.order-card__divider {
  height: 1rpx;
  margin: 0;
  background: linear-gradient(90deg, transparent, $border-subtle, transparent);
}

.order-card__counterparty {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 0;

  &:active {
    opacity: 0.6;
  }
}

.order-card__counterparty-name {
  font-size: 26rpx;
  color: $text-secondary;
}

.order-card__chat {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(201, 180, 138, 0.1);
  border: 1rpx solid rgba(201, 180, 138, 0.24);

  &:active {
    background: rgba(201, 180, 138, 0.16);
  }
}

.order-card__chat-text {
  font-size: 22rpx;
  color: $gold-light;
}
</style>
