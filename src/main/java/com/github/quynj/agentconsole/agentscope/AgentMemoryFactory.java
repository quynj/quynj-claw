package com.github.quynj.agentconsole.agentscope;

import com.github.quynj.agentconsole.config.AgentConsoleProperties;
import com.github.quynj.agentconsole.dto.ChatSessionDTO;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.memory.Memory;
import io.agentscope.core.memory.autocontext.AutoContextConfig;
import io.agentscope.core.memory.autocontext.AutoContextMemory;
import io.agentscope.core.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AgentMemoryFactory {
    private static final Logger log = LoggerFactory.getLogger(AgentMemoryFactory.class);
    private final AgentConsoleProperties properties;

    public AgentMemoryFactory(AgentConsoleProperties properties) {
        this.properties = properties;
    }

    public Memory createMemory(ChatSessionDTO session, Model model) {
        try {
            AutoContextConfig config = AutoContextConfig.builder()
                    .lastKeep(10)
                    .tokenRatio(0.7)
                    .build();
            return new AutoContextMemory(config, model);
        } catch (RuntimeException e) {
            if (!properties.memory.fallbackToInMemory) {
                throw e;
            }
            log.warn("AutoContextMemory initialization failed for session {}. Falling back to InMemoryMemory.", session.id, e);
            // TODO: replace fallback with AgentScope Java AutoContextMemory according to official docs.
            return new InMemoryMemory();
        }
    }
}
