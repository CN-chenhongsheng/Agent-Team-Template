<!-- 系统聊天窗口：右侧抽屉，作为 Page Agent 的输入面（替代内置底部悬浮条） -->
<template>
  <div>
    <Teleport to="body">
      <div
        v-if="showStopFab"
        class="page-agent-stop-fab"
        data-page-agent-not-interactive="true"
        :style="{
          position: 'fixed',
          right: '24px',
          bottom: '24px',
          zIndex: DRAWER_Z_INDEX
        }"
      >
        <ElButton type="danger" @click="stopTask">{{ $t('system.pageAgent.stop') }}</ElButton>
      </div>
    </Teleport>
    <ElDrawer
      v-model="isDrawerVisible"
      class="page-agent-chat-drawer"
      :size="isMobile ? '100%' : '480px'"
      :with-header="false"
      :modal="false"
      :close-on-click-modal="false"
      destroy-on-close
      :z-index="DRAWER_Z_INDEX"
      @closed="onDrawerClosed"
    >
      <div class="flex h-full flex-col" data-page-agent-not-interactive="true">
        <div class="mb-5 flex-cb">
          <div>
            <span class="text-base font-medium">{{ $t('system.pageAgent.title') }}</span>
            <div class="mt-1.5 flex-c gap-1">
              <div class="h-2 w-2 rounded-full" :class="statusDotClass"></div>
              <span class="text-xs text-g-600">{{ statusLabel }}</span>
            </div>
          </div>
          <div class="flex-c gap-2">
            <ElButton v-if="isRunning" type="danger" size="small" plain @click="stopTask">
              {{ $t('system.pageAgent.stop') }}
            </ElButton>
            <ElIcon class="c-p" :size="20" @click="closeChat">
              <Close />
            </ElIcon>
          </div>
        </div>
        <div class="flex h-[calc(100%-70px)] flex-col">
          <!-- 聊天消息区域 -->
          <div
            class="flex-1 overflow-y-auto overflow-x-hidden border-t-d px-4 py-7.5 [&::-webkit-scrollbar]:!w-1"
            ref="messageContainer"
          >
            <template v-for="(message, index) in messages" :key="index">
              <div
                :class="[
                  'mb-7.5 flex w-full items-start gap-2',
                  message.isMe ? 'flex-row-reverse' : 'flex-row'
                ]"
              >
                <ElAvatar :size="32" :src="message.avatar" class="shrink-0" />
                <div
                  :class="[
                    'flex min-w-0 max-w-[70%] flex-col',
                    message.isMe ? 'items-end' : 'items-start'
                  ]"
                >
                  <div
                    :class="[
                      'mb-1 flex gap-2 text-xs',
                      message.isMe ? 'flex-row-reverse' : 'flex-row'
                    ]"
                  >
                    <span class="font-medium">{{ message.sender }}</span>
                    <span class="text-g-600">{{ message.time }}</span>
                  </div>
                  <div
                    :class="[
                      'max-w-full min-w-0 rounded-md px-3.5 py-2.5 text-sm leading-[1.4] text-g-900 whitespace-pre-wrap break-all',
                      message.isMe ? 'message-right bg-theme/15' : 'message-left bg-g-300/50'
                    ]"
                  >
                    {{ message.content }}
                  </div>
                </div>
              </div>
            </template>
            <div v-if="isRunning && activityText" class="text-xs text-g-600">
              {{ activityText }}
            </div>
          </div>

          <!-- 聊天输入区域 -->
          <div class="px-4 pt-4">
            <ElInput
              v-model="messageText"
              type="textarea"
              :rows="3"
              :placeholder="inputPlaceholder"
              resize="none"
              @keydown.enter.exact.prevent="sendMessage"
            />
            <div class="mt-3 flex-cb">
              <div class="flex-c">
                <ArtSvgIcon icon="ri:image-line" class="mr-5 c-p text-g-600 text-lg" />
                <ArtSvgIcon icon="ri:emotion-happy-line" class="mr-5 c-p text-g-600 text-lg" />
              </div>
              <ElButton
                v-if="isRunning && !pendingAsk"
                type="danger"
                class="min-w-20"
                @click="stopTask"
                v-ripple
              >
                {{ $t('system.pageAgent.stop') }}
              </ElButton>
              <ElButton
                v-else
                type="primary"
                class="min-w-20"
                :disabled="!messageText.trim()"
                @click="sendMessage"
                v-ripple
              >
                {{ $t('system.pageAgent.send') }}
              </ElButton>
            </div>
          </div>
        </div>
      </div>
    </ElDrawer>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import { Close } from '@element-plus/icons-vue'
  import type { AgentActivity } from 'page-agent'
  import { mittBus } from '@/utils/sys'
  import { getPageAgent, pageAgentReady, setPageAgentAskUser } from '@/utils/sys/page-agent'
  import meAvatar from '@/assets/images/avatar/avatar5.webp'
  import aiAvatar from '@/assets/images/avatar/avatar10.webp'

  defineOptions({ name: 'ArtChatWindow' })

  const { t } = useI18n()

  interface ChatMessage {
    id: number
    sender: string
    content: string
    time: string
    isMe: boolean
    avatar: string
  }

  type PendingAsk = {
    resolve: (answer: string) => void
    reject: (reason?: unknown) => void
  }

  /** 高于 Page Agent SimulatorMask（2147483641），任务执行时仍可在右侧输入 */
  const DRAWER_Z_INDEX = 2147483643
  const MOBILE_BREAKPOINT = 640
  const SCROLL_DELAY = 100
  const BOT_NAME = computed(() => t('system.pageAgent.botName'))
  const USER_NAME = computed(() => t('system.pageAgent.userName'))

  function formatCurrentTime(): string {
    return new Date().toLocaleTimeString([], {
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const { width } = useWindowSize()
  const isMobile = computed(() => width.value < MOBILE_BREAKPOINT)

  const isDrawerVisible = ref(false)
  const isRunning = ref(false)
  const activityText = ref('')
  const messageText = ref('')
  const messageId = ref(2)
  const messageContainer = ref<HTMLElement | null>(null)
  const pendingAsk = ref<PendingAsk | null>(null)

  const messages = ref<ChatMessage[]>([
    {
      id: 1,
      sender: BOT_NAME.value,
      content: t('system.pageAgent.greeting'),
      time: formatCurrentTime(),
      isMe: false,
      avatar: aiAvatar
    }
  ])

  const statusLabel = computed(() => {
    if (!pageAgentReady.value) return t('system.pageAgent.statusOffline')
    if (isRunning.value) return activityText.value || t('system.pageAgent.statusRunning')
    return t('system.pageAgent.statusOnline')
  })

  const statusDotClass = computed(() => {
    if (!pageAgentReady.value) return 'bg-danger/100'
    if (isRunning.value) return 'bg-warning/100'
    return 'bg-success/100'
  })

  const inputPlaceholder = computed(() => {
    if (pendingAsk.value) return t('system.pageAgent.inputAsk')
    if (isRunning.value) return t('system.pageAgent.inputRunning')
    return t('system.pageAgent.inputIdle')
  })

  const showStopFab = computed(() => isRunning.value && !isDrawerVisible.value && !pendingAsk.value)

  let drawerClosedResolver: (() => void) | null = null

  const onDrawerClosed = (): void => {
    drawerClosedResolver?.()
    drawerClosedResolver = null
  }

  const closeDrawerAndWait = async (): Promise<void> => {
    if (!isDrawerVisible.value) return

    await new Promise<void>((resolve) => {
      drawerClosedResolver = resolve
      isDrawerVisible.value = false
      window.setTimeout(() => {
        if (drawerClosedResolver === resolve) {
          drawerClosedResolver = null
          resolve()
        }
      }, 500)
    })
  }

  const scrollToBottom = (): void => {
    nextTick(() => {
      setTimeout(() => {
        if (messageContainer.value) {
          messageContainer.value.scrollTop = messageContainer.value.scrollHeight
        }
      }, SCROLL_DELAY)
    })
  }

  const pushMessage = (content: string, isMe: boolean): void => {
    messages.value.push({
      id: messageId.value++,
      sender: isMe ? USER_NAME.value : BOT_NAME.value,
      content,
      time: formatCurrentTime(),
      isMe,
      avatar: isMe ? meAvatar : aiAvatar
    })
    scrollToBottom()
  }

  const formatActivity = (activity: AgentActivity): string => {
    switch (activity.type) {
      case 'thinking':
        return t('system.pageAgent.thinking')
      case 'executing':
        return t('system.pageAgent.executing', { tool: activity.tool })
      case 'executed':
        return activity.output
      case 'retrying':
        return t('system.pageAgent.retrying', {
          attempt: activity.attempt,
          max: activity.maxAttempts
        })
      case 'error':
        return activity.message
      default:
        return ''
    }
  }

  const onStatusChange = (): void => {
    const agent = getPageAgent()
    isRunning.value = agent?.status === 'running'
    if (!isRunning.value) {
      activityText.value = ''
    }
  }

  const onActivity = (event: Event): void => {
    const activity = (event as CustomEvent<AgentActivity>).detail
    activityText.value = formatActivity(activity)
    scrollToBottom()
  }

  const bindAgentEvents = (): void => {
    const agent = getPageAgent()
    if (!agent) return
    agent.removeEventListener('statuschange', onStatusChange)
    agent.removeEventListener('activity', onActivity)
    agent.addEventListener('statuschange', onStatusChange)
    agent.addEventListener('activity', onActivity)
    onStatusChange()
  }

  const sendMessage = async (): Promise<void> => {
    const text = messageText.value.trim()
    if (!text) return

    if (pendingAsk.value) {
      const ask = pendingAsk.value
      pendingAsk.value = null
      messageText.value = ''
      pushMessage(text, true)
      await closeDrawerAndWait()
      ask.resolve(text)
      return
    }

    const agent = getPageAgent()
    if (!agent) {
      ElMessage.warning(t('system.pageAgent.notReady'))
      return
    }

    if (agent.status === 'running') {
      ElMessage.warning(t('system.pageAgent.busy'))
      return
    }

    messageText.value = ''
    pushMessage(text, true)
    await closeDrawerAndWait()

    try {
      const result = await agent.execute(text)
      const summary =
        result.data?.trim() ||
        (result.success ? t('system.pageAgent.taskDone') : t('system.pageAgent.taskNotDone'))
      pushMessage(summary, false)
    } catch (error) {
      const message = error instanceof Error ? error.message : String(error)
      pushMessage(message, false)
    } finally {
      openChat()
    }
  }

  const stopTask = async (): Promise<void> => {
    const agent = getPageAgent()
    if (!agent || agent.status !== 'running') return
    await agent.stop()
  }

  const openChat = (): void => {
    isDrawerVisible.value = true
    bindAgentEvents()
    scrollToBottom()
  }

  const closeChat = (): void => {
    isDrawerVisible.value = false
  }

  const handleAskUser: (question: string, options?: { signal: AbortSignal }) => Promise<string> = (
    question,
    options
  ) => {
    openChat()
    pushMessage(question, false)

    return new Promise((resolve, reject) => {
      const abort = () => {
        if (!pendingAsk.value) return
        pendingAsk.value = null
        reject(options?.signal.reason ?? new DOMException('Aborted', 'AbortError'))
      }

      pendingAsk.value = { resolve, reject }
      options?.signal.addEventListener('abort', abort, { once: true })
    })
  }

  onMounted(() => {
    scrollToBottom()
    mittBus.on('openChat', openChat)
    mittBus.on('pageAgentReady', bindAgentEvents)
    setPageAgentAskUser(handleAskUser)
    bindAgentEvents()
  })

  onUnmounted(() => {
    const agent = getPageAgent()
    agent?.removeEventListener('statuschange', onStatusChange)
    agent?.removeEventListener('activity', onActivity)
    setPageAgentAskUser(null)
    mittBus.off('openChat', openChat)
    mittBus.off('pageAgentReady', bindAgentEvents)
  })
</script>
