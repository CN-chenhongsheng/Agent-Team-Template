<!-- 退宿申请审批信息组件 -->
<template>
  <ArtApprovalInfo
    :business-type="businessType"
    :business-id="businessId"
    @approval-success="handleApprovalSuccess"
  >
    <!-- 申请信息卡片 -->
    <ElCard v-if="checkOutData" class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:file-text-line" class="header-icon" />
          <span class="header-title">申请信息</span>
        </div>
      </template>
      <div class="info-list">
        <div v-if="checkOutData.studentInfo?.campusName" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:map-pin-line" class="label-icon" />
            <span>校区</span>
          </div>
          <ElTooltip
            :content="checkOutData.studentInfo?.campusName"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(checkOutData.studentInfo?.campusName)"
          >
            <div class="row-value">{{ checkOutData.studentInfo?.campusName }}</div>
          </ElTooltip>
        </div>
        <div v-if="checkOutData.studentInfo?.floorName" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:building-2-line" class="label-icon" />
            <span>楼层</span>
          </div>
          <ElTooltip
            :content="checkOutData.studentInfo?.floorName"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(checkOutData.studentInfo?.floorName)"
          >
            <div class="row-value">{{ checkOutData.studentInfo?.floorName }}</div>
          </ElTooltip>
        </div>
        <div v-if="checkOutData.studentInfo?.roomName" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:door-open-line" class="label-icon" />
            <span>房间</span>
          </div>
          <ElTooltip
            :content="checkOutData.studentInfo?.roomName"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(checkOutData.studentInfo?.roomName)"
          >
            <div class="row-value">{{ checkOutData.studentInfo?.roomName }}</div>
          </ElTooltip>
        </div>
        <div v-if="checkOutData.studentInfo?.bedName" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:hotel-bed-line" class="label-icon" />
            <span>床位</span>
          </div>
          <ElTooltip
            :content="checkOutData.studentInfo?.bedName"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(checkOutData.studentInfo?.bedName)"
          >
            <div class="row-value">{{ checkOutData.studentInfo?.bedName }}</div>
          </ElTooltip>
        </div>
        <div v-if="checkOutData.applyDate" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:calendar-line" class="label-icon" />
            <span>申请日期</span>
          </div>
          <ElTooltip
            :content="checkOutData.applyDate"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(checkOutData.applyDate)"
          >
            <div class="row-value">{{ checkOutData.applyDate }}</div>
          </ElTooltip>
        </div>
        <div v-if="checkOutData.checkOutDate" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:calendar-check-line" class="label-icon" />
            <span>退宿日期</span>
          </div>
          <ElTooltip
            :content="checkOutData.checkOutDate"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(checkOutData.checkOutDate)"
          >
            <div class="row-value">{{ checkOutData.checkOutDate }}</div>
          </ElTooltip>
        </div>
        <div v-if="checkOutData.checkOutReason" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:file-edit-line" class="label-icon" />
            <span>退宿理由</span>
          </div>
          <ElTooltip
            :content="checkOutData.checkOutReason"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(checkOutData.checkOutReason)"
          >
            <div class="row-value">{{ checkOutData.checkOutReason }}</div>
          </ElTooltip>
        </div>
        <div v-if="checkOutData.remark" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:sticky-note-line" class="label-icon" />
            <span>备注</span>
          </div>
          <ElTooltip
            :content="checkOutData.remark"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(checkOutData.remark)"
          >
            <div class="row-value">{{ checkOutData.remark }}</div>
          </ElTooltip>
        </div>
      </div>
    </ElCard>
  </ArtApprovalInfo>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue'
  import { ElCard, ElTooltip } from 'element-plus'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import ArtApprovalInfo from '@/components/core/layouts/art-approval-info/index.vue'
  import { fetchGetCheckOutDetail } from '@/api/accommodation-manage'

  defineOptions({ name: 'CheckOutApprovalInfo' })

  interface Props {
    /** 业务类型 */
    businessType: 'check_out'
    /** 业务ID（退宿申请ID） */
    businessId: number | null
    /** 退宿申请数据（可选，用于显示申请信息） */
    checkOutData?: Api.AccommodationManage.CheckOutListItem | null
  }

  interface Emits {
    (e: 'approval-success'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const checkOutData = ref<Api.AccommodationManage.CheckOutListItem | null>(
    props.checkOutData || null
  )

  // 判断文字是否可能溢出
  const isTextOverflow = (text: string | undefined): boolean => {
    if (!text) return false
    return text.length > 30
  }

  // 加载申请信息
  const loadCheckOutData = async () => {
    if (!props.businessId) {
      checkOutData.value = null
      return
    }

    // 如果没有传入 checkOutData，则通过 businessId 获取
    if (!props.checkOutData && props.businessId) {
      try {
        checkOutData.value = await fetchGetCheckOutDetail(props.businessId)
      } catch (error) {
        console.warn('加载申请信息失败:', error)
        checkOutData.value = null
      }
    } else {
      checkOutData.value = props.checkOutData || null
    }
  }

  // 监听 checkOutData prop 变化
  watch(
    () => props.checkOutData,
    (newVal) => {
      if (newVal) {
        checkOutData.value = newVal
      }
    },
    { immediate: true }
  )

  // 监听业务ID变化
  watch(
    () => props.businessId,
    () => {
      loadCheckOutData()
    },
    { immediate: true }
  )

  // 处理审批成功
  const handleApprovalSuccess = () => {
    emit('approval-success')
  }
</script>
