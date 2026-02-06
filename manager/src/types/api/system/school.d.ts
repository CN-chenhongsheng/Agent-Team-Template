/**
 * API 系统管理 - 学校管理类型定义
 *
 * 包括校区、院系、专业、班级、学年等学校组织结构相关的类型定义
 *
 * @module types/api/system/school
 * @author 陈鸿昇
 */

declare namespace Api {
  namespace SystemManage {
    /** ==================== 校区管理 ==================== */
    /** 校区查询参数 */
    interface CampusSearchParams {
      campusCode?: string
      campusName?: string
      status?: number
      pageNum?: number
      pageSize?: number
    }

    /** 校区保存参数 */
    interface CampusSaveParams {
      id?: number
      campusCode: string
      campusName: string
      address: string
      manager?: string
      status: number
      sort?: number
    }

    /** 校区列表项 */
    interface CampusListItem {
      id: number
      campusCode: string
      campusName: string
      address: string
      manager?: string
      status: number
      statusText?: string
      sort?: number
      createTime?: string
      updateTime?: string
    }

    /** 校区分页响应 */
    interface CampusPageResponse extends Api.Common.PaginatedResponse<CampusListItem> {
      list: CampusListItem[]
      total: number
      pageNum: number
      pageSize: number
      totalPages: number
    }

    /** 校区树列表 */
    type CampusTreeList = CampusListItem[]

    /** ==================== 院系管理 ==================== */
    /** 院系查询参数 */
    interface DepartmentSearchParams {
      deptCode?: string
      deptName?: string
      campusCode?: string
      status?: number
      pageNum?: number
      pageSize?: number
    }

    /** 院系保存参数 */
    interface DepartmentSaveParams {
      id?: number
      deptCode: string
      deptName: string
      campusCode: string
      leader?: string
      phone?: string
      sort?: number
      status: number
    }

    /** 院系列表项 */
    interface DepartmentListItem {
      id: number
      deptCode: string
      deptName: string
      campusCode: string
      campusName?: string
      leader?: string
      phone?: string
      sort?: number
      status: number
      statusText?: string
      createTime?: string
      updateTime?: string
      /** 子院系列表（树形结构时使用） */
      children?: DepartmentListItem[]
    }

    /** 院系分页响应 */
    interface DepartmentPageResponse extends Api.Common.PaginatedResponse<DepartmentListItem> {
      list: DepartmentListItem[]
      total: number
      pageNum: number
      pageSize: number
      totalPages: number
    }

    /** 院系树列表 */
    type DepartmentTreeList = DepartmentListItem[]

    /** ==================== 专业管理 ==================== */
    /** 专业查询参数 */
    interface MajorSearchParams {
      majorCode?: string
      majorName?: string
      deptCode?: string
      status?: number
      pageNum?: number
      pageSize?: number
    }

    /** 专业保存参数 */
    interface MajorSaveParams {
      id?: number
      majorCode: string
      majorName: string
      deptCode: string
      director?: string
      type?: string
      duration: string
      goal?: string
    }

    /** 专业列表项 */
    interface MajorListItem {
      id: number
      majorCode: string
      majorName: string
      deptCode: string
      deptName?: string
      director?: string
      type?: string
      typeText?: string
      duration: string
      goal?: string
      status?: number
      statusText?: string
      createTime?: string
      updateTime?: string
    }

    /** 专业分页响应 */
    interface MajorPageResponse extends Api.Common.PaginatedResponse<MajorListItem> {
      list: MajorListItem[]
      total: number
      pageNum: number
      pageSize: number
      totalPages: number
    }

    /** ==================== 班级管理 ==================== */
    /** 班级查询参数 */
    interface ClassSearchParams {
      classCode?: string
      className?: string
      majorCode?: string
      grade?: string
      enrollmentYear?: number
      status?: number
      pageNum?: number
      pageSize?: number
    }

    /** 班级保存参数 */
    interface ClassSaveParams {
      id?: number
      classCode: string
      className: string
      majorCode: string
      grade: string
      teacherId?: number
      enrollmentYear: number
      currentCount?: number
    }

    /** 班级列表项 */
    interface ClassListItem {
      id: number
      classCode: string
      className: string
      majorCode: string
      majorName?: string
      grade: string
      teacherName?: string
      teacherId?: number
      enrollmentYear: number
      currentCount?: number
      status?: number
      statusText?: string
      createTime?: string
      updateTime?: string
    }

    /** 班级分页响应 */
    interface ClassPageResponse extends Api.Common.PaginatedResponse<ClassListItem> {
      list: ClassListItem[]
      total: number
      pageNum: number
      pageSize: number
      totalPages: number
    }

    /** ==================== 学年管理 ==================== */
    /** 学年查询参数 */
    interface AcademicYearSearchParams {
      yearCode?: string
      yearName?: string
      status?: number
      pageNum?: number
      pageSize?: number
    }

    /** 学期保存参数 */
    interface SemesterSaveParams {
      id?: number
      semesterCode: string
      semesterName: string
      startDate: string
      endDate: string
      semesterType?: string
    }

    /** 学年保存参数 */
    interface AcademicYearSaveParams {
      id?: number
      yearCode: string
      yearName: string
      startDate: string
      endDate: string
      status: number
      semesters?: SemesterSaveParams[]
    }

    /** 学期列表项 */
    interface SemesterListItem {
      id: number
      academicYearId: number
      semesterCode: string
      semesterName: string
      startDate: string
      endDate: string
      semesterType?: string
      createTime?: string
      updateTime?: string
    }

    /** 学年列表项 */
    interface AcademicYearListItem {
      id: number
      yearCode: string
      yearName: string
      startDate: string
      endDate: string
      status: number
      statusText?: string
      semesters?: SemesterListItem[]
      createTime?: string
      updateTime?: string
    }

    /** 学年分页响应 */
    interface AcademicYearPageResponse extends Api.Common.PaginatedResponse<AcademicYearListItem> {
      list: AcademicYearListItem[]
      total: number
      pageNum: number
      pageSize: number
      totalPages: number
    }

    /** ==================== 学校层级管理 ==================== */

    /** 层级树节点 */
    interface SchoolHierarchyNode {
      id: number
      code: string
      name: string
      type: 'campus' | 'department' | 'major' | 'class'
      parentCode?: string
      status: number
      children?: SchoolHierarchyNode[]
    }

    /** 学校层级树响应 */
    interface SchoolHierarchyResponse {
      campuses: SchoolHierarchyNode[]
    }
  }
}
