import { ref } from 'vue';
import useUserStore from '@/store/modules/user';

export function useLogin() {
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

  return {
    loginType,
    formData,
    showPassword,
    codeButtonText,
    codeCountdown,
    handleGetCode,
    handlePasswordLogin,
    handleCodeLogin,
    handleWechatLogin,
    handleForgotPassword,
  };
}
