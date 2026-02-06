/**
 * 学生信息补齐工具函数
 * 用于在列表数据加载后，补齐缺失的学生基本信息字段
 *
 * @module utils/student/enrichStudentInfo
 */

import { fetchGetStudentDetail } from '@/api/accommodation-manage'

type StudentListItem = Api.AccommodationManage.StudentListItem

// 学生信息缓存（避免重复请求）
const studentInfoCache = new Map<number, StudentListItem>()

/**
 * 获取学生信息（带缓存）
 */
async function getStudentInfo(id: number): Promise<StudentListItem | null> {
  // 检查缓存
  if (studentInfoCache.has(id)) {
    return studentInfoCache.get(id)!
  }

  try {
    const detail = await fetchGetStudentDetail(id)
    if (detail) {
      studentInfoCache.set(id, detail)
      return detail
    }
    return null
  } catch {
    return null
  }
}

/**
 * 检查列表项是否缺少学生信息字段
 */
function needsStudentInfo(item: any): boolean {
  // 有 studentId、applicantId 或 studentName/applicantName，但缺少关键的学生信息字段
  const hasStudentId = !!item.studentId
  const hasApplicantId = !!item.applicantId
  const hasStudentName = !!item.studentName || !!item.applicantName
  const hasBasicInfo = item.studentNo && item.gender !== undefined && item.phone && item.deptName

  return (hasStudentId || hasApplicantId || hasStudentName) && !hasBasicInfo
}

/**
 * 补齐列表数据中的学生信息
 *
 * @param items 列表数据
 * @returns 补齐后的列表数据
 */
export async function enrichStudentInfo<T extends { studentId?: number; applicantId?: number }>(
  items: T[]
): Promise<T[]> {
  // 如果没有数据，直接返回
  if (!items || items.length === 0) return items

  // 1. 找出需要补齐的记录
  const itemsToEnrich = items.filter(needsStudentInfo)
  if (itemsToEnrich.length === 0) return items

  // 2. 收集所有需要获取的学生ID（去重）
  // 优先使用 studentId，如果没有则使用 applicantId（对于审批实例）
  const studentIds = [
    ...new Set(
      itemsToEnrich
        .map((item) => (item as any).studentId || (item as any).applicantId)
        .filter((id): id is number => !!id)
    )
  ]

  if (studentIds.length === 0) return items

  // 3. 并发获取学生详情
  const studentDetails = await Promise.all(studentIds.map((id) => getStudentInfo(id)))

  // 4. 创建 studentId -> 学生详情的映射
  const studentMap = new Map(
    studentDetails
      .filter((detail): detail is StudentListItem => !!detail)
      .map((detail) => [detail.id, detail])
  )

  // 5. 合并学生信息到列表项
  return items.map((item) => {
    // 获取学生ID（优先使用 studentId，否则使用 applicantId）
    const studentId = (item as any).studentId || (item as any).applicantId
    if (!studentId) return item

    const studentDetail = studentMap.get(studentId)
    if (!studentDetail) return item

    return {
      ...item,
      // 只补充缺失的字段，不覆盖已有字段
      studentNo: (item as any).studentNo || studentDetail.studentNo,
      gender: (item as any).gender ?? studentDetail.gender,
      genderText: (item as any).genderText || studentDetail.genderText,
      phone: (item as any).phone || studentDetail.phone,
      nation: (item as any).nation || studentDetail.nation,
      politicalStatus: (item as any).politicalStatus || studentDetail.politicalStatus,
      campusName: (item as any).campusName || studentDetail.campusName,
      deptName: (item as any).deptName || studentDetail.deptName,
      majorName: (item as any).majorName || studentDetail.majorName,
      className: (item as any).className || studentDetail.className,
      floorName: (item as any).floorName || studentDetail.floorName,
      roomName: (item as any).roomName || studentDetail.roomName,
      bedName: (item as any).bedName || studentDetail.bedName,
      academicStatusText: (item as any).academicStatusText || studentDetail.academicStatusText,
      enrollmentYear: (item as any).enrollmentYear ?? studentDetail.enrollmentYear,
      currentGrade: (item as any).currentGrade || studentDetail.currentGrade
    }
  })
}

/**
 * 清空学生信息缓存
 */
export function clearStudentInfoCache(): void {
  studentInfoCache.clear()
}

/**
 * 清除指定学生的缓存
 */
export function clearStudentInfoCacheById(id: number): void {
  studentInfoCache.delete(id)
}
