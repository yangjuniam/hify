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
        :api="getProviderList"
        :row-style="rowStyle"
      >
        <!-- 类型列自定义渲染 -->
        <template #type="{ row }">
          <el-tag :type="typeTagType(row.type)">{{ typeMap[row.type] }}</el-tag>
        </template>

        <template #enabled="{ row }">
          <el-tag :type="row.enabled === 1 ? 'success' : 'info'">
            {{ row.enabled === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>

        <template #health="{ row }">
          <el-tag :type="healthTagType(row.health?.status)">
            {{ row.health?.status || 'UNKNOWN' }}
          </el-tag>
          <span v-if="row.health?.latencyMs != null" class="health-latency">
            {{ row.health.latencyMs }}ms
          </span>
        </template>

        <template #modelCount="{ row }">
          {{ enabledModelCount(row) }}
        </template>

        <template #actions="{ row }">
          <span class="action-buttons">
            <el-button text class="btn-test" @click="handleTestConnection(row)">连通性测试</el-button>
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
            <el-option label="OpenAI" value="OPENAI" />
            <el-option label="Claude" value="ANTHROPIC" />
            <el-option label="Ollama" value="OLLAMA" />
            <el-option label="Azure OpenAI" value="AZURE_OPENAI" />
            <el-option label="OpenAI Compatible" value="OPENAI_COMPATIBLE" />
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

        <el-form-item label="状态" prop="enabled">
          <div class="status-toggle">
            <el-button
              :type="normalizeEnabled(formData.enabled) === 1 ? 'success' : 'default'"
              @click="formData.enabled = 1"
            >
              启用
            </el-button>
            <el-button
              :type="normalizeEnabled(formData.enabled) === 0 ? 'danger' : 'default'"
              @click="formData.enabled = 0"
            >
              禁用
            </el-button>
          </div>
        </el-form-item>
      </template>
    </HifyFormDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import HifyTable from '@/components/HifyTable.vue'
import HifyFormDialog from '@/components/HifyFormDialog.vue'
import { useConfirm } from '@/composables/useConfirm'
import { notifySuccess, notifyError } from '@/utils/notify'
import { Plus } from '@element-plus/icons-vue'
import {
  getProviderList,
  createProvider,
  updateProvider,
  deleteProvider,
  testConnection,
} from '@/api/provider'
import type {
  CreateProviderRequest,
  Provider,
  ProviderDetail,
  UpdateProviderRequest,
} from '@/api/provider'
import type { TableColumn } from '@/types'

type ProviderFormData = Partial<Provider> & {
  apiKey?: string
}

/**
 * 屏幕宽度
 */
const screenWidth = ref(window.innerWidth)

/**
 * 完整列配置（宽屏）
 */
const allColumns: TableColumn<ProviderDetail>[] = [
  { label: '名称', prop: 'name', minWidth: 150 },
  { label: '类型', prop: 'type', width: 160, slot: 'type' },
  { label: 'Base URL', prop: 'baseUrl', minWidth: 200 },
  { label: '状态', prop: 'enabled', width: 100, slot: 'enabled' },
  { label: '健康状态', prop: 'health', width: 180, slot: 'health' },
  { label: '模型数', prop: 'modelCount', width: 100, slot: 'modelCount' },
  { label: '创建时间', prop: 'createdAt', width: 180 },
  { label: '操作', prop: 'actions', width: 260, slot: 'actions' },
]

/**
 * 窄屏列配置（小于 1200px）
 */
const narrowColumns: TableColumn<ProviderDetail>[] = [
  { label: '名称', prop: 'name', minWidth: 150 },
  { label: '类型', prop: 'type', width: 160, slot: 'type' },
  { label: '状态', prop: 'enabled', width: 100, slot: 'enabled' },
  { label: '健康状态', prop: 'health', width: 180, slot: 'health' },
  { label: '模型数', prop: 'modelCount', width: 100, slot: 'modelCount' },
  { label: '操作', prop: 'actions', width: 260, slot: 'actions' },
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
  OPENAI: 'OpenAI',
  ANTHROPIC: 'Claude',
  OLLAMA: 'Ollama',
  AZURE_OPENAI: 'Azure OpenAI',
  OPENAI_COMPATIBLE: 'OpenAI Compatible',
}

/**
 * 类型 Tag 类型映射
 */
const typeTagType = (type: string): string => {
  const map: Record<string, string> = {
    OPENAI: 'primary',
    ANTHROPIC: 'warning',
    OLLAMA: 'info',
    AZURE_OPENAI: 'success',
    OPENAI_COMPATIBLE: 'success',
  }
  return map[type] || ''
}

const healthTagType = (status?: string): 'success' | 'danger' | 'warning' | 'info' => {
  const map: Record<string, 'success' | 'danger' | 'warning' | 'info'> = {
    UP: 'success',
    DOWN: 'danger',
    DEGRADED: 'warning',
    UNKNOWN: 'info',
  }
  return map[status || 'UNKNOWN'] || 'info'
}

const enabledModelCount = (provider: ProviderDetail): number => {
  return provider.modelConfigs?.filter((model) => model.enabled === 1).length || 0
}

const normalizeEnabled = (enabled: unknown): number => {
  return enabled === 0 || enabled === '0' || enabled === false ? 0 : 1
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
  open: (data?: ProviderFormData | null) => void
  close: () => void
  reset: () => void
}

const tableRef = ref<TableRef>()
const dialogRef = ref<DialogRef>()

/**
 * 弹窗状态
 */
const dialogVisible = ref(false)
const editingData = ref<ProviderFormData | null>(null)

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

const buildProviderPayload = (data: ProviderFormData): CreateProviderRequest => {
  const authConfig = data.apiKey
    ? { ...(data.authConfig || {}), apiKey: data.apiKey }
    : data.authConfig

  return {
    name: data.name || '',
    type: data.type || '',
    baseUrl: data.baseUrl || '',
    authConfig,
    description: data.description,
    enabled: normalizeEnabled(data.enabled),
  }
}

const confirmDelete = useConfirm('确定要删除该提供商吗？', deleteProvider, '删除成功')

const handleCreate = () => {
  editingData.value = null
  dialogVisible.value = true
}

const handleEdit = (row: Provider) => {
  editingData.value = {
    ...row,
    apiKey: typeof row.authConfig?.apiKey === 'string' ? row.authConfig.apiKey : '',
  }
  dialogVisible.value = true
}

const handleDelete = async (row: Provider) => {
  const confirmed = await confirmDelete(row.id)
  if (confirmed) {
    tableRef.value?.refresh()
  }
}

const handleTestConnection = async (row: Provider) => {
  try {
    const result = await testConnection(row.id)
    if (result.success) {
      ElMessage.success(`连通性测试成功，延迟 ${result.latencyMs ?? '-'}ms，模型数 ${result.modelCount ?? 0}`)
      tableRef.value?.refresh()
      return
    }
    ElMessage.error(result.errorMessage || '连通性测试失败')
  } catch (error) {
    ElMessage.error('连通性测试失败')
  }
}

const handleSubmit = async (data: ProviderFormData) => {
  try {
    const payload = buildProviderPayload(data)
    if (editingData.value) {
      if (editingData.value.id == null) {
        notifyError('提供商ID不能为空')
        return
      }
      await updateProvider({ ...payload, id: editingData.value.id } as UpdateProviderRequest)
      notifySuccess('更新成功')
    } else {
      await createProvider(payload)
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

.health-latency {
  margin-left: 8px;
  color: var(--color-text-secondary);
}

.status-toggle {
  display: inline-flex;
  gap: 8px;
}

.status-toggle .el-button {
  min-width: 72px;
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

.action-buttons .btn-test {
  color: var(--color-success-600) !important;
}

.action-buttons .btn-test:hover {
  color: var(--color-success-700) !important;
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
