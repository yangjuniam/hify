.PHONY: start stop restart build clean package help

# 项目信息
VERSION := 1.0.0-SNAPSHOT
PROJECT_NAME := hify
DIST_DIR := dist
BACKEND_JAR := hify-app/target/hify-app-$(VERSION).jar

# 颜色输出
GREEN  := \033[0;32m
YELLOW := \033[1;33m
RED    := \033[0;31m
NC     := \033[0m

help:
	@echo "Hify AI Agent Platform Makefile"
	@echo ""
	@echo "用法:"
	@echo "  make start     启动服务（后端后台 + 前端前台）"
	@echo "  make stop      停止所有服务"
	@echo "  make restart   重启服务"
	@echo "  make build     构建后端 + 前端"
	@echo "  make clean     清理构建产物"
	@echo "  make package   打包成可分发的 tar.gz"
	@echo ""

start:
	@echo -e "${GREEN}▶ 启动 Hify 服务...${NC}"
	@./start.sh

stop:
	@echo -e "${YELLOW}■ 停止 Hify 服务...${NC}"
	@./stop.sh

restart: stop start

build: build-backend build-frontend
	@echo -e "${GREEN}✓ 构建完成${NC}"

build-backend:
	@echo -e "${GREEN}▶ 构建后端...${NC}"
	mvn clean package -DskipTests -q
	@echo -e "${GREEN}✓ 后端构建完成${NC}"

build-frontend:
	@echo -e "${GREEN}▶ 构建前端...${NC}"
	cd hify-web && npm run build
	@echo -e "${GREEN}✓ 前端构建完成${NC}"

clean: clean-backend clean-frontend clean-dist
	@rm -rf .pid
	@echo -e "${GREEN}✓ 清理完成${NC}"

clean-backend:
	@echo -e "${YELLOW}▶ 清理后端构建产物...${NC}"
	mvn clean -q

clean-frontend:
	@echo -e "${YELLOW}▶ 清理前端构建产物...${NC}"
	rm -rf hify-web/dist

clean-dist:
	rm -rf $(DIST_DIR)

package: build
	@echo -e "${GREEN}▶ 打包分发包...${NC}"
	@mkdir -p $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION)
	@mkdir -p $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION)/bin
	@mkdir -p $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION)/config
	@mkdir -p $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION)/logs

	# 复制后端 JAR
	@cp $(BACKEND_JAR) $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION)/bin/

	# 复制前端构建产物
	@cp -r hify-web/dist $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION)/web

	# 复制启动脚本
	@cp start.sh stop.sh $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION)/bin/
	@chmod +x $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION)/bin/*.sh

	# 复制配置文件示例
	@cp hify-app/src/main/resources/application.yml $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION)/config/

	# 复制 README 和文档
	@cp README.md $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION)/ 2>/dev/null || true
	@cp -r docs $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION)/ 2>/dev/null || true

	# 创建启动脚本
	@echo '#!/bin/bash' > $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION)/start.sh
	@echo 'cd "$$(dirname "$$0")/bin"' >> $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION)/start.sh
	@echo './start.sh' >> $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION)/start.sh
	@chmod +x $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION)/start.sh

	@echo '#!/bin/bash' > $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION)/stop.sh
	@echo 'cd "$$(dirname "$$0")/bin"' >> $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION)/stop.sh
	@echo './stop.sh' >> $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION)/stop.sh
	@chmod +x $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION)/stop.sh

	# 打包
	@cd $(DIST_DIR) && tar -czf $(PROJECT_NAME)-$(VERSION).tar.gz $(PROJECT_NAME)-$(VERSION)
	@rm -rf $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION)

	@echo -e "${GREEN}✓ 打包完成: $(DIST_DIR)/$(PROJECT_NAME)-$(VERSION).tar.gz${NC}"
