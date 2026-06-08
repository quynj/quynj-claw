package com.github.quynj.quynjclaw.application;

import com.github.quynj.quynjclaw.common.PageResult;
import com.github.quynj.quynjclaw.dto.AgentMessageDTO;
import com.github.quynj.quynjclaw.dto.ChatSessionDTO;
import com.github.quynj.quynjclaw.dto.SessionCreateRequest;
import com.github.quynj.quynjclaw.dto.SessionUpdateRequest;
import com.github.quynj.quynjclaw.store.LocalMessageStore;
import com.github.quynj.quynjclaw.store.LocalSessionStore;
import com.github.quynj.quynjclaw.store.LocalSummaryStore;
import com.github.quynj.quynjclaw.store.LocalTraceStore;
import org.springframework.stereotype.Service;

@Service
public class ConversationService {
    private final LocalSessionStore sessionStore;
    private final LocalMessageStore messageStore;
    private final LocalSummaryStore summaryStore;
    private final LocalTraceStore traceStore;
    private final SessionTitleGenerator titleGenerator;
    private static final String DEFAULT_TITLE = "New Chat";

    public ConversationService(LocalSessionStore sessionStore, LocalMessageStore messageStore,
                               LocalSummaryStore summaryStore, LocalTraceStore traceStore,
                               SessionTitleGenerator titleGenerator) {
        this.sessionStore = sessionStore;
        this.messageStore = messageStore;
        this.summaryStore = summaryStore;
        this.traceStore = traceStore;
        this.titleGenerator = titleGenerator;
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

    public ChatSessionDTO markDone(String sessionId, AgentMessageDTO message, long durationMs, int messageCountDelta) {
        ChatSessionDTO session = sessionStore.updateAfterMessage(sessionId, message, durationMs, messageCountDelta, "done");
        summaryStore.updateAfterSession(session);
        return session;
    }

    public ChatSessionDTO markError(String sessionId, AgentMessageDTO message, long durationMs, int messageCountDelta) {
        ChatSessionDTO session = sessionStore.updateAfterMessage(sessionId, message, durationMs, messageCountDelta, "error");
        summaryStore.updateAfterSession(session);
        return session;
    }

    public ChatSessionDTO markStopped(String sessionId, AgentMessageDTO message, long durationMs, int messageCountDelta) {
        ChatSessionDTO session = sessionStore.updateAfterMessage(sessionId, message, durationMs, messageCountDelta, "stopped");
        summaryStore.updateAfterSession(session);
        return session;
    }

    public ChatSessionDTO updateTitleIfNeeded(String sessionId, AgentMessageDTO userMessage) {
        ChatSessionDTO session = sessionStore.get(sessionId);
        if (DEFAULT_TITLE.equals(session.title)) {
            String newTitle = titleGenerator.generateFromUserMessage(userMessage);
            session = sessionStore.updateTitle(sessionId, newTitle);
            summaryStore.updateAfterSession(session);
        }
        return session;
    }
}
