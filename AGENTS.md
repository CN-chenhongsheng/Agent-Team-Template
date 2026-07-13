# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## 项目概述

通用后台管理模板（Admin Management Template），采用前后端分离的 monorepo 架构，包含两个独立子项目：

| 模块 | 路径 | 技术栈 | 用途 |
|------|------|--------|------|
| **Application** | `Application/` | Java 21 + Spring Boot 3.2 + MyBatis-Plus + MySQL + Redis | 后端 API 服务 |
| **manager** | `manager/` | Vue 3 + TypeScript + Element Plus + Tailwind CSS 4 + Vite | 管理后台 Web 端 |

当前内置能力：认证登录、用户/角色/菜单/字典、操作日志、登录日志、文件上传、通用导入等系统管理能力。不包含具体行业业务模块。

## 常用命令

### 后端 (Application/)

```bash
cd Application && mvn spring-boot:run
cd Application && mvn compile
cd Application && mvn package -DskipTests
cd Application && mvn test -Dmaven.test.skip=false
```

### 管理后台 (manager/)

```bash
cd manager && pnpm install
cd manager && pnpm dev
cd manager && pnpm build
cd manager && pnpm lint
```

## 后端架构 (Application)

### 两层包结构

```
com.project.core/     — 基础设施层
com.project.backend/  — 管理后台 API 层
```

**依赖方向**：`backend → core`。

### 现有模块

`system`（系统管理）、`common`（公共能力）。

### 关键技术组件

- **认证授权**：Sa-Token + Redis；接口使用 `@SaCheckPermission` 校验权限码
- **ORM**：MyBatis-Plus
- **统一响应**：`R<T>` + `PageResult<T>`
- **数据库**：MySQL `project_management`

## 管理后台架构 (manager)

基于 Art Design Pro 模板。详细规范见 `manager/AGENTS.md`。

## Git 提交规范

```
feat: 简短中文描述
  -详细说明1

提交人：陈鸿昇
```

## Codex 团队协作

- `/team-start` 启动全栈开发团队
- `/team-cleanup` 清理团队资源

## 可用 Skills

| Skill | 用途 |
|-------|------|
| `backend-java` | Java 后端编码规范 |
| `manager-frontend` | 管理后台 Vue 3 前端规范 |
| `git-commit-zh` | 中文 Git 提交信息格式 |
| `team-start` / `team-cleanup` | 启动/清理开发团队 |
