#!/bin/bash
set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
PID_DIR="$PROJECT_DIR/.pid"
BACKEND_PID_FILE="$PID_DIR/backend.pid"
FRONTEND_PID_FILE="$PID_DIR/frontend.pid"
WAIT_TIMEOUT=10

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

is_process_running() {
    local pid="$1"
    [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null
}

stop_process() {
    local pid_file="$1"
    local process_name="$2"

    if [ ! -f "$pid_file" ]; then
        log_warn "$process_name PID 文件不存在: $pid_file"
        return 0
    fi

    local pid=$(cat "$pid_file")

    if ! is_process_running "$pid"; then
        log_warn "$process_name 进程 $pid 未运行"
        rm -f "$pid_file"
        return 0
    fi

    log_info "停止 $process_name (PID: $pid)..."

    # 发送 SIGTERM
    kill -TERM "$pid" 2>/dev/null || true

    # 等待进程退出
    local wait_count=0
    while [ $wait_count -lt $WAIT_TIMEOUT ]; do
        if ! is_process_running "$pid"; then
            log_info "$process_name 已停止"
            rm -f "$pid_file"
            return 0
        fi
        wait_count=$((wait_count + 1))
        echo -n "."
        sleep 1
    done

    echo ""
    log_warn "$process_name 未在 $WAIT_TIMEOUT 秒内退出，发送 SIGKILL..."
    kill -9 "$pid" 2>/dev/null || true
    sleep 1

    if ! is_process_running "$pid"; then
        log_info "$process_name 已强制停止"
        rm -f "$pid_file"
    else
        log_error "无法停止 $process_name (PID: $pid)"
        return 1
    fi
}

stop_by_port() {
    local port="$1"
    local process_name="$2"

    local pids=$(lsof -Pi :$port -sTCP:LISTEN -t 2>/dev/null)

    if [ -z "$pids" ]; then
        return 0
    fi

    for pid in $pids; do
        log_info "发现端口 $port 上运行的 $process_name (PID: $pid)"
        kill -TERM "$pid" 2>/dev/null || true

        local wait_count=0
        while [ $wait_count -lt $WAIT_TIMEOUT ]; do
            if ! kill -0 "$pid" 2>/dev/null; then
                log_info "$process_name (PID: $pid) 已停止"
                break
            fi
            wait_count=$((wait_count + 1))
            echo -n "."
            sleep 1
        done

        if kill -0 "$pid" 2>/dev/null; then
            echo ""
            log_warn "强制停止 $process_name (PID: $pid)"
            kill -9 "$pid" 2>/dev/null || true
        fi
    done
}

log_info "开始停止 Hify 服务..."

# 停止前端
stop_process "$FRONTEND_PID_FILE" "前端"

# 停止后端
stop_process "$BACKEND_PID_FILE" "后端"

# 兜底：按端口检查并停止残留进程
echo ""
log_info "检查端口残留进程..."
stop_by_port 5173 "前端"
stop_by_port 8080 "后端"

# 清理 PID 目录
if [ -d "$PID_DIR" ] && [ -z "$(ls -A "$PID_DIR")" ]; then
    rmdir "$PID_DIR"
fi

log_info "所有服务已停止"
