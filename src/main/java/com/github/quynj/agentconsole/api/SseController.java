package com.github.quynj.agentconsole.api;

import com.github.quynj.agentconsole.application.ConversationService;
import com.github.quynj.agentconsole.application.RealtimeEventService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sessions/{sessionId}/events")
public class SseController {
    private final ConversationService conversationService;
    private final RealtimeEventService realtimeEventService;

    public SseController(ConversationService conversationService, RealtimeEventService realtimeEventService) {
        this.conversationService = conversationService;
        this.realtimeEventService = realtimeEventService;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events(@PathVariable String sessionId) {
        conversationService.get(sessionId);
        return realtimeEventService.subscribe(sessionId);
    }
}
