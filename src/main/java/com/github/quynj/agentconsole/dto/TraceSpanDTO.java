package com.github.quynj.agentconsole.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TraceSpanDTO {
    public String id;
    public String sessionId;
    public String parentSpanId;
    public String name;
    public String spanType;
    public String status;
    public Object input;
    public Object output;
    public Map<String, Object> metadata = new LinkedHashMap<>();
    public Long durationMs;
    public LocalDateTime startedAt;
    public LocalDateTime endedAt;
    public List<TraceSpanDTO> children = new ArrayList<>();
}
