import { MessagePlugin } from 'tdesign-vue-next';

export async function copyToClipboard(text: string) {
  if (!text) {
    return;
  }
  if (navigator.clipboard && typeof navigator.clipboard.writeText === 'function') {
    await navigator.clipboard.writeText(text);
    MessagePlugin.success('复制成功');
  } else {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    textArea.style.position = 'fixed';
    textArea.style.opacity = '0';
    document.body.appendChild(textArea);
    textArea.select();
    textArea.setSelectionRange(0, 99999);
    const success = document.execCommand('copy');
    document.body.removeChild(textArea);
    if (success) {
      MessagePlugin.success('复制成功');
    } else {
      MessagePlugin.error('复制失败');
    }
  }
}
