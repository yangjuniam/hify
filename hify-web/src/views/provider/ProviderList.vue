<template>
  <div class="provider-page">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">模型提供商管理</h1>
        <p class="page-description">管理和配置 LLM 模型提供商，支持 OpenAI、Claude、Gemini、Ollama 等多种模型</p>
      </div>
      <div class="page-header-actions">
        <button class="btn-primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增提供商
        </button>
      </div>
    </div>

    <!-- 表格卡片 -->
    <div class="hify-card table-card">
      <HifyTable
        ref="tableRef"
        :columns="columns"
        :api="fetchProviderList"
        :row-style="rowStyle"
      >
        <!-- 类型列自定义渲染 -->
        <template #type="{ row }">
          <el-tag :type="typeTagType(row.type)">{{ typeMap[row.type] }}</el-tag>
        </template>

        <!-- 状态列自定义渲染 -->
        <template #status="{ row }">
          <el-tag :type="row.status === 'enabled' ? 'success' : 'info'">
            {{ row.status === 'enabled' ? '启用' : '禁用' }}
          </el-tag>
        </template>

        <!-- 操作列 -->
        <template #actions="{ row }">
          <span class="action-buttons">
            <el-button text class="btn-edit" @click="handleEdit(row)">编辑</el-button>
            <el-button text class="btn-delete" @click="handleDelete(row)">删除</el-button>
          </span>
        </template>
      </HifyTable>
    </div>

    <!-- 新增/编辑弹窗 -->
    <HifyFormDialog
      ref="dialogRef"
      v-model="dialogVisible"
      title="提供商配置"
      :width="520"
      :rules="formRules"
      :edit-data="editingData"
      label-position="right"
      :label-width="'100px'"
      @submit="handleSubmit"
    >
      <template #default="{ formData }">
        <el-form-item label="名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入提供商名称"
            clearable
          />
        </el-form-item>

        <el-form-item label="类型" prop="type">
          <el-select
            v-model="formData.type"
            placeholder="请选择类型"
            style="width: 100%"
          >
            <el-option label="OpenAI" value="openai" />
            <el-option label="Claude" value="claude" />
            <el-option label="Gemini" value="gemini" />
            <el-option label="Ollama" value="ollama" />
          </el-select>
        </el-form-item>

        <el-form-item label="API Key" prop="apiKey">
          <el-input
            v-model="formData.apiKey"
            type="password"
            placeholder="请输入 API Key"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="Base URL" prop="baseUrl">
          <el-input
            v-model="formData.baseUrl"
            placeholder="https://api.openai.com/v1"
            clearable
          />
        </el-form-item>
      </template>
    </HifyFormDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import HifyTable from '@/components/HifyTable.vue'
import HifyFormDialog from '@/components/HifyFormDialog.vue'
import { useConfirm } from '@/composables/useConfirm'
import { notifySuccess, notifyError } from '@/utils/notify'
import { Plus } from '@element-plus/icons-vue'
import type { TableColumn, PageParams, PageResult } from '@/types'

/**
 * Provider 数据类型
 */
interface Provider {
  id: number
  name: string
  type: 'openai' | 'claude' | 'gemini' | 'ollama'
  apiKey: string
  baseUrl: string
  status: 'enabled' | 'disabled'
  createdAt: string
}

/**
 * Mock 数据
 */
const mockProviders: Provider[] = [
  {
    id: 1,
    name: 'GPT-4',
    type: 'openai',
    apiKey: 'sk-xxxxx',
    baseUrl: 'https://api.openai.com/v1',
    status: 'enabled',
    createdAt: '2024-05-01 10:30:00',
  },
  {
    id: 2,
    name: 'Claude-3.5',
    type: 'claude',
    apiKey: 'sk-ant-xxxxx',
    baseUrl: 'https://api.anthropic.com',
    status: 'enabled',
    createdAt: '2024-04-28 14:20:00',
  },
  {
    id: 3,
    name: 'Gemini-Pro',
    type: 'gemini',
    apiKey: 'AIzaSy-xxxxx',
    baseUrl: 'https://generativelanguage.googleapis.com',
    status: 'disabled',
    createdAt: '2024-04-25 09:15:00',
  },
  {
    id: 4,
    name: 'Ollama-Local',
    type: 'ollama',
    apiKey: '',
    baseUrl: 'http://localhost:11434',
    status: 'enabled',
    createdAt: '2024-04-20 16:45:00',
  },
  {
    id: 5,
    name: 'GPT-3.5-Turbo',
    type: 'openai',
    apiKey: 'sk-xxxxx',
    baseUrl: 'https://api.openai.com/v1',
    status: 'disabled',
    createdAt: '2024-04-15 11:00:00',
  },
]

let providers = [...mockProviders]

/**
 * 屏幕宽度
 */
const screenWidth = ref(window.innerWidth)

/**
 * 完整列配置（宽屏）
 */
const allColumns: TableColumn<Provider>[] = [
  { label: '名称', prop: 'name', minWidth: 150 },
  { label: '类型', prop: 'type', width: 120, slot: 'type' },
  { label: 'Base URL', prop: 'baseUrl', minWidth: 200 },
  { label: '状态', prop: 'status', width: 100, slot: 'status' },
  { label: '创建时间', prop: 'createdAt', width: 180 },
  { label: '操作', prop: 'actions', width: 180, slot: 'actions' },
]

/**
 * 窄屏列配置（小于 1200px）
 */
const narrowColumns: TableColumn<Provider>[] = [
  { label: '名称', prop: 'name', minWidth: 150 },
  { label: '类型', prop: 'type', width: 120, slot: 'type' },
  { label: '状态', prop: 'status', width: 100, slot: 'status' },
  { label: '操作', prop: 'actions', width: 150, slot: 'actions' },
]

/**
 * 响应式列配置
 */
const columns = computed(() => {
  return screenWidth.value < 1200 ? narrowColumns : allColumns
})

/**
 * 类型映射
 */
const typeMap: Record<string, string> = {
  openai: 'OpenAI',
  claude: 'Claude',
  gemini: 'Gemini',
  ollama: 'Ollama',
}

/**
 * 类型 Tag 类型映射
 */
const typeTagType = (type: string): string => {
  const map: Record<string, string> = {
    openai: 'primary',
    claude: 'warning',
    gemini: 'success',
    ollama: 'info',
  }
  return map[type] || ''
}

/**
 * 表格行样式
 */
const rowStyle = () => {
  return {
    height: '52px',
  }
}

/**
 * 组件引用接口
 */
interface TableRef {
  refresh: () => void
}

interface DialogRef {
  open: (data?: Provider | null) => void
  close: () => void
  reset: () => void
}

const tableRef = ref<TableRef>()
const dialogRef = ref<DialogRef>()

/**
 * 弹窗状态
 */
const dialogVisible = ref(false)
const editingData = ref<Provider | null>(null)

/**
 * 表单验证规则
 */
const formRules = {
  name: [
    { required: true, message: '请输入提供商名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
  ],
  type: [
    { required: true, message: '请选择类型', trigger: 'change' },
  ],
  apiKey: [
    { required: true, message: '请输入 API Key', trigger: 'blur' },
  ],
  baseUrl: [
    { required: true, message: '请输入 Base URL', trigger: 'blur' },
    { type: 'url', message: '请输入正确的 URL 格式', trigger: 'blur' },
  ],
}

/**
 * 处理窗口大小变化
 */
const handleResize = () => {
  screenWidth.value = window.innerWidth
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

/**
 * Mock API：获取提供商列表
 */
const fetchProviderList = async (params: PageParams): Promise<PageResult<Provider>> => {
  // 模拟网络延迟
  await new Promise((resolve) => setTimeout(resolve, 300))

  const start = (params.page - 1) * params.pageSize
  const end = start + params.pageSize
  const list = providers.slice(start, end)

  return {
    list,
    total: providers.length,
    page: params.page,
    pageSize: params.pageSize,
  }
}

/**
 * Mock API：创建提供商
 */
const createProvider = async (data: Omit<Provider, 'id' | 'status' | 'createdAt'>): Promise<Provider> => {
  await new Promise((resolve) => setTimeout(resolve, 300))

  const newProvider: Provider = {
    ...data,
    id: Math.max(...providers.map((p) => p.id)) + 1,
    status: 'enabled',
    createdAt: new Date().toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    }),
  }

  providers.unshift(newProvider)
  return newProvider
}

/**
 * Mock API：更新提供商
 */
const updateProvider = async (id: number, data: Partial<Provider>): Promise<Provider> => {
  await new Promise((resolve) => setTimeout(resolve, 300))

  const index = providers.findIndex((p) => p.id === id)
  if (index !== -1) {
    providers[index] = { ...providers[index], ...data }
    return providers[index]
  }
  throw new Error('提供商不存在')
}

/**
 * Mock API：删除提供商
 */
const deleteProvider = async (id: number): Promise<void> => {
  await new Promise((resolve) => setTimeout(resolve, 300))

  const index = providers.findIndex((p) => p.id === id)
  if (index !== -1) {
    providers.splice(index, 1)
  } else {
    throw new Error('提供商不存在')
  }
}

/**
 * 删除确认
 */
const confirmDelete = useConfirm('确定要删除该提供商吗？', deleteProvider, '删除成功')

/**
 * 打开新增弹窗
 */
const handleCreate = () => {
  editingData.value = null
  dialogVisible.value = true
}

/**
 * 打开编辑弹窗
 */
const handleEdit = (row: Provider) => {
  editingData.value = row
  dialogVisible.value = true
}

/**
 * 删除
 */
const handleDelete = async (row: Provider) => {
  const confirmed = await confirmDelete(row.id)
  if (confirmed) {
    tableRef.value?.refresh()
  }
}

/**
 * 提交表单
 */
const handleSubmit = async (data: any) => {
  try {
    if (editingData.value) {
      // 编辑
      await updateProvider(editingData.value.id, data)
      notifySuccess('更新成功')
    } else {
      // 新增
      await createProvider(data)
      notifySuccess('创建成功')
    }

    dialogVisible.value = false
    tableRef.value?.refresh()
  } catch (error) {
    notifyError(editingData.value ? '更新失败' : '创建失败')
  }
}
</script>

<style scoped>
/* Provider 页面样式 */

.table-card {
  margin-top: var(--spacing-4);
}

:deep(.el-table) {
  font-size: 14px;
}

/* 表格行 hover 效果 - 排除操作列 */
:deep(.el-table__body tr:hover > td:not(:last-child)) {
  background-color: var(--color-bg-subtle) !important;
}

/* 操作列 hover 时保持透明 */
:deep(.el-table__body tr:hover > td:last-child) {
  background-color: transparent !important;
}

:deep(.el-table__row) {
  height: 52px !important;
}

:deep(.el-table__row td) {
  height: 52px !important;
  padding: 0;
}

/* 操作列单元格样式 */
:deep(.el-table__body .el-table__row td:last-child) {
  padding: 0 16px;
  white-space: nowrap;
  background-color: transparent !important;
}

/* 操作列 hover 时保持透明 */
:deep(.el-table__body tr:hover > td:last-child) {
  background-color: transparent !important;
}

/* 操作按钮容器 */
.action-buttons {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

/* 操作按钮通用样式 */
.action-buttons .el-button {
  padding: 0;
  font-weight: 500;
  background-color: transparent !important;
  border: none !important;
  min-height: auto;
  height: auto;
  line-height: 1;
}

/* 编辑按钮 - 蓝色 */
.action-buttons .btn-edit {
  color: #6366F1 !important;
}

.action-buttons .btn-edit:hover {
  color: #4F46E5 !important;
}

/* 删除按钮 - 红色 */
.action-buttons .btn-delete {
  color: #EF4444 !important;
}

.action-buttons .btn-delete:hover {
  color: #DC2626 !important;
}

:deep(.el-tag) {
  border-radius: var(--radius-sm);
  padding: 4px 12px;
  font-weight: 500;
}

:deep(.el-tag--primary) {
  background-color: var(--color-primary-50);
  color: var(--color-primary-600);
  border-color: var(--color-primary-200);
}

:deep(.el-tag--success) {
  background-color: var(--color-success-50);
  color: var(--color-success-600);
  border-color: var(--color-success-200);
}

:deep(.el-tag--warning) {
  background-color: var(--color-warning-50);
  color: var(--color-warning-600);
  border-color: var(--color-warning-200);
}

:deep(.el-tag--info) {
  background-color: var(--color-bg-subtle);
  color: var(--color-text-secondary);
  border-color: var(--color-border);
}
</style>
