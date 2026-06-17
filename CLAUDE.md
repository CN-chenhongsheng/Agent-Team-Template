# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

宿舍管理系统（Dormitory Management System），采用前后端分离的多模块 monorepo 架构，包含两个独立子项目：

| 模块 | 路径 | 技术栈 | 用途 |
|------|------|--------|------|
| **Application** | `Application/` | Java 21 + Spring Boot 3.2 + MyBatis-Plus + MySQL + Redis | 后端 API 服务 |
| **manager** | `manager/` | Vue 3 + TypeScript + Element Plus + Tailwind CSS 4 + Vite | 管理后台 Web 端 |

## 常用命令

### 后端 (Application/)

```bash
# 启动（需要 Java 21、Maven、MySQL、Redis）
cd Application && mvn spring-boot:run

# 编译
cd Application && mvn compile

# 打包
cd Application && mvn package -DskipTests

# 运行测试
cd Application && mvn test
```

### 管理后台 (manager/)

```bash
# 安装依赖（需要 Node >=20.19, pnpm >=8.8）
cd manager && pnpm install

# 开发
cd manager && pnpm dev

# 构建（含类型检查）
cd manager && pnpm build

# Lint
cd manager && pnpm lint        # ESLint
cd manager && pnpm lint:stylelint  # Stylelint
cd manager && pnpm fix         # ESLint 自动修复
cd manager && pnpm lint:prettier   # Prettier 格式化
```

## 后端架构 (Application)

### 两层包结构

```
com.project.core/     — 基础设施层：配置、异常处理、工具类、通用实体、常量、枚举
com.project.backend/  — 管理后台 API 层：面向 manager 前端的接口
```

**依赖方向**（只能向下依赖）：`backend → core`。

### 业务模块组织

每个业务模块按功能域划分子包：`controller/`、`service/`（含 `impl/`）、`mapper/`、`entity/`、`dto/`、`vo/`。

现有业务模块：`accommodation`（住宿管理）、`approval`（审批流程）、`organization`（组织架构）、`room`（房间管理）、`school`（学校管理）、`system`（系统管理）等。

### 关键技术组件

- **认证授权**：Sa-Token（非 Spring Security），JWT + Redis 会话管理
- **ORM**：MyBatis-Plus，实体继承 `BaseEntity`
- **API 文档**：Knife4j (OpenAPI 3)
- **统一响应**：`R<T>` 包装类 + `ResultCode` 错误码 + `PageResult<T>` 分页
- **异常处理**：`BusinessException` + `GlobalExceptionHandler`
- **密码加密**：Spring Security Crypto（仅加密模块，不含认证框架）
- **工具库**：Hutool、Guava
- **数据库**：MySQL，数据库名 `project_management`，可通过 MCP 工具直接访问

### 后端命名约定

- Controller：`XxxController`，URL 路径 `/api/模块/资源`
- Service 接口：`XxxService`，实现类：`XxxServiceImpl`
- DTO：`XxxQueryDTO`（查询）、`XxxSaveDTO`（新增/修改）
- VO：`XxxVO`（返回前端的视图对象）
- Entity：与数据库表对应，使用 Lombok `@Data`

## 管理后台架构 (manager)

基于 [Art Design Pro](https://github.com/art-design-ui/art-design-pro) 模板，项目名 `art-design-pro`。

### 核心约定

- **组件优先级**：ArtTable > ArtForm > ArtSwitch > Element Plus 原生组件
- **表格**：统一使用 `useTable` hook + `ArtTable` 组件，禁止直接使用 `el-table`
- **表单**：使用 `ArtForm` 配置式表单，通过 `FormConfig[]` 定义字段
- **API 文件**：放在 `src/api/` 下，函数命名 `verbNounApi`（如 `getUserListApi`），类型定义在同文件或 `types/` 目录
- **路由**：动态路由，权限守卫在 `router/core/` 中
- **状态管理**：Pinia + 持久化插件
- **样式**：Tailwind CSS 4 + SCSS 主题系统
- **自动导入**：通过 unplugin-auto-import 和 unplugin-vue-components 自动导入 Vue API 和 Element Plus 组件

详细前端规范见 `manager/CLAUDE.md`。

## Git 提交规范

使用中文提交信息，格式：

```
feat: 简短中文描述
  -详细说明1
  -详细说明2

提交人：陈鸿昇
```

manager 项目使用 Husky + lint-staged + commitlint (Conventional Commits)。

## Claude Code 团队协作

本项目配置了 Agent Team 系统，可通过 `/team-start` 启动全栈开发团队：

- **backend-java-expert**：后端 Java 专家（senior-backend-engineer agent）
- **manager-frontend-expert**：管理后台前端专家（frontend-admin-dev agent）

团队名称：`Project-fullstack-team`。使用 `/team-cleanup` 清理团队资源。

## 可用 Skills

| Skill | 用途 |
|-------|------|
| `backend-java` | Java 后端编码规范 |
| `manager-frontend` | 管理后台 Vue 3 前端规范 |
| `git-commit-zh` | 中文 Git 提交信息格式 |
| `team-start` / `team-cleanup` | 启动/清理开发团队 |
| `ui-ux-pro-max` | UI/UX 设计辅助 |
