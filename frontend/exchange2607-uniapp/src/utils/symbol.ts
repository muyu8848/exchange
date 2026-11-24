/** 把后端交易对编码（BTC-USDT）转成展示格式（BTC/USDT） */
export function formatSymbol(code?: string | null): string {
  return code ? code.replace(/-/g, '/') : ''
}

/** 取交易对基础币种：BTC-USDT -> BTC */
export function formatBaseSymbol(code?: string | null): string {
  return formatSymbol(code).split('/')[0] || ''
}
