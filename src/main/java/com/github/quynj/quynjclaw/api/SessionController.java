package com.github.quynj.quynjclaw.api;

import com.github.quynj.quynjclaw.agentscope.AgentSessionStore;
import com.github.quynj.quynjclaw.application.ConversationService;
import com.github.quynj.quynjclaw.application.LocalFileService;
import com.github.quynj.quynjclaw.application.SummaryService;
import com.github.quynj.quynjclaw.common.PageResult;
import com.github.quynj.quynjclaw.common.Result;
import com.github.quynj.quynjclaw.dto.ChatSessionDTO;
import com.github.quynj.quynjclaw.dto.SessionCreateRequest;
import com.github.quynj.quynjclaw.dto.SessionSummaryDTO;
import com.github.quynj.quynjclaw.dto.SessionUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
    private final ConversationService conversationService;
    private final SummaryService summaryService;
    private final AgentSessionStore agentSessionStore;
    private final LocalFileService fileService;

    public SessionController(ConversationService conversationService, SummaryService summaryService,
                             AgentSessionStore agentSessionStore, LocalFileService fileService) {
        this.conversationService = conversationService;
        this.summaryService = summaryService;
        this.agentSessionStore = agentSessionStore;
        this.fileService = fileService;
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
        fileService.deleteSessionFiles(sessionId);
        return Result.ok(null);
    }

    @GetMapping("/{sessionId}/summary")
    public Result<SessionSummaryDTO> summary(@PathVariable String sessionId) {
        conversationService.get(sessionId);
        return Result.ok(summaryService.get(sessionId));
    }
}
