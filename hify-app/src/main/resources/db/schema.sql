-- Hify 数据库表结构
-- 字符集: utf8mb4
-- 引擎: InnoDB

-- provider (模型提供商)
CREATE TABLE provider (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '提供商名称 (openai/claude/gemini/ollama)',
    api_key VARCHAR(500) NOT NULL COMMENT 'API密钥',
    base_url VARCHAR(255) DEFAULT NULL COMMENT 'API基础地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-正常 1-删除',
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型提供商表';

-- model_config (模型配置)
CREATE TABLE model_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    provider_id BIGINT NOT NULL COMMENT '提供商ID',
    name VARCHAR(100) NOT NULL COMMENT '模型名称 (gpt-4/claude-3-opus/gemini-pro)',
    model_type VARCHAR(20) NOT NULL COMMENT '模型类型 (chat/completion)',
    max_tokens INT DEFAULT 4096 COMMENT '最大token数',
    temperature DECIMAL(3,2) DEFAULT 0.70 COMMENT '温度参数',
    top_p DECIMAL(3,2) DEFAULT 1.00 COMMENT 'top_p参数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-正常 1-删除',
    UNIQUE KEY uk_provider_model (provider_id, name),
    INDEX idx_model_config_provider_id (provider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型配置表';

-- mcp_server (MCP工具服务器)
CREATE TABLE mcp_server (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '服务器名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    endpoint_url VARCHAR(500) NOT NULL COMMENT '端点地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-正常 1-删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MCP工具服务器表';

-- agent (Agent配置)
CREATE TABLE agent (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT 'Agent名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    system_prompt TEXT DEFAULT NULL COMMENT 'System Prompt',
    model_config_id BIGINT NOT NULL COMMENT '关联的模型配置ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-正常 1-删除',
    INDEX idx_agent_model_config_id (model_config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent配置表';

-- agent_tool (Agent与MCP工具关联表 M:N)
CREATE TABLE agent_tool (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    agent_id BIGINT NOT NULL COMMENT 'Agent ID',
    mcp_server_id BIGINT NOT NULL COMMENT 'MCP服务器ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-正常 1-删除',
    UNIQUE KEY uk_agent_mcp (agent_id, mcp_server_id),
    INDEX idx_agent_tool_agent_id (agent_id),
    INDEX idx_agent_tool_mcp_server_id (mcp_server_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent工具关联表';

-- chat_session (对话会话)
CREATE TABLE chat_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    agent_id BIGINT NOT NULL COMMENT 'Agent ID',
    session_id VARCHAR(64) NOT NULL COMMENT '会话唯一标识',
    title VARCHAR(200) DEFAULT NULL COMMENT '会话标题',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-正常 1-删除',
    UNIQUE KEY uk_session_id (session_id),
    INDEX idx_chat_session_agent_id (agent_id),
    INDEX idx_chat_session_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话会话表';

-- chat_message (对话消息)
CREATE TABLE chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    role VARCHAR(20) NOT NULL COMMENT '角色 (user/assistant/system)',
    content TEXT NOT NULL COMMENT '消息内容',
    tokens INT DEFAULT 0 COMMENT 'token数',
    model_name VARCHAR(100) DEFAULT NULL COMMENT '使用的模型名称',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-正常 1-删除',
    INDEX idx_chat_message_session_id (session_id),
    INDEX idx_chat_message_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话消息表';