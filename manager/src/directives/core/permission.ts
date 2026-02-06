/**
 * v-permission 权限指令
 *
 * 基于用户权限标识控制 DOM 元素的显示和隐藏。
 * 用户需要拥有指定的权限标识才能看到元素，否则元素将从 DOM 中移除。
 *
 * ## 主要功能
 *
 * - 权限验证 - 检查用户是否拥有指定权限标识
 * - 多权限支持 - 支持单个权限或多个权限（满足其一即可）
 * - DOM 控制 - 无权限时自动移除元素，而非隐藏
 * - 响应式更新 - 权限变化时自动更新元素状态
 *
 * ## 使用示例
 *
 * ```vue
 * <template>
 *   <!-- 单个权限 - 只有拥有新增用户权限的用户可见 -->
 *   <el-button v-permission="'system:user:add'">新增</el-button>
 *
 *   <!-- 多个权限 - 拥有编辑或删除权限的用户可见 -->
 *   <el-button v-permission="['system:user:edit', 'system:user:delete']">操作</el-button>
 * </template>
 * ```
 *
 * ## 权限逻辑
 *
 * - 用户权限从 userStore.getUserInfo.permissions 获取
 * - 只要用户拥有指定权限中的任意一个，元素就会显示
 * - 如果用户没有任何权限或不满足条件，元素将被移除
 *
 * @module directives/permission
 * @author 陈鸿昇
 */

import { useUserStore } from '@/store/modules/user'
import { App, Directive, DirectiveBinding } from 'vue'

interface PermissionBinding extends DirectiveBinding {
  value: string | string[]
}

function checkPermission(el: HTMLElement, binding: PermissionBinding): void {
  const userStore = useUserStore()
  const userPermissions = userStore.getUserInfo.permissions

  // 如果用户权限为空或未定义，移除元素
  if (!userPermissions?.length) {
    removeElement(el)
    return
  }

  // 确保指令值为数组格式
  const requiredPermissions = Array.isArray(binding.value) ? binding.value : [binding.value]

  // 检查用户是否具有所需权限之一
  const hasPermission = requiredPermissions.some((permission: string) =>
    userPermissions.includes(permission)
  )

  // 如果没有权限，安全地移除元素
  if (!hasPermission) {
    removeElement(el)
  }
}

function removeElement(el: HTMLElement): void {
  if (el.parentNode) {
    el.parentNode.removeChild(el)
  }
}

const permissionDirective: Directive = {
  mounted: checkPermission,
  updated: checkPermission
}

export function setupPermissionDirective(app: App): void {
  app.directive('permission', permissionDirective)
}

/**
 * 检查当前用户是否拥有指定权限
 * @param permission 权限标识或权限标识数组
 * @returns 是否拥有权限
 */
export function hasPermission(permission: string | string[]): boolean {
  const userStore = useUserStore()
  const userPermissions = userStore.getUserInfo.permissions

  if (!userPermissions?.length) {
    return false
  }

  const requiredPermissions = Array.isArray(permission) ? permission : [permission]
  return requiredPermissions.some((p) => userPermissions.includes(p))
}
