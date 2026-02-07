<template>
  <view class="login-form glass-panel">
    <view class="form-corner" />
    <view class="form-content">
      <!-- ?????? -->
      <view class="login-tabs">
        <view
          class="tab-item"
          :class="{ active: loginType === 'password' }"
          @click="loginType = 'password'"
        >
          ????
        </view>
        <view
          class="tab-item"
          :class="{ active: loginType === 'code' }"
          @click="loginType = 'code'"
        >
          ?????
        </view>
      </view>

      <!-- ?????? -->
      <view v-if="loginType === 'password'" class="form-fields">
        <!-- ??/?? -->
        <view class="field-group">
          <view class="field-label">
            ??/??
          </view>
          <view class="input-wrapper">
            <view class="input-icon-wrapper">
              <u-icon name="list" size="20" color="#94a3b8" />
            </view>
            <input
              v-model="formData.username"
              class="input-field"
              placeholder="???????/??"
              type="text"
            >
          </view>
        </view>

        <!-- ?? -->
        <view class="field-group">
          <view class="field-label">
            ??
          </view>
          <view class="input-wrapper">
            <view class="input-icon-wrapper">
              <u-icon name="lock" size="20" color="#94a3b8" />
            </view>
            <input
              v-model="formData.password"
              class="input-field"
              :password="!showPassword"
              placeholder="?????"
            >
            <view class="password-toggle" @click="showPassword = !showPassword">
              <u-icon :name="showPassword ? 'eye' : 'eye-fill'" size="20" color="#94a3b8" />
            </view>
          </view>
        </view>

        <!-- ????????? -->
        <view class="form-options">
          <view class="checkbox-wrapper" @click="formData.remember = !formData.remember">
            <view class="checkbox" :class="{ checked: formData.remember }">
              <u-icon v-if="formData.remember" name="checkmark" size="12" color="#0d9488" />
            </view>
            <text class="checkbox-label">
              ????
            </text>
          </view>
          <view class="forgot-password" @click="handleForgotPassword">
            ?????
          </view>
        </view>

        <!-- ???? -->
        <view class="login-button" @click="handlePasswordLogin">
          <view class="button-shine" />
          <text>????</text>
          <u-icon name="arrow-right" size="20" color="#fff" />
        </view>
      </view>

      <!-- ????? -->
      <view v-else class="form-fields">
        <!-- ??? -->
        <view class="field-group">
          <view class="field-label">
            ???
          </view>
          <view class="input-wrapper">
            <view class="input-icon-wrapper">
              <u-icon name="phone" size="20" color="#94a3b8" />
            </view>
            <input
              v-model="formData.phone"
              class="input-field"
              placeholder="??????"
              type="number"
            >
          </view>
        </view>

        <!-- ??? -->
        <view class="field-group">
          <view class="field-label">
            ???
          </view>
          <view class="input-wrapper">
            <view class="input-icon-wrapper">
              <u-icon name="chat" size="20" color="#94a3b8" />
            </view>
            <input
              v-model="formData.code"
              class="input-field"
              placeholder="??????"
              type="number"
            >
            <view class="code-button" @click="handleGetCode">
              {{ codeButtonText }}
            </view>
          </view>
        </view>

        <!-- ???? -->
        <view class="login-button" @click="handleCodeLogin">
          <view class="button-shine" />
          <text>????</text>
          <u-icon name="arrow-right" size="20" color="#fff" />
        </view>
      </view>

      <!-- ??? -->
      <view class="divider">
        <view class="divider-line" />
        <text>????</text>
        <view class="divider-line" />
      </view>

      <!-- ???? -->
      <view class="wechat-button" @click="handleWechatLogin">
        <u-icon name="weixin-fill" size="20" color="#07c160" />
        <text>??????</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { useLogin } from '../composables/useLogin';

const {
  loginType,
  formData,
  showPassword,
  codeButtonText,
  handleGetCode,
  handlePasswordLogin,
  handleCodeLogin,
  handleWechatLogin,
  handleForgotPassword,
} = useLogin();
</script>

<style lang="scss" scoped>
// ?????
.glass-panel {
  position: relative;
  overflow: hidden;
  padding: 48rpx;
  width: 100%;
  background: rgb(255 255 255 / 75%);
  border: 2rpx solid rgb(255 255 255 / 80%);
  border-radius: 56rpx;
  box-shadow: 0 30rpx 70rpx rgb(0 0 0 / 5%), 0 10rpx 30rpx rgb(0 0 0 / 2%);
  backdrop-filter: blur(40rpx);
  box-sizing: border-box;

  .form-corner {
    position: absolute;
    top: 0;
    right: 0;
    z-index: 0;
    margin-top: -$spacing-lg;
    margin-right: -$spacing-lg;
    width: 128rpx;
    height: 128rpx;
    background: linear-gradient(to bottom left, rgb(207 250 254 / 50%), transparent);
    border-radius: 0 0 0 96rpx;
  }

  .form-content {
    position: relative;
    z-index: 10;
  }
}

// ??????
.login-tabs {
  display: flex;
  padding: 8rpx;
  margin-bottom: $spacing-lg;
  background: rgb(241 245 249 / 60%);
  border-radius: $radius-lg;

  .tab-item {
    padding: $spacing-sm;
    font-size: $font-md;
    text-align: center;
    color: $text-sub;
    border-radius: $radius-md;
    transition: $transition-normal;
    flex: 1;
    font-weight: $font-medium;

    &.active {
      color: #0d9488;
      background: #fff;
      box-shadow: 0 2rpx 8rpx rgb(0 0 0 / 5%);
      font-weight: $font-semibold;
    }
  }
}

// ????
.form-fields {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.field-label {
  margin-left: 8rpx;
  font-size: $font-sm;
  color: $text-sub;
  font-weight: $font-bold;
  text-transform: uppercase;
  letter-spacing: 0.4rpx;
}

.input-wrapper {
  display: flex;
  align-items: center;
  padding: 0 $spacing-lg;
  height: 96rpx;
  background: rgb(255 255 255 / 60%);
  border: 2rpx solid #e2e8f0;
  border-radius: $spacing-lg;
  transition: $transition-normal;

  &:focus-within {
    background: rgb(255 255 255 / 95%);
    border-color: #22d3ee;
    box-shadow: 0 0 0 8rpx rgb(34 211 238 / 10%);
  }

  .input-icon-wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-right: $spacing-md;
    width: 40rpx;
    height: 40rpx;
    transition: color 0.3s;
    flex-shrink: 0;
  }

  &:focus-within .input-icon-wrapper {
    :deep(.u-icon) {
      color: #0891b2;
    }
  }

  .input-field {
    padding: 0;
    margin-left: 0;
    min-width: 0;
    font-size: 30rpx;
    color: #0f172a;
    background: transparent;
    border: none;
    flex: 1;
    font-weight: $font-medium;

    &::placeholder {
      color: $text-placeholder;
    }
  }

  .password-toggle,
  .code-button {
    padding: 8rpx;
    margin-left: $spacing-sm;
    font-size: $font-md;
    color: $text-placeholder;
  }

  .code-button {
    color: #06b6d4;
    font-weight: $font-semibold;
    cursor: pointer;

    &:active {
      opacity: 0.7;
    }
  }
}

// ????
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: -8rpx;
  font-size: $font-sm;
  font-weight: $font-medium;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  color: $text-sub;
  cursor: pointer;

  .checkbox {
    display: flex;
    justify-content: center;
    align-items: center;
    width: $spacing-lg;
    height: $spacing-lg;
    border: 2rpx solid #cbd5e1;
    border-radius: 8rpx;
    transition: all 0.2s;

    &.checked {
      background: #0d9488;
      border-color: #0d9488;
    }
  }

  .checkbox-label {
    font-size: $font-sm;
  }
}

.forgot-password {
  font-size: $font-sm;
  color: rgb(249 115 22 / 80%);
  cursor: pointer;

  &:active {
    color: #f97316;
  }
}

// ????
.login-button {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  margin-top: $spacing-sm;
  width: 100%;
  height: 96rpx;
  font-size: $spacing-lg;
  color: #fff;
  background: linear-gradient(to right, #14b8a6, #06b6d4);
  border-radius: $spacing-lg;
  box-shadow: 0 8rpx 24rpx rgb(20 184 166 / 20%);
  transition: $transition-normal;
  gap: $spacing-sm;
  font-weight: $font-bold;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 12rpx 32rpx rgb(6 182 212 / 30%);
  }

  .button-shine {
    position: absolute;
    inset: 0;
    background: rgb(255 255 255 / 20%);
    transform: translateY(100%);
    transition: transform 0.3s;
  }

  &:active .button-shine {
    transform: translateY(0);
  }
}

// ???
.divider {
  display: flex;
  align-items: center;
  gap: $spacing-lg;
  margin: $spacing-lg 0;

  .divider-line {
    flex: 1;
    height: 2rpx;
    background: #e2e8f0;
  }

  text {
    font-size: $font-xs;
    font-weight: $font-semibold;
    color: $text-placeholder;
    text-transform: uppercase;
    letter-spacing: 0.4rpx;
  }
}

// ??????
.wechat-button {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 88rpx;
  font-size: $font-md;
  color: #475569;
  background: #fff;
  border: 2rpx solid #e2e8f0;
  border-radius: $spacing-lg;
  box-shadow: 0 2rpx 8rpx rgb(0 0 0 / 5%);
  transition: $transition-normal;
  gap: 20rpx;
  font-weight: $font-semibold;

  &:active {
    background: #f8fafc;
    border-color: rgb(7 193 96 / 30%);
    box-shadow: 0 4rpx 12rpx rgb(0 0 0 / 8%);
  }
}
</style>
