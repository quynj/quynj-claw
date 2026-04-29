package com.github.quynj.agentconsole.api;

import com.github.quynj.agentconsole.application.AgentScopeRuntimeService;
import com.github.quynj.agentconsole.common.Result;
import com.github.quynj.agentconsole.dto.ChatResponse;
import com.github.quynj.agentconsole.dto.SendMessageRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions/{sessionId}/messages")
public class ChatController {
    private final AgentScopeRuntimeService runtimeService;

    public ChatController(AgentScopeRuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @PostMapping
    public Result<ChatResponse> send(@PathVariable String sessionId, @Valid @RequestBody SendMessageRequest request) {
        return Result.ok(runtimeService.chat(sessionId, request));
    }
}
