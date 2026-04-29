package com.github.quynj.agentconsole.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SendMessageRequest {
    @NotBlank
    @Size(max = 20000)
    public String text;
    public boolean stream = false;
}
