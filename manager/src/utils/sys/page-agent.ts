/**
 * 接入 Page Agent（页内 GUI Agent）
 *
 * 内置底部悬浮 Panel 只是默认 UI。本项目用右侧 ArtChatWindow 作为输入面，
 * 因此创建后立即 dispose Panel，由聊天窗口调用 execute() 并监听事件。
 *
 * DeepSeek 官方接口没有浏览器 CORS，开发环境通过 Vite 把
 * `/deepseek-api` 代理到 `https://api.deepseek.com`。
 *
 * API Key 请写在 `.env.local`（已被 gitignore，且不要在 .env.development 里留空键，
 * 否则会覆盖 .env.local）。填好后必须重启 pnpm dev。
 */
import { ref } from 'vue'
import type { PageAgent } from 'page-agent'
import { mittBus } from '@/utils/sys'

let pageAgent: PageAgent | null = null

export type PageAgentAskUser = (
  question: string,
  options?: { signal: AbortSignal }
) => Promise<string>

let askUserHandler: PageAgentAskUser | null = null

/** 是否已完成初始化（供聊天窗口显示在线状态） */
export const pageAgentReady = ref(false)

export function getPageAgent() {
  return pageAgent
}

/**
 * 每次扫描 DOM 时再收集右侧抽屉节点。
 * PageController 用 includes(element) 判断黑名单，只排除根节点不够。
 */
const chatDrawerBlacklist = {
  *[Symbol.iterator](): IterableIterator<Element> {
    const roots = Array.from(
      document.querySelectorAll('.page-agent-chat-drawer, .page-agent-stop-fab')
    )
    for (const root of roots) {
      yield root
      yield* Array.from(root.querySelectorAll('*'))
    }
  }
}

/**
 * 由右侧聊天窗口注册「询问用户」实现。
 * 不注册时 ask_user 会退回 window.prompt。
 */
export function setPageAgentAskUser(handler: PageAgentAskUser | null) {
  askUserHandler = handler
  if (pageAgent) {
    pageAgent.onAskUser = handler ?? undefined
  }
}

export async function setupPageAgent() {
  const apiKey = import.meta.env.VITE_PAGE_AGENT_API_KEY?.trim()
  if (!apiKey) {
    console.warn(
      '[PageAgent] 未配置 VITE_PAGE_AGENT_API_KEY，已跳过初始化。请在 .env.local 填写 DeepSeek API Key 后重启 npm run dev。'
    )
    return
  }

  const { PageAgent } = await import('page-agent')

  pageAgent = new PageAgent({
    model: import.meta.env.VITE_PAGE_AGENT_MODEL || 'deepseek-v4-pro',
    baseURL: import.meta.env.VITE_PAGE_AGENT_BASE_URL || '/deepseek-api',
    apiKey,
    language: 'zh-CN',
    instructions: {
      system:
        '用户通过页面右侧聊天窗口下达任务。不要操作右侧聊天抽屉里的输入框、按钮或消息列表。表格操作列的查看、编辑、删除、更多是图标按钮，请根据 aria-label / title 识别。需要「分配权限」等收在更多里的操作时，先点击「更多」打开菜单，再点击对应菜单项。完成后用中文简短回复结果。'
    },
    // PageController 的黑名单是元素全等匹配，必须把抽屉内每个节点都列进去
    interactiveBlacklist: chatDrawerBlacklist as unknown as Element[]
  })

  // 内置 Panel 在 status=running 时会自动 show()。拆掉它，改用右侧聊天窗口。
  pageAgent.panel.dispose()
  pageAgent.onAskUser = async (question, options) => {
    if (!askUserHandler) {
      return window.prompt(question) || ''
    }
    return askUserHandler(question, options)
  }

  pageAgentReady.value = true
  window.pageAgent = pageAgent
  mittBus.emit('pageAgentReady')
  console.info('[PageAgent] 已就绪。点击顶部栏聊天图标，在右侧窗口输入任务。')
}

declare global {
  interface Window {
    pageAgent?: PageAgent
  }
}
