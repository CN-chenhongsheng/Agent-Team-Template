/**
 * 通知相关 Composable
 * @module composables/useNotice
 */

import { ref } from 'vue';
import { getUnreadNoticeCountAPI } from '@/api';

/**
 * 未读通知数量
 */
const unreadCount = ref(0);

/**
 * 使用通知功能
 */
export function useNotice() {
  /**
   * 加载未读数量
   */
  async function loadUnreadCount(): Promise<void> {
    try {
      const count = await getUnreadNoticeCountAPI();
      unreadCount.value = count;
    }
    catch (error) {
      console.error('获取未读通知数量失败', error);
    }
  }

  /**
   * 重置未读数量
   */
  function resetUnreadCount(): void {
    unreadCount.value = 0;
  }

  return {
    unreadCount,
    loadUnreadCount,
    resetUnreadCount,
  };
}
