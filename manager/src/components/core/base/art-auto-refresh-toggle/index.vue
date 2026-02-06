<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { ElTooltip } from 'element-plus'
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

/**
 * 自动刷新开关（极简图标按钮）
 *
 * 用途：
 * - 放在表格工具栏等位置，控制「自动轻量刷新当前页数据」
 * - 内部封装定时器逻辑，对外只暴露 @refresh 回调
 *
 * 推荐用法：
 * ```vue
 * <!-- 30 秒自动轻量刷新当前页 -->
 * <ArtAutoRefreshToggle
 *   v-model="autoRefreshEnabled"
 *   :interval="30 * 1000"
 *   @refresh="refreshSoft"
 * />
 * ```
 */

interface Props {
  /**
   * 是否开启自动刷新（v-model）
   */
  modelValue?: boolean
  /**
   * 刷新间隔（毫秒）
   */
  interval?: number
  /**
   * 开启时是否立刻触发一次刷新
   */
  immediate?: boolean
  /**
   * 开启状态下的提示文案
   */
  tooltipTextEnabled?: string
  /**
   * 关闭状态下的提示文案
   */
  tooltipTextDisabled?: string
}

const props = withDefaults(defineProps<Props>(), {
  interval: 30_000,
  immediate: true,
  tooltipTextEnabled: '自动刷新：已开启，每30秒轻量刷新当前页',
  tooltipTextDisabled: '自动刷新：已关闭，点击开启自动刷新'
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'refresh'): void
}>()

defineOptions({ name: 'ArtAutoRefreshToggle' })

const enabled = ref<boolean>(props.modelValue ?? false)
let timer: ReturnType<typeof setInterval> | null = null

const tooltipContent = computed(() =>
  enabled.value ? props.tooltipTextEnabled : props.tooltipTextDisabled
)

const clearTimer = (): void => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

const startTimer = (): void => {
  if (!enabled.value || !props.interval || props.interval <= 0) return

  clearTimer()
  timer = setInterval(() => {
    emit('refresh')
  }, props.interval)
}

watch(
  () => props.modelValue,
  (val) => {
    if (typeof val === 'boolean') {
      enabled.value = val
    }
  }
)

watch(
  () => props.interval,
  () => {
    if (enabled.value) {
      startTimer()
    }
  }
)

watch(
  enabled,
  (val, oldVal) => {
    clearTimer()
    emit('update:modelValue', val)

    if (val) {
      if (props.immediate && !oldVal) {
        emit('refresh')
      }
      startTimer()
    }
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  clearTimer()
})

const handleToggle = (): void => {
  enabled.value = !enabled.value
}
</script>

<template>
  <ElTooltip :content="tooltipContent" placement="bottom">
    <button
      type="button"
      class="inline-flex h-7 w-8 cursor-pointer items-center justify-center rounded-md border transition-colors duration-150"
      :class="
        enabled
          ? 'bg-primary text-white border-primary hover:bg-primary/90'
          : 'bg-g-100 text-g-600 border-g-200 hover:bg-g-200'
      "
      @click="handleToggle"
    >
      <ArtSvgIcon name="i-tabler-refresh" class="h-3.5 w-3.5" />
    </button>
  </ElTooltip>
</template>

