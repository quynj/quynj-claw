package com.github.quynj.agentconsole.application;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.quynj.agentconsole.config.AgentConsoleProperties;
import com.github.quynj.agentconsole.dto.AgentMessageDTO;
import com.github.quynj.agentconsole.dto.ContentBlockDTO;
import com.github.quynj.agentconsole.dto.SessionCreateRequest;
import com.github.quynj.agentconsole.store.LocalMessageStore;
import com.github.quynj.agentconsole.store.LocalSessionStore;
import com.github.quynj.agentconsole.store.LocalSummaryStore;
import com.github.quynj.agentconsole.store.LocalTraceStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConversationServiceSmokeTest {
    @TempDir
    Path tempDir;

    private ConversationService conversationService;
    private LocalMessageStore messageStore;
    private LocalSummaryStore summaryStore;
    private Path uiStorePath;

    @BeforeEach
    void setUp() throws Exception {
        uiStorePath = tempDir.resolve("ui-store");
        AgentConsoleProperties properties = new AgentConsoleProperties();
        properties.uiStorePath = uiStorePath.toString();
        properties.defaultAgentName = "Sunday";
        properties.defaultSystemPrompt = "You are a helpful assistant.";

        var mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        LocalSessionStore sessionStore = new LocalSessionStore(properties, mapper);
        messageStore = new LocalMessageStore(properties, mapper);
        summaryStore = new LocalSummaryStore(properties, mapper);
        LocalTraceStore traceStore = new LocalTraceStore(properties, mapper);

        sessionStore.init();
        messageStore.init();
        summaryStore.init();
        traceStore.init();
        conversationService = new ConversationService(sessionStore, messageStore, summaryStore, traceStore);
    }

    @Test
    void createMessageSummaryAndDeleteLifecycle() {
        var session = conversationService.create(new SessionCreateRequest());

        messageStore.append(session.id, message(session.id, "user", "hello"));
        AgentMessageDTO assistant = message(session.id, "assistant", "hi");
        messageStore.append(session.id, assistant);
        var done = conversationService.markDone(session.id, assistant, 123, 2);

        assertEquals(2, done.messageCount);
        assertEquals(123, done.durationMs);
        assertEquals("hi", done.lastMessagePreview);
        assertEquals(2, summaryStore.get(session.id).messageCount);
        assertEquals(2, messageStore.listMessages(session.id).size());

        conversationService.delete(session.id);

        assertFalse(Files.exists(uiStorePath.resolve("messages").resolve(session.id + ".json")));
        assertFalse(Files.exists(uiStorePath.resolve("summaries").resolve(session.id + ".json")));
        assertFalse(Files.exists(uiStorePath.resolve("traces").resolve(session.id + ".json")));
    }

    @Test
    void failedTurnCountsUserAndErrorMessages() {
        var session = conversationService.create(new SessionCreateRequest());

        messageStore.append(session.id, message(session.id, "user", "break"));
        AgentMessageDTO error = errorMessage(session.id, "Agent failed");
        messageStore.append(session.id, error);
        var errored = conversationService.markError(session.id, error, 55, 2);

        assertEquals("error", errored.status);
        assertEquals(2, errored.messageCount);
        assertEquals(55, errored.durationMs);
        assertEquals("Agent failed", errored.lastMessagePreview);
        assertEquals(2, summaryStore.get(session.id).messageCount);
        assertTrue(Files.exists(uiStorePath.resolve("messages").resolve(session.id + ".json")));
    }

    private AgentMessageDTO message(String sessionId, String role, String text) {
        AgentMessageDTO message = new AgentMessageDTO();
        message.id = role + "-" + System.nanoTime();
        message.sessionId = sessionId;
        message.name = role;
        message.role = role;
        message.content.add(ContentBlockDTO.text(text));
        message.createdAt = LocalDateTime.now();
        return message;
    }

    private AgentMessageDTO errorMessage(String sessionId, String text) {
        AgentMessageDTO message = message(sessionId, "assistant", "");
        message.content.clear();
        message.content.add(ContentBlockDTO.error(text, null));
        return message;
    }
}
