export function qrCodeImageUrl(text: string, size = 220) {
  return `https://api.qrserver.com/v1/create-qr-code/?size=${size}x${size}&data=${encodeURIComponent(text)}`
}

export function googleAuthOtpUrl(userName: string, secret: string) {
  return `otpauth://totp/${encodeURIComponent(userName)}?secret=${secret}`
}

export function getUrlParamValue(path: string, paramName: string) {
  const idx = path.indexOf('?')
  if (idx === -1) {
    return null
  }
  const queryStr = path.substring(idx + 1)
  const reg = new RegExp(`(^|&)${paramName}=([^&]*)(&|$)`, 'i')
  const match = queryStr.match(reg)
  if (match) {
    return decodeURIComponent(match[2])
  }
  return null
}
