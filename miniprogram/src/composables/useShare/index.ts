/**
 * 小程序分享
 */
import { onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app';

interface UseShareOptions {
  title?: string;
  path?: string;
  query?: string;
  imageUrl?: string;
}

export function useShare(options?: UseShareOptions) {
  // #ifdef MP-WEIXIN
  const title = options?.title ?? '';
  const path = options?.path ?? '';
  const query = options?.query ?? '';
  const imageUrl = options?.imageUrl ?? '';
  onShareAppMessage(() => {
    return {
      title,
      path: path ? `${path}${query ? `?${query}` : ''}` : '',
      imageUrl,
    };
  });
  onShareTimeline(() => {
    return {
      title,
      query: options?.query ?? '',
      imageUrl,
    };
  });
  // #endif
}
