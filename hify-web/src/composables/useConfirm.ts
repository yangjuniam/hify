import { ElMessageBox, ElMessage, type ElMessageBoxOptions } from 'element-plus'
import type { ApiMethod } from '@/types'

/**
 * 删除确认 composable
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
      } as ElMessageBoxOptions)

      await api(params)
      ElMessage.success(successMessage)
      return true
    } catch (error) {
      return false
    }
  }
}
