import { getUrlParamValue } from '@/utils/qrcode'

const WALLET_ADDR_PATTERN = /^[a-fA-F0-9]{16}$/

export function isValidWalletAddr(value: string) {
  return WALLET_ADDR_PATTERN.test(value?.trim() || '')
}

export function parseWalletAddrFromScan(content: string): string | null {
  if (!content) {
    return null
  }
  const trimmed = content.trim()
  const fromParam = getUrlParamValue(
    trimmed.includes('?') ? trimmed : `?${trimmed.replace(/^[^?]*\?/, '')}`,
    'walletAddr',
  )
  if (fromParam && isValidWalletAddr(fromParam)) {
    return fromParam.trim()
  }
  if (isValidWalletAddr(trimmed)) {
    return trimmed
  }
  const match = trimmed.match(/[a-fA-F0-9]{16}/)
  if (match && isValidWalletAddr(match[0])) {
    return match[0]
  }
  return null
}

/**
 * 从扫码结果中解析区块链提币地址。
 * 支持 URI 格式（如 ethereum:0x...、bitcoin:xxx）和纯地址。
 */
export function parseWithdrawalAddrFromScan(content: string): string | null {
  if (!content) {
    return null
  }
  const trimmed = content.trim()
  if (!trimmed) {
    return null
  }

  // 处理 URI scheme 格式，如 ethereum:0x123...、bitcoin:1xxx、tron:Txxx
  const colonIdx = trimmed.indexOf(':')
  if (colonIdx > 0 && !trimmed.startsWith('http')) {
    const after = trimmed.slice(colonIdx + 1).replace(/^\s*\/\//, '').trim()
    // 去掉可能的查询参数
    const addr = after.split('?')[0].trim()
    if (addr) {
      return addr
    }
  }

  // 纯地址直接返回
  return trimmed
}
