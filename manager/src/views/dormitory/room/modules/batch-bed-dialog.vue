<!-- 批量创建床位弹窗 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    title="批量增加床位"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="120px" label-position="right">
      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="所属校区">
            <ElInput :value="roomData?.campusName || roomData?.campusCode" disabled />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="所属楼层">
            <ElInput :value="roomData?.floorName || roomData?.floorCode" disabled />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="所属房间">
            <ElInput :value="roomData?.roomNumber || roomData?.roomCode" disabled />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="生成数量" prop="generateCount">
            <ElInputNumber
              v-model="form.generateCount"
              :min="1"
              :max="20"
              placeholder="床位数量"
              class="w-full"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="床位位置" prop="bedPosition">
            <ElSelect
              v-model="form.bedPosition"
              placeholder="请选择床位位置（可选）"
              filterable
              clearable
            >
              <ElOption
                v-for="item in bedPositionOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
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
        :title="`将创建 ${previewInfo.total} 个床位`"
        type="info"
        :closable="false"
        show-icon
      >
        <template #default>
          <div class="preview-content">
            <p>床位编码/床位号：{{ previewInfo.codes }}</p>
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
  import { fetchBatchCreateBeds } from '@/api/dormitory-manage'
  import { useDictStore } from '@/store/modules/dict'
  import { ElMessage } from 'element-plus'
  import type { FormInstance, FormRules } from 'element-plus'

  interface Props {
    visible: boolean
    roomData?: Api.SystemManage.RoomListItem | null
  }

  interface Emits {
    (e: 'update:visible', visible: boolean): void
    (e: 'submit'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    roomData: null
  })

  const emit = defineEmits<Emits>()

  const formRef = ref<FormInstance>()
  const loading = ref(false)
  const bedPositionOptions = ref<Array<{ label: string; value: string }>>([])
  const bedStatusOptions = ref<Array<{ label: string; value: number }>>([])

  // 使用字典 store
  const dictStore = useDictStore()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
  })

  const form = reactive<Api.SystemManage.BedBatchCreateParams>({
    roomId: 0,
    generateCount: 4,
    bedPosition: undefined,
    bedStatus: 1,
    checkInDate: undefined,
    checkOutDate: undefined,
    status: 1,
    remark: undefined
  })

  const rules = reactive<FormRules>({
    generateCount: [{ required: true, message: '请输入生成数量', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
  })

  // 预览信息
  const previewInfo = computed(() => {
    const count = form.generateCount || 0
    const codes = count > 0 ? `1 ~ ${count}` : ''
    return { total: count, codes }
  })

  /**
   * 加载字典数据
   */
  const loadDictData = async (): Promise<void> => {
    try {
      const dictRes = await dictStore.loadDictDataBatch([
        'dormitory_bed_position',
        'dormitory_bed_status'
      ])

      bedPositionOptions.value = (dictRes.dormitory_bed_position || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: item.value
        })
      )

      bedStatusOptions.value = (dictRes.dormitory_bed_status || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: Number(item.value)
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
      roomId: 0,
      generateCount: props.roomData?.bedCount || 4,
      bedPosition: undefined,
      bedStatus: 1,
      checkInDate: undefined,
      checkOutDate: undefined,
      status: 1,
      remark: undefined
    })
    formRef.value?.clearValidate()
  }

  /**
   * 提交表单
   */
  const handleSubmit = async (): Promise<void> => {
    if (!formRef.value) return

    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    if (!props.roomData?.id) {
      ElMessage.error('房间信息不完整')
      return
    }

    // 设置房间ID
    form.roomId = props.roomData.id

    loading.value = true
    try {
      const createdCount = await fetchBatchCreateBeds(form)
      ElMessage.success(`成功创建 ${createdCount} 个床位`)
      emit('submit')
      handleClose()
    } catch (error) {
      console.error('批量创建床位失败:', error)
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
        loadDictData()
        resetForm()
      }
    },
    { immediate: true }
  )
</script>

<style scoped lang="scss">
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
