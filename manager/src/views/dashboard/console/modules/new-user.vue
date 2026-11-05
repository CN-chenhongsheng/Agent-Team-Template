<template>
  <div class="art-card p-5 h-128 overflow-hidden mb-5 max-sm:mb-4">
    <div class="art-card-header">
      <div class="title">
        <h4>{{ $t('dashboard.newUser.title') }}</h4>
        <p> {{ $t('dashboard.newUser.growth') }}<span class="text-success">+20%</span> </p>
      </div>
      <ElRadioGroup v-model="radio2">
        <ElRadioButton value="thisMonth" :label="$t('dashboard.newUser.thisMonth')" />
        <ElRadioButton value="lastMonth" :label="$t('dashboard.newUser.lastMonth')" />
        <ElRadioButton value="thisYear" :label="$t('dashboard.newUser.thisYear')" />
      </ElRadioGroup>
    </div>
    <ArtTable
      class="w-full"
      :data="tableData"
      style="width: 100%"
      size="large"
      :border="false"
      :stripe="false"
      :header-cell-style="{ background: 'transparent' }"
    >
      <template #default>
        <ElTableColumn :label="$t('dashboard.newUser.avatar')" prop="avatar" width="150px">
          <template #default="scope">
            <div style="display: flex; align-items: center">
              <img class="size-9 rounded-lg" :src="scope.row.avatar" alt="avatar" />
              <span class="ml-2">{{ scope.row.username }}</span>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn :label="$t('dashboard.newUser.region')" prop="provinceIndex">
          <template #default="scope">
            {{ $t(`dashboard.newUser.provinces.${scope.row.provinceIndex ?? 0}`) }}
          </template>
        </ElTableColumn>
        <ElTableColumn :label="$t('dashboard.newUser.gender')" prop="avatar">
          <template #default="scope">
            <div style="display: flex; align-items: center">
              <span style="margin-left: 10px">{{
                scope.row.sex === 1 ? $t('dashboard.newUser.male') : $t('dashboard.newUser.female')
              }}</span>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn :label="$t('dashboard.newUser.progress')" width="240">
          <template #default="scope">
            <ElProgress
              :percentage="scope.row.pro"
              :color="scope.row.color"
              :stroke-width="4"
              :aria-label="
                $t('dashboard.newUser.completion', {
                  name: scope.row.username,
                  percent: scope.row.pro
                })
              "
            />
          </template>
        </ElTableColumn>
      </template>
    </ArtTable>
  </div>
</template>

<script setup lang="ts">
  import avatar1 from '@/assets/images/avatar/avatar1.webp'
  import avatar2 from '@/assets/images/avatar/avatar2.webp'
  import avatar3 from '@/assets/images/avatar/avatar3.webp'
  import avatar4 from '@/assets/images/avatar/avatar4.webp'
  import avatar5 from '@/assets/images/avatar/avatar5.webp'
  import avatar6 from '@/assets/images/avatar/avatar6.webp'

  interface UserTableItem {
    username: string
    provinceIndex: number
    sex: 0 | 1
    age: number
    percentage: number
    pro: number
    color: string
    avatar: string
  }

  const ANIMATION_DELAY = 100

  const radio2 = ref('thisMonth')

  /**
   * 新用户表格数据
   * 包含用户基本信息和完成进度
   */
  const tableData = reactive<UserTableItem[]>([
    {
      username: '中小鱼',
      provinceIndex: 0,
      sex: 0,
      age: 22,
      percentage: 60,
      pro: 0,
      color: 'var(--art-primary)',
      avatar: avatar1
    },
    {
      username: '何小荷',
      provinceIndex: 1,
      sex: 1,
      age: 21,
      percentage: 20,
      pro: 0,
      color: 'var(--art-secondary)',
      avatar: avatar2
    },
    {
      username: '誶誶淰',
      provinceIndex: 2,
      sex: 1,
      age: 23,
      percentage: 60,
      pro: 0,
      color: 'var(--art-warning)',
      avatar: avatar3
    },
    {
      username: '发呆草',
      provinceIndex: 3,
      sex: 0,
      age: 28,
      percentage: 50,
      pro: 0,
      color: 'var(--art-info)',
      avatar: avatar4
    },
    {
      username: '甜筒',
      provinceIndex: 4,
      sex: 1,
      age: 26,
      percentage: 70,
      pro: 0,
      color: 'var(--art-error)',
      avatar: avatar5
    },
    {
      username: '冷月呆呆',
      provinceIndex: 5,
      sex: 1,
      age: 25,
      percentage: 90,
      pro: 0,
      color: 'var(--art-success)',
      avatar: avatar6
    }
  ])

  /**
   * 添加进度条动画效果
   * 延迟后将进度值从 0 更新到目标百分比，触发动画
   */
  const addAnimation = (): void => {
    setTimeout(() => {
      tableData.forEach((item) => {
        item.pro = item.percentage
      })
    }, ANIMATION_DELAY)
  }

  onMounted(() => {
    addAnimation()
  })
</script>

<style lang="scss" scoped>
  .art-card {
    :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
      color: var(--el-color-primary) !important;
      background: transparent !important;
    }
  }
</style>
