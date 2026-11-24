import router from '@/router';
import { gotoPage } from '@/utils/menuRoute';

/**
 * 待办跳转：统一走菜单树的 gotoPage。
 * <p>
 * menuUrl 必须是后端菜单里配置的 path（slug 形式，如 withdraw-order），
 * 由 gotoPage 按 `父菜单/子菜单` 拼出真实路由并带上查询条件。
 */
export function navigateToAdminTodo(menuUrl: string, query?: Record<string, string>) {
  // from=todo 供目标页识别「这是待办入口」，从而清空时间条件、展示全部待处理数据
  gotoPage(router, menuUrl, { ...query, from: 'todo' });
}
