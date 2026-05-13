import { ElMessageBox, ElMessage, ElMessageBoxData } from 'element-plus'
import type { ApiMethod } from '@/types'

/**
 * 删除确认 composable
 * 一行代码完成 "确认删除 → 调接口 → 提示成功" 全流程
 *
 * @param message 确认文案
 * @param api 删除 API 方法
 * @param successMessage 成功提示文案，默认 "删除成功"
 * @returns Promise<boolean> 是否确认并执行成功
 *
 * @example
 * ```ts
 * const confirmDelete = useConfirm('确定要删除此项吗？', deleteProvider)
 *
 * const handleDelete = async (id: number) => {
 *   await confirmDelete(id)
 * }
 * ```
 */
export function useConfirm<T = any>(
  message: string,
  api: ApiMethod<any, T>,
  successMessage: string = '删除成功'
): (params: T) => Promise<boolean> {
  return async (params: T) => {
    try {
      await ElMessageBox.confirm(message, '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      } as ElMessageBoxData)

      await api(params)
      ElMessage.success(successMessage)
      return true
    } catch (error) {
      // 用户取消或调用失败
      return false
    }
  }
}
