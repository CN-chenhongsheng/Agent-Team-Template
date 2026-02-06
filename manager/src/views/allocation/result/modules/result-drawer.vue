<template>
  <ArtDrawer v-model="drawerVisible" title="匹配详情" width="600px">
    <template v-if="resultData">
      <ElDescriptions :column="2" border class="mb-4">
        <ElDescriptionsItem label="学号">{{ resultData.studentNo }}</ElDescriptionsItem>
        <ElDescriptionsItem label="姓名">{{ resultData.studentName }}</ElDescriptionsItem>
        <ElDescriptionsItem label="分配房间">{{ resultData.allocatedRoomCode }}</ElDescriptionsItem>
        <ElDescriptionsItem label="匹配分">
          <ElTag :type="getScoreType(resultData.matchScore)">{{ resultData.matchScore }}</ElTag>
        </ElDescriptionsItem>
      </ElDescriptions>

      <div class="section mb-4">
        <div class="section-title">匹配优势</div>
        <div class="section-content">
          <template v-if="resultData.advantages && resultData.advantages.length">
            <ElTag
              v-for="(item, index) in resultData.advantages"
              :key="index"
              type="success"
              class="mr-2 mb-2"
            >
              {{ item }}
            </ElTag>
          </template>
          <span v-else class="text-gray-400">无</span>
        </div>
      </div>

      <div class="section mb-4">
        <div class="section-title">潜在冲突</div>
        <div class="section-content">
          <template v-if="resultData.conflictReasons && resultData.conflictReasons.length">
            <ElTag
              v-for="(item, index) in resultData.conflictReasons"
              :key="index"
              type="danger"
              class="mr-2 mb-2"
            >
              {{ item }}
            </ElTag>
          </template>
          <span v-else class="text-gray-400">无</span>
        </div>
      </div>

      <div class="section">
        <div class="section-title">室友信息</div>
        <div class="section-content">
          <template v-if="resultData.roommateNames && resultData.roommateNames.length">
            {{ resultData.roommateNames.join('、') }}
          </template>
          <span v-else class="text-gray-400">空房间</span>
        </div>
      </div>
    </template>

    <template #footer>
      <ElButton @click="drawerVisible = false">关闭</ElButton>
    </template>
  </ArtDrawer>
</template>

<script setup lang="ts">
  import ArtDrawer from '@/components/core/layouts/art-drawer/index.vue'

  interface Props {
    visible: boolean
    resultData?: Api.Allocation.ResultListItem | null
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
  }

  const props = withDefaults(defineProps<Props>(), {
    resultData: null
  })
  const emit = defineEmits<Emits>()

  const drawerVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
  })

  // 匹配分颜色
  const getScoreType = (score: number) => {
    if (score >= 80) return 'success'
    if (score >= 60) return 'warning'
    return 'danger'
  }
</script>

<style lang="scss" scoped>
  .section {
    .section-title {
      font-weight: 600;
      margin-bottom: 8px;
      color: var(--el-text-color-primary);
    }

    .section-content {
      padding: 12px;
      background: var(--el-fill-color-light);
      border-radius: 6px;
    }
  }
</style>
