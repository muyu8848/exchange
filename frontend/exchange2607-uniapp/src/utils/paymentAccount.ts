import type { PaymentAccount } from '@/api/types'

export interface PaymentAccountListRow {
  key: string
  label: string
  value?: string
  image?: string
}

/** 客户端列表逐行内容：姓名 + 后端合成的 keyField 主要字段（image 以小图展示，点击放大） */
export function paymentAccountListRows(
  account: PaymentAccount | undefined | null,
  realNameLabel: string,
): PaymentAccountListRow[] {
  const acc = account
  if (!acc) {
    return []
  }
  const rows: PaymentAccountListRow[] = []
  if (acc.realName) {
    rows.push({ key: 'realName', label: realNameLabel, value: acc.realName })
  }
  for (const field of acc.fieldLines || []) {
    if (!field.keyField || !field.value) {
      continue
    }
    if (field.type === 'image') {
      rows.push({ key: field.label, label: field.label, image: field.value })
    }
    else {
      rows.push({ key: field.label, label: field.label, value: field.value })
    }
  }
  return rows
}

export interface PaymentAccountDetailLine {
  label: string
  value: string
}

/** 弹窗卡片逐行展示：第一行姓名，后续遍历后端合成的 fieldLines 非 image 字段 */
export function paymentAccountDetailLines(
  account: PaymentAccount | undefined | null,
  realNameLabel: string,
): PaymentAccountDetailLine[] {
  if (!account) {
    return []
  }
  const lines: PaymentAccountDetailLine[] = []
  if (account.realName) {
    lines.push({ label: realNameLabel, value: account.realName })
  }
  for (const field of account.fieldLines || []) {
    if (field.type === 'image' || !field.value) {
      continue
    }
    lines.push({ label: field.label, value: field.value })
  }
  return lines
}
