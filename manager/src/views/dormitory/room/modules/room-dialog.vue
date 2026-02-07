<!-- 房间编辑弹窗 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="720px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="120px" label-position="right">
      <ElFormItem label="房间编码" prop="roomCode">
        <ElInput
          v-model="form.roomCode"
          placeholder="请输入房间编码（如：101、102）"
          :disabled="isEdit || hasBeds"
        />
      </ElFormItem>

      <ElFormItem label="房间号" prop="roomNumber">
        <ElInput
          v-model="form.roomNumber"
          placeholder="请输入房间号（如：101、102）"
          :disabled="hasBeds"
        />
      </ElFormItem>

      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="所属校区" prop="campusCode">
            <ElSelect
              v-model="form.campusCode"
              placeholder="请选择校区"
              filterable
              clearable
              :disabled="hasBeds"
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
              v-model="form.floorId"
              placeholder="请选择楼层"
              filterable
              clearable
              :disabled="!selectedCampusCode || hasBeds"
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
          <ElFormItem label="楼层数" prop="floorNumber">
            <ElSelect
              v-model="form.floorNumber"
              placeholder="请选择楼层数"
              filterable
              clearable
              :disabled="!form.floorId || hasBeds"
            >
              <ElOption
                v-for="num in floorNumberOptions"
                :key="num"
                :label="`${num}层`"
                :value="num"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="房间类型" prop="roomType">
            <ElSelect
              v-model="form.roomType"
              placeholder="请选择房间类型"
              filterable
              clearable
              :disabled="hasBeds"
            >
              <ElOption
                v-for="item in roomTypeOptions"
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
          <ElFormItem label="房间状态" prop="roomStatus">
            <ElSelect
              v-model="form.roomStatus"
              placeholder="请选择房间状态"
              filterable
              clearable
              :disabled="hasBeds"
            >
              <ElOption
                v-for="item in roomStatusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="床位数" prop="bedCount">
            <ElInputNumber
              v-model="form.bedCount"
              :min="1"
              :max="20"
              placeholder="请输入床位数"
              class="w-full"
              :disabled="hasBeds"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="房间面积(㎡)" prop="area">
            <ElInputNumber
              v-model="form.area"
              :min="0"
              :precision="2"
              placeholder="房间面积"
              class="w-full"
              :disabled="hasBeds"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="最多入住人数" prop="maxOccupancy">
            <ElInputNumber
              v-model="form.maxOccupancy"
              :min="1"
              :max="form.bedCount || 1"
              placeholder="最多入住人数"
              class="w-full"
              :disabled="hasBeds"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElFormItem label="排序序号" prop="sort">
        <ElInputNumber v-model="form.sort" :min="0" :max="9999" :disabled="hasBeds" />
      </ElFormItem>

      <ElFormItem label="房间设施">
        <ElCheckboxGroup v-model="facilityValues" :disabled="hasBeds">
          <ElCheckbox v-for="item in facilityOptions" :key="item.value" :label="Number(item.value)">
            {{ item.label }}
          </ElCheckbox>
        </ElCheckboxGroup>
      </ElFormItem>

      <ElFormItem label="备注" prop="remark">
        <ElInput
          v-model="form.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注"
          :disabled="hasBeds"
        />
      </ElFormItem>
      <ElAlert v-if="hasBeds" type="warning" :closable="false" style="margin-bottom: 20px">
        该房间下存在床位，无法编辑
      </ElAlert>
    </ElForm>

    <template #footer>
      <ElButton @click="handleClose">取消</ElButton>
      <ElButton type="primary" :loading="loading" @click="handleSubmit">确定</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { fetchAddRoom, fetchUpdateRoom, fetchCheckRoomHasBeds } from '@/api/dormitory-manage'
  import { useDictStore } from '@/store/modules/dict'
  import { useReferenceStore } from '@/store/modules/reference'
  import type { FormInstance, FormRules } from 'element-plus'

  interface Props {
    visible: boolean
    type: 'add' | 'edit'
    editData?: Api.SystemManage.RoomListItem | null
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
  const hasBeds = ref(false)
  const campusList = ref<Api.SystemManage.CampusListItem[]>([])
  const floorList = ref<Api.SystemManage.FloorListItem[]>([])
  const selectedCampusCode = ref<string>('')
  const selectedFloor = ref<Api.SystemManage.FloorListItem | null>(null)
  const floorNumberOptions = ref<number[]>([])
  const roomTypeOptions = ref<Array<{ label: string; value: string }>>([])
  const roomStatusOptions = ref<Array<{ label: string; value: number }>>([])
  const facilityOptions = ref<Array<{ label: string; value: string }>>([])
  const facilityValues = ref<number[]>([])

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
    if (isEdit.value) return '编辑房间'
    return '新增房间'
  })

  // 扩展表单类型，包含 campusCode 用于验证（但提交时不发送）
  type RoomFormData = Partial<Api.SystemManage.RoomSaveParams> & {
    campusCode?: string
  }

  const form = reactive<RoomFormData>({
    roomCode: '',
    roomNumber: '',
    campusCode: undefined,
    floorId: undefined,
    floorNumber: undefined,
    roomType: undefined,
    bedCount: 4,
    maxOccupancy: 4,
    area: undefined,
    hasAirConditioner: 0,
    hasBathroom: 0,
    hasBalcony: 0,
    roomStatus: 1,
    sort: 0,
    status: 1,
    remark: undefined
  })

  const rules = reactive<FormRules>({
    campusCode: [{ required: true, message: '请选择所属校区', trigger: 'change' }],
    roomCode: [{ required: true, message: '请输入房间编码', trigger: 'blur' }],
    roomNumber: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
    floorId: [{ required: true, message: '请选择所属楼层', trigger: 'change' }],
    floorNumber: [{ required: true, message: '请选择楼层数', trigger: 'change' }]
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
      return
    }
    try {
      floorList.value = await referenceStore.loadFloorList(campusCode)
    } catch (error) {
      console.error('加载楼层列表失败:', error)
    }
  }

  /**
   * 加载字典数据
   */
  const loadDictData = async (): Promise<void> => {
    try {
      // 使用 store 批量加载字典数据（一个接口请求）
      const dictRes = await dictStore.loadDictDataBatch([
        'dormitory_room_type',
        'dormitory_room_status',
        'dormitory_room_facility'
      ])

      // 解析房间类型字典
      roomTypeOptions.value = (dictRes.dormitory_room_type || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: item.value
        })
      )

      // 解析房间状态字典
      roomStatusOptions.value = (dictRes.dormitory_room_status || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: Number(item.value)
        })
      )

      // 解析房间设施字典
      facilityOptions.value = (dictRes.dormitory_room_facility || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: item.value
        })
      )
    } catch (error) {
      console.error('加载字典数据失败:', error)
    }
  }

  /**
   * 校区变更处理
   */
  const handleCampusChange = (campusCode: string): void => {
    selectedCampusCode.value = campusCode
    form.campusCode = campusCode // 同步到 form，用于表单验证
    form.floorId = undefined
    form.floorNumber = undefined
    selectedFloor.value = null
    floorNumberOptions.value = []
    loadFloorList(campusCode)
  }

  /**
   * 楼层变更处理
   */
  const handleFloorChange = (floorId: number | undefined): void => {
    if (!floorId) {
      selectedFloor.value = null
      floorNumberOptions.value = []
      form.floorNumber = undefined
      return
    }

    // 查找选中的楼层信息
    const floor = floorList.value.find((item) => item.id === floorId)
    if (floor) {
      selectedFloor.value = floor
      // 生成楼层数选项：从1层到该楼的楼层数
      const maxFloor = floor.floorNumber || 10
      floorNumberOptions.value = Array.from({ length: maxFloor }, (_, i) => i + 1)
    } else {
      selectedFloor.value = null
      floorNumberOptions.value = []
    }
    form.floorNumber = undefined
  }

  /**
   * 加载表单数据
   */
  const loadFormData = async (): Promise<void> => {
    if (isEdit.value && props.editData) {
      // 检查是否有床位关联
      if (props.editData.id) {
        try {
          hasBeds.value = await fetchCheckRoomHasBeds(props.editData.id)
        } catch (error) {
          console.error('检查房间关联关系失败:', error)
          hasBeds.value = false
        }
      }

      const campusCode = props.editData.campusCode || ''
      selectedCampusCode.value = campusCode
      await loadFloorList(campusCode)
      // 编辑时，需要触发楼层变更处理，以加载楼层数选项
      if (props.editData.floorId) {
        handleFloorChange(props.editData.floorId)
      }

      Object.assign(form, {
        id: props.editData.id,
        roomCode: props.editData.roomCode,
        roomNumber: props.editData.roomNumber,
        campusCode: campusCode,
        floorId: props.editData.floorId,
        floorNumber: props.editData.floorNumber || undefined,
        roomType: props.editData.roomType || undefined,
        bedCount: props.editData.bedCount || 4,
        maxOccupancy: props.editData.maxOccupancy || 4,
        area: props.editData.area || undefined,
        hasAirConditioner: props.editData.hasAirConditioner || 0,
        hasBathroom: props.editData.hasBathroom || 0,
        hasBalcony: props.editData.hasBalcony || 0,
        roomStatus: props.editData.roomStatus || 1,
        sort: props.editData.sort || 0,
        status: props.editData.status,
        remark: props.editData.remark || undefined
      })

      // 设置设施复选框
      facilityValues.value = []
      if (form.hasAirConditioner === 1) facilityValues.value.push(1)
      if (form.hasBathroom === 1) facilityValues.value.push(2)
      if (form.hasBalcony === 1) facilityValues.value.push(3)
    } else {
      hasBeds.value = false
      resetForm()
    }
  }

  /**
   * 重置表单
   */
  const resetForm = (): void => {
    Object.assign(form, {
      roomCode: '',
      roomNumber: '',
      campusCode: undefined,
      floorId: undefined,
      floorNumber: undefined,
      roomType: undefined,
      bedCount: 4,
      maxOccupancy: 4,
      area: undefined,
      hasAirConditioner: 0,
      hasBathroom: 0,
      hasBalcony: 0,
      roomStatus: 1,
      sort: 0,
      status: 1,
      remark: undefined
    })
    hasBeds.value = false
    selectedCampusCode.value = ''
    selectedFloor.value = null
    floorNumberOptions.value = []
    facilityValues.value = []
    floorList.value = []
    formRef.value?.clearValidate()
  }

  /**
   * 提交表单
   */
  const handleSubmit = async (): Promise<void> => {
    if (!formRef.value) return

    // 如果被床位关联，不允许提交
    if (hasBeds.value) {
      ElMessage.warning('该房间下存在床位，无法编辑')
      return
    }

    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    // 确保 floorId 不为 undefined
    if (form.floorId === undefined) {
      ElMessage.error('请选择所属楼层')
      return
    }

    // 同步设施复选框到表单
    form.hasAirConditioner = facilityValues.value.includes(1) ? 1 : 0
    form.hasBathroom = facilityValues.value.includes(2) ? 1 : 0
    form.hasBalcony = facilityValues.value.includes(3) ? 1 : 0

    loading.value = true
    try {
      // 提交时排除 campusCode（后端会根据 floorId 自动填充）
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { campusCode: _campusCode, ...submitData } = form
      if (isEdit.value) {
        await fetchUpdateRoom(form.id!, submitData as Api.SystemManage.RoomSaveParams)
      } else {
        await fetchAddRoom(submitData as Api.SystemManage.RoomSaveParams)
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

  /**
   * 监听床位数变化，自动调整最大入住人数
   * 床位数增加时，最多入住人数自动同步增加
   * 床位数减少时，如果最多入住人数超过床位数，则自动调整为床位数
   */
  watch(
    () => form.bedCount,
    (newBedCount, oldBedCount) => {
      if (!newBedCount) return

      // 如果床位数增加，且最多入住人数未设置或小于床位数，则自动设置为床位数
      if (oldBedCount !== undefined && newBedCount > oldBedCount) {
        if (!form.maxOccupancy || form.maxOccupancy < newBedCount) {
          form.maxOccupancy = newBedCount
        }
      }
      // 如果床位数减少，且最多入住人数超过床位数，则自动调整为床位数
      else if (oldBedCount !== undefined && newBedCount < oldBedCount) {
        if (form.maxOccupancy && form.maxOccupancy > newBedCount) {
          form.maxOccupancy = newBedCount
        }
      }
      // 初始化时，如果最多入住人数未设置，则设置为床位数
      else if (oldBedCount === undefined && !form.maxOccupancy) {
        form.maxOccupancy = newBedCount
      }
    }
  )
</script>
