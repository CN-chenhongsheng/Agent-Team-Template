<template>
  <ElDrawer
    v-model="visible"
    title="通知详情"
    :size="drawerWidth"
    destroy-on-close
    @close="handleClose"
  >
    <div v-loading="loading" class="notice-drawer-content">
      <template v-if="!loading && noticeDetail">
        <!-- 基本信息 -->
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="通知标题" :span="2">
            <div class="flex items-center gap-2">
              <ElTag v-if="noticeDetail.isTop" type="danger" size="small">置顶</ElTag>
              <span>{{ noticeDetail.title }}</span>
            </div>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="通知类型">
            {{ noticeDetail.noticeTypeText || '--' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="状态">
            <ElTag :type="noticeDetail.status === 1 ? 'info' : 'success'">
              {{ noticeDetail.statusText || '--' }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="发布人">
            {{ noticeDetail.publisherName || '--' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="发布时间">
            {{ noticeDetail.publishTime || '--' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="阅读次数" :span="2">
            {{ noticeDetail.readCount || 0 }} 次
          </ElDescriptionsItem>
          <ElDescriptionsItem v-if="noticeDetail.targetFloorNames" label="目标楼层" :span="2">
            {{ noticeDetail.targetFloorNames }}
          </ElDescriptionsItem>
          <ElDescriptionsItem v-if="noticeDetail.remark" label="备注" :span="2">
            {{ noticeDetail.remark }}
          </ElDescriptionsItem>
        </ElDescriptions>

        <!-- 封面图片 -->
        <div v-if="noticeDetail.coverImage" class="mt-4">
          <div class="text-sm font-semibold mb-2 text-gray-700">封面图片</div>
          <ElImage
            :src="noticeDetail.coverImage"
            :preview-src-list="[noticeDetail.coverImage]"
            fit="cover"
            class="w-full max-w-md rounded-lg"
          />
        </div>

        <!-- 通知内容 -->
        <div class="mt-4">
          <div class="text-sm font-semibold mb-2 text-gray-700">通知内容</div>
          <div class="notice-content p-4 bg-gray-50 rounded-lg" v-html="noticeDetail.content"></div>
        </div>

        <!-- 附件列表 -->
        <div v-if="attachmentList.length > 0" class="mt-4">
          <div class="text-sm font-semibold mb-2 text-gray-700">附件</div>
          <div class="space-y-2">
            <div
              v-for="(attachment, index) in attachmentList"
              :key="index"
              class="flex items-center gap-2 p-2 bg-gray-50 rounded"
            >
              <ElIcon><Document /></ElIcon>
              <ElLink :href="attachment" target="_blank" :underline="false">
                附件{{ index + 1 }}
              </ElLink>
            </div>
          </div>
        </div>
      </template>
    </div>

    <template #footer>
      <div class="flex justify-end gap-2">
        <ElButton @click="handleClose">关闭</ElButton>
        <ElButton type="primary" @click="handleEdit" v-permission="'system:notice:edit'">
          编辑
        </ElButton>
      </div>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
  import {
    ElDrawer,
    ElDescriptions,
    ElDescriptionsItem,
    ElTag,
    ElImage,
    ElButton,
    ElLink,
    ElIcon
  } from 'element-plus'
  import { Document } from '@element-plus/icons-vue'
  import { fetchGetNoticeDetail } from '@/api/system-manage'

  defineOptions({ name: 'NoticeDrawer' })

  interface Props {
    visible: boolean
    noticeId?: number
    noticeData?: Api.SystemManage.NoticeListItem
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'edit', noticeData: Api.SystemManage.NoticeListItem): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const visible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
  })

  const loading = ref(false)
  const noticeDetail = ref<Api.SystemManage.NoticeListItem>()
  const drawerWidth = computed(() => (window.innerWidth < 768 ? '90%' : '50%'))

  // 附件列表
  const attachmentList = computed(() => {
    if (!noticeDetail.value?.attachments) return []
    if (typeof noticeDetail.value.attachments === 'string') {
      try {
        return JSON.parse(noticeDetail.value.attachments)
      } catch {
        return []
      }
    }
    return noticeDetail.value.attachments
  })

  // 加载详情数据
  const loadNoticeDetail = async () => {
    if (!props.noticeId) {
      noticeDetail.value = props.noticeData
      return
    }

    loading.value = true
    try {
      const res = await fetchGetNoticeDetail(props.noticeId)
      noticeDetail.value = res
    } catch (error) {
      console.error('加载通知详情失败:', error)
      // Fallback 到传入的数据
      noticeDetail.value = props.noticeData
    } finally {
      loading.value = false
    }
  }

  // 监听 visible 变化，加载数据
  watch(
    () => props.visible,
    (val) => {
      if (val) {
        loadNoticeDetail()
      }
    },
    { immediate: true }
  )

  // 监听 noticeData 变化
  watch(
    () => props.noticeData,
    (val) => {
      if (val && !props.noticeId) {
        noticeDetail.value = val
      }
    }
  )

  const handleClose = () => {
    visible.value = false
  }

  const handleEdit = () => {
    if (noticeDetail.value) {
      emit('edit', noticeDetail.value)
    }
  }
</script>

<style scoped lang="scss">
  .notice-drawer-content {
    min-height: 200px;
  }

  .notice-content {
    line-height: 1.8;
    color: var(--el-text-color-regular);
    word-wrap: break-word;
    overflow-wrap: break-word;

    :deep(img) {
      max-width: 100%;
      height: auto;
      margin: 8px 0;
    }

    :deep(p) {
      margin: 8px 0;
    }

    :deep(h1),
    :deep(h2),
    :deep(h3),
    :deep(h4),
    :deep(h5),
    :deep(h6) {
      margin: 12px 0 8px;
      font-weight: 600;
    }

    :deep(ul),
    :deep(ol) {
      padding-left: 24px;
      margin: 8px 0;
    }

    :deep(blockquote) {
      padding-left: 12px;
      margin: 8px 0;
      color: var(--el-text-color-secondary);
      border-left: 4px solid var(--el-color-primary);
    }

    :deep(code) {
      padding: 2px 6px;
      font-family: 'Courier New', monospace;
      background-color: var(--el-fill-color-light);
      border-radius: 4px;
    }

    :deep(pre) {
      padding: 12px;
      overflow-x: auto;
      background-color: var(--el-fill-color-light);
      border-radius: 4px;

      code {
        padding: 0;
        background-color: transparent;
      }
    }
  }
</style>
