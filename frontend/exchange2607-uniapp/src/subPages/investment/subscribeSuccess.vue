<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'

definePage({
  name: 'investmentSubscribeSuccess',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
  },
})

const router = useRouter()

const { t } = useI18n()
const { currentLang } = useI18nSync()



const goldIcon = '#C9B48A'

const assetCode = ref('')
const amount = ref('')

onLoad((query) => {
  assetCode.value = (query?.assetCode as string) || ''
  amount.value = (query?.amount as string) || ''
})

function goAssets() {
  router.pushTab({ name: 'assets' })
}

function goInvestmentHome() {
  router.replace({ name: 'investment' })
}
</script>

<template>
  <view class="iss-page">
    <AppNavBar :title="t('investmentSubscribeSuccess.title')" />
    <!-- 头部 -->
    <view class="iss-hero">
      <wd-icon name="check-circle-fill" size="88rpx" :color="goldIcon" />
      <text class="iss-hero__title">{{ t('investmentSubscribeSuccess.title') }}</text>
      <view class="iss-hero__amount-row">
        <text class="iss-hero__amount">{{ amount }}</text>
        <text class="iss-hero__unit">{{ assetCode }}</text>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="iss-footer">
      <view class="iss-footer__btn iss-footer__btn--outline" @click="goInvestmentHome">
        <text>{{ t('investmentSubscribeSuccess.continueInvest') }}</text>
      </view>
      <view class="iss-footer__btn iss-footer__btn--primary" @click="goAssets">
        <text>{{ t('investmentSubscribeSuccess.viewAssets') }}</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.iss-page {
  @include page-shell;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  box-sizing: border-box;
}

.iss-hero {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48rpx 28rpx;
  gap: 24rpx;
}

.iss-hero__title {
  font-size: 34rpx;
  color: $text-highlight;
  font-weight: 600;
}

.iss-hero__amount-row {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
}

.iss-hero__amount {
  font-size: 56rpx;
  color: $gold;
  font-weight: 700;
  @include amount-text;
}

.iss-hero__unit {
  font-size: 24rpx;
  color: $text-muted;
}

.iss-footer {
  display: flex;
  gap: 20rpx;
  padding: 24rpx 24rpx calc(24rpx + env(safe-area-inset-bottom));
}

.iss-footer__btn {
  flex: 1;
  height: 88rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 600;

  &--primary {
    @include btn-gold-block;
  }

  &--outline {
    @include btn-outline;
    height: 88rpx;
    font-size: 28rpx;
    color: $gold-light;
    border-color: rgba(201, 180, 138, 0.24);
    background: $gold-soft;
  }
}
</style>
