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
        <button class="btn-primary">新建 Agent</button>
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
  </div>
</template>

<script setup lang="ts">
import { Filter } from '@element-plus/icons-vue'
</script>

<style scoped>
/* Agent 页面样式 */
</style>
