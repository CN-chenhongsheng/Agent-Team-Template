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
        <ElButton @click="handleClose">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ submitLoading ? $t('system.common.saving') : $t('common.confirm') }}
        </ElButton>
      </ElSpace>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import {
    fetchAddDictData,
    fetchUpdateDictData,
    fetchGetDictDataDetail
  } from '@/api/system-manage'
  import type { FormRules } from 'element-plus'
  import ArtForm from '@/components/core/forms/art-form/index.vue'
  import type { FormItem } from '@/components/core/forms/art-form/index.vue'
  import type { DialogType } from '@/types'

  const { t } = useI18n()

  type DictDataListItem = Api.SystemManage.DictDataListItem

  interface Props {
    modelValue: boolean
    dialogType: DialogType
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
    return props.dialogType === 'add'
      ? t('system.dict.dataForm.addTitle')
      : t('system.dict.dataForm.editTitle')
  })

  /**
   * 表单配置项
   */
  const formItems = computed<FormItem[]>(() => [
    {
      key: 'dictCode',
      label: t('system.dict.dataForm.dictCode'),
      type: 'input',
      span: 12,
      props: {
        placeholder: t('system.dict.dataForm.codePlaceholder'),
        disabled: true
      }
    },
    {
      key: 'label',
      label: t('system.dict.dataForm.label'),
      type: 'input',
      span: 12,
      props: {
        placeholder: t('system.dict.dataForm.labelPlaceholder'),
        maxlength: 100,
        showWordLimit: true
      }
    },
    {
      key: 'value',
      label: t('system.dict.dataForm.value'),
      type: 'input',
      span: 12,
      props: {
        placeholder: t('system.dict.dataForm.valuePlaceholder'),
        maxlength: 100,
        showWordLimit: true
      }
    },
    {
      key: 'sort',
      label: t('system.dict.dataForm.sort'),
      type: 'number',
      span: 12,
      props: {
        min: 0,
        max: 999,
        placeholder: t('system.dict.dataForm.sortPlaceholder'),
        style: 'width: 100%'
      }
    },
    {
      key: 'listClass',
      label: t('system.dict.dataForm.style'),
      type: 'select',
      span: 12,
      props: {
        placeholder: t('system.dict.dataForm.stylePlaceholder'),
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
      label: t('system.dict.dataForm.cssClass'),
      type: 'input',
      span: 12,
      props: {
        placeholder: t('system.dict.dataForm.cssClassPlaceholder'),
        maxlength: 100
      }
    },
    {
      key: 'isDefault',
      label: t('system.dict.dataForm.isDefault'),
      type: 'radiogroup',
      span: 12,
      props: {
        options: [
          { label: t('system.dict.dataForm.yes'), value: 1 },
          { label: t('system.dict.dataForm.no'), value: 0 }
        ]
      }
    },
    {
      key: 'remark',
      label: t('system.dict.dataForm.remark'),
      type: 'input',
      span: 24,
      props: {
        type: 'textarea',
        rows: 3,
        placeholder: t('system.dict.dataForm.remarkPlaceholder'),
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
    label: [{ required: true, message: t('system.dict.dataForm.labelRequired'), trigger: 'blur' }],
    value: [{ required: true, message: t('system.dict.dataForm.valueRequired'), trigger: 'blur' }],
    sort: [{ required: true, message: t('system.dict.dataForm.sortRequired'), trigger: 'blur' }]
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
        if (!form.id) return
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
