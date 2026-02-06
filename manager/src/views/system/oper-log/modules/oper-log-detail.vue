<template>
  <ElDialog
    v-model="dialogVisible"
    title="操作日志详情"
    width="800px"
    :close-on-click-modal="false"
  >
    <div class="oper-log-detail">
      <ElDescriptions :column="2" border>
        <ElDescriptionsItem label="操作模块" :span="2">
          {{ logData?.title || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="业务类型">
          <ElTag :type="getBusinessTypeTag(logData?.businessType)">
            {{ logData?.businessTypeText || '-' }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="操作状态">
          <ElTag :type="logData?.status === 0 ? 'success' : 'danger'">
            {{ logData?.statusText || '-' }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="操作人员">
          {{ logData?.operName || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="设备类型">
          {{ logData?.deviceTypeText || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="操作IP">
          {{ logData?.operIp || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="操作地点">
          {{ logData?.operLocation || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="操作时间">
          {{ logData?.operTime || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="耗时">
          {{ logData?.costTime ? `${logData.costTime} ms` : '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="请求方式">
          {{ logData?.requestMethod || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="请求URL" :span="2">
          {{ logData?.operUrl || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="方法名称" :span="2">
          {{ logData?.method || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="请求参数" :span="2">
          <ElInput
            v-model="operParamFormatted"
            type="textarea"
            :rows="6"
            readonly
            :placeholder="logData?.operParam || '无'"
          />
        </ElDescriptionsItem>
        <ElDescriptionsItem label="返回参数" :span="2">
          <ElInput
            v-model="jsonResultFormatted"
            type="textarea"
            :rows="6"
            readonly
            :placeholder="logData?.jsonResult || '无'"
          />
        </ElDescriptionsItem>
        <ElDescriptionsItem v-if="logData?.errorMsg" label="错误消息" :span="2">
          <ElAlert type="error" :closable="false">
            {{ logData.errorMsg }}
          </ElAlert>
        </ElDescriptionsItem>
      </ElDescriptions>
    </div>
    <template #footer>
      <ElButton @click="dialogVisible = false">关闭</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ElDescriptions, ElDescriptionsItem, ElTag, ElInput, ElAlert } from 'element-plus'

  interface Props {
    visible: boolean
    logData?: Api.SystemManage.OperLogListItem | null
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
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
