package com.github.quynj.quynjclaw.application;

import com.github.quynj.quynjclaw.dto.AgentMessageDTO;
import io.agentscope.core.ReActAgent;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ActiveRunRegistry {
    private final Map<String, ActiveRun> runs = new ConcurrentHashMap<>();

    public ActiveRun start(String sessionId) {
        ActiveRun run = new ActiveRun(sessionId);
        ActiveRun existing = runs.putIfAbsent(sessionId, run);
        if (existing != null) {
            throw new IllegalStateException("Session is already running: " + sessionId);
        }
        return run;
    }

    public boolean cancel(String sessionId) {
        ActiveRun run = runs.get(sessionId);
        if (run == null) {
            return false;
        }
        run.cancel();
        return true;
    }

    public void finish(String sessionId, ActiveRun run) {
        runs.remove(sessionId, run);
    }

    public static class ActiveRun {
        private final String sessionId;
        private final AtomicBoolean cancelRequested = new AtomicBoolean(false);
        private final AtomicReference<AgentMessageDTO> lastProjection = new AtomicReference<>();
        private final AtomicReference<ReActAgent> agent = new AtomicReference<>();

        private ActiveRun(String sessionId) {
            this.sessionId = sessionId;
        }

        public String sessionId() {
            return sessionId;
        }

        public void attach(ReActAgent value) {
            agent.set(value);
            if (cancelRequested.get() && value != null) {
                value.interrupt();
            }
        }

        public void remember(AgentMessageDTO message) {
            if (message != null) {
                lastProjection.set(message);
            }
        }

        public AgentMessageDTO lastProjection() {
            return lastProjection.get();
        }

        public boolean isCancelRequested() {
            return cancelRequested.get();
        }

        public void cancel() {
            cancelRequested.set(true);
            ReActAgent current = agent.get();
            if (current != null) {
                current.interrupt();
            }
        }
    }
}
