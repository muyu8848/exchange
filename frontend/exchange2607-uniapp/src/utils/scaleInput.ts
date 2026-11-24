import { nextTick } from 'vue'

/**
 * 将输入串截断到指定小数位（不四舍五入，直接丢弃多余小数位）。
 * 例: limitScaleStr('12.234', 2) → '12.23'；limitScaleStr('100', 2) → '100'
 */
export function limitScaleStr(v: string, scale: number): string {
  if (!v || scale < 0) {
    return v
  }
  const idx = v.indexOf('.')
  if (idx < 0) {
    return v
  }
  return v.slice(0, idx + 1 + scale)
}

/**
 * 过滤/截断后的值回写 model。
 *
 * 原生 input 非完全受控：
 * 1. 合法输入（cleaned === raw）也必须同步 model，否则 model 滞后于 UI，首个被过滤字符要等到再输入一位才生效
 * 2. 若被过滤后的值与存量值相同，Vue 不会触发更新，UI 会停留在未过滤的输入上
 *    此时先置空再 nextTick 回填强制刷新
 */
export function commitFilteredInput(target: { value: string }, cleaned: string, raw: string): void {
  if (cleaned === raw) {
    target.value = cleaned
    return
  }
  if (cleaned !== target.value) {
    target.value = cleaned
    return
  }
  target.value = ''
  nextTick(() => {
    target.value = cleaned
  })
}

/**
 * 截断输入串到指定精度并回写（组合 limitScaleStr + commitFilteredInput）
 */
export function truncateScaleInput(target: { value: string }, raw: string, scale: number): void {
  commitFilteredInput(target, limitScaleStr(raw, scale), raw)
}

/**
 * 从多种输入事件格式中提取裸字符串值。
 * wd-input @input 回传值可能是字符串/数字，也可能是 { value?: string | number }。
 */
export function takeRawValue(v: unknown): string | null {
  const raw = typeof v === 'string' || typeof v === 'number' ? v : (v as { value?: string | number } | null)?.value
  return raw === undefined || raw === null ? null : String(raw)
}

type ScaleOfFn = () => number

/**
 * 生成 wd-input @input 处理器：按指定精度截断并 force-refresh 回写。
 *
 * 用法:
 *   const onAmountInput = createScaleInputHandler(amount, () => amountScale.value)
 *   <wd-input :value="amount" @input="onAmountInput" />
 */
export function createScaleInputHandler(target: { value: string }, scaleOf: ScaleOfFn) {
  return (v: unknown) => {
    const raw = takeRawValue(v)
    if (raw === null) {
      return
    }
    if (raw === '') {
      target.value = ''
      return
    }
    truncateScaleInput(target, raw, scaleOf())
  }
}

/**
 * 从原生 InputEvent 中提取值：优先 e.detail.value（uni-app 小程序），其次 e.target.value（H5）。
 */
export function extractInputValue(e: unknown): string {
  if (e && typeof e === 'object') {
    const detail = (e as { detail?: { value?: string } }).detail
    if (detail && typeof detail.value === 'string') {
      return detail.value
    }
    const target = (e as { target?: { value?: string } }).target
    if (target && typeof target.value === 'string') {
      return target.value
    }
  }
  return ''
}

/**
 * 生成原生 <input> @input 处理器：按指定精度截断并 force-refresh 回写。
 *
 * 用法:
 *   const onRawInput = createNativeInputHandler(quantityInputRef, () => amountScale.value, onQuantityInput)
 *   <input :value="quantityInput" @input="onRawInput" />
 *
 * @param after 截断回写后的副作用回调（如联动计算）
 */
export function createNativeInputHandler(
  target: { value: string },
  scaleOf: ScaleOfFn,
  after?: () => void,
) {
  return (e: unknown) => {
    const raw = extractInputValue(e)
    if (raw === '') {
      target.value = ''
      after?.()
      return
    }
    truncateScaleInput(target, raw, scaleOf())
    after?.()
  }
}
