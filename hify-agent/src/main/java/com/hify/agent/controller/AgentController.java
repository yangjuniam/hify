package com.hify.agent.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hify.agent.dto.AgentDetailVO;
import com.hify.agent.dto.CreateAgentRequest;
import com.hify.agent.dto.UpdateAgentRequest;
import com.hify.agent.entity.Agent;
import com.hify.agent.service.AgentService;
import com.hify.common.dto.PageResult;
import com.hify.common.dto.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @PostMapping
    public Result<Agent> createAgent(@Valid @RequestBody CreateAgentRequest request) {
        Agent agent = agentService.createAgent(request);
        return Result.success(agent);
    }

    @GetMapping
    public Result<PageResult<Agent>> listAgents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<Agent> pageResult = agentService.listAgents(page, pageSize);
        return Result.success(PageResult.of(
                pageResult.getRecords(),
                pageResult.getTotal(),
                page,
                pageSize
        ));
    }

    @GetMapping("/{id}")
    public Result<AgentDetailVO> getAgentDetail(@PathVariable Long id) {
        AgentDetailVO detail = agentService.getAgentDetail(id);
        return Result.success(detail);
    }

    @PutMapping("/{id}")
    public Result<Agent> updateAgent(@PathVariable Long id, @Valid @RequestBody UpdateAgentRequest request) {
        request.setId(id);
        Agent agent = agentService.updateAgent(request);
        return Result.success(agent);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAgent(@PathVariable Long id) {
        agentService.deleteAgent(id);
        return Result.success();
    }

    @PatchMapping("/{id}/enabled")
    public Result<Agent> updateAgentEnabled(@PathVariable Long id, @RequestParam Integer enabled) {
        Agent agent = agentService.updateAgentEnabled(id, enabled);
        return Result.success(agent);
    }
}
