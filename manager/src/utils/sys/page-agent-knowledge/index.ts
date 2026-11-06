/**
 * PageAgent 知识注入层：把项目知识（全局速览 + 页面手册）组装进 agent 配置。
 * 知识内容本身维护在 global.md / pages.ts，本文件只做拼装与路由匹配。
 */
import globalKnowledge from './global.md?raw'
import { PAGE_KNOWLEDGE } from './pages'

/** 基础行为指令与项目知识之间的分隔 */
const SECTION_SEPARATOR = '\n\n---\n\n'

/** 空 hash 或根路径时的归一化目标（首页） */
const DEFAULT_PATH = '/dashboard/console'

/**
 * 从完整 URL 中提取归一化的 hash 路由路径。
 * 例：'http://localhost:3006/#/system/user?tab=1' → '/system/user'
 */
export function normalizeRoutePath(url: string): string {
  const hash = url.split('#')[1] ?? ''
  const path = (hash.split('?')[0] ?? '').replace(/\/+$/, '')
  if (!path || path === '/') {
    return DEFAULT_PATH
  }
  return path
}

/**
 * 组装全局系统指令：基础行为指令 + 项目知识速览。
 */
export function buildSystemInstructions(base: string): string {
  return `${base}${SECTION_SEPARATOR}${globalKnowledge.trim()}`
}

/**
 * 获取当前页面的知识说明。精确匹配失败时逐级前缀回退
 * （如 /system/user → /system），仍无匹配返回 undefined（不注入）。
 */
export function getPageInstructions(url: string): string | undefined {
  let path = normalizeRoutePath(url)
  while (path) {
    const hit = PAGE_KNOWLEDGE[path]
    if (hit) {
      return hit
    }
    const idx = path.lastIndexOf('/')
    if (idx <= 0) {
      break
    }
    path = path.slice(0, idx)
  }
  return undefined
}
