<template>
  <view class="section">
    <view class="section-title">
      水电统计
    </view>
    <view class="utility-stats">
      <!-- 用电统计 -->
      <view class="glass-card utility-card">
        <view class="utility-bg-blob utility-bg-electric" />
        <view class="utility-header">
          <view class="utility-info">
            <view class="utility-title-row">
              <view class="utility-icon-wrapper utility-icon-electric">
                <u-icon name="grid" size="18" color="#0adbc3" />
              </view>
              <text class="utility-title">
                本年用电
              </text>
            </view>
            <view class="utility-value-row">
              <text class="utility-value">
                {{ electricity.value }}
              </text>
              <text class="utility-unit">
                kWh
              </text>
            </view>
          </view>
          <view class="utility-trend" :class="electricity.trendClass">
            <u-icon :name="electricity.trendIcon" size="14" :color="electricity.trendColor" />
            <text>{{ electricity.trendText }}</text>
          </view>
        </view>
        <view class="utility-chart">
          <view class="chart-container">
            <qiun-ucharts type="line" :opts="electricityChartOpts" :chart-data="electricityChartData" canvas-id="electricity-chart" />
          </view>
        </view>
      </view>

      <!-- 用水统计 -->
      <view class="glass-card utility-card">
        <view class="utility-bg-blob utility-bg-water" />
        <view class="utility-header">
          <view class="utility-info">
            <view class="utility-title-row">
              <view class="utility-icon-wrapper utility-icon-water">
                <u-icon name="grid" size="18" color="#60a5fa" />
              </view>
              <text class="utility-title">
                本年用水
              </text>
            </view>
            <view class="utility-value-row">
              <text class="utility-value">
                {{ water.value }}
              </text>
              <text class="utility-unit">
                m³
              </text>
            </view>
          </view>
          <view class="utility-trend" :class="water.trendClass">
            <u-icon :name="water.trendIcon" size="14" :color="water.trendColor" />
            <text>{{ water.trendText }}</text>
          </view>
        </view>
        <view class="utility-chart">
          <view class="chart-container">
            <qiun-ucharts type="line" :opts="waterChartOpts" :chart-data="waterChartData" canvas-id="water-chart" />
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  electricity: { value: string; trendText: string; trendClass: string; trendIcon: string; trendColor: string };
  water: { value: string; trendText: string; trendClass: string; trendIcon: string; trendColor: string };
  electricityChartOpts: any;
  electricityChartData: any;
  waterChartOpts: any;
  waterChartData: any;
}>();
</script>

<style lang="scss" scoped>
.section {
  margin-bottom: $spacing-lg;
}

.section-title {
  margin-bottom: 20rpx;
  font-size: $font-xl;
  color: $text-main;
  font-weight: $font-bold;
}

.utility-stats {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.utility-card {
  position: relative;
  overflow: visible;
  padding: $spacing-lg;
}

.utility-bg-blob {
  position: absolute;
  top: -96rpx;
  right: -96rpx;
  z-index: 0;
  width: 320rpx;
  height: 320rpx;
  border-radius: 50%;
  filter: blur(64rpx);
  pointer-events: none;

  &.utility-bg-electric { background: rgb(10 219 195 / 10%); }
  &.utility-bg-water { background: rgb(96 165 250 / 10%); }
}

.utility-header {
  position: relative;
  z-index: 10;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-md;
}

.utility-info {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.utility-title-row {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.utility-icon-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12rpx;
  border-radius: 50%;

  &.utility-icon-electric { background: rgb(10 219 195 / 10%); }
  &.utility-icon-water { background: rgb(96 165 250 / 10%); }
}

.utility-title {
  font-size: $font-lg;
  font-weight: $font-bold;
  color: $text-main;
  letter-spacing: 0.5rpx;
}

.utility-value-row {
  display: flex;
  align-items: baseline;
  gap: $spacing-xs;
  margin-top: $spacing-sm;
}

.utility-value {
  font-size: 90rpx;
  line-height: 1;
  font-weight: $font-bold;
  color: $text-main;
  letter-spacing: -2rpx;
}

.utility-unit {
  font-size: $font-md;
  color: $text-sub;
  font-weight: $font-medium;
  transform: translateY(-4rpx);
}

.utility-trend {
  display: flex;
  align-items: center;
  padding: 12rpx 20rpx;
  font-size: $font-sm;
  border-radius: $radius-sm;
  gap: $spacing-xs;
  font-weight: $font-semibold;

  &.trend-down {
    color: #22c55e;
    background: rgb(34 197 94 / 10%);
    border: 2rpx solid rgb(34 197 94 / 20%);
  }

  &.trend-up {
    color: #ef4444;
    background: rgb(239 68 68 / 10%);
    border: 2rpx solid rgb(239 68 68 / 20%);
  }
}

.utility-chart {
  position: relative;
  z-index: 10;
  overflow: visible;
  margin-top: $spacing-md;
  margin-bottom: $spacing-xs;
  width: 100%;
  height: 240rpx;
}

.chart-container {
  position: relative;
  overflow: visible;
  width: 100%;
  height: 100%;
}
</style>
