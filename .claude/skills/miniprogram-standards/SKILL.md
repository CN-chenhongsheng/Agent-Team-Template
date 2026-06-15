---
name: miniprogram-standards
description: Enforces UniApp miniprogram architecture and module conventions (folder+index, API/types alignment, form entry consolidation, constants/build boundaries). Use when working in miniprogram/, refactoring structure, adding APIs, services, composables, or components.
---

# Miniprogram Standards

## Scope
- Applies to `miniprogram/` only.
- Use for any structural change, new module, or refactor.
- Do **not** add docs/tests unless the user explicitly requests.

## Folder + index Rules
- `src/api/<module>/index.ts`
  - `api/common` and `api/user` may keep `types.ts` inside their folder.
- `src/utils/<module>/index.ts`
- `src/services/<module>/index.ts`
- `src/composables/useXxx/index.ts`
- `src/components/<component>/index.vue`
- `src/utils/request/<part>/index.ts` for `interceptors/`, `status/`, `type/`

## Barrel Exports (must stay complete)
- `src/api/index.ts` exports all public API modules.
- `src/utils/index.ts` exports all utils submodules.
- `src/services/index.ts` exports all services submodules.
- `src/composables/index.ts` exports all composables.
- `src/components/index.ts` exports all public components.

## API ↔ Types Alignment
- API modules **must not** define interfaces inline.
- Define API types under `src/types/api/` and import from `@/types/api`.
- Keep `types/api/index.ts` exporting all API types.

## Components
- Base components live in `components/base/`, business components in `components/business/`.
- Public components are exported from `components/index.ts`.
- Local page components stay under `pages/**/components/` and use relative imports.

## Form Entry Consolidation
- All apply forms must go through `pages/apply/form/index.vue`.
- Support `form?type=checkIn|checkOut|stay|transfer|repair`.
- Do **not** create new `pages/apply/check-in|check-out|stay|transfer` pages.
  - If legacy paths are required, use redirect or a thin wrapper.

## Constants Boundary
- `src/constants/index.ts`: business constants only (routes, UI, validation, storage keys).
- `build/constant.ts` or `.env`: environment/build config (BaseURL, API prefix, mock).

## Request Module
- `utils/request/index.ts` only orchestrates and exports.
- `interceptors`, `status`, `type` each live in their own folder with `index.ts`.

## Naming
- Files/folders: kebab-case (`apply-card/`, `use-submit/`).
- Components: PascalCase (`ApplyCard`, `UserCard`).
- Composables: `useXxx` only.
- Functions/variables: camelCase.
- Constants: UPPER_SNAKE_CASE.
- API functions: `verbNounAPI` (e.g., `getDormInfoAPI`, `submitCheckInAPI`).

## Import Priority
1. Vue/UniApp core
2. Third-party libs
3. `@/constants`, `@/utils`
4. `@/types`, `@/types/api`
5. `@/api`
6. `@/services`
7. `@/composables`
8. `@/components`
9. Relative imports (local page components only)

## Page Structure Constraints
- Page SFCs must stay under 500 lines; split into `pages/**/components/` and composables when larger.
- Business logic goes to `services`, UI logic to `composables`.
- Repeated UI must be extracted to `components/base` or `components/business`.
- Page-local components must live under `pages/**/components/` and use relative imports.
