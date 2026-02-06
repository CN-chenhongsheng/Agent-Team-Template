/**
 * 通用接口
 */
import type { SendCodeParams, SendCodeResult, UploadFileItem } from './types';
import { post } from '@/utils/request';
import { TokenPrefix, getToken } from '@/utils/auth';

enum URL {
  sendCode = '/sendCode',
  /** 统一文件上传接口 */
  fileUpload = '/api/v1/common/files/upload',
}

/**
 * 统一图片上传接口（支持签名、附件等）
 * @param filePath 图片文件路径
 * @returns Promise<string[]> 返回图片 URL 数组
 */
export const uploadImageAPI = (filePath: string): Promise<string[]> => {
  return new Promise((resolve, reject) => {
    const token = getToken();
    const baseUrl = import.meta.env.VITE_APP_BASE_API || '';

    uni.uploadFile({
      url: baseUrl + URL.fileUpload,
      filePath,
      name: 'file',
      header: {
        Authorization: TokenPrefix + token,
      },
      success: (res) => {
        if (res.statusCode !== 200) {
          reject(new Error(`HTTP ${res.statusCode}: 请求失败`));
          return;
        }

        try {
          const data = JSON.parse(res.data);
          if (data.code === 200) {
            // 后端返回 [{ url, name, size }]，提取 url 数组
            const fileList: UploadFileItem[] = data.data || [];
            const urls = fileList.map(item => item.url);
            resolve(urls);
          }
          else {
            reject(new Error(data.message || '上传失败'));
          }
        }
        catch (e) {
          reject(new Error('服务器返回格式错误'));
        }
      },
      fail: (err) => {
        reject(new Error(err.errMsg || '上传失败'));
      },
    });
  });
};

/**
 * 签名图片上传（统一使用 uploadImageAPI）
 * @param filePath 图片文件路径
 * @returns Promise<string[]> 返回图片 URL 数组
 */
export const uploadSignatureAPI = uploadImageAPI;

// 发送验证码
export const sendCode = (data: SendCodeParams) => post<SendCodeResult>({ url: URL.sendCode, data });
