import { get, post, put, del } from '@/utils/request'
import type { PageResult } from '@/types'

/**
 * 提供商类型
 */
export interface Provider {
  id: number
  name: string
  type: string
  baseUrl: string
  authConfig?: Record<string, any>
  description?: string
  enabled: number
  createdAt: string
  updatedAt?: string
}

/**
 * 提供商详情（包含模型配置和健康状态）
 */
export interface ProviderDetail extends Provider {
  modelConfigs: ModelConfig[]
  health: ProviderHealth
}

export interface ModelConfig {
  id: number
  providerId: number
  name: string
  modelId: string
  contextSize?: number
  enabled: number
}

export interface ProviderHealth {
  id: number
  providerId: number
  status: string
  lastCheckAt?: string
  lastSuccessAt?: string
  failCount: number
  latencyMs?: number
  errorMessage?: string
}

/**
 * 创建提供商请求
 */
export interface CreateProviderRequest {
  name: string
  type: string
  baseUrl: string
  authConfig?: Record<string, any>
  description?: string
  enabled?: number
}

/**
 * 更新提供商请求
 */
export interface UpdateProviderRequest extends CreateProviderRequest {
  id: number
}

/**
 * 获取分页列表
 */
export const getProviderList = (params: {
  page: number
  pageSize: number
}): Promise<PageResult<Provider>> => {
  return get<PageResult<Provider>>('/v1/providers', params)
}

/**
 * 获取详情
 */
export const getProviderDetail = (id: number): Promise<ProviderDetail> => {
  return get<ProviderDetail>(`/v1/providers/${id}`)
}

/**
 * 创建
 */
export const createProvider = (data: CreateProviderRequest): Promise<Provider> => {
  return post<Provider>('/v1/providers', data)
}

/**
 * 更新
 */
export const updateProvider = (data: UpdateProviderRequest): Promise<Provider> => {
  return put<Provider>('/v1/providers', data)
}

/**
 * 删除
 */
export const deleteProvider = (id: number): Promise<void> => {
  return del<void>(`/v1/providers/${id}`)
}

/**
 * 测试连接
 */
export const testProviderConnection = (id: number): Promise<{
  success: boolean
  latencyMs?: number
  modelCount?: number
  errorMessage?: string
}> => {
  return post(`/v1/providers/${id}/test-connection`)
}
