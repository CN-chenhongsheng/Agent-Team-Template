import { computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import useUserStore from '@/store/modules/user';

const DEFAULT_AVATAR
  = 'https://lh3.googleusercontent.com/aida-public/AB6AXuB1JhVdkgPRVmEBExS0YehcQ10P72onHobtiZJ0rdv4crelIznydQa9E0SH0nqNH0mDheCZuKECSYNzW6swWmOyiY2JuW3KRd8mI67CiEYqLla4FXLPapNSkbn-r9kLNFa9RU82GWhiG7IKB7VQiqw_cgfAKdQ4uw9fMKA_1GBRiITCRXLqnw2FgJ4GxGa_4T_EQQvbIer3JkyPy8qkEDBrUFOMntcaEexRiAYr7jTrxmY8H7qMkTE-kpUExISpzTxkifDrhBj4Ow7S';

export function useProfile() {
  const userStore = useUserStore();

  const userInfo = computed(() => userStore.userInfo);
  const defaultAvatar = DEFAULT_AVATAR;

  // 页面显示时检查登录状态
  onShow(() => {
    if (!userStore.token) {
      uni.reLaunch({ url: '/pages/common/login/index' });
    }
  });

  // 编辑个人信息
  function handleEdit() {
    uni.navigateTo({ url: '/pages/profile/edit/index' });
  }

  // 快速操作
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
        title: '功能开发中',
        icon: 'none',
      });
    }
  }

  // 菜单点击
  function handleMenuClick(type: string) {
    const actions: Record<string, () => void> = {
      password: () => {
        uni.navigateTo({ url: '/pages/profile/change-password/index' });
      },
      settings: () => {
        uni.showToast({
          title: '设置功能开发中',
          icon: 'none',
        });
      },
      help: () => {
        uni.showToast({
          title: '帮助与反馈功能开发中',
          icon: 'none',
        });
      },
      about: () => {
        uni.showToast({
          title: '关于我们功能开发中',
          icon: 'none',
        });
      },
    };

    if (actions[type]) {
      actions[type]();
    }
  }

  // 退出登录
  async function handleLogout() {
    uni.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await userStore.logout();
            uni.reLaunch({ url: '/pages/common/login/index' });
          }
          catch (error) {
            console.error('退出登录失败:', error);
            uni.reLaunch({ url: '/pages/common/login/index' });
          }
        }
      },
    });
  }

  return {
    userInfo,
    defaultAvatar,
    handleEdit,
    handleQuickAction,
    handleMenuClick,
    handleLogout,
  };
}
