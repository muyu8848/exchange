import i18n from '@/locale'
import { useAuthStore } from '@/store/auth'

export function getUploadHeaders(): Record<string, string> {
  const authStore = useAuthStore()
  authStore.loadFromStorage()
  if (!authStore.tokenName || !authStore.tokenValue) {
    return {}
  }
  return { [authStore.tokenName]: authStore.tokenValue }
}

export function parseUploadUrl(responseData: string): string {
  const json = JSON.parse(responseData) as { code: number, msg?: string, data?: string[] }
  if (json.code === 200 && Array.isArray(json.data) && json.data.length > 0) {
    return json.data[0]
  }
  throw new Error(json.msg || i18n.global.t('realName.uploadFailed'))
}

const STORAGE_UPLOAD_URL = (import.meta.env.VITE_API_BASE_URL || '') + '/storage/upload'

export function uploadOneFile(filePath: string): Promise<string> {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: STORAGE_UPLOAD_URL,
      filePath,
      name: 'files',
      header: getUploadHeaders(),
      success: (res) => {
        try {
          resolve(parseUploadUrl(res.data as string))
        }
        catch (e) {
          reject(e)
        }
      },
      fail: reject,
    })
  })
}

export function chooseAndUploadImage(count = 1): Promise<string[]> {
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count,
      success: async (res) => {
        try {
          const urls: string[] = []
          for (const path of res.tempFilePaths) {
            urls.push(await uploadOneFile(path))
          }
          resolve(urls)
        }
        catch (e) {
          reject(e)
        }
      },
      fail: reject,
    })
  })
}
