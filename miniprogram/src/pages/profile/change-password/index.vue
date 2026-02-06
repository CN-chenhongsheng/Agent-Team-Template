<template>
  <view class="change-pwd-page">
    <!-- 背景装饰 -->
    <view class="bg-decorations">
      <view class="blob blob-1" />
      <view class="blob blob-2" />
    </view>

    <view class="page-container">
      <!-- 顶部导航栏 -->
      <view class="nav-header">
        <view class="nav-back" @click="handleBack">
          <u-icon name="arrow-left" size="20" color="#111817" />
        </view>
        <view class="nav-title">
          修改密码
        </view>
        <view class="nav-placeholder" />
      </view>

      <!-- 表单内容 -->
      <view class="glass-card form-section">
        <!-- 原密码 -->
        <view class="form-item">
          <view class="form-label">
            原密码
          </view>
          <input
            v-model="formData.oldPassword"
            class="form-input"
            :type="(showOldPwd ? 'text' : 'password') as any"
            placeholder="请输入原密码"
          >
          <view class="pwd-toggle" @click="showOldPwd = !showOldPwd">
            <u-icon :name="showOldPwd ? 'eye' : 'eye-off'" size="20" color="#999" />
          </view>
        </view>

        <!-- 新密码 -->
        <view class="form-item">
          <view class="form-label">
            新密码
          </view>
          <input
            v-model="formData.newPassword"
            class="form-input"
            :type="(showNewPwd ? 'text' : 'password') as any"
            placeholder="请输入新密码（6-20位）"
          >
          <view class="pwd-toggle" @click="showNewPwd = !showNewPwd">
            <u-icon :name="showNewPwd ? 'eye' : 'eye-off'" size="20" color="#999" />
          </view>
        </view>

        <!-- 确认密码 -->
        <view class="form-item">
          <view class="form-label">
            确认密码
          </view>
          <input
            v-model="formData.confirmPassword"
            class="form-input"
            :type="(showConfirmPwd ? 'text' : 'password') as any"
            placeholder="请再次输入新密码"
          >
          <view class="pwd-toggle" @click="showConfirmPwd = !showConfirmPwd">
            <u-icon :name="showConfirmPwd ? 'eye' : 'eye-off'" size="20" color="#999" />
          </view>
        </view>
      </view>

      <!-- 密码强度提示 -->
      <view v-if="formData.newPassword" class="pwd-strength">
        <view class="strength-label">
          密码强度：
        </view>
        <view class="strength-bars">
          <view class="bar" :class="{ active: pwdStrength >= 1 }" />
          <view class="bar" :class="{ active: pwdStrength >= 2 }" />
          <view class="bar" :class="{ active: pwdStrength >= 3 }" />
        </view>
        <view class="strength-text" :class="strengthClass">
          {{ strengthText }}
        </view>
      </view>

      <!-- 提交按钮 -->
      <view class="submit-btn" :class="{ disabled: submitting }" @click="handleSubmit">
        {{ submitting ? '提交中...' : '确认修改' }}
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { changePasswordAPI } from '@/api/auth';
import useUserStore from '@/store/modules/user';

const userStore = useUserStore();

const showOldPwd = ref(false);
const showNewPwd = ref(false);
const showConfirmPwd = ref(false);
const submitting = ref(false);

const formData = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

// 计算密码强度
const pwdStrength = computed(() => {
  const pwd = formData.newPassword;
  if (!pwd) return 0;
  let strength = 0;
  if (pwd.length >= 6) strength++;
  if (/[a-z]/i.test(pwd) && /\d/.test(pwd)) strength++;
  if (/[!@#$%^&*(),.?":{}|<>]/.test(pwd)) strength++;
  return strength;
});

const strengthText = computed(() => {
  const texts = ['', '弱', '中', '强'];
  return texts[pwdStrength.value] || '';
});

const strengthClass = computed(() => {
  const classes = ['', 'weak', 'medium', 'strong'];
  return classes[pwdStrength.value] || '';
});

function handleBack() {
  uni.navigateBack();
}

function validateForm(): boolean {
  if (!formData.oldPassword) {
    uni.showToast({ title: '请输入原密码', icon: 'none' });
    return false;
  }
  if (!formData.newPassword) {
    uni.showToast({ title: '请输入新密码', icon: 'none' });
    return false;
  }
  if (formData.newPassword.length < 6 || formData.newPassword.length > 20) {
    uni.showToast({ title: '新密码长度需为6-20位', icon: 'none' });
    return false;
  }
  if (formData.newPassword !== formData.confirmPassword) {
    uni.showToast({ title: '两次输入的密码不一致', icon: 'none' });
    return false;
  }
  if (formData.oldPassword === formData.newPassword) {
    uni.showToast({ title: '新密码不能与原密码相同', icon: 'none' });
    return false;
  }
  return true;
}

async function handleSubmit() {
  if (submitting.value) return;
  if (!validateForm()) return;

  submitting.value = true;
  try {
    await changePasswordAPI({
      oldPassword: formData.oldPassword,
      newPassword: formData.newPassword,
      confirmPassword: formData.confirmPassword,
    });

    uni.showModal({
      title: '修改成功',
      content: '密码已修改，请重新登录',
      showCancel: false,
      success: async () => {
        await userStore.logout();
        uni.reLaunch({ url: '/pages/common/login/index' });
      },
    });
  }
  catch (error: any) {
    uni.showToast({
      title: error?.message || '修改失败',
      icon: 'none',
    });
  }
  finally {
    submitting.value = false;
  }
}
</script>

<style lang="scss" scoped>
$primary: #0adbc3;
$bg-light: #f5f8f8;
$text-main: #111817;
$text-sub: #608a85;
$glass-bg: rgb(255 255 255 / 75%);
$glass-border: rgb(255 255 255 / 80%);

.change-pwd-page {
  position: relative;
  min-height: 100vh;
  background-color: $bg-light;
}

.bg-decorations {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;

  .blob {
    position: absolute;
    border-radius: 50%;
    filter: blur(100rpx);

    &.blob-1 {
      top: -10%;
      right: -15%;
      width: 800rpx;
      height: 800rpx;
      background: rgb(10 219 195 / 15%);
    }

    &.blob-2 {
      top: 30%;
      left: -10%;
      width: 600rpx;
      height: 600rpx;
      background: rgb(96 165 250 / 10%);
    }
  }
}

.page-container {
  position: relative;
  z-index: 10;
  padding: 0 32rpx 64rpx;
}

.nav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: calc(var(--status-bar-height) + 20rpx) 0 20rpx;

  .nav-back {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 80rpx;
    height: 80rpx;
  }

  .nav-title {
    font-size: 36rpx;
    font-weight: 600;
    color: $text-main;
  }

  .nav-placeholder {
    width: 80rpx;
  }
}

.glass-card {
  background: $glass-bg;
  border: 2rpx solid $glass-border;
  border-radius: 32rpx;
  box-shadow: 0 8rpx 24rpx rgb(0 0 0 / 5%);
  backdrop-filter: blur(32rpx);
}

.form-section {
  padding: 32rpx;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid rgb(0 0 0 / 5%);

  &:last-child {
    border-bottom: none;
  }

  .form-label {
    width: 160rpx;
    font-size: 28rpx;
    color: $text-sub;
    flex-shrink: 0;
  }

  .form-input {
    flex: 1;
    font-size: 28rpx;
    color: $text-main;
  }

  .pwd-toggle {
    padding: 16rpx;
  }
}

.pwd-strength {
  display: flex;
  align-items: center;
  padding: 0 16rpx;
  margin-top: 24rpx;

  .strength-label {
    font-size: 24rpx;
    color: $text-sub;
  }

  .strength-bars {
    display: flex;
    gap: 8rpx;
    margin: 0 16rpx;

    .bar {
      width: 60rpx;
      height: 8rpx;
      background: #e5e5e5;
      border-radius: 4rpx;

      &.active {
        background: #ef4444;
      }
    }

    .bar.active:nth-child(2) {
      background: #f59e0b;
    }

    .bar.active:nth-child(3) {
      background: #10b981;
    }
  }

  .strength-text {
    font-size: 24rpx;

    &.weak { color: #ef4444; }
    &.medium { color: #f59e0b; }
    &.strong { color: #10b981; }
  }
}

.submit-btn {
  padding: 28rpx;
  margin-top: 48rpx;
  font-size: 32rpx;
  text-align: center;
  color: #fff;
  background: $primary;
  border-radius: 32rpx;
  font-weight: 600;

  &.disabled {
    opacity: 0.6;
  }

  &:active {
    opacity: 0.8;
  }
}
</style>
