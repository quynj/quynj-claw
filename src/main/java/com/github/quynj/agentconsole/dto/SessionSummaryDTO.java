package com.github.quynj.agentconsole.dto;

import java.time.LocalDateTime;

public class SessionSummaryDTO {
    public String sessionId;
    public String title;
    public String agentName;
    public String status;
    public int messageCount;
    public int traceCount;
    public int totalTokens;
    public int promptTokens;
    public int completionTokens;
    public long durationMs;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
