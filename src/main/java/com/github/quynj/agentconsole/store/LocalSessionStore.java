package com.github.quynj.agentconsole.store;

import com.github.quynj.agentconsole.common.IdGenerator;
import com.github.quynj.agentconsole.common.JsonFileUtils;
import com.github.quynj.agentconsole.common.PageResult;
import com.github.quynj.agentconsole.config.AgentConsoleProperties;
import com.github.quynj.agentconsole.dto.AgentMessageDTO;
import com.github.quynj.agentconsole.dto.ChatSessionDTO;
import com.github.quynj.agentconsole.dto.SessionCreateRequest;
import com.github.quynj.agentconsole.dto.SessionUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Repository
public class LocalSessionStore {
    private final AgentConsoleProperties properties;
    private final JsonFileUtils json;
    private Path sessionsFile;

    public LocalSessionStore(AgentConsoleProperties properties, ObjectMapper mapper) {
        this.properties = properties;
        this.json = new JsonFileUtils(mapper);
    }

    @PostConstruct
    public synchronized void init() {
        sessionsFile = Path.of(properties.uiStorePath, "sessions.json");
        Document doc = json.read(sessionsFile, Document.class, new Document());
        if (doc.items == null) {
            doc.items = new ArrayList<>();
        }
        json.writeAtomic(sessionsFile, doc);
    }

    public synchronized PageResult<ChatSessionDTO> list(String keyword, String status, int page, int pageSize) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        String normalizedStatus = status == null ? "" : status.trim();
        List<ChatSessionDTO> filtered = read().items.stream()
                .filter(item -> item.deletedAt == null)
                .filter(item -> normalizedKeyword.isEmpty()
                        || nullSafe(item.title).toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || nullSafe(item.lastMessagePreview).toLowerCase(Locale.ROOT).contains(normalizedKeyword))
                .filter(item -> normalizedStatus.isEmpty() || normalizedStatus.equals(item.status))
                .sorted(Comparator.comparing((ChatSessionDTO item) -> item.updatedAt).reversed())
                .toList();
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(Math.min(pageSize, 100), 1);
        int from = Math.min((safePage - 1) * safePageSize, filtered.size());
        int to = Math.min(from + safePageSize, filtered.size());
        return new PageResult<>(filtered.subList(from, to), filtered.size(), safePage, safePageSize);
    }

    public synchronized ChatSessionDTO get(String sessionId) {
        return read().items.stream()
                .filter(item -> Objects.equals(item.id, sessionId) && item.deletedAt == null)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    }

    public synchronized ChatSessionDTO create(SessionCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        ChatSessionDTO session = new ChatSessionDTO();
        session.id = IdGenerator.sessionId();
        session.title = blankToDefault(request.title, "New Chat");
        session.agentName = blankToDefault(request.agentName, properties.defaultAgentName);
        session.agentscopeSessionId = session.id;
        session.status = "idle";
        session.modelName = blankToDefault(request.modelName, properties.model.name);
        session.systemPrompt = blankToDefault(request.systemPrompt, properties.defaultSystemPrompt);
        session.temperature = request.temperature;
        session.createdAt = now;
        session.updatedAt = now;
        Document doc = read();
        doc.items.add(session);
        write(doc);
        return session;
    }

    public synchronized ChatSessionDTO update(String sessionId, SessionUpdateRequest request) {
        Document doc = read();
        ChatSessionDTO session = doc.items.stream()
                .filter(item -> Objects.equals(item.id, sessionId) && item.deletedAt == null)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        if (request.title != null) {
            session.title = request.title;
        }
        if (request.agentName != null) {
            session.agentName = request.agentName;
        }
        if (request.modelName != null) {
            session.modelName = request.modelName;
        }
        if (request.systemPrompt != null) {
            session.systemPrompt = request.systemPrompt;
        }
        session.updatedAt = LocalDateTime.now();
        write(doc);
        return session;
    }

    public synchronized ChatSessionDTO updateStatus(String sessionId, String status) {
        Document doc = read();
        ChatSessionDTO session = findMutable(doc, sessionId);
        session.status = status;
        session.updatedAt = LocalDateTime.now();
        write(doc);
        return session;
    }

    public synchronized ChatSessionDTO updateAfterMessage(String sessionId, AgentMessageDTO message, long durationMs) {
        Document doc = read();
        ChatSessionDTO session = findMutable(doc, sessionId);
        session.status = "done";
        session.messageCount = Math.max(session.messageCount, 0) + 1;
        session.durationMs += durationMs;
        session.lastMessagePreview = preview(message);
        session.updatedAt = LocalDateTime.now();
        write(doc);
        return session;
    }

    public synchronized void delete(String sessionId) {
        Document doc = read();
        doc.items.removeIf(item -> Objects.equals(item.id, sessionId));
        write(doc);
    }

    private ChatSessionDTO findMutable(Document doc, String sessionId) {
        return doc.items.stream()
                .filter(item -> Objects.equals(item.id, sessionId) && item.deletedAt == null)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    }

    private Document read() {
        Document doc = json.read(sessionsFile, Document.class, new Document());
        if (doc.items == null) {
            doc.items = new ArrayList<>();
        }
        return doc;
    }

    private void write(Document doc) {
        json.writeAtomic(sessionsFile, doc);
    }

    private String preview(AgentMessageDTO message) {
        if (message == null || message.content == null || message.content.isEmpty()) {
            return "";
        }
        String text = message.content.get(0).text != null ? message.content.get(0).text : message.content.get(0).message;
        if (text == null) {
            return "";
        }
        return text.length() > 120 ? text.substring(0, 120) : text;
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    public static class Document {
        public List<ChatSessionDTO> items = new ArrayList<>();
    }
}
