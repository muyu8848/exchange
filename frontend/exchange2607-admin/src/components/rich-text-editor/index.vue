<template>
  <div class="rich-text-editor">
    <Editor
      :model-value="modelValue"
      :init="editorInit"
      :disabled="disabled"
      license-key="gpl"
      @update:model-value="onUpdate"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

import 'tinymce/tinymce';
import 'tinymce/themes/silver';
import 'tinymce/icons/default';
import 'tinymce/models/dom';
import 'tinymce/plugins/lists';
import 'tinymce/plugins/link';
import 'tinymce/plugins/image';
import 'tinymce/plugins/table';

import Editor from '@tinymce/tinymce-vue';

import { useUserStore } from '@/store';
import { STORAGE_UPLOAD_URL, parseStorageUploadResponse } from '@/utils/ossStorage';

interface Props {
  modelValue?: string;
  height?: number;
  disabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  height: 400,
  disabled: false,
});

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
}>();

const userStore = useUserStore();

const editorInit = computed(() => ({
  language: 'zh_CN',
  language_url: '/tinymce/langs/zh_CN.js',
  skin: 'oxide',
  skin_url: '/tinymce/skins/ui/oxide',
  content_css: '/tinymce/skins/content/default/content.min.css',
  height: props.height,
  menubar: 'file edit insert view format table',
  toolbar:
    'undo redo | blocks | bold italic underline strikethrough | forecolor backcolor | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image table | removeformat',
  plugins: 'lists link image table',
  branding: false,
  promotion: false,
  resize: true,
  elementpath: false,
  toolbar_mode: 'sliding',
  license_key: 'gpl',
  images_upload_handler: handleImageUpload,
}));

function onUpdate(value: string) {
  emit('update:modelValue', value);
}

async function handleImageUpload(blobInfo: { blob: () => Blob; filename: () => string }) {
  const file = new File([blobInfo.blob()], blobInfo.filename(), { type: blobInfo.blob().type });
  const formData = new FormData();
  formData.append('files', file);

  const headers: Record<string, string> = { ...userStore.uploadHeaders };
  const response = await fetch(STORAGE_UPLOAD_URL, {
    method: 'POST',
    headers,
    body: formData,
  });

  const url = parseStorageUploadResponse(await response.json());
  return url;
}
</script>

<style lang="less" scoped>
.rich-text-editor {
  width: 100%;
}
</style>

<style lang="less">
.tox-tinymce-aux {
  z-index: 9999 !important;
}
</style>
