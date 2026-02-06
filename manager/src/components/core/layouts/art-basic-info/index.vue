<!-- 学生基本信息模块 -->
<template>
  <div class="student-basic-info">
    <!-- 联系信息卡片 -->
    <ElCard class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:contacts-line" class="header-icon" />
          <span class="header-title">联系信息</span>
        </div>
      </template>
      <div class="info-list">
        <div v-if="data.phone" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:phone-line" class="label-icon" />
            <span>手机号</span>
          </div>
          <ElTooltip
            :content="data.phone"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(data.phone)"
          >
            <div class="row-value is-copyable">{{ data.phone }}</div>
          </ElTooltip>
        </div>
        <div v-if="data.email" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:mail-line" class="label-icon" />
            <span>邮箱</span>
          </div>
          <ElTooltip
            :content="data.email"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(data.email)"
          >
            <div class="row-value is-copyable">{{ data.email }}</div>
          </ElTooltip>
        </div>
        <div v-if="data.idCard" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:bank-card-line" class="label-icon" />
            <span>身份证号</span>
          </div>
          <ElTooltip
            :content="data.idCard"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(data.idCard)"
          >
            <div class="row-value is-mono">{{ data.idCard }}</div>
          </ElTooltip>
        </div>
        <div v-if="!data.phone && !data.email && !data.idCard" class="empty-state">
          <span class="empty-text">暂无联系信息</span>
        </div>
      </div>
    </ElCard>

    <!-- 身份信息卡片 -->
    <ElCard class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:user-settings-line" class="header-icon" />
          <span class="header-title">身份信息</span>
        </div>
      </template>
      <div class="info-list">
        <div v-if="data.birthDate" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:cake-2-line" class="label-icon" />
            <span>出生日期</span>
          </div>
          <ElTooltip
            :content="data.birthDate"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(data.birthDate)"
          >
            <div class="row-value">{{ data.birthDate }}</div>
          </ElTooltip>
        </div>
        <div v-if="data.nation" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:global-line" class="label-icon" />
            <span>民族</span>
          </div>
          <ElTooltip
            :content="data.nation"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(data.nation)"
          >
            <div class="row-value">{{ data.nation }}</div>
          </ElTooltip>
        </div>
        <div v-if="data.politicalStatus" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:flag-line" class="label-icon" />
            <span>政治面貌</span>
          </div>
          <ElTooltip
            :content="data.politicalStatus"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(data.politicalStatus)"
          >
            <div class="row-value">{{ data.politicalStatus }}</div>
          </ElTooltip>
        </div>
        <div v-if="!data.birthDate && !data.nation && !data.politicalStatus" class="empty-state">
          <span class="empty-text">暂无身份信息</span>
        </div>
      </div>
    </ElCard>

    <!-- 学校信息卡片 -->
    <ElCard class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:school-line" class="header-icon" />
          <span class="header-title">学校信息</span>
        </div>
      </template>
      <div class="info-list">
        <div v-if="data.enrollmentYear" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:calendar-check-line" class="label-icon" />
            <span>入学年份</span>
          </div>
          <ElTooltip
            :content="String(data.enrollmentYear)"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(data.enrollmentYear)"
          >
            <div class="row-value">{{ data.enrollmentYear }}</div>
          </ElTooltip>
        </div>
        <div v-if="data.schoolingLength" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:time-line" class="label-icon" />
            <span>学制</span>
          </div>
          <ElTooltip
            :content="`${data.schoolingLength}年`"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(data.schoolingLength)"
          >
            <div class="row-value">{{ data.schoolingLength }}年</div>
          </ElTooltip>
        </div>
        <div v-if="data.currentGrade" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:graduation-cap-line" class="label-icon" />
            <span>当前年级</span>
          </div>
          <ElTooltip
            :content="data.currentGrade"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(data.currentGrade)"
          >
            <div class="row-value">{{ data.currentGrade }}</div>
          </ElTooltip>
        </div>
        <div v-if="data.academicStatusText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:file-list-line" class="label-icon" />
            <span>学籍状态</span>
          </div>
          <ElTooltip
            :content="data.academicStatusText"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(data.academicStatusText)"
          >
            <div class="row-value" :class="getAcademicStatusClass(data.academicStatus)">
              <span class="value-dot"></span>
              {{ data.academicStatusText }}
            </div>
          </ElTooltip>
        </div>
        <div
          v-if="
            !data.campusName &&
            !data.deptName &&
            !data.majorName &&
            !data.className &&
            !data.enrollmentYear &&
            !data.schoolingLength &&
            !data.currentGrade &&
            !data.academicStatusText
          "
          class="empty-state"
        >
          <span class="empty-text">暂无学校信息</span>
        </div>
      </div>
    </ElCard>

    <!-- 住宿信息卡片 -->
    <ElCard class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:building-line" class="header-icon" />
          <span class="header-title">住宿信息</span>
        </div>
      </template>
      <div class="info-list">
        <div v-if="data.floorName" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:building-2-line" class="label-icon" />
            <span>楼层</span>
          </div>
          <ElTooltip
            :content="data.floorName"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(data.floorName)"
          >
            <div class="row-value">{{ data.floorName }}</div>
          </ElTooltip>
        </div>
        <div v-if="data.roomName" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:door-open-line" class="label-icon" />
            <span>房间</span>
          </div>
          <ElTooltip
            :content="data.roomName"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(data.roomName)"
          >
            <div class="row-value">{{ data.roomName }}</div>
          </ElTooltip>
        </div>
        <div v-if="data.bedName" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:hotel-bed-line" class="label-icon" />
            <span>床位</span>
          </div>
          <ElTooltip
            :content="data.bedName"
            placement="bottom-end"
            popper-class="info-card-tooltip"
            :disabled="!isTextOverflow(data.bedName)"
          >
            <div class="row-value">{{ data.bedName }}</div>
          </ElTooltip>
        </div>
        <div v-if="!data.floorName && !data.roomName && !data.bedName" class="empty-state">
          <span class="empty-text">暂无住宿信息</span>
        </div>
      </div>
    </ElCard>

    <!-- 备注信息 -->
    <ElCard v-if="data.remark" class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:file-text-line" class="header-icon" />
          <span class="header-title">备注</span>
        </div>
      </template>
      <div class="remark-content">
        <p class="remark-text">{{ data.remark }}</p>
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ElCard, ElTooltip } from 'element-plus'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

  defineOptions({ name: 'StudentBasicInfo' })

  interface Props {
    /** 学生数据 */
    data: Partial<Api.AccommodationManage.StudentDetail>
  }

  withDefaults(defineProps<Props>(), {
    data: () => ({})
  })

  // 判断文字是否可能溢出
  const isTextOverflow = (text: string | undefined | number): boolean => {
    if (!text) return false
    const str = String(text)
    return str.length > 20
  }

  // 获取学籍状态样式类
  const getAcademicStatusClass = (status?: number): string => {
    if (status === 1) return 'is-good' // 在读
    if (status === 2) return 'is-warn' // 休学
    if (status === 3) return '' // 毕业
    if (status === 4) return 'is-danger' // 退学
    return ''
  }
</script>

<style lang="scss" scoped>
  .student-basic-info {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
</style>
