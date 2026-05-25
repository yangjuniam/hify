package com.hify.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hify.agent.dto.AgentDetailVO;
import com.hify.agent.dto.CreateAgentRequest;
import com.hify.agent.dto.UpdateAgentRequest;
import com.hify.agent.entity.Agent;
import com.hify.agent.mapper.AgentMapper;
import com.hify.agent.service.AgentService;
import com.hify.common.exception.BizException;
import com.hify.common.exception.ErrorCode;
import com.hify.provider.service.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final AgentMapper agentMapper;
    private final ProviderService providerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Agent createAgent(CreateAgentRequest request) {
        validateNameAvailable(request.getName(), null);
        validateModelConfig(request.getModelConfigId());

        Agent agent = new Agent();
        BeanUtils.copyProperties(request, agent);
        agentMapper.insert(agent);
        return agent;
    }

    @Override
    public Page<Agent> listAgents(int page, int pageSize) {
        Page<Agent> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Agent::getCreatedAt);
        return agentMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public AgentDetailVO getAgentDetail(Long id) {
        Agent agent = agentMapper.selectById(id);
        if (agent == null) {
            throw new BizException(ErrorCode.AGENT_NOT_FOUND);
        }
        return buildAgentDetail(agent);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Agent updateAgent(UpdateAgentRequest request) {
        Agent agent = agentMapper.selectById(request.getId());
        if (agent == null) {
            throw new BizException(ErrorCode.AGENT_NOT_FOUND);
        }

        validateNameAvailable(request.getName(), request.getId());
        validateModelConfig(request.getModelConfigId());

        agent.setName(request.getName());
        agent.setDescription(request.getDescription());
        agent.setSystemPrompt(request.getSystemPrompt());
        agent.setModelConfigId(request.getModelConfigId());
        agent.setTemperature(request.getTemperature());
        agent.setMaxTokens(request.getMaxTokens());
        agent.setMaxContextTurns(request.getMaxContextTurns());
        agent.setEnabled(request.getEnabled());
        agentMapper.updateById(agent);
        return agent;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAgent(Long id) {
        Agent agent = agentMapper.selectById(id);
        if (agent == null) {
            throw new BizException(ErrorCode.AGENT_NOT_FOUND);
        }
        agentMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Agent updateAgentEnabled(Long id, Integer enabled) {
        Agent agent = agentMapper.selectById(id);
        if (agent == null) {
            throw new BizException(ErrorCode.AGENT_NOT_FOUND);
        }
        agent.setEnabled(enabled);
        agentMapper.updateById(agent);
        return agent;
    }

    private void validateNameAvailable(String name, Long excludeId) {
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Agent::getName, name);
        if (excludeId != null) {
            wrapper.ne(Agent::getId, excludeId);
        }
        if (agentMapper.selectCount(wrapper) > 0) {
            throw new BizException(ErrorCode.AGENT_NAME_EXISTS);
        }
    }

    private void validateModelConfig(Long modelConfigId) {
        if (!providerService.isModelConfigAvailable(modelConfigId)) {
            throw new BizException(ErrorCode.AGENT_MODEL_CONFIG_INVALID);
        }
    }

    private AgentDetailVO buildAgentDetail(Agent agent) {
        AgentDetailVO vo = new AgentDetailVO();
        BeanUtils.copyProperties(agent, vo);
        return vo;
    }
}
