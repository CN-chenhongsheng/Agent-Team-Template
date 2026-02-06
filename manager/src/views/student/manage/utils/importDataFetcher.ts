/**
 * 学生导入模板数据获取服务
 *
 * 提供组织架构和住宿信息的层级数据获取
 * 用于生成 Excel 模板的下拉选项
 *
 * @module views/student/manage/importDataFetcher
 * @author 陈鸿昇
 */
import {
  fetchGetCampusPage,
  fetchGetDepartmentPage,
  fetchGetMajorPage,
  fetchGetClassPage
} from '@/api/school-manage'
import { fetchGetFloorPage, fetchGetRoomPage, fetchGetBedPage } from '@/api/dormitory-manage'

/** 缓存数据，避免重复请求 */
const dataCache: {
  campuses?: Api.SystemManage.CampusListItem[]
  departments?: Api.SystemManage.DepartmentListItem[]
  majors?: Api.SystemManage.MajorListItem[]
  classes?: Api.SystemManage.ClassListItem[]
  floors?: Api.SystemManage.FloorListItem[]
  rooms?: Api.SystemManage.RoomListItem[]
  beds?: Api.SystemManage.BedListItem[]
} = {}

/**
 * 获取所有校区列表
 * 接口返回格式：{list: [...], total, pageNum, pageSize, totalPages}
 */
async function getAllCampuses(): Promise<Api.SystemManage.CampusListItem[]> {
  if (dataCache.campuses) return dataCache.campuses

  try {
    const res = await fetchGetCampusPage({ pageNum: 1, pageSize: 1000, status: 1 })
    dataCache.campuses = res?.list || []
    return dataCache.campuses
  } catch (error) {
    console.error('[importDataFetcher] 获取校区数据失败:', error)
    dataCache.campuses = []
    return []
  }
}

/**
 * 获取所有院系列表
 */
async function getAllDepartments(): Promise<Api.SystemManage.DepartmentListItem[]> {
  if (dataCache.departments) return dataCache.departments

  try {
    const res = await fetchGetDepartmentPage({ pageNum: 1, pageSize: 1000, status: 1 })
    dataCache.departments = res?.list || []
    return dataCache.departments
  } catch (error) {
    console.error('[importDataFetcher] 获取院系数据失败:', error)
    dataCache.departments = []
    return []
  }
}

/**
 * 获取所有专业列表
 */
async function getAllMajors(): Promise<Api.SystemManage.MajorListItem[]> {
  if (dataCache.majors) return dataCache.majors

  try {
    const res = await fetchGetMajorPage({ pageNum: 1, pageSize: 1000, status: 1 })
    dataCache.majors = res?.list || []
    return dataCache.majors
  } catch (error) {
    console.error('[importDataFetcher] 获取专业数据失败:', error)
    dataCache.majors = []
    return []
  }
}

/**
 * 获取所有班级列表
 */
async function getAllClasses(): Promise<Api.SystemManage.ClassListItem[]> {
  if (dataCache.classes) return dataCache.classes

  try {
    const res = await fetchGetClassPage({ pageNum: 1, pageSize: 1000, status: 1 })
    dataCache.classes = res?.list || []
    return dataCache.classes
  } catch (error) {
    console.error('[importDataFetcher] 获取班级数据失败:', error)
    dataCache.classes = []
    return []
  }
}

/**
 * 获取所有楼层列表
 */
async function getAllFloors(): Promise<Api.SystemManage.FloorListItem[]> {
  if (dataCache.floors) return dataCache.floors

  try {
    const res = await fetchGetFloorPage({ pageNum: 1, pageSize: 1000, status: 1 })
    dataCache.floors = res?.list || []
    return dataCache.floors
  } catch (error) {
    console.error('[importDataFetcher] 获取楼层数据失败:', error)
    dataCache.floors = []
    return []
  }
}

/**
 * 获取所有房间列表
 */
async function getAllRooms(): Promise<Api.SystemManage.RoomListItem[]> {
  if (dataCache.rooms) return dataCache.rooms

  try {
    const res = await fetchGetRoomPage({ pageNum: 1, pageSize: 5000 })
    dataCache.rooms = res?.list || []
    return dataCache.rooms
  } catch (error) {
    console.error('[importDataFetcher] 获取房间数据失败:', error)
    dataCache.rooms = []
    return []
  }
}

/**
 * 获取所有床位列表
 * 不过滤 bedStatus，获取所有床位供选择
 */
async function getAllBeds(): Promise<Api.SystemManage.BedListItem[]> {
  if (dataCache.beds) return dataCache.beds

  try {
    const res = await fetchGetBedPage({ pageNum: 1, pageSize: 10000 })
    dataCache.beds = res?.list || []
    return dataCache.beds
  } catch (error) {
    console.error('[importDataFetcher] 获取床位数据失败:', error)
    dataCache.beds = []
    return []
  }
}

/**
 * 清除缓存（在每次生成模板前调用）
 */
export function clearImportDataCache(): void {
  dataCache.campuses = undefined
  dataCache.departments = undefined
  dataCache.majors = undefined
  dataCache.classes = undefined
  dataCache.floors = undefined
  dataCache.rooms = undefined
  dataCache.beds = undefined
}

// ===================== 组织架构选项获取 =====================

/**
 * 获取校区下拉选项
 * 格式：校区名称
 */
export async function fetchCampusOptions(): Promise<string[]> {
  const campuses = await getAllCampuses()
  return campuses.map((c) => c.campusName)
}

/**
 * 获取院系下拉选项（带校区前缀）
 * 格式：校区名称 / 院系名称
 */
export async function fetchDepartmentOptions(): Promise<string[]> {
  const [campuses, departments] = await Promise.all([getAllCampuses(), getAllDepartments()])

  const campusMap = new Map(campuses.map((c) => [c.campusCode, c.campusName]))

  return departments.map((d) => {
    const campusName = campusMap.get(d.campusCode) || '未知校区'
    return `${campusName} / ${d.deptName}`
  })
}

/**
 * 获取专业下拉选项（带院系前缀）
 * 格式：院系名称 / 专业名称
 */
export async function fetchMajorOptions(): Promise<string[]> {
  const [departments, majors] = await Promise.all([getAllDepartments(), getAllMajors()])

  const deptMap = new Map(departments.map((d) => [d.deptCode, d.deptName]))

  return majors.map((m) => {
    const deptName = deptMap.get(m.deptCode) || '未知院系'
    return `${deptName} / ${m.majorName}`
  })
}

/**
 * 获取班级下拉选项（带专业前缀）
 * 格式：专业名称 / 班级名称
 */
export async function fetchClassOptions(): Promise<string[]> {
  const [majors, classes] = await Promise.all([getAllMajors(), getAllClasses()])

  const majorMap = new Map(majors.map((m) => [m.majorCode, m.majorName]))

  return classes.map((c) => {
    const majorName = majorMap.get(c.majorCode) || '未知专业'
    return `${majorName} / ${c.className}`
  })
}

// ===================== 住宿信息选项获取 =====================

/**
 * 获取楼层下拉选项（带校区前缀）
 * 格式：校区名称 / 楼层名称
 */
export async function fetchFloorOptions(): Promise<string[]> {
  const [campuses, floors] = await Promise.all([getAllCampuses(), getAllFloors()])

  const campusMap = new Map(campuses.map((c) => [c.campusCode, c.campusName]))

  return floors.map((f) => {
    const campusName = campusMap.get(f.campusCode) || '未知校区'
    return `${campusName} / ${f.floorName}`
  })
}

/**
 * 获取房间下拉选项（带楼层前缀）
 * 格式：楼层名称 / 房间号
 */
export async function fetchRoomOptions(): Promise<string[]> {
  const [floors, rooms] = await Promise.all([getAllFloors(), getAllRooms()])

  const floorMap = new Map(floors.map((f) => [f.id, f.floorName]))

  return rooms.map((r) => {
    const floorName = floorMap.get(r.floorId) || '未知楼层'
    return `${floorName} / ${r.roomCode}`
  })
}

/**
 * 获取床位下拉选项（带房间前缀）
 * 格式：房间号 / 床位号
 */
export async function fetchBedOptions(): Promise<string[]> {
  const [rooms, beds] = await Promise.all([getAllRooms(), getAllBeds()])

  const roomMap = new Map(rooms.map((r) => [r.id, r.roomCode]))

  return beds.map((b) => {
    const roomCode = roomMap.get(b.roomId) || '未知房间'
    return `${roomCode} / ${b.bedCode}`
  })
}

// ===================== 完整路径选项获取（用于高级导入）=====================

/**
 * 获取完整组织架构路径选项
 * 格式：校区 / 院系 / 专业 / 班级
 */
export async function fetchFullOrgPathOptions(): Promise<string[]> {
  const [campuses, departments, majors, classes] = await Promise.all([
    getAllCampuses(),
    getAllDepartments(),
    getAllMajors(),
    getAllClasses()
  ])

  const campusMap = new Map(campuses.map((c) => [c.campusCode, c.campusName]))
  const deptMap = new Map(
    departments.map((d) => [d.deptCode, { name: d.deptName, campusCode: d.campusCode }])
  )
  const majorMap = new Map(
    majors.map((m) => [m.majorCode, { name: m.majorName, deptCode: m.deptCode }])
  )

  return classes.map((c) => {
    const majorInfo = majorMap.get(c.majorCode)
    const deptInfo = majorInfo ? deptMap.get(majorInfo.deptCode) : null
    const campusName = deptInfo ? campusMap.get(deptInfo.campusCode) : '未知校区'
    const deptName = deptInfo?.name || '未知院系'
    const majorName = majorInfo?.name || '未知专业'

    return `${campusName} / ${deptName} / ${majorName} / ${c.className}`
  })
}

/**
 * 获取完整住宿路径选项（只显示空闲床位）
 * 格式：校区 / 楼层 / 房间 / 床位
 */
export async function fetchFullDormPathOptions(): Promise<string[]> {
  const [campuses, floors, rooms, beds] = await Promise.all([
    getAllCampuses(),
    getAllFloors(),
    getAllRooms(),
    getAllBeds()
  ])

  const campusMap = new Map(campuses.map((c) => [c.campusCode, c.campusName]))
  const floorMap = new Map(
    floors.map((f) => [f.id, { name: f.floorName, campusCode: f.campusCode }])
  )
  const roomMap = new Map(rooms.map((r) => [r.id, { code: r.roomCode, floorId: r.floorId }]))

  return beds.map((b) => {
    const roomInfo = roomMap.get(b.roomId)
    const floorInfo = roomInfo ? floorMap.get(roomInfo.floorId) : null
    const campusName = floorInfo ? campusMap.get(floorInfo.campusCode) : '未知校区'
    const floorName = floorInfo?.name || '未知楼层'
    const roomCode = roomInfo?.code || '未知房间'

    return `${campusName} / ${floorName} / ${roomCode} / ${b.bedCode}`
  })
}
