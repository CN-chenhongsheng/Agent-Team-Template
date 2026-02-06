<!-- 通用导入弹窗组件 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    :title="title"
    :width="width"
    align-center
    destroy-on-close
    class="art-import-dialog"
    @close="handleClose"
  >
    <div class="flex flex-col gap-5">
      <!-- 下载模板区域 -->
      <div
        class="import-card border border-[var(--el-border-color-lighter)] bg-[var(--el-fill-color-blank)] p-4"
      >
        <div class="mb-3 flex items-center gap-2">
          <div class="import-header-icon flex items-center justify-center">
            <ElIcon class="text-base"><Download /></ElIcon>
          </div>
          <span class="text-sm font-medium text-[var(--el-text-color-primary)]">下载模板</span>
        </div>
        <div class="ml-9 flex items-center justify-between gap-3">
          <div class="flex items-center gap-3">
            <div
              class="flex h-10 w-10 items-center justify-center rounded-lg bg-[var(--el-color-primary-light-9)]"
            >
              <ElIcon class="text-xl text-[var(--el-color-primary)]"><Document /></ElIcon>
            </div>
            <div class="flex flex-col">
              <span class="text-sm font-medium text-[var(--el-text-color-primary)]">
                {{ templateName }}
              </span>
              <span class="text-xs text-[var(--el-text-color-secondary)]">
                Excel 模板文件（点击右侧图标下载）
              </span>
            </div>
          </div>
          <ElButton
            type="default"
            v-ripple
            class="flex h-9 w-9 items-center justify-center rounded-md !p-0 border border-[var(--el-border-color-lighter)] hover:border-[var(--el-color-primary)] hover:bg-[var(--el-color-primary-light-9)]"
            :loading="downloadLoading"
            @click="handleDownloadTemplate"
            :aria-label="downloadLoading ? '模板下载中' : '下载模板'"
          >
            <ElIcon v-if="!downloadLoading" class="text-lg text-[var(--el-color-primary)]">
              <Download />
            </ElIcon>
          </ElButton>
        </div>
      </div>

      <!-- 上传文件区域 -->
      <div
        class="import-card border border-[var(--el-border-color-lighter)] bg-[var(--el-fill-color-blank)] p-4"
      >
        <div class="mb-3 flex items-center justify-between gap-2">
          <div class="flex items-center gap-2">
            <div class="import-header-icon flex items-center justify-center">
              <ElIcon class="text-base"><Upload /></ElIcon>
            </div>
            <span class="text-sm font-medium text-[var(--el-text-color-primary)]">上传文件</span>
            <ElTag v-if="uploadDisabled" size="small" type="info" class="ml-2">暂未开放</ElTag>
          </div>

          <!-- 重新选择：移动到标题行右侧，仅在已选文件时显示 -->
          <ElUpload
            v-if="selectedFile"
            class="inline-block"
            :auto-upload="false"
            :accept="accept"
            :limit="1"
            :show-file-list="false"
            :disabled="uploadDisabled || scanState === 'scanning'"
            :on-change="handleFileChange"
            :on-exceed="handleExceed"
          >
            <ElButton
              v-ripple
              size="small"
              link
              :disabled="uploadDisabled || scanState === 'scanning'"
            >
              重新选择
            </ElButton>
          </ElUpload>
        </div>
        <div class="ml-9">
          <!-- 未选文件：拖拽上传区 -->
          <ElUpload
            v-if="!selectedFile"
            ref="uploadRef"
            class="w-full"
            drag
            :auto-upload="false"
            :accept="accept"
            :limit="1"
            :disabled="uploadDisabled"
            :on-change="handleFileChange"
            :on-exceed="handleExceed"
            :on-remove="handleFileRemove"
          >
            <div
              class="flex flex-col items-center py-4"
              :class="uploadDisabled ? 'cursor-not-allowed opacity-50' : 'cursor-pointer'"
            >
              <div
                class="flex h-20 w-20 items-center justify-center rounded-full transition-all duration-300"
                :class="
                  uploadDisabled
                    ? 'bg-[var(--el-fill-color-light)]'
                    : 'bg-[var(--el-color-primary-light-9)] group-hover:bg-[var(--el-color-primary-light-8)]'
                "
              >
                <ElIcon
                  :size="48"
                  class="transition-all duration-300"
                  :style="
                    uploadDisabled
                      ? { color: 'var(--el-text-color-placeholder)' }
                      : { color: 'var(--el-color-primary)' }
                  "
                >
                  <UploadFilled />
                </ElIcon>
              </div>
              <div class="text-center">
                <p v-if="uploadDisabled" class="text-sm text-[var(--el-text-color-placeholder)]">
                  上传功能暂未开放
                </p>
                <p v-else class="text-sm">
                  <span class="font-medium text-[var(--el-color-primary)]">点击上传</span>
                  <span class="text-[var(--el-text-color-regular)]"> 或将文件拖拽到此处</span>
                </p>
                <p class="mt-1 text-xs text-[var(--el-text-color-secondary)]">仅支持 XLSX 格式</p>
              </div>
            </div>
          </ElUpload>

          <!-- 已选文件：文件卡片（替代拖拽区） -->
          <div
            v-else
            class="import-file-card import-card relative bg-[var(--el-fill-color-light)] p-4"
            :class="scanState === 'scanning' ? 'is-scanning' : ''"
          >
            <div class="flex items-start justify-between gap-3">
              <div class="flex min-w-0 flex-1 items-start gap-3">
                <div
                  class="flex h-10 w-10 flex-none items-center justify-center rounded-lg bg-[var(--el-fill-color-light)]"
                >
                  <ElIcon class="text-xl text-[var(--el-text-color-regular)]"><Document /></ElIcon>
                </div>
                <div class="min-w-0">
                  <div class="truncate text-sm font-medium text-[var(--el-text-color-primary)]">
                    {{ selectedFile.name }}
                  </div>
                  <div
                    class="mt-0.5 whitespace-nowrap text-xs text-[var(--el-text-color-secondary)]"
                  >
                    {{ formatBytes(selectedFile.size) }} <span class="mx-1">·</span>
                    {{ scanStatusText }}
                  </div>
                </div>
              </div>

              <div class="flex flex-none items-center justify-end gap-2">
                <ElButton
                  v-ripple
                  :loading="scanState === 'scanning'"
                  :disabled="uploadDisabled || scanState === 'scanning'"
                  @click="handleScan"
                >
                  {{ scanButtonText }}
                </ElButton>
                <ElIcon
                  class="remove-icon"
                  :class="
                    uploadDisabled || scanState === 'scanning'
                      ? 'cursor-not-allowed opacity-40'
                      : 'cursor-pointer text-[var(--el-text-color-secondary)] hover:text-[var(--el-color-danger)]'
                  "
                  @click.stop="uploadDisabled || scanState === 'scanning' ? undefined : handleFileRemove()"
                >
                  <Close />
                </ElIcon>
              </div>
            </div>

            <!-- 扫描进度与动画 -->
            <div
              v-if="showScanProgress"
              class="mt-3 transition-opacity duration-500"
              :class="scanProgressFading ? 'opacity-0' : 'opacity-100'"
            >
              <div
                class="mb-2 flex items-center justify-between text-xs text-[var(--el-text-color-secondary)]"
              >
                <span>扫描中，请稍候...</span>
                <span>{{ scanProgress }}%</span>
              </div>
              <ElProgress :percentage="scanProgress" :stroke-width="8" :show-text="false" />
            </div>

            <div
              v-if="scanState === 'success' && showScanResultText"
              class="mt-3 text-xs text-[var(--el-color-success)]"
            >
              扫描通过，可以开始上传
            </div>

            <div
              v-if="scanState === 'failed' && showScanResultText"
              class="mt-3 flex items-center justify-between text-xs text-[var(--el-color-danger)]"
            >
              <span>扫描失败：{{ scanErrorCountText }}</span>
              <span
                v-if="lastValidationError"
                class="cursor-pointer text-[var(--el-color-primary)] hover:underline"
                @click="handleViewScanError"
              >
                查看详情
              </span>
            </div>

            <div
              v-if="scanState === 'skipped' && showScanResultText"
              class="mt-3 text-xs text-[var(--el-color-warning)]"
            >
              文件较大，已跳过前端扫描，将由服务器进行校验
            </div>
          </div>

          <!-- 分片上传进度显示 -->
          <div
            v-if="enableChunkUpload && uploadProgress"
            class="mt-4 rounded-md bg-[var(--el-fill-color-light)] p-4"
          >
            <div class="mb-2 flex items-center justify-between">
              <span class="text-sm text-[var(--el-text-color-primary)]">
                {{
                  uploadState === 'hashing'
                    ? '计算文件哈希中...'
                    : uploadState === 'uploading'
                      ? '上传中...'
                      : uploadState === 'merging'
                        ? '合并文件中...'
                        : uploadState === 'paused'
                          ? '已暂停'
                          : '准备中...'
                }}
              </span>
              <span class="text-sm text-[var(--el-text-color-secondary)]">
                {{ uploadProgress.percent }}%
              </span>
            </div>
            <ElProgress
              :percentage="uploadProgress.percent"
              :status="uploadState === 'paused' ? 'warning' : undefined"
              :stroke-width="8"
            />
            <div
              class="mt-2 flex items-center justify-between text-xs text-[var(--el-text-color-secondary)]"
            >
              <span>
                {{ formatBytes(uploadProgress.uploadedBytes) }} /
                {{ formatBytes(uploadProgress.totalBytes) }}
              </span>
              <span v-if="uploadProgress.speed > 0">
                {{ formatBytes(uploadProgress.speed) }}/s
              </span>
            </div>
            <!-- 暂停/恢复按钮 -->
            <div
              v-if="uploadState === 'uploading' || uploadState === 'paused'"
              class="mt-3 flex gap-2"
            >
              <ElButton
                v-if="uploadState === 'uploading'"
                v-ripple
                size="small"
                @click="pauseUpload"
              >
                暂停
              </ElButton>
              <ElButton v-if="uploadState === 'paused'" v-ripple size="small" @click="resumeUpload">
                恢复
              </ElButton>
              <ElButton v-ripple size="small" @click="abortUpload"> 取消 </ElButton>
            </div>
          </div>

          <!-- 哈希计算进度 -->
          <div
            v-if="enableChunkUpload && uploadState === 'hashing' && hashProgress > 0"
            class="mt-4 rounded-md bg-[var(--el-fill-color-light)] p-4"
          >
            <div class="mb-2 flex items-center justify-between">
              <span class="text-sm text-[var(--el-text-color-primary)]">计算文件哈希中...</span>
              <span class="text-sm text-[var(--el-text-color-secondary)]">{{ hashProgress }}%</span>
            </div>
            <ElProgress :percentage="hashProgress" :stroke-width="8" />
          </div>
        </div>
      </div>

      <!-- 导入须知 -->
      <div
        v-if="tips && tips.length > 0"
        class="import-card import-warning-card p-4"
      >
        <div class="mb-2 flex items-center gap-2">
          <div class="import-warning-card-icon flex items-center justify-center">
            <ElIcon class="text-base"><InfoFilled /></ElIcon>
          </div>
          <span class="import-warning-card-title text-sm font-medium">导入须知</span>
        </div>
        <ul class="ml-9 list-inside list-disc space-y-1">
          <li
            v-for="(tip, index) in tips"
            :key="index"
            class="text-xs leading-relaxed text-[var(--el-text-color-regular)]"
          >
            {{ tip }}
          </li>
        </ul>
      </div>
    </div>

    <ScanResultDialog
      v-if="scanResultMeta"
      v-model:visible="scanResultVisible"
      :meta="scanResultMeta"
    />

    <template #footer>
      <div class="w-full pb-4">
        <ElButton
          type="primary"
          class="w-full"
          :disabled="uploadDisabled || !selectedFile || (scanState !== 'success' && scanState !== 'skipped')"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          {{ submitButtonText }}
        </ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref, computed, watch, onUnmounted } from 'vue'
  import {
    ElDialog,
    ElButton,
    ElUpload,
    ElIcon,
    ElMessage,
    ElTag,
    ElProgress,
    type UploadInstance,
    type UploadFile,
    type UploadRawFile
  } from 'element-plus'
  import { Download, Upload, Document, UploadFilled, InfoFilled, Close } from '@element-plus/icons-vue'
  import { ChunkUploader, type UploadProgress, type UploadResult } from '@/utils/upload'
  import type { ImportValidationErrorMeta } from '@/utils/excel/importValidation/errors'
  import ScanResultDialog from './ScanResultDialog.vue'

  defineOptions({ name: 'ArtImportDialog' })

  interface Props {
    /** 是否显示弹窗 */
    modelValue: boolean
    /** 弹窗标题 */
    title?: string
    /** 弹窗宽度 */
    width?: string | number
    /** 模板下载地址（URL 方式） */
    templateUrl?: string
    /** 模板文件名 */
    templateName?: string
    /** 自定义下载模板函数 */
    templateDownloadFn?: () => Promise<void>
    /** 接受的文件类型 */
    accept?: string
    /** 导入提示信息数组 */
    tips?: string[]
    /** 是否禁用上传功能 */
    uploadDisabled?: boolean
    /** 是否启用分片上传（大文件） */
    enableChunkUpload?: boolean
    /** 分片上传阈值（字节），超过此大小使用分片上传，默认 10MB */
    chunkUploadThreshold?: number
    /** 跳过前端扫描的文件大小阈值（字节），超过此大小直接由后端校验，默认 50MB */
    skipScanThreshold?: number
    /** 扫描函数：点击“扫描”时触发，抛出异常则视为扫描失败（兼容旧用法） */
    scanFn?: (file: UploadRawFile) => Promise<void>
    /**
     * 支持进度回调的扫描函数（优先使用，推荐通过 Web Worker 实现）
     * - onProgress 用于上报真实进度（0-100）
     * - 返回 totalRows 用于传给后端计算进度（可选）
     */
    scanWithProgressFn?: (
      file: UploadRawFile,
      onProgress: (payload: { scannedRows: number; totalRows: number; percent: number }) => void
    ) => Promise<number | null | void>
    /**
     * 上传前校验钩子
     * - 抛出异常则阻断上传
     * - 可用于解析 Excel 并做前端格式校验
     */
    beforeUpload?: (file: UploadRawFile) => Promise<void>
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'upload', file: UploadRawFile): void
    (e: 'upload-success', result: UploadResult & { totalRows?: number | null }): void
    (e: 'upload-error', error: Error): void
    (e: 'download-template'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    title: '导入数据',
    width: '520px',
    templateName: '导入模板.xlsx',
    accept: '.xlsx',
    tips: () => [],
    uploadDisabled: false,
    enableChunkUpload: false,
    chunkUploadThreshold: 10 * 1024 * 1024, // 10MB
    skipScanThreshold: 50 * 1024 * 1024 // 50MB - 超过此大小跳过前端扫描
  })

  const emit = defineEmits<Emits>()

  // 状态
  const downloadLoading = ref(false)
  const submitLoading = ref(false)
  const selectedFile = ref<UploadRawFile | null>(null)
  const uploadRef = ref<UploadInstance>()
  const scannedTotalRows = ref<number | null>(null) // 扫描得到的总行数

  // 扫描状态：新增 'skipped' 表示大文件跳过前端扫描
  type ScanState = 'idle' | 'scanning' | 'success' | 'failed' | 'skipped'
  const scanState = ref<ScanState>('idle')
  const scanProgress = ref(0)
  const scanStartTime = ref(0)
  const showScanProgress = ref(false)
  const scanProgressFading = ref(false)
  const showScanResultText = ref(false)
  const lastValidationError = ref<any>(null)
  const scanTimer = ref<number | null>(null)
  const scanResultVisible = ref(false)

  // 分片上传相关状态
  const chunkUploader = ref<ChunkUploader | null>(null)
  const uploadProgress = ref<UploadProgress | null>(null)
  const uploadState = ref<string>('idle')
  const hashProgress = ref(0)

  // 弹窗显示控制
  const dialogVisible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const scanStatusText = computed(() => {
    if (scanState.value === 'scanning') return '扫描中'
    if (scanState.value === 'success') return '已扫描 · 通过'
    if (scanState.value === 'failed') return '已扫描 · 未通过'
    if (scanState.value === 'skipped') return '已跳过 · 大文件'
    return '未扫描'
  })

  const scanButtonText = computed(() => {
    if (scanState.value === 'scanning') return '扫描中...'
    if (scanState.value === 'success') return '重新扫描'
    if (scanState.value === 'failed') return '重新扫描'
    if (scanState.value === 'skipped') return '已跳过'
    return '扫描'
  })

  const scanErrorCountText = computed(() => {
    const meta = (lastValidationError.value as any)?.meta as ImportValidationErrorMeta | undefined
    if (!meta) return '请查看详情'
    return `共 ${meta.errorCount} 处问题（仅展示前 ${meta.preview?.length || 0} 条）`
  })

  const scanResultMeta = computed<ImportValidationErrorMeta | null>(() => {
    const err = lastValidationError.value as any
    if (!err) return null
    if (err.meta) return err.meta as ImportValidationErrorMeta
    if (typeof err.message === 'string' && err.message.trim()) {
      return {
        errorCount: 1,
        truncated: false,
        preview: [
          {
            row: 0,
            column: '系统提示',
            message: err.message,
            value: ''
          }
        ]
      }
    }
    return null
  })

  const submitButtonText = computed(() => {
    if (submitLoading.value) return '上传中...'
    if (!selectedFile.value) return '请选择文件'
    if (scanState.value === 'scanning') return '扫描中...'
    if (scanState.value === 'skipped') return '开始上传（跳过扫描）'
    if (scanState.value !== 'success') return '请先扫描'
    return '开始上传'
  })

  /**
   * 格式化字节数
   */
  const formatBytes = (bytes: number): string => {
    // 处理无效值：undefined、NaN、负数、0
    if (!bytes || bytes <= 0 || !isFinite(bytes)) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    // 确保索引在有效范围内，避免 sizes[i] 返回 undefined
    const safeIndex = Math.max(0, Math.min(i, sizes.length - 1))
    return parseFloat((bytes / Math.pow(k, safeIndex)).toFixed(2)) + ' ' + sizes[safeIndex]
  }

  /**
   * 下载模板
   */
  const handleDownloadTemplate = async () => {
    downloadLoading.value = true

    try {
      // 优先使用自定义下载函数
      if (props.templateDownloadFn) {
        await props.templateDownloadFn()
      } else if (props.templateUrl) {
        // 使用 URL 方式下载
        const link = document.createElement('a')
        link.href = props.templateUrl
        link.download = props.templateName || '导入模板.xlsx'
        link.style.display = 'none'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
      } else {
        ElMessage.warning('未配置模板下载地址')
        return
      }

      emit('download-template')
      ElMessage.success('模板下载成功')
    } catch (error) {
      console.error('下载模板失败:', error)
      ElMessage.error('模板下载失败，请稍后重试')
    } finally {
      downloadLoading.value = false
    }
  }

  /**
   * 文件变化处理
   */
  const handleFileChange = (uploadFile: UploadFile) => {
    if (uploadFile.raw) {
      const name = uploadFile.raw.name || ''
      if (!name.toLowerCase().endsWith('.xlsx')) {
        ElMessage.error('仅支持上传 .xlsx 文件')
        uploadRef.value?.clearFiles()
        selectedFile.value = null
        return
      }
      selectedFile.value = uploadFile.raw
      // 重置扫描状态
      scanState.value = 'idle'
      scanProgress.value = 0
      showScanProgress.value = false
      scanProgressFading.value = false
      showScanResultText.value = false
      lastValidationError.value = null
    }
  }

  /**
   * 文件移除处理
   */
  const handleFileRemove = () => {
    selectedFile.value = null
    scanState.value = 'idle'
    scanProgress.value = 0
    showScanProgress.value = false
    scanProgressFading.value = false
    showScanResultText.value = false
    lastValidationError.value = null
    stopScanProgress()
  }

  /**
   * 超出文件数量限制
   */
  const handleExceed = () => {
    ElMessage.warning('只能上传一个文件，请先移除已选文件')
  }

  const stopScanProgress = () => {
    if (scanTimer.value !== null) {
      window.clearInterval(scanTimer.value)
      scanTimer.value = null
    }
  }

  const startFakeScanProgress = () => {
    stopScanProgress()
    // 起步给一点进度，避免一直显示 0%，1~20 的随机数
    scanProgress.value = Math.floor(Math.random() * 20) + 1
    showScanProgress.value = true
    scanProgressFading.value = false

    scanTimer.value = window.setInterval(() => {
      // 预留 100% 给真实完成，只在 1-99 之间做缓慢增长
      if (scanProgress.value >= 99) return

      const current = scanProgress.value
      let step = 1

      if (current < 60) {
        // 前段：增长较快
        step = Math.floor(Math.random() * 6) + 4 // 4~9
      } else if (current < 85) {
        // 中段：中速
        step = Math.floor(Math.random() * 4) + 2 // 2~5
      } else {
        // 尾段：慢速
        step = Math.floor(Math.random() * 2) + 1 // 1~2
      }

      scanProgress.value = Math.min(99, current + step)
    }, 140)
  }

  const handleViewScanError = async () => {
    if (!scanResultMeta.value) {
      ElMessage.warning('暂无可查看的错误详情')
      return
    }
    scanResultVisible.value = true
  }

  const handleScan = async () => {
    if (!selectedFile.value) {
      ElMessage.warning('请先选择要扫描的文件')
      return
    }
    if (props.uploadDisabled) return
    if (scanState.value === 'scanning') return

    // 大文件检测：超过阈值自动跳过前端扫描
    if (selectedFile.value.size > props.skipScanThreshold!) {
      const sizeMB = (selectedFile.value.size / 1024 / 1024).toFixed(1)
      const thresholdMB = (props.skipScanThreshold! / 1024 / 1024).toFixed(0)
      scanState.value = 'skipped'
      showScanResultText.value = true
      ElMessage.warning({
        message: `文件较大（${sizeMB}MB），已跳过前端扫描。超过 ${thresholdMB}MB 的文件将由服务器进行校验。`,
        duration: 5000
      })
      return
    }

    scanState.value = 'scanning'
    scanStartTime.value = Date.now()
    lastValidationError.value = null
    showScanResultText.value = false

    // 如果提供了带进度回调的扫描函数，优先使用真实进度
    const useProgressScan = !!props.scanWithProgressFn
    if (useProgressScan) {
      // 真实进度模式不需要假进度条
      showScanProgress.value = true
      scanProgress.value = 0
      scanProgressFading.value = false
      stopScanProgress()
    } else {
      // 兼容旧用法，使用假进度条
      startFakeScanProgress()
    }

    // 关键：让出主线程，确保进度条 UI 渲染后再开始扫描
    // 使用 nextTick + requestAnimationFrame 双重保证
    await nextTick()
    await new Promise((resolve) => requestAnimationFrame(() => setTimeout(resolve, 0)))

    try {
      if (useProgressScan && props.scanWithProgressFn) {
        // 真实进度：由外部（通常是 Web Worker 客户端）上报
        const totalRows = await props.scanWithProgressFn(selectedFile.value, ({ percent }) => {
          // 预留 100% 给 finally 收尾，只在 0-99 之间更新
          const safePercent = Math.max(0, Math.min(99, percent))
          // 进度只能前进不后退
          scanProgress.value = Math.max(scanProgress.value, safePercent)
          showScanProgress.value = true
        })
        // 保存扫描得到的总行数
        scannedTotalRows.value = totalRows || null
      } else {
        // 兼容旧用法：优先使用 scanFn，其次是 beforeUpload
        if (props.scanFn) {
          await props.scanFn(selectedFile.value)
        } else if (props.beforeUpload) {
          // 兼容旧用法：把 beforeUpload 当作扫描逻辑
          await props.beforeUpload(selectedFile.value)
        } else {
          ElMessage.warning('未配置扫描函数')
          scanState.value = 'failed'
          return
        }
      }

      scanState.value = 'success'
      ElMessage.success('扫描通过')
    } catch (error) {
      scanState.value = 'failed'
      lastValidationError.value = error
      ElMessage.error('扫描未通过，请点击“查看详情”查看错误列表')
    } finally {
      // 确保进度条至少展示一小段时间，再收尾到 100%，然后缓慢淡出
      const elapsed = Date.now() - scanStartTime.value
      const minDuration = 600
      if (elapsed < minDuration) {
        await new Promise((resolve) => setTimeout(resolve, minDuration - elapsed))
      }
      scanProgress.value = 100
      // 开始淡出动画
      scanProgressFading.value = true
      await new Promise((resolve) => setTimeout(resolve, 500))
      showScanProgress.value = false
      scanProgressFading.value = false
      showScanResultText.value = true
      stopScanProgress()
      if (scanState.value === 'scanning') {
        scanState.value = 'failed'
      }
    }
  }

  /**
   * 判断是否需要使用分片上传
   */
  const shouldUseChunkUpload = computed(() => {
    if (!props.enableChunkUpload || !selectedFile.value) {
      return false
    }
    return selectedFile.value.size > props.chunkUploadThreshold
  })

  /**
   * 初始化分片上传器
   */
  const initChunkUploader = () => {
    if (chunkUploader.value) {
      return chunkUploader.value
    }

    chunkUploader.value = new ChunkUploader({
      onProgress: (progress: UploadProgress) => {
        uploadProgress.value = progress
      },
      onStateChange: (state: string) => {
        uploadState.value = state
      },
      onHashProgress: (percent: number) => {
        hashProgress.value = percent
      }
    })

    return chunkUploader.value
  }

  /**
   * 提交导入
   */
  const handleSubmit = async () => {
    if (!selectedFile.value) {
      ElMessage.warning('请先选择要导入的文件')
      return
    }
    // 允许 success（扫描通过）或 skipped（大文件跳过扫描）状态上传
    if (scanState.value !== 'success' && scanState.value !== 'skipped') {
      ElMessage.warning('请先扫描并通过校验后再上传')
      return
    }

    submitLoading.value = true

    try {
      // 判断是否使用分片上传
      if (shouldUseChunkUpload.value) {
        // 使用分片上传
        const uploader = initChunkUploader()
        const result = await uploader.upload(selectedFile.value)
        emit('upload-success', { ...result, totalRows: scannedTotalRows.value })
        // 不在这里显示成功消息，由父组件的 handleUploadSuccess 统一处理导入结果消息
        dialogVisible.value = false
      } else {
        // 使用普通上传方式，由父组件处理
        emit('upload', selectedFile.value)
      }
    } catch (error) {
      // 2) 上传失败
      console.error('导入失败:', error)
      emit('upload-error', error instanceof Error ? error : new Error(String(error)))
      ElMessage.error('导入失败，请稍后重试')
    } finally {
      submitLoading.value = false
      uploadProgress.value = null
      hashProgress.value = 0
    }
  }

  /**
   * 暂停上传
   */
  const pauseUpload = () => {
    chunkUploader.value?.pause()
  }

  /**
   * 恢复上传
   */
  const resumeUpload = async () => {
    await chunkUploader.value?.resume()
  }

  /**
   * 取消上传
   */
  const abortUpload = async () => {
    await chunkUploader.value?.abort()
    uploadProgress.value = null
    hashProgress.value = 0
    uploadState.value = 'idle'
  }

  // 监听弹窗关闭，清理上传器
  watch(
    () => props.modelValue,
    (visible) => {
      if (!visible && chunkUploader.value) {
        // 弹窗关闭时，如果正在上传，取消上传
        if (['uploading', 'hashing', 'merging'].includes(uploadState.value)) {
          abortUpload()
        }
      }
    }
  )

  // 组件卸载时清理
  onUnmounted(() => {
    if (chunkUploader.value) {
      abortUpload()
    }
  })

  /**
   * 关闭弹窗
   */
  const handleClose = () => {
    // 重置状态
    selectedFile.value = null
    uploadRef.value?.clearFiles()
    scanState.value = 'idle'
    scanProgress.value = 0
    lastValidationError.value = null
    stopScanProgress()
  }

  // 暴露方法供父组件调用
  defineExpose({
    /** 清空已选文件 */
    clearFiles: () => {
      selectedFile.value = null
      uploadRef.value?.clearFiles()
      scanState.value = 'idle'
      scanProgress.value = 0
      lastValidationError.value = null
      stopScanProgress()
    },
    /** 关闭弹窗 */
    close: () => {
      dialogVisible.value = false
    },
    /** 设置提交状态 */
    setSubmitLoading: (loading: boolean) => {
      submitLoading.value = loading
    },
    /** 暂停上传 */
    pauseUpload,
    /** 恢复上传 */
    resumeUpload,
    /** 取消上传 */
    abortUpload,
    /** 获取上传进度 */
    getUploadProgress: () => uploadProgress.value,
    /** 获取上传状态 */
    getUploadState: () => uploadState.value
  })
</script>

<style lang="scss">
  .art-import-dialog {
    .import-file-card {
      position: relative;
      overflow: hidden;

      &.is-scanning::before {
        content: '';
        position: absolute;
        inset: 0;
        background: linear-gradient(
          110deg,
          transparent 0%,
          rgba(255, 255, 255, 0.35) 45%,
          transparent 70%
        );
        transform: translateX(-100%);
        animation: importScanShimmer 1.2s ease-in-out infinite;
        pointer-events: none;
      }
    }

    @keyframes importScanShimmer {
      0% {
        transform: translateX(-100%);
      }
      100% {
        transform: translateX(100%);
      }
    }

    // 卡片圆角 - 使用系统动态圆角变量
    .import-card {
      border-radius: calc(var(--custom-radius) / 2 + 2px);
    }

    .import-header-icon {
      width: 28px;
      height: 28px;
      border-radius: 8px;
      background-color: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
    }

    .import-warning-card {
      border: 1px solid var(--el-color-warning-light-5);
      background-color: var(--el-color-warning-light-9);
    }

    .import-warning-card-icon {
      width: 28px;
      height: 28px;
      border-radius: 8px;
      background-color: var(--el-color-warning-light-7);
      color: var(--el-color-warning);
    }

    .import-warning-card-title {
      color: var(--el-color-warning-dark-2);
    }

    .el-upload {
      width: 100%;
    }

    .el-upload-dragger {
      width: 100%;
      padding: 0;
      border-radius: var(--el-border-radius-base);
      border-color: var(--el-border-color-light);
      transition: all 0.3s;

      &:hover:not(.is-disabled) {
        border-color: var(--el-color-primary);
        background-color: var(--el-color-primary-light-9);
      }

      &.is-dragover {
        border-color: var(--el-color-primary);
        background-color: var(--el-color-primary-light-9);
      }

      &.is-disabled {
        cursor: not-allowed;
        background-color: var(--el-fill-color-light);
      }
    }

    .el-upload-list {
      margin-top: 12px;

      .el-upload-list__item {
        border-radius: var(--el-border-radius-small);
        transition: all 0.3s;

        &:hover {
          background-color: var(--el-fill-color-light);
        }
      }
    }
  }
</style>
