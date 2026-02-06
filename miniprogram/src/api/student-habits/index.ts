/**
 * 学生生活习惯相关API
 */
import { get, put } from '@/utils/request';
import type { IStudentHabitsForm } from '@/types/api/student-habits';

/**
 * 获取学生生活习惯信息
 */
export function getStudentHabits() {
  return get<IStudentHabitsForm>({
    url: '/api/v1/student/habits',
  });
}

/**
 * 更新学生生活习惯信息
 * @param data 学生生活习惯表单数据
 */
export function updateStudentHabits(data: IStudentHabitsForm) {
  return put<{ success: boolean }>({
    url: '/api/v1/student/habits',
    data,
  });
}
