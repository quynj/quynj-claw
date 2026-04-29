package com.github.quynj.agentconsole.application;

import com.github.quynj.agentconsole.common.PageResult;
import com.github.quynj.agentconsole.dto.AgentMessageDTO;
import com.github.quynj.agentconsole.dto.ChatSessionDTO;
import com.github.quynj.agentconsole.dto.SessionCreateRequest;
import com.github.quynj.agentconsole.dto.SessionUpdateRequest;
import com.github.quynj.agentconsole.store.LocalMessageStore;
import com.github.quynj.agentconsole.store.LocalSessionStore;
import com.github.quynj.agentconsole.store.LocalSummaryStore;
import com.github.quynj.agentconsole.store.LocalTraceStore;
import org.springframework.stereotype.Service;

@Service
public class ConversationService {
    private final LocalSessionStore sessionStore;
    private final LocalMessageStore messageStore;
    private final LocalSummaryStore summaryStore;
    private final LocalTraceStore traceStore;

    public ConversationService(LocalSessionStore sessionStore, LocalMessageStore messageStore,
                               LocalSummaryStore summaryStore, LocalTraceStore traceStore) {
        this.sessionStore = sessionStore;
        this.messageStore = messageStore;
        this.summaryStore = summaryStore;
        this.traceStore = traceStore;
    }

    public PageResult<ChatSessionDTO> list(String keyword, String status, int page, int pageSize) {
        return sessionStore.list(keyword, status, page, pageSize);
    }

    public ChatSessionDTO get(String sessionId) {
        return sessionStore.get(sessionId);
    }

    public ChatSessionDTO create(SessionCreateRequest request) {
        ChatSessionDTO session = sessionStore.create(request);
        messageStore.initSession(session.id);
        summaryStore.initFromSession(session);
        traceStore.initSession(session.id);
        return session;
    }

    public ChatSessionDTO update(String sessionId, SessionUpdateRequest request) {
        ChatSessionDTO session = sessionStore.update(sessionId, request);
        summaryStore.updateAfterSession(session);
        return session;
    }

    public void delete(String sessionId) {
        sessionStore.delete(sessionId);
        messageStore.deleteBySessionId(sessionId);
        summaryStore.delete(sessionId);
        traceStore.deleteBySessionId(sessionId);
    }

    public ChatSessionDTO markRunning(String sessionId) {
        ChatSessionDTO session = sessionStore.updateStatus(sessionId, "running");
        summaryStore.updateAfterSession(session);
        return session;
    }

    public ChatSessionDTO markDone(String sessionId, AgentMessageDTO message, long durationMs) {
        ChatSessionDTO session = sessionStore.updateAfterMessage(sessionId, message, durationMs);
        summaryStore.updateAfterSession(session);
        return session;
    }

    public ChatSessionDTO markError(String sessionId) {
        ChatSessionDTO session = sessionStore.updateStatus(sessionId, "error");
        summaryStore.updateAfterSession(session);
        return session;
    }
}
