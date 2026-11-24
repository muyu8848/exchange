<script setup lang="ts">
definePage({
  name: 'customWebView',
  layout: 'default',
  style: {
    navigationBarTitleText: '详情',
    navigationBarBackgroundColor: '#0a0a0c',
    navigationBarTextStyle: 'white',
    backgroundColor: '#f5f6f8',
  },
})

/** 幂等解码：兼容上游传了 0 层 / 1 层 / 2 层编码的情况 */
function safeDecode(value: string): string {
  let result = value
  for (let i = 0; i < 2; i++) {
    try {
      const next = decodeURIComponent(result)
      if (next === result) {
        break
      }
      result = next
    }
    catch {
      break
    }
  }
  return result
}

const link = ref('')

onLoad((query) => {
  const raw = safeDecode((query?.link as string) || '')
  // App 端 web-view 必须是完整 http(s) 地址，否则会被当作本地相对路径去加载
  link.value = /^https?:\/\//i.test(raw) ? raw : ''
})
</script>

<template>
  <web-view v-if="link" :src="link" />
</template>
