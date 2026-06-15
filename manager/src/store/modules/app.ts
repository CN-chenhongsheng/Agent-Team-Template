/**
 * 应用全局状态管理
 *
 * @module store/modules/app
 * @author 陈鸿昇
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 系统初始化状态（首次加载用户信息和菜单数据时为 true）
  const initializing = ref(false)

  /**
   * 设置初始化状态
   */
  const setInitializing = (value: boolean) => {
    initializing.value = value
  }

  return {
    initializing,
    setInitializing
  }
})
