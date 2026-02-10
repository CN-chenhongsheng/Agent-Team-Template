/**
 * 学生测试数据生成脚本
 * 
 * 使用方法：
 * 1. cd scripts
 * 2. npm init -y && npm install exceljs
 * 3. node generate-student-data.js
 * 4. 生成的 Excel 文件在 scripts/output/ 目录下
 * 
 * 注意：级联字段格式与系统模板匹配
 */

const ExcelJS = require('exceljs')
const path = require('path')
const fs = require('fs')

// ================== 配置区 ==================

// 生成的学生数量
const STUDENT_COUNT = 900000

// 起始学号
const START_STUDENT_NO = 2024001001

// 入学年份
const ENROLLMENT_YEAR = 2024

// 批量写入大小（避免内存溢出）
const BATCH_SIZE = 10000

// ================== 基础数据 ==================

// 姓氏库
const SURNAMES = ['张', '李', '王', '刘', '陈', '杨', '黄', '赵', '周', '吴', '徐', '孙', '马', '朱', '胡', '郭', '何', '林', '罗', '高']

// 名字库
const NAMES = ['伟', '芳', '娜', '敏', '静', '丽', '强', '磊', '军', '洋', '勇', '艳', '杰', '娟', '涛', '明', '超', '秀英', '霞', '平', '浩', '宇', '欣', '雪', '梦', '琳', '婷', '慧', '莉', '佳']

// 民族
const NATIONS = ['汉族', '满族', '蒙古族', '回族', '藏族', '维吾尔族', '苗族', '彝族', '壮族', '布依族']

// 政治面貌
const POLITICAL_STATUS = ['群众', '共青团员', '中共党员', '中共预备党员']

// 学籍状态
const ACADEMIC_STATUS = ['在读', '休学', '毕业']

// 省份
const PROVINCES = ['北京市', '上海市', '广东省', '江苏省', '浙江省', '山东省', '四川省', '湖北省', '湖南省', '河南省', '河北省', '福建省', '陕西省', '安徽省', '辽宁省']

// ================== 组织架构数据（与系统模板格式匹配）==================
// 格式说明：
// - 院系：主校区_计算机科学与技术学院（不带前缀）
// - 专业：主校区_计算机科学与技术学院_软件工程（不带前缀）
// - 班级：主校区_计算机科学与技术学院_软件工程_C2023级软件工程1班（班级带 C 前缀）

const ORG_DATA = {
  campuses: ['主校区'],
  // 校区 -> 院系映射
  departments: {
    '主校区': ['主校区_计算机科学与技术学院']
  },
  // 院系 -> 专业映射
  majors: {
    '主校区_计算机科学与技术学院': [
      '主校区_计算机科学与技术学院_软件工程'
    ]
  },
  // 专业 -> 班级映射
  classes: {
    '主校区_计算机科学与技术学院_软件工程': [
      '主校区_计算机科学与技术学院_软件工程_C2023级软件工程1班',
      '主校区_计算机科学与技术学院_软件工程_C2023级AI算法1班'
    ]
  }
}

// ================== 住宿数据（与系统模板格式匹配）==================
// 格式说明：
// - 楼层：主校区_F1号楼（带 F 前缀）
// - 房间：主校区_F1号楼_R0101（带 R 前缀）
// - 床位：主校区_F1号楼_R0101_B1（带 B 前缀）

const DORM_DATA = {
  // 校区 -> 楼层映射
  floors: {
    '主校区': ['主校区_F1号楼', '主校区_F2号楼', '主校区_F3号楼']
  },
  // 楼层 -> 房间映射（根据模板中的实际数据）
  rooms: {
    '主校区_F1号楼': [
      '主校区_F1号楼_R0101', '主校区_F1号楼_R0102', '主校区_F1号楼_R0103', '主校区_F1号楼_R0104', '主校区_F1号楼_R0105',
      '主校区_F1号楼_R0201', '主校区_F1号楼_R0202', '主校区_F1号楼_R0203', '主校区_F1号楼_R0204', '主校区_F1号楼_R0205',
      '主校区_F1号楼_R0301', '主校区_F1号楼_R0302', '主校区_F1号楼_R0303', '主校区_F1号楼_R0304', '主校区_F1号楼_R0305',
      '主校区_F1号楼_R0401', '主校区_F1号楼_R0402', '主校区_F1号楼_R0403', '主校区_F1号楼_R0404', '主校区_F1号楼_R0405',
      '主校区_F1号楼_R0501', '主校区_F1号楼_R0502', '主校区_F1号楼_R0503', '主校区_F1号楼_R0504', '主校区_F1号楼_R0505',
      '主校区_F1号楼_R0601', '主校区_F1号楼_R0602', '主校区_F1号楼_R0603', '主校区_F1号楼_R0604', '主校区_F1号楼_R0605'
    ],
    '主校区_F2号楼': [
      '主校区_F2号楼_R0101', '主校区_F2号楼_R0102', '主校区_F2号楼_R0103', '主校区_F2号楼_R0104', '主校区_F2号楼_R0105',
      '主校区_F2号楼_R0201', '主校区_F2号楼_R0202', '主校区_F2号楼_R0203', '主校区_F2号楼_R0204', '主校区_F2号楼_R0205',
      '主校区_F2号楼_R0301', '主校区_F2号楼_R0302', '主校区_F2号楼_R0303', '主校区_F2号楼_R0304', '主校区_F2号楼_R0305',
      '主校区_F2号楼_R0401', '主校区_F2号楼_R0402', '主校区_F2号楼_R0403', '主校区_F2号楼_R0404', '主校区_F2号楼_R0405',
      '主校区_F2号楼_R0501', '主校区_F2号楼_R0502', '主校区_F2号楼_R0503', '主校区_F2号楼_R0504', '主校区_F2号楼_R0505'
    ],
    '主校区_F3号楼': [
      '主校区_F3号楼_R0101', '主校区_F3号楼_R0102', '主校区_F3号楼_R0103', '主校区_F3号楼_R0104', '主校区_F3号楼_R0105',
      '主校区_F3号楼_R0201', '主校区_F3号楼_R0202', '主校区_F3号楼_R0203', '主校区_F3号楼_R0204', '主校区_F3号楼_R0205',
      '主校区_F3号楼_R0301', '主校区_F3号楼_R0302', '主校区_F3号楼_R0303', '主校区_F3号楼_R0304', '主校区_F3号楼_R0305',
      '主校区_F3号楼_R0401', '主校区_F3号楼_R0402', '主校区_F3号楼_R0403', '主校区_F3号楼_R0404', '主校区_F3号楼_R0405',
      '主校区_F3号楼_R0501', '主校区_F3号楼_R0502', '主校区_F3号楼_R0503', '主校区_F3号楼_R0504', '主校区_F3号楼_R0505'
    ]
  },
  // 房间 -> 床位映射（有床位的房间）
  beds: {
    '主校区_F1号楼_R0101': ['主校区_F1号楼_R0101_B1', '主校区_F1号楼_R0101_B2', '主校区_F1号楼_R0101_B3', '主校区_F1号楼_R0101_B4', '主校区_F1号楼_R0101_B5', '主校区_F1号楼_R0101_B6'],
    '主校区_F1号楼_R0102': ['主校区_F1号楼_R0102_B1', '主校区_F1号楼_R0102_B2', '主校区_F1号楼_R0102_B3', '主校区_F1号楼_R0102_B4', '主校区_F1号楼_R0102_B5', '主校区_F1号楼_R0102_B6'],
    '主校区_F1号楼_R0104': ['主校区_F1号楼_R0104_B1', '主校区_F1号楼_R0104_B2', '主校区_F1号楼_R0104_B3', '主校区_F1号楼_R0104_B4', '主校区_F1号楼_R0104_B5', '主校区_F1号楼_R0104_B6'],
    '主校区_F2号楼_R0101': ['主校区_F2号楼_R0101_B1', '主校区_F2号楼_R0101_B2', '主校区_F2号楼_R0101_B3', '主校区_F2号楼_R0101_B4'],
    '主校区_F2号楼_R0103': ['主校区_F2号楼_R0103_B1', '主校区_F2号楼_R0103_B2', '主校区_F2号楼_R0103_B3', '主校区_F2号楼_R0103_B4'],
    '主校区_F3号楼_R0101': ['主校区_F3号楼_R0101_B1', '主校区_F3号楼_R0101_B2', '主校区_F3号楼_R0101_B3', '主校区_F3号楼_R0101_B4'],
    '主校区_F3号楼_R0102': ['主校区_F3号楼_R0102_B1', '主校区_F3号楼_R0102_B2', '主校区_F3号楼_R0102_B3', '主校区_F3号楼_R0102_B4']
  }
}

// ================== 生活习惯选项 ==================

const LIFESTYLE_OPTIONS = {
  // student_smoking_status
  smokingStatus: ['不吸烟', '吸烟'],
  // student_smoking_tolerance
  smokingTolerance: ['不接受', '接受'],
  // student_sleep_schedule
  sleepSchedule: ['早睡早起(22:00-6:00)', '正常(23:00-7:00)', '晚睡晚起(24:00-8:00)', '夜猫子(01:00-9:00)'],
  // student_sleep_quality
  sleepQuality: ['浅睡易醒', '正常', '深睡'],
  // student_snores
  snores: ['不打呼噜', '打呼噜'],
  // student_sensitive_to_light
  sensitiveToLight: ['不敏感', '敏感'],
  // student_sensitive_to_sound
  sensitiveToSound: ['不敏感', '敏感'],
  // student_cleanliness_level
  cleanlinessLevel: ['非常整洁', '整洁', '一般', '随意', '不整洁'],
  // student_bedtime_cleanup
  bedtimeCleanup: ['不整理', '偶尔整理', '经常整理', '总是整理'],
  // student_social_preference
  socialPreference: ['喜欢安静', '中等', '喜欢热闹'],
  // student_allow_visitors
  allowVisitors: ['不允许', '偶尔可以', '可以'],
  // student_phone_call_time
  phoneCallTime: ['喜欢在宿舍打电话', '偶尔在宿舍', '不在宿舍打电话'],
  // student_study_in_room
  studyInRoom: ['不在', '偶尔', '经常', '总是'],
  // student_study_environment
  studyEnvironment: ['需要安静', '需要轻音乐', '可以接受声音'],
  // student_computer_usage_time
  computerUsageTime: ['不用', '很少(1-2h/天)', '正常(3-5h/天)', '很多(6h+/天)'],
  // student_gaming_preference
  gamingPreference: ['不玩游戏', '偶尔玩', '经常玩'],
  // student_music_preference
  musicPreference: ['不听', '偶尔听', '经常听'],
  // student_music_volume
  musicVolume: ['喜欢小声', '中等', '喜欢大声'],
  // student_eat_in_room
  eatInRoom: ['不吃', '偶尔', '经常']
}

// ================== 工具函数 ==================

function randomItem(arr) {
  return arr[Math.floor(Math.random() * arr.length)]
}

function randomPhone() {
  const prefixes = ['138', '139', '136', '137', '158', '159', '188', '189', '186', '187']
  return randomItem(prefixes) + Math.random().toString().slice(2, 10)
}

function randomIdCard(birthDate) {
  const areaCodes = ['110101', '310101', '440101', '320101', '330101', '370101', '510101', '420101', '430101', '410101']
  const areaCode = randomItem(areaCodes)
  const birthCode = birthDate.replace(/-/g, '')

  // 顺序码：3位数字，保证整体 17 位本体码
  const sequenceCode = String(Math.floor(Math.random() * 999)).padStart(3, '0')
  const body = areaCode + birthCode + sequenceCode // 6 + 8 + 3 = 17

  // 标准身份证校验码计算（与前端 validateChineseIDCard 一致）
  const weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
  const checkCodes = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']
  let sum = 0
  for (let i = 0; i < 17; i++) {
    sum += parseInt(body[i], 10) * weights[i]
  }
  const checkCode = checkCodes[sum % 11]

  return body + checkCode
}

function randomBirthDate(year) {
  const month = String(Math.floor(Math.random() * 12) + 1).padStart(2, '0')
  const day = String(Math.floor(Math.random() * 28) + 1).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function randomEmail(name, studentNo) {
  const domains = ['qq.com', '163.com', 'gmail.com', 'outlook.com', '126.com']
  return `${studentNo}@${randomItem(domains)}`
}

function randomAddress() {
  const province = randomItem(PROVINCES)
  const districts = ['海淀区', '朝阳区', '西城区', '东城区', '浦东新区', '天河区', '江干区', '武昌区']
  const streets = ['中关村大街', '建国路', '南京路', '人民路', '解放路', '和平路', '文化路', '学院路']
  return `${province}${randomItem(districts)}${randomItem(streets)}${Math.floor(Math.random() * 200) + 1}号`
}

// ================== 生成学生数据 ==================

function generateStudent(index) {
  const studentNo = String(START_STUDENT_NO + index)
  const surname = randomItem(SURNAMES)
  const name = surname + randomItem(NAMES) + (Math.random() > 0.7 ? randomItem(NAMES) : '')
  const gender = Math.random() > 0.5 ? '男' : '女'
  const birthYear = ENROLLMENT_YEAR - 18 - Math.floor(Math.random() * 3)
  const birthDate = randomBirthDate(birthYear)
  
  // 随机选择组织架构
  const campus = randomItem(ORG_DATA.campuses)
  const department = randomItem(ORG_DATA.departments[campus] || [])
  const major = randomItem(ORG_DATA.majors[department] || [])
  const classInfo = randomItem(ORG_DATA.classes[major] || [])
  
  // 随机选择住宿信息（80%的学生有住宿信息）
  let floor = '', room = '', bed = ''
  if (Math.random() > 0.2) {
    floor = randomItem(DORM_DATA.floors[campus] || [])
    if (floor) {
      room = randomItem(DORM_DATA.rooms[floor] || [])
      if (room) {
        bed = randomItem(DORM_DATA.beds[room] || [])
      }
    }
  }
  
  return {
    studentNo,
    studentName: name,
    gender,
    idCard: randomIdCard(birthDate),
    phone: randomPhone(),
    email: randomEmail(name, studentNo),
    birthDate,
    nation: randomItem(NATIONS),
    politicalStatus: randomItem(POLITICAL_STATUS),
    enrollmentYear: String(ENROLLMENT_YEAR),
    schoolingLength: '4',
    currentGrade: '大一',
    academicStatus: '在读',
    // 组织信息（级联格式）
    campus,
    department,
    major,
    class: classInfo,
    // 住宿信息（级联格式）
    floor,
    room,
    bed,
    // 联系信息
    parentName: surname + (gender === '男' ? '父' : '母'),
    parentPhone: randomPhone(),
    emergencyContact: surname + (gender === '男' ? '母' : '父'),
    emergencyPhone: randomPhone(),
    homeAddress: randomAddress(),
    // 生活习惯
    smokingStatus: randomItem(LIFESTYLE_OPTIONS.smokingStatus),
    smokingTolerance: randomItem(LIFESTYLE_OPTIONS.smokingTolerance),
    sleepSchedule: randomItem(LIFESTYLE_OPTIONS.sleepSchedule),
    sleepQuality: randomItem(LIFESTYLE_OPTIONS.sleepQuality),
    snores: randomItem(LIFESTYLE_OPTIONS.snores),
    sensitiveToLight: randomItem(LIFESTYLE_OPTIONS.sensitiveToLight),
    sensitiveToSound: randomItem(LIFESTYLE_OPTIONS.sensitiveToSound),
    cleanlinessLevel: randomItem(LIFESTYLE_OPTIONS.cleanlinessLevel),
    bedtimeCleanup: randomItem(LIFESTYLE_OPTIONS.bedtimeCleanup),
    socialPreference: randomItem(LIFESTYLE_OPTIONS.socialPreference),
    allowVisitors: randomItem(LIFESTYLE_OPTIONS.allowVisitors),
    phoneCallTime: randomItem(LIFESTYLE_OPTIONS.phoneCallTime),
    studyInRoom: randomItem(LIFESTYLE_OPTIONS.studyInRoom),
    studyEnvironment: randomItem(LIFESTYLE_OPTIONS.studyEnvironment),
    computerUsageTime: randomItem(LIFESTYLE_OPTIONS.computerUsageTime),
    gamingPreference: randomItem(LIFESTYLE_OPTIONS.gamingPreference),
    musicPreference: randomItem(LIFESTYLE_OPTIONS.musicPreference),
    musicVolume: randomItem(LIFESTYLE_OPTIONS.musicVolume),
    eatInRoom: randomItem(LIFESTYLE_OPTIONS.eatInRoom),
    remark: ''
  }
}

// ================== 生成 Excel 文件 ==================

async function generateExcel() {
  console.log(`🚀 开始生成 ${STUDENT_COUNT.toLocaleString()} 条学生数据...`)
  console.log(`📦 批量大小: ${BATCH_SIZE.toLocaleString()} 条/批`)
  
  const startTime = Date.now()
  
  // 确保输出目录存在
  const outputDir = path.join(__dirname, 'output')
  if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true })
  }
  
  // 使用流式写入处理大文件，文件名带日期+时间避免覆盖
  const now = new Date()
  const datePart = now.toISOString().slice(0, 10) // YYYY-MM-DD
  const timePart = now.toTimeString().slice(0, 8).replace(/:/g, '') // HHmmss
  const filename = `学生测试数据_${STUDENT_COUNT}_${datePart}_${timePart}.xlsx`
  const filepath = path.join(outputDir, filename)
  // 使用 streaming writer，避免一次性占用大量内存
  const workbook = new ExcelJS.stream.xlsx.WorkbookWriter({
    filename: filepath,
    useStyles: true,
    useSharedStrings: true
  })
  workbook.creator = '数据生成脚本'
  workbook.created = new Date()

  const worksheet = workbook.addWorksheet('学生数据')
  
  // 表头（与模板完全匹配）
  const headers = [
    '*学号', '*姓名', '性别', '身份证号', '手机号', '邮箱', '出生日期', '民族', '政治面貌',
    '入学年份', '学制', '当前年级', '学籍状态',
    '*校区', '*院系', '*专业', '*班级',
    '楼层', '房间', '床位',
    '家长姓名', '家长电话', '紧急联系人', '紧急联系电话', '家庭地址',
    '是否吸烟', '接受室友吸烟', '作息时间', '睡眠质量', '是否打呼噜', '对光线敏感', '对声音敏感',
    '整洁程度', '睡前整理习惯', '社交偏好', '允许访客', '宿舍打电话习惯',
    '宿舍学习频率', '学习环境偏好', '电脑使用时间', '游戏频率', '听音乐频率', '音乐音量偏好', '宿舍吃东西习惯',
    '备注'
  ]
  
  // 设置列宽
  worksheet.columns = headers.map((h, i) => ({
    header: h,
    key: `col${i}`,
    width: Math.max(h.length * 2.5, 12)
  }))
  
  // 设置表头样式（stream 模式下直接在 header 行设置即可）
  const headerRow = worksheet.getRow(1)
  headerRow.font = { bold: true, color: { argb: 'FFFFFF' }, name: '微软雅黑', size: 11 }
  headerRow.fill = {
    type: 'pattern',
    pattern: 'solid',
    fgColor: { argb: '4472C4' }
  }
  headerRow.alignment = { horizontal: 'center', vertical: 'middle' }
  headerRow.height = 25
  headerRow.commit()
  
  // 批量生成数据
  const totalBatches = Math.ceil(STUDENT_COUNT / BATCH_SIZE)
  
  for (let batch = 0; batch < totalBatches; batch++) {
    const batchStart = batch * BATCH_SIZE
    const batchEnd = Math.min((batch + 1) * BATCH_SIZE, STUDENT_COUNT)
    const batchSize = batchEnd - batchStart
    
    process.stdout.write(`\r📝 生成中: 批次 ${batch + 1}/${totalBatches} (${batchEnd.toLocaleString()}/${STUDENT_COUNT.toLocaleString()})`)
    
    for (let i = batchStart; i < batchEnd; i++) {
      const student = generateStudent(i)
      const row = worksheet.addRow([
        student.studentNo,
        student.studentName,
        student.gender,
        student.idCard,
        student.phone,
        student.email,
        student.birthDate,
        student.nation,
        student.politicalStatus,
        student.enrollmentYear,
        student.schoolingLength,
        student.currentGrade,
        student.academicStatus,
        student.campus,
        student.department,
        student.major,
        student.class,
        student.floor,
        student.room,
        student.bed,
        student.parentName,
        student.parentPhone,
        student.emergencyContact,
        student.emergencyPhone,
        student.homeAddress,
        student.smokingStatus,
        student.smokingTolerance,
        student.sleepSchedule,
        student.sleepQuality,
        student.snores,
        student.sensitiveToLight,
        student.sensitiveToSound,
        student.cleanlinessLevel,
        student.bedtimeCleanup,
        student.socialPreference,
        student.allowVisitors,
        student.phoneCallTime,
        student.studyInRoom,
        student.studyEnvironment,
        student.computerUsageTime,
        student.gamingPreference,
        student.musicPreference,
        student.musicVolume,
        student.eatInRoom,
        student.remark
      ])
      row.commit()
    }
  }
  
  console.log('\n💾 正在保存文件（这可能需要一些时间）...')
  
  // 提交工作表和工作簿（stream 模式）
  worksheet.commit()
  await workbook.commit()
  
  const endTime = Date.now()
  const duration = ((endTime - startTime) / 1000).toFixed(2)
  const fileSize = (fs.statSync(filepath).size / 1024 / 1024).toFixed(2)
  
  console.log(`\n✅ 数据生成完成！`)
  console.log(`📁 文件保存至: ${filepath}`)
  console.log(`📊 共生成 ${STUDENT_COUNT.toLocaleString()} 条学生数据`)
  console.log(`📦 文件大小: ${fileSize} MB`)
  console.log(`⏱️ 耗时: ${duration} 秒`)
}

// 运行
generateExcel().catch(err => {
  console.error('❌ 生成失败:', err)
  process.exit(1)
})
