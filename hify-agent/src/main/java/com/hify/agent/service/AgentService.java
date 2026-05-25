package com.hify.agent.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hify.agent.dto.AgentDetailVO;
import com.hify.agent.dto.CreateAgentRequest;
import com.hify.agent.dto.UpdateAgentRequest;
import com.hify.agent.entity.Agent;

public interface AgentService {

    Agent createAgent(CreateAgentRequest request);

    Page<Agent> listAgents(int page, int pageSize);

    AgentDetailVO getAgentDetail(Long id);

    Agent updateAgent(UpdateAgentRequest request);

    void deleteAgent(Long id);

    Agent updateAgentEnabled(Long id, Integer enabled);
}
