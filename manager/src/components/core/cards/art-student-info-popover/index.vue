<!-- 学生信息悬浮卡片组件 -->
<template>
  <div class="student-info-popover-wrapper">
    <div ref="scrollEl" class="student-info-popover-content">
      <div class="info-list">
        <!-- 基本信息 -->
        <div
          v-if="hasBasicInfo"
          class="section-title"
          @click="
            collapsed.basic = !collapsed.basic
            nextTick(updateFade)
          "
        >
          <span>基本信息</span>
          <ArtSvgIcon
            icon="ri:arrow-down-s-line"
            class="section-toggle-icon"
            :class="{ 'is-collapsed': collapsed.basic }"
          />
        </div>
        <div v-if="hasBasicInfo" v-show="!collapsed.basic">
          <div v-if="student.studentNo" class="info-row">
            <div class="row-label">
              <ArtSvgIcon icon="ri:hashtag" class="label-icon" />
              <span>学号</span>
            </div>
            <div class="row-value">{{ student.studentNo }}</div>
          </div>
          <div v-if="student.studentName" class="info-row">
            <div class="row-label">
              <ArtSvgIcon icon="ri:user-line" class="label-icon" />
              <span>姓名</span>
            </div>
            <div class="row-value">{{ student.studentName }}</div>
          </div>
          <div v-if="student.genderText" class="info-row">
            <div class="row-label">
              <ArtSvgIcon
                :icon="student.gender === 1 ? 'ri-men-line' : 'ri-women-line'"
                class="label-icon"
                :class="student.gender === 1 ? 'text-primary' : 'text-pink-500'"
              />
              <span>性别</span>
            </div>
            <div class="row-value">{{ student.genderText }}</div>
          </div>
          <div v-if="student.phone" class="info-row">
            <div class="row-label">
              <ArtSvgIcon icon="ri:phone-line" class="label-icon" />
              <span>手机号</span>
            </div>
            <div class="row-value is-copyable">{{ student.phone }}</div>
          </div>
          <div v-if="student.nation" class="info-row">
            <div class="row-label">
              <ArtSvgIcon icon="ri:global-line" class="label-icon" />
              <span>民族</span>
            </div>
            <div class="row-value">{{ student.nation }}</div>
          </div>
          <div v-if="student.politicalStatus" class="info-row">
            <div class="row-label">
              <ArtSvgIcon icon="ri:flag-line" class="label-icon" />
              <span>政治面貌</span>
            </div>
            <div class="row-value">{{ student.politicalStatus }}</div>
          </div>
        </div>

        <!-- 学校信息 -->
        <div
          v-if="hasSchoolInfo"
          class="section-title"
          @click="
            collapsed.school = !collapsed.school
            nextTick(updateFade)
          "
        >
          <span>学校信息</span>
          <ArtSvgIcon
            icon="ri:arrow-down-s-line"
            class="section-toggle-icon"
            :class="{ 'is-collapsed': collapsed.school }"
          />
        </div>
        <div v-if="hasSchoolInfo" v-show="!collapsed.school">
          <div v-if="student.campusName" class="info-row">
            <div class="row-label">
              <ArtSvgIcon icon="ri:map-pin-line" class="label-icon" />
              <span>校区</span>
            </div>
            <div class="row-value">{{ student.campusName }}</div>
          </div>
          <div v-if="student.deptName" class="info-row">
            <div class="row-label">
              <ArtSvgIcon icon="ri:building-line" class="label-icon" />
              <span>院系</span>
            </div>
            <div class="row-value is-multiline">{{ student.deptName }}</div>
          </div>
          <div v-if="student.majorName" class="info-row">
            <div class="row-label">
              <ArtSvgIcon icon="ri:book-open-line" class="label-icon" />
              <span>专业</span>
            </div>
            <div class="row-value is-multiline">{{ student.majorName }}</div>
          </div>
          <div v-if="student.className" class="info-row">
            <div class="row-label">
              <ArtSvgIcon icon="ri:group-line" class="label-icon" />
              <span>班级</span>
            </div>
            <div class="row-value is-multiline">{{ student.className }}</div>
          </div>
        </div>

        <!-- 住宿信息 -->
        <div
          v-if="hasDormInfo"
          class="section-title"
          @click="
            collapsed.dorm = !collapsed.dorm
            nextTick(updateFade)
          "
        >
          <span>住宿信息</span>
          <ArtSvgIcon
            icon="ri:arrow-down-s-line"
            class="section-toggle-icon"
            :class="{ 'is-collapsed': collapsed.dorm }"
          />
        </div>
        <div v-if="hasDormInfo" v-show="!collapsed.dorm">
          <div v-if="student.floorName" class="info-row">
            <div class="row-label">
              <ArtSvgIcon icon="ri:building-2-line" class="label-icon" />
              <span>楼栋</span>
            </div>
            <div class="row-value">{{ student.floorName }}</div>
          </div>
          <div v-if="student.roomName" class="info-row">
            <div class="row-label">
              <ArtSvgIcon icon="ri:door-open-line" class="label-icon" />
              <span>房间</span>
            </div>
            <div class="row-value">{{ student.roomName }}</div>
          </div>
          <div v-if="student.bedName" class="info-row">
            <div class="row-label">
              <ArtSvgIcon icon="ri:hotel-bed-line" class="label-icon" />
              <span>床位</span>
            </div>
            <div class="row-value">{{ student.bedName }}</div>
          </div>
        </div>

        <!-- 学籍信息 -->
        <div
          v-if="hasAcademicInfo"
          class="section-title"
          @click="
            collapsed.academic = !collapsed.academic
            nextTick(updateFade)
          "
        >
          <span>学籍信息</span>
          <ArtSvgIcon
            icon="ri:arrow-down-s-line"
            class="section-toggle-icon"
            :class="{ 'is-collapsed': collapsed.academic }"
          />
        </div>
        <div v-if="hasAcademicInfo" v-show="!collapsed.academic">
          <div v-if="student.academicStatusText" class="info-row">
            <div class="row-label">
              <ArtSvgIcon icon="ri:file-list-line" class="label-icon" />
              <span>学籍状态</span>
            </div>
            <div class="row-value" :class="getAcademicStatusClass(student.academicStatus)">
              <span v-if="student.academicStatus" class="value-dot"></span>
              {{ student.academicStatusText }}
            </div>
          </div>
          <div v-if="student.enrollmentYear" class="info-row">
            <div class="row-label">
              <ArtSvgIcon icon="ri:calendar-check-line" class="label-icon" />
              <span>入学年份</span>
            </div>
            <div class="row-value">{{ student.enrollmentYear }}</div>
          </div>
          <div v-if="student.currentGrade" class="info-row">
            <div class="row-label">
              <ArtSvgIcon icon="ri:graduation-cap-line" class="label-icon" />
              <span>当前年级</span>
            </div>
            <div class="row-value">{{ student.currentGrade }}</div>
          </div>
        </div>
      </div>
    </div>
    <!-- 底部渐变遮罩，提示还有更多内容 -->
    <div v-show="showFade" class="scroll-fade-bottom" />
  </div>
</template>

<script setup lang="ts">
  import { computed, reactive, ref, onMounted, nextTick } from 'vue'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

  defineOptions({ name: 'StudentInfoPopover' })

  interface Props {
    /** 学生数据（支持 StudentListItem 或 StudentBasicInfo 类型） */
    student: Partial<Api.Common.StudentBasicInfo>
  }

  const props = withDefaults(defineProps<Props>(), {
    student: () => ({})
  })

  const collapsed = reactive({ basic: false, school: false, dorm: false, academic: false })

  const scrollEl = ref<HTMLElement | null>(null)
  const showFade = ref(false)

  const updateFade = () => {
    const el = scrollEl.value
    if (!el) return
    showFade.value =
      el.scrollHeight > el.clientHeight && el.scrollTop + el.clientHeight < el.scrollHeight - 2
  }

  onMounted(() => {
    updateFade()
    scrollEl.value?.addEventListener('scroll', updateFade)
  })

  const hasBasicInfo = computed(() => {
    const s = props.student || {}
    return Boolean(
      s.studentNo || s.studentName || s.genderText || s.phone || s.nation || s.politicalStatus
    )
  })

  const hasSchoolInfo = computed(() => {
    const s = props.student || {}
    return Boolean(s.campusName || s.deptName || s.majorName || s.className)
  })

  const hasDormInfo = computed(() => {
    const s = props.student || {}
    return Boolean(s.floorName || s.roomName || s.bedName)
  })

  const hasAcademicInfo = computed(() => {
    const s = props.student || {}
    return Boolean(s.academicStatusText || s.enrollmentYear || s.currentGrade)
  })

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
  .student-info-popover-wrapper {
    position: relative;
  }

  .student-info-popover-content {
    max-height: 500px;
    overflow-y: auto;

    /* 隐藏滚动条但保留滚动功能 */
    scrollbar-width: none; /* Firefox */
    -ms-overflow-style: none; /* IE/Edge */

    &::-webkit-scrollbar {
      display: none; /* Chrome/Safari/Opera */
    }

    .info-list {
      display: flex;
      flex-direction: column;
      gap: 2px;
      padding-bottom: 10px; /* 为底部渐变遮罩预留空间 */
    }

    .section-title {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 0 2px;
      margin: 6px 0 2px;
      font-size: 11px;
      font-weight: 600;
      color: var(--el-text-color-secondary);
      letter-spacing: 0.5px;
      cursor: var(--cursor-pointer);
      user-select: none;

      .section-toggle-icon {
        width: 14px;
        height: 14px;
        transition: transform 0.2s ease;

        &.is-collapsed {
          transform: rotate(-90deg);
        }
      }
    }

    .section-title + .info-row {
      margin-top: 2px;
    }

    .info-row {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      padding: 8px;
      border-bottom: 1px solid var(--el-border-color-extra-light);
      transition: background-color 0.15s ease;

      &:last-of-type {
        border-bottom: none;
      }

      &:hover {
        background-color: var(--el-fill-color-light);
        border-radius: calc(var(--el-border-radius-base) - 1px);
      }

      .row-label {
        display: flex;
        flex-shrink: 0;
        gap: 8px;
        align-items: center;
        font-size: 12px;
        color: var(--el-text-color-secondary);

        .label-icon {
          width: 14px;
          height: 14px;
          color: var(--el-text-color-placeholder);
        }
      }

      .row-value {
        display: flex;
        flex-shrink: 1;
        gap: 6px;
        align-items: center;
        max-width: 220px;
        padding-left: 12px;
        overflow: hidden;
        font-size: 12px;
        font-weight: 500;
        color: var(--el-text-color-primary);
        text-overflow: ellipsis;
        white-space: nowrap;
        cursor: var(--cursor-text);

        .value-dot {
          flex-shrink: 0;
          width: 4px;
          height: 4px;
          margin-right: 2px;
          background: var(--el-text-color-placeholder);
          border-radius: 50%;
        }

        &.is-good {
          color: var(--el-color-success);

          .value-dot {
            background: var(--el-color-success);
          }
        }

        &.is-warn {
          color: var(--el-color-warning-dark-2);

          .value-dot {
            background: var(--el-color-warning);
          }
        }

        &.is-danger {
          color: var(--el-color-danger);

          .value-dot {
            background: var(--el-color-danger);
          }
        }

        &.is-copyable {
          color: var(--el-color-primary);
          cursor: var(--cursor-pointer);

          &:hover {
            text-decoration: underline;
          }
        }

        &.is-multiline {
          word-break: break-all;
          white-space: normal;
        }
      }
    }
  }
</style>

<style lang="scss">
  // 全局样式，用于覆盖 ElPopover 的默认样式
  .student-info-popover {
    max-width: 320px !important;
    padding: 14px 16px !important;
    background: var(--el-bg-color-overlay) !important;
    border: 1px solid var(--el-border-color-light) !important;
    border-radius: 8px !important;
    box-shadow: var(--el-box-shadow-lighter) !important;

    .el-popover__title {
      display: none;
    }

    // 底部渐变遮罩
    .scroll-fade-bottom {
      position: absolute;
      right: 0;
      bottom: 0;
      left: 0;
      height: 40px;
      pointer-events: none;
      background: linear-gradient(
        to bottom,
        transparent 0%,
        rgb(255 255 255 / 60%) 50%,
        rgb(255 255 255 / 95%) 100%
      );
      border-radius: 0 0 8px 8px;
    }
  }

  // 暗色模式适配
  .dark .student-info-popover {
    .scroll-fade-bottom {
      background: linear-gradient(
        to bottom,
        transparent 0%,
        rgb(30 30 30 / 60%) 50%,
        rgb(30 30 30 / 95%) 100%
      );
    }
  }
</style>
