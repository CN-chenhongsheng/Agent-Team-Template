<template>
  <view class="profile-page">
    <!-- ????? -->
    <view class="ambient-blob blob-primary" />
    <view class="ambient-blob blob-accent" />
    <view class="ambient-blob blob-blue" />

    <!-- ???? -->
    <view class="header">
      <view class="header-left">
        <view class="page-title">
          ??
        </view>
      </view>
    </view>

    <!-- ??? -->
    <view class="main-content">
      <!-- ?????? -->
      <view class="glass-card user-card">
        <view class="user-card-content">
          <!-- ???? -->
          <view class="avatar-section">
            <image
              class="avatar"
              :src="userInfo?.avatar || defaultAvatar"
              mode="aspectFill"
            />
            <!-- ???? -->
            <view class="online-badge">
              <view class="online-dot" />
            </view>
          </view>

          <!-- ???? -->
          <view class="user-info">
            <view class="user-name">
              {{ userInfo?.studentInfo?.studentName || userInfo?.nickname || '???' }}
            </view>
            <view class="role-tag">
              <u-icon name="star" size="14" color="#f59e0b" />
              <text>{{ userInfo?.roleName || '??' }}</text>
            </view>
            <view class="info-tags">
              <view class="info-tag">
                <u-icon name="order" size="14" color="#0adbc3" />
                <text>{{ userInfo?.studentInfo?.studentNo || '2023001' }}</text>
              </view>
              <view class="info-tag">
                <u-icon name="home" size="14" color="#0adbc3" />
                <text>{{ userInfo?.studentInfo?.roomCode || 'A? 302?' }}</text>
              </view>
            </view>
          </view>

          <!-- ???? -->
          <view class="edit-btn" @click="handleEdit">
            <u-icon name="edit-pen" size="18" color="#0adbc3" />
          </view>
        </view>
      </view>

      <!-- ???? -->
      <view class="section">
        <view class="section-title">
          ????
        </view>
        <view class="service-grid">
          <!-- ???? -->
          <view class="glass-card service-item" @click="handleQuickAction('apply')">
            <view class="service-gradient" :style="{ background: 'linear-gradient(to bottom right, rgba(20, 184, 166, 0.05), transparent)' }" />
            <view class="service-bg-icon">
              <u-icon name="list" size="100" color="#14b8a6" />
            </view>
            <view class="service-content">
              <u-icon name="list" size="32" color="#14b8a6" />
              <view class="service-name">
                ????
              </view>
            </view>
          </view>

          <!-- ???? -->
          <view class="glass-card service-item" @click="handleQuickAction('repair')">
            <view class="service-gradient" :style="{ background: 'linear-gradient(to bottom right, rgba(59, 130, 246, 0.05), transparent)' }" />
            <view class="service-bg-icon">
              <u-icon name="setting" size="100" color="#3b82f6" />
            </view>
            <view class="service-content">
              <u-icon name="setting" size="32" color="#3b82f6" />
              <view class="service-name">
                ????
              </view>
            </view>
          </view>

          <!-- ???? -->
          <view class="glass-card service-item" @click="handleQuickAction('roommates')">
            <view class="service-gradient" :style="{ background: 'linear-gradient(to bottom right, rgba(139, 92, 246, 0.05), transparent)' }" />
            <view class="service-bg-icon">
              <u-icon name="account" size="100" color="#8b5cf6" />
            </view>
            <view class="service-content">
              <u-icon name="account" size="32" color="#8b5cf6" />
              <view class="service-name">
                ????
              </view>
            </view>
          </view>

          <!-- ???? -->
          <view class="glass-card service-item" @click="handleQuickAction('notice')">
            <view class="service-gradient" :style="{ background: 'linear-gradient(to bottom right, rgba(249, 115, 22, 0.05), transparent)' }" />
            <view class="service-bg-icon">
              <u-icon name="bell" size="100" color="#f97316" />
            </view>
            <view class="service-content">
              <u-icon name="bell" size="32" color="#f97316" />
              <view class="service-name">
                ????
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- ???? -->
      <view class="section">
        <view class="section-title">
          ??
        </view>
        <view class="glass-card menu-list">
          <!-- ???? -->
          <view class="menu-item" @click="handleMenuClick('password')">
            <view class="password-bg menu-icon">
              <u-icon name="lock" size="18" color="#fff" />
            </view>
            <view class="menu-label">
              ????
            </view>
            <u-icon name="arrow-right" size="18" color="#c4c4c4" />
          </view>

          <!-- ???? -->
          <view class="menu-item" @click="handleMenuClick('settings')">
            <view class="menu-icon settings-bg">
              <u-icon name="setting" size="18" color="#fff" />
            </view>
            <view class="menu-label">
              ????
            </view>
            <u-icon name="arrow-right" size="18" color="#c4c4c4" />
          </view>

          <!-- ????? -->
          <view class="menu-item" @click="handleMenuClick('help')">
            <view class="menu-icon help-bg">
              <u-icon name="info-circle" size="18" color="#fff" />
            </view>
            <view class="menu-label">
              ?????
            </view>
            <u-icon name="arrow-right" size="18" color="#c4c4c4" />
          </view>

          <!-- ???? -->
          <view class="menu-item" @click="handleMenuClick('about')">
            <view class="menu-icon about-bg">
              <u-icon name="info" size="18" color="#fff" />
            </view>
            <view class="menu-label">
              ????
            </view>
            <view class="version-badge">
              v1.2.0
            </view>
            <u-icon name="arrow-right" size="18" color="#c4c4c4" />
          </view>
        </view>
      </view>

      <!-- ?????? -->
      <view class="logout-section">
        <view class="glass-card logout-btn" @click="handleLogout">
          <u-icon name="arrow-leftward" size="18" color="#ef4444" />
          <text>????</text>
        </view>
      </view>
    </view>

    <!-- ?????? -->
    <view class="safe-bottom" />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import useUserStore from '@/store/modules/user';

const userStore = useUserStore();
const defaultAvatar = 'https://lh3.googleusercontent.com/aida-public/AB6AXuB1JhVdkgPRVmEBExS0YehcQ10P72onHobtiZJ0rdv4crelIznydQa9E0SH0nqNH0mDheCZuKECSYNzW6swWmOyiY2JuW3KRd8mI67CiEYqLla4FXLPapNSkbn-r9kLNFa9RU82GWhiG7IKB7VQiqw_cgfAKdQ4uw9fMKA_1GBRiITCRXLqnw2FgJ4GxGa_4T_EQQvbIer3JkyPy8qkEDBrUFOMntcaEexRiAYr7jTrxmY8H7qMkTE-kpUExISpzTxkifDrhBj4Ow7S';

const userInfo = computed(() => userStore.userInfo);

// ???????????
onShow(() => {
  if (!userStore.token) {
    uni.reLaunch({ url: '/pages/common/login/index' });
  }
});

// ??????
function handleEdit() {
  uni.navigateTo({ url: '/pages/profile/edit/index' });
}

// ????
function handleQuickAction(type: string) {
  const routes: Record<string, string> = {
    apply: '/pages/apply/form/index',
    repair: '/pages/service/repair-list/index',
    roommates: '/pages/common/dorm-info/index',
    notice: '/pages/service/message/index',
  };

  if (routes[type]) {
    if (type === 'apply') {
      uni.switchTab({ url: routes[type] });
    }
    else {
      uni.navigateTo({ url: routes[type] });
    }
  }
  else {
    uni.showToast({
      title: '?????',
      icon: 'none',
    });
  }
}

// ????
function handleMenuClick(type: string) {
  const actions: Record<string, () => void> = {
    password: () => {
      uni.navigateTo({ url: '/pages/profile/change-password/index' });
    },
    settings: () => {
      uni.showToast({
        title: '???????',
        icon: 'none',
      });
    },
    help: () => {
      uni.showToast({
        title: '??????????',
        icon: 'none',
      });
    },
    about: () => {
      uni.showToast({
        title: '?????????',
        icon: 'none',
      });
    },
  };

  if (actions[type]) {
    actions[type]();
  }
}

// ????
async function handleLogout() {
  uni.showModal({
    title: '????',
    content: '?????????',
    success: async (res) => {
      if (res.confirm) {
        try {
          await userStore.logout();
          uni.reLaunch({ url: '/pages/common/login/index' });
        }
        catch (error) {
          console.error('??????:', error);
          uni.reLaunch({ url: '/pages/common/login/index' });
        }
      }
    },
  });
}
</script>

<style lang="scss" scoped>
// ????????

.profile-page {
  position: relative;
  overflow: hidden;
  padding-bottom: env(safe-area-inset-bottom);
  min-height: 100vh;
  background-color: $bg-light;
}

// ?????
// .ambient-blob ???? components.scss ??

// ????
.header {
  position: relative;
  z-index: 10;
  align-items: flex-start;
  padding: calc(var(--status-bar-height) + 45rpx) $spacing-lg 25rpx;
  margin-bottom: 10rpx;

  .header-left {
    .page-title {
      font-size: $font-2xl;
      color: $text-main;
      font-weight: $font-bold;
      line-height: 1.2;
    }
  }
}

// ???
.main-content {
  position: relative;
  z-index: 10;
  padding: 0 $spacing-lg;
}

// .glass-card ???? components.scss ??

// ????
.user-card {
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;

  .user-card-content {
    display: flex;
    align-items: center;
    gap: $spacing-md;
  }

  .avatar-section {
    position: relative;
    flex-shrink: 0;

    .avatar {
      width: 120rpx;
      height: 120rpx;
      background: #e2e8f0;
      border: 4rpx solid $glass-border;
      border-radius: 50%;
    }

    .online-badge {
      position: absolute;
      right: 4rpx;
      bottom: 4rpx;
      display: flex;
      justify-content: center;
      align-items: center;
      width: 24rpx;
      height: 24rpx;
      background: #fff;
      border-radius: 50%;
      box-shadow: $shadow-sm;

      .online-dot {
        width: 16rpx;
        height: 16rpx;
        background: $primary;
        border-radius: 50%;
        animation: pulse 2s infinite;
      }
    }
  }

  .user-info {
    flex: 1;
    min-width: 0;

    .user-name {
      font-size: $font-xl;
      font-weight: $font-bold;
      color: $text-main;
      line-height: 1.2;
    }

    .role-tag {
      display: inline-flex;
      align-items: center;
      padding: $spacing-xs 20rpx;
      margin-top: $spacing-sm;
      font-size: $font-sm;
      background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
      border-radius: 12rpx;
      gap: $spacing-xs;

      text {
        color: #92400e;
        font-weight: $font-semibold;
      }
    }

    .info-tags {
      display: flex;
      flex-wrap: wrap;
      gap: $spacing-sm;
      margin-top: $spacing-sm;

      .info-tag {
        display: flex;
        align-items: center;
        gap: 6rpx;
        font-size: $font-sm;
        color: $text-sub;
        font-weight: $font-medium;
      }
    }
  }

  .edit-btn {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 72rpx;
    height: 72rpx;
    background: rgb(10 219 195 / 10%);
    border-radius: 50%;
    transition: $transition-fast;
    flex-shrink: 0;

    &:active {
      background: rgb(10 219 195 / 20%);
      transform: scale(0.95);
    }
  }
}

// ????
.section {
  margin-bottom: $spacing-lg;
}

.section-title {
  margin-bottom: 20rpx;
  font-size: $font-xl;
  color: $text-main;
  font-weight: $font-bold;
}

// ??????
.service-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;
}

.service-item {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  width: 100%;
  height: 160rpx;
  transition: $transition-normal;
  flex-direction: column;

  &:active {
    transform: scale(0.95);
  }

  .service-gradient {
    position: absolute;
    inset: 0;
    pointer-events: none;
  }

  .service-bg-icon {
    position: absolute;
    right: -50rpx;
    bottom: -50rpx;
    z-index: 0;
    opacity: 0.08;
    transition: $transition-slow;
    transform: rotate(12deg);
    pointer-events: none;

    .service-item:active & {
      transform: rotate(0deg) scale(1.25);
    }
  }

  .service-content {
    position: relative;
    z-index: 10;
    display: flex;
    flex-direction: column;
    align-items: center;
    transition: transform 0.3s;

    .service-item:active & {
      transform: translateY(-4rpx);
    }
  }

  .service-name {
    margin-top: $spacing-xs;
    font-size: $font-sm;
    font-weight: $font-bold;
    color: #334155;
  }
}

// ????
.menu-list {
  overflow: hidden;

  .menu-item {
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 28rpx $spacing-md;
    transition: background-color 0.2s;
    border-bottom: 1rpx solid rgb(229 231 235 / 50%);

    &:last-child {
      border-bottom: none;
    }

    &:active {
      background: rgb(248 250 252 / 80%);
    }
  }

  .menu-icon {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 56rpx;
    height: 56rpx;
    border-radius: 14rpx;
    flex-shrink: 0;

    &.password-bg {
      background: linear-gradient(135deg, #fca5a5 0%, #ef4444 100%);
    }

    &.settings-bg {
      background: linear-gradient(135deg, #a5b4fc 0%, #6366f1 100%);
    }

    &.help-bg {
      background: linear-gradient(135deg, #5eead4 0%, $primary 100%);
    }

    &.about-bg {
      background: linear-gradient(135deg, #fdba74 0%, #f97316 100%);
    }
  }

  .menu-label {
    flex: 1;
    font-size: $font-md;
    color: $text-main;
    font-weight: $font-medium;
  }

  .version-badge {
    padding: 4rpx 12rpx;
    margin-right: $spacing-xs;
    font-size: $font-xs;
    color: $text-disabled;
    background: rgb(241 245 249 / 80%);
    border-radius: $spacing-xs;
  }
}

// ????
.logout-section {
  padding: 0 4rpx;
  margin-top: $spacing-xs;

  .logout-btn {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 12rpx;
    padding: 28rpx;
    border: 2rpx solid #fecaca;
    transition: $transition-fast;

    text {
      font-size: $font-md;
      color: #ef4444;
      font-weight: $font-medium;
    }

    &:active {
      background: rgb(254 242 242 / 80%);
      transform: scale(0.99);
    }
  }
}

// ??????
// .safe-bottom ???? components.scss ??

// ????
// @keyframes pulse ???? components.scss ??
</style>
