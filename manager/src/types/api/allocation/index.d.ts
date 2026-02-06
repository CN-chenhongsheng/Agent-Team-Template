/**
 * 分配管理模块 API 类型定义
 *
 * 包含配置管理、任务管理、结果管理、问卷管理等接口类型
 *
 * @module types/api/allocation
 * @author 陈鸿昇
 * @date 2025-02-02
 */

declare namespace Api {
  /** 分配管理 */
  namespace Allocation {
    // ==================== 配置管理 ====================

    /** 配置搜索参数 */
    interface ConfigSearchParams {
      pageNum: number
      pageSize?: number
      configName?: string
      algorithmType?: string
      status?: number
    }

    /** 配置列表项 */
    interface ConfigListItem {
      /** 配置ID */
      id: number
      /** 配置名称 */
      configName: string
      /** 算法类型 */
      algorithmType: string
      /** 算法类型名称 */
      algorithmTypeName: string
      /** 吸烟约束：1-启用 0-禁用 */
      smokingConstraint: number
      /** 性别约束：1-启用 0-禁用 */
      genderConstraint: number
      /** 作息硬约束：1-启用 0-禁用 */
      sleepHardConstraint: number
      /** 睡眠权重 */
      sleepWeight: number
      /** 吸烟权重 */
      smokingWeight: number
      /** 整洁权重 */
      cleanlinessWeight: number
      /** 社交权重 */
      socialWeight: number
      /** 学习权重 */
      studyWeight: number
      /** 娱乐权重 */
      entertainmentWeight: number
      /** 同院系加分 */
      sameDeptBonus: number
      /** 同专业加分 */
      sameMajorBonus: number
      /** 同班级加分 */
      sameClassBonus: number
      /** 最低匹配分 */
      minMatchScore: number
      /** 状态：1-启用 0-禁用 */
      status: number
      /** 备注 */
      remark?: string
      /** 创建时间 */
      createTime: string
      /** 更新时间 */
      updateTime?: string
    }

    /** 配置分页响应 */
    interface ConfigPageResponse {
      records: ConfigListItem[]
      total: number
      current: number
      size: number
    }

    /** 配置保存参数 */
    interface ConfigSaveParams {
      id?: number
      configName: string
      smokingConstraint: number
      genderConstraint: number
      sleepHardConstraint: number
      sleepWeight: number
      smokingWeight: number
      cleanlinessWeight: number
      socialWeight: number
      studyWeight: number
      entertainmentWeight: number
      algorithmType: string
      sameDeptBonus: number
      sameMajorBonus: number
      sameClassBonus: number
      minMatchScore: number
      remark?: string
    }

    /** 算法选项 */
    interface AlgorithmOption {
      /** 算法类型 */
      type: string
      /** 算法名称 */
      name: string
      /** 算法描述 */
      description?: string
      /** 是否推荐 */
      recommended?: boolean
    }

    // ==================== 任务管理 ====================

    /** 任务搜索参数 */
    interface TaskSearchParams {
      pageNum: number
      pageSize?: number
      taskName?: string
      taskType?: number
      status?: number
    }

    /** 任务列表项 */
    interface TaskListItem {
      /** 任务ID */
      id: number
      /** 任务名称 */
      taskName: string
      /** 任务类型：1-批量分配 2-单个推荐 3-调宿优化 */
      taskType: number
      /** 配置ID */
      configId: number
      /** 配置名称 */
      configName: string
      /** 目标入学年份 */
      targetEnrollmentYear?: number
      /** 目标性别 */
      targetGender?: string
      /** 目标校区ID */
      targetCampusId?: number
      /** 目标校区名称 */
      targetCampusName?: string
      /** 目标楼栋ID */
      targetBuildingId?: number
      /** 目标楼栋名称 */
      targetBuildingName?: string
      /** 总学生数 */
      totalStudents: number
      /** 已分配数 */
      allocatedCount: number
      /** 已确认数 */
      confirmedCount: number
      /** 平均匹配分 */
      avgMatchScore?: number
      /** 状态：0-待执行 1-执行中 2-已完成 3-部分确认 4-全部确认 5-已取消 */
      status: number
      /** 状态名称 */
      statusName?: string
      /** 创建时间 */
      createTime: string
      /** 执行时间 */
      executeTime?: string
      /** 完成时间 */
      finishTime?: string
    }

    /** 任务分页响应 */
    interface TaskPageResponse {
      records: TaskListItem[]
      total: number
      current: number
      size: number
    }

    /** 任务保存参数 */
    interface TaskSaveParams {
      taskName: string
      taskType: number
      configId: number
      targetEnrollmentYear?: number
      targetGender?: string
      targetCampusId?: number
      targetBuildingId?: number
      remark?: string
    }

    /** 任务预览参数 */
    interface TaskPreviewParams {
      configId: number
      targetEnrollmentYear?: number
      targetGender?: string
      targetCampusId?: number
      targetBuildingId?: number
    }

    /** 任务预览结果 */
    interface TaskPreviewResult {
      studentCount: number
      bedCount: number
      estimatedTime: number
      warnings?: string[]
    }

    /** 任务进度 */
    interface TaskProgress {
      taskId: number
      status: number
      progressPercent: number
      currentStage?: string
      successCount?: number
      failedCount?: number
      errorMessage?: string
      completed: boolean
    }

    // ==================== 结果管理 ====================

    /** 结果搜索参数 */
    interface ResultSearchParams {
      pageNum: number
      pageSize?: number
      taskId?: number
      studentNo?: string
      studentName?: string
      roomCode?: string
      status?: number
      problemOnly?: boolean
    }

    /** 结果列表项 */
    interface ResultListItem {
      /** 结果ID */
      id: number
      /** 任务ID */
      taskId: number
      /** 学生ID */
      studentId: number
      /** 学号 */
      studentNo: string
      /** 学生姓名 */
      studentName: string
      /** 分配楼层 */
      allocatedFloorId?: number
      /** 分配楼层编码 */
      allocatedFloorCode?: string
      /** 分配房间ID */
      allocatedRoomId?: number
      /** 分配房间编码 */
      allocatedRoomCode?: string
      /** 分配床位ID */
      allocatedBedId?: number
      /** 分配床位编码 */
      allocatedBedCode?: string
      /** 匹配分数 */
      matchScore: number
      /** 匹配等级 */
      matchScoreLevel?: string
      /** 室友姓名列表 */
      roommateNames?: string[]
      /** 匹配优势 */
      advantages?: string[]
      /** 冲突原因 */
      conflictReasons?: string[]
      /** 状态：0-待确认 1-已确认 2-已拒绝 3-已调整 */
      status: number
      /** 确认时间 */
      confirmTime?: string
      /** 拒绝原因 */
      rejectReason?: string
    }

    /** 结果分页响应 */
    interface ResultPageResponse {
      records: ResultListItem[]
      total: number
      current: number
      size: number
    }

    // ==================== 问卷管理 ====================

    /** 问卷搜索参数 */
    interface SurveySearchParams {
      pageNum: number
      pageSize?: number
      studentName?: string
      studentNo?: string
      classCode?: string
      enrollmentYear?: number
      fillStatus?: string
    }

    /** 问卷列表项 */
    interface SurveyListItem {
      /** 学生ID */
      studentId: number
      /** 学号 */
      studentNo: string
      /** 学生姓名 */
      studentName: string
      /** 班级名称 */
      className: string
      /** 入学年份 */
      enrollmentYear: number
      /** 填写状态：filled-已填写 unfilled-未填写 */
      fillStatus: string
      /** 填写时间 */
      fillTime?: string
      /** 作息时间描述 */
      sleepSchedule?: string
      /** 吸烟状态描述 */
      smokingStatus?: string
    }

    /** 问卷分页响应 */
    interface SurveyPageResponse {
      records: SurveyListItem[]
      total: number
      current: number
      size: number
    }

    /** 问卷统计 */
    interface SurveyStatistics {
      total: number
      filled: number
      unfilled: number
      fillRate: number
    }

    /** 问卷详情 */
    interface SurveyDetail {
      studentId: number
      studentNo: string
      studentName: string
      className: string
      fillTime?: string
      // 作息习惯
      sleepSchedule?: number
      sleepScheduleText?: string
      sleepQuality?: number
      snores?: number
      sensitiveToLight?: number
      sensitiveToSound?: number
      // 生活习惯
      smokingStatus?: number
      smokingStatusText?: string
      smokingTolerance?: number
      cleanlinessLevel?: number
      cleanlinessLevelText?: string
      bedtimeCleanup?: number
      eatInRoom?: number
      // 社交偏好
      socialPreference?: number
      socialPreferenceText?: string
      allowVisitors?: number
      phoneCallTime?: number
      // 学习娱乐
      studyInRoom?: number
      studyEnvironment?: number
      computerUsageTime?: number
      gamingPreference?: number
      musicPreference?: number
      musicVolume?: number
    }
  }
}
