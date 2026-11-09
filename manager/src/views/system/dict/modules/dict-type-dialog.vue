<template>
  <ElDialog v-model="visible" :title="dialogTitle" width="600px" align-center @close="handleClose">
    <ArtForm
      ref="formRef"
      v-model="form"
      :items="formItems"
      :span="24"
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
    fetchAddDictType,
    fetchUpdateDictType,
    fetchGetDictTypeDetail
  } from '@/api/system-manage'
  import type { FormRules } from 'element-plus'
  import ArtForm from '@/components/core/forms/art-form/index.vue'
  import type { FormItem } from '@/components/core/forms/art-form/index.vue'
  import type { DialogType } from '@/types'

  const { t } = useI18n()

  type DictTypeListItem = Api.SystemManage.DictTypeListItem

  interface Props {
    modelValue: boolean
    dialogType: DialogType
    typeData?: DictTypeListItem
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success', payload: { action: DialogType; dictCode: string }): void
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    dialogType: 'add',
    typeData: undefined
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
      ? t('system.dict.typeForm.addTitle')
      : t('system.dict.typeForm.editTitle')
  })

  /**
   * 表单配置项
   */
  const formItems = computed<FormItem[]>(() => [
    {
      key: 'dictName',
      label: t('system.dict.typeForm.dictName'),
      type: 'input',
      span: 24,
      props: {
        placeholder: t('system.dict.typeForm.namePlaceholder'),
        maxlength: 100,
        showWordLimit: true
      }
    },
    {
      key: 'dictCode',
      label: t('system.dict.typeForm.dictCode'),
      type: 'input',
      span: 24,
      props: {
        placeholder: t('system.dict.typeForm.codePlaceholder'),
        maxlength: 100,
        showWordLimit: true,
        disabled: props.dialogType === 'edit'
      },
      slots: {
        default: () => h('div', { class: 'form-tip' }, t('system.dict.typeForm.codeTip'))
      }
    },
    {
      key: 'remark',
      label: t('system.dict.typeForm.remark'),
      type: 'input',
      span: 24,
      props: {
        type: 'textarea',
        rows: 3,
        placeholder: t('system.dict.typeForm.remarkPlaceholder'),
        maxlength: 500,
        showWordLimit: true
      }
    }
  ])

  const form = reactive<Api.SystemManage.DictTypeSaveParams>({
    dictName: '',
    dictCode: '',
    status: 1,
    remark: ''
  })

  const rules = computed<FormRules>(() => ({
    dictName: [
      { required: true, message: t('system.dict.typeForm.dictNameRequired'), trigger: 'blur' }
    ],
    dictCode: [
      { required: true, message: t('system.dict.typeForm.dictCodeRequired'), trigger: 'blur' },
      {
        pattern: /^[a-z][a-z0-9_]*$/,
        message: t('system.dict.typeForm.dictCodePattern'),
        trigger: 'blur'
      }
    ]
  }))

  // 初始化表单数据
  watch(
    () => props.modelValue,
    async (newVal) => {
      if (newVal) {
        if (props.dialogType === 'edit' && props.typeData?.id) {
          try {
            const detail = await fetchGetDictTypeDetail(props.typeData.id)
            Object.assign(form, {
              id: detail.id,
              dictName: detail.dictName,
              dictCode: detail.dictCode,
              status: detail.status,
              remark: detail.remark || ''
            })
          } catch (error) {
            console.error('获取字典类型详情失败:', error)
          }
        } else {
          // 重置表单
          Object.assign(form, {
            id: undefined,
            dictName: '',
            dictCode: '',
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
        await fetchAddDictType(form)
      } else {
        if (!form.id) return
        await fetchUpdateDictType(form.id, form)
      }

      emit('success', { action: props.dialogType, dictCode: form.dictCode })
      handleClose()
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitLoading.value = false
    }
  }
</script>

<style scoped lang="scss">
  .form-tip {
    margin-top: 4px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
</style>
