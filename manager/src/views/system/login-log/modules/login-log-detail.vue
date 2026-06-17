<template>
  <ElDialog v-model="dialogVisible" title="登入日志详情" width="800px">
    <div class="login-log-detail">
      <ElDescriptions :column="2" border>
        <ElDescriptionsItem label="用户账号">
          {{ logData?.username || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="登录类型">
          <ElTag :type="logData?.loginType === 'login' ? 'primary' : 'info'">
            {{ logData?.loginTypeText || '-' }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="登录状态">
          <ElTag :type="loginStatusTagType">
            {{ logData?.loginStatusText || '-' }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="IP地址">
          {{ logData?.ipAddress || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="登录地点" :span="2">
          {{ logData?.loginLocation || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="浏览器">
          {{ logData?.browser || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="操作系统">
          {{ logData?.os || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="登录时间" :span="2">
          {{ logData?.loginTime || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem v-if="logData?.message" label="提示消息" :span="2">
          <ElAlert type="info" :closable="false">
            {{ logData.message }}
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
  import { ElDescriptions, ElDescriptionsItem, ElAlert } from 'element-plus'

  defineOptions({ name: 'LoginLogDetail' })

  interface Props {
    visible: boolean
    logData?: Api.SystemManage.LoginLogListItem | null
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

  // 登录状态标签颜色（与表格保持一致：0-失败 danger / 1-成功 success / 2-登出 info）
  const loginStatusTagType = computed(() => {
    const typeMap: Record<number, 'danger' | 'success' | 'info'> = {
      0: 'danger',
      1: 'success',
      2: 'info'
    }
    return typeMap[props.logData?.loginStatus as 0 | 1 | 2] || 'info'
  })
</script>
