/**
 * 大文件切片上传 - API 接口
 *
 * @module api/chunk-upload
 * @author 陈鸿昇
 */

import request from '@/utils/http'
import {
  API_PRECHECK,
  API_CHUNK,
  API_VERIFY,
  API_MERGE,
  API_ABORT
} from '@/utils/upload/constants'
import type {
  PrecheckRequest,
  PrecheckResponse,
  MergeRequest,
  MergeResponse
} from '@/utils/upload/types'

/**
 * 预检接口
 *
 * 检查文件是否可以秒传，或返回已上传的分片列表用于断点续传
 *
 * @param data 预检参数
 * @returns 预检结果
 */
export function fetchUploadPrecheck(data: PrecheckRequest) {
  return request.post<PrecheckResponse>({
    url: API_PRECHECK,
    data,
    showErrorMessage: true
  })
}

/**
 * 上传单个分片
 *
 * @param file 分片文件
 * @param fileHash 文件哈希
 * @param chunkIndex 分片索引
 * @param totalChunks 分片总数
 * @returns void
 */
export function fetchUploadChunk(
  file: Blob,
  fileHash: string,
  chunkIndex: number,
  totalChunks: number
) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('fileHash', fileHash)
  formData.append('chunkIndex', String(chunkIndex))
  formData.append('totalChunks', String(totalChunks))

  return request.post<void>({
    url: API_CHUNK,
    data: formData,
    showErrorMessage: false // 单个分片失败不显示错误，由上传器处理重试
  })
}

/**
 * 校验已上传的分片
 *
 * 返回服务端实际存在的分片索引列表，用于双端校验
 *
 * @param fileHash 文件哈希
 * @param totalChunks 分片总数
 * @returns 已上传的分片索引列表
 */
export function fetchVerifyChunks(fileHash: string, totalChunks: number) {
  return request.post<number[]>({
    url: API_VERIFY,
    data: { fileHash, totalChunks },
    showErrorMessage: true
  })
}

/**
 * 合并分片
 *
 * 所有分片上传完成后调用，服务端将分片合并为完整文件
 *
 * @param data 合并参数
 * @returns 合并结果（文件访问 URL 等信息）
 */
export function fetchMergeChunks(data: MergeRequest) {
  return request.post<MergeResponse>({
    url: API_MERGE,
    data,
    showErrorMessage: true,
    showSuccessMessage: true,
    successMessage: '文件上传成功'
  })
}

/**
 * 取消上传
 *
 * 清理服务端临时分片文件
 *
 * @param fileHash 文件哈希
 * @returns void
 */
export function fetchAbortUpload(fileHash: string) {
  return request.post<void>({
    url: API_ABORT,
    data: { fileHash },
    showErrorMessage: false
  })
}
