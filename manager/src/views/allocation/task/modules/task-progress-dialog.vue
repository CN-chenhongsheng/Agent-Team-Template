<template>
  <ElDialog
    v-model="dialogVisible"
    title="执行进度"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="text-center py-6">
      <ElProgress
        type="circle"
        :percentage="progressData.progressPercent || 0"
        :status="progressData.completed ? 'success' : undefined"
        :width="150"
      />
      <div class="mt-4 text-lg">{{ progressData.currentStage || '准备中...' }}</div>
      <div class="mt-2 text-gray-500">
        <span v-if="progressData.successCount !== undefined">
          成功: {{ progressData.successCount }} | 失败: {{ progressData.failedCount }}
        </span>
      </div>
      <div v-if="progressData.errorMessage" class="mt-2 text-red-500">
        {{ progressData.errorMessage }}
      </div>
    </div>

    <template #footer>
      <ElButton @click="dialogVisible = false">关闭</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { fetchGetTaskProgress } from '@/api/allocation-manage'

  interface Props {
    visible: boolean
    taskId?: number
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'completed'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
  })

  const progressData = ref<Api.Allocation.TaskProgress>({
    taskId: 0,
    status: 0,
    progressPercent: 0,
    completed: false
  })

  let progressTimer: ReturnType<typeof setInterval> | null = null

  // 开始轮询进度
  const startPolling = () => {
    stopPolling()
    if (!props.taskId) return

    const poll = async () => {
      try {
        const data = await fetchGetTaskProgress(props.taskId!)
        progressData.value = data

        if (data.completed) {
          stopPolling()
          emit('completed')
        }
      } catch (e) {
        stopPolling()
      }
    }

    poll()
    progressTimer = setInterval(poll, 2000)
  }

  // 停止轮询
  const stopPolling = () => {
    if (progressTimer) {
      clearInterval(progressTimer)
      progressTimer = null
    }
  }

  const handleClose = () => {
    stopPolling()
  }

  // 监听visible变化
  watch(
    () => props.visible,
    (val) => {
      if (val && props.taskId) {
        progressData.value = {
          taskId: props.taskId,
          status: 1,
          progressPercent: 0,
          completed: false
        }
        startPolling()
      } else {
        stopPolling()
      }
    }
  )

  // 组件卸载时停止轮询
  onUnmounted(() => {
    stopPolling()
  })
</script>
