<!-- 学年编辑弹窗 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="800px"
    top="5vh"
    align-center
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="right">
      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="学年编码" prop="yearCode">
            <ElInput v-model="form.yearCode" placeholder="请输入学年编码" :disabled="isEdit" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="学年名称" prop="yearName">
            <ElInput v-model="form.yearName" placeholder="请输入学年名称，如：2023-2024学年" />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="开始日期" prop="startDate">
            <ElDatePicker
              v-model="form.startDate"
              type="date"
              placeholder="选择开始日期"
              style="width: 100%"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="结束日期" prop="endDate">
            <ElDatePicker
              v-model="form.endDate"
              type="date"
              placeholder="选择结束日期"
              style="width: 100%"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <!-- 学期列表 -->
      <ElFormItem label="学期管理" prop="semesters">
        <div class="semester-wrapper">
          <!-- 学期列表 -->
          <div v-if="form.semesters.length > 0" class="semester-list">
            <div v-for="(semester, index) in form.semesters" :key="index" class="semester-card">
              <div class="semester-header">
                <div class="semester-index">
                  <ArtSvgIcon icon="ri:calendar-line" class="index-icon" />
                  <span class="index-text">学期 {{ index + 1 }}</span>
                </div>
                <ElButton
                  text
                  type="danger"
                  :icon="Delete"
                  class="delete-btn"
                  @click="removeSemester(index)"
                >
                  删除
                </ElButton>
              </div>
              <div class="semester-content">
                <ElRow :gutter="12">
                  <ElCol :span="12">
                    <div class="form-field">
                      <label class="field-label">学期名称</label>
                      <ElInput
                        v-model="semester.semesterName"
                        placeholder="如：第一学期"
                        size="default"
                      />
                    </div>
                  </ElCol>
                  <ElCol :span="12">
                    <div class="form-field">
                      <label class="field-label">学期编码</label>
                      <ElInput
                        v-model="semester.semesterCode"
                        placeholder="如：2023-1"
                        size="default"
                      />
                    </div>
                  </ElCol>
                  <ElCol :span="12">
                    <div class="form-field">
                      <label class="field-label">开始日期</label>
                      <ElDatePicker
                        v-model="semester.startDate"
                        type="date"
                        placeholder="选择开始日期"
                        style="width: 100%"
                        size="default"
                      />
                    </div>
                  </ElCol>
                  <ElCol :span="12">
                    <div class="form-field">
                      <label class="field-label">结束日期</label>
                      <ElDatePicker
                        v-model="semester.endDate"
                        type="date"
                        placeholder="选择结束日期"
                        style="width: 100%"
                        size="default"
                      />
                    </div>
                  </ElCol>
                </ElRow>
              </div>
            </div>
          </div>
          <!-- 空状态 -->
          <div v-else class="semester-empty">
            <ArtSvgIcon icon="ri:inbox-line" class="empty-icon" />
            <p class="empty-text">暂无学期，请点击下方按钮添加学期</p>
          </div>
          <!-- 添加按钮 -->
          <ElButton type="primary" :icon="Plus" class="add-semester-btn" @click="addSemester">
            添加学期
          </ElButton>
        </div>
      </ElFormItem>
    </ElForm>

    <template #footer>
      <ElButton @click="handleClose">取消</ElButton>
      <ElButton type="primary" :loading="loading" @click="handleSubmit">确定</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { Plus, Delete } from '@element-plus/icons-vue'
  import { ElDatePicker } from 'element-plus'
  import type { FormInstance, FormRules } from 'element-plus'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import { fetchAddAcademicYear, fetchUpdateAcademicYear } from '@/api/school-manage'

  interface SemesterItem {
    semesterName: string
    semesterCode: string
    startDate: string | null
    endDate: string | null
    semesterType?: string
  }

  interface Props {
    visible: boolean
    type: 'add' | 'edit'
    editData?: any | null
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

  const dialogVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
  })

  const isEdit = computed(() => props.type === 'edit')

  const dialogTitle = computed(() => {
    if (isEdit.value) return '编辑学年'
    return '新增学年'
  })

  const form = reactive({
    id: undefined as number | undefined,
    yearCode: '',
    yearName: '',
    startDate: null as string | null,
    endDate: null as string | null,
    status: 1,
    semesters: [] as SemesterItem[]
  })

  const rules = reactive<FormRules>({
    yearCode: [{ required: true, message: '请输入学年编码', trigger: 'blur' }],
    yearName: [{ required: true, message: '请输入学年名称', trigger: 'blur' }],
    startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
    endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
    semesters: [
      {
        required: true,
        validator: (_rule, value, callback) => {
          if (!value || value.length === 0) {
            callback(new Error('请至少添加一个学期'))
          } else {
            callback()
          }
        },
        trigger: 'change'
      }
    ]
  })

  /**
   * 添加学期
   */
  const addSemester = (): void => {
    form.semesters.push({
      semesterName: '',
      semesterCode: '',
      startDate: null,
      endDate: null
    })
    // 触发验证
    formRef.value?.validateField('semesters')
  }

  /**
   * 删除学期
   */
  const removeSemester = (index: number): void => {
    form.semesters.splice(index, 1)
    // 触发验证
    formRef.value?.validateField('semesters')
  }

  /**
   * 加载表单数据
   */
  const loadFormData = (): void => {
    if (isEdit.value && props.editData) {
      Object.assign(form, {
        id: props.editData.id,
        yearCode: props.editData.yearCode || '',
        yearName: props.editData.yearName || '',
        startDate: props.editData.startDate || null,
        endDate: props.editData.endDate || null,
        status: props.editData.status || 1,
        semesters: props.editData.semesters || []
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
      id: undefined,
      yearCode: '',
      yearName: '',
      startDate: null,
      endDate: null,
      status: 1,
      semesters: []
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

    loading.value = true
    try {
      // 格式化提交数据
      const submitData: Api.SystemManage.AcademicYearSaveParams = {
        id: form.id,
        yearCode: form.yearCode,
        yearName: form.yearName,
        startDate: form.startDate || '',
        endDate: form.endDate || '',
        status: form.status,
        semesters: form.semesters.map((semester) => ({
          id: (semester as any).id,
          semesterCode: semester.semesterCode,
          semesterName: semester.semesterName,
          startDate: semester.startDate || '',
          endDate: semester.endDate || '',
          semesterType: semester.semesterType
        }))
      }

      if (isEdit.value && form.id) {
        await fetchUpdateAcademicYear(form.id, submitData)
      } else {
        await fetchAddAcademicYear(submitData)
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
        loadFormData()
      }
    },
    { immediate: true }
  )
</script>

<style scoped lang="scss">
  .semester-wrapper {
    width: 100%;

    .semester-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-bottom: 16px;

      .semester-card {
        overflow: hidden;
        background-color: var(--el-bg-color);
        border: 1px solid var(--el-border-color-lighter);
        border-radius: 6px;
        transition: all 0.3s ease;

        &:hover {
          border-color: var(--el-color-primary-light-7);
          box-shadow: 0 2px 8px 0 rgb(0 0 0 / 6%);

          .semester-header {
            background: linear-gradient(
              135deg,
              rgb(249 245 255 / 80%) 0%,
              var(--el-fill-color-lighter) 100%
            );
          }
        }

        .semester-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 10px 14px;
          background-color: var(--el-fill-color-lighter);
          border-bottom: 1px solid var(--el-border-color-lighter);
          transition: background 0.3s ease;

          .semester-index {
            display: flex;
            gap: 8px;
            align-items: center;

            .index-icon {
              font-size: 16px;
              color: var(--el-color-primary);
            }

            .index-text {
              font-size: 14px;
              font-weight: 600;
              color: var(--el-text-color-primary);
            }
          }

          .delete-btn {
            padding: 4px 12px;
            font-size: 13px;
            transition: all 0.2s ease;

            &:hover {
              color: var(--el-color-danger);
              background-color: var(--el-color-danger-light-9);
            }
          }
        }

        .semester-content {
          padding: 12px 14px;

          .form-field {
            margin-bottom: 8px;

            &:last-child {
              margin-bottom: 0;
            }

            .field-label {
              display: block;
              padding-left: 2px;
              margin-bottom: 4px;
              font-size: 13px;
              font-weight: 500;
              color: var(--el-text-color-secondary);
            }
          }
        }
      }
    }

    .semester-empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 48px 24px;
      margin-bottom: 16px;
      background-color: var(--el-fill-color-extra-light);
      border: 2px dashed var(--el-border-color-light);
      border-radius: 8px;

      .empty-icon {
        margin-bottom: 12px;
        font-size: 64px;
        color: var(--el-text-color-placeholder);
        opacity: 0.6;
      }

      .empty-text {
        margin: 0;
        font-size: 14px;
        color: var(--el-text-color-secondary);
      }
    }

    .add-semester-btn {
      width: 100%;
      height: 40px;
      font-weight: 500;
      border-radius: 6px;
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 4px 12px 0 rgb(64 158 255 / 30%);
        transform: translateY(-1px);
      }
    }
  }
</style>
