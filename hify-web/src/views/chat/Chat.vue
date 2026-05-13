<template>
  <div class="chat-page">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">对话</h1>
        <p class="page-description">与您的 AI Agent 进行多轮对话，支持流式响应和上下文管理</p>
      </div>
      <div class="page-header-actions">
        <button class="btn-secondary">历史记录</button>
        <button class="btn-primary">新建对话</button>
      </div>
    </div>

    <!-- 对话区域 -->
    <div class="hify-card chat-container">
      <div class="card-header">
        <h2 class="card-title">对话会话</h2>
        <div class="card-actions">
          <el-button :icon="Refresh">刷新</el-button>
        </div>
      </div>
      <div class="card-body">
        <el-empty description="开始一个新的对话，选择一个 Agent 开始交流" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Refresh } from '@element-plus/icons-vue'
</script>

<style scoped>
.chat-container {
  min-height: 500px;
}
</style>
