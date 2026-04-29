package com.github.quynj.agentconsole.api;

import com.github.quynj.agentconsole.agentscope.AgentSessionStore;
import com.github.quynj.agentconsole.application.ConversationService;
import com.github.quynj.agentconsole.application.SummaryService;
import com.github.quynj.agentconsole.common.PageResult;
import com.github.quynj.agentconsole.common.Result;
import com.github.quynj.agentconsole.dto.ChatSessionDTO;
import com.github.quynj.agentconsole.dto.SessionCreateRequest;
import com.github.quynj.agentconsole.dto.SessionSummaryDTO;
import com.github.quynj.agentconsole.dto.SessionUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
    private final ConversationService conversationService;
    private final SummaryService summaryService;
    private final AgentSessionStore agentSessionStore;

    public SessionController(ConversationService conversationService, SummaryService summaryService,
                             AgentSessionStore agentSessionStore) {
        this.conversationService = conversationService;
        this.summaryService = summaryService;
        this.agentSessionStore = agentSessionStore;
    }

    @GetMapping
    public Result<PageResult<ChatSessionDTO>> list(@RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(conversationService.list(keyword, status, page, pageSize));
    }

    @PostMapping
    public Result<ChatSessionDTO> create(@Valid @RequestBody SessionCreateRequest request) {
        return Result.ok(conversationService.create(request));
    }

    @GetMapping("/{sessionId}")
    public Result<ChatSessionDTO> get(@PathVariable String sessionId) {
        return Result.ok(conversationService.get(sessionId));
    }

    @PatchMapping("/{sessionId}")
    public Result<ChatSessionDTO> update(@PathVariable String sessionId,
                                         @Valid @RequestBody SessionUpdateRequest request) {
        return Result.ok(conversationService.update(sessionId, request));
    }

    @DeleteMapping("/{sessionId}")
    public Result<Void> delete(@PathVariable String sessionId) {
        ChatSessionDTO session = conversationService.get(sessionId);
        conversationService.delete(sessionId);
        agentSessionStore.delete(session.agentscopeSessionId);
        return Result.ok(null);
    }

    @GetMapping("/{sessionId}/summary")
    public Result<SessionSummaryDTO> summary(@PathVariable String sessionId) {
        conversationService.get(sessionId);
        return Result.ok(summaryService.get(sessionId));
    }
}
