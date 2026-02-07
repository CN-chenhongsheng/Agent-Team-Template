<!-- 入住申请审批信息组件 -->
<template>
  <ArtApprovalInfo
    :business-type="businessType"
    :business-id="businessId"
    @approval-success="handleApprovalSuccess"
  >
    <!-- 申请信息卡片 -->
    <ElCard v-if="checkInData" class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:file-text-line" class="header-icon" />
          <span class="header-title">申请信息</span>
        </div>
      </template>
      <div class="info-list">
        <div v-if="checkInData.checkInTypeText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:home-line" class="label-icon" />
            <span>入住类型</span>
          </div>
          <ElTooltip
            :content="checkInData.checkInTypeText"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(checkInData.checkInTypeText)"
          >
            <div class="row-value">
              <ElTag :type="checkInTypeTag" size="small">
                {{ checkInData.checkInTypeText }}
              </ElTag>
            </div>
          </ElTooltip>
        </div>
        <div v-if="checkInData.applyDate" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:calendar-line" class="label-icon" />
            <span>申请日期</span>
          </div>
          <ElTooltip
            :content="checkInData.applyDate"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(checkInData.applyDate)"
          >
            <div class="row-value">{{ checkInData.applyDate }}</div>
          </ElTooltip>
        </div>
        <div v-if="checkInData.checkInDate" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:calendar-check-line" class="label-icon" />
            <span>入住日期</span>
          </div>
          <ElTooltip
            :content="checkInData.checkInDate"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(checkInData.checkInDate)"
          >
            <div class="row-value">{{ checkInData.checkInDate }}</div>
          </ElTooltip>
        </div>
        <div v-if="checkInData.expectedCheckOutDate" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:calendar-close-line" class="label-icon" />
            <span>预计退宿日期</span>
          </div>
          <ElTooltip
            :content="checkInData.expectedCheckOutDate"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(checkInData.expectedCheckOutDate)"
          >
            <div class="row-value">{{ checkInData.expectedCheckOutDate }}</div>
          </ElTooltip>
        </div>
        <div v-if="checkInData.applyReason" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:file-edit-line" class="label-icon" />
            <span>申请原因</span>
          </div>
          <ElTooltip
            :content="checkInData.applyReason"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(checkInData.applyReason)"
          >
            <div class="row-value">{{ checkInData.applyReason }}</div>
          </ElTooltip>
        </div>
        <div v-if="checkInData.remark" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:sticky-note-line" class="label-icon" />
            <span>备注</span>
          </div>
          <ElTooltip
            :content="checkInData.remark"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(checkInData.remark)"
          >
            <div class="row-value">{{ checkInData.remark }}</div>
          </ElTooltip>
        </div>
      </div>
    </ElCard>
  </ArtApprovalInfo>
</template>

<script setup lang="ts">
  import { ref, watch, computed } from 'vue'
  import { ElCard, ElTooltip } from 'element-plus'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import ArtApprovalInfo from '@/components/core/layouts/art-approval-info/index.vue'
  import { fetchGetCheckInDetail } from '@/api/accommodation-manage'

  defineOptions({ name: 'CheckInApprovalInfo' })

  interface Props {
    /** 业务类型 */
    businessType: 'check_in'
    /** 业务ID（入住申请ID） */
    businessId: number | null
    /** 入住申请数据（可选，用于显示申请信息） */
    checkInData?: Api.AccommodationManage.CheckInListItem | null
  }

  interface Emits {
    (e: 'approval-success'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const checkInData = ref<Api.AccommodationManage.CheckInListItem | null>(props.checkInData || null)

  // 判断文字是否可能溢出
  const isTextOverflow = (text: string | undefined): boolean => {
    if (!text) return false
    return text.length > 30
  }

  // 计算标签类型，避免 ESLint 误报过滤器
  const checkInTypeTag = computed<'primary' | 'warning'>(() => {
    return checkInData.value?.checkInType === 1 ? 'primary' : 'warning'
  })

  // 加载申请信息
  const loadCheckInData = async () => {
    if (!props.businessId) {
      checkInData.value = null
      return
    }

    // 如果没有传入 checkInData，则通过 businessId 获取
    if (!props.checkInData && props.businessId) {
      try {
        checkInData.value = await fetchGetCheckInDetail(props.businessId)
      } catch (error) {
        console.warn('加载申请信息失败:', error)
        checkInData.value = null
      }
    } else {
      checkInData.value = props.checkInData || null
    }
  }

  // 监听 checkInData prop 变化
  watch(
    () => props.checkInData,
    (newVal) => {
      if (newVal) {
        checkInData.value = newVal
      }
    },
    { immediate: true }
  )

  // 监听业务ID变化
  watch(
    () => props.businessId,
    () => {
      loadCheckInData()
    },
    { immediate: true }
  )

  // 处理审批成功
  const handleApprovalSuccess = () => {
    emit('approval-success')
  }
</script>
