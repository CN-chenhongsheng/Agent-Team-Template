---
name: manager-frontend
description: Enforces Vue 3 frontend coding standards for the manager admin dashboard. Use when working with Vue files in manager/, creating components, pages, forms, tables, API files, or any frontend code. Covers component usage priorities (ArtTable, ArtForm, ArtSwitch), hooks (useTable), API writing and calling standards, naming conventions, form configurations, and table specifications.
---

# Manager Frontend Coding Standards

## Component Usage Priority

**Must prioritize project-encapsulated components, never use Element Plus directly:**

### Tables
- ✅ **Must use** `ArtTable` instead of `ElTable`
- ✅ **Must use** `ArtTableHeader` for table header operations
- ✅ **Must use** `useTable` hook for table data, pagination, loading
- ✅ **Must use** `useTableColumns` hook for column configuration
- ✅ **Must enable** context menu (`contextMenu: { enabled: true }`)
- ✅ **Date/time columns must have** `sortable: true`
- ✅ **Status columns must use** `ArtSwitch` component
- ✅ **Action columns must return** `ActionButtonConfig[]` array
- ❌ **Forbidden**: `ElTable`, `ElTableColumn`, `ElPagination`

### Forms
- ✅ **Must use** `ArtForm` instead of `ElForm` (config-based)
- ✅ **Must use** `ArtSearchBar` for search forms
- ✅ **Must use** `ArtSwitch` instead of `ElSwitch`
- ✅ **Dialog forms must use** `ArtForm` with `formItems` configuration
- ✅ **Submit button loading**: variable `submitLoading`, text `{{ submitLoading ? '提交中...' : '确定' }}`
- ❌ **Forbidden**: `ElForm`, `ElFormItem` (unless inside `ArtForm`)

### Layout
- ✅ **Must use** `ArtDrawer` instead of `ElDrawer`
- ✅ **Must use** `ArtBasicInfo` for basic info
- ✅ **Must use** `ArtApprovalInfo` for approval info
- ✅ **Must use** `ArtPageContent` as page container

### Component Visibility Control
- ✅ **Use `v-show` instead of `v-if`** when component state needs to be preserved (e.g., switching between view/edit modes)
- ✅ **Create separate `visible` states** for different components (e.g., `drawerVisible` for drawer, `dialogVisible` for dialog)
- ✅ **Ensure mutual exclusivity** - when opening one component, explicitly close others
- ❌ **Forbidden**: Using the same `visible` state for multiple components that can be shown simultaneously

## Component Check Flow

Before coding, check in order:

1. Check `src/components/core/` for encapsulated components
2. Check `src/hooks/` for available hooks
3. Review similar implementations in `src/views/`
4. Only use Element Plus if no encapsulated component exists

## Dialog Form Standard (ArtForm Configuration)

**All dialog forms must use `ArtForm` with configuration:**

```vue
<template>
  <ElDialog v-model="visible" title="Edit User" width="600px">
    <ArtForm
      ref="formRef"
      v-model="formData"
      :formItems="formItems"
      labelWidth="100px"
      :showSubmit="false"
      :showReset="false"
    />
    <template #footer>
      <ElButton @click="visible = false">Cancel</ElButton>
      <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">
        {{ submitLoading ? '提交中...' : '确定' }}
      </ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
const submitLoading = ref(false)
const formData = ref({ name: '', age: undefined, status: 1 })

const formItems = computed<FormItemProps[]>(() => [
  {
    prop: 'name',
    label: 'Name',
    type: 'input',
    placeholder: 'Enter name',
    rules: [{ required: true, message: 'Enter name', trigger: 'blur' }]
  }
])
</script>
```

## Search Form Standard

**Must use `ref` directly, never `reactive`:**

```typescript
// ✅ Correct: Use ref directly with type
const searchForm = ref<Api.Module.SearchParams>({
  pageNum: 1,
  field1: undefined,
  field2: undefined
})

// ❌ Wrong: Using reactive or initialSearchState
const formFilters = reactive({ ... })  // Forbidden!
```

## Table Configuration

### useTable Hook

```typescript
const {
  columns,
  data,
  loading,
  pagination,
  getData,
  refreshData,
  contextMenuItems,
  contextMenuWidth,
  handleRowContextmenu,
  handleContextMenuSelect
} = useTable<typeof fetchGetCheckInPage>({
  core: {
    apiFn: fetchGetCheckInPage,
    apiParams: computed(() => ({
      pageNum: searchForm.value.pageNum,
      studentNo: searchForm.value.studentNo || undefined,
      studentName: searchForm.value.studentName || undefined
    })),
    paginationKey: { current: 'pageNum', size: 'pageSize' },
    columnsFactory: () => [...]
  },
  contextMenu: { enabled: true }
})
```

### apiParams Standard

**Must explicitly list fields, never use spread syntax:**

```typescript
// ✅ Correct: Explicit field listing
apiParams: computed(() => ({
  pageNum: searchForm.value.pageNum,
  studentNo: searchForm.value.studentNo || undefined,
  studentName: searchForm.value.studentName || undefined
}))

// ❌ Wrong: Spread syntax
apiParams: computed(() => ({
  ...searchForm.value  // Forbidden!
}))
```

### Column Width Standards

**Fixed width columns (use `width`):**
- Selection: `50`
- Index: `60`
- ID: `80`
- Student No: `120`
- Gender: `70`
- Phone: `125`
- Status: `100`
- Date: `120`
- DateTime: `180`
- Action (1 button): `120`
- Action (2 buttons): `150`
- Action (3+ buttons): `180`

**Variable width columns (use `minWidth`):**
- Name: `100`
- Organization names: `100-120`
- Description: `200`
- Address: `200`

### Date/Time Columns

```typescript
// ✅ Must have sortable: true and width
{ prop: 'applyDate', label: 'Apply Date', width: 120, sortable: true }
{ prop: 'createTime', label: 'Create Time', width: 180, sortable: true }
```

### Status Columns

```typescript
// ✅ Must use ArtSwitch
{
  prop: 'status',
  label: 'Status',
  width: 100,
  render: (row) => h(ArtSwitch, {
    modelValue: row.status === 1,
    inlinePrompt: true,
    onChange: async (value) => {
      await handleStatusChange(row, value ? 1 : 0)
    }
  })
}
```

### Action Column

**CRITICAL: Action column must return button config array directly in formatter, never use separate `getRowActions` function:**

```typescript
// ✅ Correct: Inline action configuration in formatter
{
  prop: 'action',
  label: '操作',
  width: 120,
  fixed: 'right',
  formatter: (row: SurveyListItem) => {
    const actions = []

    if (row.fillStatus === 'filled') {
      actions.push({
        type: 'view',
        label: '查看详情',
        onClick: () => handleViewDetail(row),
        auth: 'allocation:survey:detail'
      })
    }

    if (row.fillStatus === 'unfilled') {
      actions.push({
        type: 'notify',
        label: '发送提醒',
        onClick: () => handleSendReminder(row),
        auth: 'allocation:survey:statistics'
      })
    }

    return actions
  }
}

// ❌ Wrong: Using separate getRowActions function
const getRowActions = (row: SurveyListItem) => { ... }
formatter: (row) => getRowActions(row)  // Forbidden!

// ❌ Wrong: Using actionsGetter in contextMenu config
contextMenu: {
  enabled: true,
  actionsGetter: getRowActions  // Forbidden!
}
```

**Handler Functions Position:**
- ✅ **Must define handler functions BEFORE `useTable` call**
- ✅ **Use direct function declarations** (not `let` with type annotation)

```typescript
// ✅ Correct: Define handlers before useTable
const handleViewDetail = (row: SurveyListItem) => {
  currentStudentId.value = row.studentId
  currentStudentName.value = row.studentName
  drawerVisible.value = true
}

const handleSendReminder = async (row: SurveyListItem) => {
  try {
    await ElMessageBox.confirm(
      `确定要向 ${row.studentName} 发送问卷填写提醒吗？`,
      '发送提醒',
      { type: 'info' }
    )
    ElMessage.success('提醒发送成功')
  } catch {
    // 取消操作
  }
}

const { columns, data, loading, ... } = useTable({
  // useTable configuration
})

// ❌ Wrong: Defining handlers after useTable
// ❌ Wrong: Using let with type annotation
let handleViewDetail: (row: SurveyListItem) => void
handleViewDetail = (row: SurveyListItem) => { ... }  // Forbidden!
```

**Button Type Requirements:**
- ✅ **Must use button types from database dictionary** (`sys_dict_data` where `dict_code='table_button_config'`)
- ✅ **Common types**: `add`, `edit`, `delete`, `view`, `more`, `share`, `reset`, `link`, `copy`, `notify`
- ✅ **All button configs must include `label` property**

```typescript
// ✅ Correct: Using dictionary-defined type with label
{ type: 'notify', label: '发送提醒', onClick: () => handleNotify(row) }

// ❌ Wrong: Using custom type not in dictionary
{ type: 'custom-action', onClick: () => {} }  // Must add to dictionary first!
```

## Naming Conventions

### Event Handlers (Must be consistent)

| Function | Standard Name |
|----------|---------------|
| Search | `handleSearch` |
| Reset | `handleReset` |
| Refresh | `handleRefresh` |
| Add | `handleAdd` |
| Edit | `handleEdit` |
| Delete | `handleDelete` |
| View | `handleView` |
| Status Change | `handleStatusChange` |

### Variables (Must be consistent)

| Variable | Standard Name |
|----------|---------------|
| Search form | `formFilters` |
| Table data | `data` |
| Loading | `loading` |
| Pagination | `pagination` |
| Columns | `columns` |
| Submit loading | `submitLoading` |

### Files & Components

- **Files**: kebab-case (`check-in-drawer.vue`)
- **Components**: PascalCase (`CheckInDrawer`)
- **Functions/Variables**: camelCase (`handleSearch`, `searchForm`)
- **Constants**: UPPER_SNAKE_CASE (`API_BASE_URL`)

## TypeScript Standards

- ✅ **Must use** strict types, no `any`
- ✅ **Must use** `Api.` namespace for API types
- ✅ **Must use** `defineOptions({ name: 'ComponentName' })` for component name

## Code Style

- **Quotes**: Single quotes (`'string'`)
- **Semicolons**: None (ESLint config)
- **Indentation**: 2 spaces
- **Line length**: 100 characters (Prettier)

## API Writing Standards

### File Structure

**All API files must follow this structure:**

```typescript
/**
 * [Module Name] API
 * [Brief description of what this module contains]
 *
 * @module api/[module-name]
 * @author 陈鸿昇
 * @date YYYY-MM-DD
 */

import request from '@/utils/http'

/** ==================== [Sub-module Name] ==================== */

/**
 * [Function description]
 * @param params Query parameters
 */
export function fetchGetXxxPage(params: Api.ModuleName.XxxSearchParams) {
  return request.get<Api.ModuleName.XxxPageResponse>({
    url: '/api/v1/[module]/[resource]/page',
    params
  })
}
```

### Function Naming Convention

**Must use `fetch` prefix + verb + noun:**

| Pattern | Example | Purpose |
|---------|---------|---------|
| `fetchGet` + `Xxx` + `Page` | `fetchGetUserPage` | Get paginated list (MUST use `Page` suffix) |
| `fetchGetAll` + `Xxx` | `fetchGetAllFlows` | Get full list (no pagination) |
| `fetchGet` + `Xxx` + `Detail` | `fetchGetUserDetail` | Get single item |
| `fetchAdd` + `Xxx` | `fetchAddUser` | Create new item |
| `fetchUpdate` + `Xxx` | `fetchUpdateUser` | Update existing item |
| `fetchDelete` + `Xxx` | `fetchDeleteUser` | Delete single item |
| `fetchBatchDelete` + `Xxx` | `fetchBatchDeleteUser` | Delete multiple items |
| `fetchUpdate` + `Xxx` + `Status` | `fetchUpdateUserStatus` | Update status |

### Request Methods

```typescript
// ✅ GET - Query parameters
export function fetchGetUserList(params: Api.SystemManage.UserSearchParams) {
  return request.get<Api.SystemManage.UserPageResponse>({
    url: '/api/v1/system/user/list',
    params
  })
}

// ✅ GET - Path parameter
export function fetchGetUserDetail(id: number) {
  return request.get<Api.SystemManage.UserListItem>({
    url: `/api/v1/system/user/${id}`
  })
}

// ✅ POST - Create
export function fetchAddUser(data: Api.SystemManage.UserSaveParams) {
  return request.post({
    url: '/api/v1/system/user',
    data,
    showSuccessMessage: true
  })
}

// ✅ PUT - Update
export function fetchUpdateUser(id: number, data: Api.SystemManage.UserSaveParams) {
  return request.put({
    url: `/api/v1/system/user/${id}`,
    data,
    showSuccessMessage: true
  })
}

// ✅ DELETE - Delete
export function fetchDeleteUser(id: number) {
  return request.del({
    url: `/api/v1/system/user/${id}`,
    showSuccessMessage: true
  })
}

// ✅ DELETE - Batch delete (with data)
export function fetchBatchDeleteUser(ids: number[]) {
  return request.del({
    url: '/api/v1/system/user/batch',
    data: ids,
    showSuccessMessage: true
  })
}
```

### Type Definition Location

**All API types MUST be defined in `src/types/api/` under the `Api.` namespace, NEVER inline in API files:**

```typescript
// ✅ Correct: Types defined in src/types/api/approval/index.d.ts
declare namespace Api {
  namespace ApprovalManage {
    interface ApprovalFlow { ... }
  }
}

// ✅ Correct: API file uses namespace type directly
export function fetchGetFlowList(params: Api.ApprovalManage.ApprovalFlowQueryParams) { ... }

// ✅ Correct: If consumers need named exports, use type alias (not duplication)
export type ApprovalFlow = Api.ApprovalManage.ApprovalFlow

// ❌ Forbidden: Inline type definitions in API files
export interface ApprovalFlow { ... }  // Must be in types/api/!
```

### Type Naming Convention

**Must use `Api.` namespace with consistent naming:**

| Type Pattern | Example | Purpose |
|--------------|---------|---------|
| `Api.ModuleName.XxxSearchParams` | `Api.SystemManage.UserSearchParams` | Query/search parameters |
| `Api.ModuleName.XxxPageResponse` | `Api.SystemManage.UserPageResponse` | Paginated response |
| `Api.ModuleName.XxxListItem` | `Api.SystemManage.UserListItem` | List item type |
| `Api.ModuleName.XxxSaveParams` | `Api.SystemManage.UserSaveParams` | Create/update parameters |
| `Api.ModuleName.XxxDetail` | `Api.SystemManage.UserDetail` | Detail type |

### Success Message Configuration

**For POST/PUT/DELETE operations, use `showSuccessMessage: true`:**

```typescript
// ✅ Correct: Show success message
export function fetchAddUser(data: Api.SystemManage.UserSaveParams) {
  return request.post({
    url: '/api/v1/system/user',
    data,
    showSuccessMessage: true  // Auto-show success message
  })
}

// ✅ Correct: Custom success message
export function fetchAddUser(data: Api.SystemManage.UserSaveParams) {
  return request.post({
    url: '/api/v1/system/user',
    data,
    showSuccessMessage: true,
    successMessage: '用户创建成功'  // Custom message
  })
}

// ✅ Correct: No success message (for GET requests)
export function fetchGetUserList(params: Api.SystemManage.UserSearchParams) {
  return request.get({
    url: '/api/v1/system/user/list',
    params
    // No showSuccessMessage needed for GET
  })
}
```

### Module Separation

**Use comment separators to organize API functions by sub-module:**

```typescript
/** ==================== 用户管理 ==================== */
export function fetchGetUserList() { ... }
export function fetchAddUser() { ... }

/** ==================== 角色管理 ==================== */
export function fetchGetRoleList() { ... }
export function fetchAddRole() { ... }
```

## API Calling Standards

### Import API Functions

**Import API functions at the top of the component:**

```typescript
// ✅ Correct: Import API functions
import { fetchGetUserList, fetchAddUser, fetchDeleteUser } from '@/api/system-manage'
```

### Using API in useTable

**Pass API function to `useTable` hook:**

```typescript
import { fetchGetUserList } from '@/api/system-manage'

const {
  data,
  loading,
  pagination,
  getData,
  refreshData,
  refreshRemove
} = useTable<typeof fetchGetUserList>({
  core: {
    apiFn: fetchGetUserList,  // Pass API function
    apiParams: computed(() => ({
      pageNum: formFilters.value.pageNum,
      username: formFilters.value.username || undefined
    })),
    paginationKey: { current: 'pageNum', size: 'pageSize' },
    columnsFactory: () => [...]
  }
})
```

### Calling API in Event Handlers

**Use try-catch for error handling, call refresh methods after success:**

```typescript
/**
 * Delete user
 */
const handleDelete = async (row: UserListItem): Promise<void> => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.username}" 吗？此操作不可恢复！`,
      '删除用户',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // Call API
    await fetchDeleteUser(row.id)

    // Refresh table data
    await refreshRemove()

    // Note: Success message is shown automatically by API layer
  } catch (error) {
    // Handle cancel action (user clicked cancel)
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      // Error message is shown automatically by API layer
    }
  }
}

/**
 * Add user
 */
const handleAdd = async (): Promise<void> => {
  const submitLoading = ref(false)

  try {
    submitLoading.value = true

    // Validate form
    const valid = await formRef.value.validate()
    if (!valid) return

    // Call API
    await fetchAddUser(formData.value)

    // Close dialog
    dialogVisible.value = false

    // Refresh table data
    await refreshCreate()

    // Note: Success message is shown automatically
  } catch (error) {
    console.error('添加用户失败:', error)
    // Error message is shown automatically
  } finally {
    submitLoading.value = false
  }
}
```

### Refresh Methods

**Use appropriate refresh method based on operation:**

| Operation | Refresh Method | Purpose |
|-----------|---------------|---------|
| Create | `refreshCreate()` | Refresh and stay on current page |
| Update | `refreshUpdate()` | Refresh and stay on current page |
| Delete | `refreshRemove()` | Refresh and stay on current page |
| General | `refreshData()` | Refresh table data |
| Search | `getData()` | Fetch data with current params |

### Error Handling

**API layer handles errors automatically, component only needs to handle business logic:**

```typescript
// ✅ Correct: API layer handles error messages
try {
  await fetchDeleteUser(id)
  await refreshRemove()
  // Success message shown automatically
} catch (error) {
  // Error message shown automatically
  // Only handle cancel or business logic here
  if (error !== 'cancel') {
    console.error('删除失败:', error)
  }
}

// ❌ Wrong: Don't manually show error messages
try {
  await fetchDeleteUser(id)
  ElMessage.success('删除成功')  // Already shown by API layer
} catch (error) {
  ElMessage.error('删除失败')  // Already shown by API layer
}
```

### Loading States

**For dialog forms, use `submitLoading`:**

```typescript
const submitLoading = ref(false)

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    await fetchAddUser(formData.value)
    dialogVisible.value = false
    await refreshCreate()
  } finally {
    submitLoading.value = false
  }
}
```

**For table operations, use `loading` from `useTable`:**

```typescript
const { loading, refreshData } = useTable(...)

// loading is automatically managed by useTable
```

## Dictionary Usage Standards

### Unified Dictionary Store

**All dictionary data must use `useDictStore` from `@/store/modules/dict`:**

```typescript
// ✅ Correct: Import dict store
import { useDictStore } from '@/store/modules/dict'

const dictStore = useDictStore()
```

### Dictionary Methods

**Three main methods for dictionary access:**

| Method | Purpose | Cache Behavior |
|--------|---------|----------------|
| `getDictData(dictCode)` | Get cached data (synchronous) | Returns cached data or empty array |
| `loadDictData(dictCode, force?)` | Load single dictionary | **Prioritizes cache**, loads if missing |
| `loadDictDataBatch(dictCodes, force?)` | Load multiple dictionaries | **Prioritizes cache**, batch loads if missing |

### Priority: Cache First

**Always prioritize reading from cache:**

```typescript
// ✅ Correct: Use getDictData for computed (reads cache)
const dictOptions = computed(() => {
  const data = dictStore.getDictData('dict_code')
  return data.map(item => ({ label: item.label, value: item.value }))
})

// ✅ Correct: Use loadDictData (automatically uses cache if available)
const loadDict = async () => {
  // If cache exists, returns immediately without API call
  await dictStore.loadDictData('dict_code')
}

// ✅ Correct: Batch load (checks cache first)
const loadDicts = async () => {
  const result = await dictStore.loadDictDataBatch(['dict1', 'dict2', 'dict3'])
  // If all cached, returns immediately without API call
}
```

### Usage Patterns

#### Pattern 1: Direct Store Usage

```typescript
import { useDictStore } from '@/store/modules/dict'

const dictStore = useDictStore()

// Load dictionary on component mount
onMounted(async () => {
  await dictStore.loadDictData('dict_code')
})

// Use in computed (reads from cache)
const options = computed(() => {
  const data = dictStore.getDictData('dict_code')
  return data.map(item => ({ label: item.label, value: item.value }))
})
```

#### Pattern 2: Using Encapsulated Hooks

```typescript
// ✅ Correct: Use existing hooks (e.g., useBusinessType)
import { useBusinessType } from '@/hooks'

const { businessTypeOptions, fetchBusinessTypes } = useBusinessType()

// Load on mount
onMounted(async () => {
  await fetchBusinessTypes()
})

// Use in template (automatically reactive)
// businessTypeOptions is computed from cache
```

#### Pattern 3: Batch Loading in Dialogs

```typescript
const dictStore = useDictStore()

const loadDictData = async (): Promise<void> => {
  try {
    // ✅ Correct: Batch load multiple dictionaries
    const dictRes = await dictStore.loadDictDataBatch([
      'dormitory_room_type',
      'dormitory_room_status',
      'dormitory_room_facility'
    ])

    // Transform to options format
    roomTypeOptions.value = (dictRes.dormitory_room_type || []).map(
      (item: Api.SystemManage.DictDataListItem) => ({
        label: item.label,
        value: item.value
      })
    )
  } catch (error) {
    console.error('加载字典数据失败:', error)
  }
}

onMounted(() => {
  loadDictData()
})
```

### Dictionary Refresh

**Refresh dictionary cache when data is updated:**

```typescript
// After updating dictionary data in dictionary management page
await dictStore.refreshDictData('dict_code')

// Or clear specific cache
dictStore.clearDictCache('dict_code')

// Or clear all cache
dictStore.clearDictCache()
```

### Forbidden Patterns

```typescript
// ❌ Wrong: Don't call API directly for dictionary data
import { fetchGetDictDataList } from '@/api/system-manage'
const data = await fetchGetDictDataList('dict_code')  // Forbidden!

// ❌ Wrong: Don't create local dictionary cache
const localDictCache = ref({})  // Forbidden!

// ❌ Wrong: Don't bypass dictStore
// Always use useDictStore, never create custom dictionary management
```

### Best Practices

1. **Always use `useDictStore`** - Never call dictionary API directly
2. **Prioritize cache** - Use `getDictData` for computed, `loadDictData` for async loading
3. **Batch loading** - Use `loadDictDataBatch` when loading multiple dictionaries
4. **Use hooks when available** - Prefer encapsulated hooks like `useBusinessType`
5. **Refresh after updates** - Call `refreshDictData` after modifying dictionary data

## Permission Code Verification Standards

### Database-Frontend Permission Alignment

**CRITICAL: All `auth` properties in action buttons must match database permission codes exactly:**

```typescript
// ✅ Correct: Verify permissions against sys_menu table
// Query: SELECT name, permission FROM sys_menu WHERE permission LIKE 'module:submodule:%'
{
  type: 'view',
  label: '查看详情',
  onClick: () => handleViewDetail(row),
  auth: 'allocation:survey:detail'  // Must exist in sys_menu.permission
}

// ❌ Wrong: Using permission code that doesn't exist in database
{
  type: 'view',
  label: '查看详情',
  auth: 'allocation:survey:view'  // Database has 'allocation:survey:detail' instead
}
```

### Permission Verification Process

**Before implementing action buttons, verify permissions in database:**

1. Connect to MySQL database:
   ```sql
   mysql -h localhost -u root -p project_management
   ```

2. Query module permissions:
   ```sql
   SELECT id, name, permission, parent_id
   FROM sys_menu
   WHERE permission LIKE 'module:submodule:%'
   ORDER BY sort;
   ```

3. Match frontend `auth` codes to `permission` column values

### Common Permission Patterns

| Module Level | Permission Pattern | Example | Purpose |
|--------------|-------------------|---------|---------|
| Page access | `module:submodule:view` | `allocation:survey:view` | View page permission |
| Detail view | `module:submodule:detail` | `allocation:survey:detail` | View detail permission |
| Create | `module:submodule:add` | `student:manage:add` | Create permission |
| Update | `module:submodule:edit` | `student:manage:edit` | Update permission |
| Delete | `module:submodule:delete` | `student:manage:delete` | Delete permission |
| Statistics | `module:submodule:statistics` | `allocation:survey:statistics` | View statistics permission |
| Export | `module:submodule:export` | `student:manage:export` | Export data permission |

### Permission Debugging

```typescript
// Use hasPermission directive to check permission availability
import { hasPermission } from '@/directives/core/permission'

// Filter buttons by permission before rendering
const visibleButtons = buttons.filter((btn) => !btn.auth || hasPermission(btn.auth))
```

## Action Button Icon Configuration Standards

### Central Configuration File

**ALL button types and icons are centrally configured in `manager/src/utils/table/actionButtons.ts`:**

```typescript
/**
 * Button type label mapping
 */
const BUTTON_LABELS: Record<string, string> = {
  view: '查看',
  add: '新增',
  edit: '编辑',
  delete: '删除',
  detail: '详情',
  download: '下载',
  upload: '上传',
  assign: '分配',
  reset: '重置',
  share: '分配',
  link: '绑定',
  notify: '提醒'  // Must add label for new button types
}

/**
 * Button type icon mapping (using RemixIcon)
 */
export const BUTTON_ICONS: Record<string, string> = {
  view: 'ri:eye-line',
  add: 'ri:add-line',
  edit: 'ri:edit-line',
  delete: 'ri:delete-bin-line',
  detail: 'ri:file-list-line',
  download: 'ri:download-line',
  upload: 'ri:upload-line',
  assign: 'ri:user-settings-line',
  reset: 'ri:refresh-line',
  share: 'ri:share-line',
  link: 'ri:link',
  notify: 'ri:notification-line'  // Must add icon for new button types
}
```

### Adding New Button Types

**When adding a new button type, complete ALL three steps:**

1. **Add to database dictionary** (`sys_dict_data` table):
   ```sql
   INSERT INTO sys_dict_data (dict_code, label, value, sort, is_default, status)
   VALUES ('table_button_config', '提醒', 'notify', 10, 0, 1);
   ```

2. **Add label to `BUTTON_LABELS`** in `actionButtons.ts`:
   ```typescript
   const BUTTON_LABELS: Record<string, string> = {
     // ... existing labels
     notify: '提醒'  // Add new label
   }
   ```

3. **Add icon to `BUTTON_ICONS`** in `actionButtons.ts`:
   ```typescript
   export const BUTTON_ICONS: Record<string, string> = {
     // ... existing icons
     notify: 'ri:notification-line'  // Add new icon
   }
   ```

**Missing any step will cause:**
- Missing step 1: TypeScript error (type not in dictionary)
- Missing step 2: Button shows type name instead of label
- Missing step 3: **Button icon not visible** (clickable padding but no icon)

### Icon Selection Guidelines

**Use RemixIcon (ri:) with consistent naming:**

| Button Type | Icon | Pattern |
|-------------|------|---------|
| view | `ri:eye-line` | View/see actions |
| edit | `ri:edit-line` | Modify actions |
| delete | `ri:delete-bin-line` | Delete actions |
| add | `ri:add-line` | Create actions |
| download | `ri:download-line` | Download actions |
| upload | `ri:upload-line` | Upload actions |
| notify | `ri:notification-line` | Notification actions |
| share | `ri:share-line` | Share/distribute actions |
| link | `ri:link` | Link/bind actions |
| reset | `ri:refresh-line` | Reset/refresh actions |

**Icon naming conventions:**
- ✅ Use `-line` suffix for outlined icons (preferred)
- ✅ Use `-fill` suffix for filled icons (use sparingly)
- ❌ Don't mix icon styles within the same context

## Element Plus Auto-Import Rules

**The project uses `unplugin-auto-import` + `ElementPlusResolver`, the following are globally available and MUST NOT be explicitly imported:**

| Auto-Imported Global | Usage |
|---------------------|-------|
| `ElMessage` | `ElMessage.success(...)` |
| `ElMessageBox` | `ElMessageBox.confirm(...)` |
| `ElTag` | `h(ElTag, ...)` |
| `ElProgress` | `h(ElProgress, ...)` |
| `ElRate` | `h(ElRate, ...)` |
| `ElInputNumber` | `h(ElInputNumber, ...)` |
| `ElNotification` | `ElNotification(...)` |
| `ElLoading` | `ElLoading.service(...)` |

```typescript
// ❌ Forbidden: Importing auto-imported globals
import { ElMessage, ElMessageBox } from 'element-plus'
import { ElTag } from 'element-plus'

// ✅ Correct: Use directly without import
ElMessage.success('操作成功')
ElMessageBox.confirm('确定删除?', '提示')
h(ElTag, { type: 'success' }, () => '已通过')
```

**Components used in `h()` render functions that are NOT auto-imported still need explicit import:**

```typescript
// ✅ Correct: These need explicit import when used in h()
import { ElImage, ElPopover, ElTooltip, ElEmpty } from 'element-plus'

// ✅ Correct: Type-only imports always needed
import type { FormRules, FormInstance } from 'element-plus'
```

## Import Order

```typescript
// 1. Vue (auto-imported, usually not needed)
import { ref, computed, watch } from 'vue'

// 2. Element Plus (ONLY non-auto-imported components used in h(), and type imports)
import { ElPopover, ElTooltip, ElImage } from 'element-plus'
import type { FormRules } from 'element-plus'

// 3. Project hooks
import { useTable } from '@/hooks/core/useTable'
import { useBusinessType } from '@/hooks'

// 4. Project components
import ArtTable from '@/components/core/tables/art-table/index.vue'

// 5. Store
import { useDictStore } from '@/store/modules/dict'

// 6. API
import { fetchGetCheckInPage, fetchAddUser, fetchDeleteUser } from '@/api/accommodation-manage'

// 7. Types
import type { CheckInListItem } from '@/api/accommodation-manage'
```

## Approval Progress Display Standards

### Required Type Definition

**ApprovalProgress interface (Api.Common.ApprovalProgress):**

```typescript
interface ApprovalProgressNode {
  /** 节点ID */
  nodeId: number
  /** 节点名称 */
  nodeName: string
  /** 审批人姓名（多个用顿号分隔） */
  assigneeNames: string
  /** 节点状态：1-待处理 2-已通过 3-已拒绝 */
  status: number
  /** 节点状态文本 */
  statusText: string
  /** 审批动作文本 */
  actionText?: string
  /** 审批时间 */
  approveTime?: string
}

interface ApprovalProgress {
  /** 审批状态：1-待审核 2-已通过 3-已拒绝 4-已完成 */
  status: number
  /** 审批状态文本 */
  statusText: string
  /** 申请人姓名 */
  applicantName?: string
  /** 流程发起时间 */
  startTime?: string
  /** 当前审批节点名称 */
  currentNodeName?: string
  /** 下一审批人姓名 */
  nextApproverName?: string
  /** 审批进度描述文本 */
  progressText: string
  /** 已完成节点数 */
  completedNodes?: number
  /** 节点总数 */
  totalNodes?: number
  /** 审批进度百分比 */
  progressPercent?: number
  /** 审批流程节点进度列表 */
  nodeTimeline?: ApprovalProgressNode[]
}
```

### ListItem Fields

All approval-related ListItems must include:

```typescript
interface CheckInListItem {
  // ... other fields
  approvalInstanceId?: number
  approvalProgress?: Api.Common.ApprovalProgress
}
```

### Display Component Usage

**✅ Must use `ApprovalProgressTag` component:**

```typescript
import ApprovalProgressTag from '@/components/core/approval/approval-progress-tag/index.vue'

// In columnsFactory
{
  prop: 'approvalProgress',
  label: '审批状态',
  width: 120,  // Recommended width for card-style design
  formatter: (row: CheckInListItem) => {
    return h(ApprovalProgressTag, {
      approvalProgress: row.approvalProgress
    })
  }
}
```

**Display Rules**:
- **In progress (status=1)**: show progress bar with percentage
- **Completed/Approved/Rejected**: show Tag with statusText
- **Hover popover**: show applicant, start time, current node + progress bar + scrollable timeline

**❌ Forbidden: Display only status Tag without progress:**

```typescript
// ❌ Wrong: Only shows status Tag, no progress info
{
  prop: 'status',
  label: '状态',
  width: 100,
  formatter: (row) => {
    return h(ElTag, { type: 'warning' }, () => '待审核')
  }
}
```

## Table Column Hover Popover Standards

### Student Information Popover

**✅ Must use `ArtStudentInfoPopover` component for student name columns:**

```typescript
import { ElPopover } from 'element-plus'
import StudentInfoPopover from '@/components/core/cards/art-student-info-popover/index.vue'

// In columnsFactory
{
  prop: 'studentName',
  label: '姓名',
  minWidth: 100,
  formatter: (row: StudentListItem) => {
    // Handle empty name
    if (!row.studentName) {
      return h('span', row.studentName || '--')
    }
    // Use ElPopover with StudentInfoPopover component
    return h(ElPopover, {
      placement: 'bottom-start',
      trigger: 'hover',
      width: 320,
      popperClass: 'student-info-popover'
    }, {
      default: () => h(StudentInfoPopover, { student: row }),
      reference: () =>
        h('span', { class: 'text-primary cursor-pointer hover:underline' }, row.studentName)
    })
  }
}
```

**Display Rules**:
- **Trigger**: `hover` - Show popover on mouse hover
- **Placement**: `bottom-start` - Align to bottom-left of name
- **Width**: `320px` - Fixed width for consistent layout
- **Name styling**: Primary color, pointer cursor, underline on hover
- **Component**: Uses `ArtStudentInfoPopover` to display student details

**Backend Data Contract**:
When backend returns `studentName`, it **must** also return all fields required for popover display:
- Basic info: `studentNo`, `gender`/`genderText`, `phone`, `nation`, `politicalStatus`
- School info: `campusName`, `deptName`, `majorName`, `className`
- Accommodation info: `floorName`, `roomName`, `bedName`
- Academic info: `academicStatusText`, `enrollmentYear`, `currentGrade`

See `src/types/api/api.d.ts` for complete data contract documentation.

**❌ Forbidden: Direct text display without popover:**

```typescript
// ❌ Wrong: Only shows name text, no hover popover
{
  prop: 'studentName',
  label: '姓名',
  minWidth: 100
  // Missing formatter with popover
}
```

### progressText Format

Backend assembles `progressText` in these formats:

- **Pending** (status=1): `{节点名称}({审批人姓名})`, e.g., `辅导员审批(张三)`
- **Approved** (status=2): `已通过`
- **Rejected** (status=3): `已拒绝`
- **Completed** (status=4): `已完成`
- **Unknown/Error**: `未知进度`

**Format Note**: The "审核中 -> " prefix was removed for cleaner display, as the Tag already shows the status.

## Drawer/Dialog Data Loading Best Practices

### Problem: Empty Data After Component Switch

**When switching between view/edit modes or opening detail drawers, data may appear empty due to:**
1. Component destruction/recreation with `v-if`
2. Incomplete data passed from list (missing related entity data)
3. Data loading timing issues

### Solution: Three-Part Approach

#### 1. Use `v-show` Instead of `v-if` (When State Preservation Needed)

**Use `v-show` to avoid component destruction when switching between view/edit:**

```vue
<!-- ✅ Correct: Use v-show to preserve component state -->
<StudentDrawer
  v-show="dialogType === 'view'"
  v-model:visible="drawerVisible"
  :student-id="editData?.id"
  :student-data="editData"
/>

<StudentDialog
  v-show="dialogType === 'edit' || dialogType === 'add'"
  v-model:visible="dialogVisible"
  :student-id="editData?.id"
  :student-data="editData"
/>
```

**When to use `v-show` vs `v-if`:**
- ✅ **Use `v-show`**: When switching between related components (view/edit), or when component state needs preservation
- ✅ **Use `v-if`**: When components are completely independent and don't need state preservation

#### 2. Separate Visible States for Different Components

**Create independent `visible` states to avoid conflicts:**

```typescript
// ✅ Correct: Separate states for different components
const drawerVisible = ref(false) // For view drawer
const dialogVisible = ref(false) // For edit/add dialog

// In handlers, ensure mutual exclusivity
const handleView = (row: StudentListItem) => {
  dialogType.value = 'view'
  editData.value = row
  drawerVisible.value = true
  dialogVisible.value = false // Explicitly close dialog
}

const handleEdit = (row: StudentListItem) => {
  dialogType.value = 'edit'
  editData.value = row
  dialogVisible.value = true
  drawerVisible.value = false // Explicitly close drawer
}
```

#### 3. Prioritize ID-Based API Calls Over Passed Data

**Always prioritize using ID to fetch complete data via API, rather than relying on incomplete list data:**

```typescript
// ✅ Correct: Prioritize API call with ID
const loadStudentDetail = async () => {
  if (!props.studentId) {
    formData.value = {}
    return
  }

  // Priority 1: Use ID to fetch complete data from API
  try {
    loading.value = true
    const res = await fetchGetStudentDetail(props.studentId)
    if (res) {
      formData.value = res
    }
  } catch {
    // Priority 2: Fallback to passed data if API fails
    if (props.studentData) {
      formData.value = { ...props.studentData }
    }
    ElMessage.error('获取学生详情失败')
  } finally {
    loading.value = false
  }
}
```

**Why this approach:**
- List data may be incomplete (missing related entities like `floorName`, `roomName`, `bedName`)
- API detail endpoint returns complete data with all related entities
- Ensures data freshness and completeness

#### 4. Add Watch Listeners as Backup

**Add watch listeners for passed data as a fallback when ID is not available:**

```typescript
// ✅ Correct: Watch for data changes as backup
watch(
  () => props.studentData,
  (newVal) => {
    if (props.visible && newVal && !props.studentId) {
      // Only use passed data when ID is not available
      formData.value = { ...newVal }
    }
  }
)
```

### Complete Example: Drawer Component

```vue
<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { fetchGetStudentDetail } from '@/api/accommodation-manage'
import { ElMessage } from 'element-plus'

interface Props {
  visible: boolean
  studentId?: number
  studentData?: StudentListItem | null
}

const props = defineProps<Props>()

const formData = ref<Partial<StudentDetail>>({})
const loading = ref(false)

// Priority: Use ID to fetch complete data
const loadStudentDetail = async () => {
  if (!props.studentId) {
    formData.value = {}
    return
  }

  try {
    loading.value = true
    // Priority 1: API call with ID
    const res = await fetchGetStudentDetail(props.studentId)
    if (res) {
      formData.value = res
    }
  } catch {
    // Priority 2: Fallback to passed data
    if (props.studentData) {
      formData.value = { ...props.studentData }
    }
    ElMessage.error('获取学生详情失败')
  } finally {
    loading.value = false
  }
}

// Watch for visible changes
watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      loadStudentDetail()
    }
  }
)

// Watch for ID changes
watch(
  () => props.studentId,
  () => {
    if (props.visible) {
      loadStudentDetail()
    }
  }
)

// Watch for data changes (backup)
watch(
  () => props.studentData,
  (newVal) => {
    if (props.visible && newVal && !props.studentId) {
      formData.value = { ...newVal }
    }
  }
)
</script>
```

### Complete Example: Parent Component

```vue
<template>
  <!-- View Drawer -->
  <StudentDrawer
    v-show="dialogType === 'view'"
    v-model:visible="drawerVisible"
    :student-id="editData?.id"
    :student-data="editData"
  />

  <!-- Edit Dialog -->
  <StudentDialog
    v-show="dialogType === 'edit' || dialogType === 'add'"
    v-model:visible="dialogVisible"
    :student-id="editData?.id"
    :student-data="editData"
  />
</template>

<script setup lang="ts">
const drawerVisible = ref(false) // Separate state for drawer
const dialogVisible = ref(false) // Separate state for dialog
const dialogType = ref<'view' | 'edit' | 'add'>('view')
const editData = ref<StudentListItem | null>(null)

const handleView = (row: StudentListItem) => {
  dialogType.value = 'view'
  editData.value = row
  drawerVisible.value = true
  dialogVisible.value = false // Ensure dialog is closed
}

const handleEdit = (row: StudentListItem) => {
  dialogType.value = 'edit'
  editData.value = row
  dialogVisible.value = true
  drawerVisible.value = false // Ensure drawer is closed
}
</script>
```

### Forbidden Patterns

```typescript
// ❌ Wrong: Using same visible state for multiple components
const dialogVisible = ref(false) // Used for both drawer and dialog
// This causes both components to show/hide together

// ❌ Wrong: Using v-if when state preservation is needed
<StudentDrawer v-if="dialogType === 'view'" /> // Component destroyed on switch

// ❌ Wrong: Prioritizing passed data over API call
if (props.studentData) {
  formData.value = props.studentData // May be incomplete
  return
}
const res = await fetchGetStudentDetail(props.studentId) // Never reached

// ❌ Wrong: Not ensuring mutual exclusivity
const handleView = (row) => {
  drawerVisible.value = true
  // Missing: dialogVisible.value = false
}
```

## Code Review Checklist

### Component Usage
- [ ] Uses encapsulated components?
- [ ] Dialog forms use `ArtForm` with `formItems`?
- [ ] Uses `useTable` for tables?
- [ ] Uses `v-show` when component state needs preservation?
- [ ] Separate `visible` states for different components?
- [ ] Data loading prioritizes ID-based API calls?

### Forms
- [ ] Submit loading variable is `submitLoading`?
- [ ] Submit button text shows loading state?
- [ ] Search form uses `ref` (not `reactive`)?

### Tables
- [ ] Column widths configured correctly?
- [ ] Date/time columns have `sortable: true` and `width`?
- [ ] Status columns use `ArtSwitch`?
- [ ] `apiParams` explicitly lists fields (no spread)?
- [ ] Context menu enabled?
- [ ] Action column returns `ActionButtonConfig[]` with `label`?
- [ ] Action handlers defined BEFORE `useTable` call?
- [ ] No separate `getRowActions` function or `actionsGetter` in config?
- [ ] Button types exist in database dictionary?
- [ ] All button types have icons configured in `actionButtons.ts`?

### API Writing
- [ ] File header includes `@module`, `@author`, `@date`?
- [ ] Functions use `fetch` prefix + verb + noun?
- [ ] Paginated list uses `fetchGetXxxPage` (not `fetchGetXxxList`)?
- [ ] Types defined in `src/types/api/` namespace (not inline in API files)?
- [ ] Types use `Api.ModuleName.Xxx` namespace?
- [ ] POST/PUT/DELETE have `showSuccessMessage: true`?
- [ ] JSDoc comments for all functions?
- [ ] Module separators (`/** ==================== */`) used?

### API Calling
- [ ] API functions imported correctly?
- [ ] `useTable` receives API function via `apiFn`?
- [ ] Event handlers use try-catch?
- [ ] Appropriate refresh method called after operations?
- [ ] No manual success/error messages (handled by API layer)?
- [ ] Loading states managed correctly?

### Dictionary Usage
- [ ] Uses `useDictStore` for all dictionary data?
- [ ] Prioritizes cache (uses `getDictData` or `loadDictData`)?
- [ ] Uses `loadDictDataBatch` for multiple dictionaries?
- [ ] No direct API calls to dictionary endpoints?
- [ ] Uses existing hooks (e.g., `useBusinessType`) when available?
- [ ] Refreshes dictionary cache after updates?

### Permission Verification
- [ ] All `auth` codes verified against `sys_menu` table?
- [ ] Permission codes match database exactly (no typos)?
- [ ] New button types added to database dictionary first?
- [ ] Button icons configured in `actionButtons.ts`?
- [ ] Button labels configured in `actionButtons.ts`?

### Element Plus Imports
- [ ] No explicit imports of auto-imported globals (ElMessage, ElMessageBox, ElTag, etc.)?
- [ ] Non-auto-imported components used in `h()` have explicit imports (ElPopover, ElTooltip, etc.)?
- [ ] Type imports use `import type { ... }` syntax?

### Naming
- [ ] Event handlers use standard names?
- [ ] Variables use standard names?
- [ ] No `any` types?
