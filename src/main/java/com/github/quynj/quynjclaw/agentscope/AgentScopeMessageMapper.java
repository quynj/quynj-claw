package com.github.quynj.quynjclaw.agentscope;

import com.github.quynj.quynjclaw.application.LocalFileService;
import com.github.quynj.quynjclaw.common.IdGenerator;
import com.github.quynj.quynjclaw.dto.AgentMessageDTO;
import com.github.quynj.quynjclaw.dto.ContentBlockDTO;
import com.github.quynj.quynjclaw.dto.MessageAttachmentDTO;
import com.github.quynj.quynjclaw.dto.SendMessageRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.ImageBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Component
public class AgentScopeMessageMapper {
    private final ObjectMapper mapper;
    private final LocalFileService fileService;

    public AgentScopeMessageMapper(ObjectMapper mapper, LocalFileService fileService) {
        this.mapper = mapper;
        this.fileService = fileService;
    }

    public Msg toUserMsg(String sessionId, SendMessageRequest request) {
        List<ContentBlock> blocks = new ArrayList<>();
        if (request.text != null && !request.text.isBlank()) {
            blocks.add(TextBlock.builder().text(request.text).build());
        }
        for (MessageAttachmentDTO attachment : safeAttachments(request)) {
            requireImageAttachment(attachment);
            blocks.add(ImageBlock.builder()
                    .source(fileService.base64Source(sessionId, attachment))
                    .build());
        }
        return Msg.builder()
                .name("user")
                .role(MsgRole.USER)
                .content(blocks)
                .build();
    }

    public AgentMessageDTO toUserProjection(String sessionId, SendMessageRequest request) {
        AgentMessageDTO message = base(sessionId, "user", "user");
        List<ContentBlockDTO> blocks = new ArrayList<>();
        if (request.text != null && !request.text.isBlank()) {
            blocks.add(ContentBlockDTO.text(request.text));
        }
        for (MessageAttachmentDTO attachment : safeAttachments(request)) {
            requireImageAttachment(attachment);
            Map<String, Object> source = new LinkedHashMap<>();
            source.put("id", attachment.id);
            source.put("contentType", attachment.contentType);
            source.put("fileName", attachment.fileName);
            source.put("size", attachment.size);
            blocks.add(ContentBlockDTO.image(attachment.url, attachment.fileName, source));
        }
        message.content = blocks;
        return message;
    }

    public AgentMessageDTO toAssistantProjection(String sessionId, String agentName, Msg msg) {
        AgentMessageDTO message = base(sessionId, agentName, roleOf(msg, "assistant"));
        String text = msg == null ? "" : msg.getTextContent();
        message.rawMsg = safeRaw(msg);
        message.metadata = metadataOf(message.rawMsg);
        message.content = contentOf(message.rawMsg, text == null ? "" : text);
        return message;
    }

    public AgentMessageDTO toEventProjection(String sessionId, String fallbackName, Msg msg) {
        String name = msg == null || msg.getName() == null || msg.getName().isBlank() ? fallbackName : msg.getName();
        AgentMessageDTO message = base(sessionId, name, roleOf(msg, "assistant"));
        if (msg != null && msg.getId() != null && !msg.getId().isBlank()) {
            message.id = msg.getId();
        }
        String text = msg == null ? "" : msg.getTextContent();
        message.rawMsg = safeRaw(msg);
        message.metadata = metadataOf(message.rawMsg);
        message.content = contentOf(message.rawMsg, text == null ? "" : text);
        return message;
    }

    public AgentMessageDTO toErrorProjection(String sessionId, String detail) {
        AgentMessageDTO message = base(sessionId, "system", "system");
        message.content = List.of(ContentBlockDTO.error("Agent 调用失败", detail));
        return message;
    }

    public AgentMessageDTO toCancelledProjection(String sessionId, String agentName, AgentMessageDTO partial) {
        AgentMessageDTO message = partial == null ? base(sessionId, agentName, "assistant") : partial;
        message.sessionId = sessionId;
        message.name = message.name == null || message.name.isBlank() ? agentName : message.name;
        message.role = "assistant";
        if (message.content == null || message.content.isEmpty() || onlyBlankText(message.content)) {
            message.content = List.of(ContentBlockDTO.text("回复已取消。"));
        }
        if (message.metadata == null) {
            message.metadata = new LinkedHashMap<>();
        }
        message.metadata.put("status", "cancelled");
        message.metadata.put("incomplete", true);
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

    private Map<String, Object> metadataOf(JsonNode raw) {
        if (raw == null || !raw.hasNonNull("metadata") || !raw.get("metadata").isObject()) {
            return new LinkedHashMap<>();
        }
        return mapper.convertValue(raw.get("metadata"), new TypeReference<LinkedHashMap<String, Object>>() {});
    }

    private List<ContentBlockDTO> contentOf(JsonNode raw, String fallbackText) {
        if (raw == null || !raw.has("content") || !raw.get("content").isArray()) {
            return List.of(ContentBlockDTO.text(fallbackText));
        }

        List<ContentBlockDTO> blocks = new ArrayList<>();
        for (JsonNode node : raw.get("content")) {
            ContentBlockDTO block = contentBlockOf(node);
            if (block != null) {
                blocks.add(block);
            }
        }
        if (blocks.isEmpty()) {
            blocks.add(ContentBlockDTO.text(fallbackText));
        }
        return blocks;
    }

    private ContentBlockDTO contentBlockOf(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        String type = textValue(node, "type", "raw");
        ContentBlockDTO block = new ContentBlockDTO();
        block.type = type;
        block.metadata = objectValue(node, "metadata");

        switch (type) {
            case "text" -> block.text = textValue(node, "text", "");
            case "thinking" -> block.thinking = textValue(node, "thinking", "");
            case "tool_use" -> {
                block.id = textValue(node, "id", null);
                block.name = textValue(node, "name", null);
                block.input = objectValue(node, "input");
                block.content = textValue(node, "content", null);
            }
            case "tool_result" -> {
                block.toolUseId = textValue(node, "id", null);
                block.id = block.toolUseId;
                block.name = textValue(node, "name", null);
                block.output = valueOf(node.get("output"));
                block.isError = booleanValue(node, "isError");
            }
            case "image", "audio", "video" -> {
                block.source = valueOf(node.get("source"));
                block.url = sourceUrl(node.get("source"));
            }
            default -> {
                block.output = valueOf(node);
            }
        }
        return block;
    }

    private String textValue(JsonNode node, String field, String fallback) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? fallback : value.asText();
    }

    private Boolean booleanValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asBoolean();
    }

    private Map<String, Object> objectValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || !value.isObject()) {
            return null;
        }
        return mapper.convertValue(value, new TypeReference<LinkedHashMap<String, Object>>() {});
    }

    private Object valueOf(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return mapper.convertValue(node, Object.class);
    }

    private List<MessageAttachmentDTO> safeAttachments(SendMessageRequest request) {
        return request.attachments == null ? List.of() : request.attachments;
    }

    private void requireImageAttachment(MessageAttachmentDTO attachment) {
        if (attachment == null || attachment.type == null || !"image".equals(attachment.type)) {
            throw new IllegalArgumentException("Only image attachments are supported.");
        }
    }

    private boolean onlyBlankText(List<ContentBlockDTO> blocks) {
        return blocks.stream().allMatch(block -> {
            if (block == null) {
                return true;
            }
            if ("text".equals(block.type)) {
                return block.text == null || block.text.isBlank();
            }
            return false;
        });
    }

    private String sourceUrl(JsonNode source) {
        if (source == null || !source.isObject()) {
            return null;
        }
        for (String field : List.of("url", "uri", "data")) {
            if (source.hasNonNull(field)) {
                return source.get(field).asText();
            }
        }
        return null;
    }

    public boolean sameRawMessage(AgentMessageDTO left, AgentMessageDTO right) {
        if (left == null || right == null || left.rawMsg == null || right.rawMsg == null) {
            return false;
        }
        JsonNode leftId = left.rawMsg.get("id");
        JsonNode rightId = right.rawMsg.get("id");
        return leftId != null && rightId != null && Objects.equals(leftId.asText(), rightId.asText());
    }
}
