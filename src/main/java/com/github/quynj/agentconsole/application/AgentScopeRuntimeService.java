package com.github.quynj.agentconsole.application;

import com.github.quynj.agentconsole.agentscope.AgentFactory;
import com.github.quynj.agentconsole.agentscope.AgentScopeMessageMapper;
import com.github.quynj.agentconsole.agentscope.AgentSessionStore;
import com.github.quynj.agentconsole.dto.AgentMessageDTO;
import com.github.quynj.agentconsole.dto.ChatResponse;
import com.github.quynj.agentconsole.dto.ChatSessionDTO;
import com.github.quynj.agentconsole.dto.SendMessageRequest;
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
    private final AgentFactory agentFactory;
    private final AgentSessionStore agentSessionStore;
    private final AgentScopeMessageMapper messageMapper;

    public AgentScopeRuntimeService(ConversationService conversationService,
                                    MessageProjectionService messageProjectionService,
                                    RealtimeEventService realtimeEventService,
                                    AgentFactory agentFactory,
                                    AgentSessionStore agentSessionStore,
                                    AgentScopeMessageMapper messageMapper) {
        this.conversationService = conversationService;
        this.messageProjectionService = messageProjectionService;
        this.realtimeEventService = realtimeEventService;
        this.agentFactory = agentFactory;
        this.agentSessionStore = agentSessionStore;
        this.messageMapper = messageMapper;
    }

    public ChatResponse chat(String sessionId, SendMessageRequest request) {
        ChatSessionDTO session = conversationService.get(sessionId);
        AgentMessageDTO userProjection = messageMapper.toUserProjection(sessionId, request);
        messageProjectionService.append(sessionId, userProjection);
        realtimeEventService.publishMessageCreated(sessionId, userProjection);
        ChatSessionDTO running = conversationService.markRunning(sessionId);
        realtimeEventService.publishSessionUpdated(sessionId, running);

        long start = System.currentTimeMillis();
        try {
            ReActAgent agent = agentFactory.createAgent(session);
            agentSessionStore.loadIfExists(agent, session.agentscopeSessionId);
            if (request.stream) {
                return streamChat(sessionId, session, request, agent, start);
            }
            return blockingChat(sessionId, session, request, agent, start);
        } catch (RuntimeException e) {
            long durationMs = System.currentTimeMillis() - start;
            AgentMessageDTO error = messageMapper.toErrorProjection(sessionId, e.getMessage());
            messageProjectionService.append(sessionId, error);
            ChatSessionDTO errored = conversationService.markError(sessionId, error, durationMs, 2);
            realtimeEventService.publishError(sessionId, "Agent 调用失败", e.getMessage());
            realtimeEventService.publishMessageCreated(sessionId, error);
            realtimeEventService.publishSessionUpdated(sessionId, errored);
            throw e;
        }
    }

    private ChatResponse blockingChat(String sessionId, ChatSessionDTO session, SendMessageRequest request,
                                      ReActAgent agent, long start) {
        Msg assistantMsg = agent.call(messageMapper.toUserMsg(request)).block();
        long durationMs = System.currentTimeMillis() - start;
        agentSessionStore.save(agent, session.agentscopeSessionId);

        AgentMessageDTO assistantProjection =
                messageMapper.toAssistantProjection(sessionId, session.agentName, assistantMsg);
        messageProjectionService.append(sessionId, assistantProjection);
        ChatSessionDTO done = conversationService.markDone(sessionId, assistantProjection, durationMs, 2);
        realtimeEventService.publishMessageCreated(sessionId, assistantProjection);
        realtimeEventService.publishSessionUpdated(sessionId, done);
        return ChatResponse.of(assistantProjection);
    }

    private ChatResponse streamChat(String sessionId, ChatSessionDTO session, SendMessageRequest request,
                                    ReActAgent agent, long start) {
        Map<String, AgentMessageDTO> completed = new LinkedHashMap<>();
        AgentMessageDTO[] finalProjection = new AgentMessageDTO[1];
        StreamOptions options = StreamOptions.builder()
                .eventTypes(EventType.ALL, EventType.AGENT_RESULT)
                .incremental(false)
                .build();

        agent.stream(messageMapper.toUserMsg(request), options)
                .doOnNext(event -> handleStreamEvent(sessionId, session, event, completed, finalProjection))
                .blockLast();

        long durationMs = System.currentTimeMillis() - start;
        agentSessionStore.save(agent, session.agentscopeSessionId);

        AgentMessageDTO assistantProjection = finalProjection[0];
        if (assistantProjection == null) {
            assistantProjection = completed.values().stream()
                    .reduce((first, second) -> second)
                    .orElseGet(() -> messageMapper.toAssistantProjection(sessionId, session.agentName, null));
        }

        int appended = appendCompletedMessages(sessionId, completed, assistantProjection);
        ChatSessionDTO done = conversationService.markDone(sessionId, assistantProjection, durationMs, 1 + appended);
        realtimeEventService.publishSessionUpdated(sessionId, done);
        return ChatResponse.of(assistantProjection);
    }

    private void handleStreamEvent(String sessionId, ChatSessionDTO session, Event event,
                                   Map<String, AgentMessageDTO> completed,
                                   AgentMessageDTO[] finalProjection) {
        if (event == null || event.getMessage() == null) {
            return;
        }
        AgentMessageDTO projection = messageMapper.toEventProjection(sessionId, session.agentName, event.getMessage());
        if (event.getType() == EventType.AGENT_RESULT) {
            finalProjection[0] = projection;
            return;
        }

        realtimeEventService.publishMessageDelta(sessionId, projection, event.getType().name(), event.isLast());
        if (event.isLast()) {
            completed.put(projection.id, projection);
        }
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
}
