# manager 前端规范

本目录为管理后台前端项目，基于 Art Design Pro（`art-design-pro`）。

## 技术栈

Vue 3 + TypeScript + Element Plus + Tailwind CSS 4 + Vite + Pinia

## 核心约定

- **表格**：`useTable` + `ArtTable`，禁止直接使用 `el-table`
- **表单**：`ArtForm` 配置式表单
- **API**：`src/api/` 下 `fetchXxxApi` 命名；类型放在 `src/types/api/`
- **路由**：动态路由 + `router/core/` 权限守卫
- **字典**：统一使用 `useDictStore`，禁止直接调字典 API

完整规范见仓库根目录 `.claude/skills/manager-frontend/SKILL.md`。

## 常用命令

```bash
pnpm install
pnpm dev
pnpm build
pnpm lint
```
