# 微信小程序学生端开发任务清单

> 生成日期：2026-01-16
>
> 本文档基于对后台管理系统、后端API接口和微信小程序现有实现的分析，列出学生端接下来需要开发的功能任务。

---

## 一、项目现状概述

### 1.1 已实现的核心功能

| 功能模块 | 实现状态 | 说明 |
|---------|---------|------|
| 身份认证 | ✅ 已完成 | 账号密码登录、验证码登录、微信登录、Token刷新 |
| 入住申请 | ✅ 已完成 | 长期入住、临时入住申请表单和列表 |
| 退宿申请 | ✅ 已完成 | 退宿申请表单和列表 |
| 调宿申请 | ✅ 已完成 | 调宿申请表单和列表 |
| 留宿申请 | ✅ 已完成 | 留宿申请表单和列表 |
| 报修服务 | ✅ 已完成 | 报修申请、图片上传、报修记录列表 |
| 生活习惯 | ✅ 已完成 | 生活习惯填写和展示 |
| 公告通知 | ✅ 已完成 | 公告列表、详情、未读统计 |
| 数据统计 | ✅ 已完成 | 申请统计图表（周/月/年） |
| 消息中心 | ✅ 已完成 | 消息分类、列表展示 |

### 1.2 待完善/开发的功能

| 功能模块 | 当前状态 | 问题描述 |
|---------|---------|---------|
| 个人资料编辑 | ⚠️ 占位 | 点击显示"编辑功能开发中" |
| 通用设置 | ⚠️ 占位 | 点击显示"设置功能开发中" |
| 帮助与反馈 | ⚠️ 占位 | 点击显示"帮助与反馈功能开发中" |
| 关于我们 | ⚠️ 占位 | 点击显示"关于我们功能开发中" |
| 宿舍信息 | ⚠️ 硬编码 | 使用硬编码数据，未对接API |
| 室友信息 | ⚠️ 硬编码 | 使用硬编码mock数据 |
| 申请撤回 | ❌ 未实现 | 无法撤回已提交的申请 |
| 密码修改 | ❌ 未实现 | 无密码修改入口 |
| 头像上传 | ❌ 未实现 | 无法更换头像 |

---

## 二、开发任务清单

### 🔴 P0 - 必须完成（核心功能补全）

#### 2.1 个人资料编辑页面

**文件位置**: `src/pages/profile/edit/index.vue` (新建)

**功能需求**:
- 展示当前个人信息（姓名、学号、手机号、邮箱等）
- 支持修改可编辑字段（手机号、邮箱、紧急联系人）
- 头像上传功能
- 表单验证和提交

**涉及API**:
```typescript
GET  /api/v1/auth/user/profile    // 获取用户详情
PUT  /api/v1/auth/user/profile    // 更新用户信息
POST /api/v1/common/upload        // 上传头像
```

---

#### 2.2 宿舍信息对接真实API

**文件位置**: `src/pages/common/dorm-info/index.vue` (修改)

**当前问题**: 页面使用硬编码数据，需要对接后端API

**功能需求**:
- 从API获取当前学生的宿舍信息（校区、楼栋、楼层、房间、床位）
- 从API获取室友列表信息
- 添加加载状态和错误处理
- 支持下拉刷新

**涉及API**:
```typescript
GET /api/v1/student/dorm-info     // 获取宿舍信息
GET /api/v1/student/roommates     // 获取室友列表
```

---

#### 2.3 密码修改功能

**文件位置**: `src/pages/profile/change-password/index.vue` (新建)

**功能需求**:
- 输入原密码验证
- 输入新密码（两次确认）
- 密码强度校验
- 修改成功后重新登录

**涉及API**:
```typescript
POST /api/v1/auth/change-password  // 修改密码
```

---

#### 2.4 申请撤回功能

**文件位置**: `src/pages/apply/detail/index.vue` (修改)

**功能需求**:
- 在申请详情页添加"撤回申请"按钮
- 仅"待审批"状态的申请可撤回
- 撤回前二次确认
- 撤回后更新列表状态

**涉及API**:
```typescript
POST /api/v1/system/check-in/{id}/cancel    // 撤回入住申请
POST /api/v1/system/check-out/{id}/cancel   // 撤回退宿申请
POST /api/v1/system/transfer/{id}/cancel    // 撤回调宿申请
POST /api/v1/system/stay/{id}/cancel        // 撤回留宿申请
```

---

### 🟠 P1 - 重要功能（用户体验提升）

#### 2.5 通用设置页面

**文件位置**: `src/pages/profile/settings/index.vue` (新建)

**功能需求**:
- 消息通知开关（系统通知、申请进度通知）
- 清除缓存功能
- 隐私设置（是否展示联系方式给室友）
- 深色模式切换（可选）

---

#### 2.6 帮助与反馈页面

**文件位置**: `src/pages/profile/help/index.vue` (新建)

**功能需求**:
- 常见问题FAQ列表（可折叠展开）
- 意见反馈表单（问题类型、描述、截图上传）
- 联系客服入口
- 使用指南/操作手册

**涉及API**:
```typescript
POST /api/v1/feedback/submit      // 提交反馈
GET  /api/v1/faq/list             // 获取FAQ列表
```

---

#### 2.7 关于我们页面

**文件位置**: `src/pages/profile/about/index.vue` (新建)

**功能需求**:
- 应用版本信息
- 更新日志
- 用户协议链接
- 隐私政策链接
- 开发团队信息（可选）

---

#### 2.8 报修评价功能完善

**文件位置**: `src/pages/service/repair-detail/index.vue` (新建)

**功能需求**:
- 报修详情页面
- 已完成的报修可进行评价
- 星级评分 + 文字评价
- 评价后不可修改

**涉及API**:
```typescript
GET  /api/v1/service/repair/{id}  // 获取报修详情
POST /api/v1/service/repair/rate  // 提交评价
```

---

### 🟡 P2 - 新功能开发（功能扩展）

#### 2.9 访客登记功能

**文件位置**: `src/pages/service/visitor/index.vue` (新建)

**功能需求**:
- 访客预约登记表单（访客姓名、身份证、来访时间、来访事由）
- 访客记录列表
- 访客状态追踪（待审批/已通过/已拒绝/已到访）
- 生成访客二维码（审批通过后）

**涉及API**:
```typescript
GET  /api/v1/visitor/page         // 访客记录列表
POST /api/v1/visitor/register     // 登记访客
GET  /api/v1/visitor/{id}/qrcode  // 获取访客二维码
```

---

#### 2.10 晚归登记功能

**文件位置**: `src/pages/service/late-return/index.vue` (新建)

**功能需求**:
- 晚归申请表单（预计返回时间、晚归原因）
- 晚归记录列表
- 支持紧急情况快速登记

**涉及API**:
```typescript
GET  /api/v1/late-return/page     // 晚归记录列表
POST /api/v1/late-return/apply    // 提交晚归申请
```

---

#### 2.11 卫生检查记录查看

**文件位置**: `src/pages/service/hygiene/index.vue` (新建)

**功能需求**:
- 查看本宿舍卫生检查记录
- 检查评分和评语展示
- 历史记录列表（按时间排序）
- 卫生评分趋势图表

**涉及API**:
```typescript
GET /api/v1/hygiene/records       // 卫生检查记录列表
GET /api/v1/hygiene/statistics    // 卫生评分统计
```

---

#### 2.12 水电费查询功能

**文件位置**: `src/pages/service/utility-bill/index.vue` (新建)

**功能需求**:
- 当月水电费用展示
- 历史账单列表
- 用量趋势图表
- 缴费提醒

**涉及API**:
```typescript
GET /api/v1/utility/current       // 当月费用
GET /api/v1/utility/history       // 历史账单
```

---

#### 2.13 失物招领功能

**文件位置**: `src/pages/service/lost-found/index.vue` (新建)

**功能需求**:
- 失物招领列表（Tab切换：寻物启事/招领启事）
- 发布寻物/招领信息
- 图片上传
- 联系失主/拾主
- 状态更新（已找到/已认领）

**涉及API**:
```typescript
GET  /api/v1/lost-found/page      // 失物招领列表
POST /api/v1/lost-found/publish   // 发布信息
PUT  /api/v1/lost-found/{id}      // 更新状态
```

---

### 🟢 P3 - 体验优化（锦上添花）

#### 2.14 室友匹配推荐

**文件位置**: `src/pages/service/roommate-match/index.vue` (新建)

**功能需求**:
- 基于生活习惯的室友匹配推荐
- 匹配度评分展示
- 查看推荐室友的生活习惯详情
- 发起室友申请

**涉及API**:
```typescript
GET /api/v1/roommate/recommend    // 获取推荐室友列表
POST /api/v1/roommate/apply       // 发起室友申请
```

---

#### 2.15 消息推送优化

**功能需求**:
- 微信订阅消息授权
- 申请状态变更推送
- 报修进度推送
- 公告通知推送

**涉及技术**:
- 微信小程序订阅消息API
- 后端消息推送服务对接

---

#### 2.16 离线缓存优化

**功能需求**:
- 关键数据本地缓存（用户信息、宿舍信息）
- 离线状态提示
- 网络恢复后自动同步
- 缓存过期策略

---

#### 2.17 骨架屏加载优化

**功能需求**:
- 为主要页面添加骨架屏
- 首页、申请列表、消息列表等
- 提升用户感知加载速度

---

## 三、页面路由配置参考

新增页面需要在 `pages.json` 中注册，建议按以下结构组织：

```json
{
  "subPackages": [
    {
      "root": "pages/profile",
      "pages": [
        { "path": "edit/index" },
        { "path": "change-password/index" },
        { "path": "settings/index" },
        { "path": "help/index" },
        { "path": "about/index" }
      ]
    },
    {
      "root": "pages/service",
      "pages": [
        { "path": "repair-detail/index" },
        { "path": "visitor/index" },
        { "path": "late-return/index" },
        { "path": "hygiene/index" },
        { "path": "utility-bill/index" },
        { "path": "lost-found/index" },
        { "path": "roommate-match/index" }
      ]
    }
  ]
}
```

---

## 四、开发优先级总结

| 优先级 | 任务数量 | 说明 |
|-------|---------|------|
| 🔴 P0 | 4个 | 核心功能补全，必须完成 |
| 🟠 P1 | 4个 | 用户体验提升，重要功能 |
| 🟡 P2 | 5个 | 新功能开发，功能扩展 |
| 🟢 P3 | 4个 | 体验优化，锦上添花 |

**建议开发顺序**：P0 → P1 → P2 → P3

---

## 五、注意事项

1. **API对接**：部分新功能需要后端先开发对应API接口
2. **UI一致性**：新页面应遵循现有的毛玻璃设计风格
3. **类型定义**：新增API需在 `src/types/api/` 下定义对应类型
4. **Mock数据**：开发阶段可先使用Mock数据进行UI开发
5. **权限控制**：部分功能可能需要根据用户角色进行权限控制

---

> 文档结束
