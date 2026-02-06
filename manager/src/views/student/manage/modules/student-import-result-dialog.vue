<!-- 学生导入结果弹窗 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    width="85%"
    align-center
    append-to-body
    destroy-on-close
    class="import-result-dialog"
    title="导入结果"
  >
    <div v-if="result" class="import-result-layout h-full flex flex-col gap-4">
      <ElCard
        shadow="never"
        class="shrink-0 !rounded-[calc(var(--el-border-radius-base))] border-[var(--el-border-color-lighter)]"
      >
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <div class="text-sm font-medium text-[var(--el-text-color-primary)]">
              导入完成
            </div>
            <div class="mt-1 text-xs text-[var(--el-text-color-secondary)]">
              成功 {{ result.successCount ?? 0 }} 条，失败 {{ result.failCount ?? 0 }} 条
            </div>
            <div
              v-if="(result.failCount ?? 0) > 0 && (result.successCount ?? 0) === 0"
              class="mt-2 text-xs text-[var(--el-color-warning)]"
            >
              若全部失败，多为 Excel 中「校区/院系/专业/班级」与系统配置不一致，请核对后端日志中的「导入可用校区」与「示例失败原因」。
            </div>
          </div>
          <div class="flex items-center gap-3">
            <div class="flex flex-col items-end gap-1 text-right">
              <div class="flex items-baseline gap-2">
                <span class="text-2xl font-semibold text-[var(--el-color-success)]">
                  {{ result.successCount ?? 0 }}
                </span>
                <span class="text-xs text-[var(--el-text-color-secondary)]">成功</span>
              </div>
              <div class="flex items-baseline gap-2 mt-1">
                <span class="text-2xl font-semibold text-[var(--el-color-danger)]">
                  {{ result.failCount ?? 0 }}
                </span>
                <span class="text-xs text-[var(--el-text-color-secondary)]">失败</span>
              </div>
            </div>
          </div>
        </div>
      </ElCard>

      <div
        v-if="result.errors && result.errors.length > 0"
        class="import-result-main flex-1 min-h-0 overflow-hidden p-0.5 flex gap-4"
      >
        <div
          v-if="(result.failCount ?? 0) > (result.errors?.length ?? 0)"
          class="shrink-0 rounded-[calc(var(--el-border-radius-base))] bg-[var(--el-color-warning-light-9)] px-3 py-2 text-xs text-[var(--el-color-warning-dark-2)]"
        >
          仅展示前 {{ result.errors?.length ?? 0 }} 条错误，共 {{ result.failCount ?? 0 }} 条失败。完整失败原因可查看后端日志（关键字：本批次全部失败、示例失败原因）。
        </div>
        <!-- 错误列表表格 -->
        <div
          class="import-table-wrapper flex min-w-0 flex-1 flex-col min-h-0 overflow-auto rounded-[calc(var(--el-border-radius-base)+4px)] bg-[var(--el-fill-color-blank)]"
        >
          <ElTable :data="tableRows" border size="small" class="import-result-table">
            <ElTableColumn prop="index" label="#" width="60" />
            <ElTableColumn prop="row" label="行号" width="80" />
            <ElTableColumn prop="column" label="字段" width="130" show-overflow-tooltip />
            <ElTableColumn prop="value" label="原值" min-width="160" show-overflow-tooltip />
            <ElTableColumn prop="message" label="错误原因" min-width="220" show-overflow-tooltip />
          </ElTable>
        </div>

        <!-- 右侧：按行聚合的错误列表 -->
        <div class="flex w-80 flex-col gap-3">
          <ElCard shadow="never" class="shrink-0 !rounded-[calc(var(--el-border-radius-base))]">
            <div class="mb-2 text-sm font-medium text-[var(--el-text-color-primary)]">
              按行查看错误
            </div>
            <div class="text-xs text-[var(--el-text-color-secondary)] leading-relaxed">
              列表按照 Excel 行号分组，建议从上到下逐行修正。
            </div>
          </ElCard>

          <div
            class="import-error-groups flex-1 overflow-auto rounded-[calc(var(--el-border-radius-base)+4px)] bg-[var(--el-fill-color-light)] p-4"
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

      <div
        v-else
        class="flex flex-1 items-center justify-center text-sm text-[var(--el-text-color-secondary)]"
      >
        所有数据导入成功，无错误记录
      </div>
    </div>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { ElDialog, ElCard, ElTable, ElTableColumn } from 'element-plus'

  defineOptions({ name: 'StudentImportResultDialog' })

  interface Props {
    /** 是否显示 */
    visible: boolean
    /** 导入结果数据 */
    result: Api.StudentImport.ImportResult | null
  }

  const props = defineProps<Props>()

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void
  }>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value: boolean) => emit('update:visible', value)
  })

  const result = computed(() => props.result)

  const tableRows = computed(() => {
    if (!props.result?.errors) return []
    return props.result.errors.map((item, index) => ({
      index: index + 1,
      row: item.row,
      column: item.column,
      value: item.value ?? '',
      message: item.message
    }))
  })

  const groupedByRow = computed(() => {
    if (!props.result?.errors) return []
    const map = new Map<number, Api.StudentImport.ImportError[]>()
    props.result.errors.forEach((item) => {
      const list = map.get(item.row) || []
      list.push(item)
      map.set(item.row, list)
    })
    return Array.from(map.entries())
      .sort((a, b) => a[0] - b[0])
      .map(([row, items]) => ({ row, items }))
  })
</script>

<style scoped lang="scss">
  .import-result-dialog {
    :deep(.el-dialog__body) {
      padding: 16px 20px;
      height: 75vh;
      overflow: hidden;
    }
  }

  .import-result-main {
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

  .import-result-table {
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

  .import-error-groups {
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
</style>
