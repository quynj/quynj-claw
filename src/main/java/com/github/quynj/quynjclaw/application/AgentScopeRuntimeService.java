package com.github.quynj.quynjclaw.application;

import com.github.quynj.quynjclaw.agentscope.AgentFactory;
import com.github.quynj.quynjclaw.agentscope.AgentScopeMessageMapper;
import com.github.quynj.quynjclaw.agentscope.AgentSessionStore;
import com.github.quynj.quynjclaw.dto.AgentMessageDTO;
import com.github.quynj.quynjclaw.dto.ChatResponse;
import com.github.quynj.quynjclaw.dto.ChatSessionDTO;
import com.github.quynj.quynjclaw.dto.SendMessageRequest;
import com.github.quynj.quynjclaw.dto.TraceSpanDTO;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.Event;
import io.agentscope.core.agent.EventType;
import io.agentscope.core.agent.StreamOptions;
import io.agentscope.core.message.Msg;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AgentScopeRuntimeService {
    private final ConversationService conversationService;
    private final MessageProjectionService messageProjectionService;
    private final RealtimeEventService realtimeEventService;
    private final TraceService traceService;
    private final ActiveRunRegistry activeRunRegistry;
    private final AgentFactory agentFactory;
    private final AgentSessionStore agentSessionStore;
    private final AgentScopeMessageMapper messageMapper;

    public AgentScopeRuntimeService(ConversationService conversationService,
                                    MessageProjectionService messageProjectionService,
                                    RealtimeEventService realtimeEventService,
                                    TraceService traceService,
                                    ActiveRunRegistry activeRunRegistry,
                                    AgentFactory agentFactory,
                                    AgentSessionStore agentSessionStore,
                                    AgentScopeMessageMapper messageMapper) {
        this.conversationService = conversationService;
        this.messageProjectionService = messageProjectionService;
        this.realtimeEventService = realtimeEventService;
        this.traceService = traceService;
        this.activeRunRegistry = activeRunRegistry;
        this.agentFactory = agentFactory;
        this.agentSessionStore = agentSessionStore;
        this.messageMapper = messageMapper;
    }

    public ChatResponse chat(String sessionId, SendMessageRequest request) {
        ChatSessionDTO session = conversationService.get(sessionId);
        ActiveRunRegistry.ActiveRun run = activeRunRegistry.start(sessionId);
        boolean userAppended = false;
        long start = System.currentTimeMillis();
        TraceSpanDTO rootSpan = traceService.begin(sessionId, null, "Chat request", "workflow",
                chatInput(request), traceMetadata(session));
        try {
            AgentMessageDTO userProjection = messageMapper.toUserProjection(sessionId, request);
            messageProjectionService.append(sessionId, userProjection);
            userAppended = true;
            traceService.recordSuccess(sessionId, rootSpan.id, "Persist user message", "message",
                    Map.of("messageId", userProjection.id), Map.of("role", userProjection.role), null);
            realtimeEventService.publishMessageCreated(sessionId, userProjection);
            ChatSessionDTO running = conversationService.markRunning(sessionId);
            realtimeEventService.publishSessionUpdated(sessionId, running);

            ReActAgent agent = agentFactory.createAgent(session);
            run.attach(agent);
            traceService.recordSuccess(sessionId, rootSpan.id, "Create AgentScope agent", "agent",
                    null, Map.of("agentName", session.agentName, "modelName", session.modelName), null);
            agentSessionStore.loadIfExists(agent, session.agentscopeSessionId);
            traceService.recordSuccess(sessionId, rootSpan.id, "Load AgentScope session", "memory",
                    Map.of("agentscopeSessionId", session.agentscopeSessionId), Map.of("loaded", true), null);
            if (request.stream) {
                return streamChat(sessionId, session, request, agent, run, start, rootSpan);
            }
            return blockingChat(sessionId, session, request, agent, start, rootSpan);
        } catch (RuntimeException e) {
            if (run.isCancelRequested() && userAppended) {
                return cancelledChat(sessionId, session, run, start, rootSpan);
            }
            if (!userAppended) {
                traceService.finishError(rootSpan, errorOutput(e), null);
                throw e;
            }
            long durationMs = System.currentTimeMillis() - start;
            AgentMessageDTO error = messageMapper.toErrorProjection(sessionId, e.getMessage());
            messageProjectionService.append(sessionId, error);
            ChatSessionDTO errored = conversationService.markError(sessionId, error, durationMs, 2);
            traceService.finishError(rootSpan, errorOutput(e), Map.of("messageId", error.id));
            realtimeEventService.publishError(sessionId, "Agent 调用失败", e.getMessage());
            realtimeEventService.publishMessageCreated(sessionId, error);
            realtimeEventService.publishSessionUpdated(sessionId, errored);
            throw e;
        } finally {
            activeRunRegistry.finish(sessionId, run);
        }
    }

    private ChatResponse blockingChat(String sessionId, ChatSessionDTO session, SendMessageRequest request,
                                      ReActAgent agent, long start, TraceSpanDTO rootSpan) {
        TraceSpanDTO callSpan = traceService.begin(sessionId, rootSpan.id, "AgentScope blocking call", "agent",
                chatInput(request), null);
        Msg assistantMsg;
        try {
            assistantMsg = agent.call(messageMapper.toUserMsg(sessionId, request)).block();
            traceService.finishSuccess(callSpan, Map.of("hasMessage", assistantMsg != null), null);
        } catch (RuntimeException e) {
            traceService.finishError(callSpan, errorOutput(e), null);
            throw e;
        }
        long durationMs = System.currentTimeMillis() - start;
        agentSessionStore.save(agent, session.agentscopeSessionId);
        traceService.recordSuccess(sessionId, rootSpan.id, "Save AgentScope session", "memory",
                Map.of("agentscopeSessionId", session.agentscopeSessionId), Map.of("saved", true), null);

        AgentMessageDTO assistantProjection =
                messageMapper.toAssistantProjection(sessionId, session.agentName, assistantMsg);
        messageProjectionService.append(sessionId, assistantProjection);
        ChatSessionDTO done = conversationService.markDone(sessionId, assistantProjection, durationMs, 2);
        traceService.finishSuccess(rootSpan, Map.of(
                "messageId", assistantProjection.id,
                "durationMs", durationMs
        ), null);
        realtimeEventService.publishMessageCreated(sessionId, assistantProjection);
        realtimeEventService.publishSessionUpdated(sessionId, done);
        return ChatResponse.of(assistantProjection);
    }

    private ChatResponse streamChat(String sessionId, ChatSessionDTO session, SendMessageRequest request,
                                    ReActAgent agent, ActiveRunRegistry.ActiveRun run, long start,
                                    TraceSpanDTO rootSpan) {
        Map<String, AgentMessageDTO> completed = new LinkedHashMap<>();
        AgentMessageDTO[] finalProjection = new AgentMessageDTO[1];
        TraceSpanDTO streamSpan = traceService.begin(sessionId, rootSpan.id, "AgentScope stream", "agent",
                chatInput(request), Map.of("incremental", false));
        StreamOptions options = StreamOptions.builder()
                .eventTypes(EventType.ALL, EventType.AGENT_RESULT)
                .incremental(false)
                .build();

        try {
            agent.stream(messageMapper.toUserMsg(sessionId, request), options)
                    .doOnNext(event -> handleStreamEvent(sessionId, session, event, run, completed, finalProjection, rootSpan))
                    .takeUntil(ignored -> run.isCancelRequested())
                    .blockLast();
        } catch (RuntimeException e) {
            traceService.finishError(streamSpan, errorOutput(e), null);
            throw e;
        }

        if (run.isCancelRequested()) {
            traceService.finishCancelled(streamSpan, Map.of("completedMessages", completed.size()), null);
            return cancelledChat(sessionId, session, run, start, rootSpan);
        }
        traceService.finishSuccess(streamSpan, Map.of("completedMessages", completed.size()), null);

        long durationMs = System.currentTimeMillis() - start;
        agentSessionStore.save(agent, session.agentscopeSessionId);
        traceService.recordSuccess(sessionId, rootSpan.id, "Save AgentScope session", "memory",
                Map.of("agentscopeSessionId", session.agentscopeSessionId), Map.of("saved", true), null);

        AgentMessageDTO assistantProjection = finalProjection[0];
        if (assistantProjection == null) {
            assistantProjection = completed.values().stream()
                    .reduce((first, second) -> second)
                    .orElseGet(() -> messageMapper.toAssistantProjection(sessionId, session.agentName, null));
        }

        int appended = appendCompletedMessages(sessionId, completed, assistantProjection);
        ChatSessionDTO done = conversationService.markDone(sessionId, assistantProjection, durationMs, 1 + appended);
        traceService.finishSuccess(rootSpan, Map.of(
                "messageId", assistantProjection.id,
                "durationMs", durationMs,
                "appendedMessages", appended
        ), null);
        realtimeEventService.publishSessionUpdated(sessionId, done);
        return ChatResponse.of(assistantProjection);
    }

    private void handleStreamEvent(String sessionId, ChatSessionDTO session, Event event,
                                   ActiveRunRegistry.ActiveRun run,
                                   Map<String, AgentMessageDTO> completed,
                                   AgentMessageDTO[] finalProjection,
                                   TraceSpanDTO rootSpan) {
        if (event == null || event.getMessage() == null) {
            return;
        }
        AgentMessageDTO projection = messageMapper.toEventProjection(sessionId, session.agentName, event.getMessage());
        if (event.getType() == EventType.AGENT_RESULT) {
            finalProjection[0] = projection;
            traceService.recordSuccess(sessionId, rootSpan.id, "Stream final result", "message",
                    Map.of("eventType", event.getType().name()),
                    Map.of("messageId", projection.id, "role", projection.role), null);
            return;
        }

        realtimeEventService.publishMessageDelta(sessionId, projection, event.getType().name(), event.isLast());
        run.remember(projection);
        if (event.isLast()) {
            completed.put(projection.id, projection);
            traceService.recordSuccess(sessionId, rootSpan.id, "Stream event", "message",
                    Map.of("eventType", event.getType().name()),
                    Map.of("messageId", projection.id, "last", true), null);
        }
    }

    private ChatResponse cancelledChat(String sessionId, ChatSessionDTO session,
                                       ActiveRunRegistry.ActiveRun run, long start, TraceSpanDTO rootSpan) {
        long durationMs = System.currentTimeMillis() - start;
        AgentMessageDTO partial = messageMapper.toCancelledProjection(sessionId, session.agentName, run.lastProjection());
        messageProjectionService.append(sessionId, partial);
        ChatSessionDTO stopped = conversationService.markStopped(sessionId, partial, durationMs, 2);
        traceService.finishCancelled(rootSpan, Map.of(
                "messageId", partial.id,
                "durationMs", durationMs
        ), null);
        realtimeEventService.publishMessageCreated(sessionId, partial);
        realtimeEventService.publishSessionUpdated(sessionId, stopped);
        return ChatResponse.of(partial);
    }

    private int appendCompletedMessages(String sessionId, Map<String, AgentMessageDTO> completed,
                                        AgentMessageDTO assistantProjection) {
        int appended = 0;
        for (AgentMessageDTO message : completed.values()) {
            if (messageMapper.sameRawMessage(message, assistantProjection)) {
                continue;
            }
            messageProjectionService.append(sessionId, message);
            realtimeEventService.publishMessageCreated(sessionId, message);
            appended += 1;
        }
        messageProjectionService.append(sessionId, assistantProjection);
        realtimeEventService.publishMessageCreated(sessionId, assistantProjection);
        return appended + 1;
    }

    private Map<String, Object> chatInput(SendMessageRequest request) {
        Map<String, Object> input = new LinkedHashMap<>();
        String text = request.text == null ? "" : request.text;
        input.put("textLength", text.length());
        input.put("stream", request.stream);
        input.put("attachmentCount", request.attachments == null ? 0 : request.attachments.size());
        return input;
    }

    private Map<String, Object> traceMetadata(ChatSessionDTO session) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("agentName", session.agentName);
        metadata.put("modelName", session.modelName);
        metadata.put("agentscopeSessionId", session.agentscopeSessionId);
        return metadata;
    }

    private Map<String, Object> errorOutput(RuntimeException e) {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("type", e.getClass().getSimpleName());
        output.put("message", e.getMessage());
        return output;
    }
}
