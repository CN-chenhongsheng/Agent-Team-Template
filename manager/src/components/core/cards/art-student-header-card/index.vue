<!-- 学生头部卡片组件 -->
<template>
  <div class="student-header-card">
    <!-- 主信息区 -->
    <div class="main-info">
      <!-- 头像 -->
      <div class="avatar-wrapper">
        <div class="avatar-ring">
          <div class="avatar-box">
            <ArtSvgIcon icon="ri:user-line" class="avatar-icon" />
          </div>
        </div>
        <span
          v-if="student?.status !== undefined"
          class="status-badge"
          :class="student.status === 1 ? 'is-active' : 'is-inactive'"
        >
          {{ student.status === 1 ? '在校' : '离校' }}
        </span>
      </div>

      <!-- 核心信息 -->
      <div class="core-info">
        <div class="name-line">
          <h3 class="student-name">{{ student?.studentName || '-' }}</h3>
          <span
            v-if="student?.genderText"
            class="gender-badge"
            :class="student.gender === 1 ? 'is-male' : 'is-female'"
          >
            <ArtSvgIcon
              :icon="student.gender === 1 ? 'ri:men-line' : 'ri:women-line'"
              class="gender-icon"
            />
          </span>
        </div>
        <div class="student-no">
          <ArtSvgIcon icon="ri:hashtag" class="no-icon" />
          <span class="student-no-text">{{ student?.studentNo || '-' }}</span>
        </div>
      </div>
    </div>

    <!-- 扩展信息 -->
    <div class="extra-info">
      <div v-if="student?.campusName" class="info-chip">
        <ArtSvgIcon icon="ri:map-pin-line" class="chip-icon" />
        <span>{{ student.campusName }}</span>
      </div>
      <div v-if="student?.deptName" class="info-chip">
        <ArtSvgIcon icon="ri:building-line" class="chip-icon" />
        <span>{{ student.deptName }}</span>
      </div>
      <div v-if="student?.majorName" class="info-chip">
        <ArtSvgIcon icon="ri:book-open-line" class="chip-icon" />
        <span>{{ student.majorName }}</span>
      </div>
      <div v-if="student?.className" class="info-chip">
        <ArtSvgIcon icon="ri:group-line" class="chip-icon" />
        <span>{{ student.className }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

  defineOptions({ name: 'ArtStudentHeaderCard' })

  type StudentDetail = Api.AccommodationManage.StudentDetail

  interface Props {
    /** 学生数据 */
    student?: Partial<StudentDetail>
  }

  withDefaults(defineProps<Props>(), {
    student: undefined
  })
</script>

<style lang="scss" scoped>
  // 顶部用户卡片
  .student-header-card {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding: 20px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: calc(var(--custom-radius) / 2 + 2px) !important;
    transition:
      border-color 0.2s ease,
      box-shadow 0.2s ease;

    &:hover {
      background: linear-gradient(
        0deg,
        #fff0 0%,
        color-mix(in srgb, var(--el-color-primary) 3%, var(--el-bg-color)) 100%
      );
      border-color: var(--el-color-primary-light-7);
      box-shadow: 0 2px 5px rgb(0 0 0 / 4%);
    }

    // 主信息区
    .main-info {
      display: flex;
      gap: 16px;
      align-items: center;
    }

    // 头像
    .avatar-wrapper {
      position: relative;
      flex-shrink: 0;

      .avatar-ring {
        padding: 3px;
        background: linear-gradient(
          135deg,
          var(--el-color-primary-light-5),
          var(--el-color-primary-light-8)
        );
        border-radius: 50%;
      }

      .avatar-box {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 64px;
        height: 64px;
        background: var(--el-bg-color);
        border-radius: 50%;
      }

      .status-badge {
        position: absolute;
        right: 50%;
        bottom: -6px;
        padding: 2px 8px;
        font-size: 11px;
        font-weight: 500;
        white-space: nowrap;
        border-radius: 10px;
        transform: translateX(50%);

        &.is-active {
          color: var(--el-color-success);
          background: var(--el-color-success-light-9);
        }

        &.is-inactive {
          color: var(--el-text-color-secondary);
          background: var(--el-fill-color);
        }
      }
    }

    // 核心信息
    .core-info {
      flex: 1;
      min-width: 0;

      .name-line {
        display: flex;
        gap: 8px;
        align-items: center;
        margin-bottom: 3px;
      }

      .student-name {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        line-height: 1.3;
        color: var(--el-text-color-primary);
      }

      .gender-badge {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 22px;
        height: 22px;
        border-radius: 50%;

        &.is-male {
          color: var(--el-color-primary);
          background: var(--el-color-primary-light-9);
        }

        &.is-female {
          color: var(--el-color-danger);
          background: var(--el-color-danger-light-9);
        }

        .gender-icon {
          width: 14px;
          height: 14px;
        }
      }

      .student-no {
        display: inline-flex;
        align-items: center;
        padding-right: 4px;
        padding-left: 4px;
        font-family: 'SF Mono', Menlo, monospace;
        font-size: 13px;
        font-weight: 500;
        color: var(--el-text-color-regular);
        background: var(--el-color-primary-light-9);
        border-top-left-radius: var(--el-border-radius-base);

        .no-icon {
          width: 12px;
          height: 12px;
          padding-right: 2px;
          color: var(--el-text-color-placeholder);
        }

        .student-no-text {
          display: inline-block;
          padding-left: 2px;
          font-size: 13px;
          font-weight: 500;
          background: var(--el-color-primary-light-9);
          border-top-left-radius: var(--el-border-radius-base);
        }
      }
    }

    // 扩展信息
    .extra-info {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      padding-top: 12px;
      border-top: 1px dashed var(--el-border-color-lighter);

      .info-chip {
        display: inline-flex;
        gap: 6px;
        align-items: center;
        padding: 6px 12px;
        font-size: 13px;
        color: var(--el-text-color-regular);
        background: var(--el-bg-color);
        border: 1px solid var(--el-border-color-lighter);
        border-radius: 16px;
        transition: all 0.15s ease;

        &:hover {
          background: var(--el-color-primary-light-9);
          border-color: var(--el-color-primary-light-7);
        }

        .chip-icon {
          width: 14px;
          height: 14px;
          color: var(--el-color-primary);
        }
      }
    }
  }

  // 图标大小样式
  .avatar-icon {
    width: 32px;
    height: 32px;
    font-size: 32px;
    color: var(--el-color-primary-light-3);
  }
</style>
