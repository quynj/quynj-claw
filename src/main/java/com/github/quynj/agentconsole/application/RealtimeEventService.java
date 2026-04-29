package com.github.quynj.agentconsole.application;

import com.github.quynj.agentconsole.dto.AgentMessageDTO;
import com.github.quynj.agentconsole.dto.ChatSessionDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RealtimeEventService {
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String sessionId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.computeIfAbsent(sessionId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(sessionId, emitter));
        emitter.onTimeout(() -> remove(sessionId, emitter));
        emitter.onError(error -> remove(sessionId, emitter));
        return emitter;
    }

    public void publishMessageCreated(String sessionId, AgentMessageDTO message) {
        publish(sessionId, "message.created", Map.of("sessionId", sessionId, "message", message));
    }

    public void publishSessionUpdated(String sessionId, ChatSessionDTO session) {
        publish(sessionId, "session.updated", Map.of("sessionId", sessionId, "session", session));
    }

    public void publishError(String sessionId, String message, String detail) {
        publish(sessionId, "error", Map.of("sessionId", sessionId, "message", message, "detail", detail));
    }

    private void publish(String sessionId, String event, Object data) {
        List<SseEmitter> sessionEmitters = emitters.getOrDefault(sessionId, List.of());
        for (SseEmitter emitter : sessionEmitters) {
            try {
                emitter.send(SseEmitter.event().name(event).data(data));
            } catch (Exception e) {
                removeAndComplete(sessionId, emitter);
            }
        }
    }

    private void removeAndComplete(String sessionId, SseEmitter emitter) {
        remove(sessionId, emitter);
        try {
            emitter.complete();
        } catch (Exception ignored) {
            // The client may already be gone; removing the stale emitter is enough.
        }
    }

    private void remove(String sessionId, SseEmitter emitter) {
        List<SseEmitter> sessionEmitters = emitters.get(sessionId);
        if (sessionEmitters != null) {
            sessionEmitters.remove(emitter);
        }
    }
}
