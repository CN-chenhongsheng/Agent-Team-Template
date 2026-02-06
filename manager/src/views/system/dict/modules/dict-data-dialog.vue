<template>
  <ElDialog
    v-model="visible"
    :title="dialogTitle"
    width="700px"
    align-center
    class="el-dialog-border"
    @close="handleClose"
  >
    <ArtForm
      ref="formRef"
      v-model="form"
      :items="formItems"
      :span="12"
      :gutter="20"
      label-width="100px"
      :rules="rules"
      :showSubmit="false"
      :showReset="false"
    />

    <template #footer>
      <ElSpace>
        <ElButton @click="handleClose">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ submitLoading ? '提交中...' : '确定' }}
        </ElButton>
      </ElSpace>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import {
    fetchAddDictData,
    fetchUpdateDictData,
    fetchGetDictDataDetail
  } from '@/api/system-manage'
  import { ElMessage, type FormRules } from 'element-plus'
  import ArtForm from '@/components/core/forms/art-form/index.vue'
  import type { FormItem } from '@/components/core/forms/art-form/index.vue'

  type DictDataListItem = Api.SystemManage.DictDataListItem

  interface Props {
    modelValue: boolean
    dialogType: 'add' | 'edit'
    dataData?: DictDataListItem
    dictCode: string
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    dialogType: 'add',
    dataData: undefined,
    dictCode: ''
  })

  const emit = defineEmits<Emits>()

  const formRef = ref()
  const submitLoading = ref(false)

  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const dialogTitle = computed(() => {
    return props.dialogType === 'add' ? '新增字典数据' : '编辑字典数据'
  })

  /**
   * 表单配置项
   */
  const formItems = computed<FormItem[]>(() => [
    {
      key: 'dictCode',
      label: '字典编码',
      type: 'input',
      span: 12,
      props: {
        placeholder: '请输入字典编码',
        disabled: true
      }
    },
    {
      key: 'label',
      label: '字典标签',
      type: 'input',
      span: 12,
      props: {
        placeholder: '请输入字典标签',
        maxlength: 100,
        showWordLimit: true
      }
    },
    {
      key: 'value',
      label: '字典值',
      type: 'input',
      span: 12,
      props: {
        placeholder: '请输入字典值',
        maxlength: 100,
        showWordLimit: true
      }
    },
    {
      key: 'sort',
      label: '排序',
      type: 'number',
      span: 12,
      props: {
        min: 0,
        max: 999,
        placeholder: '排序',
        style: 'width: 100%'
      }
    },
    {
      key: 'listClass',
      label: '表格样式',
      type: 'select',
      span: 12,
      props: {
        placeholder: '请选择表格样式',
        clearable: true,
        style: 'width: 100%',
        options: [
          { label: 'primary', value: 'primary' },
          { label: 'success', value: 'success' },
          { label: 'info', value: 'info' },
          { label: 'warning', value: 'warning' },
          { label: 'danger', value: 'danger' }
        ]
      }
    },
    {
      key: 'cssClass',
      label: 'CSS类名',
      type: 'input',
      span: 12,
      props: {
        placeholder: '请输入CSS类名',
        maxlength: 100
      }
    },
    {
      key: 'isDefault',
      label: '是否默认',
      type: 'radiogroup',
      span: 12,
      props: {
        options: [
          { label: '是', value: 1 },
          { label: '否', value: 0 }
        ]
      }
    },
    {
      key: 'remark',
      label: '备注',
      type: 'input',
      span: 24,
      props: {
        type: 'textarea',
        rows: 3,
        placeholder: '请输入备注',
        maxlength: 500,
        showWordLimit: true
      }
    }
  ])

  const form = reactive<Api.SystemManage.DictDataSaveParams>({
    dictCode: '',
    label: '',
    value: '',
    cssClass: '',
    listClass: '',
    sort: 0,
    isDefault: 0,
    status: 1,
    remark: ''
  })

  const rules = computed<FormRules>(() => ({
    label: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
    value: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
    sort: [{ required: true, message: '请输入排序', trigger: 'blur' }]
  }))

  // 初始化表单数据
  watch(
    () => props.modelValue,
    async (newVal) => {
      if (newVal) {
        if (props.dialogType === 'edit' && props.dataData?.id) {
          try {
            const detail = await fetchGetDictDataDetail(props.dataData.id)
            Object.assign(form, {
              id: detail.id,
              dictCode: detail.dictCode,
              label: detail.label,
              value: detail.value,
              cssClass: detail.cssClass || '',
              listClass: detail.listClass || '',
              sort: detail.sort || 0,
              isDefault: detail.isDefault || 0,
              status: detail.status,
              remark: detail.remark || ''
            })
          } catch (error) {
            console.error('获取字典数据详情失败:', error)
          }
        } else {
          // 重置表单，设置字典编码
          Object.assign(form, {
            id: undefined,
            dictCode: props.dictCode,
            label: '',
            value: '',
            cssClass: '',
            listClass: '',
            sort: 0,
            isDefault: 0,
            status: 1,
            remark: ''
          })
        }
        nextTick(() => {
          formRef.value?.ref?.clearValidate()
        })
      }
    },
    { immediate: true }
  )

  const handleClose = () => {
    visible.value = false
    formRef.value?.reset()
  }

  const handleSubmit = async () => {
    if (!formRef.value) return

    try {
      await formRef.value.validate()

      submitLoading.value = true

      if (props.dialogType === 'add') {
        await fetchAddDictData(form)
      } else {
        if (!form.id) {
          ElMessage.error('字典数据ID不能为空')
          return
        }
        await fetchUpdateDictData(form.id, form)
      }

      emit('success')
      handleClose()
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitLoading.value = false
    }
  }
</script>
