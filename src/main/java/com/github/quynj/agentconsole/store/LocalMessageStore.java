package com.github.quynj.agentconsole.store;

import com.github.quynj.agentconsole.common.JsonFileUtils;
import com.github.quynj.agentconsole.config.AgentConsoleProperties;
import com.github.quynj.agentconsole.dto.AgentMessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class LocalMessageStore {
    private final AgentConsoleProperties properties;
    private final JsonFileUtils json;
    private Path dir;

    public LocalMessageStore(AgentConsoleProperties properties, ObjectMapper mapper) {
        this.properties = properties;
        this.json = new JsonFileUtils(mapper);
    }

    @PostConstruct
    public synchronized void init() throws IOException {
        dir = Path.of(properties.uiStorePath, "messages");
        Files.createDirectories(dir);
    }

    public synchronized void initSession(String sessionId) {
        json.writeAtomic(path(sessionId), new Document(sessionId));
    }

    public synchronized List<AgentMessageDTO> listMessages(String sessionId) {
        return read(sessionId).items;
    }

    public synchronized AgentMessageDTO append(String sessionId, AgentMessageDTO message) {
        Document doc = read(sessionId);
        doc.items.add(message);
        json.writeAtomic(path(sessionId), doc);
        return message;
    }

    public synchronized Optional<AgentMessageDTO> getMessage(String messageId) {
        try (var paths = Files.list(dir)) {
            return paths.filter(path -> path.toString().endsWith(".json"))
                    .map(path -> json.read(path, Document.class, new Document()))
                    .flatMap(doc -> doc.items.stream())
                    .filter(item -> Objects.equals(item.id, messageId))
                    .findFirst();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan messages", e);
        }
    }

    public synchronized void deleteBySessionId(String sessionId) {
        try {
            Files.deleteIfExists(path(sessionId));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete messages for " + sessionId, e);
        }
    }

    private Document read(String sessionId) {
        return json.read(path(sessionId), Document.class, new Document(sessionId));
    }

    private Path path(String sessionId) {
        return dir.resolve(sessionId + ".json");
    }

    public static class Document {
        public String sessionId;
        public List<AgentMessageDTO> items = new ArrayList<>();

        public Document() {
        }

        public Document(String sessionId) {
            this.sessionId = sessionId;
        }
    }
}
