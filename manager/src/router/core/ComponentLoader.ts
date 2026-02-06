/**
 * 组件加载器
 *
 * 负责动态加载 Vue 组件
 *
 * @module router/core/ComponentLoader
 * @author 陈鸿昇
 */

import { h } from 'vue'

export class ComponentLoader {
  private modules: Record<string, () => Promise<any>>

  constructor() {
    // 使用相对路径 glob，Vite 返回的 key 为相对本文件的路径；别名 @ 在 glob key 中会被解析成其它格式导致匹配失败
    this.modules = import.meta.glob('../../views/**/*.vue')
  }

  /**
   * 规范化后端返回的 component 路径：去掉首尾斜杠、去掉 /src/views 前缀
   * 后端可能返回 "/student/manage" 或 "/src/views/student/manage" 或 "/index/index"
   */
  private normalizeComponentPath(componentPath: string): string {
    let s = componentPath.trim().replace(/^\/+/, '').replace(/\/+$/, '')
    const prefix = 'src/views/'
    if (s.toLowerCase().startsWith(prefix)) {
      s = s.slice(prefix.length)
    } else if (s.startsWith('/src/views/')) {
      s = s.replace(/^\/src\/views\/?/i, '')
    }
    return s
  }

  /**
   * 加载组件
   */
  load(componentPath: string): () => Promise<any> {
    if (!componentPath) {
      return this.createEmptyComponent()
    }

    const normalized = this.normalizeComponentPath(componentPath)
    if (!normalized) {
      return this.createEmptyComponent()
    }

    // 与 glob key 一致：相对路径 ../../views/xxx.vue 或 ../../views/xxx/index.vue
    const fullPath = `../../views/${normalized}.vue`
    const fullPathWithIndex = `../../views/${normalized}/index.vue`

    const module = this.modules[fullPath] || this.modules[fullPathWithIndex]

    if (!module) {
      console.error(
        `[ComponentLoader] 未找到组件: ${componentPath}，尝试过的路径: ${fullPath} 和 ${fullPathWithIndex}`
      )
      return this.createErrorComponent(componentPath)
    }

    return module
  }

  /**
   * 加载布局组件
   */
  loadLayout(): () => Promise<any> {
    return () => import('@/views/index/index.vue')
  }

  /**
   * 加载 iframe 组件
   */
  loadIframe(): () => Promise<any> {
    return () => import('@/views/outside/Iframe.vue')
  }

  /**
   * 创建空组件
   */
  private createEmptyComponent(): () => Promise<any> {
    return () =>
      Promise.resolve({
        render() {
          return h('div', {})
        }
      })
  }

  /**
   * 创建错误提示组件
   */
  private createErrorComponent(componentPath: string): () => Promise<any> {
    return () =>
      Promise.resolve({
        render() {
          return h('div', { class: 'route-error' }, `组件未找到: ${componentPath}`)
        }
      })
  }
}
