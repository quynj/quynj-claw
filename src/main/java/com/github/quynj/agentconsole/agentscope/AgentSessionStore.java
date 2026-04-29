package com.github.quynj.agentconsole.agentscope;

import com.github.quynj.agentconsole.config.AgentConsoleProperties;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.session.JsonSession;
import io.agentscope.core.session.Session;
import io.agentscope.core.state.SimpleSessionKey;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

@Component
public class AgentSessionStore {
    private static final Pattern SAFE_SESSION_ID = Pattern.compile("^[A-Za-z0-9_-]+$");
    private final AgentConsoleProperties properties;
    private Session session;

    public AgentSessionStore(AgentConsoleProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() throws IOException {
        Path path = Path.of(properties.agentscopeSessionStorePath);
        Files.createDirectories(path);
        session = new JsonSession(path);
    }

    public boolean loadIfExists(ReActAgent agent, String sessionId) {
        validateSessionId(sessionId);
        return agent.loadIfExists(session, sessionId);
    }

    public void save(ReActAgent agent, String sessionId) {
        validateSessionId(sessionId);
        agent.saveTo(session, sessionId);
    }

    public boolean exists(String sessionId) {
        validateSessionId(sessionId);
        return session.exists(SimpleSessionKey.of(sessionId));
    }

    public void delete(String sessionId) {
        validateSessionId(sessionId);
        session.delete(SimpleSessionKey.of(sessionId));
    }

    private void validateSessionId(String sessionId) {
        if (sessionId == null || !SAFE_SESSION_ID.matcher(sessionId).matches()
                || sessionId.contains("..") || sessionId.contains("/") || sessionId.contains("\\")) {
            throw new IllegalArgumentException("Unsafe AgentScope session id");
        }
    }
}
