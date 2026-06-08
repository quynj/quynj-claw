package com.github.quynj.quynjclaw.application;

import com.github.quynj.quynjclaw.dto.AgentMessageDTO;
import org.springframework.stereotype.Service;

@Service
public class SessionTitleGenerator {

    private static final String DEFAULT_TITLE = "New Chat";
    private static final int MAX_TITLE_LENGTH = 30;

    public String generateFromUserMessage(AgentMessageDTO message) {
        if (message == null || message.content == null || message.content.isEmpty()) {
            return DEFAULT_TITLE;
        }

        String text = extractText(message);
        if (text == null || text.isBlank()) {
            return DEFAULT_TITLE;
        }

        return generateTitle(text);
    }

    private String generateTitle(String text) {
        text = text.trim();

        if (text.length() <= MAX_TITLE_LENGTH) {
            return capitalize(text);
        }

        return capitalize(text.substring(0, MAX_TITLE_LENGTH)) + "...";
    }

    private String extractText(AgentMessageDTO message) {
        return message.content.stream()
                .map(block -> block.text != null ? block.text : block.message)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        text = text.trim();
        if (text.isEmpty()) {
            return text;
        }

        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}