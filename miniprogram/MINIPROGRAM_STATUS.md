# 微信小程序项目状态说明

本文档基于当前代码库梳理：**已接接口的页面**、**未实现/未接接口的页面**、**样式较普通需完善的页面**，便于后续对接与优化。

---

## 一、已接接口的页面/模块

以下页面或模块已调用真实后端 API（`USE_MOCK = false` 时走真实接口）。

| 页面/模块 | 路径 | 使用的接口 | 说明 |
|-----------|------|------------|------|
| **登录** | `pages/common/login/index` | `loginAPI`、`studentLoginAPI`、`wxLoginAPI`、`getUserInfoAPI`、`refreshTokenAPI`、`logoutAPI` | 通过 store 的 user 模块调用 auth 相关 API |
| **申请详情** | `pages/apply/detail/index` | `getCheckInDetailAPI`、`getCheckOutDetailAPI`、`getTransferDetailAPI`、`getStayDetailAPI`、`getApprovalInstanceByBusinessAPI`、各类 `cancel*API` | 按申请类型拉取详情与审批实例 |
| **申请表单（入住/调宿/退宿/留宿）** | `pages/apply/form` | `submitCheckInAPI`、`submitTransferAPI`、`submitCheckOutAPI`、`submitStayAPI`、`uploadImageAPI`、`uploadSignatureAPI` | 提交与附件/签名上传已接；报修提交未接 |
| **宿舍信息** | `pages/common/dorm-info/index` | `getDormInfoAPI`、getRoommatesAPI | 宿舍信息与室友列表 |
| **个人资料编辑** | `pages/profile/edit/index` | `updateUserProfileAPI` | 更新用户资料 |
| **修改密码** | `pages/profile/change-password/index` | `changePasswordAPI` | 修改密码 |
| **学生习惯** | `pages/profile/student-habits/index` | `getStudentHabits`、`updateStudentHabits` | 习惯的获取与更新 |
| **公告详情** | `pages/service/notice-detail/index` | `getNoticeDetailAPI`、`markNoticeReadAPI` | 公告详情与已读标记 |
| **请求层** | `utils/request/interceptors` | `refreshTokenAPI` | 401 时刷新 token |

---

## 二、未实现或未接接口的页面/功能

以下页面仍使用前端 Mock 数据或占位逻辑，尚未对接列表/统计等接口。

| 页面/功能 | 路径 | 当前实现 | 建议对接的接口 |
|-----------|------|----------|----------------|
| **首页-通知公告** | `pages/tab/home/index` | `noticeList` 在 `loadData` 中写死为本地数组 | `getNoticePageAPI`（或现有通知分页接口） |
| **首页-近期申请** | `pages/tab/home/index` | 非 Mock 时 `applyList` 为空；`getStudentHomeStatisticsAPI` 已注释，有 TODO | `getStudentHomeStatisticsAPI` + 各申请类型分页/列表接口（如 `getCheckInPageAPI` 等） |
| **首页-水电统计** | `pages/tab/home/index` | 使用本地 `electricityData` / `waterData` 等静态数据 | 若有水电统计接口可在此处接入 |
| **申请中心 Tab 列表** | `pages/tab/apply/index` | `loadData` 内写死 `mockData`，有 TODO「调用API加载数据」 | 按 Tab（全部/待审批/已通过/已拒绝）对接各申请分页接口（如 `getCheckInPageAPI`、`getTransferPageAPI` 等）或统一申请列表接口 |
| **消息中心列表** | `pages/service/message/index` | `loadData` 内写死 `mockData` | 若有消息/通知列表接口（如 `getNoticePageAPI`、未读数量 `getUnreadNoticeCountAPI`）可在此接入 |
| **报修记录列表** | `pages/service/repair-list/index` | `repairList` 为组件内静态数组，无请求 | `getRepairPageAPI` |
| **申请表单-报修提交** | `pages/apply/form`（useApplyFormActions） | 选择报修类型后提交仅 `uni.showToast('报修功能开发中')`，未调接口 | `submitRepairAPI`（`api/service/repair` 已定义） |

---

## 三、样式较普通、建议完善的页面

项目已有一套毛玻璃卡片（`glass-card`）、背景装饰（`bg-decorations`/`blob`）、`variables.scss` 等统一风格。以下页面仍为常规白底+简单卡片，未使用这套风格，建议与首页/个人中心/申请列表等页面统一。

| 页面 | 路径 | 当前样式简述 | 建议 |
|------|------|--------------|------|
| **报修记录** | `pages/service/repair-list/index` | 背景 `#f5f5f5`，列表项为白卡+圆角+简单阴影，无背景装饰与 glass-card | 增加背景装饰、使用 `glass-card`、与「消息中心」等列表页风格统一 |
| **宿舍信息** | `pages/common/dorm-info/index` | 背景 `#f5f5f5`，`info-card` 为白卡+边框，无毛玻璃与渐变背景 | 使用 `glass-card`、增加 blob/渐变背景，与个人中心/编辑页风格统一 |
| **公告详情** | `pages/service/notice-detail/index` | 背景 `#f5f5f5`，标题与正文区域为普通块级，无卡片与装饰 | 使用 `glass-card` 包裹内容、增加背景装饰，与消息/详情类页面统一 |
| **网页容器** | `pages/common/webview/index` | 仅一个 `web-view` 组件，无壳样式 | 若需与小程序整体风格一致，可加一层容器与背景（一般可保持极简） |

---

## 四、API 定义与页面使用情况速览

- **已有且未被页面调用的接口举例**
  - 统计：`getStudentHomeStatisticsAPI`（首页已注释未用）
  - 通知：`getNoticePageAPI`、`getUnreadNoticeCountAPI`
  - 报修：`getRepairPageAPI`、`submitRepairAPI` 等
  - 住宿申请分页：`getCheckInPageAPI`、`getCheckOutPageAPI`、`getTransferPageAPI`、`getStayPageAPI` 等（列表页尚未接入）

- **Mock 开关**
  - `src/mock/index.ts` 中 `USE_MOCK = false`，当前为走真实接口；列表/首页统计等仍多为前端写死数据，需在对应页面改为调用上表建议的 API。

---

## 五、建议优先级（参考）

1. **高**：申请中心 Tab 列表、首页近期申请与统计对接真实接口，保证核心流程数据真实。
2. **高**：报修记录列表接入 `getRepairPageAPI`；申请表单报修提交接入 `submitRepairAPI`。
3. **中**：首页通知公告、消息中心列表接入通知/消息相关接口。
4. **中**：报修记录、宿舍信息、公告详情三页样式统一为 glass-card + 背景装饰。
5. **低**：webview 壳样式（按产品需求决定是否加）。

---

*文档生成自当前代码扫描，后续接口或页面若有新增/下线，请同步更新此文档。*
