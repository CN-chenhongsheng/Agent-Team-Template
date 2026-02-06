<!-- 床位编辑弹窗 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="120px" label-position="right">
      <ElFormItem label="床位编码" prop="bedCode">
        <ElInput
          v-model="form.bedCode"
          placeholder="请输入床位编码（如：101-1、101-2）"
          :disabled="isEdit"
        />
      </ElFormItem>

      <ElFormItem label="床位号" prop="bedNumber">
        <ElInput v-model="form.bedNumber" placeholder="请输入床位号（如：1、2、3、4）" />
      </ElFormItem>

      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="所属校区" prop="campusCode">
            <ElSelect
              v-model="selectedCampusCode"
              placeholder="请选择校区"
              filterable
              clearable
              :disabled="isEdit"
              @change="handleCampusChange"
            >
              <ElOption
                v-for="item in campusList"
                :key="item.campusCode"
                :label="item.campusName"
                :value="item.campusCode"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="所属楼层" prop="floorId">
            <ElSelect
              v-model="selectedFloorId"
              placeholder="请选择楼层"
              filterable
              clearable
              :disabled="isEdit || !selectedCampusCode"
              @change="handleFloorChange"
            >
              <ElOption
                v-for="item in floorList"
                :key="item.id"
                :label="item.floorName || item.floorCode"
                :value="item.id"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
      </ElRow>
      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="所属房间" prop="roomId">
            <ElSelect
              v-model="form.roomId"
              placeholder="请选择房间"
              filterable
              clearable
              :disabled="isEdit || !selectedFloorId"
            >
              <ElOption
                v-for="item in roomList"
                :key="item.id"
                :label="item.roomNumber || item.roomCode"
                :value="item.id"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="床位位置" prop="bedPosition">
            <ElSelect v-model="form.bedPosition" placeholder="请选择床位位置" filterable clearable>
              <ElOption
                v-for="item in bedPositionOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="床位状态" prop="bedStatus">
            <ElSelect v-model="form.bedStatus" placeholder="请选择床位状态" filterable clearable>
              <ElOption
                v-for="item in bedStatusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="入住学生" prop="studentName">
            <ElInput v-model="form.studentName" disabled placeholder="如有学生入住，则自动显示" />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="入住日期" prop="checkInDate">
            <ElDatePicker
              v-model="form.checkInDate"
              type="date"
              placeholder="请选择入住日期"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="退宿日期" prop="checkOutDate">
            <ElDatePicker
              v-model="form.checkOutDate"
              type="date"
              placeholder="请选择退宿日期"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElFormItem label="排序序号" prop="sort">
        <ElInputNumber v-model="form.sort" :min="0" :max="9999" />
      </ElFormItem>

      <ElFormItem label="备注" prop="remark">
        <ElInput v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </ElFormItem>
    </ElForm>

    <template #footer>
      <ElButton @click="handleClose">取消</ElButton>
      <ElButton type="primary" :loading="loading" @click="handleSubmit">确定</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { fetchAddBed, fetchUpdateBed } from '@/api/dormitory-manage'
  import { useDictStore } from '@/store/modules/dict'
  import { useReferenceStore } from '@/store/modules/reference'
  import type { FormInstance, FormRules } from 'element-plus'

  interface Props {
    visible: boolean
    type: 'add' | 'edit'
    editData?: Api.SystemManage.BedListItem | null
  }

  interface Emits {
    (e: 'update:visible', visible: boolean): void
    (e: 'submit'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    editData: null
  })

  const emit = defineEmits<Emits>()

  const formRef = ref<FormInstance>()
  const loading = ref(false)
  const campusList = ref<Api.SystemManage.CampusListItem[]>([])
  const floorList = ref<Api.SystemManage.FloorListItem[]>([])
  const roomList = ref<Api.SystemManage.RoomListItem[]>([])
  const selectedCampusCode = ref<string>('')
  const selectedFloorId = ref<number | undefined>(undefined)
  const bedPositionOptions = ref<Array<{ label: string; value: string }>>([])
  const bedStatusOptions = ref<Array<{ label: string; value: number }>>([])

  // 使用字典 store
  const dictStore = useDictStore()
  // 使用参考数据 store
  const referenceStore = useReferenceStore()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
  })

  const isEdit = computed(() => props.type === 'edit')

  const dialogTitle = computed(() => {
    if (isEdit.value) return '编辑床位'
    return '新增床位'
  })

  const form = reactive<Api.SystemManage.BedSaveParams>({
    bedCode: '',
    bedNumber: '',
    roomId: undefined,
    bedPosition: undefined,
    bedStatus: 1,
    studentId: undefined,
    studentName: undefined,
    checkInDate: undefined,
    checkOutDate: undefined,
    sort: 0,
    status: 1,
    remark: undefined
  })

  const rules = reactive<FormRules>({
    bedCode: [{ required: true, message: '请输入床位编码', trigger: 'blur' }],
    bedNumber: [{ required: true, message: '请输入床位号', trigger: 'blur' }],
    roomId: [{ required: true, message: '请选择所属房间', trigger: 'change' }]
  })

  /**
   * 加载校区列表（使用 store 缓存）
   */
  const loadCampusList = async (): Promise<void> => {
    try {
      campusList.value = await referenceStore.loadCampusTree()
    } catch (error) {
      console.error('加载校区列表失败:', error)
    }
  }

  /**
   * 加载楼层列表（使用 store 缓存）
   */
  const loadFloorList = async (campusCode?: string): Promise<void> => {
    if (!campusCode) {
      floorList.value = []
      selectedFloorId.value = undefined
      roomList.value = []
      return
    }
    try {
      floorList.value = await referenceStore.loadFloorList(campusCode)
    } catch (error) {
      console.error('加载楼层列表失败:', error)
    }
  }

  /**
   * 加载房间列表（使用 store 缓存）
   */
  const loadRoomList = async (floorId?: number): Promise<void> => {
    if (!floorId) {
      roomList.value = []
      return
    }
    try {
      roomList.value = await referenceStore.loadRoomList(floorId)
    } catch (error) {
      console.error('加载房间列表失败:', error)
    }
  }

  /**
   * 加载字典数据
   */
  const loadDictData = async (): Promise<void> => {
    try {
      // 使用 store 批量加载字典数据（一个接口请求）
      const dictRes = await dictStore.loadDictDataBatch([
        'dormitory_bed_position',
        'dormitory_bed_status'
      ])

      // 解析床位位置字典
      bedPositionOptions.value = (dictRes.dormitory_bed_position || []).map((item) => ({
        label: item.label,
        value: item.value
      }))

      // 解析床位状态字典
      bedStatusOptions.value = (dictRes.dormitory_bed_status || []).map((item) => ({
        label: item.label,
        value: Number(item.value)
      }))
    } catch (error) {
      console.error('加载字典数据失败:', error)
    }
  }

  /**
   * 校区变更处理
   */
  const handleCampusChange = (campusCode: string): void => {
    selectedCampusCode.value = campusCode
    selectedFloorId.value = undefined
    form.roomId = undefined
    loadFloorList(campusCode)
    roomList.value = []
  }

  /**
   * 楼层变更处理
   */
  const handleFloorChange = (floorId: number): void => {
    selectedFloorId.value = floorId
    form.roomId = undefined
    loadRoomList(floorId)
  }

  /**
   * 加载表单数据
   */
  const loadFormData = async (): Promise<void> => {
    if (isEdit.value && props.editData) {
      // 先加载校区列表（如果还没有加载）
      if (campusList.value.length === 0) {
        await loadCampusList()
      }

      // 设置校区并加载楼层列表
      selectedCampusCode.value = props.editData.campusCode || ''
      if (props.editData.campusCode) {
        await loadFloorList(props.editData.campusCode)
      }

      // 根据楼层ID或楼层编码查找楼层
      if (props.editData.floorId) {
        // 优先使用 floorId
        selectedFloorId.value = props.editData.floorId
        await loadRoomList(props.editData.floorId)
      } else if (props.editData.floorCode && floorList.value.length > 0) {
        // 如果没有 floorId，使用 floorCode 查找
        const floor = floorList.value.find((f) => f.floorCode === props.editData?.floorCode)
        if (floor) {
          selectedFloorId.value = floor.id
          await loadRoomList(floor.id)
        }
      }

      Object.assign(form, {
        id: props.editData.id,
        bedCode: props.editData.bedCode,
        bedNumber: props.editData.bedNumber,
        roomId: props.editData.roomId,
        bedPosition: props.editData.bedPosition || undefined,
        bedStatus: props.editData.bedStatus || 1,
        studentId: props.editData.studentId || undefined,
        studentName: props.editData.studentInfo?.studentName || undefined,
        checkInDate: props.editData.checkInDate || undefined,
        checkOutDate: props.editData.checkOutDate || undefined,
        sort: props.editData.sort || 0,
        status: props.editData.status,
        remark: props.editData.remark || undefined
      })
    } else {
      resetForm()
    }
  }

  /**
   * 重置表单
   */
  const resetForm = (): void => {
    Object.assign(form, {
      bedCode: '',
      bedNumber: '',
      roomId: undefined,
      bedPosition: undefined,
      bedStatus: 1,
      studentId: undefined,
      studentName: undefined,
      checkInDate: undefined,
      checkOutDate: undefined,
      sort: 0,
      status: 1,
      remark: undefined
    })
    selectedCampusCode.value = ''
    selectedFloorId.value = undefined
    floorList.value = []
    roomList.value = []
    formRef.value?.clearValidate()
  }

  /**
   * 提交表单
   */
  const handleSubmit = async (): Promise<void> => {
    if (!formRef.value) return

    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    loading.value = true
    try {
      if (isEdit.value) {
        await fetchUpdateBed(form.id!, form)
      } else {
        await fetchAddBed(form)
      }
      emit('submit')
      handleClose()
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      loading.value = false
    }
  }

  /**
   * 关闭弹窗
   */
  const handleClose = (): void => {
    resetForm()
    emit('update:visible', false)
  }

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        loadCampusList()
        loadDictData()
        loadFormData()
      }
    },
    { immediate: true }
  )
</script>
