<template>
  <span class="art-status-dot" :class="[`art-status-dot--${resolvedType}`]">
    <span class="art-status-dot__indicator" />
    <span class="art-status-dot__text">{{ displayText }}</span>
  </span>
</template>

<script setup lang="ts">
  defineOptions({ name: 'ArtStatusDot' })

  export type StatusDotType = 'success' | 'danger' | 'warning' | 'info' | 'primary'

  export interface StatusDotMap {
    [key: string]: { text: string; type: StatusDotType } | undefined
  }

  const props = withDefaults(
    defineProps<{
      /** 显示文本 */
      text?: string
      /** 状态类型，控制颜色 */
      type?: StatusDotType
      /** 值 + 映射模式：当前值 */
      value?: string | number | boolean
      /** 值 + 映射模式：值到 { text, type } 的映射 */
      map?: StatusDotMap
    }>(),
    {
      text: '',
      type: 'info',
      value: undefined,
      map: undefined
    }
  )

  const resolvedType = computed(() => {
    if (props.map && props.value !== undefined) {
      const key = String(props.value)
      return props.map[key]?.type ?? props.type
    }
    return props.type
  })

  const resolvedText = computed(() => {
    if (props.map && props.value !== undefined) {
      const key = String(props.value)
      return props.map[key]?.text ?? props.text
    }
    return props.text
  })

  // 暴露给模板
  const displayText = resolvedText
</script>

<style scoped lang="scss">
  .art-status-dot {
    display: inline-flex;
    gap: 6px;
    align-items: center;
    font-weight: 500;
    border-radius: 9999px;

    &__indicator {
      position: relative;
      display: inline-block;
      flex-shrink: 0;
      width: 7px;
      height: 7px;
      border-radius: 50%;

      // 外圈呼吸光晕
      &::after {
        position: absolute;
        inset: -3px;
        content: '';
        border-radius: 50%;
        opacity: 0.4;
      }
    }

    &__text {
      font-size: 13px;
      line-height: 1.4;
      letter-spacing: 0.01em;
    }

    // ========== 配色方案 ==========

    &--success {
      .art-status-dot__indicator {
        background-color: #10b981;
        box-shadow: 0 0 0 2px rgb(16 185 129 / 20%);
      }

      .art-status-dot__text {
        color: #059669;
      }
    }

    &--danger {
      .art-status-dot__indicator {
        background-color: #ef4444;
        box-shadow: 0 0 0 2px rgb(239 68 68 / 20%);
      }

      .art-status-dot__text {
        color: #dc2626;
      }
    }

    &--warning {
      .art-status-dot__indicator {
        background-color: #f59e0b;
        box-shadow: 0 0 0 2px rgb(245 158 11 / 20%);
      }

      .art-status-dot__text {
        color: #d97706;
      }
    }

    &--info {
      .art-status-dot__indicator {
        background-color: #9ca3af;
        box-shadow: 0 0 0 2px rgb(156 163 175 / 20%);
      }

      .art-status-dot__text {
        color: #6b7280;
      }
    }

    &--primary {
      .art-status-dot__indicator {
        background-color: #3b82f6;
        box-shadow: 0 0 0 2px rgb(59 130 246 / 20%);
      }

      .art-status-dot__text {
        color: #2563eb;
      }
    }
  }

  // ========== 暗色主题适配 ==========
  :global(.dark) {
    .art-status-dot {
      &--success {
        background-color: rgb(16 185 129 / 12%);

        .art-status-dot__text {
          color: #34d399;
        }
      }

      &--danger {
        background-color: rgb(239 68 68 / 12%);

        .art-status-dot__text {
          color: #f87171;
        }
      }

      &--warning {
        background-color: rgb(245 158 11 / 12%);

        .art-status-dot__text {
          color: #fbbf24;
        }
      }

      &--info {
        background-color: rgb(107 114 128 / 15%);

        .art-status-dot__text {
          color: #9ca3af;
        }
      }

      &--primary {
        background-color: rgb(59 130 246 / 12%);

        .art-status-dot__text {
          color: #60a5fa;
        }
      }
    }
  }
</style>
