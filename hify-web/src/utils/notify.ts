import { ElMessage, type MessageParams } from 'element-plus'

/**
 * 统一通知配置
 */
const defaultConfig: MessageParams = {
  duration: 3000,
  showClose: true,
}

/**
 * 成功提示
 *
 * @param message 提示内容
 * @param config 额外配置
 *
 * @example
 * ```ts
 * import { notifySuccess } from '@/utils/notify'
 *
 * notifySuccess('操作成功')
 * notifySuccess('保存成功', { duration: 5000 })
 * ```
 */
export function notifySuccess(message: string, config?: Partial<MessageParams>): void {
  ElMessage.success({
    ...defaultConfig,
    message,
    ...config,
  })
}

/**
 * 错误提示
 *
 * @param message 提示内容
 * @param config 额外配置
 *
 * @example
 * ```ts
 * import { notifyError } from '@/utils/notify'
 *
 * notifyError('操作失败')
 * notifyError('保存失败', { duration: 5000 })
 * ```
 */
export function notifyError(message: string, config?: Partial<MessageParams>): void {
  ElMessage.error({
    ...defaultConfig,
    message,
    ...config,
  })
}

/**
 * 警告提示
 *
 * @param message 提示内容
 * @param config 额外配置
 *
 * @example
 * ```ts
 * import { notifyWarning } from '@/utils/notify'
 *
 * notifyWarning('请检查输入')
 * notifyWarning('即将过期', { duration: 5000 })
 * ```
 */
export function notifyWarning(message: string, config?: Partial<MessageParams>): void {
  ElMessage.warning({
    ...defaultConfig,
    message,
    ...config,
  })
}
