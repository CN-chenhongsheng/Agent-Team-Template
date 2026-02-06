/**
 * API 住宿管理类型定义
 *
 * 包括学生管理、入住管理、调宿管理、退宿管理、留宿管理等住宿业务相关的类型定义
 *
 * @module types/api/accommodation
 * @author 陈鸿昇
 */

declare namespace Api {
  /** 住宿管理类型 */
  namespace AccommodationManage {
    /** ==================== 人员管理（学生） ==================== */
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

    /** ==================== 入住管理 ==================== */
    /** 入住管理查询参数 */
    interface CheckInSearchParams {
      studentNo?: string
      studentName?: string
      studentId?: number
      checkInType?: number
      campusCode?: string
      bedId?: number
      status?: number
      applyDateStart?: string
      applyDateEnd?: string
      pageNum?: number
      pageSize?: number
    }

    /** 入住管理保存参数 */
    interface CheckInSaveParams {
      id?: number
      studentId: number
      checkInType: number
      campusCode?: string
      floorCode?: string
      roomId?: number
      roomCode?: string
      bedId?: number
      bedCode?: string
      applyDate?: string
      checkInDate?: string
      expectedCheckOutDate?: string
      status?: number
      approverId?: number
      approverName?: string
      approveOpinion?: string
      applyReason?: string
      remark?: string
    }

    /** 入住管理列表项 */
    interface CheckInListItem {
      id: number
      studentId: number
      studentInfo?: Api.Common.StudentBasicInfo
      checkInType: number
      checkInTypeText?: string
      roomId?: number
      roomCode?: string
      bedId?: number
      bedCode?: string
      applyDate?: string
      checkInDate?: string
      expectedCheckOutDate?: string
      status: number
      statusText?: string
      approvalInstanceId?: number
      approvalProgress?: Api.Common.ApprovalProgress
      approverId?: number
      approverName?: string
      approveTime?: string
      approveOpinion?: string
      applyReason?: string
      remark?: string
      createTime?: string
      updateTime?: string
    }

    /** 入住管理分页响应 */
    interface CheckInPageResponse {
      list: CheckInListItem[]
      total: number
      pageNum: number
      pageSize: number
      totalPages: number
    }

    /** ==================== 调宿管理 ==================== */
    /** 调宿管理查询参数 */
    interface TransferSearchParams {
      studentNo?: string
      studentName?: string
      studentId?: number
      originalCampusCode?: string
      targetCampusCode?: string
      status?: number
      applyDateStart?: string
      applyDateEnd?: string
      pageNum?: number
      pageSize?: number
    }

    /** 调宿管理保存参数 */
    interface TransferSaveParams {
      id?: number
      studentId: number
      originalCampusCode?: string
      originalFloorCode?: string
      originalRoomId?: number
      originalRoomCode?: string
      originalBedId?: number
      originalBedCode?: string
      targetCampusCode?: string
      targetFloorCode?: string
      targetRoomId?: number
      targetRoomCode?: string
      targetBedId?: number
      targetBedCode?: string
      applyDate?: string
      transferDate?: string
      status?: number
      approverId?: number
      approverName?: string
      approveOpinion?: string
      transferReason?: string
      remark?: string
    }

    /** 调宿管理列表项 */
    interface TransferListItem {
      id: number
      studentId: number
      studentInfo?: Api.Common.StudentBasicInfo
      originalCampusCode?: string
      originalCampusName?: string
      originalFloorCode?: string
      originalFloorName?: string
      originalRoomId?: number
      originalRoomCode?: string
      originalRoomName?: string
      originalBedId?: number
      originalBedCode?: string
      originalBedName?: string
      targetCampusCode?: string
      targetCampusName?: string
      targetFloorCode?: string
      targetFloorName?: string
      targetRoomId?: number
      targetRoomCode?: string
      targetRoomName?: string
      targetBedId?: number
      targetBedCode?: string
      targetBedName?: string
      applyDate?: string
      transferDate?: string
      status: number
      statusText?: string
      approvalInstanceId?: number
      approvalProgress?: Api.Common.ApprovalProgress
      approverId?: number
      approverName?: string
      approveTime?: string
      approveOpinion?: string
      transferReason?: string
      remark?: string
      createTime?: string
      updateTime?: string
    }

    /** 调宿管理分页响应 */
    interface TransferPageResponse {
      list: TransferListItem[]
      total: number
      pageNum: number
      pageSize: number
      totalPages: number
    }

    /** ==================== 退宿管理 ==================== */
    /** 退宿管理查询参数 */
    interface CheckOutSearchParams {
      studentNo?: string
      studentName?: string
      studentId?: number
      campusCode?: string
      bedId?: number
      status?: number
      applyDateStart?: string
      applyDateEnd?: string
      pageNum?: number
      pageSize?: number
    }

    /** 退宿管理保存参数 */
    interface CheckOutSaveParams {
      id?: number
      studentId: number
      campusCode?: string
      floorCode?: string
      roomId?: number
      roomCode?: string
      bedId?: number
      bedCode?: string
      applyDate?: string
      checkOutDate?: string
      status?: number
      approverId?: number
      approverName?: string
      approveOpinion?: string
      checkOutReason: string
      remark?: string
    }

    /** 退宿管理列表项 */
    interface CheckOutListItem {
      id: number
      studentId: number
      studentInfo?: Api.Common.StudentBasicInfo
      roomId?: number
      roomCode?: string
      bedId?: number
      bedCode?: string
      applyDate?: string
      checkOutDate?: string
      status: number
      statusText?: string
      approvalInstanceId?: number
      approvalProgress?: Api.Common.ApprovalProgress
      approverId?: number
      approverName?: string
      approveTime?: string
      approveOpinion?: string
      checkOutReason: string
      remark?: string
      createTime?: string
      updateTime?: string
    }

    /** 退宿管理分页响应 */
    interface CheckOutPageResponse {
      list: CheckOutListItem[]
      total: number
      pageNum: number
      pageSize: number
      totalPages: number
    }

    /** ==================== 留宿管理 ==================== */
    /** 留宿管理查询参数 */
    interface StaySearchParams {
      studentNo?: string
      studentName?: string
      studentId?: number
      campusCode?: string
      bedId?: number
      status?: number
      applyDateStart?: string
      applyDateEnd?: string
      stayStartDateStart?: string
      stayStartDateEnd?: string
      pageNum?: number
      pageSize?: number
    }

    /** 留宿管理保存参数 */
    interface StaySaveParams {
      id?: number
      studentId: number
      campusCode?: string
      floorCode?: string
      roomId?: number
      roomCode?: string
      bedId?: number
      bedCode?: string
      applyDate?: string
      stayStartDate?: string
      stayEndDate?: string
      status?: number
      approverId?: number
      approverName?: string
      approveOpinion?: string
      stayReason: string
      remark?: string
      parentName?: string
      parentPhone?: string
      parentAgree?: string
      signature?: string
      images?: string
    }

    /** 留宿管理列表项 */
    interface StayListItem {
      id: number
      studentId: number
      studentInfo?: Api.Common.StudentBasicInfo
      roomId?: number
      roomCode?: string
      bedId?: number
      bedCode?: string
      applyDate?: string
      stayStartDate?: string
      stayEndDate?: string
      status: number
      statusText?: string
      approvalInstanceId?: number
      approvalProgress?: Api.Common.ApprovalProgress
      approverId?: number
      approverName?: string
      approveTime?: string
      approveOpinion?: string
      stayReason: string
      remark?: string
      parentName?: string
      parentPhone?: string
      parentAgree?: string
      parentAgreeText?: string
      signature?: string
      images?: string
      createTime?: string
      updateTime?: string
    }

    /** 留宿管理分页响应 */
    interface StayPageResponse {
      list: StayListItem[]
      total: number
      pageNum: number
      pageSize: number
      totalPages: number
    }
  }
}
