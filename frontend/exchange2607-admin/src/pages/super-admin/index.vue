<template>
  <div>
    <t-card :bordered="false">
      <t-table row-key="id" :data="data" :columns="columns" table-layout="auto" :hover="true" :loading="dataLoading">
      </t-table>
    </t-card>
  </div>
</template>
<script lang="ts">
export default {
  name: 'SuperAdmin',
};
</script>

<script setup lang="ts">
import { PrimaryTableCol, TableRowData } from 'tdesign-vue-next';
import { onMounted, ref } from 'vue';

import { request } from '@/utils/request';

const columns: PrimaryTableCol<TableRowData>[] = [
  {
    title: '账号',
    colKey: 'userName',
  },
  {
    title: '最后登录时间',
    colKey: 'latelyLoginTime',
  },
];
const data = ref([]);
const dataLoading = ref(false);

onMounted(() => {
  fetchData();
});

const fetchData = async () => {
  data.value = await request.post({
    url: '/backgroundAccount/findSuperAdmin',
  });
};
</script>

<style lang="less" scoped></style>
