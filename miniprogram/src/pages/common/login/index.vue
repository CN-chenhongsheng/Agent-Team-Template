<template>
  <view class="login-page">
    <!-- 背景装饰 -->
    <view class="bg-decoration">
      <view class="bg-image" />
      <view class="bg-gradient-1" />
      <view class="bg-gradient-2" />
      <view class="bg-blob-1" />
      <view class="bg-blob-2" />
    </view>

    <!-- 主内容 -->
    <view class="login-content">
      <!-- Logo 和标题 -->
      <view class="logo-section">
        <view class="logo-container">
          <view class="logo-bg-blur" />
          <view class="logo-deco-icon deco-1">
            <u-icon name="wifi" size="48" color="rgba(103, 232, 249, 0.4)" />
          </view>
          <view class="logo-deco-icon deco-2">
            <u-icon name="home" size="48" color="rgba(20, 184, 166, 0.4)" />
          </view>
          <view class="logo-deco-icon deco-3">
            <u-icon name="flash" size="48" color="rgba(249, 115, 22, 0.3)" />
          </view>
          <view class="logo-main-circle">
            <view class="logo-circle-gradient" />
            <view class="logo-icon-box">
              <u-icon name="home" size="32" color="#fff" />
            </view>
          </view>
          <view class="logo-shine" />
        </view>
        <view class="title-wrapper">
          <view class="title-main">
            <text class="title-gradient">
              智慧
            </text>
            <text>宿管</text>
          </view>
          <view class="title-sub-card">
            <view class="title-dot dot-1" />
            <text>Smart Dormitory System</text>
            <view class="title-dot dot-2" />
          </view>
        </view>
      </view>

      <!-- 登录表单 -->
      <view class="login-form glass-panel">
        <view class="form-corner" />
        <view class="form-content">
          <!-- 登录方式切换 -->
          <view class="login-tabs">
            <view
              class="tab-item"
              :class="{ active: loginType === 'password' }"
              @click="loginType = 'password'"
            >
              账号密码
            </view>
            <view
              class="tab-item"
              :class="{ active: loginType === 'code' }"
              @click="loginType = 'code'"
            >
              验证码登录
            </view>
          </view>

          <!-- 账号密码登录 -->
          <view v-if="loginType === 'password'" class="form-fields">
            <!-- 学号/工号 -->
            <view class="field-group">
              <view class="field-label">
                学号/工号
              </view>
              <view class="input-wrapper">
                <view class="input-icon-wrapper">
                  <u-icon name="list" size="20" color="#94a3b8" />
                </view>
                <input
                  v-model="formData.username"
                  class="input-field"
                  placeholder="请输入您的学号/工号"
                  type="text"
                >
              </view>
            </view>

            <!-- 密码 -->
            <view class="field-group">
              <view class="field-label">
                密码
              </view>
              <view class="input-wrapper">
                <view class="input-icon-wrapper">
                  <u-icon name="lock" size="20" color="#94a3b8" />
                </view>
                <input
                  v-model="formData.password"
                  class="input-field"
                  :password="!showPassword"
                  placeholder="请输入密码"
                >
                <view class="password-toggle" @click="showPassword = !showPassword">
                  <u-icon :name="showPassword ? 'eye' : 'eye-fill'" size="20" color="#94a3b8" />
                </view>
              </view>
            </view>

            <!-- 自动登录和忘记密码 -->
            <view class="form-options">
              <view class="checkbox-wrapper" @click="formData.remember = !formData.remember">
                <view class="checkbox" :class="{ checked: formData.remember }">
                  <u-icon v-if="formData.remember" name="checkmark" size="12" color="#0d9488" />
                </view>
                <text class="checkbox-label">
                  自动登录
                </text>
              </view>
              <view class="forgot-password" @click="handleForgotPassword">
                忘记密码？
              </view>
            </view>

            <!-- 登录按钮 -->
            <view class="login-button" @click="handlePasswordLogin">
              <view class="button-shine" />
              <text>立即登录</text>
              <u-icon name="arrow-right" size="20" color="#fff" />
            </view>
          </view>

          <!-- 验证码登录 -->
          <view v-else class="form-fields">
            <!-- 手机号 -->
            <view class="field-group">
              <view class="field-label">
                手机号
              </view>
              <view class="input-wrapper">
                <view class="input-icon-wrapper">
                  <u-icon name="phone" size="20" color="#94a3b8" />
                </view>
                <input
                  v-model="formData.phone"
                  class="input-field"
                  placeholder="请输入手机号"
                  type="number"
                >
              </view>
            </view>

            <!-- 验证码 -->
            <view class="field-group">
              <view class="field-label">
                验证码
              </view>
              <view class="input-wrapper">
                <view class="input-icon-wrapper">
                  <u-icon name="chat" size="20" color="#94a3b8" />
                </view>
                <input
                  v-model="formData.code"
                  class="input-field"
                  placeholder="请输入验证码"
                  type="number"
                >
                <view class="code-button" @click="handleGetCode">
                  {{ codeButtonText }}
                </view>
              </view>
            </view>

            <!-- 登录按钮 -->
            <view class="login-button" @click="handleCodeLogin">
              <view class="button-shine" />
              <text>立即登录</text>
              <u-icon name="arrow-right" size="20" color="#fff" />
            </view>
          </view>

          <!-- 分隔线 -->
          <view class="divider">
            <view class="divider-line" />
            <text>其他方式</text>
            <view class="divider-line" />
          </view>

          <!-- 微信登录 -->
          <view class="wechat-button" @click="handleWechatLogin">
            <u-icon name="weixin-fill" size="20" color="#07c160" />
            <text>微信一键登录</text>
          </view>
        </view>
      </view>

      <!-- 底部提示 -->
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
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import useUserStore from '@/store/modules/user';

const userStore = useUserStore();

// 登录方式
const loginType = ref<'password' | 'code'>('password');

// 表单数据
const formData = ref({
  username: '',
  password: '',
  phone: '',
  code: '',
  remember: false,
});

// 显示密码
const showPassword = ref(false);

// 验证码按钮文字
const codeButtonText = ref('获取验证码');
const codeCountdown = ref(0);

// 获取验证码
function handleGetCode() {
  if (codeCountdown.value > 0) return;
  if (!formData.value.phone) {
    uni.showToast({ title: '请输入手机号', icon: 'none' });
    return;
  }
  // TODO: 调用获取验证码接口
  codeCountdown.value = 60;
  const timer = setInterval(() => {
    codeCountdown.value--;
    codeButtonText.value = `${codeCountdown.value}秒后重试`;
    if (codeCountdown.value <= 0) {
      clearInterval(timer);
      codeButtonText.value = '获取验证码';
    }
  }, 1000);
  uni.showToast({ title: '验证码已发送', icon: 'success' });
}

// 密码登录
async function handlePasswordLogin() {
  if (!formData.value.username) {
    uni.showToast({ title: '请输入学号/工号', icon: 'none' });
    return;
  }
  if (!formData.value.password) {
    uni.showToast({ title: '请输入密码', icon: 'none' });
    return;
  }

  try {
    // 判断是学号还是工号
    // 学号通常是纯数字，工号可能包含字母
    const isStudentNo = /^\d+$/.test(formData.value.username);

    if (isStudentNo) {
      // 学生登录
      uni.showLoading({ title: '登录中...', mask: true });
      await (userStore as any).studentLogin(
        formData.value.username,
        formData.value.password,
      );
      uni.hideLoading();
      uni.showToast({ title: '登录成功', icon: 'success' });
      setTimeout(() => {
        uni.reLaunch({ url: '/pages/tab/home/index' });
      }, 500);
    }
    else {
      // 管理员/宿管员登录
      uni.showLoading({ title: '登录中...', mask: true });
      await (userStore as any).login({
        username: formData.value.username,
        password: formData.value.password,
      });
      uni.hideLoading();
      uni.showToast({ title: '登录成功', icon: 'success' });
      setTimeout(() => {
        uni.reLaunch({ url: '/pages/tab/home/index' });
      }, 500);
    }
  }
  catch (error: any) {
    uni.hideLoading();
    const errorMsg = error?.data?.message || error?.message || '登录失败，请检查账号密码';
    uni.showToast({ title: errorMsg, icon: 'none', duration: 2000 });
  }
}

// 验证码登录
async function handleCodeLogin() {
  if (!formData.value.phone) {
    uni.showToast({ title: '请输入手机号', icon: 'none' });
    return;
  }
  if (!formData.value.code) {
    uni.showToast({ title: '请输入验证码', icon: 'none' });
    return;
  }

  try {
    // TODO: 调用验证码登录接口
    await (userStore as any).wxLogin();
    uni.reLaunch({ url: '/pages/tab/home/index' });
  }
  catch (error) {
    console.error('登录失败:', error);
    uni.showToast({ title: '登录失败', icon: 'none' });
  }
}

// 微信登录
async function handleWechatLogin() {
  try {
    uni.showLoading({ title: '微信登录中...', mask: true });
    await (userStore as any).wxLogin();
    uni.hideLoading();
    uni.showToast({ title: '登录成功', icon: 'success' });
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/tab/home/index' });
    }, 500);
  }
  catch (error: any) {
    uni.hideLoading();
    console.error('微信登录失败:', error);
    const errorMsg = error?.data?.message || error?.message || '微信登录失败';
    uni.showToast({ title: errorMsg, icon: 'none', duration: 2000 });
  }
}

// 忘记密码
function handleForgotPassword() {
  uni.showToast({ title: '功能开发中', icon: 'none' });
}
</script>

<style lang="scss" scoped>
.login-page {
  position: relative;
  display: flex;
  overflow: hidden;
  padding: $spacing-lg;
  padding-top: 43rpx;
  min-height: calc(100vh - 43rpx - $spacing-lg);
  background-color: #f0f9ff;
  box-sizing: content-box;
}

// 背景装饰
.bg-decoration {
  position: fixed;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;

  .bg-image {
    position: absolute;
    background-position: center;
    background-size: cover;
    opacity: 0.03;
    filter: grayscale(100%);
    inset: 0;
    background-image: url('https://via.placeholder.com/800x600');
    mix-blend-mode: multiply;
  }

  .bg-gradient-1 {
    position: absolute;
    inset: 0;
    background: linear-gradient(to bottom right, rgb(207 250 254 / 90%), rgb(255 255 255 / 70%), rgb(255 237 213 / 50%));
  }

  .bg-gradient-2 {
    position: absolute;
    inset: 0;
    background: linear-gradient(to top, rgb(255 255 255 / 80%), transparent, transparent);
  }

  .bg-blob-1 {
    position: absolute;
    top: 0;
    right: 0;
    width: 800rpx;
    height: 800rpx;
    background: rgb(103 232 249 / 30%);
    border-radius: 50%;
    filter: blur(200rpx);
    transform: translate(50%, -33%);
  }

  .bg-blob-2 {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 700rpx;
    height: 700rpx;
    background: rgb(249 115 22 / 20%);
    border-radius: 50%;
    filter: blur(180rpx);
    transform: translate(-33%, 33%);
  }
}

// 主内容
.login-content {
  position: relative;
  z-index: 10;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: auto;
  width: 100%;
  height: 100%;
  flex-direction: column;
  box-sizing: border-box;
}

// Logo 区域
.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 80rpx;
  width: 100%;
}

.logo-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: $spacing-lg;
  width: 256rpx;
  height: 256rpx;

  .logo-bg-blur {
    position: absolute;
    background: linear-gradient(to top right, rgb(207 250 254 / 50%), rgb(20 184 166 / 30%));
    border-radius: 50%;
    opacity: 0.7;
    filter: blur(60rpx);
    inset: 0;
    transform: scale(1.25);
  }

  .logo-deco-icon {
    position: absolute;
    z-index: 1;

    &.deco-1 {
      top: -16rpx;
      left: -64rpx;
      transform: rotate(12deg) scale(0.75);
    }

    &.deco-2 {
      top: 80rpx;
      right: -96rpx;
      transform: rotate(-12deg) scale(0.75);
    }

    &.deco-3 {
      bottom: -16rpx;
      left: -32rpx;
      transform: rotate(45deg) scale(0.5);
    }
  }

  .logo-main-circle {
    position: relative;
    z-index: 10;
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden;
    width: 192rpx;
    height: 192rpx;
    background: #fff;
    border: 2rpx solid rgb(255 255 255 / 60%);
    border-radius: 50%;
    box-shadow: 0 20rpx 40rpx rgb(13 148 136 / 5%);

    .logo-circle-gradient {
      position: absolute;
      inset: 0;
      background: linear-gradient(to bottom, #fff, rgb(207 250 254 / 50%));
    }

    .logo-icon-box {
      position: relative;
      z-index: 20;
      display: flex;
      justify-content: center;
      align-items: center;
      width: 96rpx;
      height: 96rpx;
      background: linear-gradient(135deg, #06b6d4, #0d9488);
      border-radius: 16rpx;
      box-shadow: 0 8rpx 24rpx rgb(6 182 212 / 30%);
      transition: transform 0.3s;
      transform: rotate(3deg);

      :deep(.u-icon) {
        filter: drop-shadow(0 2rpx 4rpx rgb(0 0 0 / 20%));
      }
    }
  }

  .logo-shine {
    position: absolute;
    z-index: 30;
    background: linear-gradient(to top right, transparent, rgb(255 255 255 / 40%), transparent);
    border-radius: 50%;
    inset: 0;
    pointer-events: none;
  }
}

.title-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
}

.title-main {
  display: flex;
  align-items: center;
  font-size: 64rpx;
  color: #1e293b;
  filter: drop-shadow(0 2rpx 4rpx rgb(0 0 0 / 5%));
  font-weight: $font-bold;
  letter-spacing: 4rpx;
  gap: $spacing-sm;

  .title-gradient {
    background: linear-gradient(to right, #0d9488, #06b6d4);
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.title-sub-card {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 12rpx $spacing-lg;
  background: rgb(255 255 255 / 40%);
  backdrop-filter: blur(20rpx);
  border: 2rpx solid rgb(255 255 255 / 50%);
  border-radius: 9999rpx;
  box-shadow: 0 2rpx 8rpx rgb(0 0 0 / 5%);

  .title-dot {
    width: 12rpx;
    height: 12rpx;
    border-radius: 50%;
    animation: pulse-dot 2s ease-in-out infinite;

    &.dot-1 {
      background: #06b6d4;
    }

    &.dot-2 {
      background: #0d9488;
      animation-delay: 0.75s;
    }
  }

  text {
    font-size: $font-xs;
    color: $text-sub;
    font-weight: $font-bold;
    letter-spacing: 4rpx;
    text-transform: uppercase;
  }
}

@keyframes pulse-dot {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }

  50% {
    opacity: 0.5;
    transform: scale(1.2);
  }
}

// 毛玻璃面板
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

// 登录方式切换
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

// 表单字段
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

// 表单选项
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

// 登录按钮
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

// 分隔线
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

// 微信登录按钮
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

// 底部提示
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
