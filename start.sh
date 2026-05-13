#!/bin/bash
set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_PORT=8080
FRONTEND_PORT=5173
BACKEND_PID=""
FRONTEND_PID=""
PID_DIR="$PROJECT_DIR/.pid"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

cleanup() {
    if [ -n "$BACKEND_PID" ] && kill -0 "$BACKEND_PID" 2>/dev/null; then
        log_info "正在停止后端服务 (PID: $BACKEND_PID)..."
        kill "$BACKEND_PID" 2>/dev/null || true
    fi
    rm -rf "$PID_DIR"
}

trap cleanup EXIT

# 创建 PID 目录
mkdir -p "$PID_DIR"

# 检查 Redis 是否可用（可选，因为开发环境可能还没配置 Redis）
log_info "检查 Redis 连接..."
if command -v redis-cli &>/dev/null; then
    if redis-cli ping 2>/dev/null | grep -q "PONG"; then
        log_info "Redis 连接正常"
    else
        log_warn "Redis 连接失败，继续启动（当前开发环境未强制依赖 Redis）"
    fi
else
    log_warn "redis-cli 未找到，跳过 Redis 检查"
fi

# 构建后端
log_info "开始构建后端..."
cd "$PROJECT_DIR"
if ! mvn clean package -DskipTests -q; then
    log_error "后端构建失败，请检查 Maven 配置和代码"
    exit 1
fi
log_info "后端构建成功"

# 查找 JAR 文件
JAR_FILE=$(find "$PROJECT_DIR/hify-app/target" -name "hify-app-*.jar" -type f | head -1)
if [ -z "$JAR_FILE" ]; then
    log_error "未找到后端 JAR 文件"
    exit 1
fi
log_info "找到 JAR 文件: $(basename "$JAR_FILE")"

# 检查端口是否被占用
if lsof -Pi :$BACKEND_PORT -sTCP:LISTEN -t >/dev/null 2>&1; then
    log_warn "端口 $BACKEND_PORT 已被占用，尝试关闭..."
    lsof -Pi :$BACKEND_PORT -sTCP:LISTEN -t | xargs kill -15 2>/dev/null || true
    sleep 2
fi

# 启动后端服务（后台运行）
log_info "启动后端服务..."
cd "$PROJECT_DIR"
java -jar "$JAR_FILE" --spring.profiles.active=dev > /tmp/hify-backend.log 2>&1 &
BACKEND_PID=$!
echo "$BACKEND_PID" > "$PID_DIR/backend.pid"
log_info "后端服务已启动，PID: $BACKEND_PID"

# 轮询等待后端健康检查通过
log_info "等待后端服务就绪..."
MAX_RETRIES=30
RETRY_COUNT=0
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -s http://localhost:$BACKEND_PORT/api/v1/health > /dev/null 2>&1; then
        log_info "后端服务已就绪！"
        break
    fi

    # 检查进程是否还在运行
    if ! kill -0 $BACKEND_PID 2>/dev/null; then
        log_error "后端服务意外退出，请检查日志: /tmp/hify-backend.log"
        exit 1
    fi

    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo -n "."
    sleep 2
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo ""
    log_error "后端服务启动超时，请检查日志: /tmp/hify-backend.log"
    exit 1
fi

echo ""

# 启动前端开发服务器
log_info "启动前端开发服务器..."
cd "$PROJECT_DIR/hify-web"

# 检查 node_modules 是否存在
if [ ! -d "node_modules" ]; then
    log_info "安装前端依赖..."
    if ! npm install; then
        log_error "前端依赖安装失败"
        exit 1
    fi
fi

log_info "前端服务启动中，请访问 http://localhost:$FRONTEND_PORT"
log_info "按 Ctrl+C 停止所有服务"
npm run dev
