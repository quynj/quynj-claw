package com.github.quynj.agentconsole.store;

import com.github.quynj.agentconsole.common.JsonFileUtils;
import com.github.quynj.agentconsole.config.AgentConsoleProperties;
import com.github.quynj.agentconsole.dto.AgentMessageDTO;
import com.github.quynj.agentconsole.dto.ChatSessionDTO;
import com.github.quynj.agentconsole.dto.SessionSummaryDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Repository
public class LocalSummaryStore {
    private final AgentConsoleProperties properties;
    private final JsonFileUtils json;
    private Path dir;

    public LocalSummaryStore(AgentConsoleProperties properties, ObjectMapper mapper) {
        this.properties = properties;
        this.json = new JsonFileUtils(mapper);
    }

    @PostConstruct
    public void init() throws IOException {
        dir = Path.of(properties.uiStorePath, "summaries");
        Files.createDirectories(dir);
    }

    public synchronized SessionSummaryDTO get(String sessionId) {
        return json.read(path(sessionId), SessionSummaryDTO.class, null);
    }

    public synchronized void initFromSession(ChatSessionDTO session) {
        SessionSummaryDTO summary = new SessionSummaryDTO();
        summary.sessionId = session.id;
        summary.title = session.title;
        summary.agentName = session.agentName;
        summary.status = session.status;
        summary.messageCount = session.messageCount;
        summary.traceCount = session.traceCount;
        summary.totalTokens = session.totalTokens;
        summary.promptTokens = session.promptTokens;
        summary.completionTokens = session.completionTokens;
        summary.durationMs = session.durationMs;
        summary.createdAt = session.createdAt;
        summary.updatedAt = session.updatedAt;
        update(summary);
    }

    public synchronized void update(SessionSummaryDTO summary) {
        json.writeAtomic(path(summary.sessionId), summary);
    }

    public synchronized void updateAfterSession(ChatSessionDTO session) {
        SessionSummaryDTO summary = get(session.id);
        if (summary == null) {
            initFromSession(session);
            return;
        }
        summary.title = session.title;
        summary.agentName = session.agentName;
        summary.status = session.status;
        summary.messageCount = session.messageCount;
        summary.traceCount = session.traceCount;
        summary.totalTokens = session.totalTokens;
        summary.promptTokens = session.promptTokens;
        summary.completionTokens = session.completionTokens;
        summary.durationMs = session.durationMs;
        summary.updatedAt = session.updatedAt;
        update(summary);
    }

    public synchronized void updateAfterMessage(String sessionId, AgentMessageDTO message, long durationMs) {
        SessionSummaryDTO summary = get(sessionId);
        if (summary == null) {
            return;
        }
        summary.status = "done";
        summary.messageCount += 1;
        summary.durationMs += durationMs;
        summary.updatedAt = message.createdAt;
        update(summary);
    }

    public synchronized void delete(String sessionId) {
        try {
            Files.deleteIfExists(path(sessionId));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete summary for " + sessionId, e);
        }
    }

    private Path path(String sessionId) {
        return dir.resolve(sessionId + ".json");
    }
}
