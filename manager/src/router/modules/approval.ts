import { AppRouteRecord } from '@/types/router'

export const approvalRoutes: AppRouteRecord = {
  path: '/approval',
  name: 'Approval',
  component: '/index/index',
  meta: {
    title: 'menus.approval.title',
    icon: 'ri:flow-chart',
    roles: ['R_SUPER', 'R_ADMIN']
  },
  children: [
    {
      path: 'flow-config',
      name: 'FlowConfig',
      component: '/approval/flow-config/index',
      meta: {
        title: 'menus.approval.flowConfig',
        keepAlive: true,
        roles: ['R_SUPER']
      }
    },
    {
      path: 'pending',
      name: 'ApprovalPending',
      component: '/approval/pending/index',
      meta: {
        title: 'menus.approval.pending',
        keepAlive: true,
        roles: ['R_SUPER', 'R_ADMIN']
      }
    },
    {
      path: 'history',
      name: 'ApprovalHistory',
      component: '/approval/history/index',
      meta: {
        title: 'menus.approval.history',
        keepAlive: true,
        roles: ['R_SUPER', 'R_ADMIN']
      }
    }
  ]
}
