# 通用导入框架使用指南

## 概述

`useGenericImport` 是一个配置驱动的通用导入 Hook，用于快速实现 Excel 导入功能。

## 核心特性

- 配置驱动：通过配置对象快速接入，无需重复编写导入逻辑
- 模板生成：自动生成带级联下拉的 Excel 模板
- 前端校验：支持 Worker 异步校验，不阻塞主线程
- 分片上传：大文件自动分片上传（可配置阈值）
- 进度监听：SSE + 轮询双模式，实时显示导入进度
- 类型安全：完整的 TypeScript 类型定义

## 快速开始

### 1. 创建导入配置

```typescript
// config/your-import-config.ts
import type { GenericImportConfig } from '@/composables/useGenericImport'
import { fetchImportData, subscribeProgress, pollProgress } from '@/api/your-module'

export const yourImportConfig: GenericImportConfig = {
  // 模板配置
  templateColumns: [
    { key: 'name', title: '姓名', required: true, width: 15 },
    { key: 'phone', title: '手机号', width: 12 },
    // ... 更多字段
  ],
  templateFilename: '数据导入模板',
  templateSheetName: '数据',

  // API 配置
  fetchContextData: async () => {
    // 获取级联下拉所需的数据
    return await fetchContextTrees()
  },
  uploadApi: (fileUrl, totalRows) => {
    return fetchImportData(fileUrl, totalRows)
  },
  subscribeApi: subscribeProgress,
  pollApi: pollProgress,

  // 可选配置
  enableChunkUpload: true,
  chunkSizeThreshold: 5 * 1024 * 1024, // 5MB
  progressTitle: '数据导入'
}
```

### 2. 在页面中使用

```vue
<script setup lang="ts">
import { useGenericImport } from '@/composables/useGenericImport'
import { yourImportConfig } from './config/your-import-config'
import ArtImportDialog from '@/components/core/forms/art-import-dialog/index.vue'

const {
  dialogVisible,
  handleDownloadTemplate,
  handleScanFile,
  handleUploadSuccess
} = useGenericImport({
  ...yourImportConfig,
  onImportComplete: (result, status) => {
    // 导入完成后刷新列表
    refreshData()
  },
  onViewDetail: (result) => {
    // 显示导入结果详情
    showResultDialog(result)
  }
})

const importTips = [
  '请先下载模板，按模板格式填写数据',
  '必填字段不能为空',
  '请选择 XLSX 文件'
]
</script>

<template>
  <!-- 导入按钮 -->
  <ElButton @click="() => { dialogVisible = true }">导入数据</ElButton>

  <!-- 导入对话框 -->
  <ArtImportDialog
    v-model="dialogVisible"
    title="导入数据"
    :template-download-fn="handleDownloadTemplate"
    :scan-with-progress-fn="handleScanFile"
    :tips="importTips"
    :enable-chunk-upload="true"
    @upload-success="handleUploadSuccess"
  />
</template>
```

## 配置选项详解

### GenericImportConfig

| 选项 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `templateColumns` | `TemplateColumn[]` | 是 | 模板列配置 |
| `templateFilename` | `string` | 是 | 模板文件名 |
| `templateSheetName` | `string` | 否 | 工作表名称（默认：'导入数据'） |
| `fetchContextData` | `() => Promise<any>` | 是 | 获取上下文数据（级联下拉、字典等） |
| `uploadApi` | `(fileUrl, totalRows?) => Promise<Response>` | 是 | 上传导入 API |
| `subscribeApi` | `(taskId, callbacks) => () => void` | 否 | SSE 订阅 API（异步导入必须） |
| `pollApi` | `(taskId) => Promise<TaskVO>` | 否 | 轮询 API（SSE 降级使用） |
| `enableChunkUpload` | `boolean` | 否 | 是否启用分片上传（默认：true） |
| `chunkSizeThreshold` | `number` | 否 | 分片上传阈值（默认：5MB） |
| `skipScanThreshold` | `number` | 否 | 跳过前端扫描的文件大小阈值（默认：50MB） |
| `onImportComplete` | `(result, status) => void` | 否 | 导入完成回调 |
| `onViewDetail` | `(result) => void` | 否 | 查看详情回调 |
| `progressTitle` | `string` | 否 | 进度通知标题 |

## 最佳实践

### 1. 模板字段配置

使用 `COMMON_FIELDS` 中的通用字段：

```typescript
import { COMMON_FIELDS } from '@/utils/excel'

export const templateColumns = [
  { key: 'name', title: '姓名', required: true, width: 15 },
  COMMON_FIELDS.gender,
  COMMON_FIELDS.phone,
  COMMON_FIELDS.idCard,
  COMMON_FIELDS.email
]
```

### 2. 级联下拉配置

```typescript
export const templateColumns = [
  {
    key: 'campusName',
    title: '校区',
    required: true,
    cascadeType: 'campus' // 第一级
  },
  {
    key: 'deptName',
    title: '院系',
    required: true,
    cascadeType: 'department' // 第二级，依赖校区
  }
]
```

### 3. 字段校验

使用通用校验器：

```typescript
import { validatePhone, validateEmail, validateIdCard } from '@/utils/excel'

// 在 Worker 中使用
const phoneError = validatePhone(value)
if (phoneError) {
  errors.push({ row, column: 'phone', message: phoneError, value })
}
```

### 4. 自定义校验逻辑

如果需要复杂的业务校验，创建专门的 Worker：

```typescript
export const yourImportConfig: GenericImportConfig = {
  // ...
  workerPath: '/src/workers/your-import.worker.ts',
  buildContext: (contextData) => {
    // 将上下文数据转换为 Worker 需要的格式
    return {
      dictSets: contextData.dictSets,
      orgTree: contextData.orgTree
    }
  }
}
```

## 示例参考

完整示例请参考：

- **学生导入**：`src/views/student/manage/`
  - 配置文件：`config/student-import-config.ts`
  - 页面组件：`index.vue`
  - Worker：`src/workers/student-import.worker.ts`

## 常见问题

### Q: 如何自定义扫描逻辑？

A: 可以提供自己的 `scanFn` 或 `scanWithProgressFn`：

```typescript
const { handleScanFile } = useGenericImport({
  ...config,
  // 使用自定义扫描函数
})

// 在组件中使用自己的扫描逻辑
const customScanFile = async (file, onProgress) => {
  // 自定义扫描逻辑
}
```

### Q: 如何处理同步导入？

A: 如果后端直接返回导入结果，不需要配置 `subscribeApi` 和 `pollApi`：

```typescript
uploadApi: async (fileUrl) => {
  // 直接返回导入结果
  return await importDataSync(fileUrl)
}
```

### Q: 如何自定义进度通知？

A: 使用 `onImportComplete` 和 `onViewDetail` 回调：

```typescript
const { ... } = useGenericImport({
  ...config,
  onImportComplete: (result, status) => {
    if (status === 'success') {
      ElMessage.success(`成功导入 ${result.successCount} 条`)
    }
    refreshData()
  }
})
```

## 迁移指南

如果你有旧的导入实现，可以按以下步骤迁移：

1. 创建配置文件（抽取模板字段、API 等）
2. 使用 `useGenericImport` 替换原有的导入逻辑
3. 更新模板中的事件处理器
4. 移除冗余代码（手动的进度监听、状态管理等）

迁移后代码量通常可减少 60-80%。
