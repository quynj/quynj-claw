package com.github.quynj.agentconsole.dto;

import java.time.LocalDateTime;

public class ChatSessionDTO {
    public String id;
    public String title;
    public String agentName;
    public String agentscopeSessionId;
    public String status;
    public String modelName;
    public String systemPrompt;
    public Double temperature;
    public int messageCount;
    public int traceCount;
    public int totalTokens;
    public int promptTokens;
    public int completionTokens;
    public long durationMs;
    public String lastMessagePreview;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public LocalDateTime deletedAt;
}
