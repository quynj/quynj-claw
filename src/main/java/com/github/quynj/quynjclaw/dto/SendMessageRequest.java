package com.github.quynj.quynjclaw.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class SendMessageRequest {
    @Size(max = 20000)
    public String text;
    public boolean stream = false;
    public List<MessageAttachmentDTO> attachments = new ArrayList<>();

    @AssertTrue(message = "Message text or attachment is required")
    public boolean hasTextOrAttachment() {
        return (text != null && !text.isBlank()) || (attachments != null && !attachments.isEmpty());
    }

    @AssertTrue(message = "A message can include at most 6 attachments")
    public boolean hasAttachmentLimit() {
        return attachments == null || attachments.size() <= 6;
    }
}
