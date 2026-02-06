<template>
  <ElDialog
    v-model="visible"
    :title="dialogTitle"
    width="600px"
    align-center
    class="el-dialog-border"
    @close="handleClose"
  >
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
    fetchAddDictType,
    fetchUpdateDictType,
    fetchGetDictTypeDetail
  } from '@/api/system-manage'
  import { ElMessage, type FormRules } from 'element-plus'
  import ArtForm from '@/components/core/forms/art-form/index.vue'
  import type { FormItem } from '@/components/core/forms/art-form/index.vue'

  type DictTypeListItem = Api.SystemManage.DictTypeListItem

  interface Props {
    modelValue: boolean
    dialogType: 'add' | 'edit'
    typeData?: DictTypeListItem
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
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
    return props.dialogType === 'add' ? '新增字典类型' : '编辑字典类型'
  })

  /**
   * 表单配置项
   */
  const formItems = computed<FormItem[]>(() => [
    {
      key: 'dictName',
      label: '字典名称',
      type: 'input',
      span: 24,
      props: {
        placeholder: '请输入字典名称',
        maxlength: 100,
        showWordLimit: true
      }
    },
    {
      key: 'dictCode',
      label: '字典编码',
      type: 'input',
      span: 24,
      props: {
        placeholder: '请输入字典编码（如：sys_user_sex）',
        maxlength: 100,
        showWordLimit: true,
        disabled: props.dialogType === 'edit'
      },
      slots: {
        default: () => h('div', { class: 'form-tip' }, '编码唯一，编辑后不可修改')
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

  const form = reactive<Api.SystemManage.DictTypeSaveParams>({
    dictName: '',
    dictCode: '',
    status: 1,
    remark: ''
  })

  const rules = computed<FormRules>(() => ({
    dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
    dictCode: [
      { required: true, message: '请输入字典编码', trigger: 'blur' },
      {
        pattern: /^[a-z][a-z0-9_]*$/,
        message: '字典编码只能包含小写字母、数字和下划线，且必须以字母开头',
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
        if (!form.id) {
          ElMessage.error('字典类型ID不能为空')
          return
        }
        await fetchUpdateDictType(form.id, form)
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

<style scoped lang="scss">
  .form-tip {
    margin-top: 4px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
</style>
