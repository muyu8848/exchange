<template>
  <div v-if="items.length > 0" class="todo-bar">
    <t-tooltip v-for="item in items" :key="item.key" :content="tooltipText(item)" placement="bottom">
      <button
        type="button"
        class="todo-chip"
        :class="[chipClass(item.key), { 'todo-chip--zero': item.count === 0 }]"
        @click="handleClick(item)"
      >
        <span class="todo-chip__dot" />
        <span class="todo-chip__label">{{ item.label }}</span>
        <span class="todo-chip__count">{{ item.count }}</span>
      </button>
    </t-tooltip>
  </div>
</template>

<script setup lang="ts">
import { storeToRefs } from 'pinia';

import type { AdminTodoItem } from '@/api/adminTodo';
import { useAdminTodoStore } from '@/store';
import { navigateToAdminTodo } from '@/utils/adminTodoNavigate';

const adminTodoStore = useAdminTodoStore();
const { items } = storeToRefs(adminTodoStore);

/** 提币打款涉及出金，最高优先级；提币审核次之；其余统一蓝色 */
const CHIP_CLASS: Record<string, string> = {
  withdrawPayout: 'is-danger',
  withdrawAudit: 'is-warning',
};

function chipClass(key: string) {
  return CHIP_CLASS[key] || 'is-info';
}

function tooltipText(item: AdminTodoItem) {
  return item.count > 0 ? `${item.label}：${item.count} 项待处理` : `${item.label}：暂无待处理`;
}

function handleClick(item: AdminTodoItem) {
  navigateToAdminTodo(item.menuUrl, item.query);
  adminTodoStore.fetchTodoList();
}
</script>

<style lang="less" scoped>
.todo-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  margin-right: 4px;
}

.todo-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 28px;
  padding: 0 10px;
  border: none;
  border-radius: 14px;
  background: transparent;
  cursor: pointer;
  font-size: 12px;
  line-height: 1;
  color: var(--td-text-color-primary);
  transition: background-color 0.2s linear;
  white-space: nowrap;

  &:hover {
    background: var(--td-bg-color-container-hover);
  }

  &.is-danger {
    --todo-accent: var(--td-error-color);
  }

  &.is-warning {
    --todo-accent: var(--td-warning-color);
  }

  &.is-info {
    --todo-accent: var(--td-brand-color);
  }

  &--zero {
    --todo-accent: var(--td-text-color-placeholder);
    opacity: 0.45;
    cursor: default;
  }

  &__dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    flex-shrink: 0;
    background: var(--todo-accent);
  }

  &__count {
    font-size: 13px;
    font-weight: 600;
    font-variant-numeric: tabular-nums;
    color: var(--todo-accent);
  }
}

/* 中窄屏只保留色点与数字，悬停由 tooltip 补足名称 */
@media (max-width: 1600px) {
  .todo-chip {
    padding: 0 8px;

    &__label {
      display: none;
    }
  }
}
</style>
