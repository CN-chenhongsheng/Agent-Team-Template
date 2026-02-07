<template>
  <view class="footer-tips">
    <view class="footer-text">
      仅限校内人员使用
    </view>
    <view class="footer-badges">
      <view class="badge-item">
        <u-icon name="checkmark-circle" size="12" color="#0d9488" />
        <text>安全连接</text>
      </view>
      <view class="badge-dot" />
      <view class="badge-item">
        <u-icon name="info-circle" size="12" color="#0d9488" />
        <text>隐私保护</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.footer-tips {
  margin-top: 80rpx;
  text-align: center;
}

.footer-text {
  font-size: $font-sm;
  color: $text-placeholder;
  font-weight: 300;
  letter-spacing: 0.4rpx;
}

.footer-badges {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: $spacing-md;
  font-size: $font-xs;
  color: rgb(148 163 184 / 70%);
  gap: $spacing-md;
  font-weight: $font-medium;
}

.badge-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.badge-dot {
  width: 8rpx;
  height: 8rpx;
  background: #cbd5e1;
  border-radius: 50%;
}
</style>
