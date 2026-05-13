import { ref } from 'vue'
import type { ApiMethod } from '@/types'

/**
 * 请求状态管理 composable
 * 自动管理 loading、error 状态，避免每个页面写 try-catch-finally 样板代码
 *
 * @param api API 请求方法
 * @param immediate 是否立即执行，默认 false
 * @returns 请求状态对象
 *
 * @example
 * ```ts
 * const { data, loading, error, execute } = useRequest(getProviderList)
 *
 * // 手动触发请求
 * const fetchData = async () => {
 *   await execute({ id: 1 })
 * }
 *
 * // 立即执行
 * const { data, loading } = useRequest(getProviderList, true)
 * ```
 */
export function useRequest<T = any, P = any>(
  api: ApiMethod<T, P>,
  immediate: boolean = false
) {
  const data = ref<T | null>(null)
  const loading = ref(false)
  const error = ref<Error | null>(null)

  const execute = async (params: P): Promise<T | null> => {
    loading.value = true
    error.value = null

    try {
      const result = await api(params)
      data.value = result
      return result
    } catch (err) {
      error.value = err as Error
      return null
    } finally {
      loading.value = false
    }
  }

  if (immediate) {
    execute({} as P)
  }

  return {
    data,
    loading,
    error,
    execute,
  }
}
