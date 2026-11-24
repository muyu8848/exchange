<template>
  <div>
    <t-card :bordered="false">
      <t-row>
        <t-col :span="8" :offset="2">
          <t-alert theme="warning" class="data-clean-tip">
            仅用于测试环境清理数据。清理后不可恢复，且仅超级管理员可操作。
          </t-alert>
          <t-form ref="form" :data="formData" :rules="formRules" label-align="top" class="data-clean-form">
            <t-form-item label="数据类型" name="dataTypes">
              <t-checkbox-group v-model="formData.dataTypes">
                <t-checkbox v-for="item in DATA_CLEAN_TYPES" :key="item.value" :value="item.value">
                  {{ item.label }}
                </t-checkbox>
              </t-checkbox-group>
            </t-form-item>
            <t-form-item label="数据日期范围" name="dateRange">
              <t-date-range-picker
                v-model="formData.dateRange"
                :presets="tableDateRangePresets"
                allow-input
                clearable
              />
            </t-form-item>
            <t-form-item>
              <t-button v-if="hasPermission('dataClean.clean')" theme="danger" variant="outline" @click="onDataClean">清理数据</t-button>
              <template #help><div class="data-clean-warning">清理过的数据不能恢复，请谨慎操作。</div></template>
            </t-form-item>
          </t-form>
        </t-col>
      </t-row>
    </t-card>
  </div>
</template>
<script lang="ts">
export default { name: 'DataClean' };
</script>
<script setup lang="ts">
import { DialogPlugin, type FormInstanceFunctions, type FormRule, MessagePlugin } from 'tdesign-vue-next';
import { ref } from 'vue';

import { dataClean } from '@/api/setting';
import { DATA_CLEAN_TYPES } from '@/constants/dataClean';
import { tableDateRangePresets } from '@/utils/dateRangePresets';
import { hasPermission } from '@/utils/permission';

const form = ref<FormInstanceFunctions>();
const formData = ref({ dataTypes: [] as string[], dateRange: [] as string[] });
const formRules: Record<string, FormRule[]> = {
  dataTypes: [{ required: true, message: '请选择数据类型', type: 'error', trigger: 'submit' }],
  dateRange: [{ required: true, message: '请选择数据日期范围', type: 'error', trigger: 'submit' }],
};

const onDataClean = async () => {
  const error = await form.value?.validateOnly();
  if (error !== true) return;
  const selectedLabels = DATA_CLEAN_TYPES.filter((item) => formData.value.dataTypes.includes(item.value)).map(
    (i) => i.label,
  );
  const dialogInstance = DialogPlugin.confirm({
    header: '提示',
    body: `确认清理 ${formData.value.dateRange[0]} 至 ${formData.value.dateRange[1]} 的以下数据吗？\n${selectedLabels.join('、')}`,
    onConfirm: async () => {
      await dataClean({
        dataTypes: formData.value.dataTypes,
        timeStart: formData.value.dateRange[0],
        timeEnd: formData.value.dateRange[1],
      });
      dialogInstance.destroy();
      MessagePlugin.success('操作成功');
    },
  });
};
</script>
<style lang="less" scoped>
.data-clean-tip {
  margin-bottom: 16px;
}
.data-clean-warning {
  color: var(--td-error-color);
  margin-top: 8px;
}
</style>
