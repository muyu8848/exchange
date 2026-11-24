/**
 * 获取当前页面路径
 * @returns 当前页面路径
 */
export function getCurrentPath() {
  const pages = getCurrentPages()
  if (!pages || pages.length === 0) {
    return ''
  }
  const currentPage = pages[pages.length - 1]
  return currentPage?.route || ''
}
