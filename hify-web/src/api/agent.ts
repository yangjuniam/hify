import { get, post, put, del } from '@/utils/request'
import type { PageResult } from '@/types'

export interface Agent {
  id: number
  name: string
  description?: string
  systemPrompt?: string
  modelConfigId: number
  temperature?: number
  maxTokens?: number
  maxContextTurns?: number
  enabled: number
  createdAt: string
  updatedAt?: string
}

export interface AgentListParams {
  page: number
  pageSize: number
}

export interface CreateAgentRequest {
  name: string
  description?: string
  systemPrompt?: string
  modelConfigId: number
  temperature?: number
  maxTokens?: number
  maxContextTurns?: number
  enabled?: number
}

export interface UpdateAgentRequest extends CreateAgentRequest {
  id: number
}

export const getAgentList = (params: AgentListParams): Promise<PageResult<Agent>> => {
  return get<PageResult<Agent>>('/v1/agents', params)
}

export const getAgentDetail = (id: number): Promise<Agent> => {
  return get<Agent>(`/v1/agents/${id}`)
}

export const createAgent = (data: CreateAgentRequest): Promise<Agent> => {
  return post<Agent>('/v1/agents', data)
}

export const updateAgent = (data: UpdateAgentRequest): Promise<Agent> => {
  const { id, ...body } = data
  return put<Agent>(`/v1/agents/${id}`, body)
}

export const deleteAgent = (id: number): Promise<void> => {
  return del<void>(`/v1/agents/${id}`)
}
