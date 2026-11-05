<template>
  <ElDialog v-model="dialogVisible" :title="$t('system.loginLog.detailTitle')" width="800px">
    <div class="login-log-detail">
      <ElDescriptions :column="2" border>
        <ElDescriptionsItem :label="$t('system.loginLog.userAccount')">
          {{ logData?.username || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('system.loginLog.loginType')">
          <ElTag :type="logData?.loginType === 'login' ? 'primary' : 'info'">
            {{ logData?.loginTypeText || '-' }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('system.loginLog.loginStatus')">
          <ElTag :type="loginStatusTagType">
            {{ logData?.loginStatusText || '-' }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('system.loginLog.ip')">
          {{ logData?.ipAddress || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('system.loginLog.location')" :span="2">
          {{ logData?.loginLocation || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('system.loginLog.browser')">
          {{ logData?.browser || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('system.loginLog.os')">
          {{ logData?.os || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('system.loginLog.loginTime')" :span="2">
          {{ logData?.loginTime || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem
          v-if="logData?.message"
          :label="$t('system.loginLog.message')"
          :span="2"
        >
          <ElAlert type="info" :closable="false">
            {{ logData.message }}
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
  import { ElDescriptions, ElDescriptionsItem, ElAlert } from 'element-plus'

  defineOptions({ name: 'LoginLogDetail' })

  interface Props {
    modelValue: boolean
    logData?: Api.SystemManage.LoginLogListItem | null
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
