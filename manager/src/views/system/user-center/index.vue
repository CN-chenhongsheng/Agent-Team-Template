<!-- 个人中心页面 -->
<template>
  <div class="w-full h-full p-0 bg-transparent border-none shadow-none">
    <div class="relative flex-b mt-2.5 max-md:block max-md:mt-1">
      <!-- 左侧用户信息卡片 -->
      <div class="w-112 mr-5 max-md:w-full max-md:mr-0">
        <div class="art-card-sm relative p-9 pb-6 overflow-hidden text-center">
          <img class="absolute top-0 left-0 w-full h-50 object-cover" src="@imgs/user/bg.webp" />
          <img
            class="relative z-10 w-20 h-20 mt-30 mx-auto object-cover border-2 border-white rounded-full"
            :src="userProfile?.avatar || defaultAvatar"
          />
          <h2 class="mt-5 text-xl font-normal">{{ userProfile?.nickname || '加载中...' }}</h2>
          <p class="mt-5 text-sm text-g-500">{{
            userProfile?.introduction || '这个人很懒，什么都没写~'
          }}</p>

          <div class="w-75 mx-auto mt-7.5 text-left">
            <div class="mt-2.5" v-if="userProfile?.email">
              <ArtSvgIcon icon="ri:mail-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ userProfile.email }}</span>
            </div>
            <div class="mt-2.5" v-if="userProfile?.genderText">
              <ArtSvgIcon icon="ri:user-3-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ userProfile.genderText }}</span>
            </div>
            <div class="mt-2.5" v-if="userProfile?.address">
              <ArtSvgIcon icon="ri:map-pin-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ userProfile.address }}</span>
            </div>
            <div class="mt-2.5" v-if="userProfile?.phone">
              <ArtSvgIcon icon="ri:phone-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ userProfile.phone }}</span>
            </div>
            <div class="mt-2.5" v-if="userProfile?.roleNames">
              <ArtSvgIcon icon="ri:building-line" class="text-g-700" />
              <span class="inline-block ml-2 text-sm">
                <div
                  v-for="(role, index) in userProfile.roleNames"
                  :key="role"
                  :class="[
                    'py-1 px-1.5 mb-2.5 text-xs border border-g-300 rounded',
                    index < userProfile.roleNames.length - 1 ? 'mr-2.5' : 'mr-0'
                  ]"
                >
                  {{ role }}
                </div>
              </span>
            </div>
          </div>

          <!-- 管理范围 -->
          <div class="mt-10" v-if="userProfile?.manageScope">
            <h3 class="text-sm font-medium">管理范围</h3>
            <div class="flex flex-wrap justify-center mt-3.5">
              <ElTag
                v-for="(part, index) in manageScopeTags"
                :key="index"
                size="small"
                type="primary"
                class="mb-2.5"
                :class="index < manageScopeTags.length - 1 ? 'mr-2.5' : 'mr-0'"
              >
                {{ part }}
              </ElTag>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区域 -->
      <div class="flex-1 overflow-hidden max-md:w-full max-md:mt-3.5">
        <!-- 基本设置表单 -->
        <div class="art-card-sm">
          <h1 class="p-4 text-xl font-normal border-b border-g-300">基本设置</h1>

          <ElForm
            :model="form"
            class="box-border p-5 [&>.el-row_.el-form-item]:w-[calc(50%-10px)] [&>.el-row_.el-input]:w-full [&>.el-row_.el-select]:w-full"
            ref="profileFormRef"
            :rules="profileRules"
            label-width="86px"
            label-position="top"
          >
            <ElRow>
              <ElFormItem label="昵称" prop="nickname">
                <ElInput v-model="form.nickname" :disabled="!isEdit" placeholder="请输入昵称" />
              </ElFormItem>
              <ElFormItem label="性别" prop="gender" class="ml-5">
                <ElSelect v-model="form.gender" placeholder="请选择性别" :disabled="!isEdit">
                  <ElOption
                    v-for="item in genderOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </ElSelect>
              </ElFormItem>
            </ElRow>

            <ElRow>
              <ElFormItem label="邮箱" prop="email">
                <ElInput v-model="form.email" :disabled="!isEdit" placeholder="请输入邮箱" />
              </ElFormItem>
              <ElFormItem label="手机" prop="phone" class="ml-5">
                <ElInput v-model="form.phone" :disabled="!isEdit" placeholder="请输入手机号" />
              </ElFormItem>
            </ElRow>

            <ElFormItem label="地址" prop="address">
              <ElInput v-model="form.address" :disabled="!isEdit" placeholder="请输入地址" />
            </ElFormItem>

            <ElFormItem label="个人介绍" prop="introduction" class="h-32">
              <ElInput
                type="textarea"
                :rows="4"
                v-model="form.introduction"
                :disabled="!isEdit"
                placeholder="请输入个人介绍"
              />
            </ElFormItem>

            <div class="flex-c justify-end [&_.el-button]:!w-27.5">
              <ElButton v-if="isEdit" class="mr-2.5" v-ripple @click="cancelEdit"> 取消 </ElButton>
              <ElButton
                type="primary"
                class="w-22.5"
                v-ripple
                :loading="profileLoading"
                @click="handleProfileSubmit"
              >
                {{ isEdit ? '保存' : '编辑' }}
              </ElButton>
            </div>
          </ElForm>
        </div>

        <!-- 更改密码表单 -->
        <div class="art-card-sm my-5">
          <h1 class="p-4 text-xl font-normal border-b border-g-300">更改密码</h1>

          <ElForm
            :model="pwdForm"
            class="box-border p-5"
            ref="pwdFormRef"
            :rules="pwdRules"
            label-width="86px"
            label-position="top"
          >
            <ElFormItem label="当前密码" prop="oldPassword">
              <ElInput
                v-model="pwdForm.oldPassword"
                type="password"
                :disabled="!isEditPwd"
                show-password
                placeholder="请输入当前密码"
              />
            </ElFormItem>

            <ElFormItem label="新密码" prop="newPassword">
              <ElInput
                v-model="pwdForm.newPassword"
                type="password"
                :disabled="!isEditPwd"
                show-password
                placeholder="请输入新密码（6-20位）"
              />
            </ElFormItem>

            <ElFormItem label="确认新密码" prop="confirmPassword">
              <ElInput
                v-model="pwdForm.confirmPassword"
                type="password"
                :disabled="!isEditPwd"
                show-password
                placeholder="请再次输入新密码"
              />
            </ElFormItem>

            <div class="flex-c justify-end [&_.el-button]:!w-27.5">
              <ElButton v-if="isEditPwd" class="mr-2.5" v-ripple @click="cancelPwdEdit">
                取消
              </ElButton>
              <ElButton
                type="primary"
                class="w-22.5"
                v-ripple
                :loading="pwdLoading"
                @click="handlePwdSubmit"
              >
                {{ isEditPwd ? '保存' : '编辑' }}
              </ElButton>
            </div>
          </ElForm>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ElMessage, ElTag } from 'element-plus'
  import type { FormInstance, FormRules } from 'element-plus'
  import { fetchGetUserProfile, fetchUpdateUserProfile, fetchChangePassword } from '@/api/auth'
  import { useDictStore } from '@/store/modules/dict'
  import defaultAvatarImg from '@/assets/images/user/avatar.webp'
  import { formatManageScopeSync } from '@/utils/school/scopeFormatter'

  defineOptions({ name: 'UserCenter' })

  // 默认头像
  const defaultAvatar = defaultAvatarImg

  // 用户详细信息
  const userProfile = ref<Api.Auth.UserProfile | null>(null)

  // 性别选项（从字典获取）
  const genderOptions = ref<{ label: string; value: number }[]>([
    { label: '未知', value: 0 },
    { label: '男', value: 1 },
    { label: '女', value: 2 }
  ])

  // 使用字典 store
  const dictStore = useDictStore()

  // 管理范围标签列表
  const manageScopeTags = computed(() => {
    if (!userProfile.value?.manageScope) return []
    const formatted = formatManageScopeSync(userProfile.value.manageScope)
    if (!formatted || formatted === '未设置' || formatted === '格式错误') return []
    return formatted.split('、').filter((part) => part.trim())
  })

  // 表单引用
  const profileFormRef = ref<FormInstance>()
  const pwdFormRef = ref<FormInstance>()

  // 编辑状态
  const isEdit = ref(false)
  const isEditPwd = ref(false)

  // 加载状态
  const profileLoading = ref(false)
  const pwdLoading = ref(false)

  // 用户信息表单
  const form = reactive({
    nickname: '',
    email: '',
    phone: '',
    gender: 0 as number | undefined,
    address: '',
    introduction: ''
  })

  // 表单原始数据（用于取消编辑时恢复）
  const originalForm = reactive({ ...form })

  // 密码修改表单
  const pwdForm = reactive({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })

  // 个人信息表单验证规则
  const profileRules = reactive<FormRules>({
    nickname: [
      { required: true, message: '请输入昵称', trigger: 'blur' },
      { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
    ],
    email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
    phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }],
    address: [{ max: 255, message: '地址长度不能超过255个字符', trigger: 'blur' }],
    introduction: [{ max: 500, message: '个人介绍长度不能超过500个字符', trigger: 'blur' }]
  })

  // 确认密码验证
  const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
    if (value !== pwdForm.newPassword) {
      callback(new Error('两次输入的密码不一致'))
    } else {
      callback()
    }
  }

  // 密码表单验证规则
  const pwdRules = reactive<FormRules>({
    oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度应在 6 到 20 个字符', trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: '请再次输入新密码', trigger: 'blur' },
      { validator: validateConfirmPassword, trigger: 'blur' }
    ]
  })

  /**
   * 获取用户个人信息
   */
  const loadUserProfile = async () => {
    try {
      const data = await fetchGetUserProfile()
      userProfile.value = data

      // 填充表单数据
      form.nickname = data.nickname || ''
      form.email = data.email || ''
      form.phone = data.phone || ''
      form.gender = data.gender ?? 0
      form.address = data.address || ''
      form.introduction = data.introduction || ''

      // 保存原始数据
      Object.assign(originalForm, form)
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  /**
   * 加载性别字典
   */
  const loadGenderDict = async () => {
    try {
      // 使用 store 加载字典数据（自动缓存）
      const data = await dictStore.loadDictData('sys_user_sex')
      if (data && data.length > 0) {
        genderOptions.value = data.map((item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: Number(item.value)
        }))
      }
    } catch (error) {
      console.error('获取性别字典失败:', error)
      // 使用默认值
    }
  }

  /**
   * 处理个人信息提交
   */
  const handleProfileSubmit = async () => {
    if (!isEdit.value) {
      isEdit.value = true
      return
    }

    // 验证表单
    const valid = await profileFormRef.value?.validate().catch(() => false)
    if (!valid) return

    profileLoading.value = true
    try {
      await fetchUpdateUserProfile({
        nickname: form.nickname,
        email: form.email || undefined,
        phone: form.phone || undefined,
        gender: form.gender,
        address: form.address || undefined,
        introduction: form.introduction || undefined
      })

      ElMessage.success('个人信息更新成功')
      isEdit.value = false

      // 重新加载用户信息
      await loadUserProfile()
    } catch (error) {
      console.error('更新个人信息失败:', error)
    } finally {
      profileLoading.value = false
    }
  }

  /**
   * 取消编辑个人信息
   */
  const cancelEdit = () => {
    // 恢复原始数据
    Object.assign(form, originalForm)
    isEdit.value = false
  }

  /**
   * 处理密码修改提交
   */
  const handlePwdSubmit = async () => {
    if (!isEditPwd.value) {
      isEditPwd.value = true
      return
    }

    // 验证表单
    const valid = await pwdFormRef.value?.validate().catch(() => false)
    if (!valid) return

    pwdLoading.value = true
    try {
      await fetchChangePassword({
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword,
        confirmPassword: pwdForm.confirmPassword
      })

      ElMessage.success('密码修改成功')
      isEditPwd.value = false

      // 清空密码表单
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
    } catch (error) {
      console.error('修改密码失败:', error)
    } finally {
      pwdLoading.value = false
    }
  }

  /**
   * 取消编辑密码
   */
  const cancelPwdEdit = () => {
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    isEditPwd.value = false
  }

  onMounted(() => {
    loadUserProfile()
    loadGenderDict()
  })
</script>
