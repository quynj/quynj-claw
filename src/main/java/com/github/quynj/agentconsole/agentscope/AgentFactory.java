package com.github.quynj.agentconsole.agentscope;

import com.github.quynj.agentconsole.config.AgentConsoleProperties;
import com.github.quynj.agentconsole.dto.ChatSessionDTO;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.Memory;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OllamaChatModel;
import io.agentscope.core.model.OpenAIChatModel;
import org.springframework.stereotype.Component;

@Component
public class AgentFactory {
    private final AgentConsoleProperties properties;
    private final AgentMemoryFactory memoryFactory;

    public AgentFactory(AgentConsoleProperties properties, AgentMemoryFactory memoryFactory) {
        this.properties = properties;
        this.memoryFactory = memoryFactory;
    }

    public ReActAgent createAgent(ChatSessionDTO session) {
        Model model = createModel(session);
        Memory memory = memoryFactory.createMemory(session, model);
        /*
         * Session isolation: every request creates a fresh ReActAgent and a fresh Memory instance.
         * Only AgentScope JsonSession for the current agentscopeSessionId is loaded before call()
         * and saved after call(). UI messages are never replayed into the prompt here.
         */
        return ReActAgent.builder()
                .name(session.agentName)
                .sysPrompt(session.systemPrompt)
                .model(model)
                .memory(memory)
                .build();
    }

    private Model createModel(ChatSessionDTO session) {
        String provider = properties.model.provider == null ? "ollama" : properties.model.provider.toLowerCase();
        String modelName = session.modelName == null || session.modelName.isBlank() ? properties.model.name : session.modelName;
        return switch (provider) {
            case "dashscope" -> {
                requireApiKey("DASHSCOPE_API_KEY", properties.model.apiKey);
                yield DashScopeChatModel.builder()
                        .apiKey(properties.model.apiKey)
                        .modelName(modelName)
                        .build();
            }
            case "openai" -> {
                requireApiKey("OPENAI_API_KEY", properties.model.apiKey);
                yield OpenAIChatModel.builder()
                        .apiKey(properties.model.apiKey)
                        .modelName(modelName)
                        .baseUrl(properties.model.baseUrl)
                        .build();
            }
            case "ollama" -> OllamaChatModel.builder()
                    .modelName(modelName)
                    .baseUrl(properties.model.baseUrl)
                    .build();
            default -> throw new IllegalStateException("Unsupported model provider: " + provider);
        };
    }

    private void requireApiKey(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is not configured.");
        }
    }
}
