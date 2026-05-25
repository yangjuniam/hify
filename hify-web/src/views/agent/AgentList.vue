<template>
  <div class="agent-page">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">Agent 管理</h1>
        <p class="page-description">创建和配置 AI Agent，绑定模型、工具和系统提示词，构建智能对话助手</p>
      </div>
      <div class="page-header-actions">
        <button class="btn-secondary">从模板创建</button>
        <button class="btn-primary" @click="handleCreate">新建 Agent</button>
      </div>
    </div>

    <!-- Agent 列表卡片 -->
    <div class="hify-card">
      <div class="card-header">
        <h2 class="card-title">我的 Agent</h2>
        <div class="card-actions">
          <el-input placeholder="搜索 Agent..." style="width: 240px;" />
          <el-button :icon="Filter">筛选</el-button>
        </div>
      </div>
      <div class="card-body">
        <el-empty description="暂无 Agent，点击上方按钮创建您的第一个 AI 助手" />
      </div>
    </div>

    <!-- 新建 Agent 弹窗 -->
    <HifyFormDialog
      v-model="dialogVisible"
      title="新建 Agent"
      :width="560"
      :rules="formRules"
      label-position="right"
      :label-width="'120px'"
      @submit="handleSubmit"
    >
      <template #default="{ formData }">
        <el-form-item label="名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入 Agent 名称"
            clearable
          />
        </el-form-item>

        <el-form-item label="模型配置ID" prop="modelConfigId">
          <el-input-number
            v-model="formData.modelConfigId"
            :min="1"
            :precision="0"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入 Agent 描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="系统提示词" prop="systemPrompt">
          <el-input
            v-model="formData.systemPrompt"
            type="textarea"
            :rows="5"
            placeholder="请输入 System Prompt"
          />
        </el-form-item>

        <el-form-item label="温度" prop="temperature">
          <el-input-number
            v-model="formData.temperature"
            :min="0"
            :max="1"
            :step="0.1"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="最大输出Token" prop="maxTokens">
          <el-input-number
            v-model="formData.maxTokens"
            :min="1"
            :max="200000"
            :precision="0"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="上下文轮数" prop="maxContextTurns">
          <el-input-number
            v-model="formData.maxContextTurns"
            :min="1"
            :max="100"
            :precision="0"
            style="width: 100%"
          />
        </el-form-item>
      </template>
    </HifyFormDialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Filter } from '@element-plus/icons-vue'
import HifyFormDialog from '@/components/HifyFormDialog.vue'
import { createAgent } from '@/api/agent'
import type { CreateAgentRequest } from '@/api/agent'
import { notifySuccess, notifyError } from '@/utils/notify'

type AgentFormData = Partial<CreateAgentRequest>

const dialogVisible = ref(false)

const formRules = {
  name: [
    { required: true, message: '请输入 Agent 名称', trigger: 'blur' },
    { max: 100, message: '长度不能超过 100 个字符', trigger: 'blur' },
  ],
  modelConfigId: [
    { required: true, message: '请输入模型配置ID', trigger: 'blur' },
  ],
  description: [
    { max: 500, message: '长度不能超过 500 个字符', trigger: 'blur' },
  ],
}

const handleCreate = () => {
  dialogVisible.value = true
}

const buildAgentPayload = (data: AgentFormData): CreateAgentRequest => {
  return {
    name: data.name || '',
    description: data.description,
    systemPrompt: data.systemPrompt,
    modelConfigId: Number(data.modelConfigId),
    temperature: data.temperature ?? 0.7,
    maxTokens: data.maxTokens ?? 2048,
    maxContextTurns: data.maxContextTurns ?? 10,
    enabled: 1,
  }
}

const handleSubmit = async (data: AgentFormData) => {
  try {
    await createAgent(buildAgentPayload(data))
    notifySuccess('创建成功')
    dialogVisible.value = false
  } catch (error) {
    notifyError('创建失败')
  }
}
</script>

<style scoped>
/* Agent 页面样式 */
</style>
