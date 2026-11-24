/**
 * 扫码 / 深链内容分发（扩展点）。
 *
 * 历史模板遗留的三个分支已移除，其页面与后端接口在本项目中均不存在：
 * - memberTransfer  -> 无 name 为 memberTransfer 的页面，后端无 /client/memberTransfer/*
 * - payOrderDetail  -> 无对应页面，后端无 /client/payOrder/*
 * - authorize       -> 无对应页面
 * 新增深链时在此按内容前缀分发，并确保「页面 definePage name」与「后端 client 接口」同时存在。
 */
export async function handleDeepLinkContent(content: string) {
  // 启动页快捷方式等无意义内容直接忽略
  if (!content || content === '{"startFromLauncher":"shortcut"}') {
    return
  }
  if (import.meta.env.MODE === 'development') {
    console.info('[deepLink] 未处理的内容:', content)
  }
}
