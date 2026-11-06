<template>
  <canvas
    ref="canvasRef"
    :width="canvasSize"
    :height="canvasSize"
    :style="{ width: `${size}px`, height: `${size}px` }"
    class="rounded-full"
    aria-hidden="true"
  />
</template>

<script setup lang="ts">
  import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
  import botAvatar from '@/assets/images/avatar/page-agent-bot.svg'

  defineOptions({ name: 'AiAvatarCanvas' })

  interface Props {
    /** 头像显示尺寸（px），内部按 devicePixelRatio 放大绘制保证清晰 */
    size?: number
  }

  const props = withDefaults(defineProps<Props>(), { size: 32 })

  const canvasRef = ref<HTMLCanvasElement | null>(null)
  const dpr = window.devicePixelRatio || 1
  const canvasSize = props.size * dpr

  let botImage: HTMLImageElement | null = null

  /**
   * 将开源的 Bottts SVG 头像绘制到 canvas（SVG 为矢量，任意尺寸清晰）。
   * 头像来源：DiceBear bottts 风格（设计师 Pablo Stanley，免费商用）。
   */
  function draw(): void {
    const canvas = canvasRef.value
    if (!canvas || !botImage) return
    const ctx = canvas.getContext('2d')
    if (!ctx) return

    const s = props.size
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
    ctx.clearRect(0, 0, s, s)
    ctx.drawImage(botImage, 0, 0, s, s)
  }

  onMounted(() => {
    botImage = new Image()
    botImage.onload = draw
    botImage.src = botAvatar
  })

  onBeforeUnmount(() => {
    if (botImage) {
      botImage.onload = null
      botImage = null
    }
  })

  watch(
    () => props.size,
    () => draw()
  )
</script>
