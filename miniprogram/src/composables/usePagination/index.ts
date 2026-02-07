/**
 * 通用分页 Composable
 * @module composables/usePagination
 */

import { type Ref, ref } from 'vue';

export interface PaginationResult<T> {
  list: T[];
  total: number;
}

export interface UsePaginationOptions<T> {
  /** 数据获取函数，接收 pageNum 和 pageSize，返回 { list, total } */
  fetchFn: (pageNum: number, pageSize: number) => Promise<PaginationResult<T>>;
  /** 每页大小，默认 20 */
  pageSize?: number;
  /** 是否立即加载，默认 false */
  immediate?: boolean;
}

export interface UsePaginationReturn<T> {
  /** 数据列表 */
  list: Ref<T[]>;
  /** 是否正在加载 */
  loading: Ref<boolean>;
  /** 是否正在刷新 */
  refreshing: Ref<boolean>;
  /** 是否已加载完全部数据 */
  finished: Ref<boolean>;
  /** 总数 */
  total: Ref<number>;
  /** 当前页码 */
  pageNum: Ref<number>;
  /** 加载更多（上拉加载） */
  loadMore: () => Promise<void>;
  /** 刷新（下拉刷新） */
  refresh: () => Promise<void>;
  /** 重置（切换 tab 等场景） */
  reset: () => void;
}

/**
 * 通用分页逻辑
 * @param options 分页配置
 */
export function usePagination<T>(options: UsePaginationOptions<T>): UsePaginationReturn<T> {
  const { fetchFn, pageSize = 20, immediate = false } = options;

  const list = ref<T[]>([]) as Ref<T[]>;
  const loading = ref(false);
  const refreshing = ref(false);
  const finished = ref(false);
  const total = ref(0);
  const pageNum = ref(1);

  async function loadData(isRefresh = false): Promise<void> {
    if (loading.value) return;

    loading.value = true;
    try {
      const result = await fetchFn(pageNum.value, pageSize);

      if (isRefresh) {
        list.value = result.list;
      }
      else {
        list.value = [...list.value, ...result.list];
      }

      total.value = result.total;
      finished.value = list.value.length >= result.total;
    }
    catch (error) {
      console.error('[usePagination] 加载失败:', error);
      if (isRefresh || list.value.length === 0) {
        list.value = [];
        total.value = 0;
      }
    }
    finally {
      loading.value = false;
      refreshing.value = false;
    }
  }

  async function loadMore(): Promise<void> {
    if (finished.value || loading.value) return;
    pageNum.value += 1;
    await loadData();
  }

  async function refresh(): Promise<void> {
    refreshing.value = true;
    pageNum.value = 1;
    finished.value = false;
    await loadData(true);
  }

  function reset(): void {
    list.value = [];
    pageNum.value = 1;
    total.value = 0;
    finished.value = false;
    loading.value = false;
    refreshing.value = false;
  }

  if (immediate) {
    loadData(true);
  }

  return {
    list,
    loading,
    refreshing,
    finished,
    total,
    pageNum,
    loadMore,
    refresh,
    reset,
  };
}
