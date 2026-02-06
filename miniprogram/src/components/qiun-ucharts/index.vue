<template>
  <view class="qiun-charts" :style="{ width: containerWidth, height: containerHeight }">
    <canvas
      :id="canvasId"
      :canvas-id="canvasId"
      :type="canvasType as any"
      class="qiun-charts-canvas"
      :style="{ width: canvasWidth, height: canvasHeight }"
    />
  </view>
</template>

<script setup lang="ts">
import { getCurrentInstance, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
// @ts-expect-error - @qiun/ucharts 缺少类型声明
import uCharts from '@qiun/ucharts/u-charts.js';

const props = withDefaults(defineProps<Props>(), {
  canvasType: '2d',
  opts: () => ({}),
  chartData: () => ({}),
});

interface Props {
  type: 'line' | 'area' | 'column' | 'bar' | 'pie' | 'ring' | 'radar' | 'gauge' | 'candle' | 'mix';
  opts?: any;
  chartData?: any;
  canvasId: string;
  canvasType?: '2d' | 'webgl';
}

let chartInstance: any = null;
let ctx: any = null;

// 保存组件实例，用于 uni.createSelectorQuery().in()
const instance = getCurrentInstance();

const containerWidth = ref('100%');
const containerHeight = ref('100%');
const canvasWidth = ref('750');
const canvasHeight = ref('500');

// 初始化图�?
function initChart() {
  if (!props.chartData || !props.chartData.categories || !props.chartData.series) {
    return;
  }

  nextTick(() => {
    try {
      // #ifdef MP-WEIXIN
      // 在 Vue 3 组合式 API 中，需要指定组件实例作用域
      // 注意：instance 必须在 setup 顶层获取，不能在异步回调中调用 getCurrentInstance()
      const query = instance ? uni.createSelectorQuery().in(instance) : uni.createSelectorQuery();
      // 对于 type="2d" 的 canvas，需要使用 .node() 方法获取 canvas 节点
      query.select(`#${props.canvasId}`).node((canvasRes: any) => {
        // 再次查询获取尺寸（需要在同一个组件实例作用域内）
        const sizeQuery = instance ? uni.createSelectorQuery().in(instance) : uni.createSelectorQuery();
        sizeQuery.select(`#${props.canvasId}`).boundingClientRect((sizeRes: any) => {
          if (canvasRes && canvasRes.node) {
            const canvas = canvasRes.node;
            const dpr = uni.getSystemInfoSync().pixelRatio || 2;
            ctx = canvas.getContext('2d', {});
            // 从尺寸查询结果获取宽高
            const width = sizeRes?.width || 375;
            let height = sizeRes?.height || 250;

            // 如果高度为 0，尝试从容器计算
            if (height === 0 || height < 10) {
              if (containerHeight.value.includes('rpx')) {
                height = Number.parseInt(containerHeight.value) / 2;
              }
              else {
                height = Number.parseInt(containerHeight.value) || 250;
              }
            }

            // 如果高度仍然无效，延迟重新获取
            if (height === 0 || height < 10) {
              // 延迟获取父容器实际高度
              setTimeout(() => {
                // 使用保存的实例，而不是在异步回调中调用 getCurrentInstance()
                const parentQuery = instance ? uni.createSelectorQuery().in(instance) : uni.createSelectorQuery();
                parentQuery.select(`#${props.canvasId}`).boundingClientRect((rect: any) => {
                  if (rect && rect.height > 0) {
                    const actualHeight = rect.height;
                    canvas.width = (width || 375) * dpr;
                    canvas.height = actualHeight * dpr;
                    ctx.scale(dpr, dpr);

                    const config = {
                      type: props.type,
                      context: ctx,
                      width: width || 375,
                      height: actualHeight,
                      categories: props.chartData.categories || [],
                      series: props.chartData.series || [],
                      ...props.opts,
                    };

                    if (chartInstance) {
                      chartInstance = null;
                    }
                    chartInstance = new (uCharts as any)(config);
                    if (chartInstance && typeof chartInstance.update === 'function') {
                      chartInstance.update();
                    }
                  }
                }).exec(() => {});
              }, 200);
              return;
            }

            // 确保 canvas 有有效的尺寸
            canvas.width = (width || 375) * dpr;
            canvas.height = (height || 250) * dpr;
            ctx.scale(dpr, dpr);

            const config = {
              type: props.type,
              context: ctx,
              width,
              height,
              categories: props.chartData.categories || [],
              series: props.chartData.series || [],
              ...props.opts,
            };

            if (chartInstance) {
              chartInstance = null;
            }
            chartInstance = new (uCharts as any)(config);

            // 确保图表立即渲染
            if (chartInstance && typeof chartInstance.update === 'function') {
              chartInstance.update();
            }
          }
        }).exec(() => {});
      }).exec(() => {});
      // #endif

      // #ifdef H5
      // H5 中增加延迟，确保容器尺寸已经渲染完成
      setTimeout(() => {
        // 在 H5 中，先尝试使用 uni.createSelectorQuery 获取 canvas（与 MP-WEIXIN 保持一致）
        const query = uni.createSelectorQuery();
        query.select(`#${props.canvasId}`)
          .fields({ node: true, size: true }, () => {})
          .exec((res: any) => {
            if (res && res[0] !== null && res[0] !== undefined && res[0].node) {
              // 使用 query 返回的 canvas node
              const canvas = res[0].node;
              // 如果 height 为 0，使用容器高度或默认值
              let width = res[0].width || 375;
              let height = res[0].height || 250;

              // 如果高度为 0，尝试从容器获取或使用默认值
              if (height === 0 || height < 10) {
                // 如果 containerHeight 是 100%，尝试从父容器获取实际高度
                if (containerHeight.value === '100%') {
                  try {
                    // 优先使用 canvas 的父容器获取尺寸
                    // canvas 的父容器是 .qiun-charts，其父容器是 .chart-container
                    const canvas = document.getElementById(props.canvasId) as HTMLElement;
                    if (canvas) {
                      // 先查找 .qiun-charts 容器
                      const container = canvas.parentElement;
                      if (container && container.classList.contains('qiun-charts')) {
                        // 如果 .qiun-charts 的高度为 0，尝试查找其父容器 .chart-container
                        const qiunRect = container.getBoundingClientRect();
                        if (qiunRect.height > 0) {
                          height = qiunRect.height;
                        }
                        else {
                          // 尝试从 .chart-container 获取高度
                          const chartContainer = container.parentElement;
                          if (chartContainer) {
                            const chartRect = chartContainer.getBoundingClientRect();
                            if (chartRect.height > 0) {
                              height = chartRect.height;
                            }
                            else {
                              // 如果父容器高度也为 0，尝试从 canvas 自身获取
                              const canvasRect = canvas.getBoundingClientRect();
                              if (canvasRect.height > 0) {
                                height = canvasRect.height;
                              }
                              else {
                                height = 250; // 默认值
                              }
                            }
                          }
                          else {
                            height = 250; // 默认值
                          }
                        }
                      }
                      else {
                        // canvas 的父容器不是 .qiun-charts，尝试直接从 canvas 获取
                        const canvasRect = canvas.getBoundingClientRect();
                        if (canvasRect.height > 0) {
                          height = canvasRect.height;
                        }
                        else {
                          height = 250; // 默认值
                        }
                      }
                    }
                    else {
                      // 回退到查找 .qiun-charts
                      const fallbackContainer = document.querySelector('.qiun-charts') as HTMLElement;
                      if (fallbackContainer) {
                        const rect = fallbackContainer.getBoundingClientRect();
                        if (rect.height > 0) {
                          height = rect.height;
                        }
                        else {
                          height = 250; // 默认值
                        }
                      }
                      else {
                        height = 250; // 默认值
                      }
                    }
                  }
                  catch (error) {
                    height = 250; // 默认值
                  }
                }
                else if (containerHeight.value.includes('rpx')) {
                  height = Number.parseInt(containerHeight.value) / 2;
                }
                else {
                  height = Number.parseInt(containerHeight.value) || 250;
                }
                // 如果宽度也为 0，也使用默认值
                if (width === 0 || width < 10) {
                  if (containerWidth.value.includes('rpx')) {
                    width = Number.parseInt(containerWidth.value) / 2;
                  }
                  else if (containerWidth.value === '100%') {
                    // 100% 宽度，尝试从窗口获取
                    const systemInfo = uni.getSystemInfoSync();
                    width = systemInfo.windowWidth || 375;
                  }
                  else {
                    width = Number.parseInt(containerWidth.value) || 375;
                  }
                }
              }
              try {
                ctx = canvas.getContext('2d', {});
              }
              catch (error: any) {
                console.error('获取 canvas context 失败:', error);
                return;
              }

              if (ctx) {
                // 确保 canvas 有有效的尺寸
                // 在 H5 中，canvas 的实际像素尺寸需要考虑设备像素比（DPR）
                // 获取 canvas 元素的 CSS 显示尺寸
                const canvasElement = canvas as HTMLElement;
                const computedStyle = window.getComputedStyle(canvasElement);
                const cssWidth = Number.parseFloat(computedStyle.width) || width || 375;
                const cssHeight = Number.parseFloat(computedStyle.height) || height || 250;

                // 获取设备像素比（保留变量以供注释说明，但当前策略不使用 DPR 缩放）
                // const _dpr = window.devicePixelRatio || 1;

                // 在移动设备上，为了确保图表完整显示，我们需要考虑 DPR
                // 但是 uCharts 可能不能正确处理 DPR 缩放，所以我们需要调整策略
                // 方案：不缩放 context，直接使用 CSS 尺寸作为 canvas 的实际像素尺寸
                // 这样可以避免缩放导致的坐标系统问题
                // 使用 Math.round 确保 canvas 尺寸是整数，避免小数导致的渲染问题
                canvas.width = Math.round(cssWidth);
                canvas.height = Math.round(cssHeight);

                // 不缩放 context，直接使用 CSS 尺寸
                // ctx.scale(dpr, dpr); // 注释掉，避免坐标系统问题

                // uCharts 会自动处理 padding，所以直接传递完整的 CSS 尺寸
                // 使用 Math.round 确保传递给 uCharts 的尺寸是整数，避免小数导致的渲染问题
                // 在移动设备上，为了确保图表不被裁剪，我们稍微减少传递给 uCharts 的尺寸（减去 4px 作为安全边距）
                const safeMargin = 4; // 安全边距，避免图表被裁剪
                const configWidth = Math.max(1, Math.round(cssWidth) - safeMargin * 2);
                const configHeight = Math.max(1, Math.round(cssHeight));
                const config = {
                  type: props.type,
                  context: ctx,
                  width: configWidth, // uCharts 使用逻辑尺寸（CSS 尺寸），会自动减去 padding
                  height: configHeight,
                  categories: props.chartData.categories || [],
                  series: props.chartData.series || [],
                  ...props.opts,
                };
                if (chartInstance) {
                  chartInstance = null;
                }
                chartInstance = new (uCharts as any)(config);

                // 确保图表立即渲染
                if (chartInstance) {
                  // uCharts 可能在构造函数中已经渲染，或者需要调用 updateData 而不是 update
                  if (typeof chartInstance.update === 'function') {
                    chartInstance.update();
                  }
                  else if (typeof chartInstance.updateData === 'function') {
                    chartInstance.updateData({
                      categories: props.chartData.categories || [],
                      series: props.chartData.series || [],
                    });
                  }
                }
              }
            }
            else {
              const canvas = document.getElementById(props.canvasId) as HTMLCanvasElement;
              if (canvas && canvas instanceof HTMLCanvasElement && typeof canvas.getContext === 'function') {
                try {
                  ctx = canvas.getContext('2d');
                }
                catch (error: any) {
                  console.error('获取 canvas context 失败 (fallback):', error);
                  return;
                }

                if (ctx) {
                  const rect = canvas.getBoundingClientRect();
                  const cssWidth = rect.width || 375;
                  const cssHeight = rect.height || 250;

                  // 获取设备像素比（保留变量以供注释说明，但当前策略不使用 DPR 缩放）
                  // const _dpr = window.devicePixelRatio || 1;

                  // 在移动设备上，为了确保图表完整显示，我们需要考虑 DPR
                  // 但是 uCharts 可能不能正确处理 DPR 缩放，所以我们需要调整策略
                  // 方案：不缩放 context，直接使用 CSS 尺寸作为 canvas 的实际像素尺寸
                  // 这样可以避免缩放导致的坐标系统问题
                  // 使用 Math.round 确保 canvas 尺寸是整数，避免小数导致的渲染问题
                  const canvasWidthInt = Math.round(cssWidth);
                  const canvasHeightInt = Math.round(cssHeight);
                  canvas.width = canvasWidthInt;
                  canvas.height = canvasHeightInt;

                  // 不缩放 context，直接使用 CSS 尺寸
                  // ctx.scale(dpr, dpr); // 注释掉，避免坐标系统问题

                  // uCharts 会自动处理 padding，所以直接传递完整的 CSS 尺寸
                  // 使用 Math.round 确保传递给 uCharts 的尺寸是整数，避免小数导致的渲染问题
                  // 在移动设备上，为了确保图表不被裁剪，我们稍微减少传递给 uCharts 的尺寸（减去 4px 作为安全边距）
                  const safeMargin = 4; // 安全边距，避免图表被裁剪
                  const configWidth = Math.max(1, Math.round(cssWidth) - safeMargin * 2);
                  const configHeight = Math.max(1, Math.round(cssHeight));
                  const config = {
                    type: props.type,
                    context: ctx,
                    width: configWidth, // uCharts 使用逻辑尺寸（CSS 尺寸），会自动减去 padding
                    height: configHeight,
                    categories: props.chartData.categories || [],
                    series: props.chartData.series || [],
                    ...props.opts,
                  };

                  if (chartInstance) {
                    chartInstance = null;
                  }
                  chartInstance = new (uCharts as any)(config);

                  // 确保图表立即渲染
                  if (chartInstance) {
                    // uCharts 可能在构造函数中已经渲染，或者需要调用 updateData 而不是 update
                    if (typeof chartInstance.update === 'function') {
                      chartInstance.update();
                    }
                    else if (typeof chartInstance.updateData === 'function') {
                      chartInstance.updateData({
                        categories: props.chartData.categories || [],
                        series: props.chartData.series || [],
                      });
                    }
                  }
                }
              }
            }
          });
      }, 500);
      // #endif
    }
    catch (error: any) {
      console.error('图表初始化失�?', error);
    }
  });
}

// 更新图表
function updateChart() {
  if (chartInstance && props.chartData) {
    try {
      chartInstance.updateData({
        categories: props.chartData.categories || [],
        series: props.chartData.series || [],
      });
    }
    catch (error) {
      console.error('图表更新失败:', error);
    }
  }
}

// 销毁图�?
function destroyChart() {
  if (chartInstance) {
    try {
      chartInstance = null;
    }
    catch (error) {
      console.error('图表销毁失�?', error);
    }
  }
  ctx = null;
}

// 监听数据变化
watch(
  () => [props.chartData, props.opts],
  () => {
    if (chartInstance) {
      updateChart();
    }
    else {
      initChart();
    }
  },
  { deep: true },
);

onMounted(() => {
  // #ifdef MP-WEIXIN
  // 微信小程序中 canvas 渲染需要更多时间，延迟更久
  setTimeout(() => {
    initChart();
  }, 500);
  // #endif
  // #ifdef H5
  // H5 中增加延迟，确保 DOM 和容器尺寸都已渲染完成
  setTimeout(() => {
    initChart();
  }, 800);
  // #endif
});

onUnmounted(() => {
  destroyChart();
});
</script>

<style scoped lang="scss">
.qiun-charts {
  position: relative;
  overflow: visible;
  width: 100%;
  height: 100%;
}

.qiun-charts-canvas {
  display: block;
  width: 100%;
  height: 100%;
}
</style>
