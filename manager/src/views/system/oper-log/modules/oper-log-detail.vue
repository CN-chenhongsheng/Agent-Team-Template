<template>
  <ElDialog v-model="dialogVisible" :title="$t('system.operLog.detailTitle')" width="800px">
    <div class="oper-log-detail">
      <ElDescriptions :column="2" border>
        <ElDescriptionsItem :label="$t('system.operLog.module')" :span="2">
          {{ logData?.title || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('system.operLog.businessType')">
          <ElTag :type="getBusinessTypeTag(logData?.businessType)">
            {{ logData?.businessTypeText || '-' }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('system.operLog.status')">
          <ElTag :type="logData?.status === 0 ? 'success' : 'danger'">
            {{ logData?.statusText || '-' }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('system.operLog.operator')">
          {{ logData?.operName || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('system.operLog.deviceType')">
          {{ logData?.deviceTypeText || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('system.operLog.ip')">
          {{ logData?.operIp || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('system.operLog.location')">
          {{ logData?.operLocation || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('system.operLog.time')">
          {{ logData?.operTime || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('system.operLog.cost')">
          {{ logData?.costTime ? `${logData.costTime} ms` : '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('system.operLog.requestMethod')">
          {{ logData?.requestMethod || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('system.operLog.requestUrl')" :span="2">
          {{ logData?.operUrl || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('system.operLog.methodName')" :span="2">
          {{ logData?.method || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('system.operLog.requestParams')" :span="2">
          <ElInput
            v-model="operParamFormatted"
            type="textarea"
            :rows="6"
            readonly
            :placeholder="logData?.operParam || $t('system.common.none')"
          />
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('system.operLog.responseParams')" :span="2">
          <ElInput
            v-model="jsonResultFormatted"
            type="textarea"
            :rows="6"
            readonly
            :placeholder="logData?.jsonResult || $t('system.common.none')"
          />
        </ElDescriptionsItem>
        <ElDescriptionsItem
          v-if="logData?.errorMsg"
          :label="$t('system.operLog.errorMsg')"
          :span="2"
        >
          <ElAlert type="error" :closable="false">
            {{ logData.errorMsg }}
          </ElAlert>
        </ElDescriptionsItem>
      </ElDescriptions>
    </div>
    <template #footer>
      <ElButton @click="dialogVisible = false">{{ $t('system.common.close') }}</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ElDescriptions, ElDescriptionsItem, ElInput, ElAlert } from 'element-plus'

  interface Props {
    modelValue: boolean
    logData?: Api.SystemManage.OperLogListItem | null
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    logData: null
  })
  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  /**
   * 格式化JSON字符串
   */
  const formatJSON = (jsonString?: string): string => {
    if (!jsonString) return ''
    try {
      const obj = JSON.parse(jsonString)
      return JSON.stringify(obj, null, 2)
    } catch {
      // 如果不是JSON格式，直接返回原字符串
      return jsonString
    }
  }

  /**
   * 格式化后的请求参数
   */
  const operParamFormatted = computed(() => {
    return formatJSON(props.logData?.operParam)
  })

  /**
   * 格式化后的返回参数
   */
  const jsonResultFormatted = computed(() => {
    return formatJSON(props.logData?.jsonResult)
  })

  /**
   * 获取业务类型标签类型
   */
  const getBusinessTypeTag = (businessType?: number): 'info' | 'success' | 'warning' | 'danger' => {
    if (businessType === 1) return 'success' // 新增
    if (businessType === 2) return 'warning' // 修改
    if (businessType === 3) return 'danger' // 删除
    return 'info' // 其它
  }
</script>

<style scoped lang="scss">
  .oper-log-detail {
    :deep(.el-textarea__inner) {
      font-family: 'Courier New', monospace;
      font-size: 12px;
      line-height: 1.5;
    }
  }
</style>
