<!-- 批量创建房间弹窗 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    title="批量增加房间"
    width="720px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="120px" label-position="right">
      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="所属校区">
            <ElInput :value="floorData?.campusName || floorData?.campusCode" disabled />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="所属楼层">
            <ElInput :value="floorData?.floorName || floorData?.floorCode" disabled />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElFormItem label="楼层数" prop="floorNumbers">
        <ElCheckboxGroup v-model="form.floorNumbers" class="floor-checkbox-group">
          <ElCheckbox v-for="num in floorNumberOptions" :key="num" :label="num" :value="num">
            {{ num }}层
          </ElCheckbox>
        </ElCheckboxGroup>
      </ElFormItem>

      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="房间状态" prop="roomStatus">
            <ElSelect v-model="form.roomStatus" placeholder="请选择房间状态" filterable clearable>
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
          <ElFormItem label="房间类型" prop="roomType">
            <ElSelect v-model="form.roomType" placeholder="请选择房间类型" filterable clearable>
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
          <ElFormItem label="床位数" prop="bedCount">
            <ElInputNumber
              v-model="form.bedCount"
              :min="1"
              :max="20"
              placeholder="请输入床位数"
              class="w-full"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="最多入住人数" prop="maxOccupancy">
            <ElInputNumber
              v-model="form.maxOccupancy"
              :min="1"
              :max="form.bedCount || 20"
              placeholder="最多入住人数"
              class="w-full"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="每层生成数量" prop="generateCount">
            <ElInputNumber
              v-model="form.generateCount"
              :min="1"
              :max="99"
              placeholder="每层房间数"
              class="w-full"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="房间面积(㎡)" prop="area">
            <ElInputNumber
              v-model="form.area"
              :min="0"
              :precision="2"
              placeholder="房间面积"
              class="w-full"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElFormItem label="房间设施">
        <ElCheckboxGroup v-model="facilityValues">
          <ElCheckbox v-for="item in facilityOptions" :key="item.value" :label="Number(item.value)">
            {{ item.label }}
          </ElCheckbox>
        </ElCheckboxGroup>
      </ElFormItem>

      <ElFormItem label="状态" prop="status">
        <ElRadioGroup v-model="form.status">
          <ElRadio :label="1">启用</ElRadio>
          <ElRadio :label="0">停用</ElRadio>
        </ElRadioGroup>
      </ElFormItem>

      <ElFormItem label="备注" prop="remark">
        <ElInput v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </ElFormItem>

      <!-- 预览区域 -->
      <ElAlert
        v-if="previewInfo.total > 0"
        :title="`将创建 ${previewInfo.total} 个房间`"
        type="info"
        :closable="false"
        show-icon
      >
        <template #default>
          <div class="preview-content">
            <p v-for="(range, index) in previewInfo.ranges" :key="index">
              {{ range.floor }}层：{{ range.codes }}
            </p>
          </div>
        </template>
      </ElAlert>
    </ElForm>

    <template #footer>
      <ElButton @click="handleClose">取消</ElButton>
      <ElButton type="primary" :loading="loading" @click="handleSubmit">确定创建</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { fetchBatchCreateRooms } from '@/api/dormitory-manage'
  import { useDictStore } from '@/store/modules/dict'
  import { ElMessage } from 'element-plus'
  import type { FormInstance, FormRules } from 'element-plus'

  interface Props {
    visible: boolean
    floorData?: Api.SystemManage.FloorListItem | null
  }

  interface Emits {
    (e: 'update:visible', visible: boolean): void
    (e: 'submit'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    floorData: null
  })

  const emit = defineEmits<Emits>()

  const formRef = ref<FormInstance>()
  const loading = ref(false)
  const roomTypeOptions = ref<Array<{ label: string; value: string }>>([])
  const roomStatusOptions = ref<Array<{ label: string; value: number }>>([])
  const facilityOptions = ref<Array<{ label: string; value: string }>>([])
  const facilityValues = ref<number[]>([])

  // 使用字典 store
  const dictStore = useDictStore()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
  })

  // 楼层数选项（从1到楼层的最大层数）
  const floorNumberOptions = computed(() => {
    const maxFloor = props.floorData?.floorNumber || 1
    return Array.from({ length: maxFloor }, (_, i) => i + 1)
  })

  const form = reactive<Api.SystemManage.RoomBatchCreateParams>({
    floorId: 0,
    floorNumbers: [],
    generateCount: 5,
    roomType: undefined,
    roomStatus: 1,
    bedCount: 4,
    area: undefined,
    maxOccupancy: 4,
    status: 1,
    hasAirConditioner: 0,
    hasBathroom: 0,
    hasBalcony: 0,
    remark: undefined
  })

  const rules = reactive<FormRules>({
    floorNumbers: [
      {
        required: true,
        type: 'array',
        min: 1,
        message: '请至少选择一个楼层数',
        trigger: 'change'
      }
    ],
    generateCount: [{ required: true, message: '请输入每层生成数量', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
  })

  // 预览信息
  const previewInfo = computed(() => {
    const selectedFloors = [...form.floorNumbers].sort((a, b) => a - b)
    const count = form.generateCount || 0
    const total = selectedFloors.length * count

    const ranges = selectedFloors.map((floor) => {
      const startCode = floor * 100 + 1
      const endCode = floor * 100 + count
      return {
        floor,
        codes: count === 1 ? `${startCode}` : `${startCode} ~ ${endCode}`
      }
    })

    return { total, ranges }
  })

  /**
   * 加载字典数据
   */
  const loadDictData = async (): Promise<void> => {
    try {
      const dictRes = await dictStore.loadDictDataBatch([
        'dormitory_room_type',
        'dormitory_room_status',
        'dormitory_room_facility'
      ])

      roomTypeOptions.value = (dictRes.dormitory_room_type || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: item.value
        })
      )

      roomStatusOptions.value = (dictRes.dormitory_room_status || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: Number(item.value)
        })
      )

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
   * 重置表单
   */
  const resetForm = (): void => {
    Object.assign(form, {
      floorId: 0,
      floorNumbers: [],
      generateCount: 5,
      roomType: undefined,
      roomStatus: 1,
      bedCount: 4,
      area: undefined,
      maxOccupancy: 4,
      status: 1,
      hasAirConditioner: 0,
      hasBathroom: 0,
      hasBalcony: 0,
      remark: undefined
    })
    facilityValues.value = []
    formRef.value?.clearValidate()
  }

  /**
   * 提交表单
   */
  const handleSubmit = async (): Promise<void> => {
    if (!formRef.value) return

    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    if (!props.floorData?.id) {
      ElMessage.error('楼层信息不完整')
      return
    }

    // 同步设施复选框到表单
    form.hasAirConditioner = facilityValues.value.includes(1) ? 1 : 0
    form.hasBathroom = facilityValues.value.includes(2) ? 1 : 0
    form.hasBalcony = facilityValues.value.includes(3) ? 1 : 0

    // 设置楼层ID
    form.floorId = props.floorData.id

    loading.value = true
    try {
      const createdCount = await fetchBatchCreateRooms(form)
      ElMessage.success(`成功创建 ${createdCount} 个房间`)
      emit('submit')
      handleClose()
    } catch (error) {
      console.error('批量创建房间失败:', error)
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

  // 床位数变化时，自动更新最多入住人数
  watch(
    () => form.bedCount,
    (newBedCount) => {
      if (newBedCount !== undefined) {
        if (form.maxOccupancy === undefined || form.maxOccupancy < newBedCount) {
          form.maxOccupancy = newBedCount
        }
      }
    }
  )

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        loadDictData()
        resetForm()
      }
    },
    { immediate: true }
  )
</script>

<style scoped lang="scss">
  .floor-checkbox-group {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    max-height: 120px;
    padding: 4px 0;
    overflow-y: auto;
  }

  .preview-content {
    margin-top: 8px;
    font-size: 13px;
    color: var(--el-text-color-secondary);

    p {
      margin: 4px 0;
    }
  }

  :deep(.el-input-number) {
    width: 100%;
  }

  :deep(.el-select) {
    width: 100%;
  }
</style>
