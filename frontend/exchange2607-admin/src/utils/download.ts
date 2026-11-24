import { request } from '@/utils/request';

export async function downloadBlobGet(url: string, params: Recordable, fileName = '数据.xlsx') {
  const result = await request.post<Blob>(
    {
      url,
      data: params,
      responseType: 'blob',
    },
    { isTransformResponse: false },
  );
  const blob = new Blob([result], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  });
  const link = document.createElement('a');
  link.href = window.URL.createObjectURL(blob);
  link.download = fileName;
  link.click();
  window.URL.revokeObjectURL(link.href);
}
