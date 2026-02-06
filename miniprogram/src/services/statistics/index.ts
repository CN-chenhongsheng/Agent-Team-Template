/**
 * 统计业务逻辑封装
 */

import { getStudentHomeStatisticsAPI } from '@/api';

export class StatisticsService {
  static getStudentHomeStatistics() {
    return getStudentHomeStatisticsAPI();
  }
}
