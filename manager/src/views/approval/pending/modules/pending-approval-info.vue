<!-- 待办审批信息组件 -->
<template>
  <ArtApprovalInfo
    :business-type="businessType"
    :business-id="businessId"
    @approval-success="handleApprovalSuccess"
  >
    <!-- 申请信息卡片 -->
    <ElCard v-if="businessData" class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:file-text-line" class="header-icon" />
          <span class="header-title">申请信息</span>
        </div>
      </template>
      <div class="info-list">
        <!-- 业务类型 -->
        <div class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:file-list-line" class="label-icon" />
            <span>业务类型</span>
          </div>
          <ElTooltip
            :content="businessTypeText"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(businessTypeText)"
          >
            <div class="row-value">
              <ElTag type="info" size="small">{{ businessTypeText }}</ElTag>
            </div>
          </ElTooltip>
        </div>
        <!-- 入住类型（入住申请特有） -->
        <div v-if="businessType === 'check_in' && businessData.checkInTypeText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:home-line" class="label-icon" />
            <span>入住类型</span>
          </div>
          <ElTooltip
            :content="businessData.checkInTypeText"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(businessData.checkInTypeText)"
          >
            <div class="row-value">
              <ElTag :type="businessData.checkInType === 1 ? 'primary' : 'warning'" size="small">
                {{ businessData.checkInTypeText }}
              </ElTag>
            </div>
          </ElTooltip>
        </div>
        <!-- 调宿类型（调宿申请特有） -->
        <div v-if="businessType === 'transfer' && businessData.transferTypeText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:swap-line" class="label-icon" />
            <span>调宿类型</span>
          </div>
          <ElTooltip
            :content="businessData.transferTypeText"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(businessData.transferTypeText)"
          >
            <div class="row-value">
              <ElTag type="primary" size="small">{{ businessData.transferTypeText }}</ElTag>
            </div>
          </ElTooltip>
        </div>
        <!-- 留宿类型（留宿申请特有） -->
        <div v-if="businessType === 'stay' && businessData.stayTypeText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:moon-line" class="label-icon" />
            <span>留宿类型</span>
          </div>
          <ElTooltip
            :content="businessData.stayTypeText"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(businessData.stayTypeText)"
          >
            <div class="row-value">
              <ElTag type="primary" size="small">{{ businessData.stayTypeText }}</ElTag>
            </div>
          </ElTooltip>
        </div>
        <!-- 校区 -->
        <div v-if="businessData.studentInfo?.campusName" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:map-pin-line" class="label-icon" />
            <span>校区</span>
          </div>
          <ElTooltip
            :content="businessData.studentInfo?.campusName"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(businessData.studentInfo?.campusName)"
          >
            <div class="row-value">{{ businessData.studentInfo?.campusName }}</div>
          </ElTooltip>
        </div>
        <!-- 楼层 -->
        <div v-if="businessData.studentInfo?.floorName" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:building-2-line" class="label-icon" />
            <span>楼层</span>
          </div>
          <ElTooltip
            :content="businessData.studentInfo?.floorName"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(businessData.studentInfo?.floorName)"
          >
            <div class="row-value">{{ businessData.studentInfo?.floorName }}</div>
          </ElTooltip>
        </div>
        <!-- 房间 -->
        <div v-if="businessData.studentInfo?.roomName" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:door-open-line" class="label-icon" />
            <span>房间</span>
          </div>
          <ElTooltip
            :content="businessData.studentInfo?.roomName"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(businessData.studentInfo?.roomName)"
          >
            <div class="row-value">{{ businessData.studentInfo?.roomName }}</div>
          </ElTooltip>
        </div>
        <!-- 床位 -->
        <div v-if="businessData.studentInfo?.bedName" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:hotel-bed-line" class="label-icon" />
            <span>床位</span>
          </div>
          <ElTooltip
            :content="businessData.studentInfo?.bedName"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(businessData.studentInfo?.bedName)"
          >
            <div class="row-value">{{ businessData.studentInfo?.bedName }}</div>
          </ElTooltip>
        </div>
        <!-- 申请日期 -->
        <div v-if="businessData.applyDate" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:calendar-line" class="label-icon" />
            <span>申请日期</span>
          </div>
          <ElTooltip
            :content="businessData.applyDate"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(businessData.applyDate)"
          >
            <div class="row-value">{{ businessData.applyDate }}</div>
          </ElTooltip>
        </div>
        <!-- 入住日期（入住申请特有） -->
        <div v-if="businessType === 'check_in' && businessData.checkInDate" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:calendar-check-line" class="label-icon" />
            <span>入住日期</span>
          </div>
          <ElTooltip
            :content="businessData.checkInDate"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(businessData.checkInDate)"
          >
            <div class="row-value">{{ businessData.checkInDate }}</div>
          </ElTooltip>
        </div>
        <!-- 退宿日期（退宿申请特有） -->
        <div v-if="businessType === 'check_out' && businessData.checkOutDate" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:calendar-close-line" class="label-icon" />
            <span>退宿日期</span>
          </div>
          <ElTooltip
            :content="businessData.checkOutDate"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(businessData.checkOutDate)"
          >
            <div class="row-value">{{ businessData.checkOutDate }}</div>
          </ElTooltip>
        </div>
        <!-- 开始/结束日期（留宿申请特有） -->
        <div v-if="businessType === 'stay' && businessData.startDate" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:calendar-2-line" class="label-icon" />
            <span>留宿开始</span>
          </div>
          <ElTooltip
            :content="businessData.startDate"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(businessData.startDate)"
          >
            <div class="row-value">{{ businessData.startDate }}</div>
          </ElTooltip>
        </div>
        <div v-if="businessType === 'stay' && businessData.endDate" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:calendar-2-line" class="label-icon" />
            <span>留宿结束</span>
          </div>
          <ElTooltip
            :content="businessData.endDate"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(businessData.endDate)"
          >
            <div class="row-value">{{ businessData.endDate }}</div>
          </ElTooltip>
        </div>
        <!-- 申请原因 -->
        <div v-if="businessData.applyReason" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:file-edit-line" class="label-icon" />
            <span>申请原因</span>
          </div>
          <ElTooltip
            :content="businessData.applyReason"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(businessData.applyReason)"
          >
            <div class="row-value">{{ businessData.applyReason }}</div>
          </ElTooltip>
        </div>
        <!-- 备注 -->
        <div v-if="businessData.remark" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:sticky-note-line" class="label-icon" />
            <span>备注</span>
          </div>
          <ElTooltip
            :content="businessData.remark"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(businessData.remark)"
          >
            <div class="row-value">{{ businessData.remark }}</div>
          </ElTooltip>
        </div>
      </div>
    </ElCard>
  </ArtApprovalInfo>
</template>

<script setup lang="ts">
  import { ref, watch, computed } from 'vue'
  import { ElCard, ElTag, ElTooltip } from 'element-plus'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import ArtApprovalInfo from '@/components/core/layouts/art-approval-info/index.vue'
  import {
    fetchGetCheckInDetail,
    fetchGetTransferDetail,
    fetchGetCheckOutDetail,
    fetchGetStayDetail
  } from '@/api/accommodation-manage'

  defineOptions({ name: 'PendingApprovalInfo' })

  interface Props {
    /** 业务类型 */
    businessType: string
    /** 业务ID */
    businessId: number | null
  }

  interface Emits {
    (e: 'approval-success'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const businessData = ref<any>(null)

  // 判断文字是否可能溢出
  const isTextOverflow = (text: string | undefined): boolean => {
    if (!text) return false
    return text.length > 30
  }

  // 业务类型文本映射
  const businessTypeText = computed(() => {
    switch (props.businessType) {
      case 'check_in':
        return '入住申请'
      case 'transfer':
        return '调宿申请'
      case 'check_out':
        return '退宿申请'
      case 'stay':
        return '留宿申请'
      default:
        return props.businessType
    }
  })

  // 加载业务数据
  const loadBusinessData = async () => {
    if (!props.businessId || !props.businessType) {
      businessData.value = null
      return
    }

    try {
      switch (props.businessType) {
        case 'check_in':
          businessData.value = await fetchGetCheckInDetail(props.businessId)
          break
        case 'transfer':
          businessData.value = await fetchGetTransferDetail(props.businessId)
          break
        case 'check_out':
          businessData.value = await fetchGetCheckOutDetail(props.businessId)
          break
        case 'stay':
          businessData.value = await fetchGetStayDetail(props.businessId)
          break
        default:
          businessData.value = null
      }
    } catch (error) {
      console.warn('加载业务数据失败:', error)
      businessData.value = null
    }
  }

  // 监听属性变化
  watch(
    () => [props.businessType, props.businessId],
    () => {
      loadBusinessData()
    },
    { immediate: true }
  )

  // 处理审批成功
  const handleApprovalSuccess = () => {
    emit('approval-success')
  }
</script>
