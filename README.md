# Hify - 轻量级 AI Agent 开发平台

参考 Dify 的简版 AI Agent 开发平台，面向团队内部小规模使用（20-50 人同时在线）。

## 技术栈

- **后端**: Spring Boot 3.x + MyBatis-Plus + MySQL 8.x + Redis 7.x + pgvector
- **前端**: Vue 3 + TypeScript + Element Plus + Vite
- **容器化**: Docker + K8s

## 功能特性

- ✅ 多模型提供商管理（OpenAI、Claude、Gemini、Ollama）
- ✅ Agent 创建与配置
- ✅ 对话引擎（流式响应、多轮对话）
- ✅ RAG 知识库
- ✅ MCP 工具接入
- ✅ 管理控制台

## 快速开始

### 方式一：Make 命令

```bash
# 启动服务
make start

# 停止服务
make stop

# 构建
make build

# 打包
make package
```

### 方式二：直接运行脚本

```bash
# 启动
./start.sh

# 停止
./stop.sh
```

## 访问地址

- 前端界面: http://localhost:5173
- 后端接口: http://localhost:8080
- 健康检查: http://localhost:8080/api/v1/health

## License

MIT
