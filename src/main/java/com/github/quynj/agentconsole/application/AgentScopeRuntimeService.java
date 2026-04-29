package com.github.quynj.agentconsole.application;

import com.github.quynj.agentconsole.agentscope.AgentFactory;
import com.github.quynj.agentconsole.agentscope.AgentScopeMessageMapper;
import com.github.quynj.agentconsole.agentscope.AgentSessionStore;
import com.github.quynj.agentconsole.dto.AgentMessageDTO;
import com.github.quynj.agentconsole.dto.ChatResponse;
import com.github.quynj.agentconsole.dto.ChatSessionDTO;
import com.github.quynj.agentconsole.dto.SendMessageRequest;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import org.springframework.stereotype.Service;

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
        ChatSessionDTO running = conversationService.markRunning(sessionId);
        realtimeEventService.publishSessionUpdated(sessionId, running);

        long start = System.currentTimeMillis();
        try {
            ReActAgent agent = agentFactory.createAgent(session);
            agentSessionStore.loadIfExists(agent, session.agentscopeSessionId);
            Msg assistantMsg = agent.call(messageMapper.toUserMsg(request)).block();
            long durationMs = System.currentTimeMillis() - start;
            agentSessionStore.save(agent, session.agentscopeSessionId);

            AgentMessageDTO assistantProjection =
                    messageMapper.toAssistantProjection(sessionId, session.agentName, assistantMsg);
            messageProjectionService.append(sessionId, assistantProjection);
            ChatSessionDTO done = conversationService.markDone(sessionId, assistantProjection, durationMs);
            realtimeEventService.publishMessageCreated(sessionId, assistantProjection);
            realtimeEventService.publishSessionUpdated(sessionId, done);
            return ChatResponse.of(assistantProjection);
        } catch (RuntimeException e) {
            AgentMessageDTO error = messageMapper.toErrorProjection(sessionId, e.getMessage());
            messageProjectionService.append(sessionId, error);
            ChatSessionDTO errored = conversationService.markError(sessionId);
            realtimeEventService.publishError(sessionId, "Agent 调用失败", e.getMessage());
            realtimeEventService.publishMessageCreated(sessionId, error);
            realtimeEventService.publishSessionUpdated(sessionId, errored);
            throw e;
        }
    }
}
