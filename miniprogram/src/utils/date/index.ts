/**
 * 日期处理工具函数
 */

/**
 * 格式化日期为 YYYY-MM-DD 格式
 * @param date 日期对象或时间戳
 * @returns 格式化后的日期字符串
 */
export function formatDate(date: Date | number | string = new Date()): string {
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

/**
 * 格式化日期为 YYYY-MM-DD HH:mm:ss 格式
 * @param date 日期对象或时间戳
 * @returns 格式化后的日期时间字符串
 */
export function formatDateTime(date: Date | number | string = new Date()): string {
  const d = new Date(date);
  const dateStr = formatDate(d);
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  const seconds = String(d.getSeconds()).padStart(2, '0');
  return `${dateStr} ${hours}:${minutes}:${seconds}`;
}

/**
 * 从日期时间字符串中提取日期部分
 * @param dateTime 日期时间字符串
 * @returns 日期部分
 */
export function extractDate(dateTime: string): string {
  if (!dateTime) return '';
  if (dateTime.includes(' ')) {
    return dateTime.split(' ')[0];
  }
  return dateTime;
}

/**
 * 比较两个日期的先后
 * @param date1 日期1
 * @param date2 日期2
 * @returns date1 < date2 返回 -1，date1 > date2 返回 1，相等返回 0
 */
export function compareDates(date1: string | Date, date2: string | Date): number {
  const d1 = new Date(date1).getTime();
  const d2 = new Date(date2).getTime();
  if (d1 < d2) return -1;
  if (d1 > d2) return 1;
  return 0;
}

/**
 * 判断结束日期是否晚于开始日期
 * @param startDate 开始日期
 * @param endDate 结束日期
 * @returns 是否有效
 */
export function isValidDateRange(startDate: string, endDate: string): boolean {
  return compareDates(startDate, endDate) <= 0;
}
