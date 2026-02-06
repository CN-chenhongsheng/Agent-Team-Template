export interface CommonResult {
  [key: string]: any;
}

/**
 * 文件上传返回对象（单个文件）
 */
export interface UploadFileItem {
  /** 文件访问URL */
  url: string;
  /** 原始文件名 */
  name: string;
  /** 文件大小（字节） */
  size: number;
}

export interface SendCodeParams {
  phone: number;
  code: number;
}

export interface SendCodeResult {
  code: number;
}
