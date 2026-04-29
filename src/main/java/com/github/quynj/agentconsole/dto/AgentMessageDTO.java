package com.github.quynj.agentconsole.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AgentMessageDTO {
    public String id;
    public String sessionId;
    public String name;
    public String role;
    public List<ContentBlockDTO> content = new ArrayList<>();
    public Map<String, Object> metadata = new LinkedHashMap<>();
    public JsonNode rawMsg;
    public LocalDateTime createdAt;
}
