<template>
  <ElDialog v-model="visible" :title="dialogTitle" width="700px" align-center @close="handleClose">
    <div class="dict-data-context">
      <div class="dict-data-context__scope">
        <span class="dict-data-context__label">{{ $t('system.dict.dataForm.scope') }}</span>
        <span class="dict-data-context__name">{{ dictName || '-' }}</span>
        <code class="dict-data-context__code">{{ dictCode }}</code>
      </div>
      <div class="dict-data-context__preview">
        <span class="dict-data-context__label">{{ $t('system.dict.dataForm.preview') }}</span>
        <ElTag v-if="form.listClass" :type="getTagType(form.listClass)" size="small">
          {{ form.label || $t('system.dict.dataForm.previewLabel') }}
        </ElTag>
        <span v-else class="dict-data-context__empty">
          {{ $t('system.dict.dataForm.noStyle') }}
        </span>
      </div>
    </div>

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
        <ElButton
          v-if="dialogType === 'add'"
          :loading="submitLoading && submitMode === 'continue'"
          :disabled="submitLoading"
          @click="handleSubmit(true)"
        >
          {{ $t('system.dict.dataForm.saveAndContinue') }}
        </ElButton>
        <ElButton
          type="primary"
          :loading="submitLoading && submitMode === 'save'"
          :disabled="submitLoading"
          @click="handleSubmit(false)"
        >
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
  type TagType = 'primary' | 'success' | 'info' | 'warning' | 'danger'

  const TAG_TYPES: TagType[] = ['primary', 'success', 'info', 'warning', 'danger']

  interface Props {
    modelValue: boolean
    dialogType: DialogType
    dataData?: DictDataListItem
    dictCode: string
    dictName?: string
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success', payload: { action: DialogType }): void
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    dialogType: 'add',
    dataData: undefined,
    dictCode: '',
    dictName: ''
  })

  const emit = defineEmits<Emits>()

  const formRef = ref()
  const submitLoading = ref(false)
  const submitMode = ref<'save' | 'continue'>('save')

  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const dialogTitle = computed(() => {
    const title =
      props.dialogType === 'add'
        ? t('system.dict.dataForm.addTitle')
        : t('system.dict.dataForm.editTitle')
    return props.dictName ? `${title} · ${props.dictName}` : title
  })

  const getTagType = (value?: string): TagType =>
    TAG_TYPES.includes(value as TagType) ? (value as TagType) : 'info'

  /**
   * 表单配置项
   */
  const formItems = computed<FormItem[]>(() => [
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
          { label: t('system.dict.dataForm.stylePrimary'), value: 'primary' },
          { label: t('system.dict.dataForm.styleSuccess'), value: 'success' },
          { label: t('system.dict.dataForm.styleInfo'), value: 'info' },
          { label: t('system.dict.dataForm.styleWarning'), value: 'warning' },
          { label: t('system.dict.dataForm.styleDanger'), value: 'danger' }
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

  const resetForContinue = () => {
    Object.assign(form, {
      id: undefined,
      label: '',
      value: '',
      cssClass: '',
      listClass: '',
      sort: (form.sort ?? 0) + 1,
      isDefault: 0,
      status: 1,
      remark: ''
    })
    nextTick(() => {
      formRef.value?.ref?.clearValidate()
    })
  }

  const handleSubmit = async (continueAdd = false) => {
    if (!formRef.value) return

    try {
      await formRef.value.validate()

      submitMode.value = continueAdd ? 'continue' : 'save'
      submitLoading.value = true

      if (props.dialogType === 'add') {
        await fetchAddDictData(form)
      } else {
        if (!form.id) return
        await fetchUpdateDictData(form.id, form)
      }

      emit('success', { action: props.dialogType })

      if (continueAdd && props.dialogType === 'add') {
        resetForContinue()
      } else {
        handleClose()
      }
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitLoading.value = false
    }
  }
</script>

<style scoped lang="scss">
  .dict-data-context {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
    padding: 8px 12px;
    margin-bottom: 16px;
    font-size: 12px;
    line-height: 20px;
    color: var(--el-text-color-regular);
    background: var(--el-bg-color);
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 6px;

    &__scope,
    &__preview {
      display: flex;
      gap: 8px;
      align-items: center;
      min-height: 20px;
    }

    // 预览组靠右，形成「左上下文 ｜ 右预览」两区
    &__preview {
      margin-left: auto;
    }

    &__label {
      color: var(--el-text-color-secondary);
    }

    &__name {
      font-size: 13px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    &__code {
      padding: 0 6px;
      font-family:
        ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, 'Liberation Mono', monospace;
      color: var(--el-text-color-regular);
      background: var(--el-fill-color);
      border-radius: 4px;
    }

    &__empty {
      color: var(--el-text-color-placeholder);
    }
  }
</style>
