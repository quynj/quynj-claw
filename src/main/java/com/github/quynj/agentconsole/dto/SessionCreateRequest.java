package com.github.quynj.agentconsole.dto;

import jakarta.validation.constraints.Size;

public class SessionCreateRequest {
    @Size(max = 120)
    public String title;
    @Size(max = 80)
    public String agentName;
    @Size(max = 120)
    public String modelName;
    @Size(max = 4000)
    public String systemPrompt;
    public Double temperature;
}
