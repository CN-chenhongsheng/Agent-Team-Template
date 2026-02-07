<template>
  <view class="glass-card progress-card">
    <view class="progress-title">
      <u-icon name="clock-fill" size="20" color="#0adbc3" />
      <text>????</text>
    </view>

    <view class="progress-timeline">
      <view
        v-for="(step, index) in steps"
        :key="index"
        class="timeline-item"
      >
        <view class="timeline-icon-wrapper">
          <template v-if="step.completed">
            <u-icon name="checkmark-circle" size="24" color="#0adbc3" />
            <view
              v-if="index < steps.length - 1"
              class="timeline-line"
              :class="current > index ? 'timeline-line-active' : 'timeline-line-gray'"
            />
          </template>
          <template v-else-if="current === index">
            <view class="timeline-icon-pulse">
              <view class="pulse-circle-bg" />
              <view class="custom-radio-icon custom-radio-icon-active" />
            </view>
            <view v-if="index < steps.length - 1" class="timeline-line timeline-line-gray" />
          </template>
          <template v-else>
            <view class="custom-radio-icon custom-radio-icon-gray" />
            <view v-if="index < steps.length - 1" class="timeline-line timeline-line-gray" />
          </template>
        </view>

        <view class="timeline-content">
          <view class="timeline-header">
            <view class="timeline-title" :class="{ active: current === index, gray: current < index }">
              {{ step.title }}
            </view>
            <view v-if="current === index && !step.completed" class="timeline-status-badge">
              ????
            </view>
            <view v-else-if="step.time" class="timeline-time" :class="{ gray: current < index }">
              {{ step.time }}
            </view>
          </view>

          <view class="timeline-desc" :class="{ gray: current < index }">
            {{ step.desc }}
          </view>

          <view v-if="step.reviewer" class="timeline-reviewer">
            <image class="reviewer-avatar" :src="step.reviewer.avatar" mode="aspectFill" />
            <view class="reviewer-info">
              <text>{{ step.reviewer.name }}</text>
              <view class="reviewer-status" :class="step.action === 1 ? 'status-approved' : 'status-rejected'">
                {{ step.action === 1 ? '??' : '??' }}
              </view>
            </view>
          </view>

          <view v-if="step.reviewReason" class="review-reason-card">
            <view class="review-reason-triangle" />
            <view class="review-reason-header">
              <u-icon
                :name="step.action === 1 ? 'checkmark-circle-fill' : 'close-circle-fill'"
                size="16"
                :color="step.action === 1 ? '#0adbc3' : '#ff6b6b'"
              />
              <text>????</text>
            </view>
            <view class="review-reason-text">
              {{ step.reviewReason }}
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  steps: any[];
  current: number;
}>();
</script>

<style lang="scss" scoped>
.progress-card {
  padding: 40rpx 32rpx;
}

.progress-title {
  display: flex;
  align-items: center;
  margin-bottom: 40rpx;
  font-size: 32rpx;
  color: $text-main;
  gap: 16rpx;
  font-weight: 700;
}

.progress-timeline {
  padding-left: 4rpx;
}

.timeline-item {
  display: flex;
  gap: 24rpx;
  min-height: 120rpx;

  &:last-child {
    min-height: auto;
  }
}

.timeline-icon-wrapper {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 48rpx;
  flex-shrink: 0;
}

.timeline-line {
  margin: 8rpx 0;
  width: 4rpx;
  min-height: 40rpx;
  border-radius: 2rpx;
  flex: 1;

  &.timeline-line-active {
    background: linear-gradient(to bottom, $primary, rgb(10 219 195 / 30%));
  }

  &.timeline-line-gray {
    background: #e2e8f0;
  }
}

.timeline-icon-pulse {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}

.pulse-circle-bg {
  position: absolute;
  width: 36rpx;
  height: 36rpx;
  background: rgb(10 219 195 / 20%);
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

// @keyframes pulse ???? components.scss ???????????????????

.custom-radio-icon {
  width: 24rpx;
  height: 24rpx;
  border: 4rpx solid;
  border-radius: 50%;

  &.custom-radio-icon-active {
    background: $primary;
    border-color: $primary;
    box-shadow: 0 0 12rpx rgb(10 219 195 / 40%);
  }

  &.custom-radio-icon-gray {
    background: #f1f5f9;
    border-color: #cbd5e1;
  }
}

.timeline-content {
  flex: 1;
  padding-bottom: 32rpx;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.timeline-title {
  font-size: 28rpx;
  color: $text-main;
  font-weight: 600;

  &.active { color: $primary; }
  &.gray { color: $text-disabled; }
}

.timeline-status-badge {
  padding: 4rpx 16rpx;
  font-size: 22rpx;
  color: $primary;
  background: rgb(10 219 195 / 10%);
  border-radius: 20rpx;
  font-weight: 600;
}

.timeline-time {
  font-size: 24rpx;
  color: $text-sub;

  &.gray { color: $text-disabled; }
}

.timeline-desc {
  font-size: 24rpx;
  color: $text-sub;
  line-height: 1.6;

  &.gray { color: $text-disabled; }
}

.timeline-reviewer {
  display: flex;
  align-items: center;
  padding: 16rpx 20rpx;
  margin-top: 16rpx;
  background: rgb(10 219 195 / 5%);
  border-radius: 16rpx;
  gap: 16rpx;

  .reviewer-avatar {
    width: 56rpx;
    height: 56rpx;
    background: #e2e8f0;
    border-radius: 50%;
  }

  .reviewer-info {
    display: flex;
    align-items: center;
    font-size: 26rpx;
    color: $text-main;
    gap: 12rpx;
  }

  .reviewer-status {
    padding: 2rpx 12rpx;
    font-size: 22rpx;
    border-radius: 8rpx;
    font-weight: 600;

    &.status-approved {
      color: $primary;
      background: rgb(10 219 195 / 10%);
    }

    &.status-rejected {
      color: #ff6b6b;
      background: rgb(255 107 107 / 10%);
    }
  }
}

.review-reason-card {
  position: relative;
  padding: 20rpx 24rpx;
  margin-top: 16rpx;
  background: #f8fafc;
  border: 2rpx solid #e2e8f0;
  border-radius: 16rpx;

  .review-reason-triangle {
    position: absolute;
    top: -12rpx;
    left: 24rpx;
    width: 0;
    height: 0;
    border-left: 12rpx solid transparent;
    border-right: 12rpx solid transparent;
    border-bottom: 12rpx solid #e2e8f0;
  }

  .review-reason-header {
    display: flex;
    align-items: center;
    margin-bottom: 8rpx;
    font-size: 24rpx;
    color: $text-sub;
    gap: 8rpx;
    font-weight: 600;
  }

  .review-reason-text {
    font-size: 26rpx;
    color: $text-main;
    line-height: 1.6;
  }
}
</style>
