<!-- 楼层编辑弹窗 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="right">
      <ElFormItem label="楼层编码" prop="floorCode">
        <ElInput
          v-model="form.floorCode"
          placeholder="请输入楼层编码（如：F1、F2）"
          :disabled="isEdit || hasRooms"
        />
      </ElFormItem>

      <ElFormItem label="楼层名称" prop="floorName">
        <ElInput
          v-model="form.floorName"
          placeholder="请输入楼层名称（如：1楼、2楼）"
          :disabled="hasRooms"
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
              :disabled="hasRooms"
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
          <ElFormItem label="楼层数" prop="floorNumber">
            <ElInputNumber
              v-model="form.floorNumber"
              :min="1"
              :max="999"
              placeholder="请输入楼层数"
              :disabled="hasRooms"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElFormItem label="适用类别" prop="genderType">
        <ElRadioGroup v-model="form.genderType" :disabled="hasRooms">
          <ElRadio
            v-for="option in genderTypeOptions"
            :key="option.value"
            :label="Number(option.value)"
          >
            {{ option.label }}
          </ElRadio>
        </ElRadioGroup>
      </ElFormItem>

      <ElFormItem label="排序序号" prop="sort">
        <ElInputNumber v-model="form.sort" :min="0" :max="9999" :disabled="hasRooms" />
      </ElFormItem>

      <ElFormItem label="备注" prop="remark">
        <ElInput
          v-model="form.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注"
          :disabled="hasRooms"
        />
      </ElFormItem>
      <ElAlert v-if="hasRooms" type="warning" :closable="false" style="margin-bottom: 20px">
        该楼层下存在房间，无法编辑
      </ElAlert>
    </ElForm>

    <template #footer>
      <ElButton @click="handleClose">取消</ElButton>
      <ElButton type="primary" :loading="loading" @click="handleSubmit">确定</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { fetchAddFloor, fetchUpdateFloor, fetchCheckFloorHasRooms } from '@/api/dormitory-manage'
  import { useDictStore } from '@/store/modules/dict'
  import { useReferenceStore } from '@/store/modules/reference'
  import { ElMessage } from 'element-plus'
  import type { FormInstance, FormRules } from 'element-plus'

  interface Props {
    visible: boolean
    type: 'add' | 'edit'
    editData?: Api.SystemManage.FloorListItem | null
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
  const hasRooms = ref(false)
  const campusList = ref<Api.SystemManage.CampusListItem[]>([])
  const genderTypeOptions = ref<Array<{ label: string; value: string }>>([])

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
    if (isEdit.value) return '编辑楼层'
    return '新增楼层'
  })

  const form = reactive<Api.SystemManage.FloorSaveParams>({
    floorCode: '',
    floorName: '',
    floorNumber: 1,
    campusCode: '',
    genderType: 3,
    sort: 0,
    status: 1,
    remark: undefined
  })

  const rules = reactive<FormRules>({
    floorCode: [{ required: true, message: '请输入楼层编码', trigger: 'blur' }],
    floorNumber: [{ required: true, message: '请输入楼层数', trigger: 'blur' }],
    campusCode: [{ required: true, message: '请选择所属校区', trigger: 'change' }],
    genderType: [{ required: true, message: '请选择适用性别', trigger: 'change' }]
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
   * 加载字典数据
   */
  const loadDictData = async (): Promise<void> => {
    try {
      const dictRes = await dictStore.loadDictDataBatch(['dormitory_gender_type'])

      // 解析适用性别字典
      genderTypeOptions.value = (dictRes.dormitory_gender_type || []).map(
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
   * 加载表单数据
   */
  const loadFormData = async (): Promise<void> => {
    if (isEdit.value && props.editData) {
      // 检查是否有房间关联
      if (props.editData.id) {
        try {
          hasRooms.value = await fetchCheckFloorHasRooms(props.editData.id)
        } catch (error) {
          console.error('检查楼层关联关系失败:', error)
          hasRooms.value = false
        }
      }

      Object.assign(form, {
        id: props.editData.id,
        floorCode: props.editData.floorCode,
        floorName: props.editData.floorName || '',
        floorNumber: props.editData.floorNumber,
        campusCode: props.editData.campusCode,
        genderType: props.editData.genderType,
        sort: props.editData.sort || 0,
        status: props.editData.status,
        remark: props.editData.remark || undefined
      })
    } else {
      hasRooms.value = false
      resetForm()
    }
  }

  /**
   * 重置表单
   */
  const resetForm = (): void => {
    Object.assign(form, {
      floorCode: '',
      floorName: '',
      floorNumber: 1,
      campusCode: '',
      genderType: 3,
      sort: 0,
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

    // 如果被房间关联，不允许提交
    if (hasRooms.value) {
      ElMessage.warning('该楼层下存在房间，无法编辑')
      return
    }

    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    loading.value = true
    try {
      if (isEdit.value) {
        await fetchUpdateFloor(form.id!, form)
      } else {
        await fetchAddFloor(form)
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
