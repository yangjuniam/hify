import { ElMessage } from 'element-plus'
import type { MessageParams } from 'element-plus'

/**
 * 统一通知配置
 */
const defaultConfig = {
  duration: 3000,
  showClose: true,
} as const

/**
 * 合并配置
 */
function mergeConfig(message: string, config?: Partial<MessageParams>): MessageParams {
  return {
    ...defaultConfig,
    message,
    ...(config || {}),
  } as MessageParams
}

/**
 * 成功提示
 */
export function notifySuccess(message: string, config?: Partial<MessageParams>): void {
  ElMessage.success(mergeConfig(message, config))
}

/**
 * 错误提示
 */
export function notifyError(message: string, config?: Partial<MessageParams>): void {
  ElMessage.error(mergeConfig(message, config))
}

/**
 * 警告提示
 */
export function notifyWarning(message: string, config?: Partial<MessageParams>): void {
  ElMessage.warning(mergeConfig(message, config))
}
