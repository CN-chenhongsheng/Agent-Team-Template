<!-- 留宿申请审批信息组件 -->
<template>
  <ArtApprovalInfo
    :business-type="businessType"
    :business-id="businessId"
    @approval-success="handleApprovalSuccess"
  >
    <!-- 申请信息卡片 -->
    <ElCard v-if="stayData" class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:file-text-line" class="header-icon" />
          <span class="header-title">申请信息</span>
        </div>
      </template>
      <div class="info-list">
        <div v-if="stayData.applyDate" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:calendar-line" class="label-icon" />
            <span>申请日期</span>
          </div>
          <ElTooltip
            :content="stayData.applyDate"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(stayData.applyDate)"
          >
            <div class="row-value">{{ stayData.applyDate }}</div>
          </ElTooltip>
        </div>
        <div v-if="stayData.stayStartDate" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:calendar-check-line" class="label-icon" />
            <span>留宿开始日期</span>
          </div>
          <ElTooltip
            :content="stayData.stayStartDate"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(stayData.stayStartDate)"
          >
            <div class="row-value">{{ stayData.stayStartDate }}</div>
          </ElTooltip>
        </div>
        <div v-if="stayData.stayEndDate" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:calendar-close-line" class="label-icon" />
            <span>留宿结束日期</span>
          </div>
          <ElTooltip
            :content="stayData.stayEndDate"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(stayData.stayEndDate)"
          >
            <div class="row-value">{{ stayData.stayEndDate }}</div>
          </ElTooltip>
        </div>
        <div v-if="stayData.stayReason" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:file-edit-line" class="label-icon" />
            <span>留宿理由</span>
          </div>
          <ElTooltip
            :content="stayData.stayReason"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(stayData.stayReason)"
          >
            <div class="row-value">{{ stayData.stayReason }}</div>
          </ElTooltip>
        </div>
        <div v-if="stayData.remark" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:sticky-note-line" class="label-icon" />
            <span>备注</span>
          </div>
          <ElTooltip
            :content="stayData.remark"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(stayData.remark)"
          >
            <div class="row-value">{{ stayData.remark }}</div>
          </ElTooltip>
        </div>
      </div>
    </ElCard>

    <!-- 家长信息卡片 -->
    <ElCard v-if="stayData && hasParentInfo" class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:parent-line" class="header-icon" />
          <span class="header-title">家长信息</span>
        </div>
      </template>
      <div class="info-list">
        <div v-if="stayData.parentName" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:user-line" class="label-icon" />
            <span>家长姓名</span>
          </div>
          <ElTooltip
            :content="stayData.parentName"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(stayData.parentName)"
          >
            <div class="row-value">{{ stayData.parentName }}</div>
          </ElTooltip>
        </div>
        <div v-if="stayData.parentPhone" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:phone-line" class="label-icon" />
            <span>家长电话</span>
          </div>
          <ElTooltip
            :content="stayData.parentPhone"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(stayData.parentPhone)"
          >
            <div class="row-value is-copyable">{{ stayData.parentPhone }}</div>
          </ElTooltip>
        </div>
        <div v-if="stayData.parentAgreeText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:checkbox-circle-line" class="label-icon" />
            <span>家长是否同意</span>
          </div>
          <div
            class="row-value"
            :class="stayData.parentAgree === 'agree' ? 'is-good' : 'is-danger'"
          >
            <span class="value-dot"></span>
            {{ stayData.parentAgreeText }}
          </div>
        </div>
      </div>
    </ElCard>

    <!-- 附件与签名卡片 -->
    <ElCard v-if="stayData && (hasImages || hasSignature)" class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:attachment-2" class="header-icon" />
          <span class="header-title">附件与签名</span>
        </div>
      </template>
      <div class="attachment-content">
        <!-- 附件图片 -->
        <div v-if="hasImages" class="attachment-section">
          <div class="section-label">
            <ArtSvgIcon icon="ri:image-line" class="label-icon" />
            <span>附件图片</span>
          </div>
          <div class="image-list">
            <ElImage
              v-for="(img, index) in imageList"
              :key="index"
              :src="img"
              :preview-src-list="imageList"
              :initial-index="index"
              fit="cover"
              class="image-item"
            >
              <template #error>
                <div class="image-error">
                  <ArtSvgIcon icon="ri:image-line" />
                  <span>加载失败</span>
                </div>
              </template>
            </ElImage>
          </div>
        </div>
        <!-- 本人签名 -->
        <div v-if="hasSignature" class="attachment-section">
          <div class="section-label">
            <ArtSvgIcon icon="ri:quill-pen-line" class="label-icon" />
            <span>本人签名</span>
          </div>
          <div class="signature-wrapper">
            <ElImage
              v-if="signatureUrl"
              :src="signatureUrl"
              :preview-src-list="signatureList"
              fit="contain"
              class="signature-image"
            >
              <template #error>
                <div class="image-error">
                  <ArtSvgIcon icon="ri:quill-pen-line" />
                  <span>签名加载失败</span>
                </div>
              </template>
            </ElImage>
          </div>
        </div>
      </div>
    </ElCard>
  </ArtApprovalInfo>
</template>

<script setup lang="ts">
  import { ref, watch, computed } from 'vue'
  import { ElCard, ElTooltip, ElImage } from 'element-plus'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import ArtApprovalInfo from '@/components/core/layouts/art-approval-info/index.vue'
  import { fetchGetStayDetail } from '@/api/accommodation-manage'

  defineOptions({ name: 'StayApprovalInfo' })

  interface Props {
    /** 业务类型 */
    businessType: 'stay'
    /** 业务ID（留宿申请ID） */
    businessId: number | null
    /** 留宿申请数据（可选，用于显示申请信息） */
    stayData?: Api.AccommodationManage.StayListItem | null
  }

  interface Emits {
    (e: 'approval-success'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const stayData = ref<Api.AccommodationManage.StayListItem | null>(props.stayData || null)

  // 判断文字是否可能溢出
  const isTextOverflow = (text: string | undefined): boolean => {
    if (!text) return false
    return text.length > 30
  }

  // 是否有家长信息
  const hasParentInfo = computed(() => {
    return !!(
      stayData.value?.parentName ||
      stayData.value?.parentPhone ||
      stayData.value?.parentAgreeText
    )
  })

  // 解析附件图片列表
  const imageList = computed<string[]>(() => {
    if (!stayData.value?.images) return []
    try {
      const parsed = JSON.parse(stayData.value.images)
      return Array.isArray(parsed) ? parsed : []
    } catch {
      return []
    }
  })

  // 是否有附件图片
  const hasImages = computed(() => imageList.value.length > 0)

  // 解析签名列表（可能是 JSON 字符串数组，也可能是单个字符串）
  const signatureList = computed<string[]>(() => {
    if (!stayData.value?.signature) return []
    try {
      // 尝试解析为 JSON 数组
      const parsed = JSON.parse(stayData.value.signature)
      if (Array.isArray(parsed)) {
        return parsed.filter((item): item is string => typeof item === 'string' && item.length > 0)
      }
      // 如果不是数组，可能是单个字符串（旧数据格式）
      if (typeof parsed === 'string' && parsed.length > 0) {
        return [parsed]
      }
      return []
    } catch {
      // 解析失败，可能是单个字符串（旧数据格式）
      if (typeof stayData.value.signature === 'string' && stayData.value.signature.length > 0) {
        return [stayData.value.signature]
      }
      return []
    }
  })

  // 获取第一个签名 URL（用于显示）
  const signatureUrl = computed<string | undefined>(() => {
    return signatureList.value.length > 0 ? signatureList.value[0] : undefined
  })

  // 是否有签名
  const hasSignature = computed(() => signatureList.value.length > 0)

  // 加载申请信息
  const loadStayData = async () => {
    if (!props.businessId) {
      stayData.value = null
      return
    }

    // 如果没有传入 stayData，则通过 businessId 获取
    if (!props.stayData && props.businessId) {
      try {
        stayData.value = await fetchGetStayDetail(props.businessId)
      } catch (error) {
        console.warn('加载申请信息失败:', error)
        stayData.value = null
      }
    } else {
      stayData.value = props.stayData || null
    }
  }

  // 监听 stayData prop 变化
  watch(
    () => props.stayData,
    (newVal) => {
      if (newVal) {
        stayData.value = newVal
      }
    },
    { immediate: true }
  )

  // 监听业务ID变化
  watch(
    () => props.businessId,
    () => {
      loadStayData()
    },
    { immediate: true }
  )

  // 处理审批成功
  const handleApprovalSuccess = () => {
    emit('approval-success')
  }
</script>

<style scoped lang="scss">
  .attachment-content {
    padding: 12px 0;

    .attachment-section {
      margin-bottom: 20px;

      &:last-child {
        margin-bottom: 0;
      }

      .section-label {
        display: flex;
        gap: 6px;
        align-items: center;
        margin-bottom: 12px;
        font-size: 13px;
        font-weight: 500;
        color: var(--el-text-color-secondary);

        .label-icon {
          font-size: 16px;
          color: var(--el-color-primary);
        }
      }

      .image-list {
        display: flex;
        flex-wrap: wrap;
        gap: 12px;

        .image-item {
          width: 100px;
          height: 100px;
          overflow: hidden;
          cursor: pointer;
          border: 1px solid var(--el-border-color-lighter);
          border-radius: 6px;
          transition: all 0.3s ease;

          &:hover {
            border-color: var(--el-color-primary);
            box-shadow: 0 4px 12px 0 rgb(0 0 0 / 10%);
            transform: translateY(-2px);
          }

          .image-error {
            display: flex;
            flex-direction: column;
            gap: 4px;
            align-items: center;
            justify-content: center;
            width: 100%;
            height: 100%;
            font-size: 12px;
            color: var(--el-text-color-placeholder);
            background-color: var(--el-fill-color-lighter);

            svg {
              font-size: 24px;
            }
          }
        }
      }

      .signature-wrapper {
        .signature-image {
          width: 100%;
          max-height: 150px;
          overflow: hidden;
          cursor: pointer;
          border: 1px solid var(--el-border-color-lighter);
          border-radius: 6px;
          transition: all 0.3s ease;

          &:hover {
            border-color: var(--el-color-primary);
            box-shadow: 0 4px 12px 0 rgb(0 0 0 / 10%);
          }

          .image-error {
            display: flex;
            flex-direction: column;
            gap: 4px;
            align-items: center;
            justify-content: center;
            width: 100%;
            height: 100px;
            font-size: 12px;
            color: var(--el-text-color-placeholder);
            background-color: var(--el-fill-color-lighter);

            svg {
              font-size: 32px;
            }
          }
        }
      }
    }
  }
</style>
