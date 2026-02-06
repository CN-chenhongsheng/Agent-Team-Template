<template>
  <view class="notice-detail-page">
    <!-- 背景装饰 -->
    <view class="bg-decorations">
      <view class="blob blob-1" />
      <view class="blob blob-2" />
      <view class="blob blob-3" />
    </view>

    <view class="page-container">
      <view v-if="loading" class="loading">
        <view class="loading-spinner" />
        <view class="loading-text">
          加载中...
        </view>
      </view>

      <view v-else-if="notice" class="glass-card notice-content">
        <view class="notice-header">
          <view class="title">
            {{ notice.title }}
          </view>
          <view class="meta">
            <view class="publisher">
              {{ notice.publisherName }}
            </view>
            <view class="time">
              {{ notice.publishTime }}
            </view>
          </view>
        </view>

        <view class="notice-body">
          <view class="content">
            {{ notice.content }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { getNoticeDetailAPI, markNoticeReadAPI } from '@/api';

const loading = ref(false);
const notice = ref<any>(null);
const noticeId = ref(0);

async function loadDetail() {
  loading.value = true;
  try {
    notice.value = await getNoticeDetailAPI(noticeId.value);
    // 标记为已读
    await markNoticeReadAPI(noticeId.value);
  }
  catch (error) {
    console.error('加载失败:', error);
    uni.showToast({ title: '加载失败', icon: 'none' });
  }
  finally {
    loading.value = false;
  }
}

onMounted(() => {
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1] as any;
  const options = currentPage.options || currentPage.$route.query;

  noticeId.value = Number(options.id);
  if (noticeId.value) {
    loadDetail();
  }
});
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.notice-detail-page {
  position: relative;
  min-height: 100vh;
  background: linear-gradient(135deg, $bg-gradient-start 0%, $bg-gradient-end 100%);
}

.page-container {
  position: relative;
  z-index: 1;
  padding: 24rpx;
  margin: 0 auto;
  max-width: 750rpx;
  min-height: 100vh;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 100rpx 0;
  font-size: $font-md;
  color: $text-sub;
  flex-direction: column;

  .loading-spinner {
    width: 60rpx;
    height: 60rpx;
    border: 4rpx solid rgb(243 243 243);
    border-top: 4rpx solid $primary;
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }

  .loading-text {
    margin-top: 20rpx;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.notice-content {
  padding: 0;

  .notice-header {
    padding: 40rpx 30rpx;
    border-bottom: 1rpx solid rgb(229 231 235 / 50%);

    .title {
      margin-bottom: 20rpx;
      font-size: $font-xl;
      color: $text-main;
      font-weight: $font-semibold;
      line-height: 1.5;
    }

    .meta {
      display: flex;
      align-items: center;
      font-size: $font-sm;
      color: $text-sub;

      .publisher {
        margin-right: 20rpx;
      }
    }
  }

  .notice-body {
    padding: 30rpx;

    .content {
      font-size: $font-md;
      white-space: pre-wrap;
      color: $text-sub;
      line-height: 1.8;
    }
  }
}
</style>
