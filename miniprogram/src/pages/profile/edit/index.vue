<template>
  <view class="edit-page">
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
          编辑资料
        </view>
        <view class="nav-placeholder" />
      </view>

      <!-- 头像编辑 -->
      <view class="glass-card avatar-section">
        <view class="avatar-wrapper" @click="handleChooseAvatar">
          <image
            class="avatar"
            :src="formData.avatar || defaultAvatar"
            mode="aspectFill"
          />
          <view class="avatar-edit-icon">
            <u-icon name="camera" size="16" color="#fff" />
          </view>
        </view>
        <view class="avatar-tip">
          点击更换头像
        </view>
      </view>

      <!-- 表单内容 -->
      <view class="glass-card form-section">
        <view class="section-title">
          基本信息
        </view>

        <!-- 姓名（只读） -->
        <view class="form-item">
          <view class="form-label">
            姓名
          </view>
          <view class="form-value readonly">
            {{ userInfo?.studentInfo?.studentName || userInfo?.nickname || '-' }}
          </view>
        </view>

        <!-- 学号（只读） -->
        <view class="form-item">
          <view class="form-label">
            学号
          </view>
          <view class="form-value readonly">
            {{ userInfo?.studentInfo?.studentNo || '-' }}
          </view>
        </view>

        <!-- 手机号 -->
        <view class="form-item">
          <view class="form-label">
            手机号
          </view>
          <input
            :value="formData.phone"
            class="form-input"
            type="text"
            placeholder="请输入手机号"
            :maxlength="11 as any"
            @input="handlePhoneInput"
          >
        </view>

        <!-- 邮箱 -->
        <view class="form-item">
          <view class="form-label">
            邮箱
          </view>
          <input
            v-model="formData.email"
            class="form-input"
            type="text"
            placeholder="请输入邮箱"
          >
        </view>
      </view>

      <!-- 紧急联系人 -->
      <view class="glass-card form-section">
        <view class="section-title">
          紧急联系人
        </view>

        <view class="form-item">
          <view class="form-label">
            联系人姓名
          </view>
          <input
            v-model="formData.emergencyContact"
            class="form-input"
            type="text"
            placeholder="请输入紧急联系人姓名"
          >
        </view>

        <view class="form-item">
          <view class="form-label">
            联系人电话
          </view>
          <input
            :value="formData.emergencyPhone"
            class="form-input"
            type="text"
            placeholder="请输入紧急联系人电话"
            :maxlength="11 as any"
            @input="handleEmergencyPhoneInput"
          >
        </view>
      </view>

      <!-- 保存按钮 -->
      <view class="save-btn" :class="{ disabled: saving }" @click="handleSave">
        {{ saving ? '保存中...' : '保存修改' }}
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import useUserStore from '@/store/modules/user';
import { updateUserProfileAPI } from '@/api/auth';

const userStore = useUserStore();
const userInfo = computed(() => userStore.userInfo);

const defaultAvatar = 'https://lh3.googleusercontent.com/aida-public/AB6AXuB1JhVdkgPRVmEBExS0YehcQ10P72onHobtiZJ0rdv4crelIznydQa9E0SH0nqNH0mDheCZuKECSYNzW6swWmOyiY2JuW3KRd8mI67CiEYqLla4FXLPapNSkbn-r9kLNFa9RU82GWhiG7IKB7VQiqw_cgfAKdQ4uw9fMKA_1GBRiITCRXLqnw2FgJ4GxGa_4T_EQQvbIer3JkyPy8qkEDBrUFOMntcaEexRiAYr7jTrxmY8H7qMkTE-kpUExISpzTxkifDrhBj4Ow7S';

const saving = ref(false);

const formData = reactive({
  avatar: '',
  phone: '',
  email: '',
  emergencyContact: '',
  emergencyPhone: '',
});

// 初始化表单数据
onMounted(() => {
  if (userInfo.value) {
    formData.avatar = userInfo.value.avatar || '';
    formData.phone = String(userInfo.value.studentInfo?.phone || userInfo.value.phone || '');
    formData.email = userInfo.value.studentInfo?.email || userInfo.value.email || '';
    formData.emergencyContact = userInfo.value.studentInfo?.emergencyContact || '';
    formData.emergencyPhone = String(userInfo.value.studentInfo?.emergencyPhone || '');
  }
});

// 返回上一页
function handleBack() {
  uni.navigateBack();
}

// 处理手机号输入
function handlePhoneInput(e: any) {
  formData.phone = e.detail?.value || '';
}

// 处理紧急联系人电话输入
function handleEmergencyPhoneInput(e: any) {
  formData.emergencyPhone = e.detail?.value || '';
}

// 选择头像
function handleChooseAvatar() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0];
      uploadAvatar(tempFilePath);
    },
  });
}

// 上传头像
async function uploadAvatar(filePath: string) {
  uni.showLoading({ title: '上传中...' });
  try {
    const baseURL = import.meta.env.VITE_APP_BASE_API || '';
    const res = await uni.uploadFile({
      url: `${baseURL}/api/v1/common/upload`,
      filePath,
      name: 'file',
      header: {
        Authorization: `Bearer ${userStore.token}`,
      },
    });
    const data = JSON.parse(res.data);
    if (data.code === 200 && data.data) {
      formData.avatar = data.data.url || data.data;
      uni.showToast({ title: '上传成功', icon: 'success' });
    }
    else {
      uni.showToast({ title: data.msg || '上传失败', icon: 'none' });
    }
  }
  catch (error) {
    console.error('上传头像失败:', error);
    uni.showToast({ title: '上传失败', icon: 'none' });
  }
  finally {
    uni.hideLoading();
  }
}

// 表单验证
function validateForm(): boolean {
  if (formData.phone && !/^1[3-9]\d{9}$/.test(formData.phone)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' });
    return false;
  }
  if (formData.email && !/^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/.test(formData.email)) {
    uni.showToast({ title: '请输入正确的邮箱', icon: 'none' });
    return false;
  }
  if (formData.emergencyPhone && !/^1[3-9]\d{9}$/.test(formData.emergencyPhone)) {
    uni.showToast({ title: '请输入正确的紧急联系人电话', icon: 'none' });
    return false;
  }
  return true;
}

// 保存修改
async function handleSave() {
  if (saving.value) return;
  if (!validateForm()) return;

  saving.value = true;
  try {
    await updateUserProfileAPI({
      phone: formData.phone || undefined,
      email: formData.email || undefined,
      emergencyContact: formData.emergencyContact || undefined,
      emergencyPhone: formData.emergencyPhone || undefined,
      avatar: formData.avatar || undefined,
    });

    // 更新本地store
    if (userStore.userInfo) {
      userStore.userInfo.avatar = formData.avatar || userStore.userInfo.avatar;
      userStore.userInfo.phone = formData.phone || userStore.userInfo.phone;
      userStore.userInfo.email = formData.email || userStore.userInfo.email;
      if (userStore.userInfo.studentInfo) {
        userStore.userInfo.studentInfo.phone = formData.phone as any;
        userStore.userInfo.studentInfo.email = formData.email;
        userStore.userInfo.studentInfo.emergencyContact = formData.emergencyContact;
        userStore.userInfo.studentInfo.emergencyPhone = formData.emergencyPhone as any;
      }
    }

    uni.showToast({ title: '保存成功', icon: 'success' });
    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
  }
  catch (error) {
    console.error('保存失败:', error);
    uni.showToast({ title: '保存失败', icon: 'none' });
  }
  finally {
    saving.value = false;
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

.edit-page {
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

// 使用全局 .glass-card，仅覆盖页面特有属性
.glass-card {
  margin-bottom: 24rpx;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx;

  .avatar-wrapper {
    position: relative;

    .avatar {
      width: 160rpx;
      height: 160rpx;
      border: 6rpx solid #fff;
      border-radius: 50%;
      box-shadow: 0 4rpx 12rpx rgb(0 0 0 / 10%);
    }

    .avatar-edit-icon {
      position: absolute;
      right: 0;
      bottom: 0;
      display: flex;
      justify-content: center;
      align-items: center;
      width: 48rpx;
      height: 48rpx;
      background: $primary;
      border: 4rpx solid #fff;
      border-radius: 50%;
    }
  }

  .avatar-tip {
    margin-top: 16rpx;
    font-size: 24rpx;
    color: $text-sub;
  }
}

.form-section {
  padding: 32rpx;

  .section-title {
    padding-bottom: 16rpx;
    margin-bottom: 24rpx;
    font-size: 30rpx;
    color: $text-main;
    font-weight: 600;
    border-bottom: 1rpx solid rgb(0 0 0 / 5%);
  }
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
    width: 180rpx;
    font-size: 28rpx;
    color: $text-sub;
    flex-shrink: 0;
  }

  .form-value {
    flex: 1;
    font-size: 28rpx;
    color: $text-main;

    &.readonly {
      color: #999;
    }
  }

  .form-input {
    font-size: 28rpx;
    text-align: right;
    color: $text-main;
    flex: 1;
  }
}

.save-btn {
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
