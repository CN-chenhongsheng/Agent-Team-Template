import type {
  HttpError,
  HttpRequestAbstract,
  HttpRequestConfig,
  HttpResponse,
} from 'uview-plus/libs/luch-request/index';
import { showMessage } from '../status';
import { getToken } from '@/utils/auth';
import storage from '@/utils/storage';
import useUserStore from '@/store/modules/user';
import { refreshTokenAPI } from '@/api';
import { ROUTE_CONSTANTS } from '@/constants';

// 是否正在刷新token的标记
let isRefreshing: boolean = false;
// 重试队列，每一项将是一个待执行的函数形式
let requestQueue: (() => void)[] = [];
// 刷新失败次数
let refreshFailCount: number = 0;
const MAX_REFRESH_FAIL_COUNT = 3; // 最大失败次数

function requestInterceptors(http: HttpRequestAbstract) {
  /**
   * 请求拦截
   * @param {object} http
   */
  http.interceptors.request.use(
    (config: HttpRequestConfig) => {
      // 可使用async await 做异步操作
      // 初始化请求拦截器时，会执行此方法，此时data为undefined，赋予默认{}
      config.data = config.data || {};

      // 从多个地方尝试获取 baseURL
      let baseURL = config.baseURL || (http as any).config?.baseURL || '';
      // 如果还是为空，尝试从环境变量读取
      if (!baseURL) {
        baseURL = (import.meta.env.VITE_APP_BASE_API || '').trim();
        if (baseURL.endsWith('/api') && !baseURL.endsWith('/api/')) {
          baseURL = baseURL.slice(0, -4);
        }
        // 如果获取到了，设置到 config 中
        if (baseURL) {
          config.baseURL = baseURL;
        }
      }

      // 是否需要设置 token
      const isToken = config.custom?.auth === false;
      // 是否需要防止数据重复提交
      const isRepeatSubmit = config.custom?.repeatSubmit === false;
      if (getToken() && !isToken && config.header) {
        // token设置 - 使用 Authorization 头，Bearer 格式（与后端认证拦截器匹配）
        config.header.Authorization = `Bearer ${getToken()}`;
      }

      if (!isRepeatSubmit && (config.method === 'POST' || config.method === 'UPLOAD')) {
        const requestObj = {
          url: config.url,
          data: typeof config.data === 'object' ? JSON.stringify(config.data) : config.data,
          time: new Date().getTime(),
        };
        const sessionObj = storage.getJSON('sessionObj') as { url?: string; data?: string; time?: number } | null;
        if (!sessionObj) {
          storage.setJSON('sessionObj', requestObj);
        }
        else {
          const s_url = sessionObj.url; // 请求地址
          const s_data = sessionObj.data; // 请求数据
          const s_time = sessionObj.time ?? 0; // 请求时间
          const interval = 1000; // 间隔时间(ms)，小于此时间视为重复提交
          if (s_data === requestObj.data && requestObj.time - s_time < interval && s_url === requestObj.url) {
            const message = '数据正在处理，请勿重复提交';
            console.warn(`[${s_url}]: ${message}`);
            return Promise.reject(new Error(message));
          }
          else {
            storage.setJSON('sessionObj', requestObj);
          }
        }
      }
      return config;
    },
    (config: any) => { // 可使用async await 做异步操作
      return Promise.reject(config);
    },
  );
}
function responseInterceptors(http: HttpRequestAbstract) {
  /**
   * 响应拦截
   * @param {object} http
   */
  http.interceptors.response.use(
    async (response: HttpResponse) => {
      /* 对响应成功做点什么 可使用async await 做异步操作 */
      const data = response.data;
      // 配置参数
      const config = response.config;
      // 自定义参数
      const custom = config?.custom;

      // 请求成功则返回结果
      if (data.code === 200) {
        // 直接返回 data 字段的内容
        return data.data !== undefined ? data.data : data;
      }

      // 登录状态失效，尝试刷新token或跳转登录
      if (data.code === 401) {
        // 是否在刷新token中,防止重复刷新
        if (!isRefreshing) {
          isRefreshing = true;
          const userStore = useUserStore();

          try {
            // 调用刷新接口（refresh token 通过 HttpOnly Cookie 自动发送）
            const newAccessToken = await refreshTokenAPI();

            // 更新 access token（refresh token 由后端通过 Cookie 管理，前端无需处理）
            userStore.setToken(newAccessToken, '');

            // 刷新成功，重置失败计数
            refreshFailCount = 0;

            // 刷新成功，执行队列请求
            requestQueue.forEach(cb => cb());
            requestQueue = [];
            isRefreshing = false;

            // 重新执行本次请求
            return http.request(config);
          }
          catch (error) {
            // 增加失败计数
            refreshFailCount += 1;

            // 刷新token失败，清除登录状态并跳转登录页
            userStore.resetInfo();
            requestQueue = [];
            isRefreshing = false;

            // 根据失败次数显示不同提示
            const message = refreshFailCount >= MAX_REFRESH_FAIL_COUNT
              ? '多次登录失败，请稍后重试'
              : '登录已过期，请重新登录';

            // 显示提示后跳转
            uni.showToast({
              title: message,
              icon: 'none',
              duration: 1500,
              mask: true,
              success() {
                // Toast 显示成功后立即跳转
                setTimeout(() => {
                  // 重置计数
                  refreshFailCount = 0;
                  // 跳转到登录页
                  uni.reLaunch({
                    url: ROUTE_CONSTANTS.LOGIN,
                    fail(err) {
                      console.error('[Interceptor] 跳转登录页失败:', err);
                      // 尝试使用 redirectTo
                      uni.redirectTo({
                        url: ROUTE_CONSTANTS.LOGIN,
                        fail(err2) {
                          console.error('[Interceptor] redirectTo 也失败:', err2);
                        },
                      });
                    },
                  });
                }, 100);
              },
            });

            return Promise.reject(error);
          }
        }
        else {
          // 正在刷新中，将请求加入队列
          return new Promise((resolve, reject) => {
            requestQueue.push(() => {
              http.request(config).then(resolve).catch(reject);
            });
          });
        }
      }

      // 如果没有显式定义custom的toast参数为false的话，默认对报错进行toast弹出提示
      if (custom?.toast !== false)
        uni.$u.toast(data.message);

      // 如果需要catch返回，则进行reject
      if (custom?.catch) {
        return Promise.reject(data);
      }
      else {
        // 否则返回一个pending中的promise
        return new Promise(() => { });
      }
    },
    async (response: HttpError) => {
      // 处理 HTTP 401 状态码（Token 无效或过期）
      if (response.statusCode === 401) {
        const config = response.config;

        // 是否在刷新token中,防止重复刷新
        if (!isRefreshing) {
          isRefreshing = true;
          const userStore = useUserStore();

          try {
            // 调用刷新接口（refresh token 通过 HttpOnly Cookie 自动发送）
            const newAccessToken = await refreshTokenAPI();

            // 更新 access token（refresh token 由后端通过 Cookie 管理，前端无需处理）
            userStore.setToken(newAccessToken, '');

            // 刷新成功，重置失败计数
            refreshFailCount = 0;

            // 刷新成功，执行队列请求
            requestQueue.forEach(cb => cb());
            requestQueue = [];
            isRefreshing = false;

            // 重新执行本次请求
            return http.request(config);
          }
          catch (error) {
            // 增加失败计数
            refreshFailCount += 1;

            // 刷新token失败，清除登录状态并跳转登录页
            userStore.resetInfo();
            requestQueue = [];
            isRefreshing = false;

            // 根据失败次数显示不同提示
            const message = refreshFailCount >= MAX_REFRESH_FAIL_COUNT
              ? '多次登录失败，请稍后重试'
              : '登录已过期，请重新登录';

            // 显示提示后跳转
            uni.showToast({
              title: message,
              icon: 'none',
              duration: 1500,
              mask: true,
              success() {
                // Toast 显示成功后立即跳转
                setTimeout(() => {
                  // 重置计数
                  refreshFailCount = 0;
                  // 跳转到登录页
                  uni.reLaunch({
                    url: ROUTE_CONSTANTS.LOGIN,
                    fail(err) {
                      console.error('[Interceptor] 跳转登录页失败:', err);
                      // 尝试使用 redirectTo
                      uni.redirectTo({
                        url: ROUTE_CONSTANTS.LOGIN,
                        fail(err2) {
                          console.error('[Interceptor] redirectTo 也失败:', err2);
                        },
                      });
                    },
                  });
                }, 100);
              },
            });

            return Promise.reject(error);
          }
        }
        else {
          // 正在刷新中，将请求加入队列
          return new Promise((resolve, reject) => {
            requestQueue.push(() => {
              http.request(config).then(resolve).catch(reject);
            });
          });
        }
      }

      // 处理其他 HTTP 错误状态码
      if (response.statusCode) {
        // 请求已发出，但是不在2xx的范围
        showMessage(response.statusCode);
        return Promise.reject(response.data);
      }
      showMessage('网络连接异常,请稍后再试!');
      return Promise.reject(response);
    },
  );
}

export { requestInterceptors, responseInterceptors };
