<template>
  <ElDialog
    v-model="visible"
    :title="dialogTitle"
    width="900px"
    top="5vmin"
    destroy-on-close
    @close="handleClose"
  >
    <ElForm
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="120px"
      label-position="right"
    >
      <ElRow :gutter="20">
        <ElCol :span="24">
          <ElFormItem label="通知标题" prop="title">
            <ElInput
              v-model="formData.title"
              placeholder="请输入通知标题"
              maxlength="200"
              show-word-limit
              clearable
            />
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="通知类型" prop="noticeType">
            <ElSelect
              v-model="formData.noticeType"
              placeholder="请选择通知类型"
              clearable
              style="width: 100%"
            >
              <ElOption
                v-for="item in noticeTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="状态" prop="status">
            <ElSelect
              v-model="formData.status"
              placeholder="请选择状态"
              clearable
              style="width: 100%"
            >
              <ElOption label="草稿" :value="1" />
              <ElOption label="已发布" :value="2" />
            </ElSelect>
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="是否置顶" prop="isTop">
            <ElSwitch v-model="formData.isTop" active-text="是" inactive-text="否" />
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="目标楼层">
            <ElSelect
              v-model="formData.targetFloors"
              placeholder="请选择目标楼层(不选则全部)"
              multiple
              collapse-tags
              collapse-tags-tooltip
              clearable
              style="width: 100%"
            >
              <ElOption
                v-for="floor in floorOptions"
                :key="floor.id"
                :label="floor.floorName"
                :value="floor.floorCode"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>

        <ElCol :span="24">
          <ElFormItem label="封面图片">
            <ElUpload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="beforeCoverUpload"
              :on-success="handleCoverUploadSuccess"
              accept="image/*"
            >
              <ElButton type="primary" :icon="Upload">选择图片</ElButton>
              <template #tip>
                <div class="el-upload__tip">只能上传图片文件，且不超过 2MB</div>
              </template>
            </ElUpload>
            <div v-if="formData.coverImage" class="mt-2">
              <ElImage
                :src="formData.coverImage"
                :preview-src-list="[formData.coverImage]"
                fit="cover"
                style="width: 200px; height: 150px"
                class="rounded"
              />
              <ElButton
                type="danger"
                size="small"
                :icon="Delete"
                @click="formData.coverImage = ''"
                class="ml-2"
              >
                删除
              </ElButton>
            </div>
          </ElFormItem>
        </ElCol>

        <ElCol :span="24">
          <ElFormItem label="通知内容" prop="content">
            <ArtWangEditor
              v-model="formData.content"
              height="400px"
              placeholder="请输入通知内容..."
            />
          </ElFormItem>
        </ElCol>

        <ElCol :span="24">
          <ElFormItem label="附件">
            <ElUpload
              v-model:file-list="attachmentFileList"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :before-upload="beforeAttachmentUpload"
              :on-success="handleAttachmentUploadSuccess"
              :on-remove="handleAttachmentRemove"
              :limit="5"
              multiple
            >
              <ElButton type="primary" :icon="Upload">选择文件</ElButton>
              <template #tip>
                <div class="el-upload__tip">最多上传 5 个文件，单个文件不超过 10MB</div>
              </template>
            </ElUpload>
          </ElFormItem>
        </ElCol>

        <ElCol :span="24">
          <ElFormItem label="备注">
            <ElInput
              v-model="formData.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入备注"
              maxlength="500"
              show-word-limit
            />
          </ElFormItem>
        </ElCol>
      </ElRow>
    </ElForm>

    <template #footer>
      <div class="flex justify-end gap-2">
        <ElButton @click="handleClose">取消</ElButton>
        <ElButton type="primary" :loading="submitting" @click="handleSubmit">保存</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import {
    ElDialog,
    ElForm,
    ElFormItem,
    ElRow,
    ElCol,
    ElInput,
    ElSelect,
    ElOption,
    ElSwitch,
    ElUpload,
    ElButton,
    ElImage,
    ElMessage,
    type FormInstance,
    type FormRules,
    type UploadProps,
    type UploadUserFile
  } from 'element-plus'
  import { Upload, Delete } from '@element-plus/icons-vue'
  import { fetchAddNotice, fetchUpdateNotice } from '@/api/system-manage'
  import { useUserStore } from '@/store/modules/user'
  import { useReferenceStore } from '@/store/modules/reference'
  import { useDictStore } from '@/store/modules/dict'
  import ArtWangEditor from '@/components/core/forms/art-wang-editor/index.vue'
  import type { DialogType } from '@/types'

  defineOptions({ name: 'NoticeDialog' })

  interface Props {
    visible: boolean
    type: DialogType
    noticeData?: Partial<Api.SystemManage.NoticeListItem>
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const userStore = useUserStore()
  const referenceStore = useReferenceStore()
  const dictStore = useDictStore()

  const visible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
  })

  const dialogTitle = computed(() => (props.type === 'add' ? '新增通知' : '编辑通知'))

  const formRef = ref<FormInstance>()
  const submitting = ref(false)

  // 表单数据
  const formData = ref<Api.SystemManage.NoticeSaveParams>({
    title: '',
    noticeType: undefined,
    content: '',
    coverImage: '',
    attachments: [],
    isTop: false,
    status: 1,
    targetFloors: [],
    remark: ''
  })

  // 附件文件列表
  const attachmentFileList = ref<UploadUserFile[]>([])

  // 通知类型选项
  const noticeTypeOptions = ref<Array<{ label: string; value: number }>>([])

  // 楼层选项
  const floorOptions = ref<Api.SystemManage.FloorListItem[]>([])

  // 加载通知类型字典
  const loadNoticeTypeOptions = async () => {
    try {
      const data = await dictStore.loadDictData('notice_type')
      if (data && data.length > 0) {
        noticeTypeOptions.value = data.map((item) => ({
          label: item.label,
          value: Number(item.value)
        }))
      }
    } catch (error) {
      console.error('加载通知类型字典失败:', error)
    }
  }

  // 上传配置
  const uploadUrl = computed(() => `${import.meta.env.VITE_API_URL}/api/common/upload`)
  const uploadHeaders = computed(() => ({
    Authorization: userStore.accessToken
  }))

  // 表单验证规则
  const rules: FormRules = {
    title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
    noticeType: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
    content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
  }

  // 封面图片上传前
  const beforeCoverUpload: UploadProps['beforeUpload'] = (file) => {
    const isImage = file.type.startsWith('image/')
    const isLt2M = file.size / 1024 / 1024 < 2

    if (!isImage) {
      ElMessage.error('只能上传图片文件!')
      return false
    }
    if (!isLt2M) {
      ElMessage.error('图片大小不能超过 2MB!')
      return false
    }
    return true
  }

  // 封面图片上传成功
  const handleCoverUploadSuccess: UploadProps['onSuccess'] = (response) => {
    if (response.code === 200) {
      formData.value.coverImage = response.data.url
      ElMessage.success('封面图片上传成功')
    } else {
      ElMessage.error(response.message || '封面图片上传失败')
    }
  }

  // 附件上传前
  const beforeAttachmentUpload: UploadProps['beforeUpload'] = (file) => {
    const isLt10M = file.size / 1024 / 1024 < 10
    if (!isLt10M) {
      ElMessage.error('文件大小不能超过 10MB!')
      return false
    }
    return true
  }

  // 附件上传成功
  const handleAttachmentUploadSuccess: UploadProps['onSuccess'] = (response, file) => {
    if (response.code === 200) {
      if (!Array.isArray(formData.value.attachments)) {
        formData.value.attachments = []
      }
      formData.value.attachments.push(response.data.url)
      ElMessage.success(`文件 ${file.name} 上传成功`)
    } else {
      ElMessage.error(response.message || '文件上传失败')
      // 移除上传失败的文件
      const index = attachmentFileList.value.findIndex((item) => item.uid === file.uid)
      if (index > -1) {
        attachmentFileList.value.splice(index, 1)
      }
    }
  }

  // 附件移除
  const handleAttachmentRemove: UploadProps['onRemove'] = (file) => {
    if (Array.isArray(formData.value.attachments)) {
      const index = attachmentFileList.value.findIndex((item) => item.uid === file.uid)
      if (index > -1) {
        formData.value.attachments.splice(index, 1)
      }
    }
  }

  // 加载楼层选项
  const loadFloorOptions = async () => {
    try {
      const floors = await referenceStore.loadFloorList('')
      floorOptions.value = floors
    } catch (error) {
      console.error('加载楼层列表失败:', error)
      ElMessage.error('加载楼层列表失败')
    }
  }

  // 初始化表单数据
  const initFormData = () => {
    if (props.type === 'edit' && props.noticeData) {
      formData.value = {
        id: props.noticeData.id,
        title: props.noticeData.title || '',
        noticeType: props.noticeData.noticeType,
        content: props.noticeData.content || '',
        coverImage: props.noticeData.coverImage || '',
        attachments: props.noticeData.attachments || [],
        isTop: props.noticeData.isTop || false,
        status: props.noticeData.status || 1,
        targetFloors: props.noticeData.targetFloors
          ? typeof props.noticeData.targetFloors === 'string'
            ? props.noticeData.targetFloors.split(',')
            : props.noticeData.targetFloors
          : [],
        remark: props.noticeData.remark || ''
      }

      // 初始化附件列表
      if (props.noticeData.attachments) {
        const attachments =
          typeof props.noticeData.attachments === 'string'
            ? JSON.parse(props.noticeData.attachments)
            : props.noticeData.attachments
        attachmentFileList.value = attachments.map((url: string, index: number) => ({
          name: `附件${index + 1}`,
          url: url,
          uid: Date.now() + index
        }))
      }
    } else {
      formData.value = {
        title: '',
        noticeType: undefined,
        content: '',
        coverImage: '',
        attachments: [],
        isTop: false,
        status: 1,
        targetFloors: [],
        remark: ''
      }
      attachmentFileList.value = []
    }
  }

  // 监听 visible 变化
  watch(
    () => props.visible,
    (val) => {
      if (val) {
        initFormData()
        loadNoticeTypeOptions()
        loadFloorOptions()
      }
    },
    { immediate: true }
  )

  // 提交
  const handleSubmit = async () => {
    if (!formRef.value) return

    await formRef.value.validate()

    submitting.value = true
    try {
      const submitData: Api.SystemManage.NoticeSaveParams = {
        ...formData.value,
        // 确保 attachments 是数组
        attachments: Array.isArray(formData.value.attachments) ? formData.value.attachments : [],
        // 确保 targetFloors 是数组
        targetFloors: Array.isArray(formData.value.targetFloors) ? formData.value.targetFloors : []
      }

      if (props.type === 'add') {
        await fetchAddNotice(submitData)
      } else {
        if (formData.value.id) {
          await fetchUpdateNotice(formData.value.id, submitData)
        }
      }

      emit('submit')
      handleClose()
    } catch (error) {
      console.error('保存通知失败:', error)
    } finally {
      submitting.value = false
    }
  }

  // 关闭
  const handleClose = () => {
    formRef.value?.resetFields()
    visible.value = false
  }
</script>

<style scoped lang="scss">
  :deep(.el-upload__tip) {
    margin-top: 4px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
</style>
