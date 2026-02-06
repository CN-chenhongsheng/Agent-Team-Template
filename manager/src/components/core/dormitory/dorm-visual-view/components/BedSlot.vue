<!--
  BedSlot 床位格子组件
  @description 展示单个床位的状态，已入住时悬停显示学生信息
  @author 陈鸿昇
-->
<template>
  <component :is="hasStudent ? ElPopover : 'div'" v-bind="hasStudent ? popoverProps : {}">
    <template v-if="hasStudent" #reference>
      <div :class="slotClasses" @click.stop="handleClick">
        <ArtSvgIcon v-if="statusIcon" :icon="statusIcon" class="text-sm" />
        <span v-else class="text-xs">{{ statusText }}</span>
      </div>
    </template>
    <template v-if="hasStudent" #default>
      <ArtStudentInfoPopover :student="bed.studentInfo ?? {}" />
    </template>
    <!-- 非入住状态直接渲染 -->
    <div v-if="!hasStudent" :class="slotClasses" @click.stop="handleClick">
      <ElTooltip v-if="showTooltip" :content="tooltipContent" placement="top">
        <div class="size-full flex-cc">
          <ArtSvgIcon v-if="statusIcon" :icon="statusIcon" class="text-sm" />
          <span v-else class="text-xs">{{ statusText }}</span>
        </div>
      </ElTooltip>
      <template v-else>
        <ArtSvgIcon v-if="statusIcon" :icon="statusIcon" class="text-sm" />
        <span v-else class="text-xs">{{ statusText }}</span>
      </template>
    </div>
  </component>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { ElPopover, ElTooltip } from 'element-plus'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import ArtStudentInfoPopover from '@/components/core/cards/art-student-info-popover/index.vue'
  import { BedStatus, type BedWithStudent } from '../types'
  import { useStatusColors } from '../composables/useStatusColors'

  defineOptions({ name: 'BedSlot' })

  // ==================== Props ====================
  interface Props {
    /** 床位数据 */
    bed: BedWithStudent
    /** 是否可点击 */
    clickable?: boolean
  }

  const props = withDefaults(defineProps<Props>(), {
    clickable: true
  })

  // ==================== Emits ====================
  const emit = defineEmits<{
    (e: 'click', bed: BedWithStudent): void
    (e: 'empty-bed-click', bed: BedWithStudent): void
  }>()

  // ==================== Composables ====================
  const { getBedStatusClasses, getBedStatusText, getBedStatusIcon } = useStatusColors()

  // ==================== Computed ====================

  /** 是否有学生入住 */
  const hasStudent = computed(
    () => props.bed.bedStatus === BedStatus.OCCUPIED && props.bed.studentInfo
  )

  /** 床位状态文本 */
  const statusText = computed(() => {
    if (props.bed.bedStatus === BedStatus.AVAILABLE) return '空'
    return ''
  })

  /** 床位状态图标 */
  const statusIcon = computed(() => getBedStatusIcon(props.bed.bedStatus))

  /** 是否显示 Tooltip */
  const showTooltip = computed(
    () =>
      props.bed.bedStatus === BedStatus.MAINTENANCE || props.bed.bedStatus === BedStatus.RESERVED
  )

  /** Tooltip 内容 */
  const tooltipContent = computed(() => {
    if (props.bed.bedStatus === BedStatus.MAINTENANCE) {
      return `维修中 - ${props.bed.bedNumber}号床位`
    }
    if (props.bed.bedStatus === BedStatus.RESERVED) {
      return `已预订 - ${props.bed.bedNumber}号床位`
    }
    return getBedStatusText(props.bed.bedStatus)
  })

  /** 床位格子样式类 */
  const slotClasses = computed(() => {
    const baseClasses = 'size-8 flex-cc rounded tad-200'
    const statusClasses = getBedStatusClasses(props.bed.bedStatus)
    const cursorClass = props.clickable
      ? props.bed.bedStatus === BedStatus.MAINTENANCE
        ? 'cursor-not-allowed'
        : 'cursor-pointer'
      : ''

    return [baseClasses, statusClasses, cursorClass].filter(Boolean).join(' ')
  })

  /** Popover 配置 */
  const popoverProps = computed(() => ({
    placement: 'top' as const,
    trigger: 'hover' as const,
    width: 320,
    popperClass: 'student-info-popover'
  }))

  // ==================== Methods ====================

  /** 点击处理 */
  const handleClick = () => {
    if (!props.clickable) return
    if (props.bed.bedStatus === BedStatus.MAINTENANCE) return

    // 空床位点击：用于分配学生
    if (props.bed.bedStatus === BedStatus.AVAILABLE) {
      emit('empty-bed-click', props.bed)
      return
    }

    // 非空床位点击：查看详情等
    emit('click', props.bed)
  }
</script>

<style lang="scss" scoped>
  // 所有样式通过 Tailwind 类实现，无需额外 SCSS
</style>
