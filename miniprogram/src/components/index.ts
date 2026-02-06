/**
 * 组件统一导出入口
 *
 * 说明：
 * - 此文件导出所有公共组件（base、business、其他零散组件）
 * - 组件可通过 @/components 统一导入，或通过 easycom 自动引入
 * - pages 下的 components 目录为页面内局部组件，不纳入此统一导出
 *   局部组件应通过相对路径引用，保持页面内封装
 */

// 基础组件
export { default as GlassCard } from './base/glass-card/index.vue';
export { default as PageHeader } from './base/page-header/index.vue';
export { default as StatusTag } from './base/status-tag/index.vue';
export { default as EmptyState } from './base/empty-state/index.vue';

// 业务组件
export { default as UserCard } from './business/user-card/index.vue';
export { default as ApplyCard } from './business/apply-card/index.vue';
export { default as NoticeCard } from './business/notice-card/index.vue';

// 其他组件
export { default as AgreePrivacy } from './agree-privacy/index.vue';
export { default as PageNav } from './page-nav/index.vue';
export { default as QiunUcharts } from './qiun-ucharts/index.vue';
