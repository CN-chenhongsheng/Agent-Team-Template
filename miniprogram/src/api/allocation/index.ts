import { get, post } from '@/utils/request'
import type {
  ISurveyStatus,
  ISurveyData,
  ISurveyVO,
  IAllocationResult,
  IRoommateInfo,
  IBedRecommend,
} from '@/types/api'

// ==================== 问卷相关 ====================

/** 获取问卷状态 */
export const getSurveyStatus = () =>
  get<ISurveyStatus>({ url: '/v1/app/lifestyle/status' })

/** 提交问卷 */
export const submitSurvey = (data: ISurveyData) =>
  post<void>({ url: '/v1/app/lifestyle/submit', data })

/** 获取已填写的问卷数据 */
export const getSurveyData = () =>
  get<ISurveyVO>({ url: '/v1/app/lifestyle/data' })

/** 检查是否可以修改问卷 */
export const canModifySurvey = () =>
  get<boolean>({ url: '/v1/app/lifestyle/can-modify' })

// ==================== 分配结果 ====================

/** 查看我的分配结果 */
export const getMyAllocationResult = () =>
  get<IAllocationResult>({ url: '/v1/app/allocation/my-result' })

/** 查看室友信息 */
export const getRoommatesInfo = () =>
  get<IRoommateInfo[]>({ url: '/v1/app/allocation/roommates' })

// ==================== 床位推荐 ====================

/** 推荐床位（未分配学生） */
export const getRecommendBeds = (limit: number = 5) =>
  get<IBedRecommend[]>({ url: '/v1/app/recommend/beds', params: { limit } })

/** 调宿推荐 */
export const getTransferRecommend = (limit: number = 5) =>
  get<IBedRecommend[]>({ url: '/v1/app/recommend/transfer', params: { limit } })
