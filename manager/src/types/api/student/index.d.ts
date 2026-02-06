/**
 * API 学生管理类型定义
 *
 * 包括学生信息管理、学生生活习惯等相关的类型定义
 *
 * @module types/api/student
 * @author 陈鸿昇
 */

declare namespace Api {
  /** 学生管理类型 */
  namespace StudentManage {
    /** 学生查询参数 */
    interface StudentSearchParams {
      studentNo?: string
      studentName?: string
      phone?: string
      gender?: number
      campusCode?: string
      deptCode?: string
      majorCode?: string
      classId?: number
      bedId?: number
      academicStatus?: number
      status?: number
      pageNum?: number
      pageSize?: number
    }

    /** 学生保存参数 */
    interface StudentSaveParams {
      id?: number
      studentNo: string
      studentName: string
      gender?: number
      idCard?: string
      phone?: string
      email?: string
      birthDate?: string
      nation?: string
      politicalStatus?: string
      enrollmentYear?: number
      schoolingLength?: number
      currentGrade?: string
      academicStatus?: number
      homeAddress?: string
      emergencyContact?: string
      emergencyPhone?: string
      parentName?: string
      parentPhone?: string
      campusCode?: string
      deptCode?: string
      majorCode?: string
      classId?: number
      classCode?: string
      floorId?: number
      floorCode?: string
      roomId?: number
      roomCode?: string
      bedId?: number
      bedCode?: string
      status?: number
      remark?: string
      // 生活习惯字段
      smokingStatus?: number
      smokingTolerance?: number
      sleepSchedule?: number
      sleepQuality?: number
      snores?: number
      sensitiveToLight?: number
      sensitiveToSound?: number
      cleanlinessLevel?: number
      bedtimeCleanup?: number
      socialPreference?: number
      allowVisitors?: number
      phoneCallTime?: number
      studyInRoom?: number
      studyEnvironment?: number
      computerUsageTime?: number
      gamingPreference?: number
      musicPreference?: number
      musicVolume?: number
      eatInRoom?: number
      specialNeeds?: string
      roommatePreference?: string
    }

    /** 学生详情（继承列表项的所有字段，用于详情页） */
    type StudentDetail = StudentListItem

    /**
     * 学生列表项
     *
     * @remarks
     * **后端数据约定**：当返回 `studentName` 字段时，必须同时返回以下字段（用于表格姓名列的悬浮卡片显示）：
     * - `studentNo` - 学号
     * - `gender` / `genderText` - 性别
     * - `phone` - 手机号
     * - `nation` - 民族
     * - `politicalStatus` - 政治面貌
     * - `campusName` - 校区
     * - `deptName` - 院系
     * - `majorName` - 专业
     * - `className` - 班级
     * - `floorName` - 楼栋
     * - `roomName` - 房间
     * - `bedName` - 床位
     * - `academicStatusText` - 学籍状态
     * - `enrollmentYear` - 入学年份
     * - `currentGrade` - 当前年级
     */
    interface StudentListItem {
      id: number
      studentNo: string
      studentName: string
      gender?: number
      genderText?: string
      idCard?: string
      phone?: string
      email?: string
      birthDate?: string
      nation?: string
      politicalStatus?: string
      enrollmentYear?: number
      schoolingLength?: number
      currentGrade?: string
      academicStatus?: number
      academicStatusText?: string
      homeAddress?: string
      emergencyContact?: string
      emergencyPhone?: string
      parentName?: string
      parentPhone?: string
      campusCode?: string
      campusName?: string
      deptCode?: string
      deptName?: string
      majorCode?: string
      majorName?: string
      classId?: number
      classCode?: string
      className?: string
      floorId?: number
      floorCode?: string
      floorName?: string
      roomId?: number
      roomCode?: string
      roomName?: string
      bedId?: number
      bedCode?: string
      bedName?: string
      status: number
      statusText?: string
      remark?: string
      createTime?: string
      updateTime?: string
      // 生活习惯字段
      smokingStatus?: number
      smokingStatusText?: string
      smokingTolerance?: number
      smokingToleranceText?: string
      sleepSchedule?: number
      sleepScheduleText?: string
      sleepQuality?: number
      sleepQualityText?: string
      snores?: number
      snoresText?: string
      sensitiveToLight?: number
      sensitiveToLightText?: string
      sensitiveToSound?: number
      sensitiveToSoundText?: string
      cleanlinessLevel?: number
      cleanlinessLevelText?: string
      bedtimeCleanup?: number
      bedtimeCleanupText?: string
      socialPreference?: number
      socialPreferenceText?: string
      allowVisitors?: number
      allowVisitorsText?: string
      phoneCallTime?: number
      phoneCallTimeText?: string
      studyInRoom?: number
      studyInRoomText?: string
      studyEnvironment?: number
      studyEnvironmentText?: string
      computerUsageTime?: number
      computerUsageTimeText?: string
      gamingPreference?: number
      gamingPreferenceText?: string
      musicPreference?: number
      musicPreferenceText?: string
      musicVolume?: number
      musicVolumeText?: string
      eatInRoom?: number
      eatInRoomText?: string
      specialNeeds?: string
      roommatePreference?: string
    }

    /** 学生分页响应 */
    interface StudentPageResponse {
      list: StudentListItem[]
      total: number
      pageNum: number
      pageSize: number
      totalPages: number
    }
  }

  /** 学生导入相关类型 */
  namespace StudentImport {
    /** 导入错误明细 */
    interface ImportError {
      row: number
      column: string
      message: string
      value?: string
    }

    /** 导入结果 */
    interface ImportResult {
      totalRows: number
      successCount: number
      failCount: number
      errors: ImportError[]
    }

    /** 异步导入任务 VO */
    interface ImportTaskVO {
      taskId: string
      status: 'pending' | 'processing' | 'success' | 'failed'
      progressPercent?: number
      result?: ImportResult
    }

    /** 学生导入请求参数 */
    interface ImportRequest {
      /** 分片上传 merge 返回的文件访问 URL */
      fileUrl: string
      /** 前端扫描得到的有效行数（可选） */
      totalRows?: number
    }

    /** 导入接口返回：同步为 ImportResult，异步为 { taskId: string } */
    type ImportResponse = ImportResult | { taskId: string }
  }
}
