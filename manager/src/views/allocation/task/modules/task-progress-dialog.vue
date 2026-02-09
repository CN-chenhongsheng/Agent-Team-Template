<template>
  <ElDialog
    v-model="dialogVisible"
    title="任务执行进度"
    width="560px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="progress-panel">
      <!-- 顶部：圆形进度 + 状态 -->
      <div class="progress-panel__hero">
        <div class="progress-circle">
          <ElProgress
            type="circle"
            :percentage="displayPercent"
            :width="160"
            :stroke-width="8"
            :color="ringColor"
            :status="progressData.completed ? 'success' : undefined"
          >
            <template #default>
              <div v-if="progressData.completed" class="progress-circle__done">
                <ArtSvgIcon icon="ri:check-line" class="progress-circle__check" />
              </div>
              <div v-else class="progress-circle__inner">
                <span class="progress-circle__num">{{ displayPercent }}</span>
                <span class="progress-circle__pct">%</span>
              </div>
            </template>
          </ElProgress>
        </div>
        <div class="progress-panel__stage">
          <span
            v-if="!progressData.completed && !progressData.errorMessage"
            class="stage-indicator stage-indicator--running"
          >
            <span class="stage-indicator__dot" />
            {{ progressData.currentStage || '准备中...' }}
          </span>
          <span v-else-if="progressData.completed" class="stage-indicator stage-indicator--done">
            <ArtSvgIcon icon="ri:check-double-line" class="text-lg" />
            分配完成
          </span>
          <span v-else class="stage-indicator stage-indicator--error">
            <ArtSvgIcon icon="ri:error-warning-line" class="text-lg" />
            执行异常
          </span>
        </div>
        <!-- 已用时间 -->
        <div v-if="elapsedTime" class="progress-panel__elapsed">
          <ArtSvgIcon icon="ri:time-line" class="text-sm" />
          <span>已用时 {{ elapsedTime }}</span>
        </div>
      </div>

      <!-- 中部：统计卡片 -->
      <div
        v-if="progressData.successCount !== undefined || progressData.failedCount !== undefined"
        class="progress-panel__stats"
      >
        <div class="stat-card stat-card--success">
          <div class="stat-card__icon">
            <ArtSvgIcon icon="ri:check-line" />
          </div>
          <div class="stat-card__body">
            <div class="stat-card__value">{{ progressData.successCount ?? 0 }}</div>
            <div class="stat-card__label">成功</div>
          </div>
        </div>
        <div class="stat-card stat-card--danger">
          <div class="stat-card__icon">
            <ArtSvgIcon icon="ri:close-line" />
          </div>
          <div class="stat-card__body">
            <div class="stat-card__value">{{ progressData.failedCount ?? 0 }}</div>
            <div class="stat-card__label">失败</div>
          </div>
        </div>
        <div class="stat-card stat-card--primary">
          <div class="stat-card__icon">
            <ArtSvgIcon icon="ri:bar-chart-line" />
          </div>
          <div class="stat-card__body">
            <div class="stat-card__value">
              {{ (progressData.successCount ?? 0) + (progressData.failedCount ?? 0) }}
            </div>
            <div class="stat-card__label">已处理</div>
          </div>
        </div>
      </div>

      <!-- 底部：阶段时间线 -->
      <div class="progress-panel__timeline">
        <div
          v-for="(stage, index) in stages"
          :key="stage.key"
          class="timeline-item"
          :class="{
            'timeline-item--done': stage.status === 'done',
            'timeline-item--active': stage.status === 'active',
            'timeline-item--pending': stage.status === 'pending'
          }"
        >
          <div class="timeline-item__indicator">
            <div class="timeline-item__dot">
              <ArtSvgIcon v-if="stage.status === 'done'" icon="ri:check-line" class="text-xs" />
              <span v-else-if="stage.status === 'active'" class="timeline-item__pulse" />
            </div>
            <div v-if="index < stages.length - 1" class="timeline-item__line" />
          </div>
          <div class="timeline-item__content">
            <div class="timeline-item__label">{{ stage.label }}</div>
            <div class="timeline-item__desc">{{ stage.desc }}</div>
          </div>
        </div>
      </div>

      <!-- 错误信息 -->
      <ElAlert
        v-if="progressData.errorMessage"
        type="error"
        :title="progressData.errorMessage"
        show-icon
        :closable="false"
        class="mt-4"
      />
    </div>

    <template #footer>
      <ElButton @click="dialogVisible = false">关闭</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { subscribeTaskProgress, fetchGetTaskProgress } from '@/api/allocation-manage'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

  interface Props {
    visible: boolean
    taskId?: number
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'completed'): void
    (e: 'minimize'): void
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

  // ========== 圆环 ==========
  const displayPercent = computed(() => progressData.value.progressPercent || 0)

  /** 根据进度变色 */
  const ringColor = computed(() => {
    if (progressData.value.errorMessage) return '#ef4444'
    const pct = displayPercent.value
    if (pct >= 80) return '#10b981'
    if (pct >= 40) return '#3b82f6'
    return '#8b5cf6'
  })

  let unsubscribeSSE: (() => void) | null = null
  let fallbackTimer: ReturnType<typeof setInterval> | null = null

  // 计时器
  const startTimestamp = ref<number>(0)
  const elapsedSeconds = ref(0)
  let elapsedTimer: ReturnType<typeof setInterval> | null = null

  /** 已用时间格式化 */
  const elapsedTime = computed(() => {
    if (elapsedSeconds.value <= 0) return ''
    const m = Math.floor(elapsedSeconds.value / 60)
    const s = elapsedSeconds.value % 60
    if (m > 0) return `${m}分${s}秒`
    return `${s}秒`
  })

  /** 阶段定义 */
  const stageKeys = ['preparing', 'matching', 'writing', 'completed'] as const
  const stageLabels: Record<string, { label: string; desc: string }> = {
    preparing: { label: '数据准备', desc: '加载学生和床位数据' },
    matching: { label: '匹配计算', desc: '运行分配算法' },
    writing: { label: '结果写入', desc: '保存分配结果' },
    completed: { label: '执行完成', desc: '分配任务已完成' }
  }

  /** 根据 currentStage 推断当前阶段索引 */
  const currentStageIndex = computed(() => {
    if (progressData.value.completed) return stageKeys.length // 所有阶段完成
    const stage = progressData.value.currentStage || ''
    if (stage.includes('写入') || stage.includes('保存')) return 2
    if (stage.includes('匹配') || stage.includes('计算') || stage.includes('分配')) return 1
    if (stage.includes('准备') || stage.includes('加载')) return 0
    // 根据进度百分比推断
    const pct = progressData.value.progressPercent || 0
    if (pct >= 95) return 3
    if (pct >= 60) return 2
    if (pct >= 10) return 1
    return 0
  })

  /** 阶段列表（含状态） */
  const stages = computed(() => {
    return stageKeys.map((key, i) => {
      const info = stageLabels[key]
      let status: 'done' | 'active' | 'pending' = 'pending'
      if (i < currentStageIndex.value) status = 'done'
      else if (i === currentStageIndex.value) status = 'active'
      return { key, ...info, status }
    })
  })

  // ========== SSE 实时推送逻辑（带轮询降级） ==========

  const startSSE = () => {
    stopSSE()
    if (!props.taskId) return

    // 启动计时
    startTimestamp.value = Date.now()
    elapsedSeconds.value = 0
    elapsedTimer = setInterval(() => {
      elapsedSeconds.value = Math.floor((Date.now() - startTimestamp.value) / 1000)
    }, 1000)

    // 尝试 SSE 连接
    unsubscribeSSE = subscribeTaskProgress(props.taskId, {
      onProgress: (data) => {
        progressData.value = data
      },
      onComplete: (data) => {
        progressData.value = data
        stopSSE()
        emit('completed')
      },
      onError: (error) => {
        console.warn('[AllocationSSE] SSE 失败，降级到轮询:', error)
        // SSE 失败时降级到轮询
        if (unsubscribeSSE) {
          unsubscribeSSE()
          unsubscribeSSE = null
        }
        startFallbackPolling()
      }
    })
  }

  /** 降级轮询（SSE 连接失败时使用） */
  const startFallbackPolling = () => {
    if (fallbackTimer) return
    const poll = async () => {
      try {
        const data = await fetchGetTaskProgress(props.taskId!)
        progressData.value = data
        if (data.completed) {
          stopSSE()
          emit('completed')
        }
      } catch {
        stopSSE()
      }
    }
    poll()
    fallbackTimer = setInterval(poll, 2000)
  }

  const stopSSE = () => {
    if (unsubscribeSSE) {
      unsubscribeSSE()
      unsubscribeSSE = null
    }
    if (fallbackTimer) {
      clearInterval(fallbackTimer)
      fallbackTimer = null
    }
    if (elapsedTimer) {
      clearInterval(elapsedTimer)
      elapsedTimer = null
    }
  }

  const handleClose = () => {
    // 任务仍在运行中：最小化到通知栏，不停止 SSE
    if (!progressData.value.completed && !progressData.value.errorMessage && props.taskId) {
      emit('minimize')
      return
    }
    stopSSE()
  }

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
        elapsedSeconds.value = 0
        startSSE()
      } else {
        stopSSE()
      }
    }
  )

  // 暴露给父组件
  defineExpose({
    progressData,
    stopSSE
  })

  onUnmounted(() => {
    stopSSE()
  })
</script>

<style scoped lang="scss">
  .progress-panel {
    padding: 8px 0;

    // ========== 顶部英雄区 ==========
    &__hero {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      padding-bottom: 24px;
    }

    &__stage {
      .stage-indicator {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        padding: 6px 16px;
        font-size: 14px;
        font-weight: 500;
        border-radius: 20px;

        &--running {
          color: var(--el-color-primary);
          background-color: rgba(59, 130, 246, 0.08);

          .stage-indicator__dot {
            position: relative;
            width: 8px;
            height: 8px;
            background-color: var(--el-color-primary);
            border-radius: 50%;

            &::after {
              content: '';
              position: absolute;
              inset: -3px;
              border-radius: 50%;
              background-color: var(--el-color-primary);
              opacity: 0.3;
              animation: stage-pulse 1.5s ease-out infinite;
            }
          }
        }

        &--done {
          color: #059669;
          background-color: rgba(16, 185, 129, 0.08);
        }

        &--error {
          color: #dc2626;
          background-color: rgba(239, 68, 68, 0.08);
        }
      }
    }

    &__elapsed {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    // ========== 统计卡片 ==========
    &__stats {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 12px;
      padding: 0 4px 20px;
    }

    // ========== 时间线 ==========
    &__timeline {
      padding: 0 4px 8px 4px;
    }
  }

  // ========== 圆形进度 ==========
  .progress-circle {
    :deep(.el-progress-circle) {
      // 轨道圆角
      svg path:first-child {
        stroke-linecap: round;
      }
      // 进度弧圆角
      svg path:last-child {
        stroke-linecap: round;
        transition: stroke-dashoffset 0.6s ease;
      }
    }

    &__inner {
      display: flex;
      align-items: baseline;
      justify-content: center;
    }

    &__num {
      font-size: 38px;
      font-weight: 700;
      line-height: 1;
      color: var(--el-text-color-primary);
    }

    &__pct {
      font-size: 16px;
      font-weight: 500;
      margin-left: 1px;
      color: var(--el-text-color-secondary);
    }

    &__done {
      display: flex;
      align-items: center;
      justify-content: center;
    }

    &__check {
      font-size: 48px;
      color: #10b981;
      animation: check-pop 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
    }
  }

  @keyframes check-pop {
    0% {
      transform: scale(0);
      opacity: 0;
    }
    100% {
      transform: scale(1);
      opacity: 1;
    }
  }

  // ========== 统计卡片 ==========
  .stat-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 16px;
    border-radius: 10px;
    transition: all 0.2s ease;

    &__icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 36px;
      height: 36px;
      font-size: 18px;
      border-radius: 8px;
      flex-shrink: 0;
    }

    &__body {
      display: flex;
      flex-direction: column;
    }

    &__value {
      font-size: 20px;
      font-weight: 700;
      line-height: 1.2;
    }

    &__label {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    &--success {
      background-color: rgba(16, 185, 129, 0.06);

      .stat-card__icon {
        color: #059669;
        background-color: rgba(16, 185, 129, 0.12);
      }

      .stat-card__value {
        color: #059669;
      }
    }

    &--danger {
      background-color: rgba(239, 68, 68, 0.06);

      .stat-card__icon {
        color: #dc2626;
        background-color: rgba(239, 68, 68, 0.12);
      }

      .stat-card__value {
        color: #dc2626;
      }
    }

    &--primary {
      background-color: rgba(59, 130, 246, 0.06);

      .stat-card__icon {
        color: #2563eb;
        background-color: rgba(59, 130, 246, 0.12);
      }

      .stat-card__value {
        color: #2563eb;
      }
    }
  }

  // ========== 时间线 ==========
  .timeline-item {
    display: flex;
    gap: 14px;

    &__indicator {
      display: flex;
      flex-direction: column;
      align-items: center;
      flex-shrink: 0;
      width: 20px;
    }

    &__dot {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 20px;
      height: 20px;
      border-radius: 50%;
      border: 2px solid var(--el-border-color-light);
      background-color: var(--el-bg-color);
      flex-shrink: 0;
      transition: all 0.3s ease;
    }

    &__line {
      flex: 1;
      width: 2px;
      min-height: 20px;
      background-color: var(--el-border-color-lighter);
      transition: background-color 0.3s ease;
    }

    &__pulse {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background-color: var(--el-color-primary);
      animation: stage-pulse 1.5s ease-out infinite;
    }

    &__content {
      padding-bottom: 20px;
    }

    &__label {
      font-size: 14px;
      font-weight: 500;
      line-height: 20px;
      color: var(--el-text-color-regular);
    }

    &__desc {
      font-size: 12px;
      color: var(--el-text-color-placeholder);
      margin-top: 2px;
    }

    // 已完成
    &--done {
      .timeline-item__dot {
        border-color: #10b981;
        background-color: #10b981;
        color: #fff;
      }

      .timeline-item__line {
        background-color: #10b981;
      }

      .timeline-item__label {
        color: var(--el-text-color-primary);
      }
    }

    // 进行中
    &--active {
      .timeline-item__dot {
        border-color: var(--el-color-primary);
        background-color: var(--el-bg-color);
      }

      .timeline-item__label {
        color: var(--el-color-primary);
        font-weight: 600;
      }
    }

    // 待处理
    &--pending {
      .timeline-item__label {
        color: var(--el-text-color-placeholder);
      }
    }

    // 最后一项不需要底部间距
    &:last-child {
      .timeline-item__content {
        padding-bottom: 0;
      }
    }
  }

  // ========== 动画 ==========
  @keyframes stage-pulse {
    0% {
      transform: scale(1);
      opacity: 0.3;
    }
    100% {
      transform: scale(2.2);
      opacity: 0;
    }
  }

  // ========== 暗色模式 ==========
  :global(.dark) {
    .stat-card {
      &--success {
        background-color: rgba(16, 185, 129, 0.1);
        .stat-card__value {
          color: #34d399;
        }
      }

      &--danger {
        background-color: rgba(239, 68, 68, 0.1);
        .stat-card__value {
          color: #f87171;
        }
      }

      &--primary {
        background-color: rgba(59, 130, 246, 0.1);
        .stat-card__value {
          color: #60a5fa;
        }
      }
    }
  }
</style>
