<!--
  FloorSelector 楼栋/楼层选择器组件
  @description 级联选择楼栋和楼层
  @author 陈鸿昇
-->
<template>
  <ElCascader
    v-model="selectedValue"
    :options="options"
    :props="cascaderProps"
    :placeholder="placeholder"
    filterable
    clearable
    class="w-60"
    @change="handleChange"
  >
    <template #default="{ node, data }">
      <div class="flex-c gap-2">
        <ArtSvgIcon :icon="node.isLeaf ? 'ri:door-line' : 'ri:building-line'" class="text-g-500" />
        <span>
          {{
            // 楼栋节点：直接用名称
            !node.isLeaf
              ? data.name
              : // “全部层数”节点
                typeof data.id === 'string' && String(data.id).startsWith('all:')
                ? '全部层数'
                : // 具体楼层：第 n 层
                  data.floorNumber
                  ? `第 ${data.floorNumber} 层`
                  : data.name
          }}
        </span>
      </div>
    </template>
  </ElCascader>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue'
  import { ElCascader } from 'element-plus'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import type { BuildingOption, FloorSelectValue } from '../types'

  defineOptions({ name: 'FloorSelector' })

  // ==================== Props ====================
  interface Props {
    /** 选中的楼层ID */
    modelValue?: FloorSelectValue
    /** 楼栋选项列表 */
    options: BuildingOption[]
    /** 占位文本 */
    placeholder?: string
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: null,
    options: () => [],
    placeholder: '选择楼栋/楼层'
  })

  // ==================== Emits ====================
  const emit = defineEmits<{
    (e: 'update:modelValue', value: FloorSelectValue): void
    (e: 'change', value: FloorSelectValue): void
  }>()

  // ==================== State ====================

  /** 选中值（数组形式，存储 [buildingId, floorValue]） */
  const selectedValue = ref<Array<string | number>>([])

  // ==================== Cascader Config ====================

  /** 级联选择器配置 */
  const cascaderProps = {
    value: 'id',
    label: 'name',
    children: 'floors',
    expandTrigger: 'hover' as const,
    emitPath: true
  }

  // ==================== Watch ====================

  // 监听外部 modelValue 变化，同步更新内部值
  watch(
    () => props.modelValue,
    (newVal) => {
      if (newVal === null) {
        selectedValue.value = []
        return
      }

      // 根据 modelValue 找到完整路径
      for (const building of props.options) {
        for (const floor of building.floors || []) {
          if (floor.id === newVal) {
            selectedValue.value = [building.id, floor.id]
            return
          }
        }
      }
    },
    { immediate: true }
  )

  // ==================== Methods ====================

  /** 选择变更处理 */
  const handleChange = (value: unknown) => {
    const arr = value as Array<string | number> | null | undefined
    const newValue = arr && arr.length >= 2 ? (arr[arr.length - 1] as FloorSelectValue) : null
    emit('update:modelValue', newValue)
    emit('change', newValue)
  }
</script>

<style lang="scss" scoped>
  // 所有样式通过 Tailwind 类实现
</style>
