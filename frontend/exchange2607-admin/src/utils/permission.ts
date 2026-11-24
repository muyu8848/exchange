import { useUserStore } from '@/store';

/**
 * 判断当前登录账号是否拥有指定权限。
 *
 * 权限码来自后端菜单体系的 `menu_3`（权限节点），登录时随 `TokenInfo.permissions` 下发；
 * 超管由后端下发通配符 `*`。
 *
 * 用法：模板统一写 `v-if="hasPermission('role.del')"`。
 * **不要用自定义指令**——`t-dropdown-item`、`t-option` 这类「配置型组件」会被父组件
 * 当作配置消费，绑定在子节点上的自定义指令会在重建时丢失，导致隐藏失效。
 *
 * @param required 单个权限码或权限码数组（数组为「任一满足」）
 */
export function hasPermission(required?: string | string[]): boolean {
  if (!required || (Array.isArray(required) && required.length === 0)) {
    return true;
  }
  const codes = (useUserStore().permissions ?? []) as string[];
  // 超管通配
  if (codes.includes('*')) {
    return true;
  }
  const list = Array.isArray(required) ? required : [required];
  return list.some((code) => codes.includes(code));
}
