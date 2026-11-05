/**
 * 权限码常量
 *
 * 所有按钮级权限标识集中在此维护，避免字符串散落在页面中。
 *
 * @module utils/constants/permissions
 */

export const PERMISSIONS = {
  system: {
    user: {
      add: 'system:user:add',
      view: 'system:user:view',
      edit: 'system:user:edit',
      delete: 'system:user:delete',
      import: 'system:user:import',
      resetPwd: 'system:user:reset-pwd',
      assignPermission: 'system:user:assign-permission'
    },
    role: {
      add: 'system:role:add',
      edit: 'system:role:edit',
      delete: 'system:role:delete',
      assign: 'system:role:assign'
    },
    menu: {
      add: 'system:menu:add',
      edit: 'system:menu:edit',
      delete: 'system:menu:delete'
    },
    dict: {
      type: {
        add: 'system:dict:type:add',
        edit: 'system:dict:type:edit',
        delete: 'system:dict:type:delete'
      },
      data: {
        add: 'system:dict:data:add',
        edit: 'system:dict:data:edit',
        delete: 'system:dict:data:delete'
      }
    },
    operLog: {
      detail: 'system:operlog:detail',
      delete: 'system:operlog:delete',
      clean: 'system:operlog:clean'
    },
    loginLog: {
      detail: 'system:loginlog:detail',
      delete: 'system:loginlog:delete',
      clean: 'system:loginlog:clean'
    }
  }
} as const
