// 引入配置
import type { HttpRequestConfig, HttpResponse } from 'uview-plus/libs/luch-request/index';
import Request from 'uview-plus/libs/luch-request/index';
import { requestInterceptors, responseInterceptors } from './interceptors';
import type { IResponse } from './type';

const http = new Request();

// 引入拦截器配置
export function setupRequest() {
  http.setConfig((defaultConfig: HttpRequestConfig) => {
    /* defaultConfig 为默认全局配置 */
    let baseURL = import.meta.env.VITE_APP_BASE_API || '';
    // 去除前后空格
    baseURL = baseURL.trim();

    // 处理 baseURL：如果以 /api 结尾，且 API 路径也以 /api 开头，则去掉 baseURL 末尾的 /api
    // 因为所有 API 路径都已经包含了 /api 前缀
    if (baseURL.endsWith('/api') && !baseURL.endsWith('/api/')) {
      baseURL = baseURL.slice(0, -4); // 去掉末尾的 '/api'
    }

    // 确保 baseURL 不为空
    if (!baseURL) {
      console.error('[Request] baseURL 为空，请检查环境变量 VITE_APP_BASE_API');
      baseURL = 'http://localhost:8080'; // 默认值
    }

    defaultConfig.baseURL = baseURL;

    // 启用 Cookie 支持（用于自动发送 HttpOnly refresh token）
    // WeChat miniprogram 会自动处理 Cookie，无需额外配置
    // 但需要确保后端 CORS 配置允许 credentials
    defaultConfig.withCredentials = true;

    return defaultConfig;
  });

  requestInterceptors(http);
  responseInterceptors(http);
}

export function request<T = any>(config: HttpRequestConfig): Promise<T> {
  // 确保 baseURL 被设置
  if (!config.baseURL) {
    const baseURL = (http as any).config?.baseURL || '';
    if (baseURL) {
      config.baseURL = baseURL;
    }
  }
  return new Promise((resolve, reject) => {
    http.request(config).then((res: HttpResponse<IResponse<T>> | any) => {
      // uview-plus 的 luch-request 库中，响应拦截器返回的值会替换整个 response
      // res 本身就是拦截器返回的值（data.data），直接使用
      resolve(res as T);
    }).catch((error) => {
      console.error('[Request] http.request 捕获错误', {
        url: config.url,
        error,
        errorMsg: error?.errMsg || error?.message,
      });
      reject(error);
    });
  });
}

export function get<T = any>(config: HttpRequestConfig): Promise<T> {
  return request({ ...config, method: 'GET' });
}

export function post<T = any>(config: HttpRequestConfig): Promise<T> {
  return request({ ...config, method: 'POST' });
}

export function put<T = any>(config: HttpRequestConfig): Promise<T> {
  return request({ ...config, method: 'PUT' });
}

export function upload<T = any>(config: HttpRequestConfig): Promise<T> {
  return request({ ...config, method: 'UPLOAD' });
}

export function download<T = any>(config: HttpRequestConfig): Promise<T> {
  return request({ ...config, method: 'DOWNLOAD' });
}

export default setupRequest;
