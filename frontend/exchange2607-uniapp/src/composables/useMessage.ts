import type { GlobalDialogOptions } from './useGlobalDialog'
import { useGlobalDialog } from './useGlobalDialog'

type MessageConfirmOptions = Pick<GlobalDialogOptions, 'title' | 'msg' | 'confirmButtonText' | 'cancelButtonText'> & {
  message?: string
}

export function useMessage() {
  const dialog = useGlobalDialog()

  function confirm(option: MessageConfirmOptions | string) {
    const opts: GlobalDialogOptions = typeof option === 'string'
      ? { title: option }
      : {
          ...option,
          msg: option.msg ?? option.message,
        }

    // 返回 Promise<boolean>：确认 resolve(true)、取消/遮罩/关闭 resolve(false)，
    // 全程不 reject，避免「点取消 → 未捕获异常」。
    // 注意：wot 的 useDialog().show() 在取消时走 fail 分支并 reject，
    // 这里必须把 fail 也转成 resolve(false)，否则取消仍会冒泡成未捕获异常。
    return new Promise<boolean>((resolve) => {
      dialog.confirm({
        ...opts,
        success: (res) => {
          resolve(res.action === 'confirm')
        },
        fail: (res) => {
          resolve(res?.action === 'confirm')
        },
      })
    })
  }

  return { confirm }
}
