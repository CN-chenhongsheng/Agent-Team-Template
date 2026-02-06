/**
 * @name Config
 * @description 构建与运行环境相关配置
 *
 * 注意：当前项目实际使用 .env 文件中的环境变量（VITE_APP_BASE_API 等）
 * 此文件保留用于构建时配置，如需使用请取消注释并更新引用
 *
 * 实际配置位置：
 * - 开发环境：.env.development
 * - 生产环境：.env.production
 * - 通用配置：.env
 */

// prefix
export const API_PREFIX = '/api';

// serve
export const API_BASE_URL = '/api';
export const API_TARGET_URL = 'http://localhost:8080';

// mock
export const MOCK_API_BASE_URL = '/mock/api';
export const MOCK_API_TARGET_URL = 'http://localhost:3000';
