import { get, post, put, del } from '@/utils/request'
import type { PageResult } from '@/types'

export interface Provider {
  id: number
  name: string
  type: string
  baseUrl: string
  authConfig?: Record<string, unknown>
  description?: string
  enabled: number
  createdAt: string
  updatedAt?: string
}

export interface ProviderDetail extends Provider {
  modelConfigs?: ModelConfig[]
  health?: ProviderHealth
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

export interface ProviderListParams {
  page: number
  pageSize: number
  type?: string
  enabled?: number
}

export interface CreateProviderRequest {
  name: string
  type: string
  baseUrl: string
  authConfig?: Record<string, unknown>
  description?: string
  enabled?: number
}

export interface UpdateProviderRequest {
  id: number
  name: string
  type: string
  baseUrl: string
  authConfig?: Record<string, unknown>
  description?: string
  enabled?: number
}

export interface ConnectionTestResult {
  success: boolean
  latencyMs?: number
  modelCount?: number
  errorMessage?: string
}

export const getProviderList = async (params: ProviderListParams): Promise<PageResult<ProviderDetail>> => {
  const result = await get<PageResult<Provider>>('/v1/providers', params)
  const list = await Promise.all(
    result.list.map(async (provider) => {
      try {
        return await getProviderDetail(provider.id)
      } catch (error) {
        return provider
      }
    })
  )
  return {
    ...result,
    list,
  }
}

export const getProviderDetail = (id: number): Promise<ProviderDetail> => {
  return get<ProviderDetail>(`/v1/providers/${id}`)
}

export const createProvider = (data: CreateProviderRequest): Promise<Provider> => {
  return post<Provider>('/v1/providers', data)
}

export const updateProvider = (data: UpdateProviderRequest): Promise<Provider> => {
  const { id, ...body } = data
  return put<Provider>(`/v1/providers/${id}`, body)
}

export const deleteProvider = (id: number): Promise<void> => {
  return del<void>(`/v1/providers/${id}`)
}

export const testConnection = (id: number): Promise<ConnectionTestResult> => {
  return post<ConnectionTestResult>(`/v1/providers/${id}/test-connection`)
}

export const testProviderConnection = testConnection
