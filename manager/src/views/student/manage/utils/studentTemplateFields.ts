/**
 * 学生导入模板字段配置
 *
 * 定义学生批量导入所需的 Excel 模板列配置
 * 包含学生基本信息、组织信息、住宿信息、联系信息和生活习惯等完整字段
 *
 * 组织信息和住宿信息使用级联下拉：
 * - 组织架构：校区 → 院系 → 专业 → 班级
 * - 住宿信息：校区 → 楼层 → 房间 → 床位
 *
 * @module views/student/manage/studentTemplateFields
 * @author 陈鸿昇
 */
import { COMMON_FIELDS, type TemplateColumn } from '@/utils/excel'

/**
 * 学生导入模板列配置
 *
 * 字段分组：
 * 1. 基本信息：学号、姓名、性别、身份证、手机号等
 * 2. 学业信息：入学年份、学制、年级、学籍状态
 * 3. 组织信息：校区、院系、专业、班级（通过接口获取，带层级路径）
 * 4. 住宿信息：楼层、房间、床位（通过接口获取，带层级路径）
 * 5. 联系信息：家长、紧急联系人、家庭地址
 * 6. 生活习惯：吸烟、睡眠、整洁、社交、学习、娱乐等
 *
 * 使用方式：
 * ```ts
 * import { generateTemplate } from '@/utils/excel'
 * import { studentTemplateColumns } from './studentTemplateFields'
 *
 * await generateTemplate({
 *   columns: studentTemplateColumns,
 *   filename: '学生导入模板'
 * })
 * ```
 */
export const studentTemplateColumns: TemplateColumn[] = [
  // ======================== 基本信息 ========================
  {
    title: '学号',
    key: 'studentNo',
    width: 15,
    required: true,
    example: '2024001001',
    comment: '学号为必填项，不可重复'
  },
  {
    title: '姓名',
    key: 'studentName',
    width: 12,
    required: true,
    example: '张三'
  },
  COMMON_FIELDS.gender,
  COMMON_FIELDS.idCard,
  COMMON_FIELDS.phone,
  COMMON_FIELDS.email,
  COMMON_FIELDS.birthDate,
  COMMON_FIELDS.nation,
  COMMON_FIELDS.politicalStatus,

  // ======================== 学业信息 ========================
  COMMON_FIELDS.enrollmentYear,
  COMMON_FIELDS.schoolingLength,
  COMMON_FIELDS.currentGrade,
  COMMON_FIELDS.academicStatus,

  // ======================== 组织信息（级联下拉：校区→院系→专业→班级）========================
  {
    title: '校区',
    key: 'campusName',
    width: 15,
    required: true,
    cascadeType: 'campus',
    example: '主校区',
    comment: '请先选择校区，其他字段会根据校区自动筛选'
  },
  {
    title: '院系',
    key: 'deptName',
    width: 18,
    required: true,
    cascadeType: 'department',
    example: '计算机学院',
    comment: '请先选择校区，再选择院系'
  },
  {
    title: '专业',
    key: 'majorName',
    width: 18,
    required: true,
    cascadeType: 'major',
    example: '软件工程',
    comment: '请先选择院系，再选择专业'
  },
  {
    title: '班级',
    key: 'className',
    width: 20,
    required: true,
    cascadeType: 'class',
    example: '2024级1班',
    comment: '请先选择专业，再选择班级'
  },

  // ======================== 住宿信息（级联下拉：校区→楼层→房间→床位）========================
  {
    title: '楼层',
    key: 'floorName',
    width: 15,
    cascadeType: 'floor',
    example: '1号楼',
    comment: '请先选择校区，再选择楼层（可选）'
  },
  {
    title: '房间',
    key: 'roomNumber',
    width: 12,
    cascadeType: 'room',
    example: '0101',
    comment: '请先选择楼层，再选择房间（可选）'
  },
  {
    title: '床位',
    key: 'bedNumber',
    width: 10,
    cascadeType: 'bed',
    example: '1',
    comment: '请先选择房间，再选择床位（可选）'
  },

  // ======================== 联系信息 ========================
  {
    title: '家长姓名',
    key: 'parentName',
    width: 12,
    example: '张父'
  },
  {
    title: '家长电话',
    key: 'parentPhone',
    width: 15,
    example: '13900139000',
    comment: '请输入11位手机号'
  },
  {
    title: '紧急联系人',
    key: 'emergencyContact',
    width: 12,
    example: '张母'
  },
  {
    title: '紧急联系电话',
    key: 'emergencyPhone',
    width: 15,
    example: '13900139001',
    comment: '请输入11位手机号'
  },
  {
    title: '家庭地址',
    key: 'homeAddress',
    width: 35,
    example: '北京市海淀区xxx街道xxx号'
  },

  // ======================== 生活习惯 - 吸烟相关 ========================
  COMMON_FIELDS.smokingStatus,
  COMMON_FIELDS.smokingTolerance,

  // ======================== 生活习惯 - 睡眠相关 ========================
  COMMON_FIELDS.sleepSchedule,
  COMMON_FIELDS.sleepQuality,
  COMMON_FIELDS.snores,
  COMMON_FIELDS.sensitiveToLight,
  COMMON_FIELDS.sensitiveToSound,

  // ======================== 生活习惯 - 整洁相关 ========================
  COMMON_FIELDS.cleanlinessLevel,
  COMMON_FIELDS.bedtimeCleanup,

  // ======================== 生活习惯 - 社交相关 ========================
  COMMON_FIELDS.socialPreference,
  COMMON_FIELDS.allowVisitors,
  COMMON_FIELDS.phoneCallTime,

  // ======================== 生活习惯 - 学习相关 ========================
  COMMON_FIELDS.studyInRoom,
  COMMON_FIELDS.studyEnvironment,

  // ======================== 生活习惯 - 娱乐相关 ========================
  COMMON_FIELDS.computerUsageTime,
  COMMON_FIELDS.gamingPreference,
  COMMON_FIELDS.musicPreference,
  COMMON_FIELDS.musicVolume,

  // ======================== 生活习惯 - 其他 ========================
  COMMON_FIELDS.eatInRoom,

  // ======================== 备注 ========================
  {
    title: '备注',
    key: 'remark',
    width: 30,
    example: ''
  }
]

/**
 * 学生模板文件名
 */
export const STUDENT_TEMPLATE_FILENAME = '学生导入模板'

/**
 * 学生模板工作表名
 */
export const STUDENT_TEMPLATE_SHEET_NAME = '学生数据'
