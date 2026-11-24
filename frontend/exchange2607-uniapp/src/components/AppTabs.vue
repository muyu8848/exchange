<script lang="ts">
/** tab 项定义 */
export interface AppTabItem {
  /** 唯一标识，同时作为 v-model 的值 */
  key: string | number
  /** 文案（已本地化，页面传 t(...) 的结果） */
  label: string
  /** 数量角标，大于 0 时渲染为 `label(n)` */
  count?: number
  /** 是否显示；false 时该项被过滤（全部隐藏则整组不渲染） */
  visible?: boolean
  /** 是否禁用 */
  disabled?: boolean
  /**
   * 方向色（交易表单用）：buy/long 绿、sell/short 红
   * 仅在激活态使用实色底衬，未激活为 50% 透明度文字
   */
  tone?: 'buy' | 'sell'
}

/** 多实例 id 前缀递增种子，保证同页多个 AppTabs 的 scroll-into-view 目标不冲突 */
let tabsSeed = 0
</script>

<script setup lang="ts">
import { computed, nextTick, onActivated, onDeactivated, onMounted, ref } from 'vue'

const props = withDefaults(defineProps<{
  /** 当前激活项 key */
  modelValue: string | number
  /** tab 列表 */
  items: AppTabItem[]
  /**
   * 视觉变体
   * - capsule：胶囊壳 + 金色底衬（perpOrderRecord / spotOrderRecord / secondContractOrderRecord / interestDetail / loginRecord / investmentOrderRecord / orderCreate）
   * - underline：无壳 + 短下划线（MarketsPanel / spotTrade 底部 / perpTrade 底部）
   * - chip：独立胶囊 chip（overview / messageList / c2c hall）
   * - bar：等分 + 通栏底线（c2c orderDetail / merchantProfile）
   */
  variant?: 'capsule' | 'underline' | 'chip' | 'bar'
  /** 尺寸：xs=交易表单紧凑号，sm=二级小号，md=主 tab，lg=行情面板大号 */
  size?: 'xs' | 'sm' | 'md' | 'lg'
  /**
   * 对齐方式
   * - fill：等分铺满（文案超宽时撑开并横滑）
   * - start：按内容宽度从左排列
   * 不传时按变体推导：capsule / bar → fill，underline / chip → start
   *
   * 注意：这里刻意不用 boolean，因为 Vue 会把缺省的 Boolean prop 隐式转成 false，
   * 无法区分「没传」和「传了 false」。
   */
  align?: 'fill' | 'start'
  /** 是否允许横向滚动（文案超宽时可横滑，且激活项自动滚入可视区） */
  scrollable?: boolean
  /**
   * 带壳模式，仅 `bar` 变体生效：
   * 容器改为一层壳（底色 + 圆角 + 内衬 + 四边边框），激活项加金色底衬，指示条变短（32×4rpx）
   */
  boxed?: boolean
  /** 自定义项间距（rpx）；不传则使用变体默认值 */
  gap?: number
  /**
   * 覆盖 tab 项内边距，传 CSS padding 简写（如 '14rpx 32rpx'）
   *
   * 用于页面级微调（个别页面需要更宽的点击区/更松的排版），
   * 不传则完全走 variant + size 的默认值。以 inline style 实现，
   * 因此优先级最高、也不受各端 scoped 样式作用域差异影响。
   */
  itemPadding?: string
  /**
   * 覆盖 tab 项圆角（如 '12rpx'）；不传则走变体默认
   * 与 itemPadding 同属页面级微调逃生口
   */
  itemRadius?: string
}>(), {
  variant: 'capsule',
  size: 'md',
  scrollable: true,
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  'change': [value: string | number, item: AppTabItem]
}>()

const uid = `app-tabs-${++tabsSeed}`

/**
 * scroll-into-view 守卫：uni 的 scroll-view 在应用 scroll-into-view 时会直接对内部滚动节点
 * 设置 scrollLeft。页面通过 navigateBack / keep-alive 重新显示、或数据回流导致 tab 项重渲染时，
 * 该节点可能尚未就绪（为 null），从而抛 `Cannot set properties of null (setting 'scrollLeft')`。
 * 这里延迟到挂载/激活且 nextTick 之后再开启 scroll-into-view，避开该时序竞态。
 */
const ready = ref(false)

function markReady() {
  nextTick(() => {
    ready.value = true
  })
}

onMounted(markReady)
onActivated(() => {
  ready.value = false
  markReady()
})
// 离开页面时销毁 scroll-view，避免携带未就绪/损坏的内部滚动节点被 keep-alive 重插入，
// 从而在页面切换/返回时触发 uni 的 `Cannot set properties of null (setting 'scrollLeft')`
onDeactivated(() => {
  ready.value = false
})

const visibleItems = computed(() => props.items.filter(item => item.visible !== false))

const isFill = computed(() => props.align
  ? props.align === 'fill'
  : props.variant === 'capsule' || props.variant === 'bar')

const activeIndex = computed(() => visibleItems.value.findIndex(item => item.key === props.modelValue))

const activeId = computed(() => (activeIndex.value < 0 ? '' : itemId(activeIndex.value)))

const innerStyle = computed(() => (props.gap == null ? {} : { gap: `${props.gap}rpx` }))

/** 项样式覆盖（逃生口：内边距 / 圆角） */
const itemStyle = computed(() => {
  const style: Record<string, string> = {}
  if (props.itemPadding) {
    style.padding = props.itemPadding
  }
  if (props.itemRadius) {
    style.borderRadius = props.itemRadius
  }
  return Object.keys(style).length > 0 ? style : undefined
})

function itemId(index: number) {
  return `${uid}-item-${index}`
}

/** 文案 + 可选数量角标 */
function labelOf(item: AppTabItem) {
  return item.count && item.count > 0 ? `${item.label}(${item.count})` : item.label
}

function onSelect(item: AppTabItem) {
  if (item.disabled || item.key === props.modelValue) {
    return
  }
  emit('update:modelValue', item.key)
  emit('change', item.key, item)
}
</script>

<template>
  <scroll-view
    v-if="visibleItems.length && ready"
    class="app-tabs"
    :class="[
      `app-tabs--${variant}`,
      `app-tabs--${size}`,
      {
        'app-tabs--fill': isFill,
        'app-tabs--start': !isFill,
        'app-tabs--boxed': boxed && variant === 'bar',
        'app-tabs--noscroll': !scrollable,
      },
    ]"
    scroll-x
    :scroll-into-view="scrollable ? activeId : ''"
    scroll-with-animation
    :show-scrollbar="false"
  >
    <view class="app-tabs__inner" :style="innerStyle">
      <view
        v-for="(item, index) in visibleItems"
        :id="itemId(index)"
        :key="item.key"
        class="app-tabs__item"
        :class="{
          'is-active': item.key === modelValue,
          'is-disabled': item.disabled,
          'app-tabs__item--buy': item.tone === 'buy',
          'app-tabs__item--sell': item.tone === 'sell',
        }"
        :style="itemStyle"
        @click="onSelect(item)"
      >
        <text class="app-tabs__text">{{ labelOf(item) }}</text>
        <view v-if="variant === 'bar' && item.key === modelValue" class="app-tabs__bar" />
      </view>
    </view>
  </scroll-view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

/* ============ 骨架 ============ */
.app-tabs {
  position: relative;
  display: block;
  box-sizing: border-box;
  width: 100%;

  &__inner {
    display: inline-flex;
    align-items: stretch;
    box-sizing: border-box;
    width: auto;
  }

  /* 等分铺满：文案短时撑满一行，文案长时撑开容器触发横滑 */
  &--fill &__inner {
    min-width: 100%;
  }

  /*
   * 按内容宽度：
   * - 块级父容器下 width:auto 等价于撑满（行为不变）
   * - 作为 flex 子项时（例如和筛选按钮同行）自动收缩为内容宽度
   */
  &--start {
    width: auto;
  }

  &__item {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    flex: 0 0 auto;
    /* 预留边框位，避免激活态加边框时抖动 */
    border: 1rpx solid transparent;
    white-space: nowrap;
    transition: color 0.2s, background 0.2s, border-color 0.2s;

    &.is-disabled {
      opacity: 0.4;
    }

    /* 方向色：未激活为 50% 透明度文字，激活用实色底衬 + 边框 */
    &--buy {
      color: rgba(130, 184, 153, 0.5);

      &.is-active {
        color: $success;
        background: rgba(130, 184, 153, 0.12);
        border-color: rgba(130, 184, 153, 0.25);
      }
    }

    &--sell {
      color: rgba(208, 128, 128, 0.5);

      &.is-active {
        color: #d08080;
        background: rgba(208, 128, 128, 0.1);
        border-color: rgba(208, 128, 128, 0.22);
      }
    }
  }

  &--fill &__item {
    flex: 1 0 auto;
  }

  /* 非滚动（确定能铺满）时，真正等分：basis 归零 + 解除 min-width 限制，各 tab 均分容器宽度 */
  &--fill.app-tabs--noscroll &__item {
    flex: 1 1 0;
    min-width: 0;
  }

  &__text {
    line-height: 1.2;
  }
}

/* ============ 尺寸 ============ */
.app-tabs--xs .app-tabs__item {
  font-size: 24rpx;
}

.app-tabs--sm .app-tabs__item {
  font-size: 22rpx;
}

.app-tabs--md .app-tabs__item {
  font-size: 26rpx;
}

.app-tabs--lg .app-tabs__item {
  font-size: 30rpx;
}

/* ============ 变体一：capsule（胶囊壳 + 金色底衬） ============ */
.app-tabs--capsule {
  border-radius: 12rpx;
  background: $surface-2;
  border: 1rpx solid $border-subtle;

  .app-tabs__inner {
    gap: 8rpx;
    padding: 6rpx;
  }

  .app-tabs__item {
    border-radius: 8rpx;
    color: $text-muted;
    padding: 16rpx 12rpx;

    &.is-active {
      color: $gold-light;
      background: $gold-soft;
    }
  }

  &.app-tabs--sm .app-tabs__item {
    padding: 6rpx 16rpx;
    border-radius: 6rpx;
  }

  &.app-tabs--lg .app-tabs__item {
    padding: 18rpx 16rpx;
  }
}

/* 紧凑号：交易表单内部使用（壳更暗、内衬更小、字重 500） */
.app-tabs--capsule.app-tabs--xs {
  border-radius: 10rpx;
  background: $surface-3;

  .app-tabs__inner {
    padding: 4rpx;
    gap: 8rpx;
  }

  .app-tabs__item {
    padding: 8rpx 0;
    font-weight: 500;
    border-radius: 8rpx;
  }
}

/* ============ 变体二：underline（无壳 + 短下划线） ============ */
.app-tabs--underline {
  .app-tabs__inner {
    gap: 48rpx;
    align-items: center;
  }

  .app-tabs__item {
    /* 底部留出下划线空间，避免被 scroll-view 裁掉 */
    padding: 8rpx 0 14rpx;
    color: $text-secondary;

    &.is-active {
      color: $text-highlight;
      font-weight: 500;

      &::after {
        content: '';
        position: absolute;
        left: 50%;
        bottom: 6rpx;
        transform: translateX(-50%);
        width: 40rpx;
        height: 4rpx;
        border-radius: 2rpx;
        background: linear-gradient(90deg, #d1c4a8, #b8a078);
      }
    }
  }
}

/* ============ 变体三：chip（独立胶囊 chip） ============ */
.app-tabs--chip {
  .app-tabs__inner {
    gap: 12rpx;
  }

  .app-tabs__item {
    padding: 12rpx 24rpx;
    border-radius: 999rpx;
    color: $text-muted;
    background: $surface-2;
    border: 1rpx solid $border-subtle;

    &.is-active {
      color: $page-bg;
      background: linear-gradient(135deg, #d1c4a8 0%, #b8a078 100%);
      border-color: rgba(209, 196, 168, 0.24);
      font-weight: 600;
    }
  }

  &.app-tabs--sm .app-tabs__item {
    padding: 8rpx 20rpx;
  }

  &.app-tabs--lg .app-tabs__item {
    padding: 12rpx 36rpx;
  }
}

/* ============ 变体四：bar（等分 + 通栏底线） ============ */
.app-tabs--bar {
  border-bottom: 1rpx solid $border-subtle;

  .app-tabs__inner {
    gap: 0;
  }

  .app-tabs__item {
    flex-direction: column;
    gap: 12rpx;
    padding: 14rpx 0 12rpx;
    color: $text-secondary;

    &.is-active {
      color: $text-highlight;
      font-weight: 500;
    }
  }

  .app-tabs__bar {
    width: 48rpx;
    height: 3rpx;
    border-radius: 2rpx;
    background: $gold-light;
  }
}

/* bar 带壳模式：容器成壳 + 激活项底衬 + 更短的指示条（c2c 订单详情） */
.app-tabs--bar.app-tabs--boxed {
  background: $surface-1;
  border: 1rpx solid $border-subtle;
  border-radius: 16rpx;
  padding: 6rpx;

  .app-tabs__inner {
    gap: 0;
  }

  .app-tabs__item {
    gap: 8rpx;
    padding: 16rpx 0;
    border-radius: 12rpx;
    color: $text-muted;

    &.is-active {
      background: rgba(201, 180, 138, 0.12);
    }
  }

  .app-tabs__bar {
    width: 32rpx;
    height: 4rpx;
  }
}
</style>
