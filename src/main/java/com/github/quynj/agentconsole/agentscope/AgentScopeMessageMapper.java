package com.github.quynj.agentconsole.agentscope;

import com.github.quynj.agentconsole.common.IdGenerator;
import com.github.quynj.agentconsole.dto.AgentMessageDTO;
import com.github.quynj.agentconsole.dto.ContentBlockDTO;
import com.github.quynj.agentconsole.dto.SendMessageRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Component
public class AgentScopeMessageMapper {
    private final ObjectMapper mapper;

    public AgentScopeMessageMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public Msg toUserMsg(SendMessageRequest request) {
        return Msg.builder()
                .name("user")
                .role(MsgRole.USER)
                .textContent(request.text)
                .build();
    }

    public AgentMessageDTO toUserProjection(String sessionId, SendMessageRequest request) {
        AgentMessageDTO message = base(sessionId, "user", "user");
        message.content = List.of(ContentBlockDTO.text(request.text));
        return message;
    }

    public AgentMessageDTO toAssistantProjection(String sessionId, String agentName, Msg msg) {
        AgentMessageDTO message = base(sessionId, agentName, roleOf(msg, "assistant"));
        String text = msg == null ? "" : msg.getTextContent();
        message.content = List.of(ContentBlockDTO.text(text == null ? "" : text));
        message.rawMsg = safeRaw(msg);
        return message;
    }

    public AgentMessageDTO toErrorProjection(String sessionId, String detail) {
        AgentMessageDTO message = base(sessionId, "system", "system");
        message.content = List.of(ContentBlockDTO.error("Agent 调用失败", detail));
        return message;
    }

    private AgentMessageDTO base(String sessionId, String name, String role) {
        AgentMessageDTO message = new AgentMessageDTO();
        message.id = IdGenerator.messageId();
        message.sessionId = sessionId;
        message.name = name;
        message.role = role;
        message.createdAt = LocalDateTime.now();
        return message;
    }

    private String roleOf(Msg msg, String fallback) {
        if (msg == null || msg.getRole() == null) {
            return fallback;
        }
        return msg.getRole().name().toLowerCase(Locale.ROOT);
    }

    private JsonNode safeRaw(Msg msg) {
        if (msg == null) {
            return mapper.createObjectNode();
        }
        try {
            return mapper.valueToTree(msg);
        } catch (IllegalArgumentException e) {
            return mapper.createObjectNode().put("text", msg.getTextContent());
        }
    }
}
