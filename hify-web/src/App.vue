<template>
  <el-config-provider :locale="zhCn">
    <div class="hify-layout">
      <!-- 侧边栏 -->
      <aside class="hify-sidebar" :class="{ collapsed: isCollapsed }">
        <!-- Logo 区域 -->
        <div class="sidebar-logo">
          <div class="sidebar-logo-brand">Hify</div>
          <div class="sidebar-logo-subtitle">AI Agent Platform</div>
        </div>

        <!-- 菜单 -->
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          class="sidebar-menu"
          router
        >
          <el-menu-item index="/provider">
            <el-icon><Setting /></el-icon>
            <span>模型管理</span>
          </el-menu-item>
          <el-menu-item index="/agent">
            <el-icon><User /></el-icon>
            <span>Agent 管理</span>
          </el-menu-item>
          <el-menu-item index="/chat">
            <el-icon><ChatDotRound /></el-icon>
            <span>对话</span>
          </el-menu-item>
        </el-menu>

        <!-- 底部：折叠按钮 + 版本号 -->
        <div class="sidebar-footer">
          <button class="collapse-btn" @click="toggleCollapse">
            <el-icon>
              <component :is="isCollapsed ? 'ArrowRight' : 'ArrowLeft'" />
            </el-icon>
          </button>
          <div class="sidebar-version" v-if="!isCollapsed">v1.0.0</div>
        </div>
      </aside>

      <!-- 主内容区 -->
      <main class="hify-main-content">
        <!-- 顶栏 -->
        <header class="hify-header">
          <!-- 左侧：面包屑 -->
          <div class="header-left">
            <el-breadcrumb :separator-icon="ArrowRight" class="hify-breadcrumb">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <!-- 右侧：用户信息 -->
          <div class="header-right">
            <div class="user-info">
              <div class="user-avatar">A</div>
              <span class="user-name">Admin</span>
            </div>
          </div>
        </header>

        <!-- 页面内容 -->
        <div class="hify-page">
          <router-view />
        </div>
      </main>
    </div>
  </el-config-provider>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { Setting, User, ChatDotRound, ArrowRight } from '@element-plus/icons-vue'

// 引入样式
import './styles/index.css'

const route = useRoute()
const activeMenu = computed(() => route.path)
const isCollapsed = ref(false)

// 响应式断点：小于 1200px 时自动折叠
const handleResize = () => {
  if (window.innerWidth < 1200) {
    isCollapsed.value = true
  } else {
    isCollapsed.value = false
  }
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

// 当前页面标题映射
const pageTitleMap: Record<string, string> = {
  '/provider': '模型管理',
  '/agent': 'Agent 管理',
  '/chat': '对话'
}

const currentPageTitle = computed(() => {
  return pageTitleMap[route.path] || '首页'
})

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}
</script>

<style scoped>
/* 组件内样式（如有需要） */
</style>
