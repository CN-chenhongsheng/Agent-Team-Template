<template>
  <ElDialog
    v-model="dialogVisible"
    width="85%"
    align-center
    append-to-body
    destroy-on-close
    class="scan-result-dialog"
    title="扫描结果"
  >
    <div class="scan-result-layout h-full flex flex-col gap-4">
      <ElCard
        shadow="never"
        class="shrink-0 !rounded-[calc(var(--el-border-radius-base))] border-[var(--el-border-color-lighter)]"
      >
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <div class="text-sm font-medium text-[var(--el-text-color-primary)]">
              前端校验未通过，请根据列表逐行修正数据
            </div>
            <div class="mt-1 text-xs text-[var(--el-text-color-secondary)]">
              建议按行从上到下依次处理，修正后重新导出并再次扫描。
            </div>
          </div>
          <div class="flex items-center gap-3">
            <div class="flex flex-col items-end gap-1 text-right">
              <div class="flex items-baseline gap-2">
                <span class="text-2xl font-semibold text-[var(--el-color-danger)]">
                  {{ meta?.errorCount ?? 0 }}
                </span>
                <span class="text-xs text-[var(--el-text-color-secondary)]">处问题</span>
              </div>
              <ElTag v-if="meta" type="danger" size="small">
                预览前 {{ meta.preview.length }} 条
                <span v-if="meta.truncated">（已截断）</span>
              </ElTag>
            </div>
            <ElTooltip content="下载错误 Excel" placement="top">
              <ElButton
                size="small"
                type="default"
                class="download-icon-btn ml-3 flex h-9 w-9 items-center justify-center !px-0 rounded-[calc(var(--el-border-radius-base))] bg-white border-[var(--el-border-color-lighter)] text-[var(--el-color-primary)] transition-all duration-200"
                :icon="Download"
                :disabled="!meta || !meta.preview.length"
                @click="handleDownloadErrorExcel"
              />
            </ElTooltip>
          </div>
        </div>
      </ElCard>

      <div class="scan-result-main flex-1 min-h-0 overflow-hidden p-0.5 flex gap-4">
        <!-- 左侧：表格视图 -->
        <div
          class="scan-table-wrapper flex min-w-0 flex-1 flex-col min-h-0 overflow-auto rounded-[calc(var(--el-border-radius-base)+4px)] bg-[var(--el-fill-color-blank)]"
        >
          <div v-if="tableRows.length > 0">
            <ElTable :data="tableRows" border size="small" class="scan-result-table">
              <ElTableColumn prop="index" label="#" width="60" />
              <ElTableColumn prop="row" label="行号" width="80" />
              <ElTableColumn prop="column" label="字段" width="130" show-overflow-tooltip />
              <ElTableColumn prop="value" label="原值" min-width="160" show-overflow-tooltip />
              <ElTableColumn
                prop="message"
                label="错误原因"
                min-width="220"
                show-overflow-tooltip
              />
            </ElTable>
          </div>

          <div
            v-else
            class="flex flex-1 items-center justify-center text-xs text-[var(--el-text-color-secondary)]"
          >
            暂无扫描结果
          </div>
        </div>

        <!-- 右侧：按行聚合的错误列表 -->
        <div class="flex w-80 flex-col gap-3">
          <ElCard
            shadow="never"
            class="shrink-0 !rounded-[calc(var(--el-border-radius-base))]"
          >
            <div class="mb-2 text-sm font-medium text-[var(--el-text-color-primary)]">
              按行查看错误
            </div>
            <div class="text-xs text-[var(--el-text-color-secondary)] leading-relaxed">
              列表按照 Excel 行号分组，建议从上到下逐行修正。修正后可重新导出并再次扫描。
            </div>
          </ElCard>

          <div
            class="scan-error-groups flex-1 overflow-auto rounded-[calc(var(--el-border-radius-base)+4px)] bg-[var(--el-fill-color-light)] p-4"
          >
            <div
              v-for="group in groupedByRow"
              :key="group.row"
              class="mb-3 rounded-[calc(var(--el-border-radius-base)+4px)] bg-[var(--el-fill-color-blank)] p-3 last:mb-0 border border-transparent transition-all duration-200 hover:bg-[var(--el-fill-color-light)] hover:border-[var(--el-border-color-lighter)] hover:-translate-y-px"
            >
              <div class="mb-1 flex items-center justify-between">
                <div class="text-xs font-medium text-[var(--el-text-color-primary)]">
                  <span
                    class="inline-flex h-5 items-center rounded-full bg-[var(--el-color-primary-light-9)] px-2 text-[11px] text-[var(--el-color-primary)]"
                  >
                    第 {{ group.row }} 行
                  </span>
                  <span class="ml-2 text-[var(--el-text-color-secondary)]">
                    共 {{ group.items.length }} 处
                  </span>
                </div>
              </div>
              <ul class="space-y-1">
                <li
                  v-for="(item, idx) in group.items"
                  :key="idx"
                  class="text-[11px] leading-relaxed text-[var(--el-text-color-regular)]"
                >
                  <span class="font-medium">{{ item.column }}：</span>
                  <span>{{ item.message }}</span>
                  <span v-if="item.value" class="ml-1 text-[var(--el-text-color-secondary)]">
                    原值：<code class="rounded bg-[var(--el-fill-color-light)] px-1 py-0.5">{{
                      item.value
                    }}</code>
                  </span>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import {
    ElDialog,
    ElCard,
    ElTable,
    ElTableColumn,
    ElTag,
    ElButton,
    ElMessage,
    ElTooltip
  } from 'element-plus'
  import { Download } from '@element-plus/icons-vue'
  import * as XLSX from 'xlsx'
  import FileSaver from 'file-saver'

  interface PreviewItem {
    row: number
    column: string
    message: string
    value?: string
  }

  interface Meta {
    errorCount: number
    truncated: boolean
    preview: PreviewItem[]
  }

  interface Props {
    visible: boolean
    meta: Meta | null
  }

  const props = defineProps<Props>()

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void
  }>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value: boolean) => emit('update:visible', value)
  })

  const meta = computed(() => props.meta)

  const tableRows = computed(() => {
    if (!props.meta) return []
    return props.meta.preview.map((item, index) => ({
      index: index + 1,
      row: item.row,
      column: item.column,
      value: item.value ?? '',
      message: item.message
    }))
  })

  const groupedByRow = computed(() => {
    if (!props.meta) return []
    const map = new Map<number, PreviewItem[]>()
    props.meta.preview.forEach((item) => {
      const list = map.get(item.row) || []
      list.push(item)
      map.set(item.row, list)
    })
    return Array.from(map.entries())
      .sort((a, b) => a[0] - b[0])
      .map(([row, items]) => ({ row, items }))
  })

  const handleDownloadErrorExcel = () => {
    if (!props.meta || !props.meta.preview.length) {
      ElMessage.warning('暂无可导出的错误数据')
      return
    }

    const rows = props.meta.preview.map((item, index) => ({
      序号: index + 1,
      行号: item.row,
      字段: item.column,
      原值: item.value ?? '',
      错误原因: item.message
    }))

    const worksheet = XLSX.utils.json_to_sheet(rows)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '错误明细')

    const excelBuffer = XLSX.write(workbook, {
      bookType: 'xlsx',
      type: 'array',
      compression: true
    })

    const blob = new Blob([excelBuffer], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })

    const filename = `学生导入错误明细_${new Date().toISOString().slice(0, 10)}.xlsx`
    FileSaver.saveAs(blob, filename)
  }
</script>

<style scoped lang="scss">
  .scan-result-dialog {
    :deep(.el-dialog__body) {
      padding: 16px 20px;
      height: 75vh;
      overflow: hidden;
    }
  }

  .scan-result-main {
    height: 100%;
    max-height: calc(75vh - 100px);

    /* 美化滚动条 */
    &::-webkit-scrollbar {
      width: 8px;
      height: 8px;
    }

    &::-webkit-scrollbar-track {
      background: var(--el-fill-color-lighter);
      border-radius: var(--el-border-radius-small);
    }

    &::-webkit-scrollbar-thumb {
      background: var(--el-border-color);
      border-radius: var(--el-border-radius-small);
      transition: background 0.2s;

      &:hover {
        background: var(--el-border-color-darker);
      }
    }
  }

  .scan-result-table {
    border-radius: calc(var(--el-border-radius-base));
    overflow: hidden;

    :deep(.el-table__header-wrapper th) {
      background-color: var(--el-fill-color-light);
      font-weight: 600;
    }

    :deep(.el-table__body-wrapper) {
      /* 表格内部滚动条美化 */
      &::-webkit-scrollbar {
        width: 6px;
        height: 6px;
      }

      &::-webkit-scrollbar-track {
        background: transparent;
      }

      &::-webkit-scrollbar-thumb {
        background: var(--el-border-color);
        border-radius: var(--el-border-radius-small);

        &:hover {
          background: var(--el-border-color-darker);
        }
      }
    }
  }

  .scan-error-groups {
    /* 右侧错误列表滚动条美化 */
    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--el-border-color);
      border-radius: var(--el-border-radius-small);

      &:hover {
        background: var(--el-border-color-darker);
      }
    }
  }

  .download-icon-btn {
    &:hover:not(.is-disabled) {
      border-color: var(--el-color-primary);
      background-color: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
      transform: translateY(-1px);
    }

    &.is-disabled {
      box-shadow: none;
      color: var(--el-text-color-placeholder);
    }
  }
</style>
