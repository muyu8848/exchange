import type { OssSetting } from '@/api/setting';
import { getOssSetting } from '@/api/setting';

/** 运营后台上传接口地址，走 Vite 代理转发到 /admin/storage/upload */
export const STORAGE_UPLOAD_URL = '/api/storage/upload';

export function isOssStorageReady(setting?: OssSetting | null) {
  if (!setting || setting.type !== 'LOCAL') {
    return false;
  }
  return Boolean(
    setting.localStoragePath?.trim()
      && setting.publicFetchUrl?.trim(),
  );
}

export function parseStorageUploadResponse(response: unknown): string {
  const body = typeof response === 'string' ? JSON.parse(response) : response as {
    code?: number;
    data?: string[];
    msg?: string;
  };
  if (body?.code === 200 && Array.isArray(body.data) && body.data.length > 0) {
    return body.data[0];
  }
  throw new Error(body?.msg || '上传失败');
}

export async function loadOssStorageSetting() {
  const setting = await getOssSetting();
  return {
    setting,
    ready: isOssStorageReady(setting),
  };
}
